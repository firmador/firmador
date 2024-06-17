package cr.libre.firmador.previewers;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.invoke.MethodHandles;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Collectors;

import org.apache.commons.io.FilenameUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cr.libre.firmador.Settings;
import cr.libre.firmador.SettingsManager;
import cr.libre.firmador.documents.MimeTypeDetector;
import cr.libre.firmador.documents.SupportedMimeTypeEnum;

public class SofficePreviewer implements PreviewerInterface {
    final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private PDDocument document = null;
    private PDFRenderer renderer = null;
    private Settings settings;

    SofficePreviewer() {
        settings = SettingsManager.getInstance().getAndCreateSettings();
    }

    public void loadDocument(String fileName) throws Throwable {

        File importfile = new File(fileName);

        SupportedMimeTypeEnum mimetype = MimeTypeDetector.detect(fileName);

        String conversorsource = "pdf:writer_pdf_Export";
        if (mimetype == SupportedMimeTypeEnum.XLSX || mimetype == SupportedMimeTypeEnum.ODS) {
            conversorsource = "pdf:calc_pdf_Export";
        }
        if (mimetype == SupportedMimeTypeEnum.ODP || mimetype == SupportedMimeTypeEnum.PPTX) {
            conversorsource = "pdf:draw_pdf_Export";
        }

        String separator = FileSystems.getDefault().getSeparator();
        String guestFilename = FilenameUtils.removeExtension(importfile.getName()) + ".pdf";
        String tmpdir = Files.createTempDirectory("firmadorlibre").toFile().getAbsolutePath();
        String[] command = new String[]{String.format("%s --headless  --convert-to %s --outdir %s %s", settings.getSofficePath(),
                conversorsource, tmpdir, importfile.toURI().normalize())};
        Process theProcess = Runtime.getRuntime().exec(command);

        InputStream inputStream = theProcess.getErrorStream();

        String text = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8)).lines()
                .collect(Collectors.joining("\n"));

        LOG.info(text); // .transferTo(System.out);

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
        File path = new File(settings.getSofficePath());

        return path.exists();
    }
}
