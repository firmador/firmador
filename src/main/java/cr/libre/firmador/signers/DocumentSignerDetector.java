package cr.libre.firmador.signers;

import cr.libre.firmador.Settings;
import cr.libre.firmador.documents.SupportedMimeTypeEnum;
import cr.libre.firmador.gui.GUIInterface;


public class DocumentSignerDetector {
    public static DocumentSigner getDocumentSigner(GUIInterface gui, Settings settings,
            SupportedMimeTypeEnum mimeType) {
        DocumentSigner signer;
        if (mimeType.isPDF()) {
            signer = new FirmadorPAdES(gui);
        } else if (mimeType.isOpenDocument()) {
            signer = new FirmadorOpenDocument(gui);
        } else if (mimeType.isOpenxmlformats()) {
            signer = new FirmadorOpenXmlFormat(gui);
        } else if (mimeType.isXML()) {
            signer = new FirmadorXAdES(gui);
        } else {
            // signer = new FirmadorASiC(gui);
            signer = new FirmadorCAdES(gui);
        }
        return signer;
    }
}
