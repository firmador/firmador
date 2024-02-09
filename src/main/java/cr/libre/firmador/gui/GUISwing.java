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
import java.util.List;

import javax.swing.GroupLayout;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import com.apple.eawt.Application;
import eu.europa.esig.dss.enumerations.MimeType;
import eu.europa.esig.dss.enumerations.MimeTypeEnum;
import eu.europa.esig.dss.model.DSSDocument;
import eu.europa.esig.dss.model.FileDocument;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cr.libre.firmador.cards.CardSignInfo;
import cr.libre.firmador.documents.Document;
import cr.libre.firmador.documents.DocumentChangeListener;
import cr.libre.firmador.documents.MimeTypeDetector;
import cr.libre.firmador.documents.SupportedMimeTypeEnum;
import cr.libre.firmador.ConfigListener;
import cr.libre.firmador.MessageUtils;
import cr.libre.firmador.Settings;
import cr.libre.firmador.SettingsManager;
import cr.libre.firmador.validators.ValidateScheduler;
import cr.libre.firmador.gui.swing.AboutLayout;
import cr.libre.firmador.gui.swing.ConfigPanel;
import cr.libre.firmador.gui.swing.CopyableJLabel;
import cr.libre.firmador.gui.swing.DocumentSelectionGroupLayout;
import cr.libre.firmador.gui.swing.ListDocumentTablePanel;
import cr.libre.firmador.gui.swing.LoadProgressDialogWorker;
import cr.libre.firmador.gui.swing.LogHandler;
import cr.libre.firmador.gui.swing.LoggingFrame;
import cr.libre.firmador.gui.swing.RemoteDocInformation;
import cr.libre.firmador.gui.swing.RemoteHttpWorker;
import cr.libre.firmador.gui.swing.RequestPinWindow;
import cr.libre.firmador.gui.swing.SignPanel;
import cr.libre.firmador.gui.swing.SignProgressDialogWorker;
import cr.libre.firmador.gui.swing.SwingMainWindowFrame;
import cr.libre.firmador.gui.swing.ValidatePanel;
import cr.libre.firmador.plugins.PluginManager;
import cr.libre.firmador.previewers.PreviewScheduler;
import cr.libre.firmador.previewers.PreviewerInterface;
import cr.libre.firmador.signers.FirmadorCAdES;
import cr.libre.firmador.signers.FirmadorOpenDocument;
import cr.libre.firmador.signers.FirmadorPAdES;
import cr.libre.firmador.signers.FirmadorUtils;
import cr.libre.firmador.signers.FirmadorXAdES;
import cr.libre.firmador.signers.SignerScheduler;

public class GUISwing implements GUIInterface, ConfigListener, DocumentChangeListener {
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
    private JScrollPane loggingPane;
    private Image image = new ImageIcon(this.getClass().getClassLoader().getResource("firmador.png")).getImage();
    private int tabPosition;
    private ListDocumentTablePanel listdocumentpanel;
    private ValidateScheduler validatescheduler;
    private SignerScheduler signerScheduler;
    private PreviewScheduler previewScheduler;
    private SignProgressDialogWorker progressDialogWorker;
    private Document document;
    private LoadProgressDialogWorker loadDialogWorker;
    private boolean forcePreview = false;
    private List<String> currentSavedFilePath = new ArrayList<String>();
    private PluginManager pluginManager;

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
            LOG.error(MessageUtils.t("guiswing_error_loading_gui"), e);
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
            LOG.error(MessageUtils.t("guiswing_log_error_headless"));
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
        listdocumentpanel = new ListDocumentTablePanel();
        listdocumentpanel.setGUI(gui);
        JPanel aboutPanel = new JPanel();
        GroupLayout aboutLayout = new AboutLayout(aboutPanel);
        ((AboutLayout) aboutLayout).setInterface(this);

        aboutPanel.setLayout(aboutLayout);
        aboutPanel.setOpaque(false);

        JPanel configPanel = new ConfigPanel();
        configPanel.setOpaque(false);
        frameTabbedPane = new JTabbedPane();
        frameTabbedPane.addTab(MessageUtils.t("guiswing_tab_sign"), signPanel);
        frameTabbedPane.setToolTipTextAt(0, MessageUtils.t("guiswing_tab_sign_tooltip"));
        frameTabbedPane.getAccessibleContext().getAccessibleChild(0).getAccessibleContext()
                .setAccessibleDescription(MessageUtils.t("guiswing_tab_sign_tooltip_accessible"));
        frameTabbedPane.setMnemonicAt(0, '1');

        tabPosition = 1;
        if (!isRemote) {// TODO add setting for toggling validation tab
            frameTabbedPane.addTab(MessageUtils.t("guiswing_tab_validation"),
                    validatePanel.getValidateScrollPane());
            frameTabbedPane.setToolTipTextAt(tabPosition, MessageUtils.t("guiswing_tab_validation_tooltip"));
            frameTabbedPane.getAccessibleContext().getAccessibleChild(tabPosition).getAccessibleContext()
                    .setAccessibleDescription(MessageUtils.t("guiswing_tab_validation_tooltip_accessible"));
            frameTabbedPane.setMnemonicAt(tabPosition, '2');
            tabPosition++;
        }

        frameTabbedPane.add(MessageUtils.t("guiswing_tab_documents"),
                listdocumentpanel.getListDocumentScrollPane());
        frameTabbedPane.setToolTipTextAt(tabPosition, MessageUtils.t("guiswing_tab_documents_tooltip"));
        frameTabbedPane.getAccessibleContext().getAccessibleChild(tabPosition).getAccessibleContext()
                .setAccessibleDescription(MessageUtils.t("guiswing_tab_documents_tooltip_accessible"));
        frameTabbedPane.setMnemonicAt(tabPosition, '3');
        tabPosition++;
        frameTabbedPane.addTab(MessageUtils.t("guiswing_tab_settings"), configPanel);
        frameTabbedPane.setToolTipTextAt(tabPosition, MessageUtils.t("guiswing_tab_settings_tooltip"));
        frameTabbedPane.getAccessibleContext().getAccessibleChild(tabPosition).getAccessibleContext()
                .setAccessibleDescription(MessageUtils.t("guiswing_tab_settings_tooltip_accessible"));
        frameTabbedPane.addTab(MessageUtils.t("guiswing_tab_about"), aboutPanel);
        frameTabbedPane.setMnemonicAt(tabPosition, '4');
        tabPosition++;
        frameTabbedPane.setToolTipTextAt(tabPosition, MessageUtils.t("guiswing_tab_about_tooltip"));
        frameTabbedPane.getAccessibleContext().getAccessibleChild(tabPosition).getAccessibleContext()
                .setAccessibleDescription(MessageUtils.t("guiswing_tab_about_tooltip_accessible"));
        frameTabbedPane.setMnemonicAt(tabPosition, '5');
        tabPosition++;
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

        progressDialogWorker = new SignProgressDialogWorker();
        progressDialogWorker.execute();
        loadDialogWorker = new LoadProgressDialogWorker(gui);
        loadDialogWorker.execute();

        validatescheduler = new ValidateScheduler(gui);
        validatescheduler.start();

        signerScheduler = new SignerScheduler(gui, progressDialogWorker);
        signerScheduler.start();
        previewScheduler = new PreviewScheduler(gui);
        previewScheduler.start();
        if (documenttosign != null) loadDocument(documenttosign);
    }


    public SignPanel getSignPanel() {
        return signPanel;
    }

    public Document loadDocument(String fileName) {
        return loadDocument(fileName, true);
    }

    public Document loadDocument(String fileName, boolean preview) {

        if (preview)
            loadDialogWorker.setVisible(true);
        Document document = new Document(gui, fileName);
        document.registerListener(this);
        this.pluginManager.registerDocument(document);
        listdocumentpanel.addDocument(document);
        validatescheduler.addDocument(document);
        previewScheduler.addDocument(document);
        if (preview) {
            gui.nextStep(MessageUtils.t("guiswing_nextstep_load_doc"));
            setActiveDocument();
        }

        return document;
    }

    public void signMultipleDocuments(File[] files) {
        loadDialogWorker.setVisible(true);
        Document document;
        List<Document> docs = new ArrayList<Document>();
        for (File file : files) {
            document = new Document(gui, file.getAbsolutePath());
            document.registerListener(this);
            docs.add(document);
            this.pluginManager.registerDocument(document);
        }
        listdocumentpanel.addDocuments(docs);
        validatescheduler.addDocuments(docs);
        gui.displayFunctionality("document");
        // setActiveDocument();
        loadDialogWorker.setVisible(false);
    }



    public Settings getCurrentSettings() {
        Settings collectedSettings = new Settings(this.settings);
        collectedSettings.reason = signPanel.getReasonField().getText().trim().replaceAll("\t", " ");
        collectedSettings.place = signPanel.getLocationField().getText().trim().replaceAll("\t", " ");
        collectedSettings.contact = signPanel.getContactInfoField().getText().trim().replaceAll("\t", " ");
        collectedSettings.image=System.getProperty("jnlp.signatureImage");
        collectedSettings.signY = signPanel.getPDFVisibleSignatureY();
        collectedSettings.signX = signPanel.getPDFVisibleSignatureX();
        collectedSettings.pageNumber = (int) signPanel.getPageSpinner().getValue();
        if (collectedSettings.image == null) collectedSettings.image=this.settings.getImage();
        collectedSettings.hideSignatureAdvice=Boolean.getBoolean("jnlp.hideSignatureAdvice");
        collectedSettings.isVisibleSignature = !signPanel.getSignatureVisibleCheckBox().isSelected();
        collectedSettings.signASiC = signPanel.isASiC();
        collectedSettings.forceCades = signPanel.isCAdES();

        return collectedSettings;
    }
    public boolean signDocuments() {
        List<Document> docToSign = listdocumentpanel.getSelectedDocuments();
        signerScheduler.addDocuments(docToSign);
        setActiveDocument();
        return true;
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
        else if (functionality.equalsIgnoreCase("document"))
            frameTabbedPane.setSelectedIndex(2);
    }

    public void updateConfig() {
        if (settings.showLogs) showLogs(frameTabbedPane);
        else hideLogs(frameTabbedPane);
    }

    public void close() {
        mainFrame.dispatchEvent(new WindowEvent(mainFrame, WindowEvent.WINDOW_CLOSING));
    }

    public String showSaveDialog(String suffix, String extension) {
        gui.nextStep(MessageUtils.t("guiswing_getting_save_path"));
        String lastDirectory = docSelector.getLastDirectory();
        String lastFile = docSelector.getLastFile();
        String fileName = null;
        FileDialog saveDialog = null;
        saveDialog = new FileDialog(mainFrame, MessageUtils.t("guiswing_save_document"), FileDialog.SAVE);
        saveDialog.setDirectory(lastDirectory);
        String dotExtension = "";
        int lastDot = lastFile.lastIndexOf(".");
        if (extension != "") {
            suffix = ""; // XMLs could reuse same files, however
            dotExtension = extension;
        } else if (lastDot >= 0) dotExtension = lastFile.substring(lastDot);

        Path path = Paths.get(lastFile);
        lastFile=path.getFileName().toString();
        String savestrinfilename = lastFile.substring(0, lastFile.lastIndexOf(".")) + suffix + dotExtension;
        saveDialog.setFile(savestrinfilename);
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

    public String showSaveDialog(String filepath, String suffix, String extension) {
        gui.nextStep(MessageUtils.t("guiswing_nextstep_save"));
        String lastDirectory = docSelector.getLastDirectory();
        String lastFile = filepath;
        String fileName = null;
        FileDialog saveDialog = null;
        saveDialog = new FileDialog(mainFrame, MessageUtils.t("guiswing_dialog_document_save"),
                FileDialog.SAVE);
        saveDialog.setDirectory(lastDirectory);
        String dotExtension = "";
        int lastDot = lastFile.lastIndexOf(".");
        if (extension.isEmpty()) {
            dotExtension = extension;
        } else if (lastDot >= 0)
            dotExtension = lastFile.substring(lastDot);

        Path path = Paths.get(lastFile);
        lastFile = path.getFileName().toString();
        String savestringfilename = lastFile.substring(0, lastFile.lastIndexOf(".")) + suffix + dotExtension;
        saveDialog.setFile(savestringfilename);
        // saveDialog.setFilenameFilter(docSelector.getLoadDialog().getFilenameFilter());
        // // FIXME use filter based on file type containing the signature
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



    public void validateDocumentByPath(File file) {
        // TODO: Hacerlo pero solo incluyendo este path
    }

    public void clearElements() {
        docSelector.fileField.setText("");
        if (doc != null) {
            try {
                doc.close();
            } catch (IOException e) {
                LOG.error(MessageUtils.t("guiswing_log_closefile"), e);
                e.printStackTrace();
            }
        }
        // Muchas cosas mas acá
    }

    public String getExtension() {
        String extension = "";
        if (toSignDocument != null) {
            SupportedMimeTypeEnum mimeType = MimeTypeDetector.detect(toSignDocument);
            if (mimeType.isXML())
                extension = ".xml";
            else if (mimeType.isOpenDocument()) {
                extension = "." + mimeType.getExtension().toLowerCase();
            } else if (mimeType.isPDF() || mimeType.isOpenxmlformats()) {
                extension = "." + mimeType.getExtension().toLowerCase();
            } else {
                extension = ".asice";
            }
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
        frameTabbedPane.addTab(MessageUtils.t("guiswing_tab_logs"), loggingPane);
        frameTabbedPane.setToolTipTextAt(tabPosition, MessageUtils.t("guiswing_tab_logs_tooltip"));
        frameTabbedPane.getAccessibleContext().getAccessibleChild(tabPosition).getAccessibleContext()
                .setAccessibleDescription(MessageUtils.t("guiswing_tab_logs_tooltip_accessible"));
        frameTabbedPane.setMnemonicAt(tabPosition, '6');

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



    public void loadPreview(PreviewerInterface preview) {
        signPanel.getSignButton().setEnabled(true);
        signPanel.docHideButtons();
    }

    public void showMessage(String message) {
        LOG.info(MessageUtils.t("guiswing_show_message") + message);
        JOptionPane.showMessageDialog(null, new CopyableJLabel(message),
                MessageUtils.t("guiswing_document_success_joptionpane_title"),
                JOptionPane.INFORMATION_MESSAGE);
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
                message = MessageUtils.t("guiswing_show_error_nosuchmethoderror");
                break;
            case "java.security.ProviderException":
                message = MessageUtils.t("guiswing_show_error_providerexception");
                break;
            case "java.security.NoSuchAlgorithmException":
                message = MessageUtils.t("guiswing_show_error_nosuchalgorithmexception");
                break;
            case "sun.security.pkcs11.wrapper.PKCS11Exception":
                switch (message) {
                case "CKR_GENERAL_ERROR":
                    message = MessageUtils.t("guiswing_show_error_pkcs11_general");
                    break;
                case "CKR_SLOT_ID_INVALID":
                    message = MessageUtils.t("guiswing_show_error_pkcs11_slotinvalid");
                    break;
                case "CKR_PIN_INCORRECT":
                    messageType = JOptionPane.WARNING_MESSAGE;
                    message = MessageUtils.t("guiswing_show_error_pkcs11_pinincorrect");
                    break;
                case "CKR_PIN_LOCKED":
                    message = MessageUtils.t("guiswing_show_error_pkcs11_pinlocked");
                    break;
                default:
                    message = String.format(MessageUtils.t("guiswing_show_error_pkcs11_default"), className,
                            message);

                    break;
                }
                break;
            case "java.io.IOException":
                if (message.contains("asepkcs") || message.contains("libASEP11")) {
                    message = MessageUtils.t("guiswing_show_error_installers");
                }
                break;
            default:
                message = String.format(MessageUtils.t("guiswing_show_error_default"), className, message);
                break;
        }
        LOG.error(MessageUtils.t("guiswing_show_error_logmessage") + message);
        error.printStackTrace();
        JOptionPane.showMessageDialog(null, new CopyableJLabel(message),
                MessageUtils.t("guiswing_show_error_dialog_title"), messageType);
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
        this.pluginManager = pluginManager;
    }

    public void nextStep(String msg) {
        progressDialogWorker.setNote(msg);
    }

    @Override
    public void loadDocument(SupportedMimeTypeEnum mimeType, PDDocument doc) {
        // TODO Auto-generated method stub

    }

    public void signDocument(Document document) {
        signerScheduler.addDocument(document);
    }


    public void previewDone(Document document) {
        if (document.getShowPreview()) {
            setActiveDocument();
            if (document.getIsReady()) {
                loadActiveDocument(document);
            }
            if (forcePreview) {
                forcePreview = false;
                loadDialogWorker.setVisible(false);
                gui.displayFunctionality("sign");
            }
        }
    };

    public void validateDone(Document document) {
        setActiveDocument();
        if (document.getIsReady()) {
            loadActiveDocument(document);
            loadDialogWorker.setVisible(false);
        }
    };

    public void signDone(Document document) {
        signedDocument = document.getSignedDocument();
        fileName = document.getPathToSaveName(); // addSuffixToFilePath(document.getPathName(), "-firmado");
        String pathToSave = document.getPathToSave();
        if (!document.getSignwithErrors() && signedDocument != null) {
            try {
                signedDocument.save(pathToSave);
                currentSavedFilePath.add(pathToSave);
            } catch (IOException e) {
                LOG.error("Error Firmando documentos", e);
                gui.showError(FirmadorUtils.getRootCause(e));
            }
        }
    };

    public void extendsDone(Document document) {
        setActiveDocument();
    };

    public void signAllDocuments() {
        signerScheduler.addDocuments(listdocumentpanel.getAllDocuments());

    }

    public void ScheduleAllPreviewDocuments() {
        loadDialogWorker.setVisible(true);
        for (Document doc : listdocumentpanel.getAllDocuments()) {
            doc.setShowPreview(false);
            previewScheduler.addDocument(doc);
        }
    }

    public void loadActiveDocument(Document document) {
        if (document.isValid()) {
            gui.displayFunctionality("validator");
            validatePanel.reportLabel.setText(document.getReport());
            validatePanel.extendButton.setEnabled(true);
            validatePanel.reportLabel.requestFocusInWindow();
            validatePanel.reportLabel.getAccessibleContext().setAccessibleDescription(document.getPlainReport());
        } else {
            validatePanel.reportLabel.setText("");
            validatePanel.extendButton.setEnabled(false);
            gui.displayFunctionality("sign");
        }
        signPanel.setDocument(document);
        signPanel.setPreview(document.getPreviewManager());
        signPanel.paintPDFViewer();
        docSelector.setLastFile(document.getPathName());
        SupportedMimeTypeEnum mimeType = document.getMimeType();
        signPanel.getSignButton().setEnabled(true);
        try {
            // mainFrame.pack();
            mainFrame.setMinimumSize(mainFrame.getSize());
        } catch (Exception e) {
            LOG.error("Error cargando Documento con mimeType", e);
            gui.showError(FirmadorUtils.getRootCause(e));
        }
    }

    public void setActiveDocument() {
        Document currentActiveDocument = listdocumentpanel.getActiveDocument();
        if (currentActiveDocument != document) {
            document = currentActiveDocument;
            setActiveDocument(document);
        }
    }

    public void setActiveDocument(Document document) {
        docSelector.setLastFile(document.getPathName());
    }

    public ListDocumentTablePanel getListDocumentTablePanel() {
        return listdocumentpanel;
    }

    @Override
    public void validateAllDone() {
        // loadDialogWorker.setVisible(false);
    }

    @Override
    public void signAllDone() {
        String paths = "";
        File pfile;
        for (String path : currentSavedFilePath) {
            pfile = new File(path);
            paths += "<a href=\"" + pfile.toURI().normalize() + "\">" + path + "</a><br>";
        }
        currentSavedFilePath.clear();
        
        if (!paths.isEmpty())
            showMessage(MessageUtils.t("guiswing_dialog_document_success") + paths);

    }

    @Override
    public void doPreview(Document document) {
        forcePreview = true;
        loadDialogWorker.setVisible(true);
        previewScheduler.addDocument(document);

    }

    @Override
    public void clearDone() {
        signPanel.clean();
        validatePanel.clean();
        document = null;
        docSelector.clean();

    }

    @Override
    public void previewAllDone() {
        loadDialogWorker.setVisible(false);

    }
}
