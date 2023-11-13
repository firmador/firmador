package cr.libre.firmador.cards;

import java.security.Key;
import java.security.KeyStore;
import java.security.KeyStore.PasswordProtection;
import java.security.Provider;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

import cr.libre.firmador.Settings;

public class PKCS12Manager extends CertificateBaseManager implements CardManagerInterface {

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
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Key getPrivateKey(String token, Long slotID, PasswordProtection password) throws Throwable {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public X509Certificate getCertificate(String token, Long slotID, PasswordProtection password) throws Throwable {
        // TODO Auto-generated method stub
        return null;
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
        // TODO Auto-generated method stub

    }

    @Override
    public X509Certificate getCertByCN(String cn) {
        // TODO Auto-generated method stub
        return null;
    }

}
