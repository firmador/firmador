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
package cr.libre.firmador.plugins;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.List;
import javax.swing.SwingUtilities;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cr.libre.firmador.Settings;
import cr.libre.firmador.SettingsManager;
import cr.libre.firmador.gui.GUIInterface;

public class PluginManager implements Runnable {
    final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    //private GUIInterface gui;
    private Settings settings;
    private List<Plugin> runnablePlugins = new ArrayList<Plugin>();
    private List<Plugin> plugins = new ArrayList<Plugin>();

    public PluginManager(GUIInterface gui) {
        super();
        //this.gui = gui;
        this.settings = SettingsManager.getInstance().getAndCreateSettings();
    }

    private void loadPlugins() {
        for (String name : settings.activePlugins) {

            try {
                Class<?> pluginClass = Class.forName(name, true, Plugin.class.getClassLoader());
                Plugin plugin = (Plugin) pluginClass.getDeclaredConstructor().newInstance();

                if (plugin.getIsRunnable()) {
                    SwingUtilities.invokeLater((Runnable) plugin);
                    runnablePlugins.add(plugin);
                }

                plugins.add(plugin);
                plugin.start();

            } catch (ClassNotFoundException e) {
                LOG.error("Error al cargar plugin (clase no encontrada)", e.getMessage());
                e.printStackTrace();
            } catch (Exception e) {
                LOG.error("Error al cargar plugin", e.getMessage());
                e.printStackTrace();
            }
        }
    }

    public void stop() {
        for (Plugin plugin : plugins) {
            plugin.stop();
        }

    }

    public void startLogging() {
        for (Plugin plugin : plugins) {
            plugin.startLogging();
        }
    }

    @Override
    public void run() {
        loadPlugins();
    }

}
