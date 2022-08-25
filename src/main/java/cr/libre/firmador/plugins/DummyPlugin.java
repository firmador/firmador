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
package cr.libre.firmador.plugins;

import org.slf4j.LoggerFactory;

import cr.libre.firmador.Settings;
import cr.libre.firmador.SettingsManager;
import cr.libre.firmador.gui.GUISwing;

public class DummyPlugin implements Plugin {
	private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(GUISwing.class);
	public void start() {
		LOG.info("Stating DummyPlugin");
	}
	
	public void start_loggin() {
	 
		
		String[] informationkey = {
				"sun.jnu.encoding",
				"java.vm.vendor",
				"sun.arch.data.model",
				"os.name",
				"java.vm.specification.version",
				"sun.boot.library.path",
				"sun.cpu.endian",
				"user.language",
				"java.runtime.version", 
				"os.arch",
				"java.vm.version"
				
		};
		String info="";
		for (Object propertyKeyName: informationkey){
			info+=propertyKeyName+" - "+System.getProperty(propertyKeyName.toString())+"\n";
        }
		
		Settings settings = SettingsManager.getInstance().get_and_create_settings();
		String version=settings.getVersion();
        info +="firmador.libre.version - "+ version+"\n";
		
		LOG.info(info);
	}

	public void stop() {
		LOG.info("Stop DummyPlugin");
	}

	@Override
	public boolean get_isrunnable() {
		return false;
	}
	
}
