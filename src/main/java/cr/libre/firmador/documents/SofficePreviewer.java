package cr.libre.firmador.documents;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.commons.io.FilenameUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;

import cr.libre.firmador.Settings;
import cr.libre.firmador.SettingsManager;

public class SofficePreviewer implements PreviewerInterface {
    private PDDocument document = null;
    private PDFRenderer renderer = null;
    private Settings settings;

    SofficePreviewer() {
        settings = SettingsManager.getInstance().getAndCreateSettings();
    }

    public void loadDocument(String fileName) throws Throwable {

        File importfile = new File(fileName);

        System.out.println();

        String separator = FileSystems.getDefault().getSeparator();
        String guestFilename = FilenameUtils.removeExtension(importfile.getName()) + ".pdf";
        String tmpdir = Files.createTempDirectory("firmadorlibre").toFile().getAbsolutePath();
        String command = String.format("%s --headless  --convert-to pdf:writer_pdf_Export --outdir %s %s",
                settings.sofficePath, tmpdir, importfile.toURI().normalize());
        Process theProcess = Runtime.getRuntime().exec(command);

        theProcess.getErrorStream().transferTo(System.out);
        String completepath = tmpdir + separator + guestFilename;
        File path = new File(completepath);
        if (path.exists())
            document = PDDocument.load(path);
        renderer = null;
    }

    @Override
    public void loadDocument(byte[] data) throws Throwable {
        document = PDDocument.load(data);
        renderer = null;
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

    public boolean showSignLabelPreview() {
        return true;
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
}