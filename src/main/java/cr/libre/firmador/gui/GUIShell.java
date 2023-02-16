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

import org.apache.pdfbox.pdmodel.PDDocument;

import cr.libre.firmador.CardSignInfo;
//import cr.libre.firmador.FirmadorCAdES;
//import cr.libre.firmador.FirmadorOpenDocument;
import cr.libre.firmador.FirmadorPAdES;
import cr.libre.firmador.FirmadorUtils;
//import cr.libre.firmador.FirmadorXAdES;
//import cr.libre.firmador.Settings;
//import cr.libre.firmador.SettingsManager;
import cr.libre.firmador.plugins.PluginManager;

import eu.europa.esig.dss.model.DSSDocument;
import eu.europa.esig.dss.model.FileDocument;
import eu.europa.esig.dss.model.MimeType;

public class GUIShell implements GUIInterface {

    //private Settings settings;

    public void loadGUI() {
        //settings = SettingsManager.getInstance().getAndCreateSettings();
        String fileName = getDocumentToSign();
        if (fileName != null) {
            // FirmadorCAdES firmador = new FirmadorCAdES(this);
            // FirmadorOpenDocument firmador = new FirmadorOpenDocument(this);
            FirmadorPAdES firmador = new FirmadorPAdES(this);
            // FirmadorXAdES firmador = new FirmadorXAdES(this);
            CardSignInfo card = getPin();
            DSSDocument toSignDocument = new FileDocument(fileName);
            DSSDocument signedDocument = firmador.sign(toSignDocument, card, null, null, null, null, null);
            card.destroyPin();
            if (signedDocument != null) {
                fileName = getPathToSave("");
                try {
                    signedDocument.save(fileName);
                    showMessage("Documento guardado satisfactoriamente en \n" + fileName);
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
        System.err.println("Excepción: " + error.getClass().getName());
        System.err.println("Mensaje: " + error.getLocalizedMessage());
        System.exit(1);
    }

    private String readFromInput(String message) {
        System.out.println(message);
        String plaintext = null;
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        try {
            plaintext = br.readLine();
        } catch (IOException e) {
            System.err.println("No se puede leer desde stdin.");
            System.exit(1);
        }
        return plaintext;
    }

    public String getDocumentToSign() {
        String docpath = readFromInput("Ruta del documento a firmar: ");
        return Paths.get(docpath).toAbsolutePath().toString();
    }

    public String getPathToSave(String extension) {
        String docpath = readFromInput("Ruta del documento a guardar: ");
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
    public void setPluginManager(PluginManager pluginManager) {
        pluginManager.startLogging();
    }

    @Override
    public void loadDocument(String fileName) {
    }

    @Override
    public void loadDocument(MimeType mimeType, PDDocument doc) {
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

}
