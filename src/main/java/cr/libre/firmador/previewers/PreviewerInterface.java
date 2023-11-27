package cr.libre.firmador.previewers;

import java.awt.image.BufferedImage;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;

public interface PreviewerInterface {

    void loadDocument(String filename) throws Throwable;

    void loadDocument(byte[] data) throws Throwable;

    PDDocument getDocument();

    BufferedImage getPageImage(int page) throws Throwable;

    int getNumberOfPages();

    PDFRenderer getRender();

    boolean showSignLabelPreview();

    void closePreview();
}

