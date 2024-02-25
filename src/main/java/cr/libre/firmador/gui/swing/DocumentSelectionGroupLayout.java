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

import java.awt.Container;
import java.awt.FileDialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.nio.file.FileSystems;
import java.nio.file.Path;

import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;

import cr.libre.firmador.MessageUtils;
import cr.libre.firmador.Settings;
import cr.libre.firmador.SettingsManager;
import cr.libre.firmador.documents.Document;
import cr.libre.firmador.gui.GUIInterface;
import cr.libre.firmador.gui.GUISwing;

public class DocumentSelectionGroupLayout extends GroupLayout {
    private JLabel fileLabel;
    public JTextField fileField;
    public JButton fileButton;
    private static FileDialog loadDialog;
    private SwingMainWindowFrame frame;
    public GUIInterface gui;
    private String lastDirectory = null;
    private String lastFile = null;

    public void setGUI(GUIInterface gui) {
        this.gui = gui;
    }

    public DocumentSelectionGroupLayout(Container host, JTabbedPane frameTabbedPane, SwingMainWindowFrame frame) {
        super(host);
        this.frame = frame;
        fileLabel = new JLabel(MessageUtils.t("document_selection_label"));
        fileField = new JTextField(MessageUtils.t("document_selection_filefield"));
        fileField.setToolTipText(MessageUtils.t("document_selection_filefield_tooltip"));
        fileField.getAccessibleContext()
                .setAccessibleDescription(MessageUtils.t("document_selection_filefield_tooltip_accessible"));

        fileField.setEditable(false);
        fileButton = new JButton(MessageUtils.t("document_selection_btn"));
        fileButton.setToolTipText(MessageUtils.t("document_selection_btn_tooltip"));
        fileButton.getAccessibleContext()
                .setAccessibleDescription(MessageUtils.t("document_selection_btn_tooltip_accessible"));
        fileButton.setMnemonic('N');
        fileButton.setOpaque(false);

        this.setAutoCreateGaps(true);
        this.setAutoCreateContainerGaps(true);
        this.setHorizontalGroup(this.createParallelGroup().addGroup(
            this.createSequentialGroup().addComponent(fileLabel).addComponent(fileField).addComponent(fileButton))
            .addComponent(frameTabbedPane));
        this.setVerticalGroup(this
            .createSequentialGroup().addGroup(this.createParallelGroup(GroupLayout.Alignment.BASELINE)
                .addComponent(fileLabel).addComponent(fileField).addComponent(fileButton))
                .addComponent(frameTabbedPane));

    }

    public void initializeActions() {
        fileButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                showLoadDialog();
                frame.pack();
                frame.setMinimumSize(frame.getSize());
            }
        });

    }

    private void showLoadDialog() {
        loadDialog = new FileDialog(frame, MessageUtils.t("document_selection_filedialog_title"));
        loadDialog.setMultipleMode(true);
        loadDialog.setLocationRelativeTo(null);
        loadDialog.setVisible(true);
        loadDialog.dispose();

        File[] files = loadDialog.getFiles();
        if(files.length>1) { // FIXME prompt if we want to sign or validate first //ggui.validateMultipleDocuments(files);
            GUISwing ggui = (GUISwing) gui;
            ggui.signMultipleDocuments(files);
        }else if( files.length==1) {
            lastDirectory = loadDialog.getDirectory();
            lastFile = files[0].toString();
            gui.loadDocument(files[0].toString());

        }
    }

    public String getLastDirectory() {
        return lastDirectory;
    }

    public String getLastFile() {
        return lastFile;
    }

    public void setLastFile(Document document) {
        this.lastFile = document.getPathName();
        Path path= FileSystems.getDefault().getPath(this.lastFile);
        this.lastDirectory = path.getParent().toString();
        if(document.getIsremote()) {
            fileField.setText(document.getName());
            fileField.getAccessibleContext().setAccessibleDescription(String.format(
                    MessageUtils.t("document_selection_filefield_load_tooltip_accessible"), document.getName()));
        } else {
            fileField.setText(path.getFileName().toString());
            fileField.getAccessibleContext().setAccessibleDescription(
                    String.format(MessageUtils.t("document_selection_filefield_load_tooltip_accessible"),
                            path.getFileName().toString()));
        }
        fileField.requestFocus(true);
        fileField.requestFocus();
    }

    public FileDialog getLoadDialog() {
        return loadDialog;
    }

    public void clean() {
        fileField.setText("");
    }
}
