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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FileDialog;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.KeyboardFocusManager;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.lang.invoke.MethodHandles;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileFilter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cr.libre.firmador.Settings;
import cr.libre.firmador.SettingsManager;

public class ConfigPanel extends ScrollableJPanel {
    final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    JTextArea defaultSignMessage;
    JScrollPane scrollableDefaultSignMessage;
    Settings settings;
    SettingsManager manager;
    private Integer iconSize = 32;
    private JButton btFontColor, btBackgroundColor, btImage, btPKCS11Module;
    private JCheckBox withoutVisibleSign,/* useLTA,*/ overwriteSourceFile,/* startServer,*/ showLogs;
    private JComboBox<String> font, fontPosition, pAdESLevel, xAdESLevel, cAdESLevel;
    private JPanel advancedBottomSpace;
    private JScrollPane configPanel;
    private JSpinner signWidth, signHeight, fontSize, signX, signY, pageNumber, portNumber;
    private JTextField reason, place, contact, dateFormat, fontColor, backgroundColor, imageText, pKCS11ModuleText, pDFImgScaleFactor;
    private Pkcs12ConfigPanel pKCS12Panel;
    private PluginManagerPlugin pluginsActive;
    private ScrollableJPanel simplePanel, advancedPanel;
    private boolean isAdvancedOptions = false;
    private static final long serialVersionUID = 1L;

    private void createSimpleConfigPanel() {
        simplePanel = new ScrollableJPanel();
        simplePanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        simplePanel.setLayout(new BoxLayout(simplePanel, 1));
        JPanel checkpanel = new JPanel();
        checkpanel.setAlignmentX(Component.RIGHT_ALIGNMENT);
        checkpanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        checkpanel.setLayout(new BoxLayout(checkpanel, 0));
        withoutVisibleSign = new JCheckBox("Sin firma visible        ", this.settings.withoutVisibleSign);
        checkpanel.add(withoutVisibleSign);
        /*
        useLTA = new JCheckBox("Usar LTA automático", this.settings.useLTA);
        checkpanel.add(useLTA);
        useLTA.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent arg0) {
                changeLTA();
            }
        });
        */
        showLogs = new JCheckBox("Ver bitácoras        ", this.settings.showLogs);
        checkpanel.add(showLogs);
        simplePanel.add(checkpanel);
        overwriteSourceFile = new JCheckBox("Sobrescribir archivo original", this.settings.overwriteSourceFile);
        checkpanel.add(overwriteSourceFile);
        /*
        startServer = new JCheckBox("Inicializar firmado remoto", this.settings.startServer);
        checkpanel.add(startServer);
        */
        reason = new JTextField();
        reason.setText(this.settings.reason);
        place = new JTextField();
        place.setText(this.settings.place);
        contact = new JTextField();
        contact.setText(this.settings.contact);
        dateFormat = new JTextField();
        dateFormat.setText(this.settings.dateFormat);
        dateFormat.setToolTipText("Debe ser compatible con formatos de fecha de java");
        defaultSignMessage = new JTextArea();
        defaultSignMessage.setText(this.settings.getDefaultSignMessage());
        defaultSignMessage.setFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS, /* forward traversal textarea with tab */
            KeyboardFocusManager.getCurrentKeyboardFocusManager().getDefaultFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS));
        defaultSignMessage.setFocusTraversalKeys(KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS, /* reverse traversal textarea with shift+tab */
            KeyboardFocusManager.getCurrentKeyboardFocusManager().getDefaultFocusTraversalKeys(KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS));
        scrollableDefaultSignMessage = new JScrollPane(defaultSignMessage);
        if (UIManager.getLookAndFeel().getClass().getName().contains("GTKLookAndFeel")) {
            defaultSignMessage.setBorder(reason.getBorder()); // Add text margins like in text fields for GTK
            scrollableDefaultSignMessage.setViewportBorder(BorderFactory.createTitledBorder("")); // Add border to textarea for GTK
        }
        if (UIManager.getLookAndFeel().getClass().getName().contains("WindowsLookAndFeel"))
            defaultSignMessage.setFont(reason.getFont()); // Windows defaults to fixed width font (Courier), use same as jTextField
        pageNumber = new JSpinner();
        pageNumber.setModel(new SpinnerNumberModel(this.settings.pageNumber, null, null, 1));
        signWidth = new JSpinner();
        signWidth.setModel(new SpinnerNumberModel(this.settings.signWidth, null, null, 1));
        signHeight = new JSpinner();
        signHeight.setModel(new SpinnerNumberModel(this.settings.signHeight, null, null, 1));
        signX = new JSpinner();
        signX.setModel(new SpinnerNumberModel(this.settings.signX, null, null, 1));
        signY = new JSpinner();
        signY.setModel(new SpinnerNumberModel(this.settings.signY, null, null, 1));
        fontSize = new JSpinner();
        fontSize.setModel(new SpinnerNumberModel(this.settings.fontSize, null, null, 1));
        String fonts[];
        String osName = System.getProperty("os.name").toLowerCase();
        if (osName.contains("mac")) fonts = new String[] {
            "Helvetica Regular", "Helvetica Oblique", "Helvetica Bold", "Helvetica Bold Oblique",
            "Times New Roman Regular", "Times New Roman Italic", "Times New Roman Bold", "Times New Roman Bold Italic",
            "Courier New Regular", "Courier New Italic", "Courier New Bold", "Courier New Bold Italic"
        };
        else if (osName.contains("linux")) fonts = new String[] {
            "Nimbus Sans Regular", "Nimbus Sans Italic", "Nimbus Sans Bold", "Nimbus Sans Bold Italic",
            "Nimbus Roman Regular", "Nimbus Roman Italic", "Nimbus Roman Bold", "Nimbus Roman Bold Italic",
            "Nimbus Mono PS Regular", "Nimbus Mono PS Italic", "Nimbus Mono PS Bold", "Nimbus Mono PS Bold Italic"
        };
        else if (osName.contains("windows")) fonts = new String[] {
            "Arial Regular", "Arial Italic", "Arial Bold", "Arial Bold Italic",
            "Times New Roman Regular", "Times New Roman Italic", "Times New Roman Bold", "Times New Roman Bold Italic",
            "Courier New Regular", "Courier New Italic", "Courier New Bold", "Courier New Bold Italic"
        };
        else fonts = new String[] {Font.SANS_SERIF, Font.SERIF, Font.MONOSPACED};
        font = new JComboBox<String>(fonts);
        font.setSelectedItem(settings.font);
        font.setOpaque(false);
        fontPosition = new JComboBox<String>(new String[]{"RIGHT", "LEFT", "BOTTOM", "TOP"});
        fontPosition.setSelectedItem(settings.fontAlignment);
        fontPosition.setOpaque(false);
        JPanel fontColorPanel = new JPanel();
        fontColorPanel.setBorder(new EmptyBorder(0, 0, 0, 0));
        fontColorPanel.setLayout(new BoxLayout(fontColorPanel, 0));
        fontColor = new JTextField();
        fontColor.setToolTipText("Use la palabra 'transparente' si no desea un color");
        fontColor.setText(this.settings.fontColor);
        fontColor.getDocument().addDocumentListener(new DocumentListener() {
            public void updateIcon(DocumentEvent edoc) {
                try {
                    String text = fontColor.getText();
                    if (!text.isEmpty() && ! text.equalsIgnoreCase("transparente")) {
                        Color color = Color.decode(text);
                        btFontColor.setIcon(createImageIcon(color));
                    } else btFontColor.setIcon(getTransparentImageIcon());
                } catch (Exception e) {
                    LOG.error("Error cambiando color de fuente", e);
                    e.printStackTrace();
                }
            }
            public void insertUpdate(DocumentEvent e) {
                updateIcon(e);
            }
            public void removeUpdate(DocumentEvent e) {
                updateIcon(e);
            }
            public void changedUpdate(DocumentEvent e) {
                updateIcon(e);
            }
        });
        btFontColor = new JButton("Elegir");
        btFontColor.setOpaque(false);
        setIcons(btFontColor, this.settings.fontColor, this.settings.getFontColor());
        fontColorPanel.add(btFontColor);
        fontColorPanel.add(fontColor);
        JPanel backgroundColorPanel = new JPanel();
        backgroundColorPanel.setBorder(new EmptyBorder(0, 0, 0, 0));
        backgroundColorPanel.setLayout(new BoxLayout(backgroundColorPanel, 0));
        backgroundColor = new JTextField();
        backgroundColor.setToolTipText("Use la palabra 'transparente' si no desea un color de fondo");
        backgroundColor.setText(this.settings.backgroundColor);
        btBackgroundColor = new JButton("Elegir");
        btBackgroundColor.setOpaque(false);
        setIcons(btBackgroundColor, this.settings.backgroundColor, this.settings.getBackgroundColor());
        backgroundColorPanel.add(btBackgroundColor);
        backgroundColorPanel.add(backgroundColor);
        backgroundColor.getDocument().addDocumentListener(new DocumentListener() {
            public void updateIcon(DocumentEvent edoc) {
                try {
                    String text = backgroundColor.getText();
                    if (!text.isEmpty() && ! text.equalsIgnoreCase("transparente")) {
                        Color color = Color.decode(text);
                        btBackgroundColor.setIcon(createImageIcon(color));
                    } else btBackgroundColor.setIcon(getTransparentImageIcon());
                } catch (Exception e) {
                    LOG.error("Error cambiando color de fondo", e);
                    e.printStackTrace();
                }
            }
            public void insertUpdate(DocumentEvent e) {
                updateIcon(e);
            }
            public void removeUpdate(DocumentEvent e) {
                updateIcon(e);
            }
            public void changedUpdate(DocumentEvent e) {
                updateIcon(e);
            }
        });
        JPanel imagePanel = new JPanel();
        imagePanel.setBorder(new EmptyBorder(0, 0, 0, 0));
        imagePanel.setLayout(new BoxLayout(imagePanel, 0));
        imageText = new JTextField();
        btImage = new JButton("Elegir");
        if (this.settings.image != null) {
            imageText.setText(this.settings.image);
            btImage.setIcon(this.getIcon(this.settings.image));
        }
        imagePanel.add(btImage);
        imagePanel.add(imageText);
        portNumber = new JSpinner();
        portNumber.setModel(new SpinnerNumberModel((int) this.settings.portNumber, 1024, 65535, 1));
        portNumber.setEditor(new JSpinner.NumberEditor(portNumber, "0"));
        addSettingsBox(simplePanel, "Razón:", reason);
        addSettingsBox(simplePanel, "Lugar:", place);
        addSettingsBox(simplePanel, "Contacto:", contact);
        addSettingsBox(simplePanel, "Formato de fecha:", dateFormat);
        addSettingsBox(simplePanel, "Mensaje de firma:", scrollableDefaultSignMessage, new Dimension(150, 50));
        addSettingsBox(simplePanel, "Página inicial:", pageNumber);
        addSettingsBox(simplePanel, "Ancho de firma:", signWidth);
        addSettingsBox(simplePanel, "Largo de firma:", signHeight);
        addSettingsBox(simplePanel, "Posición inicial X:", signX);
        addSettingsBox(simplePanel, "Posición inicial Y:", signY);
        addSettingsBox(simplePanel, "Tamaño de fuente:", fontSize);
        addSettingsBox(simplePanel, "Fuente:", font);
        addSettingsBox(simplePanel, "Posición de fuente:", fontPosition);
        addSettingsBox(simplePanel, "Color de fuente:", fontColorPanel);
        addSettingsBox(simplePanel, "Color de fondo:", backgroundColorPanel);
        addSettingsBox(simplePanel, "Imagen de firma:", imagePanel);
        addSettingsBox(simplePanel, "Puerto de escucha:", portNumber);
        btFontColor.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                showFontColorPicker();
            }
        });
        btBackgroundColor.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                showBackgroundColorPicker();
            }
        });
        btImage.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                showImagePicker();
            }
        });
        configPanel = new JScrollPane(simplePanel, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        configPanel.setPreferredSize(new Dimension(700, 400));
        configPanel.setBorder(null);
        configPanel.setOpaque(false);
        configPanel.getViewport().setOpaque(false);
        add(configPanel, BorderLayout.CENTER);
    }
/*
    private void changeLTA() {
        if (useLTA.isSelected()){
            pAdESLevel.setSelectedItem("LTA");
            xAdESLevel.setSelectedItem("LTA");
            cAdESLevel.setSelectedItem("LTA");
        }
    }
*/
    private void createAdvancedConfigPanel() {
        advancedPanel = new ScrollableJPanel();
        advancedPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        advancedPanel.setLayout(new BoxLayout(advancedPanel, 1));
        pDFImgScaleFactor = new JTextField();
        pDFImgScaleFactor.setText(String.format("%.2f", this.settings.pDFImgScaleFactor));
        pDFImgScaleFactor.setToolTipText("Factor de escala al presentar la previsualización de la página de pdf");
        pluginsActive = new PluginManagerPlugin();
        pluginsActive.setPreferredSize(new Dimension(450, 130));
        advancedPanel.add(pluginsActive);
        pKCS12Panel = new Pkcs12ConfigPanel();
        pKCS12Panel.setList(settings.pKCS12File);
        advancedPanel.add(pKCS12Panel);
        String pAdESLevelOptions[] = {"T", "LT", "LTA"};
        pAdESLevel = new JComboBox<String>(pAdESLevelOptions);
        pAdESLevel.setSelectedItem(settings.pAdESLevel);
        pAdESLevel.setOpaque(false);
        addSettingsBox(advancedPanel, "Nivel PAdES:", pAdESLevel);
        String xAdESLevelOptions[] = {"T", "LT", "LTA"};
        xAdESLevel = new JComboBox<String>(xAdESLevelOptions);
        xAdESLevel.setSelectedItem(settings.xAdESLevel);
        xAdESLevel.setOpaque(false);
        addSettingsBox(advancedPanel, "Nivel XAdES:", xAdESLevel);
        String cAdESLevelOptions[] = {"T", "LT", "LTA"};
        cAdESLevel = new JComboBox<String>(cAdESLevelOptions);
        cAdESLevel.setSelectedItem(settings.cAdESLevel);
        cAdESLevel.setOpaque(false);
        addSettingsBox(advancedPanel, "Nivel CAdES:", cAdESLevel);
        addSettingsBox(advancedPanel, "Escala de previsualización del Pdf", pDFImgScaleFactor);
        JPanel pKCS11ModulePanel = new JPanel();
        pKCS11ModulePanel.setBorder(new EmptyBorder(0, 0, 0, 0));
        pKCS11ModulePanel.setLayout(new BoxLayout(pKCS11ModulePanel, 0));
        pKCS11ModuleText = new JTextField();
        btPKCS11Module = new JButton("Elegir");
        if (this.settings.extraPKCS11Lib != null ) pKCS11ModuleText.setText(this.settings.extraPKCS11Lib);
        pKCS11ModulePanel.add(pKCS11ModuleText);
        pKCS11ModulePanel.add(btPKCS11Module);
        btPKCS11Module.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                String path = getFilePath();
                if (path != null) pKCS11ModuleText.setText(path);
            }
        });
        addSettingsBox(advancedPanel, "Archivo PKCS11", pKCS11ModulePanel);
        advancedPanel.add(new JLabel("El archivo PKCS11 se detecta automáticamente, "));
        advancedPanel.add(new JLabel("pero podrá ser escrito usando el campo anterior"));
        advancedBottomSpace = new JPanel();
        advancedPanel.add(advancedBottomSpace);
        //changeLTA();
    }
    public ConfigPanel() {
        manager = SettingsManager.getInstance();
        settings = manager.getAndCreateSettings();
        setLayout(new BorderLayout(0, 0));
        this.createSimpleConfigPanel();
        this.createAdvancedConfigPanel();
        JPanel optionswitchpanel = new JPanel();
        add(optionswitchpanel, BorderLayout.NORTH);
        JButton showadvanced = new JButton("Opciones avanzadas");
        showadvanced.setOpaque(false);
        optionswitchpanel.add(showadvanced);
        showadvanced.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                isAdvancedOptions = !isAdvancedOptions;
                if (isAdvancedOptions) {
                    showadvanced.setText("Opciones básicas");
                    configPanel.setViewportView(advancedPanel);
                    simplePanel.setVisible(false);
                    advancedPanel.setVisible(true);
                } else {
                    showadvanced.setText("Opciones Avanzadas");
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
                restartSettings();
            }
        });
        btns.add(restartbtn);
        JButton applywithoutsave = new JButton("Aplicar sin guardar");
        applywithoutsave.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                chargeSettings();
                //if (startServer.isSelected()) showMessage("Modo remoto no se activará, debe guardar y reiniciar la aplicación.");
            }
        });
        btns.add(applywithoutsave);
        JButton btSave = new JButton("Guardar");
        btSave.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                saveSettings();
                //if (startServer.isSelected()) showMessage("El Modo remoto se iniciará al reinicio de la aplicación, puede desactivarlo con el menú contextual obtenido con clic derecho.");
            }
        });
        btns.add(btSave);
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
    public void chargeSettings() {
        settings.withoutVisibleSign = this.withoutVisibleSign.isSelected();
        settings.reason = reason.getText();
        settings.place = place.getText();
        settings.contact = contact.getText();
        settings.dateFormat = this.dateFormat.getText();
        settings.defaultSignMessage = defaultSignMessage.getText();
        settings.withoutVisibleSign = withoutVisibleSign.isSelected();
        //settings.useLTA = useLTA.isSelected();
        settings.showLogs = this.showLogs.isSelected();
        settings.overwriteSourceFile = overwriteSourceFile.isSelected();
        settings.pageNumber = Integer.parseInt(pageNumber.getValue().toString());
        settings.signWidth = Integer.parseInt(signWidth.getValue().toString());
        settings.signHeight = Integer.parseInt(signHeight.getValue().toString());
        settings.fontSize = Integer.parseInt(fontSize.getValue().toString());
        settings.signX = Integer.parseInt(signX.getValue().toString());
        settings.signY = Integer.parseInt(signY.getValue().toString());
        settings.font = font.getSelectedItem().toString();
        settings.fontAlignment = fontPosition.getSelectedItem().toString();
        settings.fontColor = fontColor.getText();
        settings.backgroundColor = backgroundColor.getText();
        settings.image = imageText.getText();
        //settings.startServer = this.startServer.isSelected();
        settings.portNumber = Integer.parseInt(portNumber.getValue().toString());
        settings.pAdESLevel = pAdESLevel.getSelectedItem().toString();
        settings.xAdESLevel = xAdESLevel.getSelectedItem().toString();
        settings.cAdESLevel = cAdESLevel.getSelectedItem().toString();
        settings.pDFImgScaleFactor = Float.parseFloat(pDFImgScaleFactor.getText().replace(",", "."));
        settings.pKCS12File = pKCS12Panel.getList();
        settings.extraPKCS11Lib = pKCS11ModuleText.getText();
        if (settings.extraPKCS11Lib.isEmpty()) settings.extraPKCS11Lib = null;
        settings.activePlugins = pluginsActive.getActivePlugin();
        settings.updateConfig();
    }

    public void restartSettings() {
        Settings settings = new Settings();
        withoutVisibleSign.setSelected(settings.withoutVisibleSign);
        //useLTA.setSelected(settings.useLTA);
        showLogs.setSelected(settings.showLogs);
        overwriteSourceFile.setSelected(settings.overwriteSourceFile);
        reason.setText(settings.reason);
        place.setText(settings.place);
        contact.setText(settings.contact);
        dateFormat.setText(settings.dateFormat);
        defaultSignMessage.setText(settings.defaultSignMessage);
        pageNumber.setValue(settings.pageNumber);
        signWidth.setValue(settings.signWidth);
        signHeight.setValue(settings.signHeight);
        signX.setValue(settings.signX);
        signY.setValue(settings.signY);
        fontSize.setValue(settings.fontSize);
        font.setSelectedItem(settings.font);
        fontColor.setText(settings.fontColor);
        backgroundColor.setText(settings.backgroundColor);
        setIcons(btFontColor, fontColor.getText(), this.settings.getFontColor());
        setIcons(btBackgroundColor, backgroundColor.getText(), this.settings.getBackgroundColor());
        //startServer.setSelected(settings.startServer);
        portNumber.setValue(settings.portNumber);
        fontPosition.setSelectedItem(settings.fontAlignment);
        pAdESLevel.setSelectedItem(settings.pAdESLevel);
        xAdESLevel.setSelectedItem(settings.xAdESLevel);
        cAdESLevel.setSelectedItem(settings.cAdESLevel);
        pDFImgScaleFactor.setText(String.format("%.2f", settings.pDFImgScaleFactor));
        if (settings.image != null) {
            imageText.setText(settings.image);
            btImage.setIcon(this.getIcon(settings.image));
        } else {
            imageText.setText("");
            btImage.setIcon(createImageIcon(new Color(255, 255, 255, 0)));
        }
        if (settings.pKCS12File != null) pKCS12Panel.setList(settings.pKCS12File);
        if (settings.extraPKCS11Lib != null) pKCS11ModuleText.setText(settings.extraPKCS11Lib);
        else pKCS11ModuleText.setText("");
        pluginsActive.loadPlugins(settings);
    }

    private void setIcons(JButton component, String text, Color color) {
        if (text.equalsIgnoreCase("transparente")) component.setIcon(getTransparentImageIcon());
        else component.setIcon(createImageIcon(color));
    }

    public void saveSettings() {
        chargeSettings();
        this.manager.setSettings(this.settings, true);
    }

    public void showFontColorPicker() {
        Color newColor = JColorChooser.showDialog(this, "Color del texto", this.settings.getFontColor());
        if (newColor != null) {
            String buf = Integer.toHexString(newColor.getRGB());
            String hex = "#" + buf.substring(buf.length() - 6);
            fontColor.setText(hex);
        }
    }

    public void showBackgroundColorPicker() {
        Color newColor = JColorChooser.showDialog(this, "Color de fondo", this.settings.getBackgroundColor());
        if (newColor != null) {
            String buf = Integer.toHexString(newColor.getRGB());
            String hex = "#" + buf.substring(buf.length() - 6);
            backgroundColor.setText(hex);
        }
    }

    public String getFilePath() {
        FileDialog loadDialog = new FileDialog(new JDialog(), "Seleccionar un archivo");
        loadDialog.setMultipleMode(false);
        loadDialog.setLocationRelativeTo(null);
        loadDialog.setVisible(true);
        loadDialog.dispose();
        File[] files = loadDialog.getFiles();
        if (files.length > 0) return files[0].toString();
        else return null;
    }

    public void showImagePicker() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.addChoosableFileFilter(new ImageFilter());
        fileChooser.setAcceptAllFileFilterUsed(false);
        int option = fileChooser.showOpenDialog(this);
        if (option == JFileChooser.APPROVE_OPTION) {
           File file = fileChooser.getSelectedFile();
           String path = file.getAbsolutePath();
           imageText.setText(path);
           btImage.setIcon(this.getIcon(path));
        }
    }

    public Icon getIcon(String path) {
        return new ImageIcon(new ImageIcon(path).getImage().getScaledInstance(iconSize, iconSize, Image.SCALE_DEFAULT));
    }

    public Icon getTransparentImageIcon() {
        BufferedImage image = new BufferedImage(iconSize, iconSize, BufferedImage.TYPE_INT_ARGB);
         Graphics2D graphics = image.createGraphics();
         graphics.setColor(new Color(0, 0, 0, 100));
         for (int x = 4; x < 28; x += 8) for (int y = 4; y < 28; y += 8) graphics.fillRect(x, y, 4, 4);
         graphics.setColor(new Color(130, 130, 130));
         for (int x = 8; x < 28; x += 8) for (int y = 8; y < 28; y += 8) graphics.fillRect(x, y, 4, 4);
         return new ImageIcon(image);
    }

    public ImageIcon createImageIcon(Color color) {
        BufferedImage image = new BufferedImage(iconSize, iconSize, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = image.createGraphics();
        graphics.setPaint(new Color(255, 255, 255, 0));
        graphics.setBackground(new Color(255, 255, 255, 0));
        graphics.fillRect(0, 0, iconSize, iconSize);
        graphics.setColor(color);
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        Ellipse2D.Float circle = new Ellipse2D.Float(4F, 6F, iconSize - 15, iconSize - 15);
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

   public boolean accept(File f) {
      if (f.isDirectory()) return true;
      String extension = getExtension(f);
      if (extension != null) if (extension.equalsIgnoreCase(TIFF) || extension.equalsIgnoreCase(TIF) || extension.equalsIgnoreCase(GIF) || extension.equalsIgnoreCase(JPEG) || extension.equalsIgnoreCase(JPG) || extension.equalsIgnoreCase(PNG)) return true;
      return false;
   }

   public String getDescription() {
      return "Image Only";
   }

   String getExtension(File f) {
      String s = f.getName();
      int i = s.lastIndexOf('.');
      if (i > 0 && i < s.length() - 1) return s.substring(i + 1).toLowerCase();
      else return null;
   }
}
