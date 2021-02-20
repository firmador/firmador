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

package app.firmador;

import java.awt.Color;
import java.awt.Font;
import java.io.IOException;
import java.net.URL;
import java.security.KeyStore.PasswordProtection;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import app.firmador.gui.GUIInterface;
import com.google.common.base.Throwables;
import eu.europa.esig.dss.enumerations.DigestAlgorithm;
import eu.europa.esig.dss.enumerations.SignatureLevel;
import eu.europa.esig.dss.enumerations.SignerTextPosition;
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
    private boolean visible_signature = true;

    public FirmadorPAdES(GUIInterface gui) {
        super(gui);
    }

    public DSSDocument timestamp(DSSDocument documentToTimestamp) {
        CertificateVerifier verifier = this.getCertificateVerifier();
        verifier.setCheckRevocationForUntrustedChains(true);
        PAdESService service = new PAdESService(verifier);
        DSSDocument timestampedDocument = null;
        try {
            OnlineTSPSource onlineTSPSource = new OnlineTSPSource(TSA_URL);
            service.setTspSource(onlineTSPSource);
        } catch (DSSException|Error e) {
            gui.showError(Throwables.getRootCause(e));
        }
        try {
            timestampedDocument = service.timestamp(documentToTimestamp,
                new PAdESTimestampParameters());
        } catch (Exception e) {
            e.printStackTrace();
            gui.showError(Throwables.getRootCause(e));
        }
        return timestampedDocument;
    }

    public DSSDocument sign(DSSDocument toSignDocument,
        PasswordProtection pin, String reason, String location,
        String contactInfo, String image) {

        CertificateVerifier verifier = this.getCertificateVerifier();
        verifier.setCheckRevocationForUntrustedChains(true);
        PAdESService service = new PAdESService(verifier);
        service.setPdfObjFactory(new PdfBoxNativeObjectFactory());
        parameters = new PAdESSignatureParameters();
        SignatureValue signatureValue = null;
        DSSDocument signedDocument = null;
        try {
            SignatureTokenConnection token = getSignatureConnection(pin);
            DSSPrivateKeyEntry privateKey = getPrivateKey(token);
            CertificateToken certificate = privateKey.getCertificate();
            parameters.setSignatureLevel(SignatureLevel.PAdES_BASELINE_LT);
            parameters.setContentSize(13312);
            parameters.setDigestAlgorithm(DigestAlgorithm.SHA256);
            parameters.setSigningCertificate(certificate);
            if (reason != null && !reason.trim().isEmpty())
                parameters.setReason(reason);
            if (location != null && !location.trim().isEmpty())
                parameters.setLocation(location);
            if (contactInfo != null && !contactInfo .trim().isEmpty())
                parameters.setContactInfo(contactInfo);
            List<CertificateToken> certificateChain = getCertificateChain(
                verifier, parameters);
            parameters.setCertificateChain(certificateChain);
            OnlineTSPSource onlineTSPSource = new OnlineTSPSource(TSA_URL);
            service.setTspSource(onlineTSPSource);
            Date date = new Date();
            if (visible_signature) {
                appendVisibleSignature(certificate, date, reason, location,
                    contactInfo, image);
            }
            parameters.bLevel().setSigningDate(date);
            ToBeSigned dataToSign = service.getDataToSign(toSignDocument,
                parameters);
            signatureValue = token.sign(dataToSign,
                parameters.getDigestAlgorithm(), privateKey);
        } catch (DSSException|Error e) {
            gui.showError(Throwables.getRootCause(e));
        }

        try {
            signedDocument = service.signDocument(toSignDocument, parameters,
                signatureValue);
        } catch (Exception e) {
            e.printStackTrace();
            gui.showMessage(
                "Aviso: no se ha podido agregar el sello de tiempo y la " +
                "información de revocación porque es posible<br>" +
                "que haya problemas de conexión a Internet o con los " +
                "servidores del sistema de Firma Digital.<br>" +
                "Detalle del error: " + Throwables.getRootCause(e) + "<br>" +
                "<br>" +
                "Se ha agregado una firma básica solamente. No obstante, si " +
                "el sello de tiempo resultara importante<br>" +
                "para este documento, debería agregarse lo antes posible " +
                "antes de enviarlo al destinatario.<br>" +
                "<br>" +
                "Si lo prefiere, puede cancelar el guardado del documento " +
                "firmado e intentar firmarlo más tarde.<br>");

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
        PAdESSignatureParameters parameters =
            new PAdESSignatureParameters();
        parameters.setSignatureLevel(SignatureLevel.PAdES_BASELINE_LTA);
        parameters.setContentSize(3072);

        CertificateVerifier verifier = this.getCertificateVerifier();
        verifier.setCheckRevocationForUntrustedChains(true);
        PAdESService service = new PAdESService(verifier);
        OnlineTSPSource onlineTSPSource = new OnlineTSPSource(TSA_URL);
        service.setTspSource(onlineTSPSource);
        DSSDocument extendedDocument = null;
        try {
            extendedDocument = service.extendDocument(document,
                parameters);
        } catch (Exception e) {
            e.printStackTrace();
            gui.showMessage(
                "Aviso: no se ha podido agregar el sello de tiempo y la " +
                "información de revocación porque es posible<br>" +
                "que haya problemas de conexión a Internet o con los " +
                "servidores del sistema de Firma Digital.<br>" +
                "Detalle del error: " + Throwables.getRootCause(e) + "<br>" +
                "<br>" +
                "Inténtelo de nuevo más tarde. Si el problema persiste, " +
                "compruebe su conexión o verifique<br>" +
                "que no se trata de un problema de los servidores de Firma " +
                "Digital o de un error de este programa.<br>");
        }

        return extendedDocument;
    }

    public boolean isVisible_signature() {
        return visible_signature;
    }


    public void setVisible_signature(boolean visible_signature) {
        this.visible_signature = visible_signature;
    }

    public void addVisibleSignature(int page, int x, int y) {
        this.page = page;
        this.x = x;
        this.y = y;
    }

    private void appendVisibleSignature(CertificateToken certificate,
        Date date, String reason, String location, String contactInfo,
        String image) {
        SignatureImageParameters imageParameters =
            new SignatureImageParameters();
        imageParameters.getFieldParameters().setOriginX(x);
        imageParameters.getFieldParameters().setOriginY(y);
        SignatureImageTextParameters textParameters =
            new SignatureImageTextParameters();
        textParameters.setFont(
            new DSSJavaFont(new Font(Font.SANS_SERIF, Font.PLAIN, 7)));
        String cn = DSSASN1Utils.getSubjectCommonName(certificate);
        X500PrincipalHelper subject = certificate.getSubject();
        String o = DSSASN1Utils.extractAttributeFromX500Principal(
            BCStyle.O, subject);
        String sn = DSSASN1Utils.extractAttributeFromX500Principal(
            BCStyle.SERIALNUMBER, subject);
        String fecha = new SimpleDateFormat("dd/MM/yyyy hh:mm a")
            .format(date);
        String additionalText =
            "Esta representación visual no es fuente" + "\n" +
            "de confianza. Valide siempre la firma.";
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
            if (hasReason || hasLocation)
                additionalText += "  Contacto: " + contactInfo;
            else additionalText = "Contacto: " + contactInfo;
        }
        textParameters.setText(
            "Firmado por " + cn + "\n" +
            o + ", " + sn + "." + "\n" +
            "Fecha declarada: " + fecha + "\n" +
            additionalText);
        textParameters.setBackgroundColor(new Color(255, 255, 255, 0));
        textParameters.setSignerTextPosition(SignerTextPosition.RIGHT);
        imageParameters.setTextParameters(textParameters);
        try {
            if (image != null && !image.trim().isEmpty())
                imageParameters.setImage(new InMemoryDocument(
                    Utils.toByteArray(new URL(image).openStream())));
        } catch (IOException e) { e.printStackTrace(); }
        imageParameters.getFieldParameters().setPage(page);
        parameters.setImageParameters(imageParameters);
    }

}
