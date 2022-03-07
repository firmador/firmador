package cr.libre.firmador.gui;

import java.awt.event.WindowEvent;
import java.io.IOException;
import java.security.KeyStore.PasswordProtection;
import java.util.HashMap;

import javax.swing.GroupLayout;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import org.apache.hc.core5.http.HttpStatus;
import org.apache.pdfbox.pdmodel.PDDocument;

import cr.libre.firmador.ConfigListener;
import cr.libre.firmador.gui.swing.AboutLayout;
import cr.libre.firmador.gui.swing.ConfigPanel;
import cr.libre.firmador.gui.swing.RemoteDocInformation;
import cr.libre.firmador.gui.swing.RemoteHttpWorker;
import cr.libre.firmador.gui.swing.SignPanel;
import cr.libre.firmador.gui.swing.SwingMainWindowFrame;
import eu.europa.esig.dss.model.InMemoryDocument;
import eu.europa.esig.dss.model.MimeType;

public class GUIRemote extends BaseSwing implements GUIInterface, ConfigListener {
	public JTabbedPane frameTabbedPane;
	private byte[] toSignByteArray;
	private RemoteHttpWorker<Void, byte[]> remote;
	private RemoteDocInformation docinfo;

	@SuppressWarnings("serial")
	public void loadGUI() {
		super.loadGUI();
	
		gui = this;
		settings.addListener(this);
		mainFrame = new SwingMainWindowFrame("Firmador Remoto");
		mainFrame.setGUIInterface(this);
		mainFrame.loadGUI();

		remote = new RemoteHttpWorker<Void, byte[]>(gui);
		remote.execute();

		signPanel = new SignPanel();
		signPanel.setGUI(this);
		signPanel.initializeActions();
		signPanel.hideButtons();

		GroupLayout signLayout = new GroupLayout(signPanel);
		signPanel.signLayout(signLayout, signPanel);
		settings.addListener(signPanel);

		JPanel aboutPanel = new JPanel();
		GroupLayout aboutLayout = new AboutLayout(aboutPanel);
		((AboutLayout) aboutLayout).setInterface(this);

		aboutPanel.setLayout(aboutLayout);
		aboutPanel.setOpaque(false);

		JPanel configPanel = new ConfigPanel();
		configPanel.setOpaque(false);
		frameTabbedPane = new JTabbedPane();
		frameTabbedPane.addTab("Firmar", signPanel);
		frameTabbedPane.setToolTipTextAt(0,
				"<html>En esta pestaña se muestran las opciones<br>para firmar el documento seleccionado.</html>");
		frameTabbedPane.addTab("Configuración", configPanel);
		frameTabbedPane.setToolTipTextAt(1, "<html>En esta estaña se configura<br>aspectos de este programa.</html>");
		frameTabbedPane.addTab("Acerca de", aboutPanel);
		frameTabbedPane.setToolTipTextAt(2,
				"<html>En esta estaña se muestra información<br>acerca de este programa.</html>");

		if(settings.showlogs) {
			this.showLogs(frameTabbedPane);
		}
		mainFrame.add(frameTabbedPane);
		// mainFrame.getContentPane().getLayout().addComponent(frameTabbedPane);
		// mainFrame.getContentPane().setLayout(signLayout);
		mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		mainFrame.pack();
		mainFrame.setMinimumSize(mainFrame.getSize());
		mainFrame.setLocationByPlatform(true);
		mainFrame.setVisible(true);
	}

	
	GUIRemote(){
		super();
		setTabnumber(3);
	}
	
	public boolean signDocuments() {
		PasswordProtection pin = getPin();
		super.signDocument(pin, true);

		try {
			signedDocument.writeTo(docinfo.getData());
			docinfo.setStatus(HttpStatus.SC_SUCCESS);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return signedDocument != null;
	}

	@Override
	public void setArgs(String[] args) {
		// TODO Auto-generated method stub

	}

	@Override
	public int getSlot() {
		return 0;
	}

	@Override
	public String getPkcs12file() {
		if(this.settings.usepkcs12file) {
			return this.settings.pkcs12file;
		}
		return "";
	}

	@Override
	public String getDocumentToSign() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getPathToSave(String extension) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void loadDocument(String fileName) {
		HashMap<String, RemoteDocInformation> docmap = remote.getDocInformation();
		docinfo = docmap.get(fileName);
		PDDocument doc;
		try {
			byte[] data = docinfo.getInputdata().readAllBytes();
			toSignDocument = new InMemoryDocument(data, fileName);
			MimeType mimeType = toSignDocument.getMimeType();
			if(MimeType.PDF == mimeType) {
				doc = PDDocument.load(data);
				loadDocument(mimeType, doc);
			
			} else if (mimeType == MimeType.XML || mimeType == MimeType.ODG || mimeType == MimeType.ODP || mimeType == MimeType.ODS || mimeType == MimeType.ODT) {
				showMessage("Está intentando firmar un documento XML o un openDocument que no posee visualización");
				signPanel.getSignButton().setEnabled(true);
			} else {
	           signPanel.shownonPDFButtons();
	           
	        }

		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	@Override
	public void extendDocument() {
		// TODO Auto-generated method stub

	}

	@Override
	public String getPathToSaveExtended(String extension) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void displayFunctionality(String functionality) {
		// TODO Auto-generated method stub

	}

	public void close() {

		// if(!remote.isCancelled())remote.cancel(true);
		mainFrame.dispatchEvent(new WindowEvent(mainFrame, WindowEvent.WINDOW_CLOSING));
		// mainFrame.dispose();

	}

	@Override
	public void updateConfig() {
		if(this.settings.showlogs) {
			showLogs(this.frameTabbedPane);
		}else {
			hideLogs(this.frameTabbedPane);
		}
		
	}

}
