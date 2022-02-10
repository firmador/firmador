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

//import cr.libre.firmador.FirmadorCAdES;
//import cr.libre.firmador.FirmadorOpenDocument;
import cr.libre.firmador.FirmadorPAdES;
import cr.libre.firmador.plugins.PluginManager;

//import cr.libre.firmador.FirmadorXAdES;
import com.google.common.base.Throwables;
import eu.europa.esig.dss.model.DSSDocument;
import eu.europa.esig.dss.model.FileDocument;

public class GUIShell implements GUIInterface {

    public void loadGUI() {
        String fileName = getDocumentToSign();
        if (fileName != null) {
            // FirmadorCAdES firmador = new FirmadorCAdES(this);
            // FirmadorOpenDocument firmador = new FirmadorOpenDocument(this);
            FirmadorPAdES firmador = new FirmadorPAdES(this);
            // FirmadorXAdES firmador = new FirmadorXAdES(this);
            PasswordProtection pin = getPin();
            DSSDocument toSignDocument = new FileDocument(fileName);
            DSSDocument signedDocument = firmador.sign(toSignDocument, pin, null, null, null, null, null, null);
            try {
                pin.destroy();
            } catch (Exception e) {}
            if (signedDocument != null) {
                fileName = getPathToSave("");
                try {
                    signedDocument.save(fileName);
                    showMessage("Documento guardado satisfactoriamente en \n" + fileName);
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

    public PasswordProtection getPin() {
        Console console = System.console();
        char[] password = null;
        if (console != null) password = console.readPassword("PIN: ");
        else password = readFromInput("PIN: ").toCharArray();
        PasswordProtection pin = new PasswordProtection(password);
        Arrays.fill(password, (char) 0);
        return pin;
    }

    @Override
    public void showMessage(String message) {
         System.out.println(message);
    }

    @Override
    public int getSlot() {
        return -1;
    }

    @Override
    public String getPkcs12file() {
        return "";
    }

	@Override
	public void setPluginManager(PluginManager pluginManager) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void loadDocument(String fileName) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void loadDocument(DSSDocument mimeDocument, PDDocument doc) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void extendDocument() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getPathToSaveExtended(String extension) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean signDocuments() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void displayFunctionality(String functionality) {
		// TODO Auto-generated method stub
		
	}

}
