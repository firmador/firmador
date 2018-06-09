package app.firmador;

import java.io.IOException;
import java.security.KeyStore.PasswordProtection;
import java.util.ArrayList;
import java.util.List;

import com.google.common.base.Throwables;

import app.firmador.gui.GUIInterface;
import app.tarjeta.TarjetaPkcs11;
import app.tarjeta.Utils;
import eu.europa.esig.dss.AbstractSignatureParameters;
import eu.europa.esig.dss.DSSUtils;
import eu.europa.esig.dss.client.crl.OnlineCRLSource;
import eu.europa.esig.dss.client.http.commons.CommonsDataLoader;
import eu.europa.esig.dss.client.ocsp.OnlineOCSPSource;
import eu.europa.esig.dss.token.DSSPrivateKeyEntry;
import eu.europa.esig.dss.token.Pkcs11SignatureToken;
import eu.europa.esig.dss.token.SignatureTokenConnection;
import eu.europa.esig.dss.tsl.KeyUsageBit;
import eu.europa.esig.dss.validation.CertificateVerifier;
import eu.europa.esig.dss.validation.CommonCertificateVerifier;
import eu.europa.esig.dss.x509.CertificateToken;
import sun.security.pkcs11.wrapper.PKCS11Exception;


public class CRSigner {
	public static final String TSA_URL = "http://tsa.sinpe.fi.cr/tsaHttp/";
	protected GUIInterface gui;
	public int selectedSlot;
	
	public CRSigner(GUIInterface gui){
		this.gui = gui;
		selectedSlot = -1;
	}
	
    protected DSSPrivateKeyEntry getPrivateKey(SignatureTokenConnection signingToken){
        /*
         * Usa la primera llave cuyo uso es no repudio, asumiendo que no hay
         * más llaves con el mismo propósito en el mismo token.
         * Esto debería funcionar bien con tarjetas de Firma Digital no
         * manipuladas pero en el futuro sería conveniente comprobar que
         * no hay ningún caso extremo y verificar que verdaderamente se trata
         * de la única y permitir elegir cuál usar. FIXME.
         */
    	

    	
    	DSSPrivateKeyEntry privateKey = null;
        try {
            for (DSSPrivateKeyEntry candidatePrivateKey
                : signingToken.getKeys()) {
                if (candidatePrivateKey.getCertificate().checkKeyUsage(
                    KeyUsageBit.nonRepudiation)) {
                        privateKey = candidatePrivateKey;
                }
            }
        } catch (Exception|Error e) {
            gui.showError(Throwables.getRootCause(e));
        }
        return privateKey;
    }
	
	private String getPkcs11Lib(){
		return Utils.getPKCS11Lib();
	}
	
	
	public SignatureTokenConnection get_signatureConnection(PasswordProtection pin){
        /*
         * ATENCIÓN: Se asume que solamente hay un token conectado.
         * Si no es el caso, podría intentar usar el PIN de otro dispositivo
         * y si no se verifica podría bloquearse por reintentos fallidos.
         * En el futuro deberían recorrerse todos los certificados encontrados.
         * FIXME.
         * Más en el futuro debería soportar otros mecanismos de acceso a
         * PKCS#11 específicos de cada sistema operativo, en busca de otros
         * fabricantes que no sean Athena/NXP (para sello electrónico).
         */
		SignatureTokenConnection signingToken = null;
		if( this.selectedSlot != -1 ){
			signingToken = new Pkcs11SignatureToken(
					getPkcs11Lib(), pin, (int)selectedSlot);
		}else{
			TarjetaPkcs11 tarjeta = new TarjetaPkcs11();
			long[] slots = null;

			try {
				slots = tarjeta.getSlots();
			}catch (Exception|Error e) {
		            gui.showError(Throwables.getRootCause(e));
		    }
			if(slots!=null || slots.length>0){
				if(slots.length==1){
					signingToken = new Pkcs11SignatureToken(
						getPkcs11Lib(), pin);
				}else{
					selectedSlot=getSelectedSlot(tarjeta, slots);
					signingToken = new Pkcs11SignatureToken(
							getPkcs11Lib(), pin, selectedSlot);
				}
			}

		}
    	return signingToken;
    }
	
	public void selectSlot(){
		TarjetaPkcs11 tarjeta = new TarjetaPkcs11();
		long[] slots = null;

		try {
			slots = tarjeta.getSlots();
		}catch (Exception|Error e) {
	            gui.showError(Throwables.getRootCause(e));
	    }
		if(slots.length==1){
			selectedSlot=0;
		}else{
			selectedSlot=getSelectedSlot(tarjeta, slots);
		}
	}
	
	private int getSelectedSlot(TarjetaPkcs11 tarjeta, long[] slots) {
		String[] propietarios = new String[slots.length];
		for(int x=0; x<slots.length; x++){
			// Fixme: podria tener null y provocar null pointers exception
			propietarios[x] = tarjeta.getPropietario(slots[x]);
		}
		return gui.getSelection(propietarios);
	}

	public CertificateVerifier getCertificateVerifier(){
        CommonCertificateVerifier commonCertificateVerifier = 
        		new CommonCertificateVerifier();
                       
        // CRLs
        CommonsDataLoader commonsHttpDataLoader = new CommonsDataLoader();
        OnlineCRLSource onlineCRLSource = new OnlineCRLSource();
        onlineCRLSource.setDataLoader(commonsHttpDataLoader);
        
        // OSCP
        commonCertificateVerifier.setCrlSource(onlineCRLSource);
        OnlineOCSPSource onlineOCSPSource = new OnlineOCSPSource();
        onlineOCSPSource.setDataLoader(commonsHttpDataLoader);
        commonCertificateVerifier.setOcspSource(onlineOCSPSource);
	   return commonCertificateVerifier;
	}
	
	public  List<CertificateToken>  getCertificateChain(CertificateVerifier commonCertificateVerifier,
			AbstractSignatureParameters parameters){
        List<CertificateToken> certificateChain =
                new ArrayList<CertificateToken>();
       
        List<CertificateToken> cert = new ArrayList<CertificateToken>(
            DSSUtils.loadPotentialIssuerCertificates(
                parameters.getSigningCertificate(),
                commonCertificateVerifier.getDataLoader()));
        certificateChain.add(cert.get(0));

        do {
            cert = new ArrayList<CertificateToken>(
                DSSUtils.loadPotentialIssuerCertificates(cert.get(0),
                    commonCertificateVerifier.getDataLoader()));
            if (!cert.isEmpty()) {
                certificateChain.add(cert.get(0));
            }
        } while (!cert.isEmpty());	
        return certificateChain;
	}
	
	public void setSlot(int slot){
		this.selectedSlot=slot;
	}
}
