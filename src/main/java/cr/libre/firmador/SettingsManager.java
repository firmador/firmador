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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SettingsManager {
    private static SettingsManager cm = new SettingsManager();
    private Path path;
    private Properties props;
    private Settings settings = null;

    public Path getConfigDir() throws IOException {
        String osName = System.getProperty("os.name").toLowerCase();
        String homepath = System.getProperty("user.home");
        String suffixpath = ".config/firmadorlibre";
        if (osName.contains("windows")) {
            homepath = System.getenv("APPDATA");
            suffixpath = "firmadorlibre";
        }
        // Se asegura que siempre exista el directorio de configuracion
        path = FileSystems.getDefault().getPath(homepath, suffixpath);
        if (!Files.isDirectory(path)) {
            Files.createDirectories(path);
            if (osName.contains("windows")) Files.setAttribute(path, "dos:hidden", true);
        }
        return path;
    }

    public Path getPathConfigFile(String name) throws IOException{
        if (path == null) {
            path = this.getConfigDir();
            path = path.getFileSystem().getPath(path.toString(), name);
        }
        return path;
    }

    public String getConfigFile(String name) throws IOException{
        return this.getPathConfigFile(name).toString();
    }

    public Path getPath() {
        return path;
    }

    public void setPath(Path path) {
        this.path = path;
    }

    public void setPath(String path) {
        this.path = FileSystems.getDefault().getPath(path);
    }

    private SettingsManager() {
        super();
        this.path = null;
        props = new Properties();
    }

    public static SettingsManager getInstance() {
        return cm; // return unique instance
    }

    public String getProperty(String key) {
        return props.getProperty(key, "");
    }

    public void setProperty(String key, String value) {
        props.setProperty(key, value);

    }

    private String getConfigFile() throws IOException {
        // Retorna el archivo de configuracion
        if (this.path == null) return this.getConfigFile("config.properties");
        else return this.path.toString();
    }

    public boolean loadConfig() {
        // Carga las configuraciones desde un archivo de texto
        File configFile;
        boolean loaded = false;
        try {
            configFile = new File(this.getConfigFile());
            if (configFile.exists()) {
                InputStream inputStream = new FileInputStream(configFile);
                Reader reader = new InputStreamReader(inputStream, "UTF-8");
                props.load(reader);
                reader.close();
                inputStream.close();
                loaded = true;
            }
        } catch (IOException ex) {
            Logger.getLogger(SettingsManager.class.getName()).log(Level.SEVERE, null, ex);
            ex.printStackTrace();
        }
        return loaded;
    }

    public void saveConfig() {
        // Guarda las configuraciones en un archivo de texto
        //File configFile = null;
        OutputStreamWriter writer = null;
        try {
            writer = new OutputStreamWriter(new FileOutputStream(this.getConfigFile()), StandardCharsets.UTF_8);
            //writer = new FileWriter(this.getConfigFile());
            props.store(writer, "Firmador Libre settings");
        } catch (IOException ex) {
            Logger.getLogger(SettingsManager.class.getName()).log(Level.SEVERE, null, ex); // FIXME using JUL instead of SLF4j, could use just a single logger
            ex.printStackTrace();
        } finally {
            try {
                if (writer != null) writer.close();
            } catch (IOException ex) {
                Logger.getLogger(SettingsManager.class.getName()).log(Level.SEVERE, null, ex);
                ex.printStackTrace();
            }
        }
    }
    private List<String> getListFromString(String data, List<String> defaultdata){
        // Si no se tienen settings activados se ponen los que se definan por defecto en el código
        if (data.isEmpty() && defaultdata != null && defaultdata.size() > 0) return defaultdata;
        List<String> plugins = new ArrayList<String>();
        for (String item : Arrays.asList(data.split("\\|"))) if (!item.isEmpty()) plugins.add(item);
        return plugins;
    }

    public Settings getSettings() {
        Settings conf = new Settings();
        boolean loaded = this.loadConfig();
        if (loaded) {
            conf.withoutVisibleSign = Boolean.parseBoolean(props.getProperty("withoutvisiblesign", String.valueOf(conf.withoutVisibleSign)));
            //conf.useLTA = Boolean.parseBoolean(props.getProperty("uselta", String.valueOf(conf.useLTA)));
            conf.showLogs = Boolean.parseBoolean(props.getProperty("showlogs", String.valueOf(conf.showLogs)));
            conf.overwriteSourceFile = Boolean.parseBoolean(props.getProperty("overwritesourcefile", String.valueOf(conf.overwriteSourceFile)));
            conf.reason = props.getProperty("reason", conf.reason);
            conf.place = props.getProperty("place", conf.place);
            conf.contact = props.getProperty("contact", conf.contact);
            conf.dateFormat = props.getProperty("dateformat", conf.dateFormat);
            conf.defaultSignMessage = props.getProperty("defaultsignmessage", conf.defaultSignMessage);
            conf.pageNumber = Integer.parseInt(props.getProperty("pagenumber", conf.pageNumber.toString()));
            conf.signWidth = Integer.parseInt(props.getProperty("signwidth", conf.signWidth.toString()));
            conf.signHeight = Integer.parseInt(props.getProperty("signheight", conf.signHeight.toString()));
            conf.fontSize = Integer.parseInt(props.getProperty("fontsize", conf.fontSize.toString()));
            conf.font = props.getProperty("font", conf.font);
            conf.fontColor = props.getProperty("fontcolor", conf.fontColor);
            conf.backgroundColor = props.getProperty("backgroundcolor", conf.backgroundColor);
            conf.signX = Integer.parseInt(props.getProperty("singx", conf.signX.toString()));
            conf.signY = Integer.parseInt(props.getProperty("singy", conf.signY.toString()));
            conf.image = props.getProperty("image");
            //conf.startServer = Boolean.parseBoolean(props.getProperty("startserver", String.valueOf(conf.startServer)));
            conf.fontAlignment =  props.getProperty("fontalignment", conf.fontAlignment);
            conf.portNumber = Integer.parseInt(props.getProperty("portnumber", conf.portNumber.toString()));
            conf.pAdESLevel = props.getProperty("padesLevel", conf.pAdESLevel);
            conf.xAdESLevel = props.getProperty("xadesLevel", conf.xAdESLevel);
            conf.cAdESLevel = props.getProperty("cadesLevel", conf.cAdESLevel);
            conf.extraPKCS11Lib = props.getProperty("extrapkcs11Lib");
            conf.pKCS12File = getListFromString(props.getProperty("pkcs12file", ""), conf.pKCS12File);
            conf.activePlugins = getListFromString(props.getProperty("plugins", ""), conf.activePlugins);
            conf.pDFImgScaleFactor = getFloatFromString(props.getProperty("pdfimgscalefactor", String.format("%.2f", conf.pDFImgScaleFactor)));
        }
        return conf;
    }

    private float getFloatFromString(String value) {
        String valueTmp = value.replace(",", ".");
        float fValue = 1;
        try {
            fValue = Float.parseFloat(valueTmp);
        } catch (Exception e) {
            Logger.getLogger(SettingsManager.class.getName()).log(Level.SEVERE, null, e);
            e.printStackTrace();
            fValue = 1;
        }
        return fValue;
    }

    private String getListRepr(List<String> items) {
        return String.join("|", items);
    }

    public void setSettings(Settings conf, boolean save) {
        setProperty("withoutvisiblesign", String.valueOf(conf.withoutVisibleSign));
        //setProperty("uselta", String.valueOf(conf.useLTA));
        setProperty("overwritesourcefile", String.valueOf(conf.overwriteSourceFile));
        setProperty("reason", conf.reason);
        setProperty("place", conf.place);
        setProperty("contact", conf.contact);
        setProperty("dateformat", conf.dateFormat);
        setProperty("defaultsignmessage", conf.defaultSignMessage);
        setProperty("pagenumber", conf.pageNumber.toString());
        setProperty("signwidth", conf.signWidth.toString());
        setProperty("signheight", conf.signHeight.toString());
        setProperty("fontsize", conf.fontSize.toString());
        setProperty("font", conf.font);
        setProperty("fontcolor", conf.fontColor);
        setProperty("backgroundcolor", conf.backgroundColor);
        setProperty("singx", conf.signX.toString());
        setProperty("singy", conf.signY.toString());
        //setProperty("startserver", String.valueOf(conf.startServer));
        setProperty("fontalignment", conf.fontAlignment.toString());
        setProperty("portnumber", conf.portNumber.toString());
        setProperty("showlogs", String.valueOf(conf.showLogs));
        setProperty("pdfimgscalefactor", String.format("%.2f", conf.pDFImgScaleFactor));
        setProperty("padesLevel", conf.pAdESLevel);
        setProperty("xadesLevel", conf.xAdESLevel);
        setProperty("cadesLevel", conf.cAdESLevel);
        setProperty("plugins", getListRepr(conf.activePlugins));
        if (conf.extraPKCS11Lib != null && conf.extraPKCS11Lib != "") setProperty("extrapkcs11Lib", conf.extraPKCS11Lib);
        else if (props.get("extrapkcs11Lib") != null) props.remove("extrapkcs11Lib");
        setProperty("pkcs12file", getListRepr(conf.pKCS12File));
        if (conf.image != null) setProperty("image", conf.image);
        else if (props.get("image") != null) props.remove("image");
        if (save) saveConfig();
    }

    public Settings getAndCreateSettings() {
        if (this.settings != null) return this.settings;
        Settings dev = new Settings();
        try {
            if (this.path != null) if (!Files.exists(this.path)) {
                Logger.getLogger(SettingsManager.class.getName()).log(Level.SEVERE, null, "Config File does not exist");
                return dev;
            }
            dev = getSettings();
        } catch (Exception e) {
            Logger.getLogger(SettingsManager.class.getName()).log(Level.SEVERE, null, e);
            e.printStackTrace();
            setSettings(dev, true);
        }
        this.settings = dev;
        return dev;
    }

}
