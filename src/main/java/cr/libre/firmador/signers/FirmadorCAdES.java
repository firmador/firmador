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

import cr.libre.firmador.Settings;
import cr.libre.firmador.SettingsManager;
import cr.libre.firmador.cards.CardSignInfo;
import cr.libre.firmador.documents.Document;
import cr.libre.firmador.gui.GUIInterface;
import eu.europa.esig.dss.alert.exception.AlertException;
import eu.europa.esig.dss.enumerations.DigestAlgorithm;
import eu.europa.esig.dss.enumerations.SignatureLevel;
import eu.europa.esig.dss.enumerations.SignaturePackaging;

import eu.europa.esig.dss.model.DSSDocument;
import eu.europa.esig.dss.model.DSSException;

import eu.europa.esig.dss.model.SignatureValue;
import eu.europa.esig.dss.model.ToBeSigned;
import eu.europa.esig.dss.model.x509.CertificateToken;


import eu.europa.esig.dss.cades.CAdESSignatureParameters;




import eu.europa.esig.dss.cades.signature.CAdESService;

import eu.europa.esig.dss.service.tsp.OnlineTSPSource;

import eu.europa.esig.dss.token.DSSPrivateKeyEntry;
import eu.europa.esig.dss.token.SignatureTokenConnection;

import eu.europa.esig.dss.validation.CertificateVerifier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FirmadorCAdES extends CRSigner implements DocumentSigner {
    final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    CAdESSignatureParameters parameters;

    public FirmadorCAdES(GUIInterface gui) {
        super(gui);
    }

    public DSSDocument sign(DSSDocument toSignDocument, CardSignInfo card, Settings settings) {
        parameters = new CAdESSignatureParameters();
        SignatureValue signatureValue = null;
        DSSDocument signedDocument = null;
        SignatureTokenConnection token = null;
        CertificateVerifier verifier = null;
        CAdESService service = null;
        gui.nextStep("Obteniendo servicios de verificación de certificados");

        try {
            token = getSignatureConnection(card);
        } catch (DSSException|AlertException|Error e) {
            LOG.error("Error al conectar con el dispositivo", e);
            gui.showError(FirmadorUtils.getRootCause(e));
            return null;
        }
        DSSPrivateKeyEntry privateKey = null;
        CertificateToken certificate = null;
        try {
            privateKey = getPrivateKey(token);
            gui.nextStep("Obteniendo manejador de llaves privadas");
        } catch (Exception e) {
            LOG.error("Error al acceder al objeto de llave del dispositivo", e);
            gui.showError(FirmadorUtils.getRootCause(e));
            return null;
        }
        try {
            gui.nextStep("Obteniendo certificados de la tarjeta");
            certificate = privateKey.getCertificate();
            parameters.setSignatureLevel(settings.getCAdESLevel());
            parameters.setSignaturePackaging(SignaturePackaging.DETACHED);

            parameters.setDigestAlgorithm(DigestAlgorithm.SHA256);
            parameters.setSigningCertificate(certificate);
        } catch (DSSException | Error e) {
            LOG.error("Error al solicitar firma al dispositivo", e);
            gui.showError(FirmadorUtils.getRootCause(e));
            return null;
        }
        OnlineTSPSource onlineTSPSource = new OnlineTSPSource(TSA_URL);
        gui.nextStep("Obteniendo servicios TSP");
        verifier = this.getCertificateVerifier(certificate);
        service = new CAdESService(verifier);
        service.setTspSource(onlineTSPSource);
        ToBeSigned dataToSign = service.getDataToSign(toSignDocument, parameters);

        gui.nextStep("Obteniendo estructura de datos a firmar");
        signatureValue = token.sign(dataToSign, parameters.getDigestAlgorithm(), privateKey);
        try {
            gui.nextStep("Firmando estructura de datos");
            signedDocument = service.signDocument(toSignDocument, parameters, signatureValue);

            gui.nextStep("Firmado del documento completo");
        } catch (Exception e) {
            LOG.error("Error al procesar información de firma avanzada", e);
            e.printStackTrace();
            gui.showMessage("Aviso: no se ha podido agregar el sello de tiempo y la información de revocación porque es posible<br>" +
                "que haya problemas de conexión a Internet o con los servidores del sistema de Firma Digital.<br>" +
                "Detalle del error: " + FirmadorUtils.getRootCause(e) + "<br><br>" +
                "Se ha agregado una firma básica solamente. No obstante, si el sello de tiempo resultara importante<br>" +
                "para este documento, debería agregarse lo antes posible antes de enviarlo al destinatario.<br><br>" +
                "Si lo prefiere, puede cancelar el guardado del documento firmado e intentar firmarlo más tarde.<br>");
            parameters.setSignatureLevel(SignatureLevel.CAdES_BASELINE_B);
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
        CAdESSignatureParameters parameters = new CAdESSignatureParameters();
        parameters.setSignatureLevel(SignatureLevel.CAdES_BASELINE_LTA);


        CertificateVerifier verifier = this.getCertificateVerifier();
        CAdESService service = new CAdESService(verifier);
        OnlineTSPSource onlineTSPSource = new OnlineTSPSource(TSA_URL);
        service.setTspSource(onlineTSPSource);
        DSSDocument extendedDocument = null;
        try {
            extendedDocument = service.extendDocument(document, parameters);
        } catch (Exception e) {
            LOG.error("Error al procesar información para al ampliar el nivel de firma avanzada a LTA (sello adicional)", e);
            e.printStackTrace();
            gui.showMessage("Aviso: no se ha podido agregar el sello de tiempo y la información de revocación porque es posible<br>" +
                "que haya problemas de conexión a Internet o con los servidores del sistema de Firma Digital.<br>" +
                "Detalle del error: " + FirmadorUtils.getRootCause(e) + "<br><br>" +
                "Inténtelo de nuevo más tarde. Si el problema persiste, compruebe su conexión o verifique<br>" +
                "que no se trata de un problema de los servidores de Firma Digital o de un error de este programa.<br>");
        }
        return extendedDocument;
    }

    public DSSDocument sign(Document toSignDocument, CardSignInfo card) {
        DSSDocument doc = sign(toSignDocument.getDSSDocument(), card, toSignDocument.getSettings());
        return doc;
    }

}
