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

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.FileDialog;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.HierarchyListener;
import java.awt.event.HierarchyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FilenameFilter;
import java.lang.reflect.Field;
import java.net.URI;
import java.nio.file.Paths;
import java.security.KeyStore.PasswordProtection;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPasswordField;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;

import com.apple.eawt.Application;
import com.google.common.base.Throwables;

public class GUISwing implements GUIInterface {

    private static FileDialog loadDialog;
    private String documenttosign = null;
    private String documenttosave = null;
    private Image image = new ImageIcon(GUISwing.class.getClassLoader()
        .getResource("firmador.png")).getImage();

    public String getDocumentToSign() {
        try {
            Application.getApplication().setDockIconImage(image);
        } catch (RuntimeException e) {
            // macOS dock icon support specific code.
        }
        try {
            UIManager.setLookAndFeel(
                UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            showError(Throwables.getRootCause(e));
        }
        JLabel fileLabel = new JLabel("Documento: ");
        final JTextField fileField = new JTextField("(Vacío)");
        fileField.setEditable(false);
        final JFrame frame = new JFrame("Firmador");
        frame.setIconImage(
            image.getScaledInstance(256, 256, Image.SCALE_SMOOTH));
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        try {
            Field awtAppClassNameField =
                toolkit.getClass().getDeclaredField("awtAppClassName");
            awtAppClassNameField.setAccessible(true);
            awtAppClassNameField.set(toolkit, "Firmador");
        } catch (Exception e) {
            // Workaround application name in GNOME
        }
        JButton fileButton = new JButton("Elegir...");
        fileButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                loadDialog = new FileDialog(frame,
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
                if (loadDialog.getFile() != null) {
                    fileField.setText(loadDialog.getDirectory()
                        + loadDialog.getFile());
                }
                documenttosign = fileField.getText();
            }
        });

        JPanel filePanel = new JPanel(new BorderLayout());
        filePanel.setBorder(new EmptyBorder(10, 10, 0, 10));
        filePanel.add(fileLabel, BorderLayout.LINE_START);
        filePanel.add(fileField, BorderLayout.CENTER);
        filePanel.add(fileButton, BorderLayout.LINE_END);
        JPanel signPanel = new JPanel();
        JPanel validatePanel = new JPanel();
        JPanel aboutPanel = new JPanel();
        aboutPanel.setLayout(new BoxLayout(aboutPanel, BoxLayout.PAGE_AXIS));
        JTabbedPane tabbedPane = new JTabbedPane();
        signPanel.setOpaque(false);
        validatePanel.setOpaque(false);
        aboutPanel.setOpaque(false);
        JLabel iconLabel = new JLabel(new ImageIcon(
            image.getScaledInstance(128, 128, Image.SCALE_SMOOTH)),
            JLabel.CENTER);
        JLabel descriptionLabel = new JLabel(
            "<html><p align='center'><b>Firmador</b><br><br>" +
            "Versión 1.3.0<br><br>" +
            "Herramienta para firmar documentos digitalmente.<br><br>" +
            "Los documentos firmados con esta herramienta cumplen con la " +
            "Política de Formatos Oficiales de los Documentos Electrónicos " +
            "Firmados Digitalmente de Costa Rica.<br><br></p></html>",
            JLabel.CENTER);
        JButton websiteButton = new JButton("Visitar sitio web del proyecto");
        websiteButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                if (Desktop.isDesktopSupported()) {
                    try {
                        Desktop.getDesktop().browse(
                            new URI("https://firmador.app"));
                    } catch (Exception e) {
                        showError(Throwables.getRootCause(e));
                    }
                }
            }
        });
        iconLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        descriptionLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        websiteButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        aboutPanel.add(iconLabel);
        aboutPanel.add(descriptionLabel);
        aboutPanel.add(websiteButton);
        tabbedPane.setBorder(new EmptyBorder(10, 10, 10, 10));
        tabbedPane.addTab("Firmar", signPanel);
        tabbedPane.addTab("Validar", validatePanel);
        tabbedPane.addTab("Acerca de", aboutPanel);
        frame.add(filePanel, BorderLayout.PAGE_START);
        frame.add(tabbedPane, BorderLayout.CENTER);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setMinimumSize(new Dimension(480, 512));
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        return documenttosign;
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
        if (saveDialog.getFile() != null) {
            fileName = saveDialog.getDirectory() + saveDialog.getFile();
        }

        return fileName;
    }

    public PasswordProtection getPin() {

        JPasswordField pinField = new JPasswordField(14);
        pinField.addHierarchyListener(new HierarchyListener() {
            public void hierarchyChanged(HierarchyEvent event) {
                final Component component = event.getComponent();
                if (component.isShowing() &&
                    (event.getChangeFlags() & HierarchyEvent.SHOWING_CHANGED)
                    != 0) {
                    Window toplevel =
                        SwingUtilities.getWindowAncestor(component);
                    toplevel.addWindowFocusListener(new WindowAdapter() {
                        public void windowGainedFocus(WindowEvent event) {
                            component.requestFocus();
                        }
                    });
                }
            }
        });
        int action = JOptionPane.showConfirmDialog(null, pinField,
            "Ingresar PIN", JOptionPane.OK_CANCEL_OPTION,
            JOptionPane.QUESTION_MESSAGE);
        pinField.grabFocus();
        if (action == 0) {
            return new PasswordProtection(pinField.getPassword());
        } else {
            return new PasswordProtection(null);
        }
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
                    "Agradecemos que comunique este mensaje de error a los " +
                    "autores del programa\n" +
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
