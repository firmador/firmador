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

package cr.libre.firmador.gui;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Paths;
import java.security.KeyStore.PasswordProtection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import cr.libre.firmador.CardSignInfo;
//import cr.libre.firmador.FirmadorCAdES;
import cr.libre.firmador.FirmadorOpenDocument;
import cr.libre.firmador.FirmadorPAdES;
import cr.libre.firmador.FirmadorXAdES;
import cr.libre.firmador.SupportedMimeTypeEnum;
import cr.libre.firmador.FirmadorUtils;
import cr.libre.firmador.plugins.PluginManager;

import eu.europa.esig.dss.enumerations.MimeType;
import eu.europa.esig.dss.enumerations.MimeTypeEnum;
import eu.europa.esig.dss.model.DSSDocument;
import eu.europa.esig.dss.model.FileDocument;
import org.apache.pdfbox.pdmodel.PDDocument;

public class GUIArgs implements GUIInterface {

    private String documenttosign;
    private String documenttosave;
    //private String pkcs12file = "";
    @SuppressWarnings("unused")
    private int slot = -1;
    private Boolean timestamp = false;
    private Boolean visibleTimestamp = false;

    public void loadGUI() {
        String fileName = getDocumentToSign();
        if (fileName != null) {
            DSSDocument toSignDocument = new FileDocument(fileName);
            DSSDocument signedDocument = null;
            if (!timestamp && !visibleTimestamp) {
                CardSignInfo card = getPin();
                MimeType mimeType = toSignDocument.getMimeType();
                if (mimeType == MimeTypeEnum.PDF) {
                    FirmadorPAdES firmador = new FirmadorPAdES(this);
                    signedDocument = firmador.sign(toSignDocument, card, null, null, null, null, null);
                } else if (mimeType == MimeTypeEnum.ODG || mimeType == MimeTypeEnum.ODP || mimeType == MimeTypeEnum.ODS || mimeType == MimeTypeEnum.ODT) {
                    FirmadorOpenDocument firmador = new FirmadorOpenDocument(this);
                    signedDocument = firmador.sign(toSignDocument, card);
                } else if (mimeType == MimeTypeEnum.XML) {
                    FirmadorXAdES firmador = new FirmadorXAdES(this);
                    signedDocument = firmador.sign(toSignDocument, card);
                } else {
                    FirmadorXAdES firmador = new FirmadorXAdES(this);
                    signedDocument = firmador.sign(toSignDocument, card);
                }
                card.destroyPin();

            } else {
                FirmadorPAdES firmador = new FirmadorPAdES(this);
                signedDocument = firmador.timestamp(toSignDocument, visibleTimestamp);
            }
            if (signedDocument != null) {
                fileName = getPathToSave("");
                try {
                    signedDocument.save(fileName);
                    showMessage("Documento guardado satisfactoriamente en " + fileName);
                } catch (IOException e) {
                    showError(FirmadorUtils.getRootCause(e));
                }
            }
        }
    }

    public void setArgs(String[] args) {
        List<String> arguments = new ArrayList<String>();
        for (String params : args) {
            if (!params.startsWith("-")) arguments.add(params);
            else if (params.startsWith("-slot")) slot = Integer.parseInt(params.replace("-slot", ""));
            else if (params.startsWith("-timestamp")) timestamp = true;
            else if (params.startsWith("-visible-timestamp")) visibleTimestamp = true;
        }
        documenttosign = Paths.get(arguments.get(0)).toAbsolutePath().toString();
        documenttosave = Paths.get(arguments.get(1)).toAbsolutePath().toString();
        //if (arguments.size() > 2) pkcs12file = Paths.get(arguments.get(2)).toAbsolutePath().toString();
    }

    public void showError(Throwable error) {
        System.err.println("Excepción: " + error.getClass().getName());
        System.err.println("Mensaje: " + error.getMessage());
        System.exit(1);
    }

    public String getDocumentToSign() {
        return documenttosign;
    }

    public String getPathToSave(String extension) {
        return documenttosave;
    }

    public CardSignInfo getPin() {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        char[] input = new char[128];
        try {
            while (reader.read(input, 0, input.length) != -1);
            reader.close();
        } catch (IOException e) {
            showError(FirmadorUtils.getRootCause(e));
        }
        int passwordLength = 0;
        for (char character: input) {
            if (character == '\0' || character == '\r' || character == '\n') break;
            passwordLength++;
        }
        char[] password = new char[passwordLength];
        for (int i = 0; i < passwordLength; i++) password[i] = input[i];
        Arrays.fill(input, '\0');
        PasswordProtection pin = new PasswordProtection(password);
        Arrays.fill(password, '\0');
        return new CardSignInfo(pin);
    }

    public void showMessage(String message) {
         System.out.println(message);
    }

    public void setPluginManager(PluginManager pluginManager) {
    }

    public void loadDocument(String fileName) {
    }

    public void loadDocument(SupportedMimeTypeEnum mimeType, PDDocument doc) {
    }

    public void extendDocument() {
    }

    public String getPathToSaveExtended(String extension) {
        return null;
    }

    public boolean signDocuments() {
        return true;
    }

    public void displayFunctionality(String functionality) {
    }

    public void nextStep(String msg) {
    }

}
