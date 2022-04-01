package cr.libre.firmador.plugins;
import eu.europa.esig.dss.enumerations.DigestAlgorithm;
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
import eu.europa.esig.dss.model.Digest;
import eu.europa.esig.dss.spi.DSSUtils;

public class CheckUpdatePlugin implements Plugin, Runnable {
	private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(CheckUpdatePlugin.class);
	public boolean isrunnable = true;
	private Settings settings;
	private class ExecutorWorker extends SwingWorker<Void, Void> {
		
		
		protected String getFileDigest(Path jarfile) throws IOException {			
			Digest digest = new Digest(DigestAlgorithm.SHA256, 
					DSSUtils.digest(DigestAlgorithm.SHA256, Files.readAllBytes(jarfile)));

			return  digest.getHexValue().toUpperCase();
		}

		
		protected Void updateDevelopment() throws IOException, URISyntaxException {
			String remotesha=get_remote_checksum();
			LOG.info("Remote sha: "+remotesha);
			String localsha=getFileDigest(get_jar_path());		
			LOG.info("Local sha: "+localsha);
			
			if(!remotesha.contains(localsha)) {
				String message = "Hay una versión nueva disponible, por favor actualice con prontitud a la nueva versión desde: <br> "+settings.base_url;

				if(can_write_path()) {
					int answer = JOptionPane.showConfirmDialog(null, new CopyableJLabel(message), "Desea descargar la actualización de Firmador disponible", JOptionPane.YES_NO_OPTION);
					if(answer== JOptionPane.YES_OPTION) {
						try {
							update_jar();
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}else {
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
		    if(!version.contentEquals(responseversion)) {
		    	String message = "Hay una versión nueva disponible, por favor actualice con prontitud a la nueva versión desde: <br> "+settings.base_url;

				if(can_write_path()) {
					int answer = JOptionPane.showConfirmDialog(null, new CopyableJLabel(message), "Desea descargar la actualización de Firmador disponible", JOptionPane.YES_NO_OPTION);
					if(answer== JOptionPane.YES_OPTION) {
						try {
							update_jar();
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}else {
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
		
			settings = SettingsManager.getInstance().get_and_create_settings();
			String version=settings.getVersion();
			try {			
				if(version.contains("SNAPSHOT")) {
					LOG.info("Updating development version");
					updateDevelopment();
				}else {
					LOG.info("Updating release version");
					updateRelease();
				}
			
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (URISyntaxException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
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
			String hexsha = getFileDigest(tmpfile);
			LOG.info("Sha256 of Downloaded file "+hexsha);
			String remotecheck=get_remote_checksum();
			LOG.info("Sha256 of Remote: "+remotecheck);
			return remotecheck.contains(hexsha);
			//return true;
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
