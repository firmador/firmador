package cr.libre.firmador.gui.swing;

import java.awt.Toolkit;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;

import org.slf4j.LoggerFactory;

import cr.libre.firmador.gui.GUIInterface;
import cr.libre.firmador.gui.GUISwing;

public class ExecutorWorkerMultipleFiles extends Thread implements PropertyChangeListener, ExecutorWorkerInterface  {

	private ProgressDialog progressMonitor;
	private GUIInterface gui;
	private ExecutorSwingWorkerMultipleFiles task;
	private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(GUISwing.class);
	private Integer progressStatus = 0;
	private File[] files;
 
	
	public ExecutorWorkerMultipleFiles(GUIInterface gui, File[] files) {
		super();
		this.gui = gui;
 
		//progressMonitor = new ProgressMonitor(((GUISwing)gui).getMainFrame().getContentPane(), "Firmando documento", "", 0, 100);
		this.files = files;
		progressMonitor = new ProgressDialog("Firma de documentos", 0, 100);
		progressMonitor.setTitle(String.format("Proceso de firmado de %d  documentos", files.length));
		progressMonitor.setVisible(true);
		progressMonitor.setSize(500, 250);

		
	}


	@Override
	public void run() {
		nextStep("Inicio de firmado");
		task= new ExecutorSwingWorkerMultipleFiles(progressMonitor, gui, files, this);
		task.addPropertyChangeListener(this);
		task.execute();	
	}

	public void nextStep(String msg) {
		if(msg == null) msg="";
		progressStatus += 5 ;
		if(progressStatus > 100) progressStatus = 99; 
		progressMonitor.setProgress(progressStatus);
		String message =
                String.format(" %d%%.\n", progressStatus);
        progressMonitor.setNote(msg+message);
		try {
            Thread.sleep(500); // change the thread to show progress bar.
        } catch (InterruptedException e) {
            //e.printStackTrace();
        } 
	}
	
    public void propertyChange(PropertyChangeEvent evt) {
        if ("progress" == evt.getPropertyName() ) {
            int progress = (Integer) evt.getNewValue();
            progressMonitor.setProgress(progress);
            String message =
                String.format("Completando... %d%%.\n", progress);
            progressMonitor.setNote(message);
             
            if (progressMonitor.isCanceled() || task.isDone()) {
                Toolkit.getDefaultToolkit().beep();
                if (progressMonitor.isCanceled()) {
                    task.cancel(true);
                    LOG.info("Tarea cancelada");
                    
                } else {
                	LOG.info("Tarea completa");
                }
                
            }
        }
 
    }
    public void setProgress(Integer status) {
    	progressStatus = status;
    	progressMonitor.setProgress(progressStatus);
    }
}
