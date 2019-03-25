/* Firmador is a program to sign documents using AdES standards.

Copyright (C) 2019 Firmador authors.

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

package app.firmador.gui;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Paths;
import java.security.KeyStore.PasswordProtection;
import java.util.ArrayList;
import java.util.List;

import app.firmador.FirmadorPAdES;
//import app.firmador.FirmadorXAdES;
import com.google.common.base.Throwables;
import eu.europa.esig.dss.DSSDocument;
import eu.europa.esig.dss.FileDocument;

public class GUIArgs implements GUIInterface {

    private String documenttosign;
    private String documenttosave;
    private int slot;

    public void loadGUI() {
        String fileName = getDocumentToSign();

        if (fileName != null) {
            // FirmadorXAdES firmador = new FirmadorXAdES(this);
            FirmadorPAdES firmador = new FirmadorPAdES(this);
            firmador.selectSlot();

            PasswordProtection pin = getPin();
            DSSDocument toSignDocument = new FileDocument(fileName);
            DSSDocument signedDocument = firmador.sign(toSignDocument, pin);
            try {
                pin.destroy();
            } catch (Exception e) {}

            if (signedDocument != null) {
                fileName = getPathToSave();
                try {
                    signedDocument.save(fileName);
                    showMessage(
                        "Documento guardado satisfactoriamente en \n" +
                        fileName);
                } catch (IOException e) {
                    showError(Throwables.getRootCause(e));
                }
            }
        }
    }

    public void setArgs(String[] args) {
        List<String> arguments = new ArrayList<String>();
        slot = 0;
        for (String params : args) {
            if (!params.startsWith("-")) {
                arguments.add(params);
            } else if (params.startsWith("-slot")) {
                slot = Integer.parseInt(params.replace("-slot", ""));
            }
        }
        documenttosign = Paths.get(arguments.get(0)).toAbsolutePath()
            .toString();
        documenttosave = Paths.get(arguments.get(1)).toAbsolutePath()
            .toString();
    }

    public void showError(Throwable error) {
        System.err.println("Exception: " + error.getClass().getName());
        System.err.println("Message: " + error.getMessage());
        System.exit(1);
    }

    public String getDocumentToSign() {
        return documenttosign;
    }

    public String getPathToSave() {
        return documenttosave;
    }

    public PasswordProtection getPin() {
        String pintext = null;
        BufferedReader br = new BufferedReader(
            new InputStreamReader(System.in));

        try {
            pintext = br.readLine();
        } catch (IOException e) {
            System.err.println("PIN not Found");
            System.exit(1);
        }

        return new PasswordProtection(pintext.toCharArray());
    }

    @Override
    public void showMessage(String message) {
         System.out.println(message);
    }

    @Override
    public int getSelection(String[] options) {
        return slot;
    }

}
