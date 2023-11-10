package cr.libre.firmador;

import java.util.List;

import eu.europa.esig.dss.model.x509.CertificateToken;
import eu.europa.esig.dss.spi.DSSUtils;
import eu.europa.esig.dss.spi.x509.CertificateSource;
import eu.europa.esig.dss.spi.x509.CommonCertificateSource;

public class CertificateManager {
    private CertificateSource certSource = new CommonCertificateSource();

    public CertificateManager() {
        certSource.addCertificate(DSSUtils.loadCertificate(this.getClass().getClassLoader()
                .getResourceAsStream("certs/CA POLITICA PERSONA FISICA - COSTA RICA v2.crt")));
        certSource.addCertificate(DSSUtils.loadCertificate(this.getClass().getClassLoader()
                .getResourceAsStream("certs/CA POLITICA PERSONA JURIDICA - COSTA RICA v2.crt")));
        certSource.addCertificate(DSSUtils.loadCertificate(this.getClass().getClassLoader()
                .getResourceAsStream("certs/CA POLITICA SELLADO DE TIEMPO - COSTA RICA v2.crt")));
        certSource.addCertificate(DSSUtils.loadCertificate(
                this.getClass().getClassLoader().getResourceAsStream("certs/CA SINPE - PERSONA FISICA v2.cer")));
        certSource.addCertificate(DSSUtils.loadCertificate(
                this.getClass().getClassLoader().getResourceAsStream("certs/CA SINPE - PERSONA FISICA v2(1).crt")));
        certSource.addCertificate(DSSUtils.loadCertificate(
                this.getClass().getClassLoader().getResourceAsStream("certs/CA SINPE - PERSONA FISICA v2(2).crt")));
        certSource.addCertificate(DSSUtils.loadCertificate(
                this.getClass().getClassLoader().getResourceAsStream("certs/CA SINPE - PERSONA JURIDICA v2.cer")));
        certSource.addCertificate(DSSUtils.loadCertificate(
                this.getClass().getClassLoader().getResourceAsStream("certs/CA SINPE - PERSONA JURIDICA v2(1).crt")));
        certSource.addCertificate(DSSUtils.loadCertificate(
                this.getClass().getClassLoader().getResourceAsStream("certs/CA SINPE - PERSONA JURIDICA v2(2).crt")));
        certSource.addCertificate(DSSUtils
                .loadCertificate(this.getClass().getClassLoader().getResourceAsStream("certs/TSA SINPE v2.cer")));
        certSource.addCertificate(DSSUtils.loadCertificate(
                this.getClass().getClassLoader().getResourceAsStream("certs/CA RAIZ NACIONAL - COSTA RICA v2.crt")));
        certSource.addCertificate(DSSUtils.loadCertificate(
                this.getClass().getClassLoader().getResourceAsStream("certs/CA RAIZ NACIONAL COSTA RICA.cer")));

    }

    public CertificateSource getCertificateChainCertificateSource() {
        return null;
    }

    public List<CertificateToken> getCertificateChain(CertificateToken subjectCertificate) {
        /**
         * Return a certificate chain of certificate emitted by trusted CR CA. If
         * Certificate is not a CR certificate return Null.
         */

        return null;
    }

}
