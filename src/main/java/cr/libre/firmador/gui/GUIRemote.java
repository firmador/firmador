package cr.libre.firmador.gui;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.URISyntaxException;
import java.security.KeyStore.PasswordProtection;
import java.util.List;

import javax.swing.GroupLayout;
import javax.swing.JPanel;
import javax.swing.SwingWorker;

import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.HttpException;
import org.apache.hc.core5.http.HttpStatus;
import org.apache.hc.core5.http.impl.bootstrap.HttpServer;
import org.apache.hc.core5.http.impl.bootstrap.ServerBootstrap;
import org.apache.hc.core5.http.io.HttpRequestHandler;
import org.apache.hc.core5.http.io.entity.ByteArrayEntity;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.apache.hc.core5.util.TimeValue;

import com.google.common.base.Throwables;

import cr.libre.firmador.Settings;
import cr.libre.firmador.SettingsManager;
import cr.libre.firmador.plugins.PluginManager;
import eu.europa.esig.dss.model.InMemoryDocument;

public class GUIRemote extends BaseSwing  implements GUIInterface  {

    private Boolean alreadySignedDocument = false;
    private byte[] toSignByteArray;
    
 
    private void createInterface() {
    	JPanel signPanel = new JPanel();
        GroupLayout signLayout = new GroupLayout(mainFrame.getContentPane());;
    	
    }
    
    @SuppressWarnings("serial")
	public void loadGUI() {
    	super.loadGUI();
            SwingWorker<Void, byte[]> remote = new SwingWorker<Void, byte[]>() {
                private HttpServer server;
                private String requestFileName;
                protected Void doInBackground() throws IOException, InterruptedException {
                    class RequestHandler implements HttpRequestHandler {
                        public RequestHandler() {
                            super();
                        }
                        public void handle(final ClassicHttpRequest request, final ClassicHttpResponse response, final HttpContext context) throws HttpException, IOException {
                            response.setHeader("Access-Control-Allow-Origin", settings.getOrigin());
                            response.setHeader("Vary", "Origin");
                            try {
                                if (request.getUri().getPath().equals("/close")) System.exit(0);
                                requestFileName = request.getUri().getPath().substring(1);
                            } catch (URISyntaxException e) {
                                e.printStackTrace();
                                showError(Throwables.getRootCause(e));
                            }
                            response.setCode(HttpStatus.SC_ACCEPTED);
                            HttpEntity entity = request.getEntity();
                            if (entity != null) {
                                if (alreadySignedDocument) {
                                    response.setCode(HttpStatus.SC_OK);
                                    ByteArrayOutputStream os = new ByteArrayOutputStream();
                                    signedDocument.writeTo(os);
                                    response.setEntity(new ByteArrayEntity(os.toByteArray(), ContentType.DEFAULT_TEXT));
                                }
                                if (entity.getContentLength() > 0) {
                                    ByteArrayOutputStream os = new ByteArrayOutputStream();
                                    entity.writeTo(os);
                                    publish(os.toByteArray());
                                }
                            }
                        }
                    };
                    Settings settings = SettingsManager.getInstance().get_and_create_settings();
                    server = ServerBootstrap.bootstrap().setListenerPort(settings.portnumber).setLocalAddress(InetAddress.getLoopbackAddress()).register("*", new RequestHandler()).create();
                    server.start();
                    server.awaitTermination(TimeValue.MAX_VALUE);
                    return null;
                }
                protected void process(List<byte[]> chunks) {
                    toSignByteArray = chunks.get(chunks.size() - 1);
                    toSignDocument = new InMemoryDocument(toSignByteArray, requestFileName);
                    loadDocument(null);
                }
            };
            remote.execute();
        
		
	}

    public boolean signDocuments() {
    	PasswordProtection pin = getPin();
    	super.signDocument(pin, true);
    	alreadySignedDocument = true;
    	return signedDocument != null;
    }
	@Override
	public void setArgs(String[] args) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void showError(Throwable error) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void showMessage(String message) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public int getSlot() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String getPkcs12file() {
		// TODO Auto-generated method stub
		return null;
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
	public PasswordProtection getPin() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setPluginManager(PluginManager pluginManager) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void loadDocument(String fileName) {
		// TODO Auto-generated method stub
		
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

}
