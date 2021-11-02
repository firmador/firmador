package cr.libre.firmador.gui.swing;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JLabel;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JTextField;

import javax.swing.JButton;
import javax.swing.SwingConstants;
import java.awt.GridLayout;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.border.EmptyBorder;

import cr.libre.firmador.Settings;
import cr.libre.firmador.SettingsManager;

import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.JTextArea;

public class ConfigPanel extends JPanel {
	private static final long serialVersionUID = 1L;
	private JTextField reason;
	private JTextField place;
	private JTextField contact;
	private JTextField dateformat;
	private JCheckBox withoutvisiblesign;
	private JCheckBox uselta;
	private JCheckBox overwritesourcefile;
	JTextArea defaultsignmessage;
	private JSpinner signwith;
	private JSpinner signheight;
	private JSpinner fontsize;
	private JSpinner signx;
	private JSpinner signy;

	Settings settings;
	SettingsManager manager;
	
	public ConfigPanel() {
		manager = SettingsManager.getInstance();
		settings = manager.get_and_create_settings();
		setLayout(new BorderLayout(0, 0));
		 
		JLabel lblValoresPorDefecto = new JLabel("Valores por defecto");
		lblValoresPorDefecto.setHorizontalAlignment(SwingConstants.CENTER);
		add(lblValoresPorDefecto, BorderLayout.NORTH);
		
		JPanel panel = new JPanel();
		panel.setBorder(new EmptyBorder(10, 10, 10, 10));
	    panel.setLayout(new GridLayout(0, 2, 4,4));
		
		withoutvisiblesign = new JCheckBox("Firma Visible", this.settings.withoutvisiblesign);
	 
		panel.add(withoutvisiblesign);
		
		uselta = new JCheckBox("Usar LTA automático", this.settings.uselta);
		panel.add(uselta);
		
		overwritesourcefile = new JCheckBox("Sobreescribir archivo original", this.settings.overwritesourcefile);
		panel.add(overwritesourcefile);
		
		JLabel lblNewLabel_8 = new JLabel("");
		panel.add(lblNewLabel_8);
		
		JLabel lreason = new JLabel("Razón:");
		panel.add(lreason);
		
		reason = new JTextField();
		reason.setText(this.settings.reason);
		panel.add(reason);
		reason.setColumns(10);
		
		JLabel lplace = new JLabel("Lugar:");
		panel.add(lplace);
		
		place = new JTextField();
		place.setText(this.settings.place);
		panel.add(place);
		place.setColumns(10);
		
		JLabel lcontact = new JLabel("Contacto:");
		panel.add(lcontact);
		
		contact = new JTextField();
		contact.setText(this.settings.contact);
		panel.add(contact);
		contact.setColumns(10);
		
		JLabel ldateformat = new JLabel("Formato de fecha");
		ldateformat.setToolTipText("Debe ser compatible con formatos de fecha de java");
		panel.add(ldateformat);
		
		dateformat = new JTextField();
		dateformat.setText(this.settings.dateformat);
		panel.add(dateformat);
		dateformat.setColumns(10);
		
		JLabel ldefaultsignmessage = new JLabel("Mensaje de firma");
		panel.add(ldefaultsignmessage);
		
		defaultsignmessage = new JTextArea();
		defaultsignmessage.setText(this.settings.defaultsignmessage);
		panel.add(defaultsignmessage);
		
		JLabel lsignwidth = new JLabel("Ancho de firma");
		panel.add(lsignwidth);
		
		signwith = new JSpinner();
		signwith.setModel(new SpinnerNumberModel(this.settings.signwith, null, null, 1));
		panel.add(signwith);
		
		JLabel lsignheight = new JLabel("Largo de firma");
		panel.add(lsignheight);
		
		signheight = new JSpinner();
		signheight.setModel(new SpinnerNumberModel(this.settings.signheight, null, null, 1));
		panel.add(signheight);
		
		JLabel lsignx = new JLabel("Posición inicial X");
		panel.add(lsignx);
		
		signx = new JSpinner();
		signx.setModel(new SpinnerNumberModel(this.settings.signx, null, null, 1));
		panel.add(signx);
		
		JLabel lsigny = new JLabel("Posición inicial Y");
		panel.add(lsigny);
		
		signy = new JSpinner();
		signy.setModel(new SpinnerNumberModel(this.settings.signy, null, null, 1));
		panel.add(signy);
		
		JLabel lfontsize = new JLabel("Tamaño de fuente");
		panel.add(lfontsize);
		
		fontsize = new JSpinner();
		fontsize.setModel(new SpinnerNumberModel(this.settings.fontsize, null, null, 1));
		panel.add(fontsize);
		
		//panel.setPreferredSize(new Dimension(550, 250));
		
		JScrollPane configPanel = new JScrollPane(panel, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		configPanel.setPreferredSize(new Dimension(600, 300));
		//configPanel.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		configPanel.setBorder(null);
		
		//configPanel.setViewportView(panel);
		configPanel.setOpaque(false);
		configPanel.getViewport().setOpaque(false);
		
		add(configPanel, BorderLayout.CENTER);
		
		
		JPanel btns = new JPanel();
		add(btns, BorderLayout.SOUTH);
		
		JButton applywithoutsave = new JButton("Aplicar sin guardar");
		applywithoutsave.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				charge_settings();
			}
		});
		btns.add(applywithoutsave);
		
		JButton btsave = new JButton("Guardar");
		btsave.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				save_settings();
			}
		});
		btns.add(btsave);
			 
		

	}
	public void charge_settings() {
		settings.withoutvisiblesign=this.withoutvisiblesign.isSelected();
		settings.reason= reason.getText();
		settings.place = place.getText();
		settings.contact= contact.getText();
		settings.dateformat = this.dateformat.getText();
		settings.defaultsignmessage= defaultsignmessage.getText();
		settings.withoutvisiblesign = withoutvisiblesign.isSelected();
		settings.uselta= uselta.isSelected();
		settings.overwritesourcefile = overwritesourcefile.isSelected();
		settings.signwith = Integer.parseInt(signwith.getValue().toString());
		settings.signheight = Integer.parseInt(signheight.getValue().toString());
		settings.fontsize = Integer.parseInt(fontsize.getValue().toString());
		settings.signx = Integer.parseInt(signx.getValue().toString());
		settings.signy = Integer.parseInt(signy.getValue().toString());

		
	}
	public void save_settings() {
		charge_settings();
		this.manager.setSettings(this.settings, true);
	}
}
