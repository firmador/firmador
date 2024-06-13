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

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.List;
import javax.swing.SwingUtilities;

import org.apache.pdfbox.pdmodel.font.FontMappers;

import cr.libre.firmador.gui.GUIInterface;
import cr.libre.firmador.gui.GUISelector;
import cr.libre.firmador.plugins.PluginManager;
import java.util.Arrays;

public class Firmador {

    public static void main(String[] args) throws Throwable {
        for (String s : args) {
            if (s.equals("run")) {
                doMain(args);
                return;
            }
        }
        List<String> command = new ArrayList<String>();
        String processCommand = ProcessHandle.current().info().command().orElse("java");
        command.add(processCommand);
        String[] arguments = ProcessHandle.current().info().arguments().orElse(new String[0]);
        if (arguments.length == 0) {
            command.add("-jar");
            command.add(MethodHandles.lookup().lookupClass().getProtectionDomain().getCodeSource().getLocation().toURI().getPath());
        }
        command.add("--add-exports");
        command.add("jdk.crypto.cryptoki/sun.security.pkcs11.wrapper=ALL-UNNAMED");
        for (String argument : arguments) command.add(argument);
        command.add("run");
        System.out.println(command.toString());

        new ProcessBuilder().inheritIO().command(command).start().waitFor();
    }

    public static void doMain(String[] args) {
        // PDFBox font cache warmup
        FontMappers.instance().getFontBoxFont(null, null);
        GUISelector guiselector = new GUISelector();
        PluginManager pluginManager = new PluginManager();
        GUIInterface gui = guiselector.getInterface(args);
        gui.setArgs(args);
        gui.setPluginManager(pluginManager);
        SwingUtilities.invokeLater(pluginManager);
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                gui.loadGUI();
                gui.configurePluginManager();
            }
        });
    }

}
