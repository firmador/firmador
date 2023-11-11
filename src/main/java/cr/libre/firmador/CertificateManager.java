package cr.libre.firmador;

import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import eu.europa.esig.dss.model.x509.CertificateToken;
import eu.europa.esig.dss.spi.DSSUtils;
import eu.europa.esig.dss.spi.x509.CertificateSource;
import eu.europa.esig.dss.spi.x509.CommonCertificateSource;
import eu.europa.esig.dss.spi.x509.CommonTrustedCertificateSource;

public class CertificateManager {
    private static final int MAXDEPTH = 20;
    private CertificateSource certSource = new CommonCertificateSource();


    public CertificateManager() {
        ClassLoader loaderclass = this.getClass().getClassLoader();
        certSource.addCertificate(DSSUtils.loadCertificate(
                loaderclass.getResourceAsStream("certs/CA POLITICA PERSONA FISICA - COSTA RICA v2.crt")));
        certSource.addCertificate(DSSUtils.loadCertificate(
                loaderclass.getResourceAsStream("certs/CA POLITICA PERSONA JURIDICA - COSTA RICA v2.crt")));
        certSource.addCertificate(DSSUtils.loadCertificate(
                loaderclass.getResourceAsStream("certs/CA POLITICA SELLADO DE TIEMPO - COSTA RICA v2.crt")));
        certSource.addCertificate(DSSUtils.loadCertificate(
                loaderclass.getResourceAsStream("certs/CA SINPE - PERSONA FISICA v2.cer")));
        certSource.addCertificate(DSSUtils.loadCertificate(
                loaderclass.getResourceAsStream("certs/CA SINPE - PERSONA FISICA v2(1).crt")));
        certSource.addCertificate(DSSUtils.loadCertificate(
                loaderclass.getResourceAsStream("certs/CA SINPE - PERSONA FISICA v2(2).crt")));
        certSource.addCertificate(DSSUtils.loadCertificate(
                loaderclass.getResourceAsStream("certs/CA SINPE - PERSONA JURIDICA v2.cer")));
        certSource.addCertificate(DSSUtils.loadCertificate(
                loaderclass.getResourceAsStream("certs/CA SINPE - PERSONA JURIDICA v2(1).crt")));
        certSource.addCertificate(DSSUtils.loadCertificate(
                loaderclass.getResourceAsStream("certs/CA SINPE - PERSONA JURIDICA v2(2).crt")));
        certSource.addCertificate(DSSUtils
                .loadCertificate(loaderclass.getResourceAsStream("certs/TSA SINPE v2.cer")));
        certSource.addCertificate(DSSUtils.loadCertificate(
                loaderclass.getResourceAsStream("certs/CA RAIZ NACIONAL - COSTA RICA v2.crt")));
        certSource.addCertificate(DSSUtils.loadCertificate(
                loaderclass.getResourceAsStream("certs/CA RAIZ NACIONAL COSTA RICA.cer")));

    }

    public CertificateSource getCertificateChainCertificateSource(CertificateToken subjectCertificate) {
        List<CertificateToken> certs = this.getCertificateChain(subjectCertificate);
        CertificateSource certificatesource = null;
        if (certs != null) {
            certificatesource = new CommonCertificateSource();
            for (CertificateToken cert : certs) {
                certificatesource.addCertificate(cert);
            }
        }
        return certificatesource;
    }

    public List<CertificateToken> getCertificateChain(CertificateToken subjectCertificate) {
        /**
         * Return a certificate chain of certificate emitted by trusted CR CA. If
         * Certificate is not a CR certificate return Null.
         */
        int counter = 0;

        List<CertificateToken> certchain = new ArrayList<CertificateToken>();
        Set<CertificateToken> ct = certSource.getBySubject(subjectCertificate.getIssuer());
        CertificateToken currentCert = subjectCertificate;
        while (currentCert != null && !ct.isEmpty() && counter < this.MAXDEPTH) {
            counter += 1; // prevent infinite cycles
            for (CertificateToken c : ct) {
                if (currentCert.isSignedBy(c)) {
                    currentCert = c;
                    if (!certchain.contains(currentCert)) {
                        certchain.add(0, currentCert);
                        ct = certSource.getBySubject(currentCert.getIssuer());
                        if (ct.isEmpty()) {
                            currentCert = null;
                        }
                        break;
                    } else {
                        currentCert = null; // Root CA is self-signed so this break recursion
                    }
                }
            }
        }
        if (certchain.isEmpty())
            return null;

        return certchain;
    }

    public List<X509Certificate> getX509CertificateChain(CertificateToken subjectCertificate) {
        List<X509Certificate> certlist = null;
        List<CertificateToken> certs = this.getCertificateChain(subjectCertificate);
        if (certs != null) {
            certlist = new ArrayList<X509Certificate>();
            for (CertificateToken cert : certs) {
                certlist.add(cert.getCertificate());
            }
        }
        return certlist;
    }

    public CommonTrustedCertificateSource getTrustedCertificateSource(CertificateToken subjectCertificate) {
        CommonTrustedCertificateSource commonTrustedCertificateSource = null;
        List<CertificateToken> certs = this.getCertificateChain(subjectCertificate);
        if (certs != null && certs.size() > 0) {
            commonTrustedCertificateSource = new CommonTrustedCertificateSource();
            commonTrustedCertificateSource.addCertificate(certs.get(0));
        }
        return commonTrustedCertificateSource;
    }

    public CertificateSource getAdjunctCertSource(CertificateToken subjectCertificate) {
        CertificateSource adjunctCertSource = null;
        List<CertificateToken> certs = this.getCertificateChain(subjectCertificate);
        if (certs != null && certs.size() > 0) {
            adjunctCertSource = new CommonCertificateSource();
            certs.remove(0); // Remove Root CA certificate, this would by on TrustedCertificateSource
            for (CertificateToken cert : certs) {
                adjunctCertSource.addCertificate(cert);
            }
        }

        return adjunctCertSource;
    }
}
