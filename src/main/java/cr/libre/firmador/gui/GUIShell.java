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
import java.io.Console;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Paths;
import java.security.KeyStore.PasswordProtection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import cr.libre.firmador.MessageUtils;
import org.apache.pdfbox.pdmodel.PDDocument;

import cr.libre.firmador.Settings;
import cr.libre.firmador.SettingsManager;
import cr.libre.firmador.cards.CardSignInfo;
import cr.libre.firmador.documents.Document;
import cr.libre.firmador.documents.DocumentChangeListener;
import cr.libre.firmador.documents.SupportedMimeTypeEnum;
//import cr.libre.firmador.FirmadorXAdES;
//import cr.libre.firmador.Settings;
//import cr.libre.firmador.SettingsManager;
import cr.libre.firmador.plugins.PluginManager;
import cr.libre.firmador.signers.FirmadorPAdES;
import cr.libre.firmador.signers.FirmadorUtils;
import eu.europa.esig.dss.model.DSSDocument;
import eu.europa.esig.dss.model.FileDocument;

public class GUIShell implements GUIInterface, DocumentChangeListener {

    private Settings settings;
    private PluginManager pluginManager;

    public void loadGUI() {
        settings = SettingsManager.getInstance().getAndCreateSettings();
        String fileName = getDocumentToSign();
        if (fileName != null) {
            // FirmadorCAdES firmador = new FirmadorCAdES(this);
            // FirmadorOpenDocument firmador = new FirmadorOpenDocument(this);
            FirmadorPAdES firmador = new FirmadorPAdES(this);
            // FirmadorXAdES firmador = new FirmadorXAdES(this);
            CardSignInfo card = getPin();
            DSSDocument toSignDocument = new FileDocument(fileName);
            DSSDocument signedDocument = firmador.sign(toSignDocument, card, settings);
            card.destroyPin();
            if (signedDocument != null) {
                fileName = getPathToSave("");
                try {
                    signedDocument.save(fileName);
                    showMessage(MessageUtils.t("guishell_saved_document_successfully_in")+" \n" + fileName);
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
        }
        if (arguments.size() > 1) Paths.get(arguments.get(0)).toAbsolutePath().toString();
        if (arguments.size() > 2) Paths.get(arguments.get(1)).toAbsolutePath().toString();
    }

    public void showError(Throwable error) {
        System.err.println(MessageUtils.t("guishell_exception") + error.getClass().getName());
        System.err.println(MessageUtils.t("guishell_message") + error.getLocalizedMessage());
        System.exit(1);
    }

    private String readFromInput(String message) {
        System.out.println(message);
        String plaintext = null;
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        try {
            plaintext = br.readLine();
        } catch (IOException e) {
            System.err.println(MessageUtils.t("guishell_cannot_read_from_stdin"));
            System.exit(1);
        }
        return plaintext;
    }

    public String getDocumentToSign() {
        String docpath = readFromInput(MessageUtils.t("guishell_sign_route_document")+" ");
        return Paths.get(docpath).toAbsolutePath().toString();
    }

    public String getPathToSave(String extension) {
        String docpath = readFromInput(MessageUtils.t("guishell_save_route_document")+" ");
        return Paths.get(docpath).toAbsolutePath().toString();
    }

    public CardSignInfo getPin() {
        Console console = System.console();
        char[] password = null;
        if (console != null) password = console.readPassword("PIN: ");
        else {
            System.out.print("PIN: ");
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
            password = new char[passwordLength];
            for (int i = 0; i < passwordLength; i++) password[i] = input[i];
            Arrays.fill(input, '\0');
        }
        PasswordProtection pin = new PasswordProtection(password);
        Arrays.fill(password, '\0');
        return new CardSignInfo(pin);
    }

    @Override
    public void showMessage(String message) {
         System.out.println(message);
    }

    @Override
    public void configurePluginManager() {
        this.pluginManager.startLogging();
    }

    @Override
    public void setPluginManager(PluginManager pluginManager){
        this.pluginManager = pluginManager;
    }

    @Override
    public Document loadDocument(String fileName) {
        return null;
    }

    @Override
    public void loadDocument(SupportedMimeTypeEnum mimeType, PDDocument doc) {
    }

    @Override
    public void extendDocument() {
    }

    @Override
    public String getPathToSaveExtended(String extension) {
        return null;
    }

    @Override
    public boolean signDocuments() {
        return false;
    }

    @Override
    public void displayFunctionality(String functionality) {
        System.out.println(functionality);

    }

    @Override
    public void nextStep(String msg) {
        System.out.println(msg);

    }

    public void previewDone(Document document) {
    };

    public void validateDone(Document document) {
    };

    public void signDone(Document document) {
    };

    public void extendsDone(Document document) {
    }

    @Override
    public void validateAllDone() {
        // TODO Auto-generated method stub

    }

    @Override
    public void signAllDone() {
        // TODO Auto-generated method stub

    }

    @Override
    public void doPreview(Document document) {
        // TODO Auto-generated method stub

    }

    @Override
    public Settings getCurrentSettings() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void signDocument(Document document) {
        // TODO Auto-generated method stub

    }

    @Override
    public void clearDone() {
        // TODO Auto-generated method stub

    }

    @Override
    public void previewAllDone() {
        // TODO Auto-generated method stub

    }

    @Override
    public Document loadDocument(Document document, boolean preview) {
        // TODO Auto-generated method stub
        return null;
    };
}
