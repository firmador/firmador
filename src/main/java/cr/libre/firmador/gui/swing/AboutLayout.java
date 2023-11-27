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

import java.awt.Container;
import java.awt.Desktop;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.invoke.MethodHandles;
import java.net.URI;
import javax.swing.GroupLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cr.libre.firmador.MessageUtils;
import cr.libre.firmador.Settings;
import cr.libre.firmador.SettingsManager;
import cr.libre.firmador.gui.GUIInterface;
import cr.libre.firmador.signers.FirmadorUtils;

public class AboutLayout extends GroupLayout {
    final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private GUIInterface swinginterface;
    private Image image = new ImageIcon(this.getClass().getClassLoader().getResource("firmador.png")).getImage();
    private Settings settings;
    private JLabel descriptionLabel;
    private JLabel iconLabel;
    private JButton websiteButton;

    public AboutLayout(Container host) {
        super(host);
        settings = SettingsManager.getInstance().getAndCreateSettings();
        JLabel iconLabel = new JLabel(new ImageIcon(image.getScaledInstance(128, 128, Image.SCALE_SMOOTH)));
        JLabel descriptionLabel = new JLabel(
                String.format(MessageUtils.t("about_description_label"), settings.getVersion()), JLabel.CENTER);

        JButton websiteButton = new JButton(MessageUtils.t("about_website_link"));
        websiteButton.setOpaque(false);
        websiteButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                openProjectWebsite();
            }
        });
        this.setAutoCreateGaps(true);
        this.setAutoCreateContainerGaps(true);
        this.setHorizontalGroup(
            this.createParallelGroup(GroupLayout.Alignment.CENTER)
                .addComponent(this.iconLabel)
                .addComponent(this.descriptionLabel)
                .addComponent(this.websiteButton)
        );
        this.setVerticalGroup(
            this.createSequentialGroup()
                .addComponent(this.iconLabel)
                .addComponent(this.descriptionLabel)
                .addComponent(this.websiteButton)
        );
        descriptionLabel.getAccessibleContext().setAccessibleDescription(
                String.format(MessageUtils.t("about_description_label"), settings.getVersion()));
        descriptionLabel
                .setToolTipText(String.format(MessageUtils.t("about_description_label"), settings.getVersion()));
        websiteButton.setToolTipText(MessageUtils.t("about_website_link"));
        websiteButton.getAccessibleContext().setAccessibleDescription(MessageUtils.t("about_website_link_accesible"));

        websiteButton.setMnemonic('O');
        descriptionLabel.setFocusable(true);
    }

    public void setInterface(GUIInterface swinginterface){
        this.swinginterface = swinginterface;
    }

    private void openProjectWebsite() {
        if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
            try {
                Desktop.getDesktop().browse(new URI(settings.baseUrl)); // GTK3 Swing backend has a bug not opening the URL until the app closes
            } catch (Exception e) {
                LOG.error(MessageUtils.t("about_log_openurl"), e);
                this.swinginterface.showError(FirmadorUtils.getRootCause(e));
            }
        }
    }

    public JLabel getDescriptionLabel(){
        return this.descriptionLabel;
    }

    public JLabel getIconLabel(){
        return this.iconLabel;
    }

    public JButton getWebsiteButton() {
        return this.websiteButton;
    }

    public Settings getSettings(){
        return this.settings;
    }

    public GUIInterface getInterface(){
        return this.swinginterface;
    }

}
