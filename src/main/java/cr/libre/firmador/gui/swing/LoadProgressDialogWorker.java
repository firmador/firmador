package cr.libre.firmador.gui.swing;

import java.util.concurrent.Semaphore;

import javax.swing.SwingWorker;

import cr.libre.firmador.MessageUtils;
import cr.libre.firmador.gui.GUIInterface;
import cr.libre.firmador.gui.GUISwing;

public class LoadProgressDialogWorker extends SwingWorker<Void, Void> {
    private ProgressDialog progressMonitor;
    private GUIInterface gui;
    private boolean stop = false;
    private Semaphore waitformessages = new Semaphore(1);

    public LoadProgressDialogWorker(GUIInterface gui) {
        progressMonitor = new ProgressDialog(((GUISwing) gui).getMainFrame(), MessageUtils.t("loadprogressdialogworker_analyzing_docs"), 0, 100);
        progressMonitor.setSize(500, 250);
        progressMonitor.setVisible(false);
    }

    @Override
    protected Void doInBackground() throws Exception {
        waitformessages.acquire(); // First time just to block for wait for messages
        while (!stop) {
            waitformessages.acquire();
            progressMonitor.setModal(true);

            progressMonitor.setVisible(true);
            progressMonitor.setVisible(false);
        }
        return null;
    }

    public void setVisible(boolean visible) {
        if (visible) {
            waitformessages.release();
        } else {
            progressMonitor.setVisible(false);
        }
    }

    public void setTitle(String message) {
        progressMonitor.setTitle(message);
    }

    public void setProgressStatus(int progress) {
        progressMonitor.setProgress(progress);
    }

    public void setNote(String note) {
        progressMonitor.setNote(note);
    }

    public boolean isCanceled() {
        return progressMonitor.isCanceled();
    }

    public void setHeaderTitle(String title) {
        progressMonitor.setHeaderTitle(title);
    }
}
