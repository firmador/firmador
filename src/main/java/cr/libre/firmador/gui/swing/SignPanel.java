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

import java.awt.Color;
import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.image.BufferedImage;

import javax.swing.ButtonGroup;
import javax.swing.GroupLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;

import com.google.common.base.Throwables;

import cr.libre.firmador.ConfigListener;
import cr.libre.firmador.Settings;
import cr.libre.firmador.SettingsManager;
import cr.libre.firmador.gui.GUIInterface;

public class SignPanel extends JPanel implements ConfigListener{
	private static final long serialVersionUID = 945116850482545687L;


	public JLabel imageLabel;
    public JLabel signatureLabel;
    public JCheckBox signatureVisibleCheckBox;
    public JLabel reasonLabel;
    public JLabel locationLabel;
    public JLabel contactInfoLabel;
    public JTextField reasonField;
    public JTextField locationField;
    public JTextField contactInfoField;
    public JLabel pageLabel;
    public JSpinner pageSpinner;
    public JLabel AdESFormatLabel;
    public ButtonGroup AdESFormatButtonGroup;
    public JRadioButton CAdESButton;
    public JRadioButton XAdESButton;
    public JButton signButton;
    public JLabel AdESLevelLabel;
    public JRadioButton levelTButton;
    public JRadioButton levelLTButton;
    public JRadioButton levelLTAButton;
    public ButtonGroup AdESLevelButtonGroup;
    protected Settings settings;
    private PDDocument doc;
    public BufferedImage pageImage;
    private PDFRenderer renderer;
    public GUIInterface gui;
    
    public void setGUI(GUIInterface gui) {
    	this.gui=gui;
    }
    
    public PDFRenderer getRender(PDDocument doc) {
    	renderer = new PDFRenderer(doc);
    	return renderer;
    }
    
    public void setDoc(PDDocument doc){
    	this.doc = doc;
    	
    }
    
	public SignPanel(){
		super();
		settings = SettingsManager.getInstance().get_and_create_settings();
        signatureVisibleCheckBox = new JCheckBox(" Sin firma visible", settings.withoutvisiblesign);
        signatureVisibleCheckBox.setToolTipText("<html>Marque esta casilla si no desea representar visualmente una firma<br>en una página del documento a la hora de firmarlo.</html>");
        signatureVisibleCheckBox.setOpaque(false);
        reasonLabel = new JLabel("Razón:");
        locationLabel = new JLabel("Lugar:");
        contactInfoLabel = new JLabel("Contacto:");
        reasonField = new JTextField();
        reasonField.setText(settings.reason);
        reasonField.setToolTipText("<html>Este campo opcional permite indicar una razón<br>o motivo por el cual firma el documento.</html>");
        locationField = new JTextField();
        locationField.setText(settings.place);
        locationField.setToolTipText("<html>Este campo opcional permite indicar el lugar físico,<br>por ejemplo la ciudad, en la cual declara firmar.</html>");
        contactInfoField = new JTextField();
        contactInfoField.setText(settings.contact);
        contactInfoField.setToolTipText("<html>Este campo opcional permite indicar una<br>manera de contactar con la persona firmante,<br>por ejemplo una dirección de correo electrónico.</html>");

        AdESFormatLabel = new JLabel("Formato AdES:");
        CAdESButton = new JRadioButton("CAdES");
        CAdESButton.setActionCommand("CAdES");
        CAdESButton.setContentAreaFilled(false);
        XAdESButton = new JRadioButton("XAdES", true);
        XAdESButton.setActionCommand("XAdES");
        XAdESButton.setContentAreaFilled(false);
        AdESFormatButtonGroup = new ButtonGroup();
        AdESFormatButtonGroup.add(CAdESButton);
        AdESFormatButtonGroup.add(XAdESButton);
        AdESLevelLabel = new JLabel("Nivel AdES:");
        levelTButton = new JRadioButton("T");
        levelTButton.setActionCommand("T");
        levelTButton.setContentAreaFilled(false);
        levelLTButton = new JRadioButton("LT");
        levelLTButton.setActionCommand("LT");
        levelLTButton.setContentAreaFilled(false);
        levelLTAButton = new JRadioButton("LTA", true);
        levelLTAButton.setActionCommand("LTA");
        levelLTAButton.setContentAreaFilled(false);
        AdESLevelButtonGroup = new ButtonGroup();
        AdESLevelButtonGroup.add(levelTButton);
        AdESLevelButtonGroup.add(levelLTButton);
        AdESLevelButtonGroup.add(levelLTAButton);

        pageLabel = new JLabel("Página:");
        pageSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 100, 1));
        pageSpinner.setToolTipText("<html>Este control permite seleccionar el número de página<br>para visualizar y seleccionar en cuál mostrar la firma visible.</html>");
        pageSpinner.setMaximumSize(pageSpinner.getPreferredSize());

        
        signatureLabel = new JLabel("<html><span style='font-size: 12pt'>" +
                "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;FIRMA<br>" +
                "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;VISIBLE</span></html>");
        signatureLabel.setForeground(new Color(0, 0, 0, 0));
        signatureLabel.setBackground(new Color(127, 127, 127, 127));
        signatureLabel.setOpaque(true);

        imageLabel = new JLabel();
        signatureLabel.setBounds(settings.signx, settings.signy, settings.signwith, settings.signheight);
        imageLabel.add(signatureLabel);
        
        signButton = new JButton("Firmar documento");
        signButton.setToolTipText("<html>Este botón permite firmar el documento seleccionado.<br>Requiere dispositivo de Firma Digital al cual se le<br>solicitará ingresar el PIN.</html>");
        

        //signatureLabel.setToolTipText("<html>Esta etiqueta es un recuadro arrastrable que representa<br>la ubicación de la firma visible en la página seleccionada.<br><br>Se puede cambiar su posición haciendo clic sobre el recuadro<br>y moviendo el mouse sin soltar el botón de clic<br>hasta soltarlo en la posición deseada.</html>");
        if (System.getProperty("os.name").startsWith("Mac")) signatureLabel.setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
        else signatureLabel.setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
        
        this.setOpaque(false);
        
        //initializeActions();
	}
	
	public void initializeActions(){
        imageLabel.addMouseMotionListener(new MouseMotionAdapter() {
            public void mouseDragged(MouseEvent e) {
                signatureLabel.setLocation(e.getX() - signatureLabel.getWidth() / 2, e.getY() - signatureLabel.getHeight() / 2);
            }
        });
        
        pageSpinner.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                try {
                    int page = (int)pageSpinner.getValue();
                    if (page > 0) {
                    	pageImage = renderer.renderImage(page - 1, 1 / 1.5f);
                    	imageLabel.setIcon(new ImageIcon(pageImage));
                    	//panel.pack();
                    	setMinimumSize(getSize());
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                    gui.showError(Throwables.getRootCause(ex));
                }
            }
        });
        
        signButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
				boolean ok = gui.signDocuments();
            }
        });
        
        
	}
	
	public void signLayout(GroupLayout signLayout, JPanel signPanel) {
        this.setLayout(signLayout);
        signLayout.setAutoCreateGaps(true);
        signLayout.setAutoCreateContainerGaps(true);
            signLayout.setHorizontalGroup(
            signLayout.createSequentialGroup()
                .addComponent(imageLabel)
                .addGroup(signLayout.createParallelGroup(GroupLayout.Alignment.TRAILING)
                    .addComponent(pageLabel)
                    .addComponent(reasonLabel)
                    .addComponent(locationLabel)
                    .addComponent(contactInfoLabel)
                    .addComponent(AdESFormatLabel)
                    .addComponent(AdESLevelLabel))
                .addGroup(signLayout.createParallelGroup()
                    .addGroup(signLayout.createSequentialGroup()
                        .addComponent(pageSpinner)
                        .addComponent(signatureVisibleCheckBox))
                    .addComponent(reasonField)
                    .addComponent(locationField)
                    .addComponent(contactInfoField)
                    .addGroup(signLayout.createSequentialGroup()
                        .addComponent(CAdESButton)
                        .addComponent(XAdESButton))
                    .addGroup(signLayout.createSequentialGroup()
                        .addComponent(levelTButton)
                        .addComponent(levelLTButton)
                        .addComponent(levelLTAButton))
                    .addComponent(signButton)));
        signLayout.setVerticalGroup(
            signLayout.createParallelGroup()
                .addComponent(imageLabel)
                .addGroup(signLayout.createSequentialGroup()
                    .addGroup(signLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                        .addComponent(pageSpinner)
                        .addComponent(signatureVisibleCheckBox)
                        .addComponent(pageLabel))
                    .addGroup(signLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                        .addComponent(reasonField)
                        .addComponent(reasonLabel))
                    .addGroup(signLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                        .addComponent(locationField)
                        .addComponent(locationLabel))
                    .addGroup(signLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                        .addComponent(contactInfoField)
                        .addComponent(contactInfoLabel))
                    .addGroup(signLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                        .addComponent(AdESFormatLabel)
                        .addComponent(CAdESButton)
                        .addComponent(XAdESButton))
                    .addGroup(signLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                        .addComponent(AdESLevelLabel)
                        .addComponent(levelTButton)
                        .addComponent(levelLTButton)
                        .addComponent(levelLTAButton))
                    .addComponent(signButton)));
	}
	
	public void hideButtons() {
        signButton.setEnabled(false);
        pageLabel.setVisible(false);
        pageSpinner.setVisible(false);
        signatureVisibleCheckBox.setVisible(false);
        reasonLabel.setVisible(false);
        reasonField.setVisible(false);
        locationLabel.setVisible(false);
        locationField.setVisible(false);
        contactInfoLabel.setVisible(false);
        contactInfoField.setVisible(false);
        AdESFormatLabel.setVisible(false);
        CAdESButton.setVisible(false);
        XAdESButton.setVisible(false);
        AdESLevelLabel.setVisible(false);
        levelTButton.setVisible(false);
        levelLTButton.setVisible(false);
        levelLTAButton.setVisible(false);
        
	}
	
	public void showSignButtons() {
        imageLabel.setVisible(true);
        pageLabel.setVisible(true);
        pageSpinner.setVisible(true);
        signatureVisibleCheckBox.setVisible(true);
        reasonLabel.setVisible(true);
        reasonField.setVisible(true);
        locationLabel.setVisible(true);
        locationField.setVisible(true);
        contactInfoLabel.setVisible(true);
        contactInfoField.setVisible(true);
/*
        AdESLevelLabel.setVisible(true);
        levelTButton.setVisible(true);
        levelLTButton.setVisible(true);
        levelLTAButton.setVisible(true);
*/
	}
	
	public void shownonPDFButtons() {
		 AdESFormatLabel.setVisible(true);
         CAdESButton.setVisible(true);
         XAdESButton.setVisible(true);
         AdESLevelLabel.setVisible(false);
         levelTButton.setVisible(false);
         levelLTButton.setVisible(false);
         levelLTAButton.setVisible(false);
         signButton.setEnabled(true);
	}
	
	public void docHideButtons() {
		 imageLabel.setVisible(false);
         pageLabel.setVisible(false);
         pageSpinner.setVisible(false);
         signatureVisibleCheckBox.setVisible(false);
         reasonLabel.setVisible(false);
         reasonField.setVisible(false);
         locationLabel.setVisible(false);
         locationField.setVisible(false);
         contactInfoLabel.setVisible(false);
         contactInfoField.setVisible(false);
         AdESFormatLabel.setVisible(false);
         CAdESButton.setVisible(false);
         XAdESButton.setVisible(false);
         AdESLevelLabel.setVisible(false);
         levelTButton.setVisible(false);
         levelLTButton.setVisible(false);
         levelLTAButton.setVisible(false);
	}
	
    public void updateConfig() {
        signatureVisibleCheckBox.setSelected(settings.withoutvisiblesign);
        reasonField.setText(settings.reason);
        locationField.setText(settings.place);
        contactInfoField.setText(settings.contact);
        signatureLabel.setBounds(settings.signx, settings.signy, settings.signwith, settings.signheight);

        try {
             if (doc != null) {
                 int pages = doc.getNumberOfPages();
                if (settings.pagenumber <= pages && settings.pagenumber > 0) {
                    pageSpinner.setValue(settings.pagenumber);
                } else {
                    pageSpinner.setValue(1);
                }
            }
        } catch (Exception e) {
            pageSpinner.setValue(0);
        }
    }
	
}
