/* Firmador is a program to sign documents using AdES standards.

Copyright (C) 2020 Firmador authors.

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
import java.security.KeyStore.PasswordProtection;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import javax.security.auth.x500.X500Principal;

import app.firmador.gui.GUIInterface;
import com.google.common.base.Throwables;
import eu.europa.esig.dss.enumerations.DigestAlgorithm;
import eu.europa.esig.dss.enumerations.SignatureLevel;
import eu.europa.esig.dss.model.DSSDocument;
import eu.europa.esig.dss.model.DSSException;
import eu.europa.esig.dss.model.SignatureValue;
import eu.europa.esig.dss.model.ToBeSigned;
import eu.europa.esig.dss.model.x509.CertificateToken;
import eu.europa.esig.dss.pades.DSSJavaFont;
import eu.europa.esig.dss.pades.PAdESSignatureParameters;
import eu.europa.esig.dss.pades.SignatureImageParameters;
import eu.europa.esig.dss.pades.SignatureImageTextParameters;
import eu.europa.esig.dss.pades.signature.PAdESService;

import eu.europa.esig.dss.pdf.pdfbox.PdfBoxNativeObjectFactory;
import eu.europa.esig.dss.service.tsp.OnlineTSPSource;
import eu.europa.esig.dss.spi.DSSASN1Utils;
import eu.europa.esig.dss.token.DSSPrivateKeyEntry;
import eu.europa.esig.dss.token.SignatureTokenConnection;
import eu.europa.esig.dss.validation.CertificateVerifier;
import org.bouncycastle.asn1.x500.style.BCStyle;

public class FirmadorPAdES extends CRSigner {

    private int page, x, y;
    PAdESSignatureParameters parameters;
    private boolean visible_signature = true; 
    
    public FirmadorPAdES(GUIInterface gui) {
        super(gui);
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

    
    private void appendVisibleSignature(CertificateToken certificate, Date date) {
        SignatureImageParameters imageParameters =
                new SignatureImageParameters();
            imageParameters.setxAxis(x);
            imageParameters.setyAxis(y);
            SignatureImageTextParameters textParameters =
                new SignatureImageTextParameters();
            textParameters.setFont(
                new DSSJavaFont(new Font(Font.SANS_SERIF, Font.PLAIN, 7)));
    	  String cn = DSSASN1Utils.getSubjectCommonName(certificate);
          X500Principal principal = certificate.getSubjectX500Principal();
          String o = DSSASN1Utils.extractAttributeFromX500Principal(
              BCStyle.O, principal);
          String sn = DSSASN1Utils.extractAttributeFromX500Principal(
              BCStyle.SERIALNUMBER, principal);

          String fecha = new SimpleDateFormat("dd/MM/yyyy hh:mm a")
              .format(date);
          textParameters.setText(
              "Firmado por " + cn + "\n" +
              o + ", " + sn + ". Fecha declarada: " + fecha + "\n" +
              "Esta representación visual no es una fuente de confianza, " +
              "valide siempre la firma.");
          textParameters.setBackgroundColor(new Color(255, 255, 255, 0));
          imageParameters.setTextParameters(textParameters);
          imageParameters.setPage(page);
          parameters.setSignatureImageParameters(imageParameters);
    }
    
    public DSSDocument sign(DSSDocument toSignDocument,
        PasswordProtection pin) {



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
            parameters.setDigestAlgorithm(DigestAlgorithm.SHA256);
            parameters.setSignatureLevel(SignatureLevel.PAdES_BASELINE_LT);
            parameters.setSignatureSize(13312);
            parameters.setSigningCertificate(certificate);
            parameters.setSignWithExpiredCertificate(true);

            List<CertificateToken> certificateChain = getCertificateChain(
                verifier, parameters);
            parameters.setCertificateChain(certificateChain);

            OnlineTSPSource onlineTSPSource = new OnlineTSPSource(TSA_URL);
            service.setTspSource(onlineTSPSource);
            Date date = new Date();
            
            if(visible_signature) {
            	appendVisibleSignature(certificate, date);
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
        parameters.setSignatureSize(3072);

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
