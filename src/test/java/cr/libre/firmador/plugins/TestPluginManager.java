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

import cr.libre.firmador.Settings;

import io.github.netmikey.logunit.api.LogCapturer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.slf4j.event.Level;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;

public class TestPluginManager {

    @Test
    void testRequiredSettingsLoaded(){
        PluginManager pluginManager = new PluginManager(null);
        Settings settings =  pluginManager.getSettings();

        assertNotNull(settings);
        assertNotNull(settings.activePlugins);
        assertInstanceOf(ArrayList.class, settings.activePlugins);
    }

    @Nested
    class TestCasesWithSpecifiedPlugins {
        private final PluginManager pluginManager = new PluginManager(null);
        private final Settings settings = this.pluginManager.getSettings();

        @RegisterExtension
        LogCapturer dummyPluginLogs = LogCapturer.create().captureForType(DummyPlugin.class);

        @BeforeEach
        void setDummyPluginForTesting(){
            this.settings.activePlugins = new ArrayList<>(List.of("cr.libre.firmador.plugins.DummyPlugin"));
        }

        @AfterEach
        void stopPlugins(){
            this.pluginManager.stop();
        }

        @Test
        void testRunPlugins(){
            this.pluginManager.run();

            this.dummyPluginLogs.assertContains("Starting DummyPlugin");

            List<Plugin> plugins = this.pluginManager.getPlugins();
            assertEquals(1, plugins.size());
            assertInstanceOf(DummyPlugin.class, plugins.get(0));
        }

        @Test
        void testStopPlugins(){
            this.pluginManager.run();  // run the plugin manager so it loads and starts the plugin
            this.dummyPluginLogs.assertContains("Starting DummyPlugin");  // check the plugin is actually running

            this.pluginManager.stop();
            this.dummyPluginLogs.assertContains("Stopping DummyPlugin");
        }

        @Test
        void testStartLogging(){
            this.pluginManager.run();  // run the plugin manager so it loads and starts the plugin
            this.dummyPluginLogs.assertContains("Starting DummyPlugin");  // check the plugin is actually running

            this.pluginManager.startLogging();

            this.dummyPluginLogs.assertContains("sun.jnu.encoding");
            this.dummyPluginLogs.assertContains("java.vm.vendor");
            this.dummyPluginLogs.assertContains("sun.arch.data.model");
            this.dummyPluginLogs.assertContains("os.name");
            this.dummyPluginLogs.assertContains("java.vm.specification.version");
            this.dummyPluginLogs.assertContains("sun.boot.library.path");
            this.dummyPluginLogs.assertContains("sun.cpu.endian");
            this.dummyPluginLogs.assertContains("user.language");
            this.dummyPluginLogs.assertContains("java.runtime.version");
            this.dummyPluginLogs.assertContains("os.arch");
            this.dummyPluginLogs.assertContains("java.vm.version");
            this.dummyPluginLogs.assertContains("firmador.libre.version");
        }

        @Test
        void testLoadNonRunnablePlugins(){
            this.pluginManager.run();

            List<Plugin> plugins = this.pluginManager.getPlugins();
            assertEquals(1, plugins.size());
            assertInstanceOf(DummyPlugin.class, plugins.get(0));

            List<Plugin> runnablePlugins = this.pluginManager.getRunnablePlugins();
            assertTrue(runnablePlugins.isEmpty());
        }

        @Test
        void testLoadRunnablePlugins() {
            this.settings.activePlugins.add("cr.libre.firmador.plugins.CheckUpdatePlugin");
            this.pluginManager.run();

            List<Plugin> plugins = this.pluginManager.getPlugins();
            assertEquals(2, plugins.size());
            assertInstanceOf(DummyPlugin.class, plugins.get(0));
            assertInstanceOf(CheckUpdatePlugin.class, plugins.get(1));

            List<Plugin> runnablePlugins = this.pluginManager.getRunnablePlugins();
            assertEquals(1, runnablePlugins.size());
            assertInstanceOf(CheckUpdatePlugin.class, runnablePlugins.get(0));
        }
    }

    @Nested
    class TestCasesWithExceptions {
        private final PluginManager pluginManager = new PluginManager(null);
        private final Settings settings = this.pluginManager.getSettings();

        @RegisterExtension
        LogCapturer pluginManagerLogs = LogCapturer.create().captureForType(PluginManager.class, Level.ERROR);

        @Test
        void testLoadPluginsWithClassNotFoundException(){
            this.settings.activePlugins = new ArrayList<>(List.of("cr.libre.firmador.plugins.NotExistingPlugin"));
            this.pluginManager.run();

            this.pluginManagerLogs.assertContains("Error al cargar plugin (clase no encontrada)");
        }

        @Test
        void testLoadPluginsWithAnotherException(){
            this.settings.activePlugins = new ArrayList<>(List.of("cr.libre.firmador.plugins.Plugin"));
            this.pluginManager.run();

            this.pluginManagerLogs.assertContains("Error al cargar plugin");
        }
    }
}
