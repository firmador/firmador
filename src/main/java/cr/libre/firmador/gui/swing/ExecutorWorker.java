/* Firmador is a program to sign documents using AdES standards.

Copyright (C) 2018, 2022 Firmador authors.

This file is part of Firmador.

Firmador is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

Firmador is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with Firmador.  If not, see <http://www.gnu.org/licenses/>.  */

package cr.libre.firmador.gui.swing;

import java.awt.Toolkit;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.slf4j.LoggerFactory;

import cr.libre.firmador.gui.GUIInterface;
import cr.libre.firmador.gui.GUISwing;

public class ExecutorWorker extends Thread implements PropertyChangeListener, ExecutorWorkerInterface  {

    private ProgressDialog progressMonitor;
    private GUIInterface gui;
    private ExecutorSwingWorker task;
    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(GUISwing.class);
    private Integer progressStatus = 0;


    public ExecutorWorker(GUIInterface gui) {
        super();
        this.gui = gui;
        //UIManager.put("ProgressMonitor.progressText", "Proceso de firmado de documentos");
        //progressMonitor = new ProgressMonitor(((GUISwing)gui).getMainFrame().getContentPane(), "Firmando documento", "", 0, 100);
        progressMonitor = new ProgressDialog("Firmando documento", 0, 100);
        progressMonitor.setVisible(true);
        progressMonitor.setSize(500, 250);
    }


    @Override
    public void run() {

        nextStep("Inicio de firmado");
        Toolkit.getDefaultToolkit().beep();

        task= new ExecutorSwingWorker(progressMonitor, gui);
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
            LOG.debug("Interrupción al correr el estado del progreso", e);
            e.printStackTrace();
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
}
