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

import java.lang.invoke.MethodHandles;

import javax.swing.SwingWorker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cr.libre.firmador.cards.CardSignInfo;
import cr.libre.firmador.documents.Document;
import cr.libre.firmador.gui.GUIInterface;
import cr.libre.firmador.gui.swing.SignProgressDialogWorker;

public class SignerWorker extends SwingWorker<Void, Void> {
    final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private GUIInterface gui;
    private SignProgressDialogWorker progressMonitor;
    private Document document;
    private CardSignInfo card;
    private SignerScheduler scheduler;

    public SignerWorker(SignerScheduler scheduler, SignProgressDialogWorker progressMonitor, GUIInterface gui,
            Document document,
            CardSignInfo card) {
        super();
        this.progressMonitor = progressMonitor;
        this.gui = gui;
        this.document = document;
        this.card = card;
        this.scheduler = scheduler;
    }

    public SignerWorker(SignProgressDialogWorker progressMonitor, GUIInterface gui) {
        super();
        this.progressMonitor = progressMonitor;
        this.gui = gui;
    }

    protected Void doInBackground() {
        try {
        this.document.sign(card);
        } catch (Exception e) {
            LOG.error("Error firmado documento", e);
            throw e;
        }
        return null;
    }

    public void done() {
        scheduler.done();
    }

    public Document getDocument() {
        return document;
    }

    public void setDocument(Document document) {
        this.document = document;
    }

    public CardSignInfo getCard() {
        return card;
    }

    public void setCard(CardSignInfo card) {
        this.card = card;
    }

}
