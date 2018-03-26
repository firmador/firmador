package cr.fran.firmador;

import java.io.Console;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import eu.europa.esig.dss.DigestAlgorithm;
import eu.europa.esig.dss.DSSDocument;
import eu.europa.esig.dss.FileDocument;
import eu.europa.esig.dss.SignatureLevel;
import eu.europa.esig.dss.SignatureValue;
import eu.europa.esig.dss.ToBeSigned;
import eu.europa.esig.dss.client.crl.OnlineCRLSource;
import eu.europa.esig.dss.client.http.commons.CommonsDataLoader;
import eu.europa.esig.dss.client.ocsp.OnlineOCSPSource;
import eu.europa.esig.dss.client.tsp.OnlineTSPSource;
import eu.europa.esig.dss.pades.PAdESSignatureParameters;
import eu.europa.esig.dss.pades.signature.PAdESService;
import eu.europa.esig.dss.token.DSSPrivateKeyEntry;
import eu.europa.esig.dss.token.Pkcs11SignatureToken;
import eu.europa.esig.dss.token.SignatureTokenConnection;
import eu.europa.esig.dss.utils.Utils;
import eu.europa.esig.dss.validation.CommonCertificateVerifier;
public class Firmador
{
    public static void main( String[] args ) throws IOException
    {
        Console console = System.console();
        console.printf("PIN: ");
        char[] pin = console.readPassword();

        /*
         * ATENCIÓN: Se asume que solamente hay un token conectado.
         * Si no es el caso, podría intentar usar el PIN de otro dispositivo
         * y si no se verifica podría bloquearla por reintentos fallidos.
         */
        SignatureTokenConnection signingToken = new Pkcs11SignatureToken(
            "/usr/lib/x64-athena/libASEP11.so", pin);

        List<DSSPrivateKeyEntry> keys = signingToken.getKeys();

        /*
         * NOTA: en este ejemplo busca la llave privada 1, no se asegura que
         * sea la de autenticación o la de firma. Para mayor robustez debería
         * comprobarse que el uso de la llave contenga el bit de "no repudio".
         */
        DSSPrivateKeyEntry privateKey = signingToken.getKeys().get(1);

        PAdESSignatureParameters parameters = new PAdESSignatureParameters();

        /*
         * En teoría la firma de archivado debería aplicarse (extender con otra
         * firma de estampado de tiempo) cuando la existente tuviera alguna
         * debilidad o llega la fecha de expiración de los certificados pero el
         * reglamento dice LTV, así que ante la ambigüedad, mejor LTA que LT.
         */
        parameters.setSignatureLevel(SignatureLevel.PAdES_BASELINE_LTA);
        parameters.setDigestAlgorithm(DigestAlgorithm.SHA256);
        parameters.setSigningCertificate(privateKey.getCertificate());

        // FIXME agregar la lista de certificados desde ficheros o AIA
        parameters.setCertificateChain(privateKey.getCertificateChain());

        CommonCertificateVerifier commonCertificateVerifier =
            new CommonCertificateVerifier();

        CommonsDataLoader commonsHttpDataLoader = new CommonsDataLoader();

        OnlineCRLSource onlineCRLSource = new OnlineCRLSource();
        onlineCRLSource.setDataLoader(commonsHttpDataLoader);
        commonCertificateVerifier.setCrlSource(onlineCRLSource);

        OnlineOCSPSource onlineOCSPSource = new OnlineOCSPSource();
        onlineOCSPSource.setDataLoader(commonsHttpDataLoader);
        commonCertificateVerifier.setOcspSource(onlineOCSPSource);

        PAdESService service = new PAdESService(commonCertificateVerifier);

        String tspServer = "http://tsa.sinpe.fi.cr/tsaHttp/";
        OnlineTSPSource onlineTSPSource = new OnlineTSPSource(tspServer);
        service.setTspSource(onlineTSPSource);

        DSSDocument toSignDocument = new FileDocument(new File(args[0]));
        ToBeSigned dataToSign = service.getDataToSign(toSignDocument,
            parameters);

        DigestAlgorithm digestAlgorithm = parameters.getDigestAlgorithm();
        SignatureValue signatureValue = signingToken.sign(dataToSign,
            digestAlgorithm, privateKey);

        DSSDocument signedDocument = service.signDocument(toSignDocument,
            parameters, signatureValue);

        Arrays.fill(pin, ' ');

        signedDocument.save(args[0].substring(0, args[0].lastIndexOf("."))
            + "-firmado.pdf");
    }
}
