package cr.libre.firmador.gui.swing;

import java.io.File;
import java.security.KeyStore.PasswordProtection;

import javax.swing.SwingWorker;

import cr.libre.firmador.gui.GUIInterface;
import cr.libre.firmador.gui.GUISwing;  

public class ExecutorSwingWorkerMultipleFiles extends SwingWorker<Void, Void> {

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
			((GUISwing) gui).signDocumentByPath(file, pin);	
		}
        return null;
	}

	 @Override
     public void done() {
		 progressMonitor.setVisible(false);
     }
	 
}
