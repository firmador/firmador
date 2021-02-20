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

package app.firmador.gui;

public class GUISelector {

    public String getGUIClassName(String[] args) {
        String dev = "swing";

        for (String params : args) {
            if (params.startsWith("-d")) {
                dev = params.substring(2);
            }
        }

        return dev;
    }

    public GUIInterface getInterface(String[] args) {
        String name = this.getGUIClassName(args);

        return this.getInterface(name);
    }

    public GUIInterface getInterface(String name) {
        GUIInterface gui = null;

        if (name.equals("args")) {
            gui = new GUIArgs();
        } else if (name.equals("shell")) {
            gui = new GUIShell();
        } else {
            gui = new GUISwing();
        }

        return gui;
    }

}
