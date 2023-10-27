package cr.libre.firmador.oxml;

import java.lang.invoke.MethodHandles;
import java.security.cert.X509Certificate;
import java.util.List;

import org.apache.poi.poifs.crypt.dsig.SignatureConfig;
import org.apache.poi.poifs.crypt.dsig.SignatureInfo;
import org.apache.poi.poifs.crypt.dsig.services.RevocationData;
import org.apache.poi.poifs.crypt.dsig.services.TSPTimeStampService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    CardManagerInterface cardmanager;
    private CertificateVerifier verifier;

    public TimeStampServiceCR(CardManagerInterface cardmanager, CertificateVerifier verifier) {
        try {
            this.certchain = cardmanager.getCertificateChainTSA();
        } catch (Throwable e) {
            LOG.error("Error obteniendo la cadena de certificados de sello en el tiempo", e);
        }
        this.cardmanager = cardmanager;
        this.verifier = verifier;
	}
	
    @Override
    public byte[] timeStamp(SignatureInfo signatureInfo, byte[] data, RevocationData revocationData) throws Exception {
		byte[] datastamped = super.timeStamp(signatureInfo, data,  revocationData);

        X509Certificate certificate;
        List<X509Certificate> revchain;
        SignatureConfig signatureConfig = signatureInfo.getSignatureConfig();
        AddTPSRevocation(signatureConfig, revocationData);
        List<X509Certificate> revocationchain = revocationData.getX509chain();
        if(revocationchain.size()>0) {
            certificate = revocationchain.get(0);
            try {
                revchain = this.cardmanager.getCertificateChain(certificate);
                for (X509Certificate c : revchain)
                    revocationData.addCertificate(c);
            } catch (Throwable e) {
                LOG.warn("Error identificando la cadena de certificados la cadena de certificados", e);
            }
        }else {
            LOG.warn("La lista de revocación debería tener almenos un certificado");
        }

        return datastamped;
    }

    public void AddTPSRevocation(SignatureConfig signatureConfig, RevocationData revocationData) {

        List<X509Certificate> chain = signatureConfig.getSigningCertificateChain();

        RevocationSource<OCSP> oscpsource = verifier.getOcspSource();
        RevocationToken<OCSP> token = oscpsource.getRevocationToken(new CertificateToken(chain.get(0)),
                new CertificateToken(chain.get(1)));
        revocationData.addOCSP(token.getEncoded());
        RevocationSource<CRL> crlsource = verifier.getCrlSource();

        CertificateToken certificate;
        CertificateToken issuerCertificate;
        RevocationToken<CRL> crltoken;

        for (int pos = certchain.size() - 1; pos > 0; pos--) {
            certificate = new CertificateToken(certchain.get(pos));
            issuerCertificate = new CertificateToken(certchain.get(pos - 1));
            try {
                crltoken = crlsource.getRevocationToken(certificate, issuerCertificate);
                revocationData.addCRL(crltoken.getEncoded());
            } catch (Throwable e) {
                LOG.warn("No se encontró CRL para el certificado " + certificate.toString(), e);
            }
        }
    }

    public void AddCertificateRevocation(SignatureConfig signatureConfig, RevocationData revocationData)
            throws Throwable {
        RevocationSource<CRL> crlsource = verifier.getCrlSource();
        CertificateToken certificate;
        CertificateToken issuerCertificate;
        certificate = new CertificateToken(this.cardmanager.getCertByCN("CA SINPE - PERSONA FISICA v2"));
        issuerCertificate = new CertificateToken(
                this.cardmanager.getCertByCN("CA POLITICA PERSONA FISICA - COSTA RICA v2"));
        RevocationToken<CRL> crltoken;
        try {
            crltoken = crlsource.getRevocationToken(certificate, issuerCertificate);
            revocationData.addCRL(crltoken.getEncoded());

        } catch (Throwable e) {
            LOG.warn("No se encontró CRL para el certificado " + certificate.toString(), e);
        }
        certificate = new CertificateToken(
                this.cardmanager.getCertByCN("CA POLITICA PERSONA FISICA - COSTA RICA v2"));
        issuerCertificate = new CertificateToken(this.cardmanager.getCertByCN("CA RAIZ NACIONAL - COSTA RICA v2"));

        try {
            crltoken = crlsource.getRevocationToken(certificate, issuerCertificate);
            revocationData.addCRL(crltoken.getEncoded());
        } catch (Throwable e) {
            LOG.warn("No se encontró CRL para el certificado " + certificate.toString(), e);
        }
        List<X509Certificate> chain = signatureConfig.getSigningCertificateChain();
        RevocationSource<OCSP> oscpsource = verifier.getOcspSource();
        RevocationToken<OCSP> token = oscpsource.getRevocationToken(new CertificateToken(chain.get(0)),
                new CertificateToken(chain.get(1)));
        revocationData.addOCSP(token.getEncoded());
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
