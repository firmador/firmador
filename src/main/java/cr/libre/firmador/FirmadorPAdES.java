/* Firmador is a program to sign documents using AdES standards.

Copyright (C) 2021 Firmador authors.

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

import java.awt.Color;
import java.awt.Font;
import java.io.IOException;
import java.net.URL;
import java.security.KeyStore.PasswordProtection;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import cr.libre.firmador.gui.GUIInterface;
import com.google.common.base.Throwables;
import eu.europa.esig.dss.alert.exception.AlertException;
import eu.europa.esig.dss.enumerations.DigestAlgorithm;
import eu.europa.esig.dss.enumerations.SignatureLevel;
import eu.europa.esig.dss.enumerations.SignerTextPosition;
import eu.europa.esig.dss.enumerations.VisualSignatureRotation;
import eu.europa.esig.dss.model.DSSDocument;
import eu.europa.esig.dss.model.DSSException;
import eu.europa.esig.dss.model.InMemoryDocument;
import eu.europa.esig.dss.model.SignatureValue;
import eu.europa.esig.dss.model.ToBeSigned;
import eu.europa.esig.dss.model.x509.CertificateToken;
import eu.europa.esig.dss.model.x509.X500PrincipalHelper;
import eu.europa.esig.dss.pades.DSSJavaFont;
import eu.europa.esig.dss.pades.PAdESSignatureParameters;
import eu.europa.esig.dss.pades.PAdESTimestampParameters;
import eu.europa.esig.dss.pades.SignatureImageParameters;
import eu.europa.esig.dss.pades.SignatureImageTextParameters;
import eu.europa.esig.dss.pades.signature.PAdESService;
import eu.europa.esig.dss.pdf.pdfbox.PdfBoxNativeObjectFactory;
import eu.europa.esig.dss.service.tsp.OnlineTSPSource;
import eu.europa.esig.dss.spi.DSSASN1Utils;
import eu.europa.esig.dss.token.DSSPrivateKeyEntry;
import eu.europa.esig.dss.token.SignatureTokenConnection;
import eu.europa.esig.dss.utils.Utils;
import eu.europa.esig.dss.validation.CertificateVerifier;
import org.bouncycastle.asn1.x500.style.BCStyle;

public class FirmadorPAdES extends CRSigner {

    private int page = 1, x, y;
    PAdESSignatureParameters parameters;
    private boolean visibleSignature = true;

    public FirmadorPAdES(GUIInterface gui) {
        super(gui);
    }

    public DSSDocument sign(DSSDocument toSignDocument, PasswordProtection pin, String reason, String location, String contactInfo, String image, Boolean hideSignatureAdvice) {
        CertificateVerifier verifier = this.getCertificateVerifier();
        PAdESService service = new PAdESService(verifier);
        service.setPdfObjFactory(new PdfBoxNativeObjectFactory());
        parameters = new PAdESSignatureParameters();
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
            parameters.setSignatureLevel(SignatureLevel.PAdES_BASELINE_LTA);
            parameters.setContentSize(13312);
            parameters.setDigestAlgorithm(DigestAlgorithm.SHA256);
            parameters.setSigningCertificate(certificate);
            if (reason != null && !reason.trim().isEmpty()) parameters.setReason(reason);
            if (location != null && !location.trim().isEmpty()) parameters.setLocation(location);
            if (contactInfo != null && !contactInfo .trim().isEmpty()) parameters.setContactInfo(contactInfo);
            OnlineTSPSource onlineTSPSource = new OnlineTSPSource(TSA_URL);
            service.setTspSource(onlineTSPSource);
            Date date = new Date();
            if (visibleSignature) appendVisibleSignature(certificate, date, reason, location, contactInfo, image, hideSignatureAdvice);
            parameters.bLevel().setSigningDate(date);
            ToBeSigned dataToSign = service.getDataToSign(toSignDocument, parameters);
            signatureValue = token.sign(dataToSign, parameters.getDigestAlgorithm(), privateKey);
        } catch (DSSException|AlertException|Error e) {
            if (Throwables.getRootCause(e).getLocalizedMessage().equals("The new signature field position overlaps with an existing annotation!")) {
                gui.showMessage("No se puede firmar: el campo de firma está solapándose sobre otra firma o anotación existente.<br>" +
                    "Debe mover la firma para ubicarla en otra posición que no tape las existentes.");
                return null;
            } else gui.showError(Throwables.getRootCause(e));
        }

        try {
            signedDocument = service.signDocument(toSignDocument, parameters, signatureValue);
        } catch (Exception e) {
            e.printStackTrace();
            gui.showMessage("Aviso: no se ha podido agregar el sello de tiempo y la información de revocación porque es posible<br>" +
                "que haya problemas de conexión a Internet o con los servidores del sistema de Firma Digital.<br>" +
                "Detalle del error: " + Throwables.getRootCause(e) + "<br><br>" +
                "Se ha agregado una firma básica solamente. No obstante, si el sello de tiempo resultara importante<br>" +
                "para este documento, debería agregarse lo antes posible antes de enviarlo al destinatario.<br><br>" +
                "Si lo prefiere, puede cancelar el guardado del documento firmado e intentar firmarlo más tarde.<br>");
            parameters.setSignatureLevel(SignatureLevel.PAdES_BASELINE_B);
            try {
                signedDocument = service.signDocument(toSignDocument, parameters, signatureValue);
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

        parameters.setContentSize(3072);
        CertificateVerifier verifier = this.getCertificateVerifier();
        PAdESService service = new PAdESService(verifier);
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

    public DSSDocument timestamp(DSSDocument documentToTimestamp, Boolean visibleTimestamp) {
        CertificateVerifier verifier = this.getCertificateVerifier();
        PAdESService service = new PAdESService(verifier);
        DSSDocument timestampedDocument = null;
        try {
            OnlineTSPSource onlineTSPSource = new OnlineTSPSource(TSA_URL);
            service.setTspSource(onlineTSPSource);
        } catch (DSSException|Error e) {
            gui.showError(Throwables.getRootCause(e));
        }
        try {
            PAdESTimestampParameters timestampParameters = new PAdESTimestampParameters();
            if (visibleTimestamp) {
                SignatureImageParameters imageParameters = new SignatureImageParameters();
                imageParameters.setRotation(VisualSignatureRotation.AUTOMATIC);
                imageParameters.getFieldParameters().setOriginX(0);
                imageParameters.getFieldParameters().setOriginY(0);
                SignatureImageTextParameters textParameters = new SignatureImageTextParameters();
                textParameters.setFont(new DSSJavaFont(new Font(Font.SANS_SERIF, Font.PLAIN, 7)));
                SimpleDateFormat date = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss a");
                date.setTimeZone(TimeZone.getTimeZone("America/Costa_Rica"));
                textParameters.setText("Este documento incluye un sello de tiempo de la\n" +
                    "Autoridad de Sellado de Tiempo (TSA) del SINPE.\n" +
                    "Fecha de solicitud a la TSA: " + date.format(new Date()));
                textParameters.setBackgroundColor(new Color(255, 255, 255, 0));
                textParameters.setSignerTextPosition(SignerTextPosition.RIGHT);
                imageParameters.setTextParameters(textParameters);
                imageParameters.getFieldParameters().setPage(1);
                timestampParameters.setImageParameters(imageParameters);
            }
            timestampedDocument = service.timestamp(documentToTimestamp, timestampParameters);
        } catch (Exception e) {
            e.printStackTrace();
            gui.showError(Throwables.getRootCause(e));
        }
        return timestampedDocument;
    }

    public void setVisibleSignature(boolean visibleSignature) {
        this.visibleSignature = visibleSignature;
    }

    public void addVisibleSignature(int page, int x, int y) {
        this.page = page;
        this.x = x;
        this.y = y;
    }

    private void appendVisibleSignature(CertificateToken certificate, Date date, String reason, String location, String contactInfo, String image, Boolean hideAdvice) {
        SignatureImageParameters imageParameters = new SignatureImageParameters();
        imageParameters.setRotation(VisualSignatureRotation.AUTOMATIC);
        imageParameters.getFieldParameters().setOriginX(x);
        imageParameters.getFieldParameters().setOriginY(y);
        SignatureImageTextParameters textParameters = new SignatureImageTextParameters();
        textParameters.setFont(new DSSJavaFont(new Font(Font.SANS_SERIF, Font.PLAIN, 7)));
        String cn = DSSASN1Utils.getSubjectCommonName(certificate);
        X500PrincipalHelper subject = certificate.getSubject();
        String o = DSSASN1Utils.extractAttributeFromX500Principal(BCStyle.O, subject);
        String sn = DSSASN1Utils.extractAttributeFromX500Principal(BCStyle.SERIALNUMBER, subject);
        SimpleDateFormat fecha = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss a");
        fecha.setTimeZone(TimeZone.getTimeZone("America/Costa_Rica"));
        String additionalText = "";
        if (hideAdvice != null && !hideAdvice) {
            additionalText = "Verifique siempre cualquier firma digital,\neste texto no tiene validez por sí mismo.";
        }
        Boolean hasReason = false;
        Boolean hasLocation = false;
        if (reason != null && !reason.trim().isEmpty()) {
            hasReason = true;
            additionalText = "Razón: " + reason + "\n";
        }
        if (location != null && !location.trim().isEmpty()) {
            hasLocation = true;
            if (hasReason) additionalText += "Lugar: " + location;
            else additionalText = "Lugar: " + location;
        }
        if (contactInfo != null && !contactInfo .trim().isEmpty()) {
            if (hasReason || hasLocation) additionalText += "  Contacto: " + contactInfo;
            else additionalText = "Contacto: " + contactInfo;
        }
        textParameters.setText(cn + "\n" + o + ", " + sn + ".\nFecha declarada: " + fecha.format(date) + "\n" + additionalText);
        textParameters.setBackgroundColor(new Color(255, 255, 255, 0));
        textParameters.setSignerTextPosition(SignerTextPosition.RIGHT);
        imageParameters.setTextParameters(textParameters);
        try {
            if (image != null && !image.trim().isEmpty()) imageParameters.setImage(new InMemoryDocument(Utils.toByteArray(new URL(image).openStream())));
        } catch (IOException e) {
            e.printStackTrace();
        }
        imageParameters.getFieldParameters().setPage(page);
        parameters.setImageParameters(imageParameters);
    }

}
