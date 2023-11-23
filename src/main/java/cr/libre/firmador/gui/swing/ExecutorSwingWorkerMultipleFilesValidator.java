/* Firmador is a program to sign documents using AdES standards.

Copyright (C) Firmador authors.

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

import java.io.File;
import java.lang.invoke.MethodHandles;
import javax.swing.SwingWorker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cr.libre.firmador.gui.GUIInterface;
import cr.libre.firmador.gui.GUISwing;
import cr.libre.firmador.signers.FirmadorUtils;

public class ExecutorSwingWorkerMultipleFilesValidator extends SwingWorker<Void, Void> {
    final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private ProgressDialog progressMonitor;
    private GUIInterface gui;
    private File[] files;
    private ExecutorWorkerMultipleFilesValidator worker;



    public ExecutorSwingWorkerMultipleFilesValidator(ProgressDialog progressMonitor, GUIInterface gui, File[] files, ExecutorWorkerMultipleFilesValidator worker) {
        super();
        this.progressMonitor = progressMonitor;
        this.gui = gui;
        this.files = files;
        this.worker = worker;
    }

    @Override
    protected Void doInBackground() throws Exception {

        for (File file : files) {
            worker.setProgress(0);
            this.progressMonitor.setHeaderTitle("Validando archivo: "+file.getName());
            try {
                ((GUISwing) gui).validateDocumentByPath(file);
            } catch (Exception e) {
                Throwable te = FirmadorUtils.getRootCause(e);
                String msg = te.toString();
                LOG.error(msg, te);




            }
        }
        return null;
    }

     @Override
     public void done() {
         progressMonitor.setVisible(false);
     }

}
