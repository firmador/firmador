package cr.libre.firmador.cards;

import java.security.Key;
import java.security.KeyStore;
import java.security.Provider;
import java.security.KeyStore.PasswordProtection;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.List;

import cr.libre.firmador.Settings;

public interface CardManagerInterface {
    public Provider getProvider();

    List<X509Certificate> getCertificates() throws Throwable;

    KeyStore getKeyStore(Long slotID, PasswordProtection password) throws Throwable;

    Key getPrivateKey(String token, Long slotID, PasswordProtection password) throws Throwable;

    X509Certificate getCertificate(String token, Long slotID, PasswordProtection password) throws Throwable;


    void setSerialNumber(String serialnumber);

    public CardSignInfo loadTokens(CardSignInfo card, KeyStore keystore);
}
