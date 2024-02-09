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

package cr.libre.firmador.signers;

import java.awt.Toolkit;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cr.libre.firmador.cards.CardSignInfo;
import cr.libre.firmador.documents.Document;
import cr.libre.firmador.gui.GUIInterface;
import cr.libre.firmador.gui.swing.ExecutorWorkerInterface;
import cr.libre.firmador.gui.swing.SignProgressDialogWorker;

public class SignerScheduler extends Thread implements PropertyChangeListener, ExecutorWorkerInterface  {
    final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    public static int MAX_FILES_PROCESS = 2;
    private GUIInterface gui;
    private SignerWorker task;
    private Integer progressStatus = 0;
    private List<Document> files;
    private boolean stop = false;
    private Semaphore waitforfiles = new Semaphore(1);
    private Semaphore maxoffilesperprocess = new Semaphore(MAX_FILES_PROCESS);
    private SignProgressDialogWorker progressMonitor;


    public SignerScheduler(GUIInterface gui, SignProgressDialogWorker progressMonitor) {
        super();
        this.gui = gui;
        this.files = new ArrayList<>();
        this.progressMonitor = progressMonitor;
    }


    @Override
    public void run() {
        try {
            this.waitforfiles.acquire(); // first time acquire and don't lock
            nextStep("Inicio de firmado");

            while (!this.stop) {
                if (this.files.isEmpty())
                    this.waitforfiles.acquire(); // lock thread until the list is not empty

                this.progressMonitor.setTitle(String.format("Proceso de firmado de %d documento(s)", files.size()));
                this.progressMonitor.setHeaderTitle("Firmando documento");
                this.progressMonitor.setVisible(true);

                CardSignInfo card = gui.getPin();
                this.progressMonitor.setProgressStatus(0);
                while (!this.files.isEmpty() && card != null) {
                    Document document = this.files.remove(0);
                    try {
                        this.maxoffilesperprocess.acquire();

                        this.progressMonitor.setHeaderTitle("Firmando " + document.getName());
                        this.task = new SignerWorker(this, progressMonitor, gui, document, card);
                        this.task.addPropertyChangeListener(this);
                        this.task.execute();
                    } catch (InterruptedException e) {
                        LOG.debug("Interrupción al obtener bloqueo del hilo en documento: " + document.getPathName(), e);
                        e.printStackTrace();
                    }
                }
                if (card == null) {
                    this.progressMonitor.setHeaderTitle("Proceso cancelado");
                    this.progressMonitor.setVisible(false);
                    this.files.clear();
                }
            }
        } catch (InterruptedException e) {
            this.stop = true;
            e.printStackTrace();
        }
    }

    public void nextStep(String msg) {
        if(msg == null) msg = "";
        this.progressStatus += 5;
        if(this.progressStatus > 100) this.progressStatus = 99;
        this.progressMonitor.setProgressStatus(this.progressStatus);
        String message = String.format(" %d%%.\n", this.progressStatus);
        this.progressMonitor.setNote(msg + message);
        try {
            sleep(500);  // change the thread to show progress bar.
        } catch (InterruptedException e) {
            LOG.debug("Interrupción al correr el estado del progreso con múltiples ficheros", e);
            e.printStackTrace();
        }
    }

    public void propertyChange(PropertyChangeEvent evt) {
        if ("progress".equals(evt.getPropertyName())) {
            int progress = (Integer) evt.getNewValue();
            this.progressMonitor.setProgressStatus(progress);
            String message = String.format("Completando... %d%%.\n", progress);
            this.progressMonitor.setNote(message);

            if (this.progressMonitor.isCanceled() || this.task.isDone()) {
                Toolkit.getDefaultToolkit().beep();
                if (this.progressMonitor.isCanceled()) {
                    this.task.cancel(true);
                    this.maxoffilesperprocess.release();
                    this.LOG.info("Tarea cancelada");
                } else {
                    this.LOG.info("Tarea completada");
                }
            }
        }
    }

    public void setProgress(Integer status) {
        this.progressStatus = status;
        this.progressMonitor.setProgressStatus(this.progressStatus);
    }

    public void done() {
        this.maxoffilesperprocess.release();
        int available = this.maxoffilesperprocess.availablePermits();
        if (available == MAX_FILES_PROCESS) {
            this.progressMonitor.setVisible(false);
            this.gui.signAllDone();
        }
    }

    public void addDocument(Document document) {
        document.setShowPreview(true);
        this.files.add(document);
        this.waitforfiles.release();
    }

    public void addDocuments(List<Document> docfiles) {
        for (Document d : docfiles) {
            d.setShowPreview(false);
            this.files.add(d);

        }
        this.waitforfiles.release();
    }

    public void sleep(int ms) throws InterruptedException{
        Thread.sleep(ms);
    }

    public GUIInterface getGui() {
        return this.gui;
    }

    public List<Document> getFiles(){
        return this.files;
    }

    public SignProgressDialogWorker getProgressMonitor() {
        return this.progressMonitor;
    }

    public void setProgressStatus(Integer progressStatus){
        this.progressStatus = progressStatus;
    }

    public Integer getProgressStatus(){
        return this.progressStatus;
    }

    public SignerWorker getTask(){
        return this.task;
    }

    public void setTask(SignerWorker task){
        this.task = task;
    }

    public boolean getStop(){
        return this.stop;
    }

    public void setWaitforfiles(Semaphore waitforfiles){
        this.waitforfiles = waitforfiles;
    }

    public void setMaxoffilesperprocess(Semaphore maxoffilesperprocess){
        this.maxoffilesperprocess = maxoffilesperprocess;
    }
}
