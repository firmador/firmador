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

import javax.swing.SwingWorker;

import cr.libre.firmador.documents.PreviewerInterface;
import cr.libre.firmador.gui.GUIInterface;
import cr.libre.firmador.gui.GUISwing;

public class BuilderPreviewActionWorker extends SwingWorker<Void, Void> {
    private BuilderPreviewWorker worker;
    private PreviewerInterface preview;
    private String filePath;
    private GUIInterface gui;

    public BuilderPreviewActionWorker(BuilderPreviewWorker worker, PreviewerInterface preview, String filePath,
            GUIInterface gui) {
        super();
        this.worker = worker;
        this.preview = preview;
        this.filePath = filePath;
        this.gui = gui;
    }

    protected Void doInBackground() throws Exception {
        try {
            preview.loadDocument(filePath);
            worker.nextStep("Generando renderización del documento");
            preview.getRender();
        } catch (Throwable e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }

    public void done() {
        SignPanel signpanel = ((GUISwing) gui).getSignPanel();
        signpanel.setPreview(preview);
        signpanel.paintPDFViewer();
        worker.done();

    }

}
