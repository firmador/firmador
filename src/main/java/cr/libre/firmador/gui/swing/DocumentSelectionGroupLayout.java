/* Firmador is a program to sign documents using AdES standards.

Copyright (C) 2021 Firmador authors.

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

import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;

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
		fileLabel = new JLabel("Documento: ");
		fileField = new JTextField("(Vacío)");
		fileField.setToolTipText(
				"<html>Este campo indica el nombre del fichero<br>que está seleccionado para firmar o validar.</html>");
		fileField.setEditable(false);
		fileButton = new JButton("Elegir...");
		fileButton.setToolTipText(
				"<html>Haga clic en este botón para seleccionar uno o<br>varios ficheros a firmar, o un fichero a validar.</html>");

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
		loadDialog = new FileDialog(frame, "Seleccionar documento a firmar");
		loadDialog.setMultipleMode(true);
		loadDialog.setLocationRelativeTo(null);
		loadDialog.setVisible(true);
		loadDialog.dispose();

		File[] files = loadDialog.getFiles();
		if(files.length>1) {
			GUISwing ggui = (GUISwing) gui;
			ggui.signMultipleDocuments(files, loadDialog.getDirectory());
		}else if( files.length==1) {
			gui.loadDocument(files[0].toString());
            lastDirectory = loadDialog.getDirectory();
            lastFile = files[0].toString();
		}
	}

	public String getLastDirectory() {
		return lastDirectory;
	}

	public void setLastDirectory(String lastDirectory) {
		this.lastDirectory = lastDirectory;
	}

	public String getLastFile() {
		return lastFile;
	}

	public void setLastFile(String lastFile) {
		this.lastFile = lastFile;
	}

	public FileDialog getLoadDialog() {
		return loadDialog;
	}

	public static void setLoadDialog(FileDialog loadDialog) {
		DocumentSelectionGroupLayout.loadDialog = loadDialog;
	}

}
