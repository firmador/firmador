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

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import cr.libre.firmador.Settings;
import cr.libre.firmador.gui.GUISwing;

import javax.swing.*;


public class TestAboutLayout {
    private final AboutLayout aboutLayout = new AboutLayout(new JPanel());
    private final Settings settings = this.aboutLayout.getSettings();

    @Test
    void testRequiredSettingsLoaded(){
        assertNotNull(this.settings);
        assertFalse(this.settings.getVersion().isEmpty());
        assertFalse(this.settings.baseUrl.isEmpty());
    }

    @Test
    void testCorrectIconLabel(){
        JLabel iconLabel = this.aboutLayout.getIconLabel();

        assertNotNull(iconLabel);
        assertNotNull(iconLabel.getIcon());
    }

    @Test
    void testCorrectDescriptionLabel(){
        JLabel descriptionLabel = this.aboutLayout.getDescriptionLabel();

        assertNotNull(descriptionLabel);
        assertFalse(descriptionLabel.getText().isEmpty());
        assertTrue(this.aboutLayout.getDescriptionLabel().getText().contains(this.settings.getVersion()));
    }

    @Test
    void testCorrectWebsiteButton(){
        JButton websiteButton = this.aboutLayout.getWebsiteButton();

        assertNotNull(websiteButton);
        assertFalse(websiteButton.getText().isEmpty());
        assertTrue(websiteButton.getActionListeners().length > 0);
    }

    @Test
    void testSetInterface(){
        GUISwing testGUIInterface = new GUISwing();
        this.aboutLayout.setInterface(testGUIInterface);

        assertEquals(testGUIInterface, this.aboutLayout.getInterface());
    }

}
