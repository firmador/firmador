package cr.libre.firmador.cards;

import java.io.InputStream;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.security.auth.x500.X500Principal;

public class CertificateBaseManager {
    protected Map<String, X509Certificate> certificateMap = new HashMap<>();
    private void addCertificateToMap(X509Certificate certificate) {

        certificateMap.put(extractCommonName(certificate.getSubjectX500Principal().getName()), certificate);
    }

    private void loadCertificateCache() throws Throwable {

        if (certificateMap.isEmpty()) {
            addCertificateToMap(parseX509("certs/CA RAIZ NACIONAL - COSTA RICA v2.crt"));
            addCertificateToMap(parseX509("certs/CA RAIZ NACIONAL COSTA RICA.cer"));
            addCertificateToMap(parseX509("certs/CA POLITICA PERSONA FISICA - COSTA RICA v2.crt"));
            addCertificateToMap(parseX509("certs/CA POLITICA PERSONA JURIDICA - COSTA RICA v2.crt"));
            addCertificateToMap(parseX509("certs/CA POLITICA SELLADO DE TIEMPO - COSTA RICA v2.crt"));
            // addCertificateToMap(parseX509("certs/CA SINPE - PERSONA FISICA v2.cer"));
            addCertificateToMap(parseX509("certs/CA SINPE - PERSONA FISICA v2(1).crt"));
            // addCertificateToMap(parseX509("certs/CA SINPE - PERSONA FISICA v2(2).crt"));
            addCertificateToMap(parseX509("certs/CA SINPE - PERSONA JURIDICA v2.cer"));
            addCertificateToMap(parseX509("certs/CA SINPE - PERSONA JURIDICA v2(1).crt"));
            addCertificateToMap(parseX509("certs/CA SINPE - PERSONA JURIDICA v2(2).crt"));
            addCertificateToMap(parseX509("certs/TSA SINPE v2.cer"));
        }

    }

    public List<X509Certificate> getCertificateChain(X509Certificate certificate) throws Throwable {
        loadCertificateCache();
        X500Principal issuerPrincipal;
        String issuerSerialNumber;
        X509Certificate foundCertificate = null;
        X509Certificate actualCertificate = certificate;
        String actualSerialNumber;
        List<X509Certificate> certchain = new ArrayList<>();
        while (actualCertificate != null) {
            certchain.add(actualCertificate);
            actualSerialNumber = extractCommonName(actualCertificate.getSubjectX500Principal().getName());
            issuerPrincipal = actualCertificate.getIssuerX500Principal();
            issuerSerialNumber = extractCommonName(issuerPrincipal.getName());
            foundCertificate = certificateMap.get(issuerSerialNumber);
            if (!actualSerialNumber.contentEquals(issuerSerialNumber)) {
                actualCertificate = foundCertificate;
            } else {
                actualCertificate = null;
            }
        }

        return certchain;
    }

    private static String extractCommonName(String subjectName) {
        String[] parts = subjectName.split(",");
        for (String part : parts) {
            String[] keyValue = part.trim().split("=");
            if (keyValue.length == 2 && keyValue[0].equalsIgnoreCase("CN")) {
                return keyValue[1];
            }
        }
        return null;
    }

    public X509Certificate getCertByCN(String cn) {
        return certificateMap.get(cn);
    }

    protected X509Certificate parseX509(String dataname) throws CertificateException {
        InputStream data = this.getClass().getClassLoader().getResourceAsStream(dataname);
        CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
        return (X509Certificate) certificateFactory.generateCertificate(data);
    }

    public List<X509Certificate> getCertificateChainTSA() throws CertificateException {
        List<X509Certificate> certlist = new ArrayList<X509Certificate>();
        certlist.add(parseX509("certs/CA RAIZ NACIONAL - COSTA RICA v2.crt"));
        // certlist.add(parseX509("certs/CA RAIZ NACIONAL COSTA RICA.cer"));
        certlist.add(parseX509("certs/CA POLITICA SELLADO DE TIEMPO - COSTA RICA v2.crt"));
        certlist.add(parseX509("certs/TSA SINPE v2.cer"));

        return certlist;
    }

    protected void fillCAcertificates(List<X509Certificate> certlist) throws Throwable {
        certlist.add(parseX509("certs/CA RAIZ NACIONAL - COSTA RICA v2.crt"));//
        certlist.add(parseX509("certs/CA RAIZ NACIONAL COSTA RICA.cer"));
    }

    protected void getPersonaFisicaCerts(List<X509Certificate> certlist) throws Throwable {
        this.fillCAcertificates(certlist);
        certlist.add(parseX509("certs/CA POLITICA PERSONA FISICA - COSTA RICA v2.crt"));
        certlist.add(parseX509("certs/CA SINPE - PERSONA FISICA v2.cer"));
        certlist.add(parseX509("certs/CA SINPE - PERSONA FISICA v2(1).crt"));
        certlist.add(parseX509("certs/CA SINPE - PERSONA FISICA v2(2).crt"));
    }

    protected void getPersonaJuridicaCerts(List<X509Certificate> certlist) throws Throwable {
        this.fillCAcertificates(certlist);
        addCertificateToMap(parseX509("certs/CA POLITICA PERSONA JURIDICA - COSTA RICA v2.crt"));
        addCertificateToMap(parseX509("certs/CA SINPE - PERSONA JURIDICA v2.cer"));
        addCertificateToMap(parseX509("certs/CA SINPE - PERSONA JURIDICA v2(1).crt"));
        addCertificateToMap(parseX509("certs/CA SINPE - PERSONA JURIDICA v2(2).crt"));
    }

}
