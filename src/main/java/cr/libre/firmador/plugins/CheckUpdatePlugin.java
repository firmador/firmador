package cr.libre.firmador.plugins;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import javax.swing.JOptionPane;
import javax.swing.SwingWorker;

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
				URL url = new URL(settings.release_url_check);
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
					String message = "Hay una versión nueva disponible, por favor actualice con prontitud a la nueva versión desde: <br> "+settings.base_url;
					JOptionPane.showMessageDialog(null, new CopyableJLabel(message), "Actualización de Firmador disponible", JOptionPane.INFORMATION_MESSAGE);
				}
			} catch (MalformedURLException e) {
				LOG.error(e.toString());
			}catch (IOException e) {
				LOG.error(e.toString());
			}
			return null;
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
