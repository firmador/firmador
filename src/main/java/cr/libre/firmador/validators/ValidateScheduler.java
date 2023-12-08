package cr.libre.firmador.validators;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cr.libre.firmador.documents.Document;
import cr.libre.firmador.gui.GUIInterface;

public class ValidateScheduler extends Thread {
    final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    public int MAX_FILES_PROCESS = 3;
    private Semaphore waitforfiles = new Semaphore(1);
    private Semaphore maxoffilesperprocess = new Semaphore(MAX_FILES_PROCESS);
    private List<Document> files;
    private boolean stop = false;
    private GUIInterface gui;

    public ValidateScheduler(GUIInterface gui) {
        this.files = new ArrayList<Document>();
        this.gui=gui;
    }

    public void run() {
        try {
            waitforfiles.acquire();// first time adquiere and don't block
        while (!stop) {
            if (this.files.size() <= 0)
                waitforfiles.acquire(); // block thread until list is empty
            while (!this.files.isEmpty()) {
                Document document = this.files.remove(0);
                maxoffilesperprocess.acquire();
                ValidationWorker task = new ValidationWorker(document, this);
                task.execute();
            }
        }
    } catch (InterruptedException e) {
        stop = true;
        e.printStackTrace();
    }
    }

    public void addDocument(Document document) {
        this.files.add(document);
        waitforfiles.release();
    }

    public void addDocuments(List<Document> documents) {
        for (Document doc : documents) {
            this.files.add(doc);
        }
        waitforfiles.release();
    }

    public void done() {
        maxoffilesperprocess.release();
        int avalilable = maxoffilesperprocess.availablePermits();
        if (avalilable == MAX_FILES_PROCESS) {
            gui.validateAllDone();
        }
    }
}