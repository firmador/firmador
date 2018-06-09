package app.firmador;

import java.security.KeyStore.PasswordProtection;
import java.util.List;

import com.google.common.base.Throwables;

import app.firmador.gui.GUIInterface;
import eu.europa.esig.dss.DSSDocument;
import eu.europa.esig.dss.DigestAlgorithm;
import eu.europa.esig.dss.SignatureLevel;
import eu.europa.esig.dss.SignatureValue;
import eu.europa.esig.dss.ToBeSigned;
import eu.europa.esig.dss.client.tsp.OnlineTSPSource;
import eu.europa.esig.dss.pades.PAdESSignatureParameters;
import eu.europa.esig.dss.pades.signature.PAdESService;
import eu.europa.esig.dss.token.DSSPrivateKeyEntry;
import eu.europa.esig.dss.token.SignatureTokenConnection;
import eu.europa.esig.dss.validation.CertificateVerifier;
import eu.europa.esig.dss.x509.CertificateToken;

public class FirmadorPDF extends CRSigner{

	 public FirmadorPDF(GUIInterface gui) {
		super(gui);
		// TODO Auto-generated constructor stub
	}

	private DSSDocument _sign(DSSDocument toSignDocument, PasswordProtection pin){
		 
		CertificateVerifier commonCertificateVerifier = this.getCertificateVerifier();
		SignatureTokenConnection signingToken = get_signatureConnection(pin);
		DSSPrivateKeyEntry privateKey = getPrivateKey(signingToken);
		PAdESSignatureParameters parameters = new PAdESSignatureParameters();
		
		parameters.setSignatureLevel(SignatureLevel.PAdES_BASELINE_LTA);
		parameters.setSignatureSize(13312);
		parameters.setDigestAlgorithm(DigestAlgorithm.SHA256);
		parameters.setSigningCertificate(privateKey.getCertificate());
		
        List<CertificateToken> certificateChain = getCertificateChain(
        		commonCertificateVerifier, parameters);
        parameters.setCertificateChain(certificateChain);
        
        PAdESService service = new PAdESService(commonCertificateVerifier);

        OnlineTSPSource onlineTSPSource = new OnlineTSPSource(TSA_URL);
        service.setTspSource(onlineTSPSource);

        ToBeSigned dataToSign = service.getDataToSign(toSignDocument,
            parameters);

        DigestAlgorithm digestAlgorithm = parameters.getDigestAlgorithm();
        SignatureValue signatureValue = signingToken.sign(dataToSign,
            digestAlgorithm, privateKey);

        DSSDocument signedDocument = service.signDocument(toSignDocument,
            parameters, signatureValue);

		return signedDocument;
	 }
	public DSSDocument sign(DSSDocument toSignDocument, PasswordProtection pin){
		DSSDocument dev = null;
		try{
			dev = _sign(toSignDocument, pin);
		} catch (Exception|Error e) {
			gui.showError(Throwables.getRootCause(e));
		}
		return dev;
	}
	
}
