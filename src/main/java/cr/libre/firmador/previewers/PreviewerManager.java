package cr.libre.firmador.previewers;

import cr.libre.firmador.documents.SupportedMimeTypeEnum;

public class PreviewerManager {
    public static PreviewerInterface getPreviewManager(SupportedMimeTypeEnum mimetype) {
        PreviewerInterface previewer = null;
        if(mimetype.isPDF()) {
            previewer = new PDFPreviewer();
        }
        if (mimetype.isOpenxmlformats()) {
            previewer = new SofficePreviewer();
        }
        if (mimetype.isOpenDocument()) {
            previewer = new SofficePreviewer();
        }
        // check if previewer can load, else fallback to nonpreviewer
        if (previewer != null && !previewer.canConfigurePreview()) {
            previewer = null;
        }

        if (previewer == null) {
            previewer = new NonPreviewer();
        }
        return previewer;
    }
}
