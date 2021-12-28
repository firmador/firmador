package cr.libre.firmador.gui.swing;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.URISyntaxException;
import java.util.HashMap;

import javax.swing.SwingUtilities;
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
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.apache.hc.core5.io.CloseMode;
import org.apache.hc.core5.util.TimeValue;

import com.google.common.base.Throwables;

import cr.libre.firmador.Settings;
import cr.libre.firmador.SettingsManager;
import cr.libre.firmador.gui.GUIInterface;
import cr.libre.firmador.gui.GUIRemote;

public class RemoteHttpWorker<T, V> extends SwingWorker<T, V> {
		protected GUIInterface gui;
        private HttpServer server;
        private String requestFileName;
 
    	protected HashMap<String, RemoteDocInformation> docInformation = new HashMap<>();
    	
        public RemoteHttpWorker(GUIInterface gui) {
			super();
			this.gui=gui;
			
		}
        

		public HashMap<String, RemoteDocInformation> getDocInformation() {
			return docInformation;
		}


		public void setDocInformation(HashMap<String, RemoteDocInformation> docInformation) {
			this.docInformation = docInformation;
		}


		protected T doInBackground() throws IOException, InterruptedException {
            class RequestHandler implements HttpRequestHandler {
            	protected Settings settings;
            	protected GUIInterface gui;
            	
            	
                public RequestHandler(GUIInterface gui, Settings settings) {
                    super();
                    this.settings = settings;
                    this.gui=gui;
                    ((GUIRemote) gui).getMainFrame().addWindowListener(new WindowAdapter() {				
        				@Override
        				public void windowClosing(WindowEvent arg0) {
        					server.stop();
        				}			 
        	        });
                
                }
              
                
                public void processSign(String name, RemoteDocInformation data) {
                	gui.loadDocument(name);
                }
                
                public void handle(final ClassicHttpRequest request, final ClassicHttpResponse response, final HttpContext context) throws HttpException, IOException {
                    response.setHeader("Access-Control-Allow-Origin", settings.getOrigin());
                    response.setHeader("Vary", "Origin");
                    try {
                        if (request.getUri().getPath().equals("/close")) {
                        	response.setCode(HttpStatus.SC_OK);
                        	response.setEntity (new StringEntity ("Closing..."));
                        	response.close();
                        	 
                        	 SwingUtilities.invokeLater(new Runnable() {
                                 public void run() {
                                	 try {
										Thread.sleep(1000);
									} catch (InterruptedException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}
                                     ((GUIRemote) gui).close();
                                     
                                 }
                             });
                        	return;
                        }
                        
                        requestFileName = request.getUri().getPath().substring(1);
                        
                        if(request.getMethod().contains("DELETE") ) {
                        	if(docInformation.containsKey(requestFileName)) {
                        		docInformation.remove(requestFileName);
								response.setCode(HttpStatus.SC_SUCCESS);
                        		return;
                        	}
                        	response.setCode(HttpStatus.SC_NOT_FOUND);
                    		return;
                        }  
       
                    } catch (URISyntaxException e) {
                        e.printStackTrace();
                        gui.showError(Throwables.getRootCause(e));
                    } catch (Exception e) {
                    	 e.printStackTrace();
					}
                    HttpEntity entity = request.getEntity();
                    response.setCode(HttpStatus.SC_ACCEPTED);
                    RemoteDocInformation docinfo;
                    if(!docInformation.containsKey(requestFileName)) {
                    	docinfo = new RemoteDocInformation(requestFileName, new ByteArrayOutputStream(), HttpStatus.SC_ACCEPTED);
                    	if (entity.getContentLength() > 0) {
	                    	docinfo.setInputdata(entity.getContent());
	                        publish();
	                        docInformation.put(requestFileName, docinfo);
	                        processSign(requestFileName, docinfo);
                    	}else {
                    		docinfo.setStatus(HttpStatus.SC_NO_CONTENT);
                    	}
                    }else {
                    	docinfo = docInformation.get(requestFileName);
                    }
                    
					response.setEntity(new ByteArrayEntity(docinfo.getData().toByteArray(), ContentType.DEFAULT_TEXT));
                	response.setCode(docinfo.getStatus());
                	
                }
            };
            Settings settings = SettingsManager.getInstance().get_and_create_settings();
            server = ServerBootstrap.bootstrap().setListenerPort(settings.portnumber).setLocalAddress(InetAddress.getLoopbackAddress()).register("*",
            		new RequestHandler(gui, settings)).create();
            server.start();
            server.awaitTermination(TimeValue.MAX_VALUE);
            return null;
        }
}
