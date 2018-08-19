/* Firmador is a program to sign documents using AdES standards.

Copyright (C) 2018 Firmador authors.

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

import java.io.IOException;
import java.security.KeyStore.PasswordProtection;
import javax.security.auth.DestroyFailedException;
import javax.swing.ImageIcon;

import app.firmador.gui.GUIInterface;
import app.firmador.gui.GUISelector;
import com.apple.eawt.Application;
import com.google.common.base.Throwables;
import eu.europa.esig.dss.DSSDocument;
import eu.europa.esig.dss.FileDocument;

public class Firmador {

    public static void main(String[] args) {
        try {
            Application.getApplication().setDockIconImage(new ImageIcon(
                Firmador.class.getClassLoader().getResource("firmador.png"))
                .getImage());
        } catch (RuntimeException e) {
            // macOS dock icon support specific code.
        }
        GUISelector guiselector = new GUISelector();

        GUIInterface gui = guiselector.getInterface(args);
        gui.setArgs(args);
        String fileName = gui.getDocumentToSign();

        // FirmadorXAdES firmador = new FirmadorXAdES(gui);
        FirmadorPAdES firmador = new FirmadorPAdES(gui);
        firmador.selectSlot();

        PasswordProtection pin = gui.getPin();

        DSSDocument toSignDocument = new FileDocument(fileName);
        DSSDocument signedDocument = firmador.sign(toSignDocument, pin);
        try {
            pin.destroy();
        } catch (DestroyFailedException e) {}

        if(signedDocument != null) {
            fileName = gui.getPathToSave();
            try {
                signedDocument.save(fileName);
                gui.showMessage("Documento guardado satisfactoriamente en \n" +
                    fileName);
            } catch (IOException e) {
                gui.showError(Throwables.getRootCause(e));
            }
        }
    }

}
