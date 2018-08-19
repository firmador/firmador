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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.xml.crypto.dsig.CanonicalizationMethod;

import com.google.common.base.Throwables;

import app.firmador.gui.GUIInterface;
import eu.europa.esig.dss.BLevelParameters;
import eu.europa.esig.dss.DSSDocument;
import eu.europa.esig.dss.DigestAlgorithm;
import eu.europa.esig.dss.EncryptionAlgorithm;
import eu.europa.esig.dss.SignatureLevel;
import eu.europa.esig.dss.SignaturePackaging;
import eu.europa.esig.dss.SignatureValue;
import eu.europa.esig.dss.ToBeSigned;
import eu.europa.esig.dss.client.tsp.OnlineTSPSource;
import eu.europa.esig.dss.token.DSSPrivateKeyEntry;
import eu.europa.esig.dss.token.SignatureTokenConnection;
import eu.europa.esig.dss.validation.CertificateVerifier;
import eu.europa.esig.dss.x509.CertificateToken;
import eu.europa.esig.dss.x509.tsp.TSPSource;
import eu.europa.esig.dss.xades.DSSReference;
import eu.europa.esig.dss.xades.DSSTransform;
import eu.europa.esig.dss.xades.XAdESSignatureParameters;
import eu.europa.esig.dss.xades.signature.XAdESService;

public class FirmadorXAdES extends CRSigner{

    public FirmadorXAdES(GUIInterface gui) {
        super(gui);
    }

    public DSSDocument _sign(DSSDocument toSignDocument,
        PasswordProtection pin) {

        CertificateVerifier commonCertificateVerifier =
            this.getCertificateVerifier();
        XAdESService xadesService = new XAdESService(
            (CertificateVerifier)commonCertificateVerifier);

        // TSA
        TSPSource onlineTSPSource = new OnlineTSPSource(TSA_URL);
        xadesService.setTspSource((TSPSource)onlineTSPSource);

        BLevelParameters bLevelParams = new BLevelParameters();
        bLevelParams.setSigningDate(new Date());

        XAdESSignatureParameters parameters = new XAdESSignatureParameters();
        parameters.setDigestAlgorithm(DigestAlgorithm.SHA256);
        parameters.setSignatureLevel(SignatureLevel.XAdES_BASELINE_LTA);
        parameters.setSignaturePackaging(SignaturePackaging.ENVELOPED);
        parameters.setBLevelParams(bLevelParams);

        SignatureTokenConnection signingToken = get_signatureConnection(pin);
        DSSPrivateKeyEntry signerkey = getPrivateKey(signingToken);
        parameters.setSigningCertificate(signerkey.getCertificate());
        parameters.setEncryptionAlgorithm(EncryptionAlgorithm.RSA);

        final List<DSSReference> references = new ArrayList<DSSReference>();
        DSSReference dssReference = new DSSReference();
        dssReference.setId("xml_ref_id");
        dssReference.setUri("");
        dssReference.setContents(toSignDocument);
        dssReference.setDigestMethodAlgorithm(parameters.getDigestAlgorithm());

        final List<DSSTransform> transforms = new ArrayList<DSSTransform>();

        DSSTransform dssTransform = new DSSTransform();
        dssTransform.setAlgorithm(CanonicalizationMethod.ENVELOPED);
        transforms.add(dssTransform);

        dssTransform = new DSSTransform();
        dssTransform.setAlgorithm(CanonicalizationMethod.EXCLUSIVE);
        transforms.add(dssTransform);

        dssReference.setTransforms(transforms);
        references.add(dssReference);

        parameters.setReferences(references);

        List<CertificateToken> certificateChain = getCertificateChain(
                commonCertificateVerifier, parameters);
        parameters.setCertificateChain(certificateChain);

        ToBeSigned dataToSign = xadesService.getDataToSign(toSignDocument,
            parameters);

        DigestAlgorithm digestAlgorithm = parameters.getDigestAlgorithm();
        SignatureValue signatureValue = signingToken.sign(dataToSign,
            digestAlgorithm, signerkey);

        DSSDocument signedDocument = xadesService.signDocument(toSignDocument,
            parameters, signatureValue);

        return signedDocument;
    }

    public DSSDocument sign(DSSDocument toSignDocument,
        PasswordProtection pin) {

        DSSDocument dev = null;

        try {
            dev = _sign(toSignDocument, pin);
        } catch (Exception|Error e) {
            gui.showError(Throwables.getRootCause(e));
        }

        return dev;
    }

}
