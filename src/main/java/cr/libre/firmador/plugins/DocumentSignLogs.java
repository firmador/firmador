package cr.libre.firmador.plugins;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.concurrent.Semaphore;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cr.libre.firmador.Settings;
import cr.libre.firmador.SettingsManager;
import cr.libre.firmador.cards.CardSignInfo;
import cr.libre.firmador.documents.Document;
import cr.libre.firmador.documents.DocumentChangeListener;

public class DocumentSignLogs implements Plugin, DocumentChangeListener, DocumentInteractInterface {
    private static Semaphore blockfile = new Semaphore(1);
    private static final Logger LOGGER = LoggerFactory.getLogger("signLog");

    @Override
    public void previewDone(Document document) {
        // TODO Auto-generated method stub

    }

    @Override
    public void validateDone(Document document) {
        // TODO Auto-generated method stub

    }

    @Override
    public void signDone(Document document) {

        SettingsManager sm = SettingsManager.getInstance();
        Settings settings = document.getSettings();

        CardSignInfo card = document.getUsedCard();

        SimpleDateFormat date = new SimpleDateFormat(settings.getDateFormat());
        date.setTimeZone(TimeZone.getTimeZone("America/Costa_Rica"));
        String msg = date.format(new Date()) + "," + document.getPathName() + "," + document.getPathToSave() + ","
                + card.getDisplayInfo() + "\n";
        Path homepath;
        try {
            blockfile.acquire();
            homepath = sm.getConfigDir();
            Path logname = homepath.getFileSystem().getPath(homepath.toString(), "signlog.csv");
            FileUtils.writeStringToFile(logname.toFile(), msg, StandardCharsets.UTF_8, true);
           
        } catch (IOException | InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            blockfile.release();
        }

    }

    @Override
    public void extendsDone(Document document) {
        // TODO Auto-generated method stub

    }

    @Override
    public void clearDone() {
        // TODO Auto-generated method stub

    }

    @Override
    public void start() {
        // TODO Auto-generated method stub

    }

    @Override
    public void startLogging() {


    }

    @Override
    public void stop() {
        // TODO Auto-generated method stub

    }

    @Override
    public boolean getIsRunnable() {
        return false;
    }

    @Override
    public boolean interactWithDocuments() {
        return true;
    }

    @Override
    public void registerDocument(Document document) {
        document.registerListener(this);

    }

    @Override
    public void unregisterDocument(Document document) {
        // TODO Auto-generated method stub

    }

    @Override
    public void previewAllDone() {
        // TODO Auto-generated method stub

    }

    @Override
    public void validateAllDone() {
        // TODO Auto-generated method stub

    }

    @Override
    public void signAllDone() {
        // TODO Auto-generated method stub

    }

}
