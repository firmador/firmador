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
import eu.europa.esig.dss.SignaturePackaging;
import eu.europa.esig.dss.SignatureValue;
import eu.europa.esig.dss.ToBeSigned;
import eu.europa.esig.dss.client.tsp.OnlineTSPSource;
import eu.europa.esig.dss.xades.XAdESSignatureParameters;
import eu.europa.esig.dss.xades.signature.XAdESService;
import eu.europa.esig.dss.token.DSSPrivateKeyEntry;
import eu.europa.esig.dss.token.SignatureTokenConnection;
import eu.europa.esig.dss.validation.CertificateVerifier;
import eu.europa.esig.dss.x509.CertificateToken;

public class FirmadorXAdES extends CRSigner {

    public FirmadorXAdES(GUIInterface gui) {
        super(gui);
    }

    private DSSDocument _sign(DSSDocument toSignDocument,
        PasswordProtection pin) {

        CertificateVerifier commonCertificateVerifier =
            this.getCertificateVerifier();
        SignatureTokenConnection signingToken = getSignatureConnection(pin);
        DSSPrivateKeyEntry privateKey = getPrivateKey(signingToken);
        XAdESSignatureParameters parameters = new XAdESSignatureParameters();

        parameters.setSignatureLevel(SignatureLevel.XAdES_BASELINE_LTA);
        parameters.setSignaturePackaging(SignaturePackaging.ENVELOPED);
        parameters.setSigningCertificate(privateKey.getCertificate());

        List<CertificateToken> certificateChain = getCertificateChain(
            commonCertificateVerifier, parameters);
        parameters.setCertificateChain(certificateChain);

        XAdESService service = new XAdESService(commonCertificateVerifier);

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
                parameters.setSignatureLevel(SignatureLevel.XAdES_BASELINE_B);
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
        XAdESSignatureParameters parameters = new XAdESSignatureParameters();

        parameters.setSignatureLevel(SignatureLevel.XAdES_BASELINE_LTA);

        XAdESService xadesService = new XAdESService(certificateVerifier);

        OnlineTSPSource onlineTSPSource = new OnlineTSPSource(TSA_URL);
        xadesService.setTspSource(onlineTSPSource);

        DSSDocument extendedDocument = xadesService.extendDocument(document,
            parameters);

        return extendedDocument;
    }

}
