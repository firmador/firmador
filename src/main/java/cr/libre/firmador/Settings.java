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

package cr.libre.firmador;

import java.awt.Font;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import eu.europa.esig.dss.enumerations.SignatureLevel;
import eu.europa.esig.dss.enumerations.SignerTextPosition;

import java.awt.Color;

public class Settings {
    private List<ConfigListener> listeners = new ArrayList<ConfigListener>();

    public String release_url_check = "https://firmador.libre.cr/version.txt";
    public String base_url = "https://firmador.libre.cr";
    public String release_url = "https://firmador.libre.cr/firmador.jar";
    public String release_snapshot_url = "https://firmador.libre.cr/firmador-en-pruebas.jar";
    public String checksum_url = "https://firmador.libre.cr/firmador.jar.sha256";
    public String checksum_snapshot_url = "https://firmador.libre.cr/firmador-en-pruebas.jar.sha256";

    
    public String defaultdevelopmentversion = "Desarrollo";
    public boolean withoutvisiblesign = false;
    public boolean uselta = true;
    public boolean overwritesourcefile = false;
    public String reason = "";
    public String place = "";
    public String contact = "";
    public String dateformat = "dd/MM/yyyy hh:mm:ss a";
    public String defaultsignmessage = "Esta es una representación gráfica únicamente,\nverifique la validez de la firma.";
    public Integer signwith = 133;
    public Integer signheight = 33;
    public Integer fontsize = 7;
    public String font = Font.SANS_SERIF;
    public String fontcolor = "#000000";
    public String backgroundcolor = "transparente";
    public String extrapkcs11Lib = null;
    public Integer signx = 198;
    public Integer signy = 0;
    public String image = null;
    public boolean startserver = false;
    public String fontalignment = "RIGHT";
    public boolean showlogs = false;

    public Integer pagenumber = 1;
    public Integer portnumber = 3516;
    public String padesLevel = "LTA";
    public String xadesLevel = "LTA";
    public String cadesLevel = "LTA";
    public String pkcs12file = null;
    

    public List<String> active_plugins = new ArrayList<String>();
    public List<String> available_plugins = new ArrayList<String>();
    
    public Settings() {
    	active_plugins.add("cr.libre.firmador.plugins.DummyPlugin");
    	active_plugins.add("cr.libre.firmador.plugins.CheckUpdatePlugin");
    	available_plugins.add("cr.libre.firmador.plugins.DummyPlugin");
    	available_plugins.add("cr.libre.firmador.plugins.CheckUpdatePlugin");
    }

    public String getDefaultSignMessage() {
        return this.defaultsignmessage;
    }

    public String getDateFormat() {
        try {
            return this.dateformat;
        } catch (Exception e) {
            return "dd/MM/yyyy hh:mm:ss a";
        }
    }

    public void addListener(ConfigListener toAdd) {
        listeners.add(toAdd);
    }

    public void updateConfig() {
        for (ConfigListener hl : listeners)
            hl.updateConfig();
    }

    public SignerTextPosition getFontAlignment() {
        SignerTextPosition position = SignerTextPosition.RIGHT;
        switch (this.fontalignment) {
        case "RIGHT":
            position = SignerTextPosition.RIGHT;
            break;
        case "LEFT":
            position = SignerTextPosition.LEFT;
            break;
        case "BOTTOM":
            position = SignerTextPosition.BOTTOM;
            break;
        case "TOP":
            position = SignerTextPosition.TOP;
            break;
        default:
            break;
        }

        return position;
    }
    public Color getFontColor() {
        if (this.fontcolor.toLowerCase() == "transparente") {
            return new Color(255, 255, 255, 0);
        }
        try {
            return Color.decode(this.fontcolor);
        } catch (Exception e) {
            return new Color(0, 0, 0, 255);
        }
    }
    public Color getBackgroundColor() {
        if (this.backgroundcolor.toLowerCase() == "transparente") {
            return new Color(255, 255,255, 0);
        }
        try {
            return Color.decode(this.backgroundcolor);
        } catch (Exception e) {
            return new Color(255, 255,255, 0);
        }
    }

    public String getImage() {
        if (this.image == null) return null;
        File temp = new File(this.image);
        boolean exists = temp.exists();
        if (exists) return temp.toURI().toString();
        return null;
    }
    public boolean isRemote() {
        if (this.startserver) return true;
        String origin = System.getProperty("jnlp.remoteOrigin");
        boolean isRemote = (origin != null);
        return isRemote;
    }
    public String getOrigin() {
        String origin = System.getProperty("jnlp.remoteOrigin");
        if (origin == null) {
            origin = "*";
        }

        return origin;
    }
    
    public SignatureLevel getPAdESLevel() {
    	SignatureLevel level = SignatureLevel.PAdES_BASELINE_LTA;
        switch (padesLevel) {
	        case "T": level=SignatureLevel.PAdES_BASELINE_T; break;
	        case "LT": level=SignatureLevel.PAdES_BASELINE_LT; break;
	        case "LTA": level=SignatureLevel.PAdES_BASELINE_LTA; break;
	        default: level=SignatureLevel.PAdES_BASELINE_LTA; break;
        }
        return level;
    }
    
    public SignatureLevel getXAdESLevel() {
    	SignatureLevel level = SignatureLevel.XAdES_BASELINE_LTA;
        switch (xadesLevel) {
	        case "T": level=SignatureLevel.XAdES_BASELINE_T; break;
	        case "LT": level=SignatureLevel.XAdES_BASELINE_LT; break;
	        case "LTA": level=SignatureLevel.XAdES_BASELINE_LTA; break;
	        default: level=SignatureLevel.XAdES_BASELINE_LTA; break;
        }
        return level;
    }  
    public SignatureLevel getCAdESLevel() {
    	SignatureLevel level = SignatureLevel.CAdES_BASELINE_LTA;
        switch (cadesLevel) {
	        case "T": level=SignatureLevel.CAdES_BASELINE_T; break;
	        case "LT": level=SignatureLevel.CAdES_BASELINE_LT; break;
	        case "LTA": level=SignatureLevel.CAdES_BASELINE_LTA; break;
	        default: level=SignatureLevel.CAdES_BASELINE_LTA; break;
        }
        return level;
    }
    
    public String getVersion() {     
    	String versionstr = getClass().getPackage().getSpecificationVersion();
    	if(versionstr == null) versionstr=this.defaultdevelopmentversion;
    	return versionstr;
    	  
    }
    
    public String getReleaseUrl() {
    	String version = getVersion();
    	if(version.contains("SNAPSHOT")) {
    		return this.release_snapshot_url;
    	}
    	return this.release_url;
    }
    public String getReleaseCheckUrl() {
    	String version = getVersion();
    	if(version.contains("SNAPSHOT")) {
    		return "";
    	}
    	return this.release_url_check;
    }
    public String getChecksumUrl() {
    	String version = getVersion();
    	if(version.contains("SNAPSHOT")) {
    		return this.checksum_snapshot_url;
    	}
    	return this.checksum_url;
    }
      
}
