/* firmador-pdf is a program to sign PDF documents using the PAdES standard.

Copyright (C) 2018 Francisco de la Peña Fernández.

This file is part of firmador-pdf.

firmador-pdf is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

firmador-pdf is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with firmador-pdf.  If not, see <http://www.gnu.org/licenses/>.  */

package cr.fran.firmador;

import java.awt.FileDialog;
import java.awt.Dialog;
import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.security.KeyStore.PasswordProtection;
import java.util.ArrayList;
import java.util.List;
import javax.security.auth.DestroyFailedException;

import eu.europa.esig.dss.DigestAlgorithm;
import eu.europa.esig.dss.DSSDocument;
import eu.europa.esig.dss.DSSUtils;
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
import eu.europa.esig.dss.x509.CertificateToken;

public class Firmador {
    private static PasswordProtection pin;
    private static Dialog pinDialog;
    private static FileDialog loadDialog;
    public static void main(String[] args)
        throws IOException, DestroyFailedException {
        String fileName = null;
        if (args.length < 1) {
            loadDialog = new FileDialog(loadDialog,
                "Seleccionar documento a firmar");
            loadDialog.setFile("*.pdf");
            loadDialog.setFilenameFilter(new FilenameFilter() {
                public boolean accept(File dir, String name) {
                    return name.endsWith(".pdf") || name.endsWith(".PDF");
                }
            });
            loadDialog.setLocationRelativeTo(null);
            loadDialog.setVisible(true);
            loadDialog.dispose();
            if (loadDialog.getFile() == null) {
                System.out.println("Sintaxis: firmador.jar fichero.pdf");
                System.exit(1);
            } else {
                fileName = loadDialog.getDirectory() + loadDialog.getFile();
            }
        } else {
            fileName = args[0];
        }
        pinDialog = new Dialog(pinDialog, "Ingresar PIN", true);
        pinDialog.setLocationRelativeTo(null);
        final TextField pinField = new TextField(17);
        pinField.setEchoChar('●');
        pinField.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_ENTER:
                        pin = new PasswordProtection(pinField.getText().toCharArray());
                        pinDialog.dispose();
                        break;
                    case KeyEvent.VK_ESCAPE:
                        System.exit(1);
                }
            }
        });
        pinDialog.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent windowEvent) {
                System.exit(1);
            }
        });
        pinDialog.add(pinField);
        pinDialog.pack();
        pinDialog.setVisible(true);

        String pkcs11lib = "";
        String osName = System.getProperty("os.name").toLowerCase();
        if (osName.contains("mac")) {
            pkcs11lib = "/Library/Application Support/Athena/libASEP11.dylib";
        } else if (osName.contains("linux")) {
            pkcs11lib = "/usr/lib/x64-athena/libASEP11.so";
        } else if (osName.contains("windows")) {
            pkcs11lib = System.getenv("SystemRoot")
                + "\\System32\\asepkcs.dll";
        }
        /*
         * ATENCIÓN: Se asume que solamente hay un token conectado.
         * Si no es el caso, podría intentar usar el PIN de otro dispositivo
         * y si no se verifica podría bloquearse por reintentos fallidos.
         */
        SignatureTokenConnection signingToken = new Pkcs11SignatureToken(
            pkcs11lib, pin);

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
        parameters.setSignatureSize(9472 * 2);
        parameters.setDigestAlgorithm(DigestAlgorithm.SHA256);
        parameters.setSigningCertificate(privateKey.getCertificate());

        CommonCertificateVerifier commonCertificateVerifier =
            new CommonCertificateVerifier();

        CommonsDataLoader commonsHttpDataLoader = new CommonsDataLoader();

        OnlineCRLSource onlineCRLSource = new OnlineCRLSource();
        onlineCRLSource.setDataLoader(commonsHttpDataLoader);
        commonCertificateVerifier.setCrlSource(onlineCRLSource);
        OnlineOCSPSource onlineOCSPSource = new OnlineOCSPSource();
        onlineOCSPSource.setDataLoader(commonsHttpDataLoader);
        commonCertificateVerifier.setOcspSource(onlineOCSPSource);

        List<CertificateToken> certificateChain =
            new ArrayList<CertificateToken>();
        List<CertificateToken> cert = new ArrayList<CertificateToken>(
            DSSUtils.loadPotentialIssuerCertificates(
                parameters.getSigningCertificate(),
                commonCertificateVerifier.getDataLoader()));
        certificateChain.add(cert.get(0));

        do {
            cert = new ArrayList<CertificateToken>(
                DSSUtils.loadPotentialIssuerCertificates(cert.get(0),
                    commonCertificateVerifier.getDataLoader()));
            if (!cert.isEmpty()) {
                certificateChain.add(cert.get(0));
            }
        } while (!cert.isEmpty());

        parameters.setCertificateChain(certificateChain);

        PAdESService service = new PAdESService(commonCertificateVerifier);

        String tspServer = "http://tsa.sinpe.fi.cr/tsaHttp/";
        OnlineTSPSource onlineTSPSource = new OnlineTSPSource(tspServer);
        service.setTspSource(onlineTSPSource);

        DSSDocument toSignDocument = new FileDocument(fileName);
        ToBeSigned dataToSign = service.getDataToSign(toSignDocument,
            parameters);

        DigestAlgorithm digestAlgorithm = parameters.getDigestAlgorithm();
        SignatureValue signatureValue = signingToken.sign(dataToSign,
            digestAlgorithm, privateKey);

        DSSDocument signedDocument = service.signDocument(toSignDocument,
            parameters, signatureValue);

        pin.destroy();

        FileDialog saveDialog = null;
        saveDialog = new FileDialog(saveDialog,
            "Guardar documento", FileDialog.SAVE);
        saveDialog.setDirectory(loadDialog.getDirectory());
        saveDialog.setFile(loadDialog.getFile().substring(0,
            loadDialog.getFile().lastIndexOf(".")) + "-firmado.pdf");
        saveDialog.setFilenameFilter(loadDialog.getFilenameFilter());
        saveDialog.setLocationRelativeTo(null);
        saveDialog.setVisible(true);
        saveDialog.dispose();
        if (saveDialog.getFile() == null) {
            System.exit(1);
        } else {
            fileName = saveDialog.getDirectory() + saveDialog.getFile();
        }

        signedDocument.save(fileName);
    }
}
