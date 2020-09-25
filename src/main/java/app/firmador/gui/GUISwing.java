/* Firmador is a program to sign documents using AdES standards.

Copyright (C) 2020 Firmador authors.

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
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.InetAddress;
import java.net.URI;
import java.nio.file.Paths;
import java.security.KeyStore.PasswordProtection;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
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
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import app.firmador.FirmadorPAdES;
import app.firmador.FirmadorXAdES;
import app.firmador.FirmadorOpenDocument;
import app.firmador.Report;
import app.firmador.Validator;
import app.firmador.gui.swing.CopyableJLabel;
import app.firmador.gui.swing.ScrollableJPanel;
import com.apple.eawt.Application;
import com.google.common.base.Throwables;
import eu.europa.esig.dss.model.DSSDocument;
import eu.europa.esig.dss.model.FileDocument;
import eu.europa.esig.dss.model.InMemoryDocument;
import eu.europa.esig.dss.model.MimeType;
import org.apache.http.HttpEntity;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.bootstrap.HttpServer;
import org.apache.http.impl.bootstrap.ServerBootstrap;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpRequestHandler;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;

public class GUISwing implements GUIInterface {

    private static FileDialog loadDialog;
    private Boolean isRemote = false;
    private Boolean alreadySignedDocument = false;
    private String documenttosign = null;
    private String documenttosave = null;
    private String lastDirectory = null;
    private String lastFile = null;
    private Image image = new ImageIcon(this.getClass().getClassLoader()
        .getResource("firmador.png")).getImage();
    private JTextField fileField;
    private JTabbedPane tabbedPane;
    private JLabel imageLabel;
    private JLabel signatureLabel;
    private JCheckBox signatureVisibleCheckBox;
    private JLabel reasonLabel;
    private JLabel locationLabel;
    private JLabel contactInfoLabel;
    private JTextField reasonField;
    private JTextField locationField;
    private JTextField contactInfoField;
    private CopyableJLabel reportLabel;
    private DSSDocument toSignDocument;
    private DSSDocument signedDocument;
    private byte[] toSignByteArray;
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
        String origin = System.getProperty("jnlp.remoteOrigin");
        isRemote = (origin != null);
        if (isRemote) {
            SwingWorker<Void, byte[]> remote =
                new SwingWorker<Void, byte[]>() {
                private HttpServer server;
                protected Void doInBackground()
                    throws IOException, InterruptedException {
                    HttpRequestHandler requestHandler =
                        new HttpRequestHandler() {
                        public void handle(
                            HttpRequest request, HttpResponse response,
                            HttpContext context)
                                throws HttpException, IOException {
                            response.setHeader("Access-Control-Allow-Origin",
                                origin);
                            response.setHeader("Vary", "Origin");
                            if (request.getRequestLine().getUri()
                                .equals("/close")) System.exit(0);
                            response.setStatusCode(HttpStatus.SC_ACCEPTED);
                            if (request instanceof HttpEntityEnclosingRequest)
                            {
                                HttpEntity entity =
                                    ((HttpEntityEnclosingRequest)request)
                                    .getEntity();
                                if (alreadySignedDocument) {
                                    response.setStatusCode(HttpStatus.SC_OK);
                                    ByteArrayOutputStream os =
                                        new ByteArrayOutputStream();
                                    signedDocument.writeTo(os);
                                    response.setEntity(
                                        new ByteArrayEntity(os.toByteArray(),
                                            ContentType.DEFAULT_TEXT));
                                }
                                if (entity.getContentLength() > 0) {
                                    ByteArrayOutputStream os =
                                        new ByteArrayOutputStream();
                                    entity.writeTo(os);
                                    publish(os.toByteArray());
                                }
                            }
                        }
                    };
                    server = ServerBootstrap.bootstrap()
                        .setListenerPort(3516)
                        .setLocalAddress(InetAddress.getLoopbackAddress())
                        .registerHandler("*", requestHandler)
                        .create();
                    server.start();
                    server.awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS);
                    return null;
                }
                protected void process(List<byte[]> chunks) {
                    toSignByteArray = chunks.get(chunks.size() - 1);
                    toSignDocument = new InMemoryDocument(toSignByteArray);
                    loadDocument(null);
                }
            };
            remote.execute();
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
                    int page = (int)pageSpinner.getValue();
                    if (page > 0) {
                        pageImage = renderer.renderImage(page - 1, 1 / 2.5f);
                        imageLabel.setIcon(new ImageIcon(pageImage));
                    }
                } catch (Exception ex) {
                    showError(Throwables.getRootCause(ex));
                }
            }
        });
        signatureVisibleCheckBox = new JCheckBox(" Sin firma visible");
        signatureVisibleCheckBox.setAlignmentX(Component.CENTER_ALIGNMENT);
        signatureVisibleCheckBox.setOpaque(false);
        reasonLabel = new JLabel("Razón:");
        locationLabel = new JLabel("Lugar:");
        contactInfoLabel = new JLabel("Información de contacto:");
        reasonField = new JTextField();
        locationField = new JTextField();
        contactInfoField = new JTextField();
        signButton = new JButton("Firmar documento");
        signButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        signButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                signDocument();
            }
        });

        reportLabel = new CopyableJLabel();
        reportLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        extendButton = new JButton("Agregar sello de tiempo al documento");
        extendButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        extendButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                extendDocument();
            }
        });

        signButton.setEnabled(false);
        pageLabel.setEnabled(false);
        pageLabel.setVisible(false);
        pageSpinner.setEnabled(false);
        pageSpinner.setVisible(false);
        signatureVisibleCheckBox.setEnabled(false);
        signatureVisibleCheckBox.setVisible(false);
        reasonLabel.setEnabled(false);
        reasonLabel.setVisible(false);
        reasonField.setEnabled(false);
        reasonField.setVisible(false);
        locationLabel.setEnabled(false);
        locationLabel.setVisible(false);
        locationField.setEnabled(false);
        locationField.setVisible(false);
        contactInfoLabel.setEnabled(false);
        contactInfoLabel.setVisible(false);
        contactInfoField.setEnabled(false);
        contactInfoField.setVisible(false);
        extendButton.setEnabled(false);
        JButton fileButton = new JButton("Elegir...");
        imageLabel = new JLabel();
        imageLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        fileButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                showLoadDialog(frame);
            }
        });
        signatureLabel =
            new JLabel("<html><span style='font-size: 8pt'>" +
                "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;FIRMA<br>" +
                "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;VISIBLE</span></html>");
        signatureLabel.setCursor(
            Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
        signatureLabel.setForeground(new Color(0, 0, 0, 0));
        signatureLabel.setBackground(new Color(127, 127, 127, 127));
        signatureLabel.setOpaque(true);
        signatureLabel.setBounds(119, 0, 80, 20);
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
        signPanel.add(signatureVisibleCheckBox);
        signPanel.add(reasonLabel);
        signPanel.add(reasonField);
        signPanel.add(locationLabel);
        signPanel.add(locationField);
        signPanel.add(contactInfoLabel);
        signPanel.add(contactInfoField);
        JPanel validatePanel = new ScrollableJPanel();
        validatePanel.setLayout(new BoxLayout(validatePanel,
            BoxLayout.PAGE_AXIS));
        validatePanel.add(extendButton);
        validatePanel.add(reportLabel);
        JScrollPane validateScrollPane = new JScrollPane();
        validateScrollPane.setHorizontalScrollBarPolicy(
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        validateScrollPane.setBorder(null);
        validateScrollPane.setViewportView(validatePanel);
        validateScrollPane.setOpaque(false);
        validateScrollPane.getViewport().setOpaque(false);
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
                openProjectWebsite();
            }
        });
        iconLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        descriptionLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        websiteButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        aboutPanel.add(iconLabel);
        aboutPanel.add(descriptionLabel);
        aboutPanel.add(websiteButton);
        tabbedPane.setBorder(new EmptyBorder(10, 10, 10, 10));
        if (!isRemote) {
            tabbedPane.addTab("Firmar", signPanel);
            tabbedPane.addTab("Validación", validateScrollPane);
            tabbedPane.addTab("Acerca de", aboutPanel);
            frame.add(filePanel, BorderLayout.PAGE_START);
            frame.add(tabbedPane, BorderLayout.CENTER);
        } else {
            frame.add(signPanel, BorderLayout.CENTER);
        }
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setMinimumSize(new Dimension(480, 672));
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        if (documenttosign != null) {
            loadDocument(documenttosign);
        }
    }

    public void loadDocument(String fileName) {
        fileField.setText(fileName);
        signButton.setEnabled(true);
        DSSDocument mimeDocument;
        if (isRemote) {
            mimeDocument = toSignDocument;
        } else {
            mimeDocument = new FileDocument(fileName);
        }
        MimeType mimeType = mimeDocument.getMimeType();
        try {
            if (doc != null) {
                doc.close();
            }
            if (mimeType == MimeType.PDF || isRemote) {
                if (isRemote) { // Only supports PDF for remote for now
                    doc = PDDocument.load(toSignByteArray);
                } else {
                    doc = PDDocument.load(new File(fileName));
                }
                int pages = doc.getNumberOfPages();
                renderer = new PDFRenderer(doc);
                pageLabel.setVisible(true);
                pageSpinner.setVisible(true);
                signatureVisibleCheckBox.setVisible(true);
                reasonLabel.setVisible(true);
                reasonField.setVisible(true);
                locationLabel.setVisible(true);
                locationField.setVisible(true);
                contactInfoLabel.setVisible(true);
                contactInfoField.setVisible(true);
                if (pages > 0) {
                    pageImage = renderer.renderImage(0, 1 / 2.5f);
                    SpinnerNumberModel model =
                        ((SpinnerNumberModel)pageSpinner.getModel());
                    model.setMinimum(1);
                    model.setMaximum(pages);
                    pageLabel.setEnabled(true);
                    pageSpinner.setEnabled(true);
                    pageSpinner.setValue(1);
                    signatureVisibleCheckBox.setEnabled(true);
                    reasonLabel.setEnabled(true);
                    reasonField.setEnabled(true);
                    locationLabel.setEnabled(true);
                    locationField.setEnabled(true);
                    contactInfoLabel.setEnabled(true);
                    contactInfoField.setEnabled(true);
                }
                imageLabel.setBorder(new LineBorder(Color.BLACK));
                imageLabel.setIcon(new ImageIcon(pageImage));
                imageLabel.setVisible(true);
            }
            else if (mimeType == MimeType.ODG || mimeType == MimeType.ODP
                || mimeType == MimeType.ODS || mimeType == MimeType.ODT
                || mimeType == MimeType.XML) {
                imageLabel.setVisible(false);
                pageLabel.setVisible(false);
                pageSpinner.setVisible(false);
                signatureVisibleCheckBox.setVisible(false);
                reasonLabel.setVisible(true);
                reasonField.setVisible(true);
                locationLabel.setVisible(true);
                locationField.setVisible(true);
                contactInfoLabel.setVisible(true);
                contactInfoField.setVisible(true);
            }
        } catch (Exception e) {
            showError(Throwables.getRootCause(e));
        }

        if (!isRemote) {
            Validator validator = null;
            try {
                validator = new Validator(fileName);
                if (validator.isSigned()) {
                    extendButton.setEnabled(true);
                    tabbedPane.setSelectedIndex(1);
                } else {
                    extendButton.setEnabled(false);
                    tabbedPane.setSelectedIndex(0);
                }
            } catch (Exception e) {
                if (mimeType == MimeType.ODG || mimeType == MimeType.ODP
                    || mimeType == MimeType.ODS || mimeType == MimeType.ODT) {
                    // Workaround for DSS 5.6 not recognizing unsigned ODF
                } else {
                    e.printStackTrace();
                    reportLabel.setText("Error al validar documento. " +
                        "Agradeceríamos que informara sobre este " +
                        "inconveniente a los desarrolladores de la " +
                        "aplicación para repararlo.");
                }
                reportLabel.setText("");
                extendButton.setEnabled(false);
                tabbedPane.setSelectedIndex(0);
            }

            if (validator != null) {
                try {
                    Report report = new Report(validator.getReports());
                    reportLabel.setText(report.getReport());
                } catch (Exception e) {
                    e.printStackTrace();
                    reportLabel.setText("Error al generar reporte. " +
                        "Agradeceríamos que informara sobre este " +
                        "inconveniente a los desarrolladores de la " +
                        "aplicación para repararlo.");
                }
            }
        }
    }

    public void signDocument() {
        String fileName = getDocumentToSign();
        if (fileName != null || isRemote) {
            if (!isRemote) {
                toSignDocument = new FileDocument(fileName);
            }
            signedDocument = null;
            PasswordProtection pin = getPin();
            if (pin.getPassword() != null
                && pin.getPassword().length != 0) {
                MimeType mimeType = toSignDocument.getMimeType();
                if (mimeType == MimeType.PDF || isRemote) {
                    FirmadorPAdES firmador = new FirmadorPAdES(
                        GUISwing.this);
                    firmador.selectSlot();
                    if (firmador.selectedSlot == -1) return;
                    firmador.setVisible_signature(
                        !signatureVisibleCheckBox.isSelected());
                    firmador.addVisibleSignature(
                        (int)pageSpinner.getValue(),
                        (int)Math.round(signatureLabel.getX() * 2.5),
                        (int)Math.round(signatureLabel.getY() * 2.5));
                    signedDocument = firmador.sign(toSignDocument,
                        pin, reasonField.getText(), locationField.getText(),
                        contactInfoField.getText());
                } else if (mimeType == MimeType.ODG
                    || mimeType == MimeType.ODP
                    || mimeType == MimeType.ODS
                    || mimeType == MimeType.ODT) {
                    FirmadorOpenDocument firmador =
                        new FirmadorOpenDocument(GUISwing.this);
                    firmador.selectSlot();
                    if (firmador.selectedSlot == -1) return;
                    signedDocument = firmador.sign(toSignDocument,
                        pin);
                } else {
                    FirmadorXAdES firmador = new FirmadorXAdES(
                        GUISwing.this);
                    firmador.selectSlot();
                    if (firmador.selectedSlot == -1) return;
                    signedDocument = firmador.sign(toSignDocument,
                        pin);
                }
                try {
                    pin.destroy();
                } catch (Exception e) {}
            }
            if (signedDocument != null && !isRemote) {
                fileName = getPathToSave();
                if (fileName != null) {
                    try {
                        signedDocument.save(fileName);
                        showMessage(
                            "Documento guardado satisfactoriamente " +
                            "en<br>" + fileName);
                        loadDocument(fileName);
                    } catch (IOException e) {
                        showError(Throwables.getRootCause(e));
                    }
                }
            }
            if (isRemote) {
                alreadySignedDocument = true;
            }
        }
    }

    public void extendDocument() {
        String fileName = getDocumentToSign();

        if (fileName != null) {

            DSSDocument toExtendDocument = new FileDocument(fileName);
            DSSDocument extendedDocument = null;
            MimeType mimeType = toExtendDocument.getMimeType();
            if (mimeType == MimeType.PDF) {
                FirmadorPAdES firmador = new FirmadorPAdES(
                    GUISwing.this);
                extendedDocument = firmador.extend(toExtendDocument);
            } else if (mimeType == MimeType.ODG
                || mimeType == MimeType.ODP
                || mimeType == MimeType.ODS
                || mimeType == MimeType.ODT) {
                FirmadorOpenDocument firmador =
                    new FirmadorOpenDocument(GUISwing.this);
                extendedDocument = firmador.extend(toExtendDocument);
            } else {
                FirmadorXAdES firmador = new FirmadorXAdES(
                    GUISwing.this);
                extendedDocument = firmador.extend(toExtendDocument);
            }
            if (extendedDocument != null) {
                fileName = getPathToSaveExtended();
                if (fileName != null) {
                    try {
                        extendedDocument.save(fileName);
                        showMessage(
                            "Documento guardado satisfactoriamente" +
                            " en<br>" + fileName);
                        loadDocument(fileName);
                    } catch (IOException e) {
                        showError(Throwables.getRootCause(e));
                    }
                }
            }
        }
    }

    private void showLoadDialog(JFrame frame) {
        loadDialog = new FileDialog(frame,
            "Seleccionar documento a firmar");
        loadDialog.setFilenameFilter(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name.toLowerCase().endsWith(".odg")
                    || name.toLowerCase().endsWith(".odp")
                    || name.toLowerCase().endsWith(".ods")
                    || name.toLowerCase().endsWith(".odt")
                    || name.toLowerCase().endsWith(".pdf")
                    || name.toLowerCase().endsWith(".xml");
            }
        });
        loadDialog.setFile("*.odg;*.odp;*.ods;*.odt;*.pdf;*.xml");
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

    private void openProjectWebsite() {
        if (Desktop.isDesktopSupported()) {
            try {
                Desktop.getDesktop().browse(
                    new URI("https://firmador.app"));
            } catch (Exception e) {
                showError(Throwables.getRootCause(e));
            }
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
                    "versiones superiores a Java 8.<br>" +
                    "Este inconveniente se corregirá en próximas versiones. " +
                    "Disculpe las molestias.";
                break;
            case "java.security.ProviderException":
                message = "No se ha encontrado la librería de Firma Digital " +
                    "en el sistema.<br>" +
                    "¿Están instalados los controladores?";
                break;
            case "java.security.NoSuchAlgorithmException":
                message = "No se ha encontrado ninguna tarjeta conectada." +
                    "<br>Asegúrese de que la tarjeta y el lector están " +
                    "conectados de forma correcta.";
                break;
            case "sun.security.pkcs11.wrapper.PKCS11Exception":
                switch (message) {
                case "CKR_GENERAL_ERROR":
                    message = "No se ha podido contactar con el servicio " +
                        "del lector de tarjetas.<br>" +
                        "¿Está correctamente instalado o configurado?";
                    break;
                case "CKR_SLOT_ID_INVALID":
                    message = "No se ha podido encontrar ningún lector " +
                    "conectado o el controlador del lector no está instalado.";
                    break;
                case "CKR_PIN_INCORRECT":
                    messageType = JOptionPane.WARNING_MESSAGE;
                    message = "¡PIN INCORRECTO!<br><br>" +
                        "ADVERTENCIA: si se ingresa un PIN incorrecto " +
                        "varias veces sin acertar,<br>" +
                        "el dispositivo de firma se bloqueará.";
                    break;
                case "CKR_PIN_LOCKED":
                    message = "PIN BLOQUEADO<br><br>" +
                        "Lo sentimos, el dispositivo de firma no se puede " +
                        "utilizar porque está bloqueado.<br>" +
                        "Contacte con su proveedor para desbloquearlo.";
                    break;
                default:
                    break;
                }
                break;
            case "java.io.IOException":
                if (message.contains("asepkcs") ||
                    message.contains("libASEP11")) {
                    message = "No se ha encontrado la librería de Firma " +
                        "Digital en el sistema.<br>" +
                        "¿Están instalados los controladores?";
                    break;
                }
            default:
                error.printStackTrace();
                message = "Error: " + className + "<br>" +
                    "Detalle: " + message + "<br>" +
                    "Agradecemos que comunique este mensaje de error a los " +
                    "autores del programa<br>" +
                    "para detallar mejor el posible motivo de este error " +
                    "en próximas versiones.";
                break;
        }

        JOptionPane.showMessageDialog(null, new CopyableJLabel(message),
            "Mensaje de Firmador", messageType);

        if (messageType == JOptionPane.ERROR_MESSAGE) {
            System.exit(0);
        }
    }

    public void showMessage(String message) {
        JOptionPane.showMessageDialog(null, new CopyableJLabel(message),
            "Mensaje de Firmador", JOptionPane.INFORMATION_MESSAGE);
    }

    public int getSelection(String[] options) {
        int dev = 0;

        if (options == null || options.length == 0) {
            String message = "No se ha encontrado ninguna tarjeta " +
                "conectada.<br>Asegúrese de que la tarjeta y el lector " +
                "están conectados de forma correcta.";
            JOptionPane.showMessageDialog(null, new CopyableJLabel(message),
                "Error al firmar", JOptionPane.ERROR_MESSAGE);
            return -1;
        }
        String input = JOptionPane.showInputDialog(null, "Propietario: ",
            "Seleccione el dispositivo para firmar",
            JOptionPane.QUESTION_MESSAGE, null, options, options[0])
            .toString();

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
