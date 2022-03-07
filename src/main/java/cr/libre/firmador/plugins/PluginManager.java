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
package cr.libre.firmador.plugins;

import java.util.ArrayList;
import java.util.List;

import javax.swing.SwingUtilities;

import cr.libre.firmador.Settings;
import cr.libre.firmador.SettingsManager;
import cr.libre.firmador.gui.GUIInterface;

public class PluginManager implements Runnable {
	private GUIInterface gui;
	private Settings settings;
	private List<Plugin> runnables_plugins = new ArrayList<Plugin>();
	private List<Plugin> plugins = new ArrayList<Plugin>();

	public PluginManager(GUIInterface gui) {
		super();
		this.gui = gui;
		this.settings = SettingsManager.getInstance().get_and_create_settings();

	}

	private void loadPlugins() {
		for (String name : settings.active_plugins) {

			try {
				Class pluginClass = Class.forName(name, true, Plugin.class.getClassLoader());
				Plugin plugin = (Plugin) pluginClass.getDeclaredConstructor().newInstance();


				if (plugin.isrunnable) {
					SwingUtilities.invokeLater((Runnable) plugin);
					runnables_plugins.add(plugin);
				}

				plugins.add(plugin);
				plugin.start();

			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();

			}
		}
	}

	public void stop() {
		for (Plugin plugin : plugins) {
			plugin.stop();
		}
	}

	@Override
	public void run() {
		loadPlugins();

	}

}
