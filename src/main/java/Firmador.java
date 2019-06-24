/* Firmador is a program to sign documents using AdES standards.

Copyright (C) 2019 Firmador authors.

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

import javax.swing.SwingUtilities;

import app.firmador.gui.GUIInterface;
import app.firmador.gui.GUISelector;

public class Firmador {

    public static void main(String[] args) {
        // Workaround illegal access for Java 9+ until jaxb 2.4.0 gets released
        System.setProperty( "com.sun.xml.bind.v2.bytecode.ClassTailor.noOptimize", "true");
        GUISelector guiselector = new GUISelector();
        GUIInterface gui = guiselector.getInterface(args);
        gui.setArgs(args);
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                gui.loadGUI();
            }
        });
    }

}
