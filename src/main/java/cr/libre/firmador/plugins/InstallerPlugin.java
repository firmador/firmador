package cr.libre.firmador.plugins;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.CodeSource;

import javax.imageio.ImageIO;
import javax.swing.JOptionPane;

import org.apache.commons.io.FileUtils;

import cr.libre.firmador.MessageUtils;
import cr.libre.firmador.SettingsManager;

public class InstallerPlugin implements Plugin {
    private String linuxDesktopPath = "%home%/.local/share/applications/firmadorlibre.desktop";
    private String linuxExecPath = "%configpath%/firmador.jar";
    private String linuxIconPath = "%configpath%/firmador.png";
    private String linuxDesktop = "[Desktop Entry]\n" + "Encoding=UTF-8\n" + "Version=1.0\n" + "Type=Application\n"
            + "Terminal=false\n" + "Exec=java -jar %path% %u\n" + "Name=Firmador Libre\n" + "Icon=%iconpath%\n"
            + "Categories=GTK;Office;Viewer;\n"
            + "GenericName=Firmador Libre\n"
            + "Comment=Firmador de documentos con firma digital avanzada de Costa Rica\n"
            + "MimeType=MimeType=x-scheme-handler/flsign;x-scheme-handler/flauth;application/pdf;application/vnd.oasis.opendocument.text;application/vnd.oasis.opendocument.spreadsheet;application/vnd.oasis.opendocument.presentation;application/vnd.oasis.opendocument.graphics;application/vnd.openxmlformats-officedocument.wordprocessingml.document;application/vnd.openxmlformats-officedocument.spreadsheetml.sheet;application/vnd.openxmlformats-officedocument.presentationml.presentation;";

    private String powershellInstallWindows = "";

    private Path getConfigDir() throws IOException {
        SettingsManager settingsmanager = SettingsManager.getInstance();
        return settingsmanager.getConfigDir();

    }

    public Path getlinuxIconPath() throws Throwable {
        Path configdir = getConfigDir();
        Path configPath = Paths.get(linuxIconPath.replace("%configpath%", configdir.toAbsolutePath().toString()));
        return configPath;
    }
    public Path getLinuxExecPath() throws IOException {
        Path configdir = getConfigDir();
        Path execPath = Paths.get(linuxExecPath.replace("%configpath%", configdir.toAbsolutePath().toString()));
        return execPath;
    }

    public Path getMacExecPath() throws IOException {
        Path configdir = getConfigDir();
        Path execPath = Paths.get(linuxExecPath.replace("%configpath%", configdir.toAbsolutePath().toString()));
        return execPath;
    }

    public Path getLinuxDesktopPath() {
        // String username = System.getProperty("user.name");
        String homepath = System.getProperty("user.home");
        return Paths.get(linuxDesktopPath.replace("%home%", homepath));
    }


    public Path getJarPath() throws URISyntaxException {
        CodeSource codeSource = this.getClass().getProtectionDomain().getCodeSource();
        return Paths.get(codeSource.getLocation().toURI());
    }

    private boolean is_installed() throws IOException {
        boolean installed;
        if(Boolean.parseBoolean(System.getenv("FIRMADORINFLATPAK"))){
            installed = true;
        }else {
            Path execpath = null;
            String osName = System.getProperty("os.name").toLowerCase();
            if (osName.contains("mac"))
                execpath = getMacExecPath();
            else if (osName.contains("linux"))
                execpath = getLinuxExecPath();
            else if (osName.contains("windows"))
                execpath = getWindowsExecPath();

            installed = Files.exists(execpath);
        }
        return installed;
    }

    public void install_on_mac() {

    }

    public void install_on_linux() throws Throwable {
        Path source = getJarPath();
        Path dest = getLinuxExecPath();
        FileUtils.copyFile(source.toFile(), dest.toFile());

        String desktopstr = linuxDesktop.replace("%path%", dest.toString()).replace("%iconpath%",
                getlinuxIconPath().toString());
        File file = getLinuxDesktopPath().toFile();
        FileUtils.write(file, desktopstr, StandardCharsets.UTF_8);
        Path iconpath = getlinuxIconPath();
        File outputfile = iconpath.toFile();
        ImageIO.write(ImageIO.read(this.getClass().getClassLoader().getResource("firmador.png")), "png", outputfile);
        try {

            Runtime.getRuntime().exec("xdg-mime default firmadorlibre.desktop x-scheme-handler/flsign");
            Runtime.getRuntime().exec("xdg-mime default firmadorlibre.desktop x-scheme-handler/flauth");
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    public Path getWindowsExecPath() {
        String homepath = System.getenv("APPDATA");
        return Paths.get(homepath + "/firmadorlibre/firmador.jar");
    }

    public Path getWindowsIconPath() {
        String homepath = System.getenv("APPDATA");
        return Paths.get(homepath + "/firmadorlibre/icon.ico");
    }

    public void install_on_windows() {

        try {
            Path iconpath = getWindowsIconPath();

            Path temp = Files.createTempFile("fsinstall", ".vbs");
            URL iconUrl = this.getClass().getClassLoader().getResource("icon.ico");
            FileUtils.copyURLToFile(iconUrl, iconpath.toFile());

            URL ps1Url = this.getClass().getClassLoader().getResource("install_windows.vbs");
            FileUtils.copyURLToFile(ps1Url, temp.toFile());

            Path source = getJarPath();
            Path dest = getWindowsExecPath();
            FileUtils.copyFile(source.toFile(), dest.toFile());


            try {

                Runtime.getRuntime()
                        .exec("powershell -Command \"Start-Process 'cmd' -Verb RunAs -ArgumentList '/c \" wscript "
                                + temp.toAbsolutePath().toString() + "\"'\"");
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            // String[] commandList = { "wscript", temp.toAbsolutePath().toString() };

//            ProcessBuilder pb = new ProcessBuilder(commandList);
            // Process p = pb.start();
        } catch (IOException | URISyntaxException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    public void install() {
        String osName = System.getProperty("os.name").toLowerCase();
        if (osName.contains("mac"))
            install_on_mac();
        else if (osName.contains("linux"))
            try {
                install_on_linux();
            } catch (Throwable e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        else if (osName.contains("windows"))
            install_on_windows();
    }

    @Override
    public void start() {



        int action = 0;
        try {
            if (!is_installed()) {
                action = JOptionPane.showConfirmDialog(null, MessageUtils.t("plugin_installer_message"),
                        MessageUtils.t("plugin_installer_title"),
                        JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
                if (action == JOptionPane.OK_OPTION) {
                    install();
                }
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Override
    public void startLogging() {
        // TODO Auto-generated method stub

    }

    @Override
    public void stop() {
        // TODO Auto-generated method stub

    }

    @Override
    public boolean getIsRunnable() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean interactWithDocuments() {
        return false;
    }
}
