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

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Component;
import java.awt.Desktop;
import java.awt.FileDialog;
import java.awt.Image;
import java.awt.Window;
import java.awt.datatransfer.DataFlavor;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDropEvent;
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
import java.io.IOException;
import java.net.InetAddress;
import java.net.URI;
import java.nio.file.Paths;
import java.security.KeyStore.PasswordProtection;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.swing.GroupLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPasswordField;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.ScrollPaneConstants;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.border.LineBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import cr.libre.firmador.FirmadorCAdES;
import cr.libre.firmador.FirmadorOpenDocument;
import cr.libre.firmador.FirmadorPAdES;
import cr.libre.firmador.FirmadorXAdES;
import cr.libre.firmador.Report;
import cr.libre.firmador.Validator;
import cr.libre.firmador.gui.swing.CopyableJLabel;
import cr.libre.firmador.gui.swing.ScrollableJPanel;
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
    private Image image = new ImageIcon(this.getClass().getClassLoader().getResource("firmador.png")).getImage();
    private JTextField fileField;
    private JTabbedPane frameTabbedPane;
    private JTabbedPane optionsTabbedPane;
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
    private JLabel AdESFormatLabel;
    private ButtonGroup AdESFormatButtonGroup;
    private JRadioButton CAdESButton;
    private JRadioButton XAdESButton;
    private JButton signButton;
    private JButton extendButton;
    private JLabel AdESLevelLabel;
    private JRadioButton levelTButton;
    private JRadioButton levelLTButton;
    private JRadioButton levelLTAButton;
    private ButtonGroup AdESLevelButtonGroup;
    private BufferedImage pageImage;
    private PDDocument doc;
    private PDFRenderer renderer;
    private JFrame frame;

    public void loadGUI() {
        try {
            Application.getApplication().setDockIconImage(image);
        } catch (RuntimeException | IllegalAccessError e) { /* macOS dock icon support specific code. */ }
        try {
            try {
                UIManager.setLookAndFeel("com.sun.java.swing.plaf.gtk.GTKLookAndFeel");
            } catch (UnsupportedLookAndFeelException | ClassNotFoundException e) {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            }
        } catch (Exception e) {
            showError(Throwables.getRootCause(e));
        }

        String origin = System.getProperty("jnlp.remoteOrigin");
        isRemote = (origin != null);
        if (isRemote) {
            SwingWorker<Void, byte[]> remote = new SwingWorker<Void, byte[]>() {
                private HttpServer server;
                private String requestFileName;
                protected Void doInBackground() throws IOException, InterruptedException {
                    HttpRequestHandler requestHandler = new HttpRequestHandler() {
                        public void handle(HttpRequest request, HttpResponse response, HttpContext context) throws HttpException, IOException {
                            response.setHeader("Access-Control-Allow-Origin", origin);
                            response.setHeader("Vary", "Origin");
                            if (request.getRequestLine().getUri().equals("/close")) System.exit(0);
                            requestFileName = request.getRequestLine().getUri().substring(1);
                            response.setStatusCode(HttpStatus.SC_ACCEPTED);
                            if (request instanceof HttpEntityEnclosingRequest) {
                                HttpEntity entity = ((HttpEntityEnclosingRequest)request).getEntity();
                                if (alreadySignedDocument) {
                                    response.setStatusCode(HttpStatus.SC_OK);
                                    ByteArrayOutputStream os = new ByteArrayOutputStream();
                                    signedDocument.writeTo(os);
                                    response.setEntity(new ByteArrayEntity(os.toByteArray(), ContentType.DEFAULT_TEXT));
                                }
                                if (entity.getContentLength() > 0) {
                                    ByteArrayOutputStream os = new ByteArrayOutputStream();
                                    entity.writeTo(os);
                                    publish(os.toByteArray());
                                }
                            }
                        }
                    };
                    server = ServerBootstrap.bootstrap().setListenerPort(3516).setLocalAddress(InetAddress.getLoopbackAddress()).registerHandler("*", requestHandler).create();
                    server.start();
                    server.awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS);
                    return null;
                }
                protected void process(List<byte[]> chunks) {
                    toSignByteArray = chunks.get(chunks.size() - 1);
                    toSignDocument = new InMemoryDocument(toSignByteArray, requestFileName);
                    loadDocument(null);
                }
            };
            remote.execute();
        }

        frame = new JFrame("Firmador");
        frame.setIconImage(image.getScaledInstance(256, 256, Image.SCALE_SMOOTH));
        frame.setDropTarget(new DropTarget() {
            public synchronized void drop(DropTargetDropEvent e) {
                try {
                    e.acceptDrop(DnDConstants.ACTION_COPY);
                    List<File> droppedFiles = (List<File>) e.getTransferable().getTransferData(DataFlavor.javaFileListFlavor);
                    for (File file : droppedFiles) {
                        // FIXME: handle multiple files on array
                        loadDocument(file.toString());
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });

        JLabel fileLabel = new JLabel("Documento: ");
        fileField = new JTextField("(Vacío)");
        fileField.setToolTipText("<html>Este campo indica el nombre del fichero<br>que está seleccionado para firmar o validar.</html>");
        fileField.setEditable(false);
        pageLabel = new JLabel("Página:");
        pageSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 100, 1));
        pageSpinner.setToolTipText("<html>Este control permite seleccionar el número de página<br>para visualizar y seleccionar en cuál mostrar la firma visible.</html>");
        pageSpinner.setMaximumSize(pageSpinner.getPreferredSize());
        pageSpinner.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                try {
                    int page = (int)pageSpinner.getValue();
                    if (page > 0) {
                        pageImage = renderer.renderImage(page - 1, 1 / 1.5f);
                        imageLabel.setIcon(new ImageIcon(pageImage));
                        frame.pack();
                        frame.setMinimumSize(frame.getSize());
                    }
                } catch (Exception ex) {
                    showError(Throwables.getRootCause(ex));
                }
            }
        });
        signatureVisibleCheckBox = new JCheckBox(" Sin firma visible");
        signatureVisibleCheckBox.setToolTipText("<html>Marque esta casilla si no desea representar visualmente una firma<br>en una página del documento a la hora de firmarlo.</html>");
        signatureVisibleCheckBox.setOpaque(false);
        reasonLabel = new JLabel("Razón:");
        locationLabel = new JLabel("Lugar:");
        contactInfoLabel = new JLabel("Contacto:");
        reasonField = new JTextField();
        reasonField.setToolTipText("<html>Este campo opcional permite indicar una razón<br>o motivo por el cual firma el documento.</html>");
        locationField = new JTextField();
        locationField.setToolTipText("<html>Este campo opcional permite indicar el lugar físico,<br>por ejemplo la ciudad, en la cual declara firmar.</html>");
        contactInfoField = new JTextField();
        contactInfoField.setToolTipText("<html>Este campo opcional permite indicar una<br>manera de contactar con la persona firmante,<br>por ejemplo una dirección de correo electrónico.</html>");

        AdESFormatLabel = new JLabel("Formato AdES:");
        CAdESButton = new JRadioButton("CAdES");
        CAdESButton.setActionCommand("CAdES");
        CAdESButton.setContentAreaFilled(false);
        XAdESButton = new JRadioButton("XAdES", true);
        XAdESButton.setActionCommand("XAdES");
        XAdESButton.setContentAreaFilled(false);
        AdESFormatButtonGroup = new ButtonGroup();
        AdESFormatButtonGroup.add(CAdESButton);
        AdESFormatButtonGroup.add(XAdESButton);
        AdESLevelLabel = new JLabel("Nivel AdES:");
        levelTButton = new JRadioButton("T");
        levelTButton.setActionCommand("T");
        levelTButton.setContentAreaFilled(false);
        levelLTButton = new JRadioButton("LT");
        levelLTButton.setActionCommand("LT");
        levelLTButton.setContentAreaFilled(false);
        levelLTAButton = new JRadioButton("LTA", true);
        levelLTAButton.setActionCommand("LTA");
        levelLTAButton.setContentAreaFilled(false);
        AdESLevelButtonGroup = new ButtonGroup();
        AdESLevelButtonGroup.add(levelTButton);
        AdESLevelButtonGroup.add(levelLTButton);
        AdESLevelButtonGroup.add(levelLTAButton);

        signButton = new JButton("Firmar documento");
        signButton.setToolTipText("<html>Este botón permite firmar el documento seleccionado.<br>Requiere dispositivo de Firma Digital al cual se le<br>solicitará ingresar el PIN.</html>");
        signButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                signDocuments();
            }
        });

        reportLabel = new CopyableJLabel();
        extendButton = new JButton("Agregar sello de tiempo al documento");
        extendButton.setToolTipText("<html>Este botón permite que el documento firmado que está cargado actualmente<br>agregue un nuevo sello de tiempo a nivel documento, con el propósito de<br>archivado longevo. También permite ampliar el nivel de firma a AdES-LTA<br>si el documento tiene un nivel de firma avanzada inferior.</html>");
        extendButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                extendDocument();
            }
        });

        signButton.setEnabled(false);
        pageLabel.setVisible(false);
        pageSpinner.setVisible(false);
        signatureVisibleCheckBox.setVisible(false);
        reasonLabel.setVisible(false);
        reasonField.setVisible(false);
        locationLabel.setVisible(false);
        locationField.setVisible(false);
        contactInfoLabel.setVisible(false);
        contactInfoField.setVisible(false);
        AdESFormatLabel.setVisible(false);
        CAdESButton.setVisible(false);
        XAdESButton.setVisible(false);
        AdESLevelLabel.setVisible(false);
        levelTButton.setVisible(false);
        levelLTButton.setVisible(false);
        levelLTAButton.setVisible(false);
        extendButton.setEnabled(false);
        JButton fileButton = new JButton("Elegir...");
        fileButton.setToolTipText("<html>Haga clic en este botón para seleccionar uno o<br>varios ficheros a firmar, o un fichero a validar.</html>");

        imageLabel = new JLabel();
        fileButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                showLoadDialog();
                frame.pack();
                frame.setMinimumSize(frame.getSize());
            }
        });
        signatureLabel = new JLabel("<html><span style='font-size: 12pt'>" +
                "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;FIRMA<br>" +
                "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;VISIBLE</span></html>");
        //signatureLabel.setToolTipText("<html>Esta etiqueta es un recuadro arrastrable que representa<br>la ubicación de la firma visible en la página seleccionada.<br><br>Se puede cambiar su posición haciendo clic sobre el recuadro<br>y moviendo el mouse sin soltar el botón de clic<br>hasta soltarlo en la posición deseada.</html>");
        if (System.getProperty("os.name").startsWith("Mac")) signatureLabel.setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
        else signatureLabel.setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
        signatureLabel.setForeground(new Color(0, 0, 0, 0));
        signatureLabel.setBackground(new Color(127, 127, 127, 127));
        signatureLabel.setOpaque(true);
        signatureLabel.setBounds(198, 0, 133, 33);
        imageLabel.addMouseMotionListener(new MouseMotionAdapter() {
            public void mouseDragged(MouseEvent e) {
                signatureLabel.setLocation(e.getX() - signatureLabel.getWidth() / 2, e.getY() - signatureLabel.getHeight() / 2);
            }
        });
        imageLabel.add(signatureLabel);

        JLabel iconLabel = new JLabel(new ImageIcon(image.getScaledInstance(128, 128, Image.SCALE_SMOOTH)));
        JLabel descriptionLabel = new JLabel("<html><p align='center'><b>Firmador</b><br><br>" +
            "Versión " + getClass().getPackage().getSpecificationVersion() + "<br><br>" +
            "Herramienta para firmar documentos digitalmente.<br><br>" +
            "Los documentos firmados con esta herramienta cumplen con la<br>" +
            "Política de Formatos Oficiales de los Documentos Electrónicos<br>" +
            "Firmados Digitalmente de Costa Rica.<br><br></p></html>", JLabel.CENTER);
        JButton websiteButton = new JButton("Visitar sitio web del proyecto");
        websiteButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                openProjectWebsite();
            }
        });

        JPanel pdfOptionsPanel = new JPanel();
        JPanel advancedOptionsPanel = new JPanel();

        optionsTabbedPane = new JTabbedPane();
        optionsTabbedPane.addTab("Opciones PDF", pdfOptionsPanel);
        optionsTabbedPane.setToolTipTextAt(0, "<html>En esta pestaña se muestran opciones específicas<br>para documentos en formato PDF.</html>");
        optionsTabbedPane.addTab("Opciones avanzadas", advancedOptionsPanel);
        optionsTabbedPane.setToolTipTextAt(1, "<html>En esta pestaña se muestran opciones avanzadas<br>relacionadas con la creación de la firma.</html>");

        JPanel signPanel = new JPanel();
        GroupLayout signLayout;
        if (isRemote) signLayout = new GroupLayout(frame.getContentPane());
        else signLayout = new GroupLayout(signPanel);
        signLayout.setAutoCreateGaps(true);
        signLayout.setAutoCreateContainerGaps(true);
            signLayout.setHorizontalGroup(
            signLayout.createSequentialGroup()
                .addComponent(imageLabel)
                .addGroup(signLayout.createParallelGroup(GroupLayout.Alignment.TRAILING)
                    .addComponent(pageLabel)
                    .addComponent(reasonLabel)
                    .addComponent(locationLabel)
                    .addComponent(contactInfoLabel)
                    .addComponent(AdESFormatLabel)
                    .addComponent(AdESLevelLabel))
                .addGroup(signLayout.createParallelGroup()
                    .addGroup(signLayout.createSequentialGroup()
                        .addComponent(pageSpinner)
                        .addComponent(signatureVisibleCheckBox))
                    .addComponent(reasonField)
                    .addComponent(locationField)
                    .addComponent(contactInfoField)
                    .addGroup(signLayout.createSequentialGroup()
                        .addComponent(CAdESButton)
                        .addComponent(XAdESButton))
                    .addGroup(signLayout.createSequentialGroup()
                        .addComponent(levelTButton)
                        .addComponent(levelLTButton)
                        .addComponent(levelLTAButton))
                    .addComponent(signButton)));
        signLayout.setVerticalGroup(
            signLayout.createParallelGroup()
                .addComponent(imageLabel)
                .addGroup(signLayout.createSequentialGroup()
                    .addGroup(signLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                        .addComponent(pageSpinner)
                        .addComponent(signatureVisibleCheckBox)
                        .addComponent(pageLabel))
                    .addGroup(signLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                        .addComponent(reasonField)
                        .addComponent(reasonLabel))
                    .addGroup(signLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                        .addComponent(locationField)
                        .addComponent(locationLabel))
                    .addGroup(signLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                        .addComponent(contactInfoField)
                        .addComponent(contactInfoLabel))
                    .addGroup(signLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                        .addComponent(AdESFormatLabel)
                        .addComponent(CAdESButton)
                        .addComponent(XAdESButton))
                    .addGroup(signLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                        .addComponent(AdESLevelLabel)
                        .addComponent(levelTButton)
                        .addComponent(levelLTButton)
                        .addComponent(levelLTAButton))
                    .addComponent(signButton)));
        if (!isRemote) signPanel.setLayout(signLayout);
        signPanel.setOpaque(false);

        JPanel validatePanel = new ScrollableJPanel();
        GroupLayout validateLayout = new GroupLayout(validatePanel);
        validateLayout.setAutoCreateGaps(true);
        validateLayout.setAutoCreateContainerGaps(true);
        validateLayout.setHorizontalGroup(
            validateLayout.createParallelGroup()
                .addComponent(reportLabel)
                .addComponent(extendButton)
        );
        validateLayout.setVerticalGroup(
            validateLayout.createSequentialGroup()
                .addComponent(reportLabel)
                .addComponent(extendButton)
        );
        validatePanel.setLayout(validateLayout);
        validatePanel.setOpaque(false);

        JScrollPane validateScrollPane = new JScrollPane();
        validateScrollPane.setPreferredSize(validateScrollPane.getPreferredSize());
        validateScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        validateScrollPane.setBorder(null);
        validateScrollPane.setViewportView(validatePanel);
        validateScrollPane.setOpaque(false);
        validateScrollPane.getViewport().setOpaque(false);

        JPanel aboutPanel = new JPanel();
        GroupLayout aboutLayout = new GroupLayout(aboutPanel);
        aboutLayout.setAutoCreateGaps(true);
        aboutLayout.setAutoCreateContainerGaps(true);
        aboutLayout.setHorizontalGroup(
            aboutLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
                .addComponent(iconLabel)
                .addComponent(descriptionLabel)
                .addComponent(websiteButton)
        );
        aboutLayout.setVerticalGroup(
            aboutLayout.createSequentialGroup()
                .addComponent(iconLabel)
                .addComponent(descriptionLabel)
                .addComponent(websiteButton)
        );
        aboutPanel.setLayout(aboutLayout);
        aboutPanel.setOpaque(false);

        frameTabbedPane = new JTabbedPane();
        frameTabbedPane.addTab("Firmar", signPanel);
        frameTabbedPane.setToolTipTextAt(0, "<html>En esta pestaña se muestran las opciones<br>para firmar el documento seleccionado.</html>");
        frameTabbedPane.addTab("Validación", validateScrollPane);
        frameTabbedPane.setToolTipTextAt(1, "<html>En esta pestaña se muestra información de validación<br>de las firmas digitales del documento seleccionado.</html>");
        frameTabbedPane.addTab("Acerca de", aboutPanel);
        frameTabbedPane.setToolTipTextAt(2, "<html>En esta estaña se muestra información<br>acerca de este programa.</html>");

        GroupLayout frameLayout = new GroupLayout(frame.getContentPane());
        frameLayout.setAutoCreateGaps(true);
        frameLayout.setAutoCreateContainerGaps(true);
        frameLayout.setHorizontalGroup(
            frameLayout.createParallelGroup()
                .addGroup(frameLayout.createSequentialGroup()
                    .addComponent(fileLabel)
                    .addComponent(fileField)
                    .addComponent(fileButton))
                .addComponent(frameTabbedPane)
        );
        frameLayout.setVerticalGroup(
            frameLayout.createSequentialGroup()
                .addGroup(frameLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(fileLabel)
                    .addComponent(fileField)
                    .addComponent(fileButton))
                .addComponent(frameTabbedPane)
        );
        if (!isRemote) frame.getContentPane().setLayout(frameLayout);
        else frame.getContentPane().setLayout(signLayout);

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setMinimumSize(frame.getSize());
        frame.setLocationByPlatform(true);
        frame.setVisible(true);

        if (documenttosign != null) loadDocument(documenttosign);
    }

    public void loadDocument(String fileName) {
        if (!isRemote) fileField.setText(Paths.get(fileName).getFileName().toString());
        signButton.setEnabled(true);
        DSSDocument mimeDocument;
        if (isRemote) mimeDocument = toSignDocument;
        else mimeDocument = new FileDocument(fileName);
        MimeType mimeType = mimeDocument.getMimeType();
        try {
            if (doc != null) doc.close();
            imageLabel.setVisible(false);
            pageLabel.setVisible(false);
            pageSpinner.setVisible(false);
            signatureVisibleCheckBox.setVisible(false);
            reasonLabel.setVisible(false);
            reasonField.setVisible(false);
            locationLabel.setVisible(false);
            locationField.setVisible(false);
            contactInfoLabel.setVisible(false);
            contactInfoField.setVisible(false);
            AdESFormatLabel.setVisible(false);
            CAdESButton.setVisible(false);
            XAdESButton.setVisible(false);
            AdESLevelLabel.setVisible(false);
            levelTButton.setVisible(false);
            levelLTButton.setVisible(false);
            levelLTAButton.setVisible(false);
            if (mimeType == MimeType.PDF) {
                if (isRemote) doc = PDDocument.load(toSignByteArray);
                else doc = PDDocument.load(new File(fileName));
                int pages = doc.getNumberOfPages();
                renderer = new PDFRenderer(doc);
                if (pages > 0) {
                    pageImage = renderer.renderImage(0, 1 / 1.5f);
                    SpinnerNumberModel model = ((SpinnerNumberModel)pageSpinner.getModel());
                    model.setMinimum(1);
                    model.setMaximum(pages);
                    pageSpinner.setValue(1);
                }
                imageLabel.setBorder(new LineBorder(Color.BLACK));
                imageLabel.setIcon(new ImageIcon(pageImage));
                imageLabel.setVisible(true);
                pageLabel.setVisible(true);
                pageSpinner.setVisible(true);
                signatureVisibleCheckBox.setVisible(true);
                reasonLabel.setVisible(true);
                reasonField.setVisible(true);
                locationLabel.setVisible(true);
                locationField.setVisible(true);
                contactInfoLabel.setVisible(true);
                contactInfoField.setVisible(true);
                AdESLevelLabel.setVisible(true);
                levelTButton.setVisible(true);
                levelLTButton.setVisible(true);
                levelLTAButton.setVisible(true);
            } else if (mimeType == MimeType.XML || mimeType == MimeType.ODG || mimeType == MimeType.ODP || mimeType == MimeType.ODS || mimeType == MimeType.ODT) {
            } else {
                AdESFormatLabel.setVisible(true);
                CAdESButton.setVisible(true);
                XAdESButton.setVisible(true);
                AdESLevelLabel.setVisible(false);
                levelTButton.setVisible(false);
                levelLTButton.setVisible(false);
                levelLTAButton.setVisible(false);
            }
            frame.pack();
            frame.setMinimumSize(frame.getSize());
        } catch (Exception e) {
            showError(Throwables.getRootCause(e));
        }
        if (!isRemote) {
            Validator validator = null;
            try {
                validator = new Validator(fileName);
                if (validator.isSigned()) {
                    extendButton.setEnabled(true);
                    frameTabbedPane.setSelectedIndex(1);
                } else {
                    extendButton.setEnabled(false);
                    frameTabbedPane.setSelectedIndex(0);
                }
            } catch (Exception e) {
                e.printStackTrace();
                reportLabel.setText("Error al validar documento.<br>" +
                    "Agradeceríamos que informara sobre este inconveniente<br>" +
                    "a los desarrolladores de la aplicación para repararlo.");
                reportLabel.setText("");
                extendButton.setEnabled(false);
                frameTabbedPane.setSelectedIndex(0);
            }
            if (validator != null) {
                try {
                    Report report = new Report(validator.getReports());
                    reportLabel.setText(report.getReport());
                } catch (Exception e) {
                    e.printStackTrace();
                    reportLabel.setText("Error al generar reporte.<br>" +
                        "Agradeceríamos que informara sobre este inconveniente<br>" +
                        "a los desarrolladores de la aplicación para repararlo.");
                }
            }
        }
    }

    public void signDocuments() {
        String fileName = getDocumentToSign();
        if (fileName != null || isRemote) {
            if (!isRemote) toSignDocument = new FileDocument(fileName);
            signedDocument = null;
            PasswordProtection pin = getPin();
            String extension = "";
            if (pin.getPassword() != null && pin.getPassword().length != 0) {
                MimeType mimeType = toSignDocument.getMimeType();
                if (mimeType == MimeType.PDF) {
                    FirmadorPAdES firmador = new FirmadorPAdES(GUISwing.this);
                    firmador.setVisibleSignature(!signatureVisibleCheckBox.isSelected());
                    firmador.addVisibleSignature((int)pageSpinner.getValue(), (int)Math.round(signatureLabel.getX() * 1.5), (int)Math.round(signatureLabel.getY() * 1.5));
                    signedDocument = firmador.sign(toSignDocument, pin, AdESLevelButtonGroup.getSelection().getActionCommand(), reasonField.getText(), locationField.getText(), contactInfoField.getText(), System.getProperty("jnlp.signatureImage"), Boolean.getBoolean("jnlp.hideSignatureAdvice"));
                } else if (mimeType == MimeType.ODG || mimeType == MimeType.ODP || mimeType == MimeType.ODS || mimeType == MimeType.ODT) {
                    FirmadorOpenDocument firmador = new FirmadorOpenDocument(GUISwing.this);
                    signedDocument = firmador.sign(toSignDocument, pin);
                } else if (mimeType == MimeType.XML || AdESFormatButtonGroup.getSelection().getActionCommand().equals("XAdES")) {
                    FirmadorXAdES firmador = new FirmadorXAdES(GUISwing.this);
                    signedDocument = firmador.sign(toSignDocument, pin);
                    extension = ".xml";
                } else {
                    FirmadorCAdES firmador = new FirmadorCAdES(GUISwing.this);
                    signedDocument = firmador.sign(toSignDocument, pin);
                    extension = ".p7s"; // p7s detached, p7m enveloping
                }
                try {
                    pin.destroy();
                } catch (Exception e) {}
            }
            if (signedDocument != null && !isRemote) {
                fileName = getPathToSave(extension);
                if (fileName != null) {
                    try {
                        signedDocument.save(fileName);
                        showMessage("Documento guardado satisfactoriamente en<br>" + fileName);
                        loadDocument(fileName);
                    } catch (IOException e) {
                        showError(Throwables.getRootCause(e));
                    }
                }
            }
            if (isRemote) alreadySignedDocument = true;
        }
    }

    public void extendDocument() {
        String fileName = getDocumentToSign();
        if (fileName != null) {
            DSSDocument toExtendDocument = new FileDocument(fileName);
            DSSDocument extendedDocument = null;
            MimeType mimeType = toExtendDocument.getMimeType();
            if (mimeType == MimeType.PDF) {
                FirmadorPAdES firmador = new FirmadorPAdES(GUISwing.this);
                extendedDocument = firmador.extend(toExtendDocument);
            } else if (mimeType == MimeType.ODG || mimeType == MimeType.ODP || mimeType == MimeType.ODS || mimeType == MimeType.ODT) {
                FirmadorOpenDocument firmador = new FirmadorOpenDocument(GUISwing.this);
                extendedDocument = firmador.extend(toExtendDocument);
            } else if (mimeType == MimeType.XML) {
                FirmadorXAdES firmador = new FirmadorXAdES(GUISwing.this);
                extendedDocument = firmador.extend(toExtendDocument);
            }
            if (extendedDocument != null) {
                fileName = getPathToSaveExtended("");
                if (fileName != null) {
                    try {
                        extendedDocument.save(fileName);
                        showMessage("Documento guardado satisfactoriamente en<br>" + fileName);
                        loadDocument(fileName);
                    } catch (IOException e) {
                        showError(Throwables.getRootCause(e));
                    }
                }
            }
        }
    }

    private void showLoadDialog() {
        loadDialog = new FileDialog(frame, "Seleccionar documento a firmar");
        loadDialog.setMultipleMode(true);
        loadDialog.setLocationRelativeTo(null);
        loadDialog.setVisible(true);
        loadDialog.dispose();
        for (File file : loadDialog.getFiles()) {
            // FIXME handle multiple files on array
            loadDocument(file.toString());
            lastDirectory = loadDialog.getDirectory();
            lastFile = file.toString();
        }
    }

    public String getDocumentToSign() {
        return lastFile;
    }

    public String getPathToSave(String extension) {
        if (documenttosave != null) return documenttosave;
        String pathToSave = showSaveDialog("-firmado", extension);
        return pathToSave;
    }

    public String getPathToSaveExtended(String extension) {
        String pathToExtend = showSaveDialog("-sellado", extension);
        return pathToExtend;
    }

    public String showSaveDialog(String suffix, String extension) {
        String fileName = null;
        FileDialog saveDialog = null;
        saveDialog = new FileDialog(saveDialog, "Guardar documento", FileDialog.SAVE);
        saveDialog.setDirectory(lastDirectory);
        String dotExtension = "";
        int lastDot = lastFile.lastIndexOf(".");
        if (extension != "") {
            suffix = ""; // XMLs could reuse same files, however
            dotExtension = extension;
        }
        else if (lastDot >= 0) dotExtension = lastFile.substring(lastDot);
        saveDialog.setFile(lastFile.substring(0, lastFile.lastIndexOf(".")) + suffix + dotExtension);
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
                if (component.isShowing() && (event.getChangeFlags() & HierarchyEvent.SHOWING_CHANGED) != 0) {
                    Window toplevel = SwingUtilities.getWindowAncestor(component);
                    toplevel.addWindowFocusListener(new WindowAdapter() {
                        public void windowGainedFocus(WindowEvent event) {
                            component.requestFocus();
                        }
                    });
                }
            }
        });
        int action = JOptionPane.showConfirmDialog(null, pinField, "Ingresar PIN", JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
        pinField.grabFocus();
        if (action == 0) return new PasswordProtection(pinField.getPassword());
        else return new PasswordProtection(null);
    }

    private void openProjectWebsite() {
        if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
            try {
                Desktop.getDesktop().browse(new URI("https://firmador.libre.cr"));
            } catch (Exception e) {
                showError(Throwables.getRootCause(e));
            }
        }
    }

    public void setArgs(String[] args) {
        List<String> arguments = new ArrayList<String>();
        for (String params : args) {
            if (!params.startsWith("-")) arguments.add(params);
        }
        if (arguments.size() > 1) documenttosign = Paths.get(arguments.get(0)).toAbsolutePath().toString();
        if (arguments.size() > 2) documenttosave = Paths.get(arguments.get(1)).toAbsolutePath().toString();
    }

    public void showError(Throwable error) {
        String message = error.getLocalizedMessage();
        int messageType = JOptionPane.ERROR_MESSAGE;
        String className = error.getClass().getName();
        switch (className) {
            case "java.lang.NoSuchMethodError":
                message = "Esta aplicación es actualmente incompatible con versiones superiores a Java 8<br>" +
                    "cuando se ejecuta desde Java Web Start.<br>" +
                    "Este inconveniente se corregirá en próximas versiones. Disculpe las molestias.";
                break;
            case "java.security.ProviderException":
                message = "No se ha encontrado la librería de Firma Digital en el sistema.<br>" +
                    "¿Están instalados los controladores?";
                break;
            case "java.security.NoSuchAlgorithmException":
                message = "No se ha encontrado ninguna tarjeta conectada.<br>" +
                    "Asegúrese de que la tarjeta y el lector están conectados de forma correcta.";
                break;
            case "sun.security.pkcs11.wrapper.PKCS11Exception":
                switch (message) {
                case "CKR_GENERAL_ERROR":
                    message = "No se ha podido contactar con el servicio del lector de tarjetas.<br>" +
                        "¿Está correctamente instalado o configurado?";
                    break;
                case "CKR_SLOT_ID_INVALID":
                    message = "No se ha podido encontrar ningún lector conectado o el controlador del lector no está instalado.";
                    break;
                case "CKR_PIN_INCORRECT":
                    messageType = JOptionPane.WARNING_MESSAGE;
                    message = "¡PIN INCORRECTO!<br><br>" +
                        "ADVERTENCIA: si se ingresa un PIN incorrecto varias veces sin acertar,<br>" +
                        "el dispositivo de firma se bloqueará.";
                    break;
                case "CKR_PIN_LOCKED":
                    message = "PIN BLOQUEADO<br><br>" +
                        "Lo sentimos, el dispositivo de firma no se puede utilizar porque está bloqueado.<br>" +
                        "Contacte con su proveedor para desbloquearlo.";
                    break;
                default:
                    error.printStackTrace();
                    message = "Error: " + className + "<br>" +
                        "Detalle: " + message + "<br>" +
                        "Agradecemos que comunique este mensaje de error a los autores del programa<br>" +
                        "para detallar mejor el posible motivo de este error en próximas versiones.";
                    break;
                }
                break;
            case "java.io.IOException":
                if (message.contains("asepkcs") || message.contains("libASEP11")) {
                    message = "No se ha encontrado la librería de Firma Digital en el sistema.<br>" +
                        "¿Están instalados los controladores?";
                    break;
                }
            default:
                error.printStackTrace();
                message = "Error: " + className + "<br>" +
                    "Detalle: " + message + "<br>" +
                    "Agradecemos que comunique este mensaje de error a los autores del programa<br>" +
                    "para detallar mejor el posible motivo de este error en próximas versiones.";
                break;
        }
        JOptionPane.showMessageDialog(null, new CopyableJLabel(message), "Mensaje de Firmador", messageType);
        if (messageType == JOptionPane.ERROR_MESSAGE) System.exit(0);
    }

    public void showMessage(String message) {
        JOptionPane.showMessageDialog(null, new CopyableJLabel(message), "Mensaje de Firmador", JOptionPane.INFORMATION_MESSAGE);
    }

    @Override
    public int getSlot() {
        return -1;
    }

    @Override
    public String getPkcs12file() {
        return "";
    }

}
