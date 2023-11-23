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

import java.awt.Toolkit;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cr.libre.firmador.cards.CardSignInfo;
import cr.libre.firmador.documents.Document;
import cr.libre.firmador.gui.GUIInterface;
import cr.libre.firmador.validators.ValidationWorker;

public class SignerScheduler extends Thread implements PropertyChangeListener, ExecutorWorkerInterface  {
    final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private GUIInterface gui;
    private SignerWorker task;
    private Integer progressStatus = 0;
    private List<Document> files;
    private boolean stop = false;
    private Semaphore waitforfiles = new Semaphore(1);
    private Semaphore maxoffilesperprocess = new Semaphore(1);
    private SignProgressDialogWorker progressMonitor;


    public SignerScheduler(GUIInterface gui, SignProgressDialogWorker progressMonitor) {
        super();
        this.gui = gui;
        files = new ArrayList<Document>();
        this.progressMonitor = progressMonitor;
    }


    @Override
    public void run() {
        int amountOfFiles = 0;
        int currentFileCounter = 0;
        try {
            waitforfiles.acquire();// first time adquiere and don't block
            nextStep("Inicio de firmado");

        while (!stop) {
            if (this.files.size() <= 0)
                waitforfiles.acquire(); // block thread until list is empty
            progressMonitor.setTitle(String.format("Proceso de firmado de %d  documentos", files.size()));
            progressMonitor.setHeaderTitle("Firmando documento");
            progressMonitor.setVisible(true);

            CardSignInfo card = gui.getPin();
            amountOfFiles = this.files.size();
            currentFileCounter = 0;
            progressMonitor.setProgressStatus(0);
            while (!this.files.isEmpty() && card != null) {
                Document document = this.files.remove(0);
                try {
                    maxoffilesperprocess.acquire();

                    progressMonitor.setHeaderTitle("Firmando " + document.getName());
                    task = new SignerWorker(this, progressMonitor, gui, document, card);
                    task.addPropertyChangeListener(this);
                    task.execute();
                } catch (InterruptedException e) {
                    LOG.debug("Interrupción al obtener bloqueo del hilo en documento: " + document.getPathName(), e);
                    e.printStackTrace();
                }
            }
            if (card == null) {
                progressMonitor.setHeaderTitle("Proceso cancelado");
                progressMonitor.setVisible(false);
                this.files.clear();
            }
        }
    } catch (InterruptedException e) {
        stop = true;
        e.printStackTrace();
    }
    }

    public void nextStep(String msg) {
        if(msg == null) msg="";
        progressStatus += 5 ;
        if(progressStatus > 100) progressStatus = 99;
        progressMonitor.setProgressStatus(progressStatus);
        String message =
                String.format(" %d%%.\n", progressStatus);
        progressMonitor.setNote(msg+message);
        try {
            Thread.sleep(500); // change the thread to show progress bar.
        } catch (InterruptedException e) {
            LOG.debug("Interrupción al correr el estado del progreso con múltiples ficheros", e);
            e.printStackTrace();
        }
    }

    public void propertyChange(PropertyChangeEvent evt) {
        if ("progress" == evt.getPropertyName() ) {
            int progress = (Integer) evt.getNewValue();
            progressMonitor.setProgressStatus(progress);
            String message =
                String.format("Completando... %d%%.\n", progress);
            progressMonitor.setNote(message);

            if (progressMonitor.isCanceled() || task.isDone()) {
                Toolkit.getDefaultToolkit().beep();
                if (progressMonitor.isCanceled()) {
                    task.cancel(true);
                    maxoffilesperprocess.release();
                    LOG.info("Tarea cancelada");
                } else {
                    LOG.info("Tarea completa");
                }
            }
        }
    }
    public void setProgress(Integer status) {
        progressStatus = status;
        progressMonitor.setProgressStatus(progressStatus);
    }

    public void done() {
        maxoffilesperprocess.release();
        if (files.isEmpty()) {
            progressMonitor.setVisible(false);
        }
    }

    public void addDocument(Document document) {
        this.files.add(document);
        waitforfiles.release();
    }

    public void addDocuments(List<Document> docfiles) {
        for (Document d : docfiles) {
            this.files.add(d);
        }
        waitforfiles.release();
    }
}
