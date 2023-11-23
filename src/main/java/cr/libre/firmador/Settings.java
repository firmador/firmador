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

package cr.libre.firmador;

import java.awt.Font;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

import eu.europa.esig.dss.enumerations.SignatureLevel;
import eu.europa.esig.dss.enumerations.SignerTextPosition;

import java.awt.Color;

public class Settings {
    private List<ConfigListener> listeners = new ArrayList<ConfigListener>();

    public String releaseUrlCheck = "https://firmador.libre.cr/version.txt";
    public String baseUrl = "https://firmador.libre.cr";
    public String releaseUrl = "https://firmador.libre.cr/firmador.jar";
    public String releaseSnapshotUrl = "https://firmador.libre.cr/firmador-en-pruebas.jar";
    public String checksumUrl = "https://firmador.libre.cr/firmador.jar.sha256";
    public String checksumSnapshotUrl = "https://firmador.libre.cr/firmador-en-pruebas.jar.sha256";


    public String defaultDevelopmentVersion = "Desarrollo";
    public boolean withoutVisibleSign = false;
    //public boolean useLTA = true;
    public boolean overwriteSourceFile = false;
    public boolean hideSignatureAdvice = false;
    public String reason = "";
    public String place = "";
    public String contact = "";
    public String dateFormat = "dd/MM/yyyy hh:mm:ss a";
    public String defaultSignMessage = "Esta es una representación gráfica únicamente,\nverifique la validez de la firma.";
    public Integer signWidth = 133;
    public Integer signHeight = 33;
    public Integer fontSize = 7;
    public String font = Font.SANS_SERIF;
    public String fontColor = "#000000";
    public String backgroundColor = "transparente";
    public String extraPKCS11Lib = null;
    public Integer signX = 198;
    public Integer signY = 0;
    public String image = null;
    //public boolean startServer = false;
    public String fontAlignment = "RIGHT";
    public boolean showLogs = false;

    public Integer pageNumber = 1;
    public Integer portNumber = 3516;
    public String pAdESLevel = "LTA";
    public String xAdESLevel = "LTA";
    public String cAdESLevel = "LTA";
    public String sofficePath = "/usr/bin/soffice";
    public List<String> pKCS12File = new ArrayList<String>();


    public List<String> activePlugins = new ArrayList<String>();
    public List<String> availablePlugins = new ArrayList<String>();

    public float pDFImgScaleFactor = 1;
    public String language = "es";
    public String country = "CR";
    public Locale locale = new Locale(language, country);

    public ResourceBundle bundle = ResourceBundle.getBundle("messages", locale);
    public boolean extendDocument = true;

    public Settings() {
        activePlugins.add("cr.libre.firmador.plugins.DummyPlugin");
        activePlugins.add("cr.libre.firmador.plugins.CheckUpdatePlugin");
        availablePlugins.add("cr.libre.firmador.plugins.DummyPlugin");
        availablePlugins.add("cr.libre.firmador.plugins.CheckUpdatePlugin");
    }

    public Settings(Settings newsettings) {
        newsettings.releaseUrlCheck = releaseUrlCheck;
        newsettings.baseUrl = baseUrl;
        newsettings.releaseUrl = releaseUrl;
        newsettings.releaseSnapshotUrl = releaseSnapshotUrl;
        newsettings.checksumUrl = checksumUrl;
        newsettings.checksumSnapshotUrl = checksumSnapshotUrl;
        newsettings.defaultDevelopmentVersion = defaultDevelopmentVersion;
        newsettings.withoutVisibleSign = withoutVisibleSign;
        newsettings.hideSignatureAdvice = hideSignatureAdvice;
        newsettings.overwriteSourceFile = overwriteSourceFile;
        newsettings.reason = reason;
        newsettings.place = place;
        newsettings.contact = contact;
        newsettings.dateFormat = dateFormat;
        newsettings.defaultSignMessage = defaultSignMessage;
        newsettings.signWidth = signWidth;
        newsettings.signHeight = signHeight;
        newsettings.fontSize = fontSize;
        newsettings.font = font;
        newsettings.fontColor = fontColor;
        newsettings.backgroundColor = backgroundColor;
        newsettings.extraPKCS11Lib = extraPKCS11Lib;
        newsettings.signX = signX;
        newsettings.signY = signY;
        newsettings.image = image;
        newsettings.fontAlignment = fontAlignment;
        newsettings.showLogs = showLogs;
        newsettings.pageNumber = pageNumber;
        newsettings.portNumber = portNumber;
        newsettings.pAdESLevel = pAdESLevel;
        newsettings.xAdESLevel = xAdESLevel;
        newsettings.cAdESLevel = cAdESLevel;
        newsettings.sofficePath = sofficePath;
        newsettings.pDFImgScaleFactor = pDFImgScaleFactor;

    }

    public String getDefaultSignMessage() {
        return this.defaultSignMessage;
    }

    public String getDateFormat() {
        try {
            return this.dateFormat;
        } catch (Exception e) {
            Logger.getLogger(Settings.class.getName()).log(Level.SEVERE, null, "Error retornando dateFormat: " + e); // FIXME does try-catch make sense here?
            e.printStackTrace();
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

    public String getFontName(String fontName, boolean isPdf) {
        String selectedFontName = "";
        switch (fontName) {
        case "Arial Regular":
        case "Arial Italic":
        case "Arial Bold":
        case "Arial Bold Italic":
            if (!isPdf) selectedFontName = "Arial";
            else selectedFontName = Font.SANS_SERIF;
            break;
        case "Helvetica Regular":
        case "Helvetica Oblique":
        case "Helvetica Bold":
        case "Helvetica Bold Oblique":
            if (!isPdf) selectedFontName = "Helvetica";
            else selectedFontName = Font.SANS_SERIF;
            break;
        case "Nimbus Sans Regular":
        case "Nimbus Sans Italic":
        case "Nimbus Sans Bold":
        case "Nimbus Sans Bold Italic":
            if (!isPdf) selectedFontName = "Nimbus Sans";
            else selectedFontName = Font.SANS_SERIF;
            break;
        case "Nimbus Roman Regular":
        case "Nimbus Roman Italic":
        case "Nimbus Roman Bold":
        case "Nimbus Roman Bold Italic":
            if (!isPdf) selectedFontName = "Nimbus Roman";
            else selectedFontName = Font.SERIF;
            break;
        case "Times New Roman Regular":
        case "Times New Roman Italic":
        case "Times New Roman Bold":
        case "Times New Roman Bold Italic":
            if (!isPdf) selectedFontName = "Times New Roman";
            else selectedFontName = Font.SERIF;
            break;
        case "Courier New Regular":
        case "Courier New Italic":
        case "Courier New Bold":
        case "Courier New Bold Italic":
            if (!isPdf) selectedFontName = "Courier New";
            else selectedFontName = Font.MONOSPACED;
            break;
        case "Nimbus Mono PS Regular":
        case "Nimbus Mono PS Italic":
        case "Nimbus Mono PS Bold":
        case "Nimbus Mono PS Bold Italic":
            if (!isPdf) selectedFontName = "Nimbus Mono PS";
            else selectedFontName = Font.MONOSPACED;
            break;
        default:
            selectedFontName = Font.SANS_SERIF;
            break;
        }
        return selectedFontName;
    }

    public int getFontStyle(String fontName) {
        switch (fontName) {
        case "Arial Regular":
        case "Courier New Regular":
        case "Helvetica Regular":
        case "Nimbus Roman Regular":
        case "Nimbus Sans Regular":
        case "Nimbus Mono PS Regular":
        case "Times New Roman Regular":
            return Font.PLAIN;
        case "Arial Italic":
        case "Courier New Italic":
        case "Helvetica Oblique":
        case "Nimbus Roman Italic":
        case "Nimbus Sans Italic":
        case "Nimbus Mono PS Italic":
        case "Times New Roman Italic":
            return Font.ITALIC;
        case "Arial Bold":
        case "Courier New Bold":
        case "Helvetica Bold":
        case "Nimbus Roman Bold":
        case "Nimbus Sans Bold":
        case "Nimbus Mono PS Bold":
        case "Times New Roman Bold":
            return Font.BOLD;
        case "Arial Bold Italic":
        case "Courier New Bold Italic":
        case "Helvetica Bold Oblique":
        case "Nimbus Roman Bold Italic":
        case "Nimbus Sans Bold Italic":
        case "Nimbus Mono PS Bold Italic":
        case "Times New Roman Bold Italic":
            return Font.BOLD + Font.ITALIC;
        default:
            return Font.PLAIN;
        }
    }

    public SignerTextPosition getFontAlignment() {
        SignerTextPosition position = SignerTextPosition.RIGHT;
        switch (this.fontAlignment) {
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
        if (this.fontColor.equalsIgnoreCase("transparente")) return new Color(255, 255, 255, 0);
        try {
            return Color.decode(this.fontColor);
        } catch (Exception e) {
            Logger.getLogger(Settings.class.getName()).log(Level.SEVERE, null, "Error decodificando fontColor:" + e);
            e.printStackTrace();
            return new Color(0, 0, 0, 255);
        }
    }
    public Color getBackgroundColor() {
        if (this.backgroundColor.equalsIgnoreCase("transparente")) return new Color(255, 255, 255, 0);
        try {
            return Color.decode(this.backgroundColor);
        } catch (Exception e) {
            Logger.getLogger(Settings.class.getName()).log(Level.SEVERE, null, "Error decodificando backgroundColor: " + e);
            e.printStackTrace();
            return new Color(255, 255, 255, 0);
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
        //if (this.startServer) return true;
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
        switch (pAdESLevel) {
            case "T": level=SignatureLevel.PAdES_BASELINE_T; break;
            case "LT": level=SignatureLevel.PAdES_BASELINE_LT; break;
            case "LTA": level=SignatureLevel.PAdES_BASELINE_LTA; break;
            default: level=SignatureLevel.PAdES_BASELINE_LTA; break;
        }
        return level;
    }

    public SignatureLevel getXAdESLevel() {
        SignatureLevel level = SignatureLevel.XAdES_BASELINE_LTA;
        switch (xAdESLevel) {
            case "T": level=SignatureLevel.XAdES_BASELINE_T; break;
            case "LT": level=SignatureLevel.XAdES_BASELINE_LT; break;
            case "LTA": level=SignatureLevel.XAdES_BASELINE_LTA; break;
            default: level=SignatureLevel.XAdES_BASELINE_LTA; break;
        }
        return level;
    }
    public SignatureLevel getCAdESLevel() {
        SignatureLevel level = SignatureLevel.CAdES_BASELINE_LTA;
        switch (cAdESLevel) {
            case "T": level=SignatureLevel.CAdES_BASELINE_T; break;
            case "LT": level=SignatureLevel.CAdES_BASELINE_LT; break;
            case "LTA": level=SignatureLevel.CAdES_BASELINE_LTA; break;
            default: level=SignatureLevel.CAdES_BASELINE_LTA; break;
        }
        return level;
    }

    public String getVersion() {
        String versionStr = getClass().getPackage().getImplementationVersion();
        if (versionStr == null) versionStr = this.defaultDevelopmentVersion;
        return versionStr;
    }

    public String getReleaseUrl() {
        String version = getVersion();
        if (version.contains("SNAPSHOT")) return this.releaseSnapshotUrl;
        return this.releaseUrl;
    }
    public String getReleaseCheckUrl() {
        String version = getVersion();
        if(version.contains("SNAPSHOT")) {
            return "";
        }
        return this.releaseUrlCheck;
    }
    public String getChecksumUrl() {
        String version = getVersion();
        if(version.contains("SNAPSHOT")) {
            return this.checksumSnapshotUrl;
        }
        return this.checksumUrl;
    }

}
