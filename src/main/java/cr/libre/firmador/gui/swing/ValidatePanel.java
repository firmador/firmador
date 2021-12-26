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
				validateLayout.createParallelGroup().addComponent(reportLabel).addComponent(extendButton));
		validateLayout.setVerticalGroup(
				validateLayout.createSequentialGroup().addComponent(reportLabel).addComponent(extendButton));
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
