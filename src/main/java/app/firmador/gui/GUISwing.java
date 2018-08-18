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

package app.firmador.gui;

import java.awt.Component;
import java.awt.FileDialog;
import java.awt.Window;
import java.awt.event.HierarchyListener;
import java.awt.event.HierarchyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FilenameFilter;
import java.nio.file.Paths;
import java.security.KeyStore.PasswordProtection;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JOptionPane;
import javax.swing.JPasswordField;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

public class GUISwing implements GUIInterface {

    private static FileDialog loadDialog;
    private String documenttosign = null;
    private String documenttosave = null;

    public String getDocumentToSign() {
        if (documenttosign != null) {
            return documenttosign;
        }
        String fileName = null;

        try {
            UIManager.setLookAndFeel(
                UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException e) {
            // TODO
        } catch (InstantiationException e) {
            // TODO
        } catch (IllegalAccessException e) {
            // TODO
        } catch (UnsupportedLookAndFeelException e) {
            // TODO
        }

        loadDialog = new FileDialog(loadDialog,
            "Seleccionar documento a firmar");
        loadDialog.setFilenameFilter(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name.endsWith(".pdf") || name.endsWith(".PDF");
            }
        });
        loadDialog.setFile("*.pdf");
        loadDialog.setLocationRelativeTo(null);
        loadDialog.setVisible(true);
        loadDialog.dispose();
        if (loadDialog.getFile() == null) {
            System.exit(0);
        } else {
            fileName = loadDialog.getDirectory() + loadDialog.getFile();
        }

        return fileName;
    }

    public String getPathToSave() {
        if (documenttosave != null) {
            return documenttosave;
        }

        String fileName = null;
        FileDialog saveDialog = null;
        saveDialog = new FileDialog(saveDialog,
            "Guardar documento", FileDialog.SAVE);
        String saveDirectory = null;
        String saveFileName = null;

        saveDirectory = loadDialog.getDirectory();
        saveFileName = loadDialog.getFile();
        saveDialog.setDirectory(saveDirectory);

        String dotExtension = "";
        int lastDot = saveFileName.lastIndexOf(".");
        if (lastDot >= 0) {
            dotExtension = saveFileName.substring(lastDot);
        }
        saveDialog.setFile(saveFileName.substring(0,
            saveFileName.lastIndexOf(".")) + "-firmado" + dotExtension);
        saveDialog.setFilenameFilter(loadDialog.getFilenameFilter());
        saveDialog.setLocationRelativeTo(null);
        saveDialog.setVisible(true);
        saveDialog.dispose();
        if (saveDialog.getFile() == null) {
            System.exit(0);
        } else {
            fileName = saveDialog.getDirectory() + saveDialog.getFile();
        }

        return fileName;
    }

    public PasswordProtection getPin() {

        JPasswordField pinField = new JPasswordField(14);
        pinField.addHierarchyListener(new HierarchyListener() {
            public void hierarchyChanged(HierarchyEvent e) {
                final Component c = e.getComponent();
                if (c.isShowing() &&
                    (e.getChangeFlags() & HierarchyEvent.SHOWING_CHANGED)
                    != 0) {
                    Window toplevel = SwingUtilities.getWindowAncestor(c);
                    toplevel.addWindowFocusListener(new WindowAdapter() {
                        public void windowGainedFocus(WindowEvent e) {
                            c.requestFocus();
                        }
                    });
                }
            }
        });
        int action = JOptionPane.showConfirmDialog(null, pinField,
            "Ingresar PIN", JOptionPane.OK_CANCEL_OPTION,
            JOptionPane.QUESTION_MESSAGE);
        pinField.grabFocus();
        if (action != 0) {
            System.exit(0);
        }

        return new PasswordProtection(pinField.getPassword());
    }

    public void setArgs(String[] args) {
        List<String> arguments = new ArrayList<String>();

        for (String params : args) {
            if (!params.startsWith("-")) {
                arguments.add(params);
            }
        }
        if (arguments.size() > 1) {
            documenttosign = Paths.get(arguments.get(0)).toAbsolutePath()
                .toString();
        }
        if (arguments.size() > 2) {
            documenttosave = Paths.get(arguments.get(1)).toAbsolutePath()
                .toString();
        }
    }

    public void showError(Throwable error) {
        String message = error.getLocalizedMessage();
        String className = error.getClass().getName();

        switch (className) {
            case "java.lang.NoSuchMethodError":
                message = "Esta aplicación es actualmente incompatible con " +
                    "versiones superiores a Java 8.\n" +
                    "Este inconveniente se corregirá en próximas versiones. " +
                    "Disculpe las molestias.";
                break;
            case "java.security.ProviderException":
                message = "No se ha encontrado la librería de Firma Digital " +
                    "en el sistema.\n" +
                    "¿Están instalados los controladores?";
                break;
            case "java.security.NoSuchAlgorithmException":
                message = "No se ha encontrado ninguna tarjeta conectada.\n" +
                    "Asegúrese de que la tarjeta y el lector están " +
                    "conectados de forma correcta.";
                break;
            case "sun.security.pkcs11.wrapper.PKCS11Exception":
                switch (message) {
                case "CKR_GENERAL_ERROR":
                    message = "No se ha podido contactar con el servicio " +
                        "del lector de tarjetas.\n" +
                        "¿Está correctamente instalado o configurado?";
                    break;
                case "CKR_SLOT_ID_INVALID":
                    message = "No se ha podido encontrar ningún lector " +
                    "conectado o el controlador del lector no está instalado.";
                    break;
                case "CKR_PIN_INCORRECT":
                    message = "¡PIN INCORRECTO!\n\n" +
                        "ADVERTENCIA: si se ingresa un PIN incorrecto " +
                        "varias veces sin acertar,\n" +
                        "el dispositivo de firma se bloqueará.";
                    break;
                case "CKR_PIN_LOCKED":
                    message = "PIN BLOQUEADO\n\n" +
                        "Lo sentimos, el dispositivo de firma no se puede " +
                        "utilizar porque está bloqueado.\n" +
                        "Contacte con su proveedor para desbloquearlo.";
                    break;
                default:
                    break;
                }
            default:
                message = "Error: " + className + "\n" +
                    "Detalle: " + message + "\n" +
                    "Agradecemos que comunique este mensaje de error al " +
                    "autor del programa\n" +
                    "para detallar mejor el posible motivo de este error " +
                    "en próximas versiones.";
                break;
        }

        JOptionPane.showMessageDialog(null, message, "Error al firmar",
            JOptionPane.ERROR_MESSAGE);

        System.exit(0);
    }

    public void showMessage(String message) {
        JOptionPane.showMessageDialog(null, message, "Mensaje importante",
            JOptionPane.INFORMATION_MESSAGE);
    }

    public int getSelection(String[] options) {
        int dev = 0;

        if (options == null || options.length == 0) {
            String message = "No se ha encontrado ninguna tarjeta " +
                "conectada.\nAsegúrese de que la tarjeta y el lector están " +
                "conectados de forma correcta.";
            JOptionPane.showMessageDialog(null, message, "Error al firmar",
            JOptionPane.ERROR_MESSAGE);
            System.exit(0);
        }
        String input = (String) JOptionPane.showInputDialog(null,
            "Propietario: ", "Seleccione el dispositivo para firmar",
            JOptionPane.QUESTION_MESSAGE, null, options, options[0]);

        if (input == null) {
            System.exit(0);
        }
        for (int x = 0; x < options.length; x++) {
            if (input.equals(options[x])) {
                dev = x;
                x = options.length;
            }
        }

        return dev;
    }

}
