package cr.libre.firmador.previewers;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cr.libre.firmador.documents.Document;
import cr.libre.firmador.gui.GUIInterface;

public class PreviewScheduler extends Thread {
    public static int MAX_FILES_PROCESS = 5;
    final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private Semaphore waitforfiles = new Semaphore(1);
    private Semaphore maxoffilesperprocess = new Semaphore(MAX_FILES_PROCESS);
    private List<Document> files;
    private boolean stop = false;

    private GUIInterface gui;

    public PreviewScheduler() {
        this.files = new ArrayList<>();
    }

    public PreviewScheduler(GUIInterface gui) {
        this.files = new ArrayList<>();
        this.gui = gui;
    }

    public void run() {
        try {
            this.waitforfiles.acquire(); // first time acquire and don't lock

            while(!this.stop) {
                if(this.files.isEmpty())
                    this.waitforfiles.acquire(); // lock thread until the list is not empty

                while(!this.files.isEmpty()) {
                    Document document = this.files.remove(0);
                    this.maxoffilesperprocess.acquire();
                    PreviewWorker task = new PreviewWorker(document, this);
                    task.execute();
                }
            }

        } catch (InterruptedException e) {
            this.stop = true;
            e.printStackTrace();
        }
    }

    public void addDocument(Document document) {
        this.files.add(document);
        this.waitforfiles.release();
    }

    public void done() {
        maxoffilesperprocess.release();
        int avalilable = maxoffilesperprocess.availablePermits();
        if (avalilable == MAX_FILES_PROCESS) {
            gui.previewAllDone();
        }

    }

    public GUIInterface getGui() {
        return this.gui;
    }

    public List<Document> getFiles(){
        return this.files;
    }

    public boolean getStop(){
        return this.stop;
    }

    public void setWaitforfiles(Semaphore waitforfiles){
        this.waitforfiles = waitforfiles;
    }

    public void setMaxoffilesperprocess(Semaphore maxoffilesperprocess){
        this.maxoffilesperprocess = maxoffilesperprocess;
    }
}
