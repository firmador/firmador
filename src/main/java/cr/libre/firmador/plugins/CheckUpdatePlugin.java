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

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.invoke.MethodHandles;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import javax.swing.JOptionPane;
import javax.swing.SwingWorker;

import eu.europa.esig.dss.model.Digest;
import eu.europa.esig.dss.spi.DSSUtils;
import eu.europa.esig.dss.enumerations.DigestAlgorithm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cr.libre.firmador.Settings;
import cr.libre.firmador.SettingsManager;
import cr.libre.firmador.gui.swing.CopyableJLabel;

public class CheckUpdatePlugin implements Plugin, Runnable {
    final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    public boolean isrunnable = true;
    private Settings settings;

    private class ExecutorWorker extends SwingWorker<Void, Void> {
        protected String getFileDigest(Path jarfile) throws IOException {
            LOG.info("Reading file for sha digest: "+jarfile.toString());
            Digest digest = new Digest(DigestAlgorithm.SHA256, DSSUtils.digest(DigestAlgorithm.SHA256, Files.readAllBytes(jarfile)));
            return  digest.getHexValue().toUpperCase();
        }

        protected Void updateDevelopment() throws IOException, URISyntaxException {
            String remoteHash = getRemoteHash();
            LOG.info("Remote SHA256: " + remoteHash);
            String localHash = getFileDigest(getJarPath());
            LOG.info("Local SHA256: " + localHash);

            if (!remoteHash.contains(localHash)) {
                String message = "Hay una versión nueva disponible, por favor actualice con prontitud a la nueva versión desde: <br> " + settings.baseUrl;

                if (canWritePath()) {
                    int answer = JOptionPane.showConfirmDialog(null, new CopyableJLabel(message), "Desea descargar la actualización de Firmador disponible", JOptionPane.YES_NO_OPTION);
                    if (answer== JOptionPane.YES_OPTION) {
                        try {
                            updateJar();
                        } catch (Throwable e) {
                            LOG.error("Error al actualizar el JAR", e.getMessage());
                            e.printStackTrace();
                        }
                    }
                } else {
                    JOptionPane.showMessageDialog(null, new CopyableJLabel(message), "Actualización de Firmador disponible", JOptionPane.INFORMATION_MESSAGE);
                }
            }
            return null;
        }

        protected Void updateRelease() throws IOException {

            URL url = new URL(settings.getReleaseCheckUrl());
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            StringBuilder textBuilder = new StringBuilder();
            try (Reader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(),
                    Charset.forName(StandardCharsets.UTF_8.name())))) {
                int c = 0;
                while ((c = reader.read()) != -1) {
                    textBuilder.append((char) c);
                }
            }
            String responseversion = textBuilder.toString();
            String version=settings.getVersion();
            if (!version.contentEquals(responseversion)) {
                String message = "Hay una versión nueva disponible, por favor actualice con prontitud a la nueva versión desde: <br> " + settings.baseUrl;

                if (canWritePath()) {
                    int answer = JOptionPane.showConfirmDialog(null, new CopyableJLabel(message), "Desea descargar la actualización de Firmador disponible", JOptionPane.YES_NO_OPTION);
                    if (answer == JOptionPane.YES_OPTION) {
                        try {
                            updateJar();
                        } catch (Throwable e) {
                            LOG.error("Error al actualizar el JAR", e.getMessage());
                            e.printStackTrace();
                        }
                    }
                } else {
                    JOptionPane.showMessageDialog(null, new CopyableJLabel(message), "Actualización de Firmador disponible", JOptionPane.INFORMATION_MESSAGE);
                }
            }
            return null;
        }

        @Override
        protected Void doInBackground()  {
            //try {
                //Thread.sleep(10*1000);
            //} catch (InterruptedException e1) { e1.printStackTrace();}
            settings = SettingsManager.getInstance().getAndCreateSettings();
            String version=settings.getVersion();
            try {
                if (version.contains("SNAPSHOT")) {
                    LOG.info("Updating development version");
                    updateDevelopment();
                } else {
                    LOG.info("Updating release version");
                    updateRelease();
                }
            } catch (IOException e) {
                LOG.error("Error de E/S al actualizar versión", e.getMessage());
                e.printStackTrace();
            } catch (URISyntaxException e) {
                LOG.error("Error en URL al actualizar versión", e.getMessage());
                e.printStackTrace();
            }
            return null;
        }

        public String getRemoteHash() {
            Settings settings = SettingsManager.getInstance().getAndCreateSettings();
            String hash = "";
            try {
                URL url = new URL(settings.getChecksumUrl());
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                StringBuilder textBuilder = new StringBuilder();
                try (Reader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), Charset.forName(StandardCharsets.UTF_8.name())))) {
                    int c = 0;
                    while ((c = reader.read()) != -1) textBuilder.append((char) c);
                }
                hash = textBuilder.toString();
            } catch (MalformedURLException e) {
                LOG.error("Error en URL al obtener hash", e.toString());
                e.printStackTrace();
            } catch (IOException e) {
                LOG.error("Error de E/S al obtener hash", e.toString());
                e.printStackTrace();
            }
            return hash.toUpperCase();
        }

        public Path getJarPath() throws URISyntaxException {
            return Paths.get(getClass().getProtectionDomain().getCodeSource().getLocation().toURI());
        }

        public boolean canWritePath()  {
            boolean dev=false;
            Path path;
            try {
                path = getJarPath();
                dev= Files.isWritable(path) && !Files.isDirectory(path);
            } catch (URISyntaxException e) {
                LOG.error("Error al obtener ruta al JAR", e.getMessage());
                e.printStackTrace();
                dev=false;
            }
            return dev;
        }

        public boolean checkHash(Path tmpfile) throws IOException {
            String hexsha = getFileDigest(tmpfile);
            LOG.info("Sha256 of Downloaded file "+hexsha);
            String remoteCheck = getRemoteHash();
            LOG.info("SHA256 of Remote: "+remoteCheck);
            return remoteCheck.contains(hexsha);

        }

        public void copyFile(Path source, Path dest) throws IOException {
            byte[] data = Files.readAllBytes(source);
            Files.write(dest, data);
        }

        public void updateJar() throws Exception {
            Settings settings = SettingsManager.getInstance().getAndCreateSettings();
            String downloadurl=settings.getReleaseUrl();
            LOG.info("Downloading from "+downloadurl);
            URL url = new URL(downloadurl);
            Path tempFile = Files.createTempFile(null, null);
            BufferedInputStream in = new BufferedInputStream(url.openStream());
            FileOutputStream fileOutputStream = new FileOutputStream(tempFile.toString());
            byte dataBuffer[] = new byte[1024];
            int bytesRead;
            while ((bytesRead = in.read(dataBuffer, 0, 1024)) != -1) {
                fileOutputStream.write(dataBuffer, 0, bytesRead);
            }
            fileOutputStream.close();
            if (checkHash(tempFile)) {
                Path jarfile = getJarPath();
                LOG.info("Copying downloader file to " + jarfile.toString());
                copyFile(tempFile, jarfile);
                //Files.copy(tempFile, jarfile, StandardCopyOption.REPLACE_EXISTING);
                File file = new File(jarfile.toString());
                file.setExecutable(true);
                file.setWritable(true);
                String message="Nueva versión ha sido descargada con éxito, debe reiniciar la aplicación.";
                if (System.getProperty("os.name").toLowerCase().contains("mac"))
                    message += "\nRecuerde que tras descargar el jar, la primera vez debe hacer control+clic para poder abrirlo.";
                JOptionPane.showMessageDialog(null, new CopyableJLabel(message), "Actualización exitosa", JOptionPane.INFORMATION_MESSAGE);
            }

        }

    }

    public void start() {
        LOG.info("Starting CheckUpdatePlugin");
    }

    @Override
    public void startLogging() {}

    @Override
    public void stop() {
        LOG.info("Stopping CheckUpdatePlugin");
    }

    @Override
    public void run() {
        ExecutorWorker task = new ExecutorWorker();
        task.execute();
    }

    @Override
    public boolean getIsRunnable() {
        return true;
    }

}
