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

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.security.KeyStore.PasswordProtection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

import cr.libre.firmador.CardSignInfo;
//import cr.libre.firmador.FirmadorCAdES;
import cr.libre.firmador.FirmadorOpenDocument;
import cr.libre.firmador.FirmadorPAdES;
import cr.libre.firmador.FirmadorXAdES;
import cr.libre.firmador.plugins.PluginManager;

import com.google.common.base.Throwables;
import eu.europa.esig.dss.model.DSSDocument;
import eu.europa.esig.dss.model.FileDocument;
import eu.europa.esig.dss.model.MimeType;
import org.apache.pdfbox.pdmodel.PDDocument;

public class GUIArgs implements GUIInterface {

    private String documenttosign;
    private String documenttosave;
    //private String pkcs12file = "";
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
                if (mimeType == MimeType.PDF) {
                    FirmadorPAdES firmador = new FirmadorPAdES(this);
                    signedDocument = firmador.sign(toSignDocument, card, null, null, null, null, null);
                } else if (mimeType == MimeType.ODG || mimeType == MimeType.ODP || mimeType == MimeType.ODS || mimeType == MimeType.ODT) {
                    FirmadorOpenDocument firmador = new FirmadorOpenDocument(this);
                    signedDocument = firmador.sign(toSignDocument, card);
                } else if (mimeType == MimeType.XML) {
                    FirmadorXAdES firmador = new FirmadorXAdES(this);
                    signedDocument = firmador.sign(toSignDocument, card);
                } else {
                    FirmadorXAdES firmador = new FirmadorXAdES(this);
                    signedDocument = firmador.sign(toSignDocument, card);
                }
                card.destroyPin();;

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
        Scanner scanner = new Scanner(System.in);
        scanner.useDelimiter("[\r\n]");
        byte[] bytes = new byte[64];
        for (byte currentByte: bytes) {
            if (!scanner.hasNextByte()) break;
            bytes[currentByte] = scanner.nextByte(); // replaced next() because it uses String (bad for pin)
        }
        scanner.close();
        CharBuffer charBuffer = StandardCharsets.UTF_8.decode(ByteBuffer.wrap(bytes));
        PasswordProtection pin = new PasswordProtection(Arrays.copyOf(charBuffer.array(), charBuffer.limit()));
        Arrays.fill(bytes, (byte) 0);
        charBuffer = StandardCharsets.UTF_8.decode(ByteBuffer.wrap(bytes)); // Also try to zero-fill buffers
        CardSignInfo card = new CardSignInfo(pin);
        try {
            pin.destroy();
        } catch (Exception e) {
            System.err.println("Error destruyendo el pin:");
            showError(Throwables.getRootCause(e));
        }
        return card;
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
	public void setPluginManager(PluginManager pluginManager) {
		// TODO Auto-generated method stub

	}

	@Override
	public void loadDocument(String fileName) {
		// TODO Auto-generated method stub

	}

	@Override
	public void loadDocument(MimeType mimeType, PDDocument doc) {
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
		return true;
	}

	@Override
	public void displayFunctionality(String functionality) {
		// TODO Auto-generated method stub

	}

	@Override
	public void nextStep(String msg) {
		// TODO Auto-generated method stub

	}
}
