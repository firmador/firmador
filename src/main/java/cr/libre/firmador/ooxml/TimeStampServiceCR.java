package cr.libre.firmador.ooxml;

import java.lang.invoke.MethodHandles;
import java.security.cert.X509Certificate;
import java.util.List;

import org.apache.poi.poifs.crypt.dsig.SignatureConfig;
import org.apache.poi.poifs.crypt.dsig.SignatureInfo;
import org.apache.poi.poifs.crypt.dsig.services.RevocationData;
import org.apache.poi.poifs.crypt.dsig.services.TSPTimeStampService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cr.libre.firmador.CertificateManager;
import cr.libre.firmador.cards.CardManagerInterface;
import eu.europa.esig.dss.model.x509.CertificateToken;
import eu.europa.esig.dss.model.x509.revocation.crl.CRL;
import eu.europa.esig.dss.model.x509.revocation.ocsp.OCSP;
import eu.europa.esig.dss.service.ocsp.OnlineOCSPSource;
import eu.europa.esig.dss.spi.x509.revocation.RevocationSource;
import eu.europa.esig.dss.spi.x509.revocation.RevocationToken;
import eu.europa.esig.dss.validation.CertificateVerifier;

public class TimeStampServiceCR extends TSPTimeStampService {
    final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	private List<X509Certificate> certchain;

    private CertificateVerifier verifier;
    private CertificateManager certManager;

    public TimeStampServiceCR(CardManagerInterface cardmanager, CertificateVerifier verifier) {
        certManager = new CertificateManager();
        try {
            this.certchain = certManager.getCertificateChainTSA();
        } catch (Throwable e) {
            LOG.error("Error obteniendo la cadena de certificados de sello en el tiempo", e);
        }
        this.verifier = verifier;
	}
	
    @Override
    public byte[] timeStamp(SignatureInfo signatureInfo, byte[] data, RevocationData revocationData) throws Exception {
		byte[] datastamped = super.timeStamp(signatureInfo, data,  revocationData);

        X509Certificate certificate;

        SignatureConfig signatureConfig = signatureInfo.getSignatureConfig();
        AddTPSRevocation(signatureConfig, revocationData);
        List<X509Certificate> revocationchain = revocationData.getX509chain();
        if (revocationchain.size() > 0) {
            certificate = revocationchain.get(0);
            try {
                List<CertificateToken> revchain = certManager.getCertificateChain(new CertificateToken(certificate));
                for (CertificateToken c : revchain)
                    revocationData.addCertificate(c.getCertificate());
            } catch (Throwable e) {
                LOG.warn("Error identificando la cadena de certificados la cadena de certificados", e);
            }
        } else {
            LOG.warn("La lista de revocación debería tener almenos un certificado");
        }

        return datastamped;
    }

    public void AddTPSRevocation(SignatureConfig signatureConfig, RevocationData revocationData) {

        List<X509Certificate> chain = signatureConfig.getSigningCertificateChain();
        CertificateToken certificate = new CertificateToken(chain.get(0));

        RevocationSource<OCSP> oscpsource = verifier.getOcspSource();
        RevocationToken<OCSP> oscptoken;
        RevocationSource<CRL> crlsource = verifier.getCrlSource();
        RevocationToken<CRL> crltoken;
        CertificateToken issuerCertificate;

        for (int pos = certchain.size() - 1; pos > 0; pos--) {
            certificate = new CertificateToken(certchain.get(pos));
            issuerCertificate = new CertificateToken(certchain.get(pos - 1));
            try {
                crltoken = crlsource.getRevocationToken(certificate, issuerCertificate);
                revocationData.addCRL(crltoken.getEncoded());
            } catch (Throwable e) {
                LOG.warn("No se encontró CRL para el certificado " + certificate.toString(), e);
            }
            oscptoken = oscpsource.getRevocationToken(certificate, new CertificateToken(chain.get(1)));
            if (oscptoken != null)
                revocationData.addOCSP(oscptoken.getEncoded());
        }
    }

    public void AddCertificateRevocation(SignatureConfig signatureConfig, RevocationData revocationData)
            throws Throwable {
        CertificateToken certificate = new CertificateToken(signatureConfig.getSigningCertificateChain().get(0));
        List<CertificateToken> chain = certManager.getCertificateChain(certificate);
        CertificateToken issuerCertificateOscp = chain.get(0);
        CertificateToken certificateOscp = certificate;
        RevocationSource<CRL> crlsource = verifier.getCrlSource();
        RevocationSource<OCSP> oscpsource = verifier.getOcspSource();
        RevocationToken<CRL> crltoken;

        CertificateToken issuerCertificate;
        RevocationToken<OCSP> oscptoken;
        certificate = chain.remove(0);
        while (!chain.isEmpty()) {
            issuerCertificate = chain.remove(0); // chain.remove(chain.size() - 1);
            crltoken = crlsource.getRevocationToken(certificate, issuerCertificate);
            if (crltoken != null)
                revocationData.addCRL(crltoken.getEncoded());
            certificate = issuerCertificate;
        }
        oscptoken = oscpsource.getRevocationToken(certificateOscp, issuerCertificateOscp);
        if (oscptoken != null) {
            revocationData.addOCSP(oscptoken.getEncoded());
        }
    }


    public void createOcspResp(X509Certificate certificate, X509Certificate issuerCertificate,
            RevocationData revocationData) throws Throwable {

        CertificateToken certToken = new CertificateToken(certificate);
        CertificateToken issuerToken = new CertificateToken(issuerCertificate);
        OnlineOCSPSource oos = new OnlineOCSPSource();
        final RevocationToken<?> onlineRevocationToken = oos.getRevocationToken(certToken, issuerToken);

        byte[] data = onlineRevocationToken.getEncoded();
        if (data != null)
            revocationData.addOCSP(data);

    }
}
