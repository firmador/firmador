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

import java.awt.Container;
import java.awt.Desktop;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URI;

import javax.swing.GroupLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;

import org.slf4j.LoggerFactory;

import com.google.common.base.Throwables;

import cr.libre.firmador.Settings;
import cr.libre.firmador.SettingsManager;
import cr.libre.firmador.gui.GUIInterface;

public class AboutLayout extends GroupLayout {
	private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(AboutLayout.class);

    private GUIInterface swinginterface;
    private Image image = new ImageIcon(this.getClass().getClassLoader().getResource("firmador.png")).getImage();

	private Settings settings;

    public AboutLayout(Container host) {
        super(host);
        settings = SettingsManager.getInstance().get_and_create_settings();

        JLabel iconLabel = new JLabel(new ImageIcon(image.getScaledInstance(128, 128, Image.SCALE_SMOOTH)));
        JLabel descriptionLabel = new JLabel("<html><p align='center'><b>Firmador</b><br><br>" +
            "Versión " + settings.getVersion()  + "<br><br>" +
            "Herramienta para firmar documentos digitalmente.<br><br>" +
            "Los documentos firmados con esta herramienta cumplen con la<br>" +
            "Política de Formatos Oficiales de los Documentos Electrónicos<br>" +
            "Firmados Digitalmente de Costa Rica.<br><br></p></html>", JLabel.CENTER);
        JButton websiteButton = new JButton("Visitar sitio web del proyecto");
        websiteButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                openProjectWebsite();
            }
        });

        this.setAutoCreateGaps(true);
        this.setAutoCreateContainerGaps(true);
        this.setHorizontalGroup(
            this.createParallelGroup(GroupLayout.Alignment.CENTER)
                .addComponent(iconLabel)
                .addComponent(descriptionLabel)
                .addComponent(websiteButton)
        );
        this.setVerticalGroup(
            this.createSequentialGroup()
                .addComponent(iconLabel)
                .addComponent(descriptionLabel)
                .addComponent(websiteButton)
        );
    }
    public void setInterface(GUIInterface swinginterface){
        this.swinginterface = swinginterface;
    }
    private void openProjectWebsite() {
        if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
            try {
                Desktop.getDesktop().browse(new URI(settings.base_url));
            } catch (Exception e) {
            	LOG.error("Error abriendo url", e);
                this.swinginterface.showError(Throwables.getRootCause(e));
            }
        }
    }

}
