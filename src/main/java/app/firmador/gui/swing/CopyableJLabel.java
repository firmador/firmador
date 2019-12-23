package app.firmador.gui.swing;

import javax.swing.BorderFactory;
import javax.swing.JTextPane;

public class CopyableJLabel extends JTextPane {

	public CopyableJLabel() {
		super();
		setDefault();
	}
	
	public CopyableJLabel(String text) {
		super();
		setDefault();
		setText(text);
	}
	
	private void setDefault() {
		this.setContentType("text/html"); // let the text pane know this is what you want
		this.setEditable(false); // as before
		this.setBackground(null); // this is the same as a JLabel
		//this.setBorder(null); // remove the border
		this.setOpaque(false);
		
		this.setBorder(BorderFactory.createCompoundBorder(
		        this.getBorder(), 
		        BorderFactory.createEmptyBorder(5, 5, 5, 5)));	
		this.setAutoscrolls(true);
	}
	
	
}
