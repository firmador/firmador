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

import java.awt.FileDialog;
import java.awt.HeadlessException;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javax.swing.GroupLayout;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;

import eu.europa.esig.dss.enumerations.MimeType;
import eu.europa.esig.dss.enumerations.MimeTypeEnum;
import eu.europa.esig.dss.model.FileDocument;
import eu.europa.esig.dss.model.InMemoryDocument;
import org.apache.hc.core5.http.HttpStatus;
import org.apache.pdfbox.io.IOUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cr.libre.firmador.CardSignInfo;
import cr.libre.firmador.ConfigListener;
import cr.libre.firmador.FirmadorUtils;
import cr.libre.firmador.gui.swing.AboutLayout;
import cr.libre.firmador.gui.swing.ConfigPanel;
import cr.libre.firmador.gui.swing.DocumentSelectionGroupLayout;
import cr.libre.firmador.gui.swing.ExecutorWorker;
import cr.libre.firmador.gui.swing.ExecutorWorkerMultipleFiles;
import cr.libre.firmador.gui.swing.ExecutorWorkerMultipleFilesValidator;
import cr.libre.firmador.gui.swing.RemoteDocInformation;
import cr.libre.firmador.gui.swing.RemoteHttpWorker;
import cr.libre.firmador.gui.swing.SignPanel;
import cr.libre.firmador.gui.swing.SwingMainWindowFrame;
import cr.libre.firmador.gui.swing.ValidatePanel;

public class GUISwing extends BaseSwing implements GUIInterface, ConfigListener{
    final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private Boolean isRemote = false;
    public JTabbedPane frameTabbedPane;
    private String documenttosign = null;
    private String documenttosave = null;
    private DocumentSelectionGroupLayout docSelector;
    private PDDocument doc;
    private String fileName;
    private RemoteDocInformation docinfo;
    private RemoteHttpWorker<Void, byte[]> remote;






    public void loadGUI() {
        super.loadGUI();
        gui = this;
        settings.addListener(this);
        try {
            mainFrame = new SwingMainWindowFrame(isRemote ? "Firmador remoto" : "Firmador");
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
        if (!isRemote) {// TODO add setting for toggling validation tab
            validatePanel = new ValidatePanel();
            validatePanel.setGUI(this);
            validatePanel.initializeActions();
            validatePanel.hideButtons();
        }
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
        int tabPosition = 1;
        if (!isRemote) {// TODO add setting for toggling validation tab
            frameTabbedPane.addTab("Validación", validatePanel.getValidateScrollPane());
            frameTabbedPane.setToolTipTextAt(tabPosition, "<html>En esta pestaña se muestra información de validación<br>de las firmas digitales.</html>");
            tabPosition++;
        }
        frameTabbedPane.addTab("Configuración", configPanel);
        frameTabbedPane.setToolTipTextAt(tabPosition, "<html>En esta pestaña se configura<br>aspectos de este programa.</html>");
        frameTabbedPane.addTab("Acerca de", aboutPanel);
        frameTabbedPane.setToolTipTextAt(tabPosition + 1, "<html>En esta pestaña se muestra información<br>acerca de este programa.</html>");
        if (settings.showLogs) this.showLogs(frameTabbedPane);
        docSelector = new DocumentSelectionGroupLayout(mainFrame.getContentPane(), frameTabbedPane, mainFrame);
        docSelector.setGUI(this);
        docSelector.initializeActions();
        if (!isRemote) mainFrame.getContentPane().setLayout(docSelector);
        else mainFrame.getContentPane().setLayout(signLayout);
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainFrame.pack();
        mainFrame.setMinimumSize(mainFrame.getSize());
        mainFrame.setLocationByPlatform(true);
        mainFrame.setVisible(true);
        if (documenttosign != null) loadDocument(documenttosign);
    }

    public void loadDocument(String fileName) {
        gui.nextStep("Cargando el documento");
        if (!isRemote) {
            clearElements();
            docSelector.setLastFile(fileName);
            docSelector.fileField.setText(Paths.get(fileName).getFileName().toString());
            FileDocument mimeDocument = new FileDocument(fileName);

            try {
                if (mimeDocument.getMimeType() == MimeTypeEnum.PDF) doc = PDDocument.load(new File(fileName));
                loadDocument(mimeDocument.getMimeType(), doc);
            } catch (IOException e) {
                LOG.error("Error Leyendo el archivo", e);
                e.printStackTrace();
                clearElements();
            }
            gui.nextStep("Validando firmas dentro del documento");
            validateDocument(fileName);
        } else {
            HashMap<String, RemoteDocInformation> docmap = remote.getDocInformation();
            docinfo = docmap.get(fileName);
            PDDocument doc;
            try {
                byte[] data =IOUtils.toByteArray( docinfo.getInputdata());
                toSignDocument = new InMemoryDocument(data, fileName);
                MimeType mimeType = toSignDocument.getMimeType();
                if (MimeTypeEnum.PDF == mimeType) {
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
    }

    public boolean signDocuments() {
        if (!isRemote) {
            worker = new ExecutorWorker(this);
            SwingUtilities.invokeLater((Runnable) worker);
            Thread.yield();
            return true;
        } else {
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
    }

    public void setArgs(String[] args) {
        List<String> arguments = new ArrayList<String>();
        for (String params : args) {
            if (!params.startsWith("-"))
                arguments.add(params);
        }
        if (arguments.size() > 1)
            documenttosign = Paths.get(arguments.get(0)).toAbsolutePath().toString();
        if (arguments.size() > 2)
            documenttosave = Paths.get(arguments.get(1)).toAbsolutePath().toString();
    }

    public void extendDocument() {
        if (fileName == null)fileName = getDocumentToSign();
        if (fileName != null) extendDocument(new FileDocument(fileName), false, null);
    }

    public String getDocumentToSign() {
        return docSelector.getLastFile();
    }

    public String getPathToSave(String extension) {
        if (settings.overwriteSourceFile)
            return getDocumentToSign();
        if (documenttosave != null)
            return documenttosave;
        String pathToSave = showSaveDialog("-firmado", extension);
        return pathToSave;
    }

    public String getPathToSaveExtended(String extension) {
        if (settings.overwriteSourceFile)
            return getDocumentToSign();
        String pathToExtend = showSaveDialog("-sellado", extension);
        return pathToExtend;
    }

    public void displayFunctionality(String functionality) {
        if (functionality.equalsIgnoreCase("sign")) frameTabbedPane.setSelectedIndex(0);
        else if (functionality.equalsIgnoreCase("validator")) frameTabbedPane.setSelectedIndex(1);
    }

    public void updateConfig() {
        if (this.settings.showLogs) showLogs(this.frameTabbedPane);
        else hideLogs(this.frameTabbedPane);
    }

    public void close() {
        mainFrame.dispatchEvent(new WindowEvent(mainFrame, WindowEvent.WINDOW_CLOSING));
    }

    private String addSuffixToFilePath(String name, String suffix) {
        String dotExtension = "";
        String newname = name + suffix;
        int lastDot = name.lastIndexOf(".");
        if (lastDot >= 0) {
            dotExtension = name.substring(lastDot);
            newname = name.substring(0, name.lastIndexOf(".")) + suffix + dotExtension;
        }
        return newname;
    }


    public boolean doSignDocuments() {
        boolean ok = false;
        fileName = getDocumentToSign();
        toSignDocument = new FileDocument(fileName);
        CardSignInfo card = getPin();
        this.signDocument(card, !signPanel.getSignatureVisibleCheckBox().isSelected(), true);

        if (signedDocument != null) {
            try {
                fileName = getPathToSave(getExtension());
                if (fileName != null) {
                    signedDocument.save(fileName);
                    showMessage("Documento guardado satisfactoriamente en<br>" + fileName);
                    loadDocument(fileName);
                }
                ok = true;
            } catch (IOException e) {
                LOG.error("Error Firmando documento", e);
                showError(FirmadorUtils.getRootCause(e));
            }
        }
        return ok;
    }

    public String showSaveDialog(String suffix, String extension) {
        gui.nextStep("Obteniendo ruta de guardado");
        String lastDirectory = docSelector.getLastDirectory();
        String lastFile = docSelector.getLastFile();
        String fileName = null;
        FileDialog saveDialog = null;
        saveDialog = new FileDialog(saveDialog, "Guardar documento", FileDialog.SAVE);
        saveDialog.setDirectory(lastDirectory);
        String dotExtension = "";
        int lastDot = lastFile.lastIndexOf(".");
        if (extension != "") {
            suffix = ""; // XMLs could reuse same files, however
            dotExtension = extension;
        } else if (lastDot >= 0) dotExtension = lastFile.substring(lastDot);

        Path path = Paths.get(lastFile);
        lastFile=path.getFileName().toString();
        saveDialog.setFile(lastFile.substring(0, lastFile.lastIndexOf(".")) + suffix + dotExtension);
        //saveDialog.setFilenameFilter(docSelector.getLoadDialog().getFilenameFilter()); // FIXME use filter based on file type containing the signature
        saveDialog.setLocationRelativeTo(null);
        saveDialog.setVisible(true);
        saveDialog.dispose();
        if (saveDialog.getFile() != null) {
            fileName = saveDialog.getDirectory() + saveDialog.getFile();
            lastDirectory = saveDialog.getDirectory();
            lastFile = saveDialog.getFile();
        }
        return fileName;
    }

    public void signDocumentByPath(File file, CardSignInfo card) {
        documenttosign = file.toString();
        loadDocument(documenttosign);
        toSignDocument = new FileDocument(documenttosign);
        this.signDocument(card, !signPanel.getSignatureVisibleCheckBox().isSelected(), false);
        if (signedDocument != null) {
            fileName = addSuffixToFilePath(documenttosign, "-firmado");
            try {
                signedDocument.save(fileName);
            } catch (IOException e) {
                LOG.error("Error Firmando Multiples documentos", e);
                gui.showError(FirmadorUtils.getRootCause(e));
            }
        }
    }

    public void signMultipleDocuments(File[] files) {
        worker = new ExecutorWorkerMultipleFiles(this, files);
        SwingUtilities.invokeLater((Runnable) worker);
        Thread.yield();
    }

    public void validateDocumentByPath(File file) {
        validateDocument(file.toString());
    }

    public void validateMultipleDocuments(File[] files) {
        worker = new ExecutorWorkerMultipleFilesValidator(this, files);
        SwingUtilities.invokeLater((Runnable) worker);
        Thread.yield();
    }

    public void clearElements() {
        docSelector.fileField.setText("");
        if (doc != null) {
            try {
                doc.close();
            } catch (IOException e) {
                LOG.error("Error cerrando archivo", e);
                e.printStackTrace();
            }
        }
        // Muchas cosas mas acá
    }

    public String getExtension() {
        String extension = "";
        if (toSignDocument != null) {
            MimeType mimeType = toSignDocument.getMimeType();
            if (mimeType == MimeTypeEnum.XML) extension = ".xml";
            else if (mimeType != MimeTypeEnum.PDF && !(mimeType == MimeTypeEnum.ODG || mimeType == MimeTypeEnum.ODP || mimeType == MimeTypeEnum.ODS || mimeType == MimeTypeEnum.ODT)) extension = ".p7s"; // p7s detached, p7m enveloping
        }
        return extension;
    }

}
