/* Firmador is a program to sign documents using AdES standards.

Copyright (C) 2018 Firmador authors.

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

import java.security.KeyStore.PasswordProtection;
import java.util.List;

import app.firmador.gui.GUIInterface;
import com.google.common.base.Throwables;
import eu.europa.esig.dss.DSSDocument;
import eu.europa.esig.dss.DSSException;
import eu.europa.esig.dss.SignatureLevel;

import eu.europa.esig.dss.SignatureValue;
import eu.europa.esig.dss.ToBeSigned;
import eu.europa.esig.dss.client.tsp.OnlineTSPSource;
import eu.europa.esig.dss.pades.PAdESSignatureParameters;
import eu.europa.esig.dss.pades.signature.PAdESService;
import eu.europa.esig.dss.token.DSSPrivateKeyEntry;
import eu.europa.esig.dss.token.SignatureTokenConnection;
import eu.europa.esig.dss.validation.CertificateVerifier;
import eu.europa.esig.dss.x509.CertificateToken;

public class FirmadorPAdES extends CRSigner {

    public FirmadorPAdES(GUIInterface gui) {
        super(gui);
    }

    public DSSDocument sign(DSSDocument toSignDocument,
        PasswordProtection pin) {

        CertificateVerifier verifier = this.getCertificateVerifier();
        verifier.setCheckRevocationForUntrustedChains(true);

        PAdESService service = new PAdESService(verifier);

        PAdESSignatureParameters parameters = new PAdESSignatureParameters();

        SignatureValue signatureValue = null;

        try {
            SignatureTokenConnection token = getSignatureConnection(pin);
            DSSPrivateKeyEntry privateKey = getPrivateKey(token);

            parameters.setSignatureLevel(SignatureLevel.PAdES_BASELINE_LTA);
            parameters.setSignatureSize(13312);
            parameters.setSigningCertificate(privateKey.getCertificate());
            parameters.setSignWithExpiredCertificate(true);

            List<CertificateToken> certificateChain = getCertificateChain(
                verifier, parameters);
            parameters.setCertificateChain(certificateChain);

            OnlineTSPSource onlineTSPSource = new OnlineTSPSource(TSA_URL);
            service.setTspSource(onlineTSPSource);

            ToBeSigned dataToSign = service.getDataToSign(toSignDocument,
                parameters);

            signatureValue = token.sign(dataToSign,
                parameters.getDigestAlgorithm(), privateKey);
        } catch (DSSException|Error e) {
            gui.showError(Throwables.getRootCause(e));
        }

        DSSDocument signedDocument = null;
        try {
            signedDocument = service.signDocument(toSignDocument, parameters,
                signatureValue);
        } catch (Exception e) {
            e.printStackTrace();
            gui.showMessage(
                "Aviso: no se ha podido agregar el sello de tiempo y la " +
                "información de revocación porque es posible\n" +
                "que haya problemas de conexión con los servidores del " +
                "sistema de Firma Digital.\n" +
                "Detalle del error: " + Throwables.getRootCause(e) + "\n" +
                "\n" +
                "Se ha agregado una firma básica solamente. No obstante, si " +
                "el sello de tiempo resultara importante\n" +
                "para este documento, debería agregarse lo antes posible " +
                "antes de enviarlo al destinatario.\n" +
                "\n" +
                "Si lo prefiere, puede cancelar el guardado del documento " +
                "firmado e intentar firmarlo más tarde.\n");

            parameters.setSignatureLevel(SignatureLevel.PAdES_BASELINE_B);
            try {
                signedDocument = service.signDocument(toSignDocument,
                    parameters, signatureValue);
            } catch (Exception ex) {
                e.printStackTrace();
                gui.showError(Throwables.getRootCause(e));
            }
        }

        return signedDocument;
    }

    public DSSDocument extend(DSSDocument document) {
        PAdESSignatureParameters parameters = new PAdESSignatureParameters();
        parameters.setSignatureLevel(SignatureLevel.PAdES_BASELINE_LTA);

        CertificateVerifier verifier = this.getCertificateVerifier();
        verifier.setCheckRevocationForUntrustedChains(true);

        PAdESService padesService = new PAdESService(verifier);

        OnlineTSPSource onlineTSPSource = new OnlineTSPSource(TSA_URL);
        padesService.setTspSource(onlineTSPSource);

        DSSDocument extendedDocument = null;
        try {
            extendedDocument = padesService.extendDocument(document,
                parameters);
        } catch (Exception e) {
            e.printStackTrace();
            gui.showMessage(
                "Aviso: no se ha podido agregar el sello de tiempo y la " +
                "información de revocación porque es posible\n" +
                "que haya problemas de conexión con los servidores del " +
                "sistema de Firma Digital.\n" +
                "Detalle del error: " + Throwables.getRootCause(e) + "\n" +
                "\n" +
                "Inténtelo de nuevo más tarde. Si el problema persiste, " +
                "compruebe su conexión a Internet o verifique\n" +
                "que no se trata de un problema de los servidores de Firma " +
                "Digital o de un error de este programa.\n");
        }

        return extendedDocument;
    }

}
