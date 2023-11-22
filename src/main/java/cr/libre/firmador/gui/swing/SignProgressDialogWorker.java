package cr.libre.firmador.gui.swing;

import java.util.concurrent.Semaphore;

import javax.swing.SwingWorker;

public class SignProgressDialogWorker extends SwingWorker<Void, Void> {
    private ProgressDialog progressMonitor;
    private boolean stop = false;
    private Semaphore waitformessages = new Semaphore(1);

    public SignProgressDialogWorker() {
        progressMonitor = new ProgressDialog("Firma de documentos", 0, 100);
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
