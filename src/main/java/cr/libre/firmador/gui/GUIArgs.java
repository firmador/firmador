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

package cr.libre.firmador.gui;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Paths;
import java.security.KeyStore.PasswordProtection;
import java.util.ArrayList;
import java.util.List;

//import cr.libre.firmador.FirmadorCAdES;
//import cr.libre.firmador.FirmadorOpenDocument;
import cr.libre.firmador.FirmadorPAdES;
//import cr.libre.firmador.FirmadorXAdES;
import com.google.common.base.Throwables;
import eu.europa.esig.dss.model.DSSDocument;
import eu.europa.esig.dss.model.FileDocument;

public class GUIArgs implements GUIInterface {

    private String documenttosign;
    private String documenttosave;
    private String pkcs12file = "";
    private int slot = -1;
    private Boolean timestamp = false;
    private Boolean visibleTimestamp = false;

    public void loadGUI() {
        String fileName = getDocumentToSign();
        if (fileName != null) {
            // FirmadorCAdES firmador = new FirmadorCAdES(this);
            // FirmadorOpenDocument firmador = new FirmadorOpenDocument(this);
            FirmadorPAdES firmador = new FirmadorPAdES(this);
            // FirmadorXAdES firmador = new FirmadorXAdES(this);
            DSSDocument toSignDocument = new FileDocument(fileName);
            DSSDocument signedDocument = null;
            if (!timestamp && !visibleTimestamp) {
                PasswordProtection pin = getPin();
                signedDocument = firmador.sign(toSignDocument, pin, null, null, null, null, null, null);
                try {
                    pin.destroy();
                } catch (Exception e) {}
            } else signedDocument = firmador.timestamp(toSignDocument, visibleTimestamp);
            if (signedDocument != null) {
                fileName = getPathToSave("");
                try {
                    signedDocument.save(fileName);
                    showMessage("Documento guardado satisfactoriamente en " + fileName);
                } catch (IOException e) {
                    showError(Throwables.getRootCause(e));
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
        if (arguments.size() > 2) pkcs12file = Paths.get(arguments.get(2)).toAbsolutePath().toString();
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

    public PasswordProtection getPin() {
        String pintext = null;
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        try {
            pintext = br.readLine();
        } catch (IOException e) {
            System.err.println("PIN not encontrado");
            System.exit(1);
        }
        return new PasswordProtection(pintext.toCharArray());
    }

    @Override
    public void showMessage(String message) {
         System.out.println(message);
    }

    @Override
    public int getSlot() {
        return slot;
    }

    @Override
    public String getPkcs12file() {
        return pkcs12file;
    }
}
