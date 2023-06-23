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

package cr.libre.firmador.gui;

import java.awt.event.WindowEvent;
import java.awt.HeadlessException;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.HashMap;
import javax.swing.GroupLayout;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import eu.europa.esig.dss.enumerations.MimeType;
import eu.europa.esig.dss.enumerations.MimeTypeEnum;
import eu.europa.esig.dss.model.InMemoryDocument;
import org.apache.hc.core5.http.HttpStatus;
import org.apache.pdfbox.io.IOUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cr.libre.firmador.CardSignInfo;
import cr.libre.firmador.ConfigListener;
import cr.libre.firmador.gui.swing.AboutLayout;
import cr.libre.firmador.gui.swing.ConfigPanel;
import cr.libre.firmador.gui.swing.RemoteDocInformation;
import cr.libre.firmador.gui.swing.RemoteHttpWorker;
import cr.libre.firmador.gui.swing.SignPanel;
import cr.libre.firmador.gui.swing.SwingMainWindowFrame;

public class GUIRemote extends BaseSwing implements GUIInterface, ConfigListener {
    final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    public JTabbedPane frameTabbedPane;
    private RemoteHttpWorker<Void, byte[]> remote;
    private RemoteDocInformation docinfo;

    public void loadGUI() {
        super.loadGUI();
        gui = this;
        settings.addListener(this);
        try {
            mainFrame = new SwingMainWindowFrame("Firmador Remoto");
        } catch (HeadlessException e) {
            LOG.error("No se pudo crear la ventana gráfica. Si se está ejecutando Java en entorno gráfico, verificar que no se ha instalado solamente el paquete headless sino el paquete completo para poder cargar la interfaz gráfica.");
            throw e;
        }
        mainFrame.setGUIInterface(this);
        mainFrame.loadGUI();

        remote = new RemoteHttpWorker<Void, byte[]>(gui);
        remote.execute();

        signPanel = new SignPanel();
        signPanel.setGUI(this);
        signPanel.initializeActions();
        signPanel.hideButtons();

        GroupLayout signLayout = new GroupLayout(signPanel);
        signPanel.signLayout(signLayout, signPanel);
        settings.addListener(signPanel);

        JPanel aboutPanel = new JPanel();
        GroupLayout aboutLayout = new AboutLayout(aboutPanel);
        ((AboutLayout) aboutLayout).setInterface(this);

        aboutPanel.setLayout(aboutLayout);
        aboutPanel.setOpaque(false);

        JPanel configPanel = new ConfigPanel();
        configPanel.setOpaque(false);
        frameTabbedPane = new JTabbedPane();
        frameTabbedPane.addTab("Firmar", signPanel);
        frameTabbedPane.setToolTipTextAt(0, "<html>En esta pestaña se muestran las opciones<br>para firmar el documento seleccionado.</html>");
        frameTabbedPane.addTab("Configuración", configPanel);
        frameTabbedPane.setToolTipTextAt(1, "<html>En esta estaña se configura<br>aspectos de este programa.</html>");
        frameTabbedPane.addTab("Acerca de", aboutPanel);
        frameTabbedPane.setToolTipTextAt(2, "<html>En esta estaña se muestra información<br>acerca de este programa.</html>");
        if (settings.showLogs) this.showLogs(frameTabbedPane);
        mainFrame.add(frameTabbedPane);
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainFrame.pack();
        mainFrame.setMinimumSize(mainFrame.getSize());
        mainFrame.setLocationByPlatform(true);
        mainFrame.setVisible(true);
    }

    GUIRemote() {
        super();
        setTabnumber(3);
    }

    public boolean signDocuments() {
        CardSignInfo card = getPin();
        super.signDocument(card, true);

        try {
            signedDocument.writeTo(docinfo.getData());
            docinfo.setStatus(HttpStatus.SC_SUCCESS);
        } catch (IOException e) {
            LOG.error("Error escribiendo documento", e);
            e.printStackTrace();
        }

        return signedDocument != null;
    }

    public void setArgs(String[] args) {}

    public String getDocumentToSign() {
        return null;
    }

    public String getPathToSave(String extension) {
        return null;
    }

    public void loadDocument(String fileName) {
        HashMap<String, RemoteDocInformation> docmap = remote.getDocInformation();
        docinfo = docmap.get(fileName);
        PDDocument doc;
        try {
            byte[] data =IOUtils.toByteArray( docinfo.getInputdata());
            toSignDocument = new InMemoryDocument(data, fileName);
            MimeType mimeType = toSignDocument.getMimeType();
            if(MimeTypeEnum.PDF == mimeType) {
                doc = PDDocument.load(data);
                loadDocument(mimeType, doc);
            } else if (mimeType == MimeTypeEnum.XML || mimeType == MimeTypeEnum.ODG || mimeType == MimeTypeEnum.ODP || mimeType == MimeTypeEnum.ODS || mimeType == MimeTypeEnum.ODT) {
                showMessage("Está intentando firmar un documento XML o un openDocument que no posee visualización");
                signPanel.getSignButton().setEnabled(true);
            } else signPanel.shownonPDFButtons();
        } catch (IOException e) {
            LOG.error("Error cargando documento", e);
            e.printStackTrace();
        }
    }

    public void extendDocument() {}

    public String getPathToSaveExtended(String extension) {
        return null;
    }

    public void displayFunctionality(String functionality) {}

    public void close() {
        mainFrame.dispatchEvent(new WindowEvent(mainFrame, WindowEvent.WINDOW_CLOSING));
    }

    public void updateConfig() {
        if (this.settings.showLogs) showLogs(this.frameTabbedPane);
        else hideLogs(this.frameTabbedPane);
    }

}
