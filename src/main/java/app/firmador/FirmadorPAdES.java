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

import com.google.common.base.Throwables;

import app.firmador.gui.GUIInterface;
import eu.europa.esig.dss.DSSDocument;
import eu.europa.esig.dss.DSSException;
import eu.europa.esig.dss.DigestAlgorithm;
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

    private DSSDocument _sign(DSSDocument toSignDocument,
        PasswordProtection pin) {

        CertificateVerifier commonCertificateVerifier =
            this.getCertificateVerifier();
        SignatureTokenConnection signingToken = getSignatureConnection(pin);
        DSSPrivateKeyEntry privateKey = getPrivateKey(signingToken);
        PAdESSignatureParameters parameters = new PAdESSignatureParameters();

        parameters.setSignatureLevel(SignatureLevel.PAdES_BASELINE_LTA);
        parameters.setSignatureSize(13312);
        parameters.setDigestAlgorithm(DigestAlgorithm.SHA256);
        parameters.setSigningCertificate(privateKey.getCertificate());

        List<CertificateToken> certificateChain = getCertificateChain(
            commonCertificateVerifier, parameters);
        parameters.setCertificateChain(certificateChain);

        PAdESService service = new PAdESService(commonCertificateVerifier);

        OnlineTSPSource onlineTSPSource = new OnlineTSPSource(TSA_URL);
        service.setTspSource(onlineTSPSource);

        ToBeSigned dataToSign = service.getDataToSign(toSignDocument,
            parameters);

        SignatureValue signatureValue = signingToken.sign(dataToSign,
            parameters.getDigestAlgorithm(), privateKey);
        DSSDocument signedDocument = null;
        try {
            signedDocument = service.signDocument(toSignDocument, parameters,
                signatureValue);
        } catch (DSSException e) {
            String className = Throwables.getRootCause(e).getClass().getName();
            // Thrown when TSA is not available, retry with a lower profile
            if (className == "java.net.UnknownHostException") {
                parameters.setSignatureLevel(SignatureLevel.PAdES_BASELINE_B);
                signedDocument = service.signDocument(toSignDocument,
                    parameters, signatureValue);
            } else {
                gui.showError(Throwables.getRootCause(e));
            }
        }

        return signedDocument;
    }

    public DSSDocument sign(DSSDocument toSignDocument,
        PasswordProtection pin) {

        DSSDocument document = null;

        try {
            document = _sign(toSignDocument, pin);
        } catch (Exception|Error e) {
            gui.showError(Throwables.getRootCause(e));
        }

        return document;
    }

    public DSSDocument extend(DSSDocument document) {
        CertificateVerifier certificateVerifier =
            this.getCertificateVerifier();
        PAdESSignatureParameters parameters = new PAdESSignatureParameters();

        parameters.setSignatureLevel(SignatureLevel.PAdES_BASELINE_LTA);

        PAdESService padesService = new PAdESService(certificateVerifier);

        OnlineTSPSource onlineTSPSource = new OnlineTSPSource(TSA_URL);
        padesService.setTspSource(onlineTSPSource);

        DSSDocument extendedDocument = padesService.extendDocument(document,
            parameters);

        return extendedDocument;
    }

}
