package cr.libre.firmador.gui.swing;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FileDialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.LineBorder;

public class Pkcs12ConfigPanel extends JPanel {
	private JButton btpkcs12;
	private JList<String> pk12list;
	private DefaultListModel<String> pk12Model;
	private JButton addbtn;
	private JButton rmbtn;
    private JTextField pk12text;

	public String getText() {
		return "";
	}
	public void setText(String text) {
		
	}
    public String getFilePath() {
    	String dev = null;
    	
    	FileDialog loadDialog = new FileDialog(new JDialog(), "Seleccionar un archivo");
		loadDialog.setMultipleMode(false);
		loadDialog.setLocationRelativeTo(null);
		loadDialog.setVisible(true);
		loadDialog.dispose();
    	
		File[] files = loadDialog.getFiles();
		
		if(files.length>=1) {
			dev=files[0].toString();
		}
		return dev;
		
    }
	
	public Pkcs12ConfigPanel() {
		JPanel mainpactive = new JPanel(new BorderLayout(1, 3));
		
		pk12text = new JTextField();
		pk12Model = new DefaultListModel<String>();
		pk12list  = new JList<String>(pk12Model);
		pk12list.setBorder(new LineBorder(new Color(0, 0, 0)));
		btpkcs12 = new JButton("Elegir");
		
		addbtn = new JButton("+");
		rmbtn = new JButton("-");
		
		this.add(mainpactive);
		mainpactive.add(pk12list);
		this.add(pk12text);
        this.add(btpkcs12);

        mainpactive.add(addbtn);
        mainpactive.add(rmbtn);

        btpkcs12.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                String path = getFilePath();
                if(path != null && path.isEmpty() ) {
                	pk12text.setText(path);
                }
            }
        });
        
        rmbtn.addActionListener(new ActionListener() {
	    	public void actionPerformed(ActionEvent arg0) {
	    		List<String> selectedValues = pk12list.getSelectedValuesList();
	    		for(String item: selectedValues) {
	    			pk12Model.removeElement(item);
	    	 
	    		}
	    	}
	    });
        addbtn.addActionListener(new ActionListener() {
	    	public void actionPerformed(ActionEvent arg0) {
	    		String item = pk12text.getText();
	    		if(!item.isEmpty())	pk12Model.addElement(item);
	    	}
	    });
        
	}

}
