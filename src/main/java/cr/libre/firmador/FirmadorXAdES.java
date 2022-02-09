/* Firmador is a program to sign documents using AdES standards.

Copyright (C) 2018, 2022 Firmador authors.

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

package cr.libre.firmador;





import java.security.KeyStore.PasswordProtection;

import java.util.Arrays;


import cr.libre.firmador.gui.GUIInterface;
import com.google.common.base.Throwables;
import eu.europa.esig.dss.alert.exception.AlertException;
import eu.europa.esig.dss.enumerations.DigestAlgorithm;
import eu.europa.esig.dss.enumerations.SignatureLevel;
import eu.europa.esig.dss.enumerations.SignaturePackaging;

import eu.europa.esig.dss.model.DSSDocument;
import eu.europa.esig.dss.model.DSSException;
import eu.europa.esig.dss.model.MimeType;
//import eu.europa.esig.dss.model.Policy; // Electronic receipts
import eu.europa.esig.dss.model.SignatureValue;
import eu.europa.esig.dss.model.ToBeSigned;
import eu.europa.esig.dss.model.x509.CertificateToken;

import eu.europa.esig.dss.service.tsp.OnlineTSPSource;
import eu.europa.esig.dss.xades.XAdESSignatureParameters;
//import eu.europa.esig.dss.xades.signature.XAdESCounterSignatureParameters; // Electronic receipts v4.4 proposal
import eu.europa.esig.dss.xades.reference.DSSReference;
import eu.europa.esig.dss.xades.reference.XPathEnvelopedSignatureTransform;
import eu.europa.esig.dss.xades.signature.XAdESService;

import eu.europa.esig.dss.token.DSSPrivateKeyEntry;
import eu.europa.esig.dss.token.SignatureTokenConnection;
//import eu.europa.esig.dss.utils.Utils; // Electronic receipts
import eu.europa.esig.dss.validation.CertificateVerifier;
//import eu.europa.esig.dss.validation.SignedDocumentValidator; // Electronic receipts v4.4 proposal

public class FirmadorXAdES extends CRSigner {

    //XAdESCounterSignatureParameters parameters; // Electronic receipts v4.4 proposal
    XAdESSignatureParameters parameters;


    public FirmadorXAdES(GUIInterface gui) {
        super(gui);
    }

    public DSSDocument sign(DSSDocument toSignDocument, PasswordProtection pin) {
        CertificateVerifier verifier = this.getCertificateVerifier();
        XAdESService service = new XAdESService(verifier);
        //parameters = new XAdESCounterSignatureParameters(); // Electronic receipts v4.4 proposal
        parameters = new XAdESSignatureParameters();
        SignatureValue signatureValue = null;
        DSSDocument signedDocument = null;
        SignatureTokenConnection token = null;
        try {
            token = getSignatureConnection(pin);
        } catch (DSSException|AlertException|Error e) {
            gui.showError(Throwables.getRootCause(e));
        }
        DSSPrivateKeyEntry privateKey = null;
        try {
            privateKey = getPrivateKey(token);
            if (privateKey == null) {
                for (int i = 0;; i++) {
                    try {
                        token = getSignatureConnection(pin, i);
                        privateKey = getPrivateKey(token);
                        if (privateKey != null) break;
                    } catch (Exception ex) {
                        if (Throwables.getRootCause(ex).getLocalizedMessage().equals("CKR_SLOT_ID_INVALID")) break;
                        else gui.showError(Throwables.getRootCause(ex));
                    }
                }
            }
        } catch (Exception e) {
            gui.showError(Throwables.getRootCause(e));
        }
        try {
            CertificateToken certificate = privateKey.getCertificate();

            //parameters.setSignatureLevel(SignatureLevel.XAdES_BASELINE_B);
            parameters.setSignatureLevel(SignatureLevel.XAdES_BASELINE_LT);

            // This couple don't apply for counter-signature (Electronic receipts v4.4 proposal)
            if (toSignDocument.getMimeType() == MimeType.XML) parameters.setSignaturePackaging(SignaturePackaging.ENVELOPED);
            else parameters.setSignaturePackaging(SignaturePackaging.DETACHED);

            parameters.setDigestAlgorithm(DigestAlgorithm.SHA256);
            parameters.setSigningCertificate(certificate);
            parameters.setPrettyPrint(true);
            //parameters.setSignatureIdToCounterSign(SignedDocumentValidator.fromDocument(toSignDocument).getSignatures().iterator().next().getId()); // Electronic receipts v4.4 proposal

            // Since DSS 5.7 defaults to XPath Filter 2.0, the following restores the XPath 1.0 transformation
            DSSReference dssReference = new DSSReference();
            dssReference.setTransforms(Arrays.asList(new XPathEnvelopedSignatureTransform()));
            dssReference.setContents(toSignDocument);
            dssReference.setId("r-" + parameters.getDeterministicId() + "-1");
            dssReference.setUri("");
            dssReference.setDigestMethodAlgorithm(parameters.getDigestAlgorithm());
            parameters.setReferences(Arrays.asList(dssReference));
            parameters.setEn319132(false); // Use ETSI TS 101 903 tags (no SigningCertificatev2 and other *v2 tags)

            OnlineTSPSource onlineTSPSource = new OnlineTSPSource(TSA_URL);
            service.setTspSource(onlineTSPSource);
            /*
            Policy policy = new Policy(); // Costa Rica tax office electronic receipts signature policy
            policy.setId("https://www.hacienda.go.cr/ATV/ComprobanteElectronico/docs/esquemas/2016/v4.3/Resoluci%C3%B3n_General_sobre_disposiciones_t%C3%A9cnicas_comprobantes_electr%C3%B3nicos_para_efectos_tributarios.pdf");
            policy.setDigestAlgorithm(parameters.getDigestAlgorithm());
            policy.setDigestValue(Utils.fromBase64("0h7Q3dFHhu0bHbcZEgVc07cEcDlquUeG08HG6Iototo="));
            parameters.bLevel().setSignaturePolicy(policy);
            */
            //parameters.bLevel().setClaimedSignerRoles(Arrays.asList("Receptor")); // Electronic receipts v4.4 proposal

            ToBeSigned dataToSign = service.getDataToSign(toSignDocument, parameters);
            //ToBeSigned dataToSign = service.getDataToBeCounterSigned(toSignDocument, parameters); // Electronic receipts v4.4 proposal

            signatureValue = token.sign(dataToSign, parameters.getDigestAlgorithm(), privateKey);
        } catch (DSSException|Error e) {
            gui.showError(Throwables.getRootCause(e));
        }





        try {
            signedDocument = service.signDocument(toSignDocument, parameters, signatureValue);
            //signedDocument = service.counterSignSignature(toSignDocument, parameters, signatureValue); // Electronic receipts v4.4 proposal
        } catch (Exception e) {
            e.printStackTrace();
            gui.showMessage("Aviso: no se ha podido agregar el sello de tiempo y la información de revocación porque es posible<br>" +
                "que haya problemas de conexión a Internet o con los servidores del sistema de Firma Digital.<br>" +
                "Detalle del error: " + Throwables.getRootCause(e) + "<br><br>" +
                "Se ha agregado una firma básica solamente. No obstante, si el sello de tiempo resultara importante<br>" +
                "para este documento, debería agregarse lo antes posible antes de enviarlo al destinatario.<br><br>" +
                "Si lo prefiere, puede cancelar el guardado del documento firmado e intentar firmarlo más tarde.<br>");
            parameters.setSignatureLevel(SignatureLevel.XAdES_BASELINE_B);
            try {
                signedDocument = service.signDocument(toSignDocument, parameters, signatureValue);
                //signedDocument = service.counterSignSignature(toSignDocument, parameters, signatureValue); // Electronic receipts v4.4 proposal
            } catch (Exception ex) {
                e.printStackTrace();
                gui.showError(Throwables.getRootCause(e));
            }
        }
        return signedDocument;
    }

    public DSSDocument extend(DSSDocument document) {
        XAdESSignatureParameters parameters = new XAdESSignatureParameters();
        parameters.setSignatureLevel(SignatureLevel.XAdES_BASELINE_LTA);
        parameters.setPrettyPrint(true);

        CertificateVerifier verifier = this.getCertificateVerifier();
        XAdESService service = new XAdESService(verifier);
        OnlineTSPSource onlineTSPSource = new OnlineTSPSource(TSA_URL);
        service.setTspSource(onlineTSPSource);
        DSSDocument extendedDocument = null;
        try {
            extendedDocument = service.extendDocument(document, parameters);
        } catch (Exception e) {
            e.printStackTrace();
            gui.showMessage("Aviso: no se ha podido agregar el sello de tiempo y la información de revocación porque es posible<br>" +
                "que haya problemas de conexión a Internet o con los servidores del sistema de Firma Digital.<br>" +
                "Detalle del error: " + Throwables.getRootCause(e) + "<br><br>" +
                "Inténtelo de nuevo más tarde. Si el problema persiste, compruebe su conexión o verifique<br>" +
                "que no se trata de un problema de los servidores de Firma Digital o de un error de este programa.<br>");
        }
        return extendedDocument;
    }

}
