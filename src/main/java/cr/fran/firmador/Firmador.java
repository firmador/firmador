package cr.fran.firmador;

import java.util.List;

import eu.europa.esig.dss.DigestAlgorithm;
import eu.europa.esig.dss.SignatureValue;
import eu.europa.esig.dss.ToBeSigned;
import eu.europa.esig.dss.token.DSSPrivateKeyEntry;
import eu.europa.esig.dss.token.Pkcs11SignatureToken;
import eu.europa.esig.dss.token.SignatureTokenConnection;
import eu.europa.esig.dss.utils.Utils;

public class Firmador
{
    public static void main( String[] args )
    {
        SignatureTokenConnection token = new Pkcs11SignatureToken("/usr/lib/x64-athena/libASEP11.so");

        List<DSSPrivateKeyEntry> keys = token.getKeys();
        for (DSSPrivateKeyEntry entry : keys) {
            System.out.println(entry.getCertificate().getCertificate());
        }

        ToBeSigned toBeSigned = new ToBeSigned("Hello world".getBytes());
        SignatureValue signatureValue = token.sign(toBeSigned, DigestAlgorithm.SHA256, keys.get(0));

        System.out.println("Signature value : " + Utils.toBase64(signatureValue.getValue()));
    }
}
