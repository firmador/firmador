/* Firmador is a program to sign documents using AdES standards.

Copyright (C) Firmador authors.

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

package cr.libre.firmador.signers;




import java.lang.invoke.MethodHandles;
import java.util.List;

import cr.libre.firmador.MessageUtils;
import cr.libre.firmador.Settings;
import cr.libre.firmador.cards.CardSignInfo;
import cr.libre.firmador.documents.Document;
import cr.libre.firmador.gui.GUIInterface;
import eu.europa.esig.dss.alert.exception.AlertException;
import eu.europa.esig.dss.enumerations.DigestAlgorithm;
import eu.europa.esig.dss.enumerations.SignatureLevel;
import eu.europa.esig.dss.enumerations.SignaturePackaging;
import eu.europa.esig.dss.enumerations.ASiCContainerType;
import eu.europa.esig.dss.model.DSSDocument;
import eu.europa.esig.dss.model.DSSException;

import eu.europa.esig.dss.model.SignatureValue;
import eu.europa.esig.dss.model.ToBeSigned;
import eu.europa.esig.dss.model.x509.CertificateToken;


import eu.europa.esig.dss.asic.xades.ASiCWithXAdESSignatureParameters;




import eu.europa.esig.dss.asic.xades.signature.ASiCWithXAdESService;

import eu.europa.esig.dss.service.tsp.OnlineTSPSource;

import eu.europa.esig.dss.token.DSSPrivateKeyEntry;
import eu.europa.esig.dss.token.SignatureTokenConnection;

import eu.europa.esig.dss.validation.CertificateVerifier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FirmadorOpenDocument extends CRSigner implements DocumentSigner {
    final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    ASiCWithXAdESSignatureParameters parameters;

    public FirmadorOpenDocument(GUIInterface gui) {
        super(gui);
    }

    public DSSDocument sign(DSSDocument toSignDocument, CardSignInfo card, Settings settings) {
        ASiCWithXAdESService service = null;

        parameters = new ASiCWithXAdESSignatureParameters();
        SignatureValue signatureValue = null;
        DSSDocument signedDocument = null;
        SignatureTokenConnection token = null;
        gui.nextStep(MessageUtils.t("signers_getting_verification_services"));

        try {
            token = getSignatureConnection(card);
        } catch (DSSException|AlertException|Error e) {
            LOG.error("Error al conectar con el dispositivo", e);
            gui.showError(FirmadorUtils.getRootCause(e));
            return null;
        }
        DSSPrivateKeyEntry privateKey = null;
        try {
            privateKey = getPrivateKey(token);
            gui.nextStep(MessageUtils.t("signers_getting_key_handler"));
        } catch (Exception e) {
            LOG.error("Error al acceder al objeto de llave del dispositivo", e);
            gui.showError(FirmadorUtils.getRootCause(e));
            return null;
        }
        try {
            gui.nextStep(MessageUtils.t("signers_getting_card_certificates"));
            CertificateToken certificate = privateKey.getCertificate();
            parameters.setSignatureLevel(settings.getXAdESLevel());
            parameters.setSignaturePackaging(SignaturePackaging.ENVELOPED);

            parameters.setDigestAlgorithm(DigestAlgorithm.SHA256);
            parameters.setSigningCertificate(certificate);

            parameters.setPrettyPrint(true);

            OnlineTSPSource onlineTSPSource = new OnlineTSPSource(TSA_URL);
            gui.nextStep(MessageUtils.t("signers_getting_tsp_services"));

            service = new ASiCWithXAdESService(this.getCertificateVerifier(certificate));

            service.setTspSource(onlineTSPSource);
            parameters.aSiC().setContainerType(ASiCContainerType.ASiC_E);

            parameters.setEn319132(false);


            ToBeSigned dataToSign = service.getDataToSign(toSignDocument, parameters);

            gui.nextStep(MessageUtils.t("signers_getting_data_structure"));
            signatureValue = token.sign(dataToSign, parameters.getDigestAlgorithm(), privateKey);
        } catch (DSSException|Error e) {
            LOG.error("Error al solicitar firma al dispositivo", e);
            gui.showError(FirmadorUtils.getRootCause(e));
        }

        try {
            gui.nextStep(MessageUtils.t("signers_signing_data_structure"));
            signedDocument = service.signDocument(toSignDocument, parameters, signatureValue);

            gui.nextStep(MessageUtils.t("signers_document_sign_complete"));
        } catch (Exception e) {
            LOG.error("Error al procesar información de firma avanzada", e);
            e.printStackTrace();
            gui.showMessage(String.format(MessageUtils.t("signers_not_possible_to_add_timestamp_sign"), FirmadorUtils.getRootCause(e)));
            parameters.setSignatureLevel(SignatureLevel.XAdES_BASELINE_B);
            try {
                signedDocument = service.signDocument(toSignDocument, parameters, signatureValue);
            } catch (Exception ex) {
                LOG.error("Error al procesar información de firma avanzada en nivel fallback (sin Internet) a AdES-B", e);
                gui.showError(FirmadorUtils.getRootCause(e));
            }
        }
        return signedDocument;
    }

    public DSSDocument extend(DSSDocument document) {
        ASiCWithXAdESSignatureParameters parameters = new ASiCWithXAdESSignatureParameters();
        parameters.setSignatureLevel(SignatureLevel.XAdES_BASELINE_LTA);
        parameters.setPrettyPrint(true);
        parameters.aSiC().setContainerType(ASiCContainerType.ASiC_E);
        CertificateVerifier verifier = this.getCertificateVerifier();
        ASiCWithXAdESService service = new ASiCWithXAdESService(verifier);
        OnlineTSPSource onlineTSPSource = new OnlineTSPSource(TSA_URL);
        service.setTspSource(onlineTSPSource);
        DSSDocument extendedDocument = null;
        try {
            extendedDocument = service.extendDocument(document, parameters);
        } catch (Exception e) {
            LOG.error("Error al procesar información para al ampliar el nivel de firma avanzada a LTA (sello adicional)", e);
            e.printStackTrace();
            gui.showMessage(String.format(MessageUtils.t("signers_not_possible_to_add_timestamp_extend"), FirmadorUtils.getRootCause(e)));
        }
        return extendedDocument;
    }

    public DSSDocument sign(Document toSignDocument, CardSignInfo card) {
        DSSDocument doc = sign(toSignDocument.getDSSDocument(), card, toSignDocument.getSettings());
        return doc;
    }

    @Override
    public void setDetached(List<DSSDocument> detacheddocs) {
        // TODO Auto-generated method stub

    }

}
