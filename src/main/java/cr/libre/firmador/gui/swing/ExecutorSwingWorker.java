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
