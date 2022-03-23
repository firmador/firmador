package cr.libre.firmador.plugins;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.swing.JOptionPane;
import javax.swing.SwingWorker;
import javax.xml.bind.DatatypeConverter;

import org.slf4j.LoggerFactory;

import cr.libre.firmador.Settings;
import cr.libre.firmador.SettingsManager;
import cr.libre.firmador.gui.swing.CopyableJLabel;


public class CheckUpdatePlugin implements Plugin, Runnable {
	private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(CheckUpdatePlugin.class);
	public boolean isrunnable = true;
	private class ExecutorWorker extends SwingWorker<Void, Void> {

		@Override
		protected Void doInBackground() throws Exception {
			try {
				Thread.sleep(10*1000);
			} catch (InterruptedException e1) { e1.printStackTrace();}
		
			Settings settings = SettingsManager.getInstance().get_and_create_settings();
			String version=settings.getVersion();
			
			try {
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
				if(!version.contentEquals(responseversion) && !version.contentEquals(settings.defaultdevelopmentversion)) {
				//if(!version.contentEquals(responseversion)) {
					String message = "Hay una versión nueva disponible, por favor actualice con prontitud a la nueva versión desde: <br> "+settings.base_url;

					if(can_write_path()) {
						int answer = JOptionPane.showConfirmDialog(null, new CopyableJLabel(message), "Desea descargar la actualización de Firmador disponible", JOptionPane.YES_NO_OPTION);
						if(answer== JOptionPane.YES_OPTION) {
							update_jar();
						}
					}else {
						JOptionPane.showMessageDialog(null, new CopyableJLabel(message), "Actualización de Firmador disponible", JOptionPane.INFORMATION_MESSAGE);
					}
				}
			} catch (MalformedURLException e) {
				LOG.error(e.toString());
			}catch (IOException e) {
				LOG.error(e.toString());
			}
			return null;
		}
		
		public String get_remote_checksum() {
			Settings settings = SettingsManager.getInstance().get_and_create_settings();
			String checksum = "";
			try {
				URL url = new URL(settings.getChecksumUrl());
				HttpURLConnection connection = (HttpURLConnection) url.openConnection();
				StringBuilder textBuilder = new StringBuilder();
			    try (Reader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), 
			    		Charset.forName(StandardCharsets.UTF_8.name())))) {
			        int c = 0;
			        while ((c = reader.read()) != -1) {
			            textBuilder.append((char) c);
			        }
			    }
			    checksum = textBuilder.toString();
			} catch (MalformedURLException e) {
				LOG.error(e.toString());
			}catch (IOException e) {
				LOG.error(e.toString());
			}
			return checksum.toUpperCase();
		}
		
		public Path get_jar_path() throws URISyntaxException{
			String jarPath = getClass()
	          .getProtectionDomain()
	          .getCodeSource()
	          .getLocation()
	          .toURI()
	          .getPath();
			
			return FileSystems.getDefault().getPath(jarPath);
			
		}
		
		public boolean can_write_path()  {
			boolean dev=false;
			Path path;
			try {
				path = get_jar_path();
				dev= Files.isWritable(path) && !Files.isDirectory(path);
			} catch (URISyntaxException e) {
				dev=false;
			}
			return dev;
			
		}
		public boolean check_md5sum(Path tmpfile) throws IOException, NoSuchAlgorithmException {
			byte[] b = Files.readAllBytes(tmpfile);
			byte[] hash = MessageDigest.getInstance("MD5").digest(b);
			String hexmd5 = DatatypeConverter.printHexBinary(hash).toUpperCase();
			LOG.info("MD5 of Downloaded file "+hexmd5);
			String remotecheck=get_remote_checksum();
			//return remotecheck.contentEquals(hexmd5);
			return true;
		}
		
		public void update_jar() throws Exception {
			Settings settings = SettingsManager.getInstance().get_and_create_settings();
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
		 
		
		    if(check_md5sum(tempFile)) {
				Path jarfile = get_jar_path();
				LOG.info("Copying downloader file to "+jarfile.toString());
				Files.copy(tempFile, jarfile, StandardCopyOption.REPLACE_EXISTING);
				File file = new File(jarfile.toString());
				file.setExecutable(true);
				file.setWritable(true);
				String message="Nueva versión ha sido descargada con éxito, debe reiniciar la aplicación";
				JOptionPane.showMessageDialog(null, new CopyableJLabel(message), "Actualización exitosa", JOptionPane.INFORMATION_MESSAGE);
		    }

		}
		
	}
	
	public void start() {
		LOG.info("Stating CheckUpdatePlugin");
		
	}
	@Override
	public void start_loggin() {}
	
	@Override
	public void stop() {
		LOG.info("Stop CheckUpdatePlugin");
		
	}
	@Override
	public void run() {
		ExecutorWorker task = new ExecutorWorker();
		task.execute();
	}
	@Override
	public boolean get_isrunnable() {
		return true;
	}

}
