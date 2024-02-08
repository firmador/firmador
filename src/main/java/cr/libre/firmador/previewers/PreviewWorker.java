package cr.libre.firmador.previewers;

import java.lang.invoke.MethodHandles;

import javax.swing.SwingWorker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cr.libre.firmador.documents.Document;

public class PreviewWorker extends SwingWorker<Void, Void> {
    final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private Document document;
    private PreviewScheduler scheduler;

    public PreviewWorker(Document document, PreviewScheduler scheduler) {
        this.document = document;
        this.scheduler = scheduler;
    }

    @Override
    protected Void doInBackground() {
        try {
            document.loadPreview();
        } catch (Throwable e) {
            LOG.error("Document preview with errors: " + document.getName());
            LOG.error("Preview Worker: " + e.getMessage(), e);

        } finally {
            LOG.info("Document preview loaded: " + document.getName());
            scheduler.done();
        }
        return null;

    }

}
