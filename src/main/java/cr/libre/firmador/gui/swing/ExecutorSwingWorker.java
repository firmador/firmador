package cr.libre.firmador.gui.swing;

import javax.swing.SwingWorker;

import org.slf4j.LoggerFactory;

import com.google.common.base.Throwables;

import cr.libre.firmador.gui.GUIInterface;
import cr.libre.firmador.gui.GUISwing;


public class ExecutorSwingWorker extends SwingWorker<Void, Void> {
	private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(ExecutorSwingWorker.class);
 
	private GUIInterface gui;
	private ProgressDialog progressMonitor;
	
	
	
	public ExecutorSwingWorker(ProgressDialog progressMonitor, GUIInterface gui) {
		super();
		this.progressMonitor = progressMonitor;
		this.gui = gui;
	}

	@Override
	protected Void doInBackground() {
        try {
			((GUISwing) gui).dosignDocuments();
       // } catch (DSSException e) {
        	
		} catch (Exception e) {
			gui.showError(Throwables.getRootCause(e));
			LOG.error("Error en docsignDocument ", e);
			// eu.europa.esig.dss.model.DSSException: Can't initialize Sun PKCS#11 security provider. Reason: Unable to instantiate PKCS11 (JDK >= 9)

			e.printStackTrace();
		}
        return null;
	}

	 @Override
     public void done() {
		 progressMonitor.setVisible(false);
		 
     }
	 
}
