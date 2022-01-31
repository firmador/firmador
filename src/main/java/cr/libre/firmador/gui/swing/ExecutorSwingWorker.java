package cr.libre.firmador.gui.swing;

import javax.swing.SwingWorker;

import cr.libre.firmador.gui.GUIInterface;
import cr.libre.firmador.gui.GUISwing;  

public class ExecutorSwingWorker extends SwingWorker<Void, Void> {

 
	private GUIInterface gui;
	private ProgressDialog progressMonitor;
	
	
	
	public ExecutorSwingWorker(ProgressDialog progressMonitor, GUIInterface gui) {
		super();
		this.progressMonitor = progressMonitor;
		this.gui = gui;
	}

	@Override
	protected Void doInBackground() throws Exception {
        ((GUISwing) gui).dosignDocuments();
        return null;
	}

	 @Override
     public void done() {
		 progressMonitor.setVisible(false);
		 
     }
	 
}
