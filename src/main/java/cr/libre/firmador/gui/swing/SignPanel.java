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
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.invoke.MethodHandles;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import javax.accessibility.AccessibleState;
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cr.libre.firmador.cards.CardSignInfo;
import cr.libre.firmador.ConfigListener;
import cr.libre.firmador.MessageUtils;
import cr.libre.firmador.Settings;
import cr.libre.firmador.SettingsManager;
import cr.libre.firmador.cards.SmartCardDetector;
import cr.libre.firmador.documents.Document;
import cr.libre.firmador.documents.PreviewerInterface;
import cr.libre.firmador.documents.SupportedMimeTypeEnum;
import cr.libre.firmador.gui.GUIInterface;
import cr.libre.firmador.gui.GUISwing;
import cr.libre.firmador.signers.FirmadorUtils;


public class SignPanel extends JPanel implements ConfigListener{
    final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private static final long serialVersionUID = 945116850482545687L;

    private static class CheckBoxListener implements PropertyChangeListener {
        @Override
        public void propertyChange(PropertyChangeEvent e) {
            String propertyName = e.getPropertyName();
            if ("AccessibleState".equals(propertyName)) {
                AccessibleState state = (AccessibleState) e.getNewValue();
                if (state == AccessibleState.CHECKED) {
                    System.out.println("Se ha seleccionado el JCheckBox");
                } else {
                    System.out.println("Se ha deseleccionado el JCheckBox");
                }
            }
        }
    }

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
    private JButton saveButton;
    private JLabel AdESLevelLabel;
    private JRadioButton levelTButton;
    private JRadioButton levelLTButton;
    private JRadioButton levelLTAButton;
    private ButtonGroup AdESLevelButtonGroup;
    protected Settings settings;
    public GUIInterface gui;
    private SmartCardDetector smartCardDetector;
    private PreviewerInterface preview;
    private Document currentDocument = null;

    public void setGUI(GUIInterface gui) {
        this.gui=gui;
    }

    public void setDocument(Document document) {
        currentDocument = document;
        SupportedMimeTypeEnum mimeType = document.getMimeType();

        hideButtons();
        if (mimeType.isPDF()) {
            showSignButtons();
        } else if (mimeType.isOpenxmlformats()) {
            getSignButton().setEnabled(true);
            saveButton.setEnabled(true);
        } else {
            shownonPDFButtons();
        }
    }

    public void setPreview(PreviewerInterface preview) {
        this.preview = preview;
        int pages = preview.getNumberOfPages();
        if (pages > 0) {
            SpinnerNumberModel model = ((SpinnerNumberModel) this.getPageSpinner().getModel());
            model.setMinimum(1);
            model.setMaximum(pages);
            if (settings.pageNumber <= pages && settings.pageNumber > 0)
                this.getPageSpinner().setValue(settings.pageNumber);
            else
                this.getPageSpinner().setValue(1);
            // signPanel.paintPDFViewer();
        }
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
        signatureVisibleCheckBox = new JCheckBox(MessageUtils.t("signpanel_visible_checkbox"),
                settings.withoutVisibleSign);
        signatureVisibleCheckBox.getAccessibleContext().setAccessibleName(MessageUtils.t("signpanel_visible_checkbox"));
        signatureVisibleCheckBox.getAccessibleContext()
                .setAccessibleDescription(MessageUtils.t("signpanel_visible_checkbox_accessible"));
        signatureVisibleCheckBox.setToolTipText(MessageUtils.t("signpanel_visible_checkbox_tooltip"));
        signatureVisibleCheckBox.setOpaque(false);
        reasonLabel = new JLabel(MessageUtils.t("signpanel_reason"));
        locationLabel = new JLabel(MessageUtils.t("signpanel_place"));
        contactInfoLabel = new JLabel(MessageUtils.t("signpanel_contact"));
        reasonField = new JTextField();
        reasonField.setText(settings.reason);
        reasonField.setToolTipText(MessageUtils.t("signpanel_reason_tooltip"));
        reasonField.getAccessibleContext()
                .setAccessibleDescription(MessageUtils.t("signpanel_reason_tooltip_accessible"));

        locationField = new JTextField();
        locationField.setText(settings.place);
        locationField.setToolTipText(MessageUtils.t("signpanel_place_tooltip"));
        locationField.getAccessibleContext()
                .setAccessibleDescription(MessageUtils.t("signpanel_place_tooltip_accessible"));

        contactInfoField = new JTextField();
        contactInfoField.setText(settings.contact);
        contactInfoField.setToolTipText(MessageUtils.t("signpanel_contact_tooltip"));
        contactInfoField.getAccessibleContext()
                .setAccessibleDescription(MessageUtils.t("signpanel_contact_tooltip_accessible"));

        AdESFormatLabel = new JLabel(MessageUtils.t("signpanel_formato_ades"));
        CAdESButton = new JRadioButton("CAdES");
        CAdESButton.setActionCommand("CAdES");
        CAdESButton.setContentAreaFilled(false);
        XAdESButton = new JRadioButton("XAdES", true);
        XAdESButton.setActionCommand("XAdES");
        XAdESButton.setContentAreaFilled(false);
        AdESFormatButtonGroup = new ButtonGroup();
        AdESFormatButtonGroup.add(CAdESButton);
        AdESFormatButtonGroup.add(XAdESButton);
        AdESLevelLabel = new JLabel(MessageUtils.t("signpanel_level_ades"));
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

        pageLabel = new JLabel(MessageUtils.t("signpanel_pages"));
        pageSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 100, 1));
        pageSpinner.setToolTipText(MessageUtils.t("signpanel_pages_tooltip"));
        pageSpinner.getAccessibleContext()
                .setAccessibleDescription(MessageUtils.t("signpanel_pages_tooltip_accessible"));
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

        signButton = new JButton(MessageUtils.t("signpanel_sign_btn"));
        signButton.setToolTipText(MessageUtils.t("signpanel_sign_tooltip"));
        signButton.getAccessibleContext().setAccessibleDescription(MessageUtils.t("signpanel_sign_tooltip_accessible"));

        signButton.setOpaque(false);
        signButton.setMnemonic(MessageUtils.k('S'));

        saveButton = new JButton(MessageUtils.t("signpanel_save_btn"));
        saveButton.setToolTipText(MessageUtils.t("signpanel_save_tooltip"));
        saveButton.getAccessibleContext().setAccessibleDescription(MessageUtils.t("signpanel_save_tooltip_accessible"));
        saveButton.setOpaque(false);
        saveButton.setMnemonic(MessageUtils.k('G'));
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
            renderPreviewViewer(page - 1);
            //setMinimumSize(getSize());
        } else if (preview.getNumberOfPages() > 0) {
            renderPreviewViewer(1);
        }
    }

    public void renderPreviewViewer(int page) {
        if (preview != null) {
            if (page < preview.getNumberOfPages()) {
                try {
                    BufferedImage pageImage = preview.getPageImage(page);
                    imageLabel.setSize(pageImage.getWidth(), pageImage.getHeight());
                    imageLabel.setIcon(new ImageIcon(pageImage));
                } catch (Throwable ex) {
                    LOG.error(MessageUtils.t("signpanel_log_render_preview"), ex);
                    gui.showError(FirmadorUtils.getRootCause(ex));
                }
                if (preview.showSignLabelPreview()) {
                    previewSignLabel();
                } else {
                    signatureLabel.setVisible(false);
                }
            showPreviewButtons();
        }
        }
    }



    public void previewSignLabel() {
        String previewimg = settings.getImage();
        String table;

        if(previewimg != null) {
            BufferedImage bufferedImage = null;
            try {
                bufferedImage = ImageIO.read(new URL(previewimg));
            } catch (Exception e) {
                LOG.error(MessageUtils.t("signpanel_error_render_image"), e);
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
                String savefile = ((GUISwing) gui).showSaveDialog(currentDocument.getPathName(), "-firmado",
                        currentDocument.getExtension());
                currentDocument.setPathToSave(savefile);
                currentDocument.setSettings(gui.getCurrentSettings());
                gui.signDocument(currentDocument);

            }
        });
        saveButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                if (currentDocument != null) {
                currentDocument.setSettings(gui.getCurrentSettings());
                    gui.showMessage(MessageUtils.t("signpanel_dialog_save_configutation"));
                }
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
                                    .addComponent(signButton).addComponent(saveButton)));

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
                                .addComponent(signButton).addComponent(saveButton)));
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
        saveButton.setVisible(false);

    }

    public void showPreviewButtons() {
        imagePanel.setVisible(true);
        imageLabel.setVisible(true);
        pageLabel.setVisible(true);
        pageSpinner.setVisible(true);
        imagePanel.setBorder(new LineBorder(Color.BLACK));
    }

    public void showSignButtons() {
        showPreviewButtons();
        signatureVisibleCheckBox.setVisible(true);
        saveButton.setVisible(true);
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
         saveButton.setEnabled(true);
         saveButton.setVisible(true);

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
         saveButton.setVisible(false);
    }

    public void updateConfig() {
        signatureVisibleCheckBox.setSelected(settings.withoutVisibleSign);
        reasonField.setText(settings.reason);
        locationField.setText(settings.place);
        contactInfoField.setText(settings.contact);
        signatureLabel.setBounds((int)((float)settings.signX * settings.pDFImgScaleFactor), (int)((float)settings.signY * settings.pDFImgScaleFactor), (int)((float)settings.signWidth * settings.pDFImgScaleFactor), (int)((float)settings.signHeight * settings.pDFImgScaleFactor));

        try {
            if (preview != null) {
                int pages = preview.getNumberOfPages();
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
            if (smartCardDetector == null)
                smartCardDetector = new SmartCardDetector();
            List<CardSignInfo> cards = smartCardDetector.readSaveListSmartCard();
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
