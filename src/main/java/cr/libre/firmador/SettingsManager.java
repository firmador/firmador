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
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SettingsManager {

    private static SettingsManager cm = new SettingsManager();
    private Path path;
    private Properties props;
    private Settings settings = null;

    public Path get_config_dir() throws IOException {
        String osName = System.getProperty("os.name").toLowerCase();
        String homepath = System.getProperty("user.home");
        String suffixpath=".config/firmadorlibre";
        if(osName.contains("windows")) { 
        	homepath = System.getenv("APPDATA");
        	suffixpath="firmadorlibre";
        }
        
        // Se asegura que siempre exista el directorio de configuracion
        path = FileSystems.getDefault().getPath(homepath, suffixpath);
        if (!Files.isDirectory(path)) {
            Files.createDirectories(path);
            if (osName.contains("windows")) Files.setAttribute(path, "dos:hidden", true);
        }
        return path;
    }

    public Path get_path_config_file(String name) throws IOException{
        if (path == null) {
            path = this.get_config_dir();
            path = path.getFileSystem().getPath(path.toString(), name);
        }
        return path;
    }

    public String get_config_file(String name) throws IOException{
        return this.get_path_config_file(name).toString();
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
        // return unique instance
        return cm;

    }

    public String getProperty(String key) {
        return props.getProperty(key, "");
    }

    public void setProperty(String key, String value) {
        props.setProperty(key, value);
       
    }

    private String get_config_file() throws IOException {
        //Retorna el archivo de configuracion
        String dev="";
        if (this.path == null) {
            dev = this.get_config_file("config.properties");
        }else{
            dev = this.path.toString();
        }
        return dev;
    }

    public boolean load_config() {
        // carga las configuraciones desde un archivo de texto
        File configFile;
        boolean loaded=false;
        try {
            configFile = new File(this.get_config_file());
            if (configFile.exists()) {
                InputStream inputStream = new FileInputStream(configFile);
                Reader reader = new InputStreamReader(inputStream, "UTF-8");
                props.load(reader);
                reader.close();
                inputStream.close();
                loaded=true;
            }
        } catch (IOException ex) {
            Logger.getLogger(SettingsManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        return loaded;
    }

    public void save_config() {
        // Guarda las configuraciones en un archivo de texto
        //File configFile = null;
        OutputStreamWriter writer = null;
        //props.setProperty("formato", "json");
        try {
            writer = new OutputStreamWriter(new FileOutputStream(this.get_config_file()), StandardCharsets.UTF_8);
            //writer = new FileWriter(this.get_config_file());
            props.store(writer, "Firmador Libre settings");

        } catch (IOException ex) {
            Logger.getLogger(SettingsManager.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                if (writer != null) {
                    writer.close();
                }
            } catch (IOException ex) {
                Logger.getLogger(SettingsManager.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    private List<String> getListFromString(String data, List<String> defaultdata){
    	// Si no se tienen settings activados se ponen los que se definan por defecto en el código
    	if(data.isEmpty() && defaultdata != null && defaultdata.size()>0 ) return defaultdata;  
    	
    	List<String> plugins = new ArrayList<String>();
    	for(String item: Arrays.asList(data.split("\\|"))){
    		if(!item.isEmpty())plugins.add(item);
    	}
    	return plugins;
    }

    public Settings getSettings() {
        Settings conf = new Settings();
        boolean loaded =this.load_config();

        if (loaded) {
            conf.withoutvisiblesign=Boolean.parseBoolean(props.getProperty("withoutvisiblesign", String.valueOf(conf.withoutvisiblesign)));
            conf.uselta=Boolean.parseBoolean(props.getProperty("uselta", String.valueOf(conf.uselta)));
            conf.showlogs = Boolean.parseBoolean(props.getProperty("showlogs", String.valueOf(conf.showlogs)));
            conf.overwritesourcefile=Boolean.parseBoolean(props.getProperty("overwritesourcefile", String.valueOf(conf.overwritesourcefile)));
            conf.reason=props.getProperty("reason", conf.reason);
            conf.place=props.getProperty("place", conf.place);
            conf.contact=props.getProperty("contact", conf.contact);
            conf.dateformat=props.getProperty("dateformat", conf.dateformat);
            conf.defaultsignmessage=new String(props.getProperty("defaultsignmessage", conf.defaultsignmessage).getBytes(StandardCharsets.UTF_8));
            conf.pagenumber=Integer.parseInt(props.getProperty("pagenumber", conf.pagenumber.toString()));
            conf.signwith=Integer.parseInt(props.getProperty("signwith", conf.signwith.toString()));
            conf.signheight=Integer.parseInt(props.getProperty("signheight", conf.signheight.toString()));
            conf.fontsize=Integer.parseInt(props.getProperty("fontsize", conf.fontsize.toString()));
            conf.font = props.getProperty("font", conf.font);
            conf.fontcolor = props.getProperty("fontcolor", conf.fontcolor);
            conf.backgroundcolor = props.getProperty("backgroundcolor", conf.backgroundcolor);
            conf.signx=Integer.parseInt(props.getProperty("singy", conf.signx.toString()));
            conf.signy=Integer.parseInt(props.getProperty("singy", conf.signy.toString()));
            conf.image = props.getProperty("image");
            conf.startserver = Boolean.parseBoolean(props.getProperty("startserver", String.valueOf(conf.startserver)));
            conf.fontalignment =  props.getProperty("fontalignment", conf.fontalignment);
            conf.portnumber=Integer.parseInt(props.getProperty("portnumber", conf.portnumber.toString()));
            conf.padesLevel = props.getProperty("padesLevel", conf.padesLevel);
            conf.xadesLevel = props.getProperty("xadesLevel", conf.xadesLevel);
            conf.cadesLevel = props.getProperty("cadesLevel", conf.cadesLevel);
            conf.extrapkcs11Lib=props.getProperty("extrapkcs11Lib");
            conf.pkcs12file=getListFromString(props.getProperty("pkcs12file", ""), conf.pkcs12file);
            conf.active_plugins=getListFromString(props.getProperty("plugins", ""), conf.active_plugins);
            conf.pdfImgScaleFactor=getFloatFromString(props.getProperty("pdfimgscalefactor", String.format("%.2f", conf.pdfImgScaleFactor)));
        }
        return conf;
    }

    private float getFloatFromString(String value) {
    	String valuetmp=value.replace(",", ".");
    	float fvalue = 1;
    	 try {
    		 fvalue = Float.parseFloat(valuetmp);
         } catch (Exception e) {
             fvalue=1;
         }	
    	return fvalue;
    }
    
    private String getListRepr(List<String> items) {
    	return String.join("|", items);
    }
    
    public void setSettings(Settings conf, boolean save) {
        setProperty("withoutvisiblesign", String.valueOf(conf.withoutvisiblesign));
        setProperty("uselta", String.valueOf(conf.uselta));
        setProperty("overwritesourcefile", String.valueOf(conf.overwritesourcefile));
        setProperty("reason", conf.reason);
        setProperty("place", conf.place);
        setProperty("contact", conf.contact);
        setProperty("dateformat", conf.dateformat);
        setProperty("defaultsignmessage", conf.defaultsignmessage);
        setProperty("pagenumber", conf.pagenumber.toString());
        setProperty("signwith", conf.signwith.toString());
        setProperty("signheight", conf.signheight.toString());
        setProperty("fontsize", conf.fontsize.toString());
        setProperty("font", conf.font);
        setProperty("fontcolor", conf.fontcolor);
        setProperty("backgroundcolor", conf.backgroundcolor);
        setProperty("singx", conf.signx.toString());
        setProperty("singy", conf.signy.toString());
        setProperty("startserver", String.valueOf(conf.startserver));
        setProperty("fontalignment", conf.fontalignment.toString());
        setProperty("portnumber", conf.portnumber.toString());
        setProperty("showlogs", String.valueOf(conf.showlogs));
        setProperty("pdfimgscalefactor", String.format("%.2f", conf.pdfImgScaleFactor));
                
        setProperty("padesLevel", conf.padesLevel);
        setProperty("xadesLevel", conf.xadesLevel);
        setProperty("cadesLevel", conf.cadesLevel);
        setProperty("plugins", getListRepr(conf.active_plugins));
                
        if (conf.extrapkcs11Lib != null && conf.extrapkcs11Lib != "") {
            setProperty("extrapkcs11Lib", conf.extrapkcs11Lib);
        }else {
        	if(props.get("extrapkcs11Lib") != null) props.remove("extrapkcs11Lib");
        }
        
        setProperty("pkcs12file", getListRepr(conf.pkcs12file));
        
        if (conf.image != null) {
            setProperty("image", conf.image);
        }else {
        	if(props.get("image") != null) props.remove("image");
        }
        if (save) save_config();
    }

    public Settings get_and_create_settings() {
        if (this.settings != null) {
            return this.settings;
        }

        Settings dev = new Settings();
        try {
            // Check if file exists
            if (this.path == null) {
                //String cpath = get_config_file("config.properties");
            } else {
                if (!Files.exists(this.path)) {
                     Logger.getLogger(SettingsManager.class.getName()).log(Level.SEVERE, null, "Config File does not exists");
                    return dev;
                }
            }
            dev = getSettings();
        } catch (Exception e) {
            e.printStackTrace();
            setSettings(dev, true);
        }
        this.settings = dev;
        return dev;
    }

}
