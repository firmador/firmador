package app.firmador;

import java.security.KeyStore.PasswordProtection;
import java.util.ArrayList;
import java.util.List;

import com.google.common.base.Throwables;

import app.firmador.gui.GUIInterface;
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


public class CRSigner {
	public static final String TSA_URL = "http://tsa.sinpe.fi.cr/tsaHttp/";
	protected GUIInterface gui;
	
	public CRSigner(GUIInterface gui){
		this.gui = gui;
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
        String pkcs11lib = "";
        String osName = System.getProperty("os.name").toLowerCase();
        if (osName.contains("mac")) {
            pkcs11lib = "/Library/Application Support/Athena/libASEP11.dylib";
        } else if (osName.contains("linux")) {
            pkcs11lib = "/usr/lib/x64-athena/libASEP11.so";
        } else if (osName.contains("windows")) {
            pkcs11lib = System.getenv("SystemRoot")
                + "\\System32\\asepkcs.dll";
        }
        return pkcs11lib;
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
		String pkcs11lib = getPkcs11Lib();
    	SignatureTokenConnection signingToken = new Pkcs11SignatureToken(
                pkcs11lib, pin);
    	return signingToken;
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
	
}
