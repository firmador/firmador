package cr.libre.firmador.previewers;

import java.awt.image.BufferedImage;
import java.io.IOException;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;

import cr.libre.firmador.Settings;
import cr.libre.firmador.SettingsManager;

public class NonPreviewer implements PreviewerInterface {
    private PDDocument document = null;
    private PDFRenderer renderer = null;
    private Settings settings;

    NonPreviewer() {
        settings = SettingsManager.getInstance().getAndCreateSettings();
    }
    @Override
    public void loadDocument(String filename) throws Throwable {
        document = PDDocument.load(this.getClass().getClassLoader().getResourceAsStream("nonPreview.pdf"));
    }

    @Override
    public void loadDocument(byte[] data) throws Throwable {
        document = PDDocument.load(this.getClass().getClassLoader().getResourceAsStream("nonPreview.pdf"));
    }

    @Override
    public PDDocument getDocument() {
        return document;
    }

    @Override
    public int getNumberOfPages() {
        if (document != null)
            return document.getNumberOfPages();
        return 0;
    }

    @Override
    public PDFRenderer getRender() {
        if (renderer == null)
            renderer = new PDFRenderer(document);
        return renderer;
    }

    @Override
    public BufferedImage getPageImage(int page) throws Throwable {

        return getRender().renderImage(page, settings.pDFImgScaleFactor);
    }

    @Override
    public boolean showSignLabelPreview() {
        return false;
    }

    @Override
    public void closePreview() {
        if (document != null) {
            try {
                document.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    @Override
    public boolean canConfigurePreview() {
        return true;
    }
}
