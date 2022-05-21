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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;

import cr.libre.firmador.gui.GUIInterface;

public class ValidatePanel extends ScrollableJPanel {
	public CopyableJLabel reportLabel;
	public JButton extendButton;
	private GUIInterface gui;

	public void setGUI(GUIInterface gui) {
		this.gui = gui;
	}

	public ValidatePanel() {
		super();

		reportLabel = new CopyableJLabel();
		extendButton = new JButton("Agregar sello de tiempo al documento");
		extendButton.setToolTipText(
				"<html>Este botón permite que el documento firmado que está cargado actualmente<br>agregue un nuevo sello de tiempo a nivel documento, con el propósito de<br>archivado longevo. También permite ampliar el nivel de firma a AdES-LTA<br>si el documento tiene un nivel de firma avanzada inferior.</html>");

		GroupLayout validateLayout = new GroupLayout(this);
		validateLayout.setAutoCreateGaps(true);
		validateLayout.setAutoCreateContainerGaps(true);
		validateLayout.setHorizontalGroup(
				validateLayout.createParallelGroup().addComponent(reportLabel)/*.addComponent(extendButton)*/);
		validateLayout.setVerticalGroup(
				validateLayout.createSequentialGroup().addComponent(reportLabel)/*.addComponent(extendButton)*/);
		this.setLayout(validateLayout);
		this.setOpaque(false);

	}

	public void initializeActions() {

		extendButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				gui.extendDocument();
			}
		});
	}

	public void hideButtons() {
		extendButton.setEnabled(false);
	}

	public CopyableJLabel getReportLabel() {
		return reportLabel;
	}

	public void setReportLabel(CopyableJLabel reportLabel) {
		this.reportLabel = reportLabel;
	}

	public JButton getExtendButton() {
		return extendButton;
	}

	public void setExtendButton(JButton extendButton) {
		this.extendButton = extendButton;
	}

	public JScrollPane getValidateScrollPane() {
		JScrollPane validateScrollPane = new JScrollPane();
		validateScrollPane.setPreferredSize(validateScrollPane.getPreferredSize());
		validateScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		validateScrollPane.setBorder(null);
		validateScrollPane.setViewportView(this);
		validateScrollPane.setOpaque(false);
		validateScrollPane.getViewport().setOpaque(false);
		validateScrollPane.setVisible(true);
		return validateScrollPane;
	}

}
