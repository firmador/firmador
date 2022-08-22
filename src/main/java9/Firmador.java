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

import javax.swing.SwingUtilities;

import org.apache.pdfbox.pdmodel.font.FontMappers;

import cr.libre.firmador.gui.GUIInterface;
import cr.libre.firmador.gui.GUISelector;
import cr.libre.firmador.plugins.PluginManager;

public class Firmador {

    public static void main(String[] args) {
        for (String s : args) {
            if (s.equals("run")) {
                doMain(args);
                return;
            }
        }
        new ProcessBuilder().inheritIO().command(ProcessHandle().current().info().command()
            "--add-exports", "jdk.crypto.cryptoki/sun.security.pkcs11.wrapper=ALL-UNNAMED",
            "-jar", MethodHandles.lookup().lookupClass().getProtectionDomain().getCodeSource().getLocation().toURI().getPath(),
            "run").start().waitFor();
        }
    }

    public static void doMain(String[] args) {
        // PDFBox font cache warmup
        FontMappers.instance().getFontBoxFont(null, null);
        GUISelector guiselector = new GUISelector();
        GUIInterface gui = guiselector.getInterface(args);
        gui.setArgs(args);
        PluginManager pluginManager = new PluginManager(gui);
       
        SwingUtilities.invokeLater(pluginManager);
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                gui.loadGUI();
                gui.setPluginManager(pluginManager);
                
            }
        });
    }

}
