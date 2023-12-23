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

package cr.libre.firmador;

import java.awt.Font;
import java.awt.Rectangle;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import cr.libre.firmador.gui.GUIInterface;
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
import eu.europa.esig.dss.pades.SignatureFieldParameters;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FirmadorPAdES extends CRSigner {
    final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private int page = 1, x, y;
    PAdESSignatureParameters parameters;
    private boolean visibleSignature = true;
    private Settings settings;

    public FirmadorPAdES(GUIInterface gui) {
        super(gui);
        settings = SettingsManager.getInstance().getAndCreateSettings();
    }

    public DSSDocument sign(DSSDocument toSignDocument, CardSignInfo card, String reason, String location, String contactInfo, String image, Boolean hideSignatureAdvice) {
        CertificateVerifier verifier = this.getCertificateVerifier();
        PAdESService service = new PAdESService(verifier);
        service.setPdfObjFactory(new PdfBoxNativeObjectFactory());
        parameters = new PAdESSignatureParameters();
        SignatureValue signatureValue = null;
        DSSDocument signedDocument = null;
        SignatureTokenConnection token = null;
        gui.nextStep("Obteniendo servicios de verificación de certificados");
        if (image == null) image = settings.getImage();
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
            gui.nextStep("Obteniendo manejador de llaves privadas");
        } catch (Exception e) {
            LOG.error("Error al acceder al objeto de llave del dispositivo", e);
            gui.showError(FirmadorUtils.getRootCause(e));
            return null;
        }
        try {
            gui.nextStep("Obteniendo certificados de la tarjeta");
            CertificateToken certificate = privateKey.getCertificate();
            parameters.setSignatureLevel(settings.getPAdESLevel());
            parameters.setAppName("Firmador " + settings.getVersion() + ", https://firmador.libre.cr");
            parameters.setContentSize(13312);
            parameters.setDigestAlgorithm(DigestAlgorithm.SHA256);
            parameters.setSigningCertificate(certificate);
            if (reason != null && !reason.trim().isEmpty()) parameters.setReason(reason.replaceAll("\t", " "));
            if (location != null && !location.trim().isEmpty()) parameters.setLocation(location.replaceAll("\t", " "));
            if (contactInfo != null && !contactInfo .trim().isEmpty()) parameters.setContactInfo(contactInfo.replaceAll("\t", " "));
            OnlineTSPSource onlineTSPSource = new OnlineTSPSource(TSA_URL);
            gui.nextStep("Obteniendo servicios TSP");
            service.setTspSource(onlineTSPSource);
            Date date = new Date();
            if (visibleSignature) appendVisibleSignature(certificate, date, reason, location, contactInfo, image, hideSignatureAdvice);
            gui.nextStep("Agregando representación gráfica de la firma");
            parameters.bLevel().setSigningDate(date);


























            ToBeSigned dataToSign = service.getDataToSign(toSignDocument, parameters);

            gui.nextStep("Obteniendo estructura de datos a firmar");
            signatureValue = token.sign(dataToSign, parameters.getDigestAlgorithm(), privateKey);
        } catch (DSSException|AlertException|Error e) {
            if (FirmadorUtils.getRootCause(e).getLocalizedMessage().equals("The new signature field position overlaps with an existing annotation!")) {
                LOG.error("Error al firmar (traslape de firma)", e);
                e.printStackTrace();
                gui.showMessage("No se puede firmar: el campo de firma está solapándose sobre otra firma o anotación existente.<br>" +
                    "Debe mover la firma para ubicarla en otra posición que no tape las existentes.<br><br>" +
                    "Si no contiene firmas previas, puede abrir el PDF con un visor de documentos e imprimirlo como fichero PDF.<br>" +
                    "El PDF resultante quedará 'aplanado' y podrá firmarse sin problemas.");
                return null;
            } else {
                LOG.error("Error al solicitar firma al dispositivo", e);
                gui.showError(FirmadorUtils.getRootCause(e));
            }
        } catch (IllegalArgumentException e) {
            if (FirmadorUtils.getRootCause(e).getMessage().contains("is expired")) {
                LOG.warn("El cetificado seleccionado para firmar ha vencido", e);
                e.printStackTrace();
                gui.showMessage("Certificado vencido, no se puede realizar la firma");
                return null;
            } else {
                LOG.error("Error al solicitar firma al dispositivo", e);
                gui.showError(FirmadorUtils.getRootCause(e));
            }
        }

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
            parameters.setSignatureLevel(SignatureLevel.PAdES_BASELINE_B);
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

    public DSSDocument timestamp(DSSDocument documentToTimestamp, Boolean visibleTimestamp) {
        CertificateVerifier verifier = this.getCertificateVerifier();
        PAdESService service = new PAdESService(verifier);
        DSSDocument timestampedDocument = null;
        try {
            OnlineTSPSource onlineTSPSource = new OnlineTSPSource(TSA_URL);
            service.setTspSource(onlineTSPSource);
        } catch (DSSException|Error e) {
            LOG.error("Error al preparar el servicio de sello de tiempo)", e);
            gui.showError(FirmadorUtils.getRootCause(e));
        }
        try {
            PAdESTimestampParameters timestampParameters = new PAdESTimestampParameters();
            if (visibleTimestamp) {
                SignatureImageParameters imageParameters = new SignatureImageParameters();
                imageParameters.getFieldParameters().setRotation(VisualSignatureRotation.AUTOMATIC);
                imageParameters.getFieldParameters().setOriginX(0);
                imageParameters.getFieldParameters().setOriginY(0);
                SignatureImageTextParameters textParameters = new SignatureImageTextParameters();
                textParameters.setFont(new DSSJavaFont(new Font(settings.getFontName(settings.font, true), settings.getFontStyle(settings.font), settings.fontSize)));
                SimpleDateFormat date = new SimpleDateFormat(settings.getDateFormat());
                date.setTimeZone(TimeZone.getTimeZone("America/Costa_Rica"));
                textParameters.setText("Este documento incluye un sello de tiempo de la\n" +
                    "Autoridad de Sellado de Tiempo (TSA) del SINPE.\n" +
                    "Fecha de solicitud a la TSA: " + date.format(new Date()));
                textParameters.setTextColor(settings.getFontColor());
                textParameters.setBackgroundColor(settings.getBackgroundColor());
                textParameters.setSignerTextPosition(SignerTextPosition.RIGHT);
                imageParameters.setTextParameters(textParameters);
                imageParameters.getFieldParameters().setPage(1);
                timestampParameters.setImageParameters(imageParameters);
                timestampParameters.setAppName("Firmador " + settings.getVersion() + ", https://firmador.libre.cr");
            }
            timestampedDocument = service.timestamp(documentToTimestamp, timestampParameters);
        } catch (Exception e) {
            LOG.error("Error al procesar información para al agregar un sello de tiempo independiente)", e);
            gui.showError(FirmadorUtils.getRootCause(e));
        }
        return timestampedDocument;
    }

    public void setVisibleSignature(boolean visibleSignature) {
        this.visibleSignature = visibleSignature;
    }

    public void addVisibleSignature(int page, Rectangle rect) {
        this.page = page;
        this.x = rect.x;
        this.y = rect.y;
        //this.width=(float)rect.width;
        //this.height=(float)rect.height;
    }
/*
    public void addVisibleSignature(int page, int x, int y) { // FIXME this seems unused
        this.page = page;
        this.x = x;
        this.y = y;
        this.width=settings.signwidth;
        this.height=settings.signheight;
    }
*/
    private void appendVisibleSignature(CertificateToken certificate, Date date, String reason, String location, String contactInfo, String image, Boolean hideAdvice) {
        SignatureImageParameters imageParameters = new SignatureImageParameters();
        imageParameters.getFieldParameters().setRotation(VisualSignatureRotation.AUTOMATIC);
        SignatureFieldParameters fparamet = imageParameters.getFieldParameters();
        fparamet.setOriginX(this.x);
        fparamet.setOriginY(this.y);
        //fparamet.setWidth(width);
        //fparamet.setHeight(height);

        SignatureImageTextParameters textParameters = new SignatureImageTextParameters();

        textParameters.setFont(new DSSJavaFont(new Font(settings.getFontName(settings.font, true), settings.getFontStyle(settings.font), settings.fontSize)));
        String cn = DSSASN1Utils.getSubjectCommonName(certificate);
        X500PrincipalHelper subject = certificate.getSubject();
        String o = DSSASN1Utils.extractAttributeFromX500Principal(BCStyle.O, subject);
        String sn = DSSASN1Utils.extractAttributeFromX500Principal(BCStyle.SERIALNUMBER, subject);
        SimpleDateFormat fecha = new SimpleDateFormat(settings.getDateFormat());
        fecha.setTimeZone(TimeZone.getTimeZone("America/Costa_Rica"));
        String additionalText = "";
        if (hideAdvice != null && !hideAdvice) {
            additionalText = settings.getDefaultSignMessage();
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
        textParameters.setText(cn + "\n" + o + ", " + sn + ".\nFecha declarada: " + fecha.format(date) + "\n" + additionalText.replaceAll("\t", " "));
        textParameters.setTextColor(settings.getFontColor());
        textParameters.setBackgroundColor(settings.getBackgroundColor());

        textParameters.setSignerTextPosition(settings.getFontAlignment());

        imageParameters.setTextParameters(textParameters);
        try {
            if (image != null && !image.trim().isEmpty())
                imageParameters.setImage(new InMemoryDocument(Utils.toByteArray(new URL(image).openStream())));
        } catch (IOException e) {
            LOG.error("Error al procesar la imagen para la representación visual de firma)", e);
            e.printStackTrace();
        }
        imageParameters.getFieldParameters().setPage(page);
        parameters.setImageParameters(imageParameters);
    }

}
