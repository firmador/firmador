/* Firmador is a program to sign documents using AdES standards.

Copyright (C) 2018 Firmador authors.

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

package app.firmador;

import app.firmador.gui.GUIInterface;
import app.firmador.gui.GUISelector;

public class Firmador {

    public static void main(String[] args) {
        GUISelector guiselector = new GUISelector();

        GUIInterface gui = guiselector.getInterface(args);
        gui.setArgs(args);
        gui.loadGUI();
    }

}
