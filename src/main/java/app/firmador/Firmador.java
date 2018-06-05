/* Firmador is a program to sign documents using AdES standards.

Copyright (C) 2018 Francisco de la Peña Fernández.

This file is part of Firmador.

Firmador is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

Firmador is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with Firmador.  If not, see <http://www.gnu.org/licenses/>.  */

package app.firmador;


import java.io.IOException;
import java.security.KeyStore.PasswordProtection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.security.auth.DestroyFailedException;

import app.firmador.gui.GUIInterface;
import app.firmador.gui.GUISelector;
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
import eu.europa.esig.dss.tsl.KeyUsageBit;
import eu.europa.esig.dss.validation.CommonCertificateVerifier;
import eu.europa.esig.dss.x509.CertificateToken;

public class Firmador {

    public static void main(String[] args) {

        GUISelector guiselector = new GUISelector();

        GUIInterface gui = guiselector.getInterface(args);
        gui.setArgs(args);
        String fileName = gui.getDocumentToSign();
        PasswordProtection pin = gui.getPin();

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
         * En el futuro deberían recorrerse todos los certificados encontrados.
         * FIXME.
         * Más en el futuro debería soportar otros mecanismos de acceso a
         * PKCS#11 específicos de cada sistema operativo, en busca de otros
         * fabricantes que no sean Athena/NXP (para sello electrónico).
         */
        SignatureTokenConnection signingToken = new Pkcs11SignatureToken(
            pkcs11lib, pin);

        /*
         * Usa la primera llave cuyo uso es no repudio, asumiendo que no hay
         * más llaves con el mismo propósito en el mismo token.
         * Esto debería funcionar bien con tarjetas de Firma Digital no
         * manipuladas pero en el futuro sería conveniente comprobar que
         * no hay ningún caso extremo y verificar que verdaderamente se trata
         * de la única y permitir elegir cuál usar. FIXME.
         */
        DSSPrivateKeyEntry privateKey = null;
        for (DSSPrivateKeyEntry candidatePrivateKey : signingToken.getKeys()) {
            if (candidatePrivateKey.getCertificate().checkKeyUsage(
                KeyUsageBit.nonRepudiation)) {
                    privateKey = candidatePrivateKey;
                }
        }

        PAdESSignatureParameters parameters = new PAdESSignatureParameters();

        parameters.setSignatureLevel(SignatureLevel.PAdES_BASELINE_LTA);
        parameters.setSignatureSize(13312);
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

        try {
            pin.destroy();
        } catch (DestroyFailedException e) {
            // TODO
        }

        fileName = gui.getPathToSave();
        try {
            signedDocument.save(fileName);
        } catch (IOException e) {
            // TODO
        }
    }
}
