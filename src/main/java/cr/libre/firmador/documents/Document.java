package cr.libre.firmador.documents;

import java.io.File;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cr.libre.firmador.Settings;
import cr.libre.firmador.SettingsManager;
import cr.libre.firmador.cards.CardSignInfo;
import cr.libre.firmador.gui.GUIInterface;
import cr.libre.firmador.signers.DocumentSigner;
import cr.libre.firmador.signers.DocumentSignerDetector;
import cr.libre.firmador.validators.Validator;
import cr.libre.firmador.validators.ValidatorFactory;
import eu.europa.esig.dss.model.DSSDocument;
import eu.europa.esig.dss.model.FileDocument;

public class Document {
    final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private List<DocumentChangeListener> listeners = new ArrayList<DocumentChangeListener>();
    private SupportedMimeTypeEnum mimeType;
    private String pathname;
    private String name;
    private DSSDocument document = null;
    private DSSDocument signedDocument = null;
    private Validator validator;
    private PreviewerInterface preview;
    private Settings settings;
    private DocumentSigner signer;
    private GUIInterface gui;
    private byte[] data;

    private String pathToSave = null;
    private String pathToSaveName = null;
    private boolean isvalid = false;
    private boolean documentIsValidate = false;
    private boolean hasPreviewLoaded = false;
    private boolean isReady = false;
    private String report;


    public Document(GUIInterface gui, String pathname) {
        this.pathname = pathname;
        this.gui = gui;
        File file = new File(pathname);
        name = file.getName();
        mimeType = MimeTypeDetector.detect(pathname);
        validator = ValidatorFactory.getValidator(pathname);
        preview = PreviewerManager.getPreviewManager(mimeType);
        settings = SettingsManager.getInstance().getAndCreateSettings();
        signer = DocumentSignerDetector.getDocumentSigner(gui, settings, mimeType);
    }

    public Document(GUIInterface gui, byte[] data, String name) {
        this.pathname = name;
        this.data = data;
        this.gui = gui;
        mimeType = MimeTypeDetector.detect(data, name);
        validator = ValidatorFactory.getValidator(data, name);
        preview = PreviewerManager.getPreviewManager(mimeType);
        settings = SettingsManager.getInstance().getAndCreateSettings();
        signer = DocumentSignerDetector.getDocumentSigner(gui, settings, mimeType);

    }

    public void setSettings(Settings settings) {
        this.settings = settings;
        // destroy old signer first
        signer = DocumentSignerDetector.getDocumentSigner(gui, settings, mimeType);
    }

    public Settings getSettings() {
        return settings;
    }

    public boolean validate() throws Throwable {

        if (validator != null && !documentIsValidate) {
            isvalid = false;
            document = validator.loadDocumentPath(pathname);
            isvalid = validator.isSigned();
            report = validator.getStringReport();
            this.validateDone();
        }
        return isvalid;
    }

    public void sign(CardSignInfo card) {
        if (document == null) {
            document = new FileDocument(this.pathname);
        }
        signedDocument = signer.sign(this, card);
        if (settings.extendDocument) {
            this.extend();
        }
    }

    public void extend() {
        signedDocument = signer.extend(signedDocument);
    }

    public void setPrincipal() throws Throwable {
        // pone el documento como principal en la GUI
        validate();
        preview.loadDocument(pathname);
    }

    public String getPathName() {
        return this.pathname;
    }

    public String getName() {
        return name;
    }

    public SupportedMimeTypeEnum getMimeType() {
        return mimeType;
    }

    public String getExtension() {
        String extension = "";
        if (mimeType.isXML())
            extension = ".xml";
        else if (mimeType.isPDF() || mimeType.isOpenDocument() || mimeType.isOpenxmlformats()) {
            extension = "." + mimeType.getExtension().toLowerCase();
        } else {
            extension = ".p7s";
        }

        return extension;
    }

    public String getPathToSave() {
        if (pathToSave == null) {
            String extension = getExtension();
            String suffix = "-firmado";
            pathToSave = pathname.substring(0, pathname.lastIndexOf(".")) + suffix + extension;
        }

        return pathToSave;
    }

    public String getPathToSaveName() {
        if (pathToSaveName == null) {
            String extension = getExtension();
            String suffix = "-firmado";
            pathToSaveName = name.substring(0, name.lastIndexOf(".")) + suffix + extension;
        }
        return pathToSaveName;
    }

    public String getReport() {
        return report;
    }

    public DSSDocument getDSSDocument() {
        return document;
    }

    public void setDSSDocument(DSSDocument document) {
        this.document = document;
        documentIsValidate = false;
    }

    public void registerListener(DocumentChangeListener listener) {
        listeners.add(listener);
    }

    public void previewDone() {
        hasPreviewLoaded = true;
        checkIsReady();
        for (DocumentChangeListener hl : listeners)
            hl.previewDone(this);
    }

    public void validateDone() {
        documentIsValidate = true;
        checkIsReady();
        for (DocumentChangeListener hl : listeners)
            hl.validateDone(this);

    };

    public void signDone() {
        for (DocumentChangeListener hl : listeners)
            hl.signDone(this);
    };

    public void extendsDone() {
        for (DocumentChangeListener hl : listeners)
            hl.extendsDone(this);
    };

    public Validator getValidator() {
        return validator;
    }

    public boolean isValid() {
        return this.isvalid;
    }

    public PreviewerInterface getPreviewManager() {
        return preview;
    }

    public void loadPreview() throws Throwable {
        preview.loadDocument(pathname);
        preview.getRender();
        this.previewDone();
    }

    public int amountOfSignatures() {
        return validator.amountOfSignatures();

    }

    public DSSDocument getSignedDocument() {
        return signedDocument;
    }

    public void setSignedDocument(DSSDocument signedDocument) {
        this.signedDocument = signedDocument;
    }

    public boolean getIsReady() {
        return isReady;
    }
    private void checkIsReady() {
        if (hasPreviewLoaded && documentIsValidate) {
            isReady = true;
        }
    }
}
