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




import cr.libre.firmador.gui.GUIInterface;
import com.google.common.base.Throwables;
import eu.europa.esig.dss.alert.exception.AlertException;
import eu.europa.esig.dss.enumerations.DigestAlgorithm;
import eu.europa.esig.dss.enumerations.SignatureLevel;


import eu.europa.esig.dss.model.DSSDocument;
import eu.europa.esig.dss.model.DSSException;

import eu.europa.esig.dss.model.SignatureValue;
import eu.europa.esig.dss.model.ToBeSigned;
import eu.europa.esig.dss.model.x509.CertificateToken;

import eu.europa.esig.dss.enumerations.SignaturePackaging;
import eu.europa.esig.dss.cades.CAdESSignatureParameters;



import eu.europa.esig.dss.cades.signature.CAdESService;

import eu.europa.esig.dss.service.tsp.OnlineTSPSource;

import eu.europa.esig.dss.token.DSSPrivateKeyEntry;
import eu.europa.esig.dss.token.SignatureTokenConnection;

import eu.europa.esig.dss.validation.CertificateVerifier;


public class FirmadorCAdES extends CRSigner {


    CAdESSignatureParameters parameters;


    public FirmadorCAdES(GUIInterface gui) {
        super(gui);
    }

    public DSSDocument sign(DSSDocument toSignDocument, PasswordProtection pin) {
        CertificateVerifier verifier = this.getCertificateVerifier();
        CAdESService service = new CAdESService(verifier);

        parameters = new CAdESSignatureParameters();
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
            parameters.setSignatureLevel(SignatureLevel.CAdES_BASELINE_LT);
            parameters.setSignaturePackaging(SignaturePackaging.DETACHED);
            parameters.setDigestAlgorithm(DigestAlgorithm.SHA256);
            parameters.setSigningCertificate(certificate);



            OnlineTSPSource onlineTSPSource = new OnlineTSPSource(TSA_URL);
            service.setTspSource(onlineTSPSource);



            ToBeSigned dataToSign = service.getDataToSign(toSignDocument, parameters);
            signatureValue = token.sign(dataToSign, parameters.getDigestAlgorithm(), privateKey);
        } catch (DSSException|Error e) {
            gui.showError(Throwables.getRootCause(e));
        }





        try {
            signedDocument = service.signDocument(toSignDocument, parameters, signatureValue);
        } catch (Exception e) {
            e.printStackTrace();
            gui.showMessage("Aviso: no se ha podido agregar el sello de tiempo y la informaci??n de revocaci??n porque es posible<br>" +
                "que haya problemas de conexi??n a Internet o con los servidores del sistema de Firma Digital.<br>" +
                "Detalle del error: " + Throwables.getRootCause(e) + "<br><br>" +
                "Se ha agregado una firma b??sica solamente. No obstante, si el sello de tiempo resultara importante<br>" +
                "para este documento, deber??a agregarse lo antes posible antes de enviarlo al destinatario.<br><br>" +
                "Si lo prefiere, puede cancelar el guardado del documento firmado e intentar firmarlo m??s tarde.<br>");
            parameters.setSignatureLevel(SignatureLevel.CAdES_BASELINE_B);
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
            e.printStackTrace();
            gui.showMessage("Aviso: no se ha podido agregar el sello de tiempo y la informaci??n de revocaci??n porque es posible<br>" +
                "que haya problemas de conexi??n a Internet o con los servidores del sistema de Firma Digital.<br>" +
                "Detalle del error: " + Throwables.getRootCause(e) + "<br><br>" +
                "Int??ntelo de nuevo m??s tarde. Si el problema persiste, compruebe su conexi??n o verifique<br>" +
                "que no se trata de un problema de los servidores de Firma Digital o de un error de este programa.<br>");
        }
        return extendedDocument;
    }

}
