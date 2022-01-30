/* Firmador is a program to sign documents using AdES standards.

Copyright (C) 2018, 2022 Firmador authors.

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

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JTextField;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.SwingConstants;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FileDialog;
import java.awt.Font;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.awt.event.ActionEvent;

import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentListener;
import javax.swing.event.DocumentEvent;

import cr.libre.firmador.Settings;
import cr.libre.firmador.SettingsManager;
import cr.libre.firmador.gui.GUISwing;
import eu.europa.esig.dss.enumerations.SignatureLevel;

import javax.swing.filechooser.FileFilter;

import org.slf4j.LoggerFactory;

import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.JTextArea;
import java.awt.Component;
import java.awt.Color;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.geom.Ellipse2D;
import javax.swing.Box;
import javax.swing.BoxLayout;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

public class ConfigPanel extends JPanel {
    private static final long serialVersionUID = 1L;
    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(ConfigPanel.class);

    private JTextField reason;
    private JTextField place;
    private JTextField contact;
    private JTextField dateformat;
    private JTextField fontcolor;
    private JTextField backgroundcolor;

    private JCheckBox withoutvisiblesign;
    private JCheckBox uselta;
    private JCheckBox overwritesourcefile;
    JTextArea defaultsignmessage;
    private JSpinner signwith;
    private JSpinner signheight;
    private JSpinner fontsize;
    private JSpinner signx;
    private JSpinner signy;
    private JComboBox<String> font;

    private JButton btfontcolor;
    private JButton btbackgroundcolor;
    private JTextField imagetext;
    private JButton btimage;
    Settings settings;
    SettingsManager manager;
    private JSpinner pagenumber;
    private Integer iconsize=32;
    private JCheckBox startserver;
    private JSpinner portnumber;
    private JComboBox<String> fontposition;
	private JCheckBox showlogs;
	private JPanel simplePanel;
	private JPanel advancedPanel;
	private boolean isadvancedoptions = false;
	private JScrollPane configPanel;
	
    private JComboBox<String> padesLevel;
    private JComboBox<String> xadesLevel;
    private JComboBox<String> cadesLevel;
	private JTextField pkcs12text;
	private JButton btpkcs12;
	private JTextField pkcs11moduletext;
	private JButton btpkcs11module;
	private JCheckBox usepkcs12file;
	private JPanel pkcs12panel;
	private JLabel pkcs12label;
	private JPanel advancedbottomspace;
	
	private void createSimpleConfigPanel() {
		simplePanel = new JPanel();
		simplePanel.setBorder(new EmptyBorder(10, 10, 10, 10));
		simplePanel.setLayout(new BoxLayout(simplePanel, 1));
        JPanel checkpanel = new JPanel();
        checkpanel.setAlignmentX(Component.RIGHT_ALIGNMENT);
        checkpanel.setBorder(new EmptyBorder(0, 0, 0, 0));
        checkpanel.setLayout(new BoxLayout(checkpanel, 0));

        withoutvisiblesign = new JCheckBox("Sin Firma Visible             ", this.settings.withoutvisiblesign);

        checkpanel.add(withoutvisiblesign);
        //checkpanel.add(Box.createRigidArea(new Dimension(5, 0)));
        uselta = new JCheckBox("Usar LTA automático", this.settings.uselta);
        checkpanel.add(uselta);
        checkpanel.add(Box.createRigidArea(new Dimension(5, 0)));

		uselta.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent arg0) { changeLTA(); } 
		});
        
        showlogs = new JCheckBox("Ver bitácoras", this.settings.showlogs);
        checkpanel.add(showlogs);
        checkpanel.add(Box.createRigidArea(new Dimension(5, 0)));

        
        simplePanel.add(checkpanel);
        simplePanel.add(Box.createRigidArea(new Dimension(0, 10)));

        JPanel checkpanelserver = new JPanel();
        checkpanelserver.setPreferredSize(new Dimension(450, 30));
        checkpanelserver.setAlignmentX(Component.RIGHT_ALIGNMENT);
        checkpanelserver.setBorder(new EmptyBorder(0, 0, 0, 0));
        checkpanelserver.setLayout(new BoxLayout(checkpanelserver, 0));

        overwritesourcefile = new JCheckBox("Sobreescribir archivo original               ", this.settings.overwritesourcefile);
        checkpanelserver.add(overwritesourcefile);
        checkpanelserver.add(Box.createRigidArea(new Dimension(5, 0)));
        startserver = new JCheckBox("Inicializar firmado remoto", this.settings.startserver);
        checkpanelserver.add(startserver);

        simplePanel.add(checkpanelserver);
        simplePanel.add(Box.createRigidArea(new Dimension(0, 10)));

        reason = new JTextField();
        reason.setText(this.settings.reason);
        place = new JTextField();
        place.setText(this.settings.place);
        contact = new JTextField();
        contact.setText(this.settings.contact);
        dateformat = new JTextField();
        dateformat.setText(this.settings.dateformat);
        dateformat.setToolTipText("Debe ser compatible con formatos de fecha de java");
        defaultsignmessage = new JTextArea();
        defaultsignmessage.setText(this.settings.getDefaultSignMessage());
        defaultsignmessage.setOpaque(false);
        pagenumber = new JSpinner();
        pagenumber.setModel(new SpinnerNumberModel(this.settings.pagenumber, null, null, 1));

        signwith = new JSpinner();
        signwith.setModel(new SpinnerNumberModel(this.settings.signwith, null, null, 1));
        signheight = new JSpinner();
        signheight.setModel(new SpinnerNumberModel(this.settings.signheight, null, null, 1));
        signx = new JSpinner();
        signx.setModel(new SpinnerNumberModel(this.settings.signx, null, null, 1));
        signy = new JSpinner();
        signy.setModel(new SpinnerNumberModel(this.settings.signy, null, null, 1));
        fontsize = new JSpinner();
        fontsize.setModel(new SpinnerNumberModel(this.settings.fontsize, null, null, 1));
        String fonts[] = { Font.SANS_SERIF, Font.DIALOG, Font.DIALOG_INPUT, Font.MONOSPACED, Font.SANS_SERIF, Font.SERIF };
        font = new JComboBox<String>(fonts);

        String fontpositions[] = { "RIGHT", "LEFT", "BOTTOM", "TOP" };
        fontposition = new JComboBox<String>(fontpositions);
        fontposition.setSelectedItem(settings.fontalignment);

        JPanel fontcolorpanel = new JPanel();
        fontcolorpanel.setBorder(new EmptyBorder(0, 0, 0, 0));
        fontcolorpanel.setLayout(new BoxLayout(fontcolorpanel, 0));
        fontcolor = new JTextField();
        fontcolor.setToolTipText("Use la palabra 'transparente' si no desea un color");
        fontcolor.setText(this.settings.fontcolor);


        fontcolor.getDocument().addDocumentListener(new DocumentListener() {
            public void updateIcon(DocumentEvent edoc) {
                try {
                    String text = fontcolor.getText();
                    if(!text.isEmpty() && ! text.equalsIgnoreCase("transparente")) {
                        Color color = Color.decode(text);
                        btfontcolor.setIcon(createImageIcon(color));
                    } else {
                        btfontcolor.setIcon(getTransparentImageIcon());
                    }
                } catch (Exception e) {
                	LOG.error("Error cambiando color de fuente", e);
                }
            }

            @Override
            public void insertUpdate(DocumentEvent e) {
                updateIcon(e);
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                updateIcon(e);
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                updateIcon(e);
            }
        });

        btfontcolor = new JButton("Elegir");
        btfontcolor.setOpaque(false);
        setIcons(btfontcolor, this.settings.fontcolor, this.settings.getFontColor());

        fontcolorpanel.add(btfontcolor);
        fontcolorpanel.add(fontcolor);

        JPanel backgroundcolorpanel = new JPanel();
        backgroundcolorpanel.setBorder(new EmptyBorder(0, 0, 0, 0));
        backgroundcolorpanel.setLayout(new BoxLayout(backgroundcolorpanel, 0));
        backgroundcolor = new JTextField();
        backgroundcolor.setToolTipText("Use la palabra 'transparente' si no desea un color de fondo");

        backgroundcolor.setText(this.settings.backgroundcolor);
        btbackgroundcolor = new JButton("Elegir");
        btbackgroundcolor.setOpaque(false);
        setIcons(btbackgroundcolor, this.settings.backgroundcolor, this.settings.getBackgroundColor());
        backgroundcolorpanel.add(btbackgroundcolor);
        backgroundcolorpanel.add(backgroundcolor);

        backgroundcolor.getDocument().addDocumentListener(new DocumentListener() {
            public void updateIcon(DocumentEvent edoc) {
                try {
                    String text = backgroundcolor.getText();
                    if(!text.isEmpty() && text != "transparente") {
                        Color color = Color.decode(text);
                        btbackgroundcolor.setIcon(createImageIcon(color));
                    } else {
                        btbackgroundcolor.setIcon(getTransparentImageIcon());
                    }
                } catch (Exception e) {
                	LOG.error("Error cambiando color de fondo", e);
                 
                }
            }

            @Override
            public void insertUpdate(DocumentEvent e) {
                updateIcon(e);
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                updateIcon(e);
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                updateIcon(e);
            }
        });

        JPanel imagepanel = new JPanel();
        imagepanel.setBorder(new EmptyBorder(0, 0, 0, 0));
        imagepanel.setLayout(new BoxLayout(imagepanel, 0));
        imagetext = new JTextField();
        btimage = new JButton("Elegir");
        //btimage.setForeground(this.settings.getBackgroundColor());
        if(this.settings.image != null) {
            imagetext.setText(this.settings.image);
            btimage.setIcon(this.getIcon(this.settings.image));
        }
        imagepanel.add(btimage);
        imagepanel.add(imagetext);

        portnumber = new JSpinner();

        portnumber.setModel(new SpinnerNumberModel(this.settings.portnumber, 2000, null, 1));

        portnumber.setEditor(new JSpinner.NumberEditor(portnumber, "0000"));

        addSettingsBox(simplePanel, "Razón:", reason);
        addSettingsBox(simplePanel, "Lugar:", place);
        addSettingsBox(simplePanel, "Contacto:", contact);
        addSettingsBox(simplePanel, "Formato de fecha:", dateformat);
        addSettingsBox(simplePanel, "Mensaje de firma:", defaultsignmessage, new Dimension(150, 50));

        addSettingsBox(simplePanel, "Página inicial:", pagenumber);
        addSettingsBox(simplePanel, "Ancho de firma:", signwith);
        addSettingsBox(simplePanel, "Largo de firma:", signheight);

        addSettingsBox(simplePanel, "Posición inicial X:", signx);
        addSettingsBox(simplePanel, "Posición inicial Y:", signy);
        addSettingsBox(simplePanel, "Tamaño de fuente:", fontsize);
        addSettingsBox(simplePanel, "Fuente:", font);
        addSettingsBox(simplePanel, "Posición de fuente:", fontposition);

        addSettingsBox(simplePanel, "Color de fuente:", fontcolorpanel);
        addSettingsBox(simplePanel, "Color de fondo:", backgroundcolorpanel);
        addSettingsBox(simplePanel, "Imagen de firma:", imagepanel);
        addSettingsBox(simplePanel, "Puerto de escucha:", portnumber);
        
        btfontcolor.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                show_fontcolor_picker();
            }
        });

        btbackgroundcolor.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                show_backgroundcolor_picker();
            }
        });

        btimage.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                show_image_picker();
            }
        });
        configPanel = new JScrollPane(simplePanel, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        configPanel.setPreferredSize(new Dimension(700, 400));
        configPanel.setBorder(null);

        // configPanel.setViewportView(panel);
        configPanel.setOpaque(false);
        configPanel.getViewport().setOpaque(false);

        add(configPanel, BorderLayout.CENTER);
	}
	
	
	private void statePKCS12CheckChange() {
		if(usepkcs12file.isSelected()){
			pkcs12panel.setVisible(true);
			pkcs12label.setVisible(true);
			advancedbottomspace.removeAll();
	        advancedbottomspace.add(Box.createRigidArea(new Dimension(400, 80)));
		}else{
		   pkcs12panel.setVisible(false);
		   pkcs12label.setVisible(false);
		   advancedbottomspace.removeAll();
	       advancedbottomspace.add(Box.createRigidArea(new Dimension(400, 120)));
		}
	}
	
	private void changeLTA() {
		if(uselta.isSelected()){
			padesLevel.setSelectedItem("LTA");
			xadesLevel.setSelectedItem("LTA");
			cadesLevel.setSelectedItem("LTA");
		}
	}
	
	private void createAdvancedConfigPanel() {
		advancedPanel = new JPanel();		
		advancedPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
		advancedPanel.setLayout(new BoxLayout(advancedPanel, 1)); 
		

		
        String padesLeveloptions[] = { "T", "LT", "LTA" };
        padesLevel = new JComboBox<String>(padesLeveloptions);
        padesLevel.setSelectedItem(settings.padesLevel);
        addSettingsBox(advancedPanel, "Nivel PAdES:", padesLevel);
        
        
        String xadesLeveloptions[] = {"T", "LT", "LTA" };
        xadesLevel = new JComboBox<String>(xadesLeveloptions);
        xadesLevel.setSelectedItem(settings.xadesLevel);
        addSettingsBox(advancedPanel, "Nivel XAdES:", xadesLevel);
        
        String cadesLeveloptions[] = {"T", "LT", "LTA"};
        cadesLevel = new JComboBox<String>(cadesLeveloptions);
        cadesLevel.setSelectedItem(settings.cadesLevel);
        addSettingsBox(advancedPanel, "Nivel CAdES:", cadesLevel);
        
        
        JPanel checkpanelpkcs12 = new JPanel();
        checkpanelpkcs12.setPreferredSize(new Dimension(450, 30));
        checkpanelpkcs12.setAlignmentX(Component.RIGHT_ALIGNMENT);
        checkpanelpkcs12.setBorder(new EmptyBorder(0, 0, 0, 0));
        checkpanelpkcs12.setLayout(new BoxLayout(checkpanelpkcs12, 0));

        usepkcs12file = new JCheckBox("Usar archivo pkcs12 para firmar", this.settings.usepkcs12file);
        checkpanelpkcs12.add(usepkcs12file);
        checkpanelpkcs12.add(Box.createRigidArea(new Dimension(5, 0)));
        
        
		usepkcs12file.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent arg0) { statePKCS12CheckChange(); } 
		});
        
        addSettingsBox(advancedPanel, "", checkpanelpkcs12);
        
        pkcs12panel = new JPanel();
        pkcs12panel.setBorder(new EmptyBorder(0, 0, 0, 0));
        pkcs12panel.setLayout(new BoxLayout(pkcs12panel, 0));
        pkcs12text = new JTextField();
        btpkcs12 = new JButton("Elegir");
        //btimage.setForeground(this.settings.getBackgroundColor());
        if(this.settings.pkcs12file != null) {
        	pkcs12text.setText(this.settings.pkcs12file);
             
        }
        pkcs12panel.add(pkcs12text);
        pkcs12panel.add(btpkcs12);
        pkcs12label = addSettingsBox(advancedPanel, "Archivo PKCS12", pkcs12panel);
        
        btpkcs12.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                String path = getFilePath();
                if(path != null) {
                	pkcs12text.setText(path);
                }
            }
        });
        
        
        JPanel pkcs11modulepanel = new JPanel();
        pkcs11modulepanel.setBorder(new EmptyBorder(0, 0, 0, 0));
        pkcs11modulepanel.setLayout(new BoxLayout(pkcs11modulepanel, 0));
        pkcs11moduletext = new JTextField();
        btpkcs11module = new JButton("Elegir");
        //btimage.setForeground(this.settings.getBackgroundColor());
        if(this.settings.extrapkcs11Lib != null) {
        	pkcs11moduletext.setText(this.settings.extrapkcs11Lib);
             
        }
        pkcs11modulepanel.add(pkcs11moduletext);
        pkcs11modulepanel.add(btpkcs11module);
        
        btpkcs11module.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                String path = getFilePath();
                if(path != null) {
                	pkcs11moduletext.setText(path);
                }
            }
        });
        
        addSettingsBox(advancedPanel, "Archivo PKCS11", pkcs11modulepanel);
        advancedPanel.add(new JLabel("El archivo pkcs11 se detecta automáticamente, "));
        advancedPanel.add(new JLabel("pero podrá ser escrito usando el campo anterior"));
        
        advancedbottomspace = new JPanel();
        advancedPanel.add(advancedbottomspace);
        statePKCS12CheckChange();
        changeLTA();
	}
    public ConfigPanel() {
        manager = SettingsManager.getInstance();
        settings = manager.get_and_create_settings();
        setLayout(new BorderLayout(0, 0));

        //JLabel lblValoresPorDefecto = new JLabel("Valores por defecto");
        //lblValoresPorDefecto.setHorizontalAlignment(SwingConstants.CENTER);
        //add(lblValoresPorDefecto, BorderLayout.NORTH);

        this.createSimpleConfigPanel();
        this.createAdvancedConfigPanel();

        JPanel optionswitchpanel = new JPanel();
        add(optionswitchpanel, BorderLayout.NORTH);
        
        JButton showadvanced = new JButton("Opciones Avanzadas"); 
        showadvanced.setOpaque(false);        
        optionswitchpanel.add(showadvanced);
        
        showadvanced.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
            	isadvancedoptions = !isadvancedoptions;
            	if(isadvancedoptions) {
            		showadvanced.setText("Opciones básicas");
            		//configPanel.getViewport().setVisible(false);
            		configPanel.setViewportView(advancedPanel);
            		simplePanel.setVisible(false);
            		advancedPanel.setVisible(true);
            	}else {
            		showadvanced.setText("Opciones Avanzadas");
            		//configPanel.getViewport().setVisible(true);
            		configPanel.setViewportView(simplePanel);
              		advancedPanel.setVisible(false);
            		simplePanel.setVisible(true);
            	}
            }
        });
        
        JPanel btns = new JPanel();
        add(btns, BorderLayout.SOUTH);

        JButton restartbtn = new JButton("Reiniciar");
        restartbtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                restart_settings();
            }
        });
        btns.add(restartbtn);

        JButton applywithoutsave = new JButton("Aplicar sin guardar");
        applywithoutsave.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                charge_settings();
                if(startserver.isSelected()) {
                    showMessage("Modo remoto no se activará, debe guardar y reiniciar la aplicación.");
                }
            }
        });
        btns.add(applywithoutsave);

        JButton btsave = new JButton("Guardar");
        btsave.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                save_settings();
                if(startserver.isSelected()) {
                    showMessage("El Modo remoto se iniciará al reinicio de la aplicación, puede desactivarlo con el menú contextual obtenido con clic derecho.");
                }
            }
        });
        btns.add(btsave);

    }
    public JLabel addSettingsBox(JPanel panel, String text, JComponent item) {
        return this.addSettingsBox(panel, text, item, new Dimension(150, 30));
    }
    public JLabel addSettingsBox(JPanel panel, String text, JComponent item, Dimension d) {
        JLabel label = new JLabel(text);
        JPanel itempanel = new JPanel();
        label.setPreferredSize(new Dimension(150, 30));
        itempanel.setBorder(new EmptyBorder(0, 0, 0, 0));
        itempanel.setLayout(new BoxLayout(itempanel, 0));
        itempanel.add(label);
        itempanel.add(item);
        panel.add(itempanel);
        panel.add(Box.createRigidArea(new Dimension(0, 10)));
        item.setPreferredSize(d);
        return label;
    }
    public void charge_settings() {
        settings.withoutvisiblesign = this.withoutvisiblesign.isSelected();
        settings.reason = reason.getText();
        settings.place = place.getText();
        settings.contact = contact.getText();
        settings.dateformat = this.dateformat.getText();
        settings.defaultsignmessage = defaultsignmessage.getText();
        settings.withoutvisiblesign = withoutvisiblesign.isSelected();
        settings.uselta = uselta.isSelected();
        settings.showlogs = this.showlogs.isSelected();
        settings.overwritesourcefile = overwritesourcefile.isSelected();
        settings.pagenumber = Integer.parseInt(pagenumber.getValue().toString());
        settings.signwith = Integer.parseInt(signwith.getValue().toString());
        settings.signheight = Integer.parseInt(signheight.getValue().toString());
        settings.fontsize = Integer.parseInt(fontsize.getValue().toString());
        settings.signx = Integer.parseInt(signx.getValue().toString());
        settings.signy = Integer.parseInt(signy.getValue().toString());
        settings.font = font.getSelectedItem().toString();
        settings.fontalignment = fontposition.getSelectedItem().toString();
        settings.fontcolor = fontcolor.getText();
        settings.backgroundcolor = backgroundcolor.getText();
        settings.image = imagetext.getText();
        settings.startserver = this.startserver.isSelected();
        settings.portnumber = Integer.parseInt(portnumber.getValue().toString());
        settings.padesLevel = padesLevel.getSelectedItem().toString();
        settings.xadesLevel = xadesLevel.getSelectedItem().toString();
        settings.cadesLevel = cadesLevel.getSelectedItem().toString();
        
        settings.usepkcs12file = usepkcs12file.isSelected();
        settings.pkcs12file = pkcs12text.getText();
        settings.extrapkcs11Lib = pkcs11moduletext.getText();
        if(settings.pkcs12file == "") settings.pkcs12file = null;
        if(settings.extrapkcs11Lib == "") settings.extrapkcs11Lib = null;
        
        settings.updateConfig();
    }

    public void restart_settings() {
        Settings settings = new Settings();

        withoutvisiblesign.setSelected(settings.withoutvisiblesign);
        uselta.setSelected(settings.uselta);
        showlogs.setSelected(settings.showlogs);
        overwritesourcefile.setSelected(settings.overwritesourcefile);
        reason.setText(settings.reason);
        place.setText(settings.place);
        contact.setText(settings.contact);
        dateformat.setText(settings.dateformat);
        defaultsignmessage.setText(settings.defaultsignmessage);
        pagenumber.setValue(settings.pagenumber);
        signwith.setValue(settings.signwith);
        signheight.setValue(settings.signheight);
        signx.setValue(settings.signx);
        signy.setValue(settings.signy);
        fontsize.setValue(settings.fontsize);
        font.setSelectedItem(settings.font);
        fontcolor.setText(settings.fontcolor);
        backgroundcolor.setText(settings.backgroundcolor);
        setIcons(btfontcolor, fontcolor.getText(), this.settings.getFontColor());
        setIcons(btbackgroundcolor, backgroundcolor.getText(), this.settings.getBackgroundColor());
        startserver.setSelected(settings.startserver);
        portnumber.setValue(settings.portnumber);
        fontposition.setSelectedItem(settings.fontalignment);
        //btbackgroundcolor.setIcon(createImageIcon(this.settings.getBackgroundColor()));
        //btfontcolor.setForeground(settings.getFontColor());
        //btfontcolor.setIcon(createImageIcon(this.settings.getFontColor()));

        padesLevel.setSelectedItem(settings.padesLevel);
        xadesLevel.setSelectedItem(settings.xadesLevel);
        cadesLevel.setSelectedItem(settings.cadesLevel);

        if(settings.image != null) {
            imagetext.setText(settings.image);
            btimage.setIcon(this.getIcon(settings.image));
        } else {
            imagetext.setText("");
            btimage.setIcon(createImageIcon(new Color(255, 255, 255, 0)));
        }
        
        if(settings.pkcs12file != null) {  pkcs12text.setText(settings.pkcs12file);
        }else{ pkcs12text.setText(""); };
        
        if(settings.extrapkcs11Lib != null) {  pkcs11moduletext.setText(settings.extrapkcs11Lib);
        }else{ pkcs11moduletext.setText(""); };
        
        usepkcs12file.setSelected(settings.usepkcs12file);
        
        


    }

    private void setIcons(JButton component, String text, Color color) {
        if(text.equalsIgnoreCase("transparente")) {
            component.setIcon(getTransparentImageIcon());
        } else {
          component.setIcon(createImageIcon(color));
        }
    }

    public void save_settings() {
        charge_settings();
        this.manager.setSettings(this.settings, true);
    }

    public void show_fontcolor_picker() {
        Color newColor = JColorChooser.showDialog(this, "Color del texto", this.settings.getFontColor());
        if (newColor != null) {
            //btfontcolor.setForeground(newColor);
            //btfontcolor.setIcon(createImageIcon(newColor));
            String buf = Integer.toHexString(newColor.getRGB());
            String hex = "#"+buf.substring(buf.length()-6);
            fontcolor.setText(hex);
        }
    }

    public void show_backgroundcolor_picker() {
        Color newColor = JColorChooser.showDialog(this, "Color de fondo", this.settings.getBackgroundColor());
        if (newColor != null) {
            //btbackgroundcolor.setIcon(createImageIcon(newColor));
            String buf = Integer.toHexString(newColor.getRGB());
            String hex = "#"+buf.substring(buf.length()-6);
            backgroundcolor.setText(hex);

        }
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
    
    public void show_image_picker() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.addChoosableFileFilter(new ImageFilter());
        fileChooser.setAcceptAllFileFilterUsed(false);
        int option = fileChooser.showOpenDialog(this);
        if(option == JFileChooser.APPROVE_OPTION) {
           File file = fileChooser.getSelectedFile();
           String path = file.getAbsolutePath();
           imagetext.setText(path);
           btimage.setIcon(this.getIcon(path));
        }

    }
    public Icon getIcon(String path) {
        Icon  icon = new ImageIcon(new ImageIcon(path).getImage().getScaledInstance(iconsize, iconsize, Image.SCALE_DEFAULT));
        return icon;
    }

    public Icon getTransparentImageIcon() {
        BufferedImage image = new BufferedImage(iconsize, iconsize, BufferedImage.TYPE_INT_ARGB);
         Graphics2D graphics = image.createGraphics();

         graphics.setColor(new Color(0, 0, 0, 100));

         for (int x=4; x<28; x+=8)
             for (int y=4; y<28; y+=8) graphics.fillRect(x, y, 4, 4);

         graphics.setColor(new Color(130, 130, 130));

         for (int x = 8; x < 28; x += 8)
             for (int y = 8; y < 28; y += 8) graphics.fillRect(x, y, 4, 4);
         //graphics.setBackground(new Color(255, 255, 255, 0));
         return new ImageIcon(image);
    }

    public ImageIcon createImageIcon(Color color) {
        BufferedImage image = new BufferedImage(iconsize, iconsize, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = image.createGraphics();
        graphics.setPaint(new Color(255, 255, 255, 0));
        graphics.setBackground(new Color(255, 255, 255, 0));
        graphics.fillRect (0, 0, iconsize, iconsize);

        graphics.setColor(color);
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        Ellipse2D.Float circle = new Ellipse2D.Float(4F, 6F, iconsize-15, iconsize-15);
        graphics.fill(circle);

        return new ImageIcon(image);
    }

    public void showMessage(String message) {
        JOptionPane.showMessageDialog(null, new CopyableJLabel(message), "Mensaje de Firmador", JOptionPane.INFORMATION_MESSAGE);
    }

}

class ImageFilter extends FileFilter {
   public final static String JPEG = "jpeg";
   public final static String JPG = "jpg";
   public final static String GIF = "gif";
   public final static String TIFF = "tiff";
   public final static String TIF = "tif";
   public final static String PNG = "png";

   @Override
   public boolean accept(File f) {
      if (f.isDirectory()) {
         return true;
      }

      String extension = getExtension(f);
      if (extension != null) {
         if (extension.equals(TIFF) ||
            extension.equals(TIF) ||
            extension.equals(GIF) ||
            extension.equals(JPEG) ||
            extension.equals(JPG) ||
            extension.equals(PNG)) {
            return true;
         } else {
            return false;
         }
      }
      return false;
   }

   @Override
   public String getDescription() {
      return "Image Only";
   }

   String getExtension(File f) {
      String ext = null;
      String s = f.getName();
      int i = s.lastIndexOf('.');

      if (i > 0 &&  i < s.length() - 1) {
         ext = s.substring(i+1).toLowerCase();
      }
      return ext;
   }
}
