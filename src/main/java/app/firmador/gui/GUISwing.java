/* Firmador is a program to sign documents using AdES standards.

Copyright (C) 2018 Francisco de la Peña Fernández.

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
import java.awt.Dialog;
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

    private static Dialog pinDialog;
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
            System.out.println("Sintaxis: firmador.jar fichero.pdf");
            System.exit(1);
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
        saveDialog.setFile(saveFileName.substring(0,
            saveFileName.lastIndexOf(".")) + "-firmado.pdf");
        saveDialog.setFilenameFilter(loadDialog.getFilenameFilter());
        saveDialog.setLocationRelativeTo(null);
        saveDialog.setVisible(true);
        saveDialog.dispose();
        if (saveDialog.getFile() == null) {
            System.exit(1);
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
            JOptionPane.PLAIN_MESSAGE);
        pinField.grabFocus();
        if (action != 0) {
            System.exit(1);
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

}
