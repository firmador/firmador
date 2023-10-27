/* Firmador is a program to sign documents using AdES standards.

Copyright (C) Firmador authors.

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
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.image.BufferedImage;
import java.lang.invoke.MethodHandles;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import javax.imageio.ImageIO;
import javax.swing.ButtonGroup;
import javax.swing.GroupLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.LineBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cr.libre.firmador.ConfigListener;
import cr.libre.firmador.FirmadorUtils;
import cr.libre.firmador.Settings;
import cr.libre.firmador.SettingsManager;
import cr.libre.firmador.cards.CardSignInfo;
import cr.libre.firmador.cards.SmartCardDetector;
import cr.libre.firmador.gui.GUIInterface;


public class SignPanel extends JPanel implements ConfigListener{
    final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private static final long serialVersionUID = 945116850482545687L;

    private JScrollPane imgScroll;
    private ScrollableJPanel imagePanel;
    private JLabel imageLabel;
    private JLabel signatureLabel;
    private JCheckBox signatureVisibleCheckBox;
    private JLabel reasonLabel;
    private JLabel locationLabel;
    private JLabel contactInfoLabel;
    private JTextField reasonField;
    private JTextField locationField;
    private JTextField contactInfoField;
    private JLabel pageLabel;
    private JSpinner pageSpinner;
    private JLabel AdESFormatLabel;
    private ButtonGroup AdESFormatButtonGroup;
    private JRadioButton CAdESButton;
    private JRadioButton XAdESButton;
    private JButton signButton;
    private JLabel AdESLevelLabel;
    private JRadioButton levelTButton;
    private JRadioButton levelLTButton;
    private JRadioButton levelLTAButton;
    private ButtonGroup AdESLevelButtonGroup;
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

    public JScrollPane getImageScrollPane(ScrollableJPanel panel) {
        JScrollPane imgScrollPane = new JScrollPane();
        imgScrollPane.setPreferredSize(new Dimension(100, 200));
        imgScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        imgScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        imgScrollPane.setBorder(null);
        imgScrollPane.setViewportView(panel);
        imgScrollPane.setOpaque(false);
        imgScrollPane.getViewport().setOpaque(false);
        imgScrollPane.setVisible(true);
        return imgScrollPane;
    }

    public SignPanel(){
        super();
        settings = SettingsManager.getInstance().getAndCreateSettings();
        signatureVisibleCheckBox = new JCheckBox(" Sin firma visible", settings.withoutVisibleSign);
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

        signatureLabel = new JLabel();
        // FIXME partially dead code?
        signatureLabel.setFont(new Font(settings.getFontName(settings.font, false), settings.getFontStyle(settings.font), settings.fontSize));
        signatureLabel.setText("<html><span style='font-size: '"+settings.fontSize * settings.pDFImgScaleFactor+"pt'" +
                "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;FIRMA<br>" +
                "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;VISIBLE</span></html>");
        signatureLabel.setForeground(new Color(0, 0, 0, 0));
        signatureLabel.setBackground(new Color(127, 127, 127, 127));
        signatureLabel.setOpaque(true);

        imagePanel = new ScrollableJPanel(false, false);

        imageLabel = new JLabel();
        signatureLabel.setBounds((int)((float)settings.signX * settings.pDFImgScaleFactor), (int)((float)settings.signY * settings.pDFImgScaleFactor), (int)((float)settings.signWidth * settings.pDFImgScaleFactor), (int)((float)settings.signHeight * settings.pDFImgScaleFactor));

        imageLabel.add(signatureLabel);
        imagePanel.add(imageLabel);
        imgScroll = this.getImageScrollPane(imagePanel);

        signButton = new JButton("Firmar documento");
        signButton.setToolTipText("<html>Este botón permite firmar el documento seleccionado.<br>Requiere dispositivo de Firma Digital al cual se le<br>solicitará ingresar el PIN.</html>");
        signButton.setOpaque(false);


        //signatureLabel.setToolTipText("<html>Esta etiqueta es un recuadro arrastrable que representa<br>la ubicación de la firma visible en la página seleccionada.<br><br>Se puede cambiar su posición haciendo clic sobre el recuadro<br>y moviendo el mouse sin soltar el botón de clic<br>hasta soltarlo en la posición deseada.</html>");
        if (System.getProperty("os.name").startsWith("Mac")) signatureLabel.setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
        else signatureLabel.setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));

        this.setOpaque(false);

        //initializeActions();
    }


    public Rectangle calculateSignatureRectangle() {
        Rectangle reg = new Rectangle(getPDFVisibleSignatureX(),
                                      getPDFVisibleSignatureY(),
                                      signatureLabel.getWidth(),
                                      signatureLabel.getHeight());

        return reg;

    }

    public int getPDFVisibleSignatureX() {
        return (int)((float)signatureLabel.getLocation().x / settings.pDFImgScaleFactor);
    }

    public int getPDFVisibleSignatureY() {
        return (int)((float)signatureLabel.getLocation().y / settings.pDFImgScaleFactor);
    }

    public void paintPDFViewer() {
        int page = (int)pageSpinner.getValue();
        if (page > 0) {
            renderPDFViewer(page - 1);
            //setMinimumSize(getSize());


        }
    }

    public void renderPDFViewer(int page) {
      try {
        pageImage = renderer.renderImage(page, settings.pDFImgScaleFactor);
        imageLabel.setSize(pageImage.getWidth(), pageImage.getHeight());
        imageLabel.setIcon(new ImageIcon(pageImage));
      } catch (Exception ex) {
          LOG.error("Error cambiando cambiando página", ex);
          gui.showError(FirmadorUtils.getRootCause(ex));
      }
      previewSignLabel();
    }



    public void previewSignLabel() {
        String previewimg = settings.getImage();
        String table;

        if(previewimg != null) {
            BufferedImage bufferedImage = null;
            try {
                bufferedImage = ImageIO.read(new URL(previewimg));
            } catch (Exception e) {
                LOG.error("Error cargando imagen", e);
                gui.showError(FirmadorUtils.getRootCause(e));
            }
            int previewimgWidth = Math.round((float) bufferedImage.getWidth() * settings.pDFImgScaleFactor / 4);
            int previewimgHeight = Math.round((float) bufferedImage.getHeight() * settings.pDFImgScaleFactor / 4);
            LOG.info("Imagen: " + bufferedImage.getWidth() + "x" + bufferedImage.getHeight() + " (" + previewimgWidth + "x" + previewimgHeight + ")");

            table = "<table cellpadding=0 cellspacing=0 border=0>";
            if(settings.fontAlignment.contains("BOTTOM")) {
                table += "<tr><td><img src=\""+settings.getImage()+"\" width=\""+previewimgWidth+"\" height=\""+previewimgHeight+"\"></td></tr>";
                table += "<tr><td><span style='font-size: "+settings.fontSize * settings.pDFImgScaleFactor+"pt'>"+getTextExample()+"</span></td></tr>";
            }
            else if(settings.fontAlignment.contains("LEFT")) {
                table += "<tr><td><span style='font-size: "+settings.fontSize * settings.pDFImgScaleFactor+"pt'>"+getTextExample()+"</span></td>";
                table += "<td><img src=\""+settings.getImage()+"\" width=\""+previewimgWidth+"\" height=\""+previewimgHeight+"\"></td></tr>";
            }else if(settings.fontAlignment.contains("TOP")) {
                table += "<tr><td><span style='font-size: "+settings.fontSize * settings.pDFImgScaleFactor+"pt'>"+getTextExample()+"</span></td></tr>";
                table += "<tr><td><img src=\""+settings.getImage()+"\" width=\""+previewimgWidth+"\" height=\""+previewimgHeight+"\"></td></tr>";
            }else {
                table += "<tr><td><img src=\""+settings.getImage()+"\" width=\""+previewimgWidth+"\" height=\""+previewimgHeight+"\"></td>";
                table += "<td><span style='font-size: "+settings.fontSize * settings.pDFImgScaleFactor+"pt'>"+getTextExample()+"</span></td></tr>";
            }
            table += "</table>";
        }else {
            table ="<span style='font-size: "+settings.fontSize * settings.pDFImgScaleFactor+"pt'>"+getTextExample()+"</span>";
        }
        signatureLabel.setFont(new Font(settings.getFontName(settings.font, false), settings.getFontStyle(settings.font), settings.fontSize));
        signatureLabel.setText("<html>"+table+"</html>");
           signatureLabel.setForeground(new Color(0, 0, 0, 0));
        signatureLabel.setBackground(new Color(127, 127, 127, 127));
        signatureLabel.setOpaque(true);
        signatureLabel.setSize(signatureLabel.getPreferredSize());

    }

    public void initializeActions(){
        imageLabel.addMouseMotionListener(new MouseMotionAdapter() {
            public void mouseDragged(MouseEvent e) {
                signatureLabel.setLocation(e.getX() - signatureLabel.getWidth() / 2, e.getY() - signatureLabel.getHeight() / 2);
            }
        });
        imageLabel.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent evt) {
                if(evt.getClickCount()==3) {
                    previewSignLabel();
                }else if (evt.getClickCount() == 2) {
                     signatureLabel.setLocation(evt.getX() - signatureLabel.getWidth() / 2, evt.getY() - signatureLabel.getHeight() / 2);
                }
            }
        });

        pageSpinner.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                    paintPDFViewer();
            }
        });

        signButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                /*boolean ok =*/ gui.signDocuments();
            }
        });


    }

    public void createLayout(GroupLayout signLayout, JPanel signPanel) {
        this.setOpaque(false);
        this.setLayout(signLayout);
        signLayout.setAutoCreateGaps(true);
        signLayout.setAutoCreateContainerGaps(true);
            signLayout.setHorizontalGroup(
            signLayout.createSequentialGroup()
                .addComponent(imgScroll, 200,  600,  GroupLayout.DEFAULT_SIZE)
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
                .addComponent(imgScroll)
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
        imagePanel.setVisible(true);
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
        imagePanel.setBorder(new LineBorder(Color.BLACK));
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
    
    public void showOpenFormatButtons() {
    	signButton.setEnabled(true);
    }

    public void docHideButtons() {
         imagePanel.setVisible(false);
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
        signatureVisibleCheckBox.setSelected(settings.withoutVisibleSign);
        reasonField.setText(settings.reason);
        locationField.setText(settings.place);
        contactInfoField.setText(settings.contact);
        signatureLabel.setBounds((int)((float)settings.signX * settings.pDFImgScaleFactor), (int)((float)settings.signY * settings.pDFImgScaleFactor), (int)((float)settings.signWidth * settings.pDFImgScaleFactor), (int)((float)settings.signHeight * settings.pDFImgScaleFactor));

        try {
             if (doc != null) {
                 int pages = doc.getNumberOfPages();
                if (settings.pageNumber <= pages && settings.pageNumber > 0) {
                    pageSpinner.setValue(settings.pageNumber);
                } else {
                    pageSpinner.setValue(1);
                }
                paintPDFViewer();
            }
        } catch (Exception e) {
            LOG.error("Error actualizando configuración", e);
            e.printStackTrace();
            pageSpinner.setValue(0);
        }
    }

    public JCheckBox getSignatureVisibleCheckBox() {
        return signatureVisibleCheckBox;
    }

    public void setSignatureVisibleCheckBox(JCheckBox signatureVisibleCheckBox) {
        this.signatureVisibleCheckBox = signatureVisibleCheckBox;
    }

    public JTextField getReasonField() {
        return reasonField;
    }

    public void setReasonField(JTextField reasonField) {
        this.reasonField = reasonField;
    }

    public JTextField getLocationField() {
        return locationField;
    }

    public void setLocationField(JTextField locationField) {
        this.locationField = locationField;
    }

    public JTextField getContactInfoField() {
        return contactInfoField;
    }

    public void setContactInfoField(JTextField contactInfoField) {
        this.contactInfoField = contactInfoField;
    }

    public JSpinner getPageSpinner() {
        return pageSpinner;
    }

    public void setPageSpinner(JSpinner pageSpinner) {
        this.pageSpinner = pageSpinner;
    }

    public JRadioButton getXAdESButton() {
        return XAdESButton;
    }

    public void setXAdESButton(JRadioButton xAdESButton) {
        XAdESButton = xAdESButton;
    }

    public JButton getSignButton() {
        return signButton;
    }

    public void setSignButton(JButton signButton) {
        this.signButton = signButton;
    }

    public JRadioButton getLevelTButton() {
        return levelTButton;
    }

    public void setLevelTButton(JRadioButton levelTButton) {
        this.levelTButton = levelTButton;
    }

    public JRadioButton getLevelLTButton() {
        return levelLTButton;
    }

    public void setLevelLTButton(JRadioButton levelLTButton) {
        this.levelLTButton = levelLTButton;
    }

    public JRadioButton getLevelLTAButton() {
        return levelLTAButton;
    }

    public void setLevelLTAButton(JRadioButton levelLTAButton) {
        this.levelLTAButton = levelLTAButton;
    }

    public BufferedImage getPageImage() {
        return pageImage;
    }

    public void setPageImage(BufferedImage pageImage) {
        this.pageImage = pageImage;
    }

    public PDFRenderer getRenderer() {
        return renderer;
    }

    public void setRenderer(PDFRenderer renderer) {
        this.renderer = renderer;
    }

    public JLabel getImageLabel() {
        return imageLabel;
    }

    public void setImageLabel(JLabel imageLabel) {
        this.imageLabel = imageLabel;
    }

    public JLabel getSignatureLabel() {
        return signatureLabel;
    }

    public void setSignatureLabel(JLabel signatureLabel) {
        this.signatureLabel = signatureLabel;
    }

    public ButtonGroup getAdESLevelButtonGroup() {
        return AdESLevelButtonGroup;
    }

    public void setAdESLevelButtonGroup(ButtonGroup adESLevelButtonGroup) {
        AdESLevelButtonGroup = adESLevelButtonGroup;
    }

    public JLabel getAdESFormatLabel() {
        return AdESFormatLabel;
    }

    public void setAdESFormatLabel(JLabel adESFormatLabel) {
        AdESFormatLabel = adESFormatLabel;
    }

    public ButtonGroup getAdESFormatButtonGroup() {
        return AdESFormatButtonGroup;
    }

    public void setAdESFormatButtonGroup(ButtonGroup adESFormatButtonGroup) {
        AdESFormatButtonGroup = adESFormatButtonGroup;
    }

    public String getTextExample() {
        //String dev="";
        String reason = reasonField.getText();
        String location = locationField.getText();
        String contactInfo = contactInfoField.getText();
        Boolean hasReason = false;
        Boolean hasLocation = false;
        Boolean hasContact = false;
        String commonName="NOMBRE DE LA PERSONA (TIPO DE CERTIFICADO)";
        String identification="XXX-XXXXXXXXXXXX";
        String organization="TIPO DE PERSONA";
        String additionalText = new String();
        try {
            SmartCardDetector cardd = new SmartCardDetector();
            List<CardSignInfo> cards = cardd.readSaveListSmartCard();
            if(!cards.isEmpty()) {
                CardSignInfo card = cards.get(0);
                commonName = card.getCommonName();
                organization = card.getOrganization();
                identification = card.getIdentification();
            }
        } catch (Throwable te) {}
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern(settings.dateFormat);
        LocalDateTime now = LocalDateTime.now();
        additionalText = commonName+"<br>"+organization+", "+identification+".<br>Fecha declarada: "+dtf.format(now)+"<br>";
        if (reason != null && !reason.trim().isEmpty()) {
            hasReason = true;
            additionalText += "Razón: " + reason + "\n";
        }
        if (location != null && !location.trim().isEmpty()) {
            hasLocation = true;
            additionalText += "Lugar: " + location;
        }
        if (contactInfo != null && !contactInfo .trim().isEmpty()) {
            hasContact=true;
            additionalText += "  Contacto: " + contactInfo;
        }
        if (!(hasReason || hasLocation ||hasContact )) {
            additionalText += settings.getDefaultSignMessage();
        }

        additionalText = additionalText.replace("\n", "<br>");

        return additionalText;
    }
}
