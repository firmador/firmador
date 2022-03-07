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

import javax.swing.BorderFactory;
import javax.swing.JEditorPane;
import javax.swing.JTextPane;
import javax.swing.UIManager;

@SuppressWarnings("serial")
public class CopyableJLabel extends JTextPane {

    public CopyableJLabel() {
        super();
        setDefault();
    }
    
    public CopyableJLabel(String text) {
        super();
        setDefault();
        setText("<html><p style=\"word-wrap: break-word; width: 400px;\">" +text + "<p></html>");
    }

    private void setDefault() {
        this.setContentType("text/html");
        this.setEditable(false);
        this.setOpaque(false);
        this.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, true);
        this.setFont(UIManager.getFont("Label.font"));
        this.setBorder(BorderFactory.createCompoundBorder(this.getBorder(), BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        this.setAutoscrolls(true);
    }

}
