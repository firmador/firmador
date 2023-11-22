package cr.libre.firmador.validators;

import javax.swing.SwingWorker;

import cr.libre.firmador.documents.Document;

public class ValidationWorker extends SwingWorker<Void, Void> {

    private Document document;
    private ValidateScheduler scheduler;

    public ValidationWorker(Document document, ValidateScheduler scheduler) {
        this.document = document;
        this.scheduler = scheduler;
    }

    @Override
    protected Void doInBackground() throws Exception {
        try {
            document.validate();
        } catch (Throwable e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            scheduler.done();
        }

        return null;
    }
}
