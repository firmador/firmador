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
				Plugin plugin = (Plugin) pluginClass.newInstance();

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
