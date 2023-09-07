/* Firmador is a program to sign documents using AdES standards.

Copyright (C) Firmador authors.

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

import java.awt.Image;
import java.awt.FileDialog;
import java.awt.HeadlessException;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javax.swing.GroupLayout;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import com.apple.eawt.Application;
import eu.europa.esig.dss.enumerations.MimeType;
import eu.europa.esig.dss.enumerations.MimeTypeEnum;
import eu.europa.esig.dss.model.DSSDocument;
import eu.europa.esig.dss.model.FileDocument;
import eu.europa.esig.dss.model.InMemoryDocument;
import org.apache.hc.core5.http.HttpStatus;
import org.apache.pdfbox.io.IOUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cr.libre.firmador.CardSignInfo;
import cr.libre.firmador.ConfigListener;
import cr.libre.firmador.FirmadorCAdES;
import cr.libre.firmador.FirmadorOpenDocument;
import cr.libre.firmador.FirmadorPAdES;
import cr.libre.firmador.FirmadorUtils;
import cr.libre.firmador.FirmadorXAdES;
import cr.libre.firmador.Report;
import cr.libre.firmador.Settings;
import cr.libre.firmador.SettingsManager;
import cr.libre.firmador.Validator;
import cr.libre.firmador.gui.swing.AboutLayout;
import cr.libre.firmador.gui.swing.ConfigPanel;
import cr.libre.firmador.gui.swing.CopyableJLabel;
import cr.libre.firmador.gui.swing.DocumentSelectionGroupLayout;
import cr.libre.firmador.gui.swing.ExecutorWorker;
import cr.libre.firmador.gui.swing.ExecutorWorkerInterface;
import cr.libre.firmador.gui.swing.ExecutorWorkerMultipleFiles;
import cr.libre.firmador.gui.swing.ExecutorWorkerMultipleFilesValidator;
import cr.libre.firmador.gui.swing.LogHandler;
import cr.libre.firmador.gui.swing.LoggingFrame;
import cr.libre.firmador.gui.swing.RemoteDocInformation;
import cr.libre.firmador.gui.swing.RemoteHttpWorker;
import cr.libre.firmador.gui.swing.RequestPinWindow;
import cr.libre.firmador.gui.swing.SignPanel;
import cr.libre.firmador.gui.swing.SwingMainWindowFrame;
import cr.libre.firmador.gui.swing.ValidatePanel;
import cr.libre.firmador.plugins.PluginManager;

public class GUISwing implements GUIInterface, ConfigListener{
    final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private Boolean isRemote;
    public JTabbedPane frameTabbedPane;
    private String documenttosign = null;
    private String documenttosave = null;
    private DocumentSelectionGroupLayout docSelector;
    private PDDocument doc;
    private String fileName;
    private RemoteDocInformation docinfo;
    private RemoteHttpWorker<Void, byte[]> remote;
    private Settings settings;
    private DSSDocument toSignDocument;
    private DSSDocument signedDocument;
    private SwingMainWindowFrame mainFrame;
    @SuppressWarnings("unused")
	private PDFRenderer renderer;
    private SignPanel signPanel;
    private ValidatePanel validatePanel;
    private GUIInterface gui;
    private ExecutorWorkerInterface worker = null;
    private JScrollPane loggingPane;
    private Image image = new ImageIcon(this.getClass().getClassLoader().getResource("firmador.png")).getImage();
    private int tabPosition;

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
            LOG.error("Error cargando GUI", e);
            showError(FirmadorUtils.getRootCause(e));
        }
        settings = SettingsManager.getInstance().getAndCreateSettings();
        isRemote = settings.isRemote();
        LoggingFrame loggingFrame = new LoggingFrame();
        LogHandler handler = LogHandler.getInstance();
        handler.setWritter(loggingFrame);
        handler.register();
        loggingPane = loggingFrame.getLogScrollPane();
        gui = this;
        settings.addListener(this);
        try {
            mainFrame = new SwingMainWindowFrame(isRemote ? "Firmador remoto" : "Firmador");
        } catch (HeadlessException e) {
            LOG.error("No se pudo crear la ventana gráfica. Si se está ejecutando Java en entorno gráfico, verificar que no se ha instalado solamente el paquete headless sino el paquete completo para poder cargar la interfaz gráfica.");
            throw e;
        }
        mainFrame.setGUIInterface(this);

        remote = new RemoteHttpWorker<Void, byte[]>(gui);
        remote.execute();

        signPanel = new SignPanel();
        signPanel.setGUI(this);
        signPanel.initializeActions();
        signPanel.hideButtons();

        GroupLayout signLayout = new GroupLayout(signPanel);
        signPanel.createLayout(signLayout, signPanel);
        settings.addListener(signPanel);
        if (!isRemote) {// TODO add setting for toggling validation tab
            validatePanel = new ValidatePanel();
            validatePanel.setGUI(this);
            validatePanel.initializeActions();
            validatePanel.hideButtons();
        }
        JPanel aboutPanel = new JPanel();
        GroupLayout aboutLayout = new AboutLayout(aboutPanel);
        ((AboutLayout) aboutLayout).setInterface(this);

        aboutPanel.setLayout(aboutLayout);
        aboutPanel.setOpaque(false);

        JPanel configPanel = new ConfigPanel();
        configPanel.setOpaque(false);
        frameTabbedPane = new JTabbedPane();
        frameTabbedPane.addTab("Firmar", signPanel);
        frameTabbedPane.setToolTipTextAt(0, "<html>En esta pestaña se muestran las opciones<br>para firmar el documento seleccionado.</html>");
        tabPosition = 1;
        if (!isRemote) {// TODO add setting for toggling validation tab
            frameTabbedPane.addTab("Validación", validatePanel.getValidateScrollPane());
            frameTabbedPane.setToolTipTextAt(tabPosition, "<html>En esta pestaña se muestra información de validación<br>de las firmas digitales.</html>");
            tabPosition++;
        }
        frameTabbedPane.addTab("Configuración", configPanel);
        frameTabbedPane.setToolTipTextAt(tabPosition, "<html>En esta pestaña se configura<br>aspectos de este programa.</html>");
        frameTabbedPane.addTab("Acerca de", aboutPanel);
        frameTabbedPane.setToolTipTextAt(tabPosition + 1, "<html>En esta pestaña se muestra información<br>acerca de este programa.</html>");
        if (settings.showLogs) showLogs(frameTabbedPane);
        docSelector = new DocumentSelectionGroupLayout(mainFrame.getContentPane(), frameTabbedPane, mainFrame);
        docSelector.setGUI(this);
        docSelector.initializeActions();
        if (!isRemote) mainFrame.getContentPane().setLayout(docSelector);
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainFrame.pack();
        mainFrame.setMinimumSize(mainFrame.getSize());
        mainFrame.setLocationByPlatform(true);
        mainFrame.setVisible(true);
        if (documenttosign != null) loadDocument(documenttosign);
    }

    public void loadDocument(String fileName) {
        gui.nextStep("Cargando el documento");
        if (!isRemote) {
            clearElements();
            docSelector.setLastFile(fileName);
            docSelector.fileField.setText(Paths.get(fileName).getFileName().toString());
            FileDocument mimeDocument = new FileDocument(fileName);

            try {
                if (mimeDocument.getMimeType() == MimeTypeEnum.PDF) doc = PDDocument.load(new File(fileName));
                loadDocument(mimeDocument.getMimeType(), doc);
            } catch (IOException e) {
                LOG.error("Error Leyendo el archivo", e);
                e.printStackTrace();
                clearElements();
            }
            gui.nextStep("Validando firmas dentro del documento");
            validateDocument(fileName);
        } else {
            HashMap<String, RemoteDocInformation> docmap = remote.getDocInformation();
            docinfo = docmap.get(fileName);
            PDDocument doc;
            try {
                byte[] data =IOUtils.toByteArray( docinfo.getInputdata());
                toSignDocument = new InMemoryDocument(data, fileName);
                MimeType mimeType = toSignDocument.getMimeType();
                if (MimeTypeEnum.PDF == mimeType) {
                    doc = PDDocument.load(data);
                    loadDocument(mimeType, doc);
                } else if (mimeType == MimeTypeEnum.XML || mimeType == MimeTypeEnum.ODG || mimeType == MimeTypeEnum.ODP || mimeType == MimeTypeEnum.ODS || mimeType == MimeTypeEnum.ODT) {
                    showMessage("Está intentando firmar un documento XML o un openDocument que no posee visualización");
                    signPanel.getSignButton().setEnabled(true);
                } else signPanel.shownonPDFButtons();
            } catch (IOException e) {
                LOG.error("Error cargando documento", e);
                e.printStackTrace();
            }
        }
    }

    public boolean signDocuments() {
        if (!isRemote) {
            worker = new ExecutorWorker(this);
            SwingUtilities.invokeLater((Runnable) worker);
            Thread.yield();
            return true;
        } else {
            CardSignInfo card = getPin();
            signDocument(card, true);
            try {
                signedDocument.writeTo(docinfo.getData());
                docinfo.setStatus(HttpStatus.SC_SUCCESS);
            } catch (IOException e) {
                LOG.error("Error escribiendo documento", e);
                e.printStackTrace();
            }
            return signedDocument != null;
        }
    }

    public void setArgs(String[] args) {
        List<String> arguments = new ArrayList<String>();
        for (String params : args) {
            if (!params.startsWith("-"))
                arguments.add(params);
        }
        if (arguments.size() > 1)
            documenttosign = Paths.get(arguments.get(0)).toAbsolutePath().toString();
        if (arguments.size() > 2)
            documenttosave = Paths.get(arguments.get(1)).toAbsolutePath().toString();
    }

    public void extendDocument() {
        if (fileName == null)fileName = getDocumentToSign();
        if (fileName != null) extendDocument(new FileDocument(fileName), false, null);
    }

    public String getDocumentToSign() {
        return docSelector.getLastFile();
    }

    public String getPathToSave(String extension) {
        if (settings.overwriteSourceFile)
            return getDocumentToSign();
        if (documenttosave != null)
            return documenttosave;
        String pathToSave = showSaveDialog("-firmado", extension);
        return pathToSave;
    }

    public String getPathToSaveExtended(String extension) {
        if (settings.overwriteSourceFile)
            return getDocumentToSign();
        String pathToExtend = showSaveDialog("-sellado", extension);
        return pathToExtend;
    }

    public void displayFunctionality(String functionality) {
        if (functionality.equalsIgnoreCase("sign")) frameTabbedPane.setSelectedIndex(0);
        else if (functionality.equalsIgnoreCase("validator")) frameTabbedPane.setSelectedIndex(1);
    }

    public void updateConfig() {
        if (settings.showLogs) showLogs(frameTabbedPane);
        else hideLogs(frameTabbedPane);
    }

    public void close() {
        mainFrame.dispatchEvent(new WindowEvent(mainFrame, WindowEvent.WINDOW_CLOSING));
    }

    private String addSuffixToFilePath(String name, String suffix) {
        String dotExtension = "";
        String newname = name + suffix;
        int lastDot = name.lastIndexOf(".");
        if (lastDot >= 0) {
            dotExtension = name.substring(lastDot);
            newname = name.substring(0, name.lastIndexOf(".")) + suffix + dotExtension;
        }
        return newname;
    }


    public boolean doSignDocuments() {
        boolean ok = false;
        fileName = getDocumentToSign();
        toSignDocument = new FileDocument(fileName);
        CardSignInfo card = getPin();
        signDocument(card, !signPanel.getSignatureVisibleCheckBox().isSelected(), true);

        if (signedDocument != null) {
            try {
                fileName = getPathToSave(getExtension());
                if (fileName != null) {
                    signedDocument.save(fileName);
                    showMessage("Documento guardado satisfactoriamente en<br>" + fileName);
                    loadDocument(fileName);
                }
                ok = true;
            } catch (IOException e) {
                LOG.error("Error Firmando documento", e);
                showError(FirmadorUtils.getRootCause(e));
            }
        }
        return ok;
    }

    public String showSaveDialog(String suffix, String extension) {
        gui.nextStep("Obteniendo ruta de guardado");
        String lastDirectory = docSelector.getLastDirectory();
        String lastFile = docSelector.getLastFile();
        String fileName = null;
        FileDialog saveDialog = null;
        saveDialog = new FileDialog(mainFrame, "Guardar documento", FileDialog.SAVE);
        saveDialog.setDirectory(lastDirectory);
        String dotExtension = "";
        int lastDot = lastFile.lastIndexOf(".");
        if (extension != "") {
            suffix = ""; // XMLs could reuse same files, however
            dotExtension = extension;
        } else if (lastDot >= 0) dotExtension = lastFile.substring(lastDot);

        Path path = Paths.get(lastFile);
        lastFile=path.getFileName().toString();
        saveDialog.setFile(lastFile.substring(0, lastFile.lastIndexOf(".")) + suffix + dotExtension);
        //saveDialog.setFilenameFilter(docSelector.getLoadDialog().getFilenameFilter()); // FIXME use filter based on file type containing the signature
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

    public void signDocumentByPath(File file, CardSignInfo card) {
        documenttosign = file.toString();
        loadDocument(documenttosign);
        toSignDocument = new FileDocument(documenttosign);
        signDocument(card, !signPanel.getSignatureVisibleCheckBox().isSelected(), false);
        if (signedDocument != null) {
            fileName = addSuffixToFilePath(documenttosign, "-firmado");
            try {
                signedDocument.save(fileName);
            } catch (IOException e) {
                LOG.error("Error Firmando Multiples documentos", e);
                gui.showError(FirmadorUtils.getRootCause(e));
            }
        }
    }

    public void signMultipleDocuments(File[] files) {
        worker = new ExecutorWorkerMultipleFiles(this, files);
        SwingUtilities.invokeLater((Runnable) worker);
        Thread.yield();
    }

    public void validateDocumentByPath(File file) {
        validateDocument(file.toString());
    }

    public void validateMultipleDocuments(File[] files) {
        worker = new ExecutorWorkerMultipleFilesValidator(this, files);
        SwingUtilities.invokeLater((Runnable) worker);
        Thread.yield();
    }

    public void clearElements() {
        docSelector.fileField.setText("");
        if (doc != null) {
            try {
                doc.close();
            } catch (IOException e) {
                LOG.error("Error cerrando archivo", e);
                e.printStackTrace();
            }
        }
        // Muchas cosas mas acá
    }

    public String getExtension() {
        String extension = "";
        if (toSignDocument != null) {
            MimeType mimeType = toSignDocument.getMimeType();
            if (mimeType == MimeTypeEnum.XML) extension = ".xml";
            else if (mimeType != MimeTypeEnum.PDF && !(mimeType == MimeTypeEnum.ODG || mimeType == MimeTypeEnum.ODP || mimeType == MimeTypeEnum.ODS || mimeType == MimeTypeEnum.ODT)) extension = ".p7s"; // p7s detached, p7m enveloping
        }
        return extension;
    }

    public SwingMainWindowFrame getMainFrame() {
        return mainFrame;
    }

    public void setMainFrame(SwingMainWindowFrame mainWindowFrame) {
        mainFrame = mainWindowFrame;
    }

    protected void showLogs(JTabbedPane frameTabbedPane) {
        frameTabbedPane.addTab("Bitácoras", loggingPane);
        frameTabbedPane.setToolTipTextAt(tabPosition + 2, "<html>En esta pestaña se muestra las bitácoras de ejecución<br> de este programa.</html>");
    }

    protected void hideLogs(JTabbedPane frameTabbedPane) {
        frameTabbedPane.remove(loggingPane);
    }

    public ByteArrayOutputStream extendDocument(DSSDocument toExtendDocument, boolean asbytes, String fileName ) {
            if (toExtendDocument == null) return null;
            DSSDocument extendedDocument = null;
            ByteArrayOutputStream outdoc = null;
            MimeType mimeType = toExtendDocument.getMimeType();
            if (mimeType == MimeTypeEnum.PDF) {
                FirmadorPAdES firmador = new FirmadorPAdES(gui);
                extendedDocument = firmador.extend(toExtendDocument);
            } else if (mimeType == MimeTypeEnum.ODG || mimeType == MimeTypeEnum.ODP || mimeType == MimeTypeEnum.ODS || mimeType == MimeTypeEnum.ODT) {
                FirmadorOpenDocument firmador = new FirmadorOpenDocument(gui);
                extendedDocument = firmador.extend(toExtendDocument);
            } else if (mimeType == MimeTypeEnum.XML) {
                FirmadorXAdES firmador = new FirmadorXAdES(gui);
                extendedDocument = firmador.extend(toExtendDocument);
            } else {
                FirmadorCAdES firmador = new FirmadorCAdES(gui);
                extendedDocument = firmador.extend(toExtendDocument);
            }
            if (extendedDocument != null) {
                if (asbytes) {
                    outdoc = new ByteArrayOutputStream();
                    try {
                        extendedDocument.writeTo(outdoc);
                    } catch (IOException e) {
                        LOG.error("Error extendiendo documento", e);
                        showError(FirmadorUtils.getRootCause(e));
                    }
                } else {
                    if (fileName == null) fileName = gui.getPathToSaveExtended("");
                    else {
                        try {
                            extendedDocument.save(fileName);
                            showMessage("Documento guardado satisfactoriamente en<br>" + fileName);
                            gui.loadDocument(fileName);
                        } catch (IOException e) {
                            LOG.error("Error guardando extendido", e);
                            showError(FirmadorUtils.getRootCause(e));
                        }
                    }
                }
            }
            return outdoc;
        }

    public Boolean validateDocument(Validator validator){
        Boolean ok = false;
        if (validator.isSigned()) {
            validatePanel.extendButton.setEnabled(true);
            gui.displayFunctionality("validator");
            ok = true;
        } else {
            validatePanel.reportLabel.setText("");
            validatePanel.extendButton.setEnabled(false);
            gui.displayFunctionality("sign");
            return false;
        }
        try {
            Report report = new Report(validator.getReports());
            validatePanel.reportLabel.setText(report.getReport()); // FIXME don't overwrite previous report
        } catch (Exception e) {
            LOG.error("Validando documento", e);
            e.printStackTrace();
            validatePanel.reportLabel.setText("Error al generar reporte.<br>" +
                "Agradeceríamos que informara sobre este inconveniente<br>" +
                "a los desarrolladores de la aplicación para repararlo.");
            ok = false;
        }
        return ok;
    }

    public void validateDocument(String fileName) {
        Validator validator = null;
        try {
            validator = new Validator(fileName);
            if (validator != null) validateDocument(validator);
        } catch (UnsupportedOperationException e) {
            LOG.error("Error documento inválido " + fileName, e);
            showError(e);
        } catch (Exception e) {
            LOG.error("Error validando documento desde archivo " + fileName, e);
            e.printStackTrace();
            validatePanel.reportLabel.setText("Error al validar documento.<br>" +
                "Agradeceríamos que informara sobre este inconveniente<br>" +
                "a los desarrolladores de la aplicación para repararlo.");
            validatePanel.reportLabel.setText("");
            validatePanel.extendButton.setEnabled(false);
            gui.displayFunctionality("sign");
        }
    }

    public void loadDocumentPDF(PDDocument doc) throws IOException {
        signPanel.getSignButton().setEnabled(true);
        signPanel.docHideButtons();
        int pages = doc.getNumberOfPages();
        renderer = signPanel.getRender(doc);
        if (pages > 0) {
            SpinnerNumberModel model = ((SpinnerNumberModel)signPanel.getPageSpinner().getModel());
            model.setMinimum(1);
            model.setMaximum(pages);
            if (settings.pageNumber <= pages && settings.pageNumber > 0) signPanel.getPageSpinner().setValue(settings.pageNumber);
            else signPanel.getPageSpinner().setValue(1);
            signPanel.paintPDFViewer();
        }
        signPanel.showSignButtons();
    }

    public void loadDocument(MimeType mimeType, PDDocument doc) {
        signPanel.setDoc(doc);
        signPanel.getSignButton().setEnabled(true);
        try {
            signPanel.docHideButtons();
            if (mimeType == MimeTypeEnum.PDF) loadDocumentPDF(doc);
            else signPanel.shownonPDFButtons();
            //else if (mimeType == MimeTypeEnum.XML) { /* Nothing for now */ }
            //else if (mimeType == MimeTypeEnum.ODG || mimeType == MimeTypeEnum.ODP || mimeType == MimeTypeEnum.ODS || mimeType == MimeTypeEnum.ODT) { /* Nothing for now */ }
            mainFrame.pack();
            mainFrame.setMinimumSize(mainFrame.getSize());
        } catch (Exception e) {
            LOG.error("Error cargando Documento con mimeType", e);
            gui.showError(FirmadorUtils.getRootCause(e));
        }
    }

    protected void signDocument(CardSignInfo card, Boolean visibleSignature) {
        signedDocument = null;
        MimeType mimeType = toSignDocument.getMimeType();
        if (mimeType == MimeTypeEnum.PDF) {
            FirmadorPAdES firmador = new FirmadorPAdES(gui);
            firmador.setVisibleSignature(visibleSignature);
            firmador.addVisibleSignature((int)signPanel.getPageSpinner().getValue(), signPanel.calculateSignatureRectangle());
            signedDocument = firmador.sign(toSignDocument, card, signPanel.getReasonField().getText(), signPanel.getLocationField().getText(),
                signPanel.getContactInfoField().getText(), System.getProperty("jnlp.signatureImage"), Boolean.getBoolean("jnlp.hideSignatureAdvice"));
        } else if (mimeType == MimeTypeEnum.ODG || mimeType == MimeTypeEnum.ODP || mimeType == MimeTypeEnum.ODS || mimeType == MimeTypeEnum.ODT) {
            FirmadorOpenDocument firmador = new FirmadorOpenDocument(gui);
            signedDocument = firmador.sign(toSignDocument, card);
        } else if (mimeType == MimeTypeEnum.XML || signPanel.getAdESFormatButtonGroup().getSelection().getActionCommand().equals("XAdES")) {
            FirmadorXAdES firmador = new FirmadorXAdES(gui);
            signedDocument = firmador.sign(toSignDocument, card);
        } else {
            FirmadorCAdES firmador = new FirmadorCAdES(gui);
            signedDocument = firmador.sign(toSignDocument, card);
        }
    }

    protected void signDocument(CardSignInfo card, Boolean visibleSignature, Boolean destroyPin) {
        if (card.isValid()) {
            gui.nextStep("Inicio del proceso de firmado");
            signDocument(card,  visibleSignature);
            if(destroyPin) {
                gui.nextStep("Destruyendo el pin");
                card.destroyPin();
            }
        }
    }

    public void showMessage(String message) {
        LOG.info("Mensaje de información mostrado: " + message);
        JOptionPane.showMessageDialog(null, new CopyableJLabel(message), "Mensaje de Firmador", JOptionPane.INFORMATION_MESSAGE);
    }

    public void showError(Throwable error) {
        showError(error, false);
    }

    public void showError(Throwable error, boolean closed) {
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
                message = "No se ha encontrado ninguna dispositivo de firma.<br>" +
                    "Asegúrese de que la tarjeta y el lector están conectados de forma correcta<br>" +
                    "y de que los controladores están instalados y ha reiniciado el sistema tras su instalación.";
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
                }
                break;
            default:
                message = "Error: " + className + "<br>" +
                    "Detalle: " + message + "<br>" +
                    "Agradecemos que comunique este mensaje de error a los autores del programa<br>" +
                    "para detallar mejor el posible motivo de este error en próximas versiones.";
                break;
        }
        LOG.error("Mensaje de error mostrado: " + message);
        error.printStackTrace();
        JOptionPane.showMessageDialog(null, new CopyableJLabel(message), "Mensaje de Firmador", messageType);
        if (closed) if (messageType == JOptionPane.ERROR_MESSAGE) System.exit(0);
    }

    public CardSignInfo getPin() {
        RequestPinWindow requestPinWindow = new RequestPinWindow();
        int action = requestPinWindow.showandwait();
        if (action == 0) return requestPinWindow.getCardInfo();
        else return null;
    }

    public void setPluginManager(PluginManager pluginManager) {
        pluginManager.startLogging();
        mainFrame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent arg0) {
                pluginManager.stop();
            }
        });
    }

    public void nextStep(String msg) {
        if (worker != null) worker.nextStep(msg);
    }

}
