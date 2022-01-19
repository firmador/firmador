package cr.libre.firmador.gui.swing;

import javax.swing.GroupLayout;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;

public class LogginFrame extends ScrollableJPanel {
	private static final long serialVersionUID = 2015584665968200047L;
	private JTextArea logtext;
	public LogginFrame() {
		super();
		logtext = new JTextArea();
		GroupLayout validateLayout = new GroupLayout(this);
		validateLayout.setAutoCreateGaps(true);
		validateLayout.setAutoCreateContainerGaps(true);
		validateLayout.setHorizontalGroup(
				validateLayout.createParallelGroup().addComponent(logtext));
		validateLayout.setVerticalGroup(
				validateLayout.createSequentialGroup().addComponent(logtext));
		this.setLayout(validateLayout);
		this.setOpaque(false);
		logtext.setOpaque(false);
	}
	public void showInfo(String message) {
		this.logtext.append("\n"+message);
		
	}
	
	public JScrollPane getLogScrollPane() {
		JScrollPane logScrollPane = new JScrollPane();
		logScrollPane.setPreferredSize(logScrollPane.getPreferredSize());
		logScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		logScrollPane.setBorder(null);
		logScrollPane.setViewportView(this);
		logScrollPane.setOpaque(false);
		logScrollPane.getViewport().setOpaque(false);
		logScrollPane.setVisible(true);
		return logScrollPane;
	}
	
}
