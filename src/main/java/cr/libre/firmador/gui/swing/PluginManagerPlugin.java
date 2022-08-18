package cr.libre.firmador.gui.swing;

import java.awt.BorderLayout;
import java.awt.Color;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.border.LineBorder;

import cr.libre.firmador.Settings;
import cr.libre.firmador.SettingsManager;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.awt.event.ActionEvent;

public class PluginManagerPlugin extends JPanel {
	private static final long serialVersionUID = -834388170590651815L;
	private JList<String> list_active;
	private JList<String> list_available;
	private DefaultListModel<String> activeModel;
	private DefaultListModel<String> availableModel;
	
	private JButton addactive;
	private JButton rmactive;
	private Settings settings;
	
	
	public void load_plugins(Settings settings) {
		if(settings==null)  settings=this.settings;
		
		activeModel.clear();
		availableModel.clear();
		for(String item: settings.active_plugins){
			activeModel.addElement(item);
		}
		for(String item: settings.available_plugins) {
			if(!activeModel.contains(item)) {
				availableModel.addElement(item);
			}
		}
	}
	
	public List<String> getActivePlugin(){
		List<String> active= new ArrayList<String>();
		for(int i = 0; i< activeModel.getSize();i++){
			active.add(activeModel.getElementAt(i));
        }
		return active;
	}
	
	public PluginManagerPlugin() {
		super(new BorderLayout(2, 2));
	    //this.setBorder(new LineBorder(new Color(0, 0, 0)));

		settings = SettingsManager.getInstance().get_and_create_settings();		
		activeModel = new DefaultListModel<String>();
		availableModel = new DefaultListModel<String>();
		load_plugins(settings);
		
	    JPanel mainpavailable = new JPanel(new BorderLayout(2, 2));
	    JPanel pavailable = new JPanel(new BorderLayout(2, 2));
	    JLabel lavailable = new JLabel("Plugins disponibles");
	    lavailable.setAlignmentX(CENTER_ALIGNMENT);
	    pavailable.add(lavailable, BorderLayout.CENTER);
	    list_available = new JList<String>(availableModel);
	    list_available.setBorder(new LineBorder(new Color(0, 0, 0)));
	    addactive = new JButton(">>");
	    pavailable.add(addactive, BorderLayout.LINE_END);
	    mainpavailable.add(pavailable, BorderLayout.NORTH);
	    mainpavailable.add(list_available, BorderLayout.CENTER);
	    
	    JPanel mainpactive = new JPanel(new BorderLayout(2, 2));
	    JPanel pactive = new JPanel(new BorderLayout(2, 2));
	    JLabel lactive = new JLabel("   Plugins activos");
	    pactive.add(lactive, BorderLayout.CENTER);
	    rmactive = new JButton("<<");

	    pactive.add(rmactive, BorderLayout.LINE_START);
	    list_active = new JList<String>(activeModel);
	    list_active.setBorder(new LineBorder(new Color(0, 0, 0)));
	    mainpactive.add(pactive, BorderLayout.NORTH);
	    mainpactive.add(list_active, BorderLayout.CENTER);
	    
	    JSplitPane sp = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, mainpavailable, mainpactive);
	    sp.setOneTouchExpandable(true);
	    sp.setResizeWeight(0.5); 
	    this.add(sp, BorderLayout.CENTER);
	    this.setOpaque(false);
	    rmactive.addActionListener(new ActionListener() {
	    	public void actionPerformed(ActionEvent arg0) {
	    		List<String> selectedValues = list_active.getSelectedValuesList();
	    		for(String item: selectedValues) {
	    			activeModel.removeElement(item);
	    			availableModel.addElement(item);
	    		}
	    	}
	    });
	    addactive.addActionListener(new ActionListener() {
	    	public void actionPerformed(ActionEvent arg0) {
	    		List<String> selectedValues = list_available.getSelectedValuesList();
	    		for(String item: selectedValues) {
	    			availableModel.removeElement(item);
	    			activeModel.addElement(item);
	    		}
	    	}
	    });
	   
	}
}
