package cr.libre.firmador.cards;

import java.io.FileInputStream;
import java.security.Key;
import java.security.KeyStore;
import java.security.KeyStore.PasswordProtection;
import java.security.KeyStoreException;
import java.security.Provider;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import cr.libre.firmador.Settings;

public class PKCS12Manager extends CertificateBaseManager implements CardManagerInterface {
    private String locationFile;
    private KeyStore keyStore;
    private Settings settings;
    @Override
    public Provider getProvider() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<X509Certificate> getCertificates() throws Throwable {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public KeyStore getKeyStore(Long slotID, PasswordProtection password) throws Throwable {
        if (keyStore == null) {
            keyStore = KeyStore.getInstance("PKCS12", "BC");
            try (FileInputStream fis = new FileInputStream(this.locationFile)) {
                keyStore.load(fis, password.getPassword());
            }
        }
        return keyStore;
    }

    @Override
    public Key getPrivateKey(String token, Long slotID, PasswordProtection password) throws Throwable {
        KeyStore keystore = this.getKeyStore(new Long(0), password);
        Key key = null;
        String alias = keystore.aliases().nextElement();
        key = keystore.getKey(alias, password.getPassword());
        return key;
    }

    @Override
    public X509Certificate getCertificate(String token, Long slotID, PasswordProtection password) throws Throwable {
        KeyStore keystore = this.getKeyStore(new Long(0), password);
        X509Certificate certificate = null;
        String alias = keystore.aliases().nextElement();
        certificate = (X509Certificate) keystore.getCertificate(alias);
        return certificate;
    }

    @Override
    public List<X509Certificate> getSignCertificates() throws Throwable {
        List<X509Certificate> certlist = new ArrayList<X509Certificate>();
        this.getPersonaJuridicaCerts(certlist);
        return certlist;
    }



    @Override
    public List<X509Certificate> getCertificateChain(X509Certificate certificate) throws Throwable {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void setSettings(Settings settings) {
        this.settings = settings;

    }

    @Override
    public X509Certificate getCertByCN(String cn) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void setSerialNumber(String serialnumber) {
        locationFile = serialnumber;
        keyStore = null;
    }

    @Override
    public CardSignInfo loadTokens(CardSignInfo card, KeyStore keystore) {

        try {
            Enumeration<String> enumeration = keystore.aliases();
            if (enumeration.hasMoreElements()) {
                String alias = enumeration.nextElement();
                card.setTokenSerialNumber(alias);
            }
        } catch (KeyStoreException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return card;
    }


}