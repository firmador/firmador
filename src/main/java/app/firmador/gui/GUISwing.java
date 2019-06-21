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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Component;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.FileDialog;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.Window;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.HierarchyListener;
import java.awt.event.HierarchyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Paths;
import java.security.KeyStore.PasswordProtection;
import java.util.ArrayList;
import java.util.List;
import javax.swing.Box;
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
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.ScrollPaneConstants;
import javax.swing.Scrollable;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import app.firmador.FirmadorPAdES;
//import app.firmador.FirmadorXAdES;
import app.firmador.Report;
import app.firmador.Validator;
import com.apple.eawt.Application;
import com.google.common.base.Throwables;
import eu.europa.esig.dss.DSSDocument;
import eu.europa.esig.dss.FileDocument;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;

public class GUISwing implements GUIInterface {

    private static FileDialog loadDialog;
    private String documenttosign = null;
    private String documenttosave = null;
    private String lastDirectory = null;
    private String lastFile = null;
    private Image image = new ImageIcon(GUISwing.class.getClassLoader()
        .getResource("firmador.png")).getImage();
    private JTextField fileField;
    private JTabbedPane tabbedPane;
    private JLabel imageLabel;
    private JLabel signatureLabel;
    private JLabel reportLabel;
    private JLabel pageLabel;
    private JSpinner pageSpinner;
    private JButton signButton;
    private JButton extendButton;
    private BufferedImage pageImage;
    private PDDocument doc;
    private PDFRenderer renderer;

    public void loadGUI() {
        try {
            Application.getApplication().setDockIconImage(image);
        } catch (RuntimeException e) {
            // macOS dock icon support specific code.
        }
        try {
            try {
                UIManager.setLookAndFeel(
                    "com.sun.java.swing.plaf.gtk.GTKLookAndFeel");
            } catch (javax.swing.UnsupportedLookAndFeelException |
                java.lang.ClassNotFoundException e) {
                UIManager.setLookAndFeel(
                    UIManager.getSystemLookAndFeelClassName());
            }
        } catch (Exception e) {
            showError(Throwables.getRootCause(e));
        }
        JLabel fileLabel = new JLabel("Documento: ");
        fileField = new JTextField("(Vacío)");
        fileField.setEditable(false);
        final JFrame frame = new JFrame("Firmador");
        frame.setIconImage(
            image.getScaledInstance(256, 256, Image.SCALE_SMOOTH));
        pageLabel = new JLabel("Página:");
        pageLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        pageSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 100, 1));
        pageSpinner.setMaximumSize(pageSpinner.getPreferredSize());
        pageSpinner.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                try {
                    int page = (int)(pageSpinner.getValue());
                    if (page > 0) {
                        pageImage = renderer.renderImage(
                            page - 1, (float)(1 / 2.5));
                        imageLabel.setIcon(new ImageIcon(pageImage));
                    }
                } catch (Exception ex) {
                    showError(Throwables.getRootCause(ex));
                }
            }
        });
        signButton = new JButton("Firmar documento");
        signButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        signButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                String fileName = getDocumentToSign();

                if (fileName != null) {
                  //FirmadorXAdES firmador = new FirmadorXAdES(GUISwing.this);
                    FirmadorPAdES firmador = new FirmadorPAdES(GUISwing.this);
                    firmador.selectSlot();
                    if (firmador.selectedSlot == -1) return;

                    PasswordProtection pin = getPin();
                    DSSDocument toSignDocument = new FileDocument(fileName);
                    DSSDocument signedDocument = null;
                    if (pin.getPassword() != null
                        && pin.getPassword().length != 0) {
                        firmador.addVisibleSignature(
                            (int)pageSpinner.getValue(),
                            (int)Math.round(signatureLabel.getX() * 2.5),
                            (int)Math.round(signatureLabel.getY() * 2.5));
                        signedDocument = firmador.sign(toSignDocument, pin);
                        try {
                            pin.destroy();
                        } catch (Exception e) {}
                    }
                    if (signedDocument != null) {
                        fileName = getPathToSave();
                        if (fileName != null) {
                            try {
                                signedDocument.save(fileName);
                                showMessage(
                                    "Documento guardado satisfactoriamente" +
                                    " en\n" + fileName);
                                loadDocument(fileName);
                            } catch (IOException e) {
                                showError(Throwables.getRootCause(e));
                            }
                        }
                    }
                }
            }
        });

        reportLabel = new JLabel();
        reportLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        extendButton = new JButton("Agregar sello de tiempo al documento");
        extendButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        extendButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                String fileName = getDocumentToSign();

                if (fileName != null) {
                  //FirmadorXAdES firmador = new FirmadorXAdES(GUISwing.this);
                    FirmadorPAdES firmador = new FirmadorPAdES(GUISwing.this);

                    DSSDocument toExtendDocument = new FileDocument(fileName);
                    DSSDocument extendedDocument = null;
                    extendedDocument = firmador.extend(toExtendDocument);
                    if (extendedDocument != null) {
                        fileName = getPathToSaveExtended();
                        if (fileName != null) {
                            try {
                                extendedDocument.save(fileName);
                                showMessage(
                                    "Documento guardado satisfactoriamente" +
                                    " en\n" + fileName);
                                loadDocument(fileName);
                            } catch (IOException e) {
                                showError(Throwables.getRootCause(e));
                            }
                        }
                    }
                }
            }
        });

        signButton.setEnabled(false);
        pageLabel.setEnabled(false);
        pageSpinner.setEnabled(false);
        extendButton.setEnabled(false);
        JButton fileButton = new JButton("Elegir...");
        imageLabel = new JLabel();
        imageLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
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
                    loadDocument(loadDialog.getDirectory()
                        + loadDialog.getFile());
                    lastDirectory = loadDialog.getDirectory();
                    lastFile = loadDialog.getFile();
                }
            }
        });
        signatureLabel = new JLabel("Firma visible", JLabel.CENTER);
        signatureLabel.setCursor(
            Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
        signatureLabel.setBackground(new Color(127, 127, 127, 127));
        signatureLabel.setOpaque(true);
        signatureLabel.setBounds(119, 0, 120, 13);
        imageLabel.addMouseMotionListener(new MouseMotionAdapter() {
            public void mouseDragged(MouseEvent e) {
                signatureLabel.setLocation(
                    e.getX() - signatureLabel.getWidth() / 2,
                    e.getY() - signatureLabel.getHeight() / 2);
            }
        });
        imageLabel.add(signatureLabel);

        JPanel filePanel = new JPanel(new BorderLayout());
        filePanel.setBorder(new EmptyBorder(10, 10, 0, 10));
        filePanel.add(fileLabel, BorderLayout.LINE_START);
        filePanel.add(fileField, BorderLayout.CENTER);
        filePanel.add(fileButton, BorderLayout.LINE_END);
        JPanel signPanel = new JPanel();
        signPanel.setLayout(new BoxLayout(signPanel, BoxLayout.PAGE_AXIS));
        signPanel.add(Box.createVerticalStrut(5));
        signPanel.add(signButton);
        signPanel.add(Box.createVerticalStrut(5));
        signPanel.add(imageLabel);
        signPanel.add(Box.createVerticalStrut(5));
        signPanel.add(pageLabel);
        signPanel.add(pageSpinner);
        JPanel validatePanel = new ScrollableJPanel();
        validatePanel.setLayout(new BoxLayout(validatePanel,
            BoxLayout.PAGE_AXIS));
        validatePanel.add(extendButton);
        validatePanel.add(reportLabel);
        JScrollPane scrollPane = new JScrollPane();
        scrollPane.setHorizontalScrollBarPolicy(
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setBorder(null);
        scrollPane.setViewportView(validatePanel);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        JPanel aboutPanel = new JPanel();
        aboutPanel.setLayout(new BoxLayout(aboutPanel, BoxLayout.PAGE_AXIS));
        tabbedPane = new JTabbedPane();
        signPanel.setOpaque(false);
        validatePanel.setOpaque(false);
        aboutPanel.setOpaque(false);
        JLabel iconLabel = new JLabel(new ImageIcon(
            image.getScaledInstance(128, 128, Image.SCALE_SMOOTH)),
            JLabel.CENTER);
        JLabel descriptionLabel = new JLabel(
            "<html><p align='center'><b>Firmador</b><br><br>" +
            "Versión " +
            getClass().getPackage().getSpecificationVersion() +
            "<br><br>" +
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
        tabbedPane.addTab("Validación", scrollPane);
        tabbedPane.addTab("Acerca de", aboutPanel);
        frame.add(filePanel, BorderLayout.PAGE_START);
        frame.add(tabbedPane, BorderLayout.CENTER);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setMinimumSize(new Dimension(480, 576));
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        if (documenttosign != null) {
            loadDocument(documenttosign);
        }
    }

    public void loadDocument(String fileName) {
        fileField.setText(fileName);
        signButton.setEnabled(true);
        pageLabel.setEnabled(true);
        pageSpinner.setEnabled(true);
        try {
            if (doc != null) {
                doc.close();
            }
            doc = PDDocument.load(new File(fileName));
            int pages = doc.getNumberOfPages();
            renderer = new PDFRenderer(doc);
            if (pages > 0) {
                pageImage = renderer.renderImage(0, (float)(1 / 2.5));
                SpinnerNumberModel model =
                    ((SpinnerNumberModel)pageSpinner.getModel());
                model.setMinimum(1);
                model.setMaximum(pages);
                pageSpinner.setValue(1);
            }
        } catch (Exception e) {
            showError(Throwables.getRootCause(e));
        }
        imageLabel.setBorder(new LineBorder(Color.BLACK));
        imageLabel.setIcon(new ImageIcon(pageImage));

        Validator validator = new Validator(fileName);
        if (validator.isSigned()) {
            extendButton.setEnabled(true);
            tabbedPane.setSelectedIndex(1);
        } else {
            extendButton.setEnabled(false);
            tabbedPane.setSelectedIndex(0);
        }
        try {
            Report report = new Report(validator.getReports());
            reportLabel.setText(report.getReport());
        } catch (Exception e) {
            e.printStackTrace();
            reportLabel.setText("<html>Error al generar reporte. " +
                "Agradeceríamos que informara sobre este inconveniente a " +
                "los desarrolladores de la aplicación para repararlo.</html>");
        }
    }

    public String getDocumentToSign() {
        return fileField.getText();
    }

    public String getPathToSave() {
        if (documenttosave != null) {
            return documenttosave;
        }
        String pathToSave = showSaveDialog("-firmado");

        return pathToSave;
    }

    public String getPathToSaveExtended() {
        String pathToExtend = showSaveDialog("-sellado");

        return pathToExtend;
    }

    public String showSaveDialog(String suffix) {
        String fileName = null;
        FileDialog saveDialog = null;
        saveDialog = new FileDialog(saveDialog,
            "Guardar documento", FileDialog.SAVE);
        saveDialog.setDirectory(lastDirectory);

        String dotExtension = "";
        int lastDot = lastFile.lastIndexOf(".");
        if (lastDot >= 0) {
            dotExtension = lastFile.substring(lastDot);
        }
        saveDialog.setFile(lastFile.substring(0,
            lastFile.lastIndexOf(".")) + suffix + dotExtension);
        saveDialog.setFilenameFilter(loadDialog.getFilenameFilter());
        saveDialog.setLocationRelativeTo(null);
        saveDialog.setVisible(true);
        saveDialog.dispose();
        if (saveDialog.getFile() != null) {
            fileName = saveDialog.getDirectory() + saveDialog.getFile();
            lastDirectory = saveDialog.getDirectory();
            lastFile = saveDialog.getFile();
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
        int messageType = JOptionPane.ERROR_MESSAGE;
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
                    messageType = JOptionPane.WARNING_MESSAGE;
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
                break;
            case "java.io.IOException":
                if (message.contains("asepkcs") || message.contains("libASEP11")) {
                    message = "No se ha encontrado la librería de Firma Digital " +
                        "en el sistema.\n" +
                        "¿Están instalados los controladores?";
                    break;
                }
            default:
                error.printStackTrace();
                message = "Error: " + className + "\n" +
                    "Detalle: " + message + "\n" +
                    "Agradecemos que comunique este mensaje de error a los " +
                    "autores del programa\n" +
                    "para detallar mejor el posible motivo de este error " +
                    "en próximas versiones.";
                break;
        }

        JOptionPane.showMessageDialog(null, message, "Mensaje de Firmador",
            messageType);

        if (messageType == JOptionPane.ERROR_MESSAGE) {
            System.exit(0);
        }
    }

    public void showMessage(String message) {
        JOptionPane.showMessageDialog(null, message, "Mensaje de Firmador",
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
            return -1;
        }
        String input = (String) JOptionPane.showInputDialog(null,
            "Propietario: ", "Seleccione el dispositivo para firmar",
            JOptionPane.QUESTION_MESSAGE, null, options, options[0]);

        if (input == null) {
            return -1;
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

class ScrollableJPanel extends JPanel implements Scrollable {

    public Dimension getPreferredScrollableViewportSize() {
        return this.getPreferredSize();
    }

    public int getScrollableUnitIncrement(Rectangle visibleRect,
        int orientation, int direction) {
        return 50;
    }

    public int getScrollableBlockIncrement(Rectangle visibleRect,
        int orientation, int direction) {
        return 80;
    }

    public boolean getScrollableTracksViewportWidth() {
        return true;
    }

    public boolean getScrollableTracksViewportHeight() {
        return false;
    }

}
