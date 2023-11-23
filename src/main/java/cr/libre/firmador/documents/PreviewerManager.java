package cr.libre.firmador.documents;

public class PreviewerManager {
    public static PreviewerInterface getPreviewManager(SupportedMimeTypeEnum mimetype) {
        PreviewerInterface previewer = new NonPreviewer();
        if(mimetype.isPDF()) {
            previewer = new PDFPreviewer();
        }
        if (mimetype.isOpenxmlformats()) {
            previewer = new SofficePreviewer();
        }
        return previewer;
    }
}
