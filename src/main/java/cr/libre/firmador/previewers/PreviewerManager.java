package cr.libre.firmador.previewers;

import cr.libre.firmador.documents.SupportedMimeTypeEnum;

public class PreviewerManager {
    public static PreviewerInterface getPreviewManager(SupportedMimeTypeEnum mimetype) {
        PreviewerInterface previewer = new NonPreviewer();
        if(mimetype.isPDF()) {
            previewer = new PDFPreviewer();
        }
        if (mimetype.isOpenxmlformats()) {
            previewer = new SofficePreviewer();
        }
        if (mimetype.isOpenDocument()) {
            previewer = new SofficePreviewer();
        }
        return previewer;
    }
}
