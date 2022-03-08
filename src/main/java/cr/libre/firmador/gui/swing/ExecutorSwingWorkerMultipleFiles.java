package cr.libre.firmador.gui.swing;

import java.io.File;
import java.security.KeyStore.PasswordProtection;

import javax.swing.SwingWorker;

import org.slf4j.LoggerFactory;

import com.google.common.base.Throwables;

import cr.libre.firmador.gui.GUIInterface;
import cr.libre.firmador.gui.GUISwing;  

public class ExecutorSwingWorkerMultipleFiles extends SwingWorker<Void, Void> {
	private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(ExecutorSwingWorkerMultipleFiles.class);
	private ProgressDialog progressMonitor;
	private GUIInterface gui;
	private File[] files;
	private ExecutorWorkerMultipleFiles worker;
 
	
	
	public ExecutorSwingWorkerMultipleFiles(ProgressDialog progressMonitor, GUIInterface gui, File[] files, ExecutorWorkerMultipleFiles worker) {
		super();
		this.progressMonitor = progressMonitor;
		this.gui = gui;
		this.files = files;
		this.worker = worker;
	}

	@Override
	protected Void doInBackground() throws Exception {
        PasswordProtection pin = gui.getPin();
		for (File file : files) {
			worker.setProgress(0);
			this.progressMonitor.setHeaderTitle("Firmando archivo: "+file.getName());
			try {
				((GUISwing) gui).signDocumentByPath(file, pin);	
			}catch (Exception e) {
				Throwable te = Throwables.getRootCause(e);
				String msg = te.toString();
				LOG.error(msg, te);
				if(te.getLocalizedMessage().equals("CKR_PIN_INCORRECT")) throw e;
            	if(msg.contains("but token only has 0 slots")) throw e;
            	if(msg.contains("failed to decrypt safe contents entry")) throw e;

			}
		}
        return null;
	}

	 @Override
     public void done() {
		 progressMonitor.setVisible(false);
     }
	 
}
