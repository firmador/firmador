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

package cr.libre.firmador.gui.swing;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
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
//import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.http.protocol.HttpContext;
//import org.apache.hc.core5.io.CloseMode;
import org.apache.hc.core5.util.TimeValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cr.libre.firmador.Settings;
import cr.libre.firmador.SettingsManager;
import cr.libre.firmador.gui.GUIInterface;
import cr.libre.firmador.gui.GUISwing;
import cr.libre.firmador.signers.FirmadorUtils;

public class RemoteHttpWorker<T, V> extends SwingWorker<T, V> {
    final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

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
                ((GUISwing) gui).getMainFrame().addWindowListener(new WindowAdapter() {
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
                        //response.setEntity(new StringEntity("Closing..."));
                        LOG.trace("Closing...");
                        response.close();

                         SwingUtilities.invokeLater(new Runnable() {
                             public void run() {
                                 try {
                                    Thread.sleep(1000);
                                } catch (InterruptedException e) {
                                    LOG.error("Interrupción al correr servidor", e);
                                    e.printStackTrace();
                                }
                                ((GUISwing) gui).close();

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
                    LOG.error("Error URISyntaxException", e);
                    gui.showError(FirmadorUtils.getRootCause(e));
                } catch (Exception e) {
                    LOG.error("Error procesando petición", e);
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

                if (docinfo.getStatus() != HttpStatus.SC_NO_CONTENT) response.setEntity(new ByteArrayEntity(docinfo.getData().toByteArray(), ContentType.DEFAULT_TEXT));
                response.setCode(docinfo.getStatus());

            }
        };
        Settings settings = SettingsManager.getInstance().getAndCreateSettings();
        server = ServerBootstrap.bootstrap().setListenerPort(settings.portNumber).setLocalAddress(InetAddress.getLoopbackAddress()).register("*",
                new RequestHandler(gui, settings)).create();
        server.start();
        server.awaitTermination(TimeValue.MAX_VALUE);
        return null;
    }
}
