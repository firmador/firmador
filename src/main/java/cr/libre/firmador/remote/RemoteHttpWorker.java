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

package cr.libre.firmador.remote;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.lang.invoke.MethodHandles;
import java.net.InetAddress;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;

import jakarta.mail.MessagingException;
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
import cr.libre.firmador.documents.Document;
import cr.libre.firmador.gui.GUIInterface;
import cr.libre.firmador.gui.GUISwing;
import cr.libre.firmador.signers.FirmadorUtils;
import eu.europa.esig.dss.model.DSSDocument;

public class RemoteHttpWorker<T, V> extends SwingWorker<T, V> {
    final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    public final static int API_ACTION_CREATE = 1;
    public final static int API_ACTION_DETAIL = 2;
    public final static int API_ACTION_DELETE = 3;
    public final static int API_ACTION_CLOSE = 4;
    public final static int API_ACTION_CLEAN = 5;

    protected GUIInterface gui;
    private HttpServer server;
    private String requestFileName;

    public RemoteHttpWorker(GUIInterface gui) {
        super();
        this.gui=gui;

    }

    protected T doInBackground() throws IOException, InterruptedException {
        class RequestHandler implements HttpRequestHandler {
            protected Settings settings;
            protected GUIInterface gui;

            public RequestHandler(GUIInterface gui, Settings settings) {
                super();
                this.gui = gui;
                this.settings = settings;
                ((GUISwing) gui).getMainFrame().addWindowListener(new WindowAdapter() {
                    @Override
                    public void windowClosing(WindowEvent arg0) {
                        server.stop();
                    }
                });

            }

            private int determineAPIAction(final ClassicHttpRequest request) throws Throwable {
                int result = 0;
                if (request.getUri().getPath().equals("/close")) {
                    result = API_ACTION_CLOSE;
                } else if (request.getMethod().contains("DELETE")) {
                    result = API_ACTION_DELETE;
                } else if (request.getUri().getPath().startsWith("/create")) {
                    result = API_ACTION_CREATE;
                } else if (request.getUri().getPath().startsWith("/clean")) {
                    result = API_ACTION_CLEAN;
                } else if (request.getUri().getPath().startsWith("/detail")) {
                    result = API_ACTION_DETAIL;
                }
                return result;

            }

            public void processSign(Document doc, boolean preview) {
                gui.loadDocument(doc, preview);

            }

            private void retrieveDocument(String name, final ClassicHttpResponse response) throws Throwable {
                Document docrequested = findDocument(requestFileName);
                if (docrequested != null) {
                    DSSDocument signeddoc = docrequested.getSignedDocument();
                    if (signeddoc != null) {
                        ByteArrayOutputStream out = new ByteArrayOutputStream();
                        signeddoc.writeTo(out);
                        response.setEntity(new ByteArrayEntity(out.toByteArray(), ContentType.DEFAULT_TEXT));
                        response.setCode(HttpStatus.SC_SUCCESS);
                        return;
                    }
                }
                response.setCode(HttpStatus.SC_NO_CONTENT);
            }

            private boolean deleteDocument(String name){
                GUISwing gui = (GUISwing) this.gui;
                boolean dev = false;
                HashMap<String, Document> result = gui.getListDocumentTablePanel().findDocument(name, true);
                if (!result.isEmpty()) {
                    Document doc = result.get(name);
                    gui.getListDocumentTablePanel().removeDocument(doc);
                    dev = true;
                }
                return dev;
            }

            private Document findDocument(String name) {
                GUISwing gui = (GUISwing) this.gui;
                HashMap<String, Document> result = gui.getListDocumentTablePanel().findDocument(name, true);
                if (!result.isEmpty()) {
                    return result.get(name);
                }
                return null;

            }

            private boolean checkMultiPart(final ClassicHttpRequest request, HttpEntity entity) {
                ContentType contentType = ContentType.parse(entity.getContentType());
                return contentType.getMimeType().equalsIgnoreCase(ContentType.MULTIPART_FORM_DATA.getMimeType());
            }

            private List<Document> extractMultiPart(final ClassicHttpRequest request, HttpEntity entity) {
                List<Document> listdoc = null;
                try {
                    listdoc = MultipartParser.parseMultipart(entity, gui);
                    if (listdoc.isEmpty())
                        listdoc = null;
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (MessagingException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                return listdoc;
            }

            private boolean closeWidget() {
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
                return true;
            }

            private String getrequestFileName(final ClassicHttpRequest request, int action) throws URISyntaxException {
                String filename = null;
                switch (action) {
                case API_ACTION_CLOSE:
                case API_ACTION_CLEAN:
                    break;
                case API_ACTION_DELETE:
                    filename = request.getUri().getPath().substring(1);
                    break;
                case API_ACTION_CREATE:
                    filename = request.getUri().getPath().substring(1).replace("create/", "");
                    break;
                case API_ACTION_DETAIL:
                    filename = request.getUri().getPath().substring(1).replace("detail/", "");
                    break;
                }
                return filename;
            }

            public void cleanDocuments() {
                GUISwing gui = (GUISwing) this.gui;
                gui.getListDocumentTablePanel().cleanDocuments();
            }
            private void createAction(final ClassicHttpRequest request, final ClassicHttpResponse response)
                    throws Throwable, IOException {
                HttpEntity entity = request.getEntity();
                if (checkMultiPart(request, entity)) {
                    // MULTI DOCUMENT
                    int counter = 0;
                    for (Document d : extractMultiPart(request, entity)) {
                        if (counter == 0) {
                            processSign(d, true);
                        } else {
                            processSign(d, false);
                        }
                        counter++;
                    }
                    response.setCode(HttpStatus.SC_SUCCESS);
                } else { // ONE DOCUMENT
                    requestFileName=getrequestFileName(request, API_ACTION_CREATE);
                    response.setCode(HttpStatus.SC_ACCEPTED);
                    if (entity.getContentLength() > 0) {
                        Document doc = new Document(gui, entity.getContent().readAllBytes(), requestFileName,
                                Document.STATUS_TOSIGN);
                        publish();
                        processSign(doc, true);
                        response.setCode(HttpStatus.SC_SUCCESS);
                    }
                }
            }

            public void handle(final ClassicHttpRequest request, final ClassicHttpResponse response, final HttpContext context) throws HttpException, IOException {
                response.setHeader("Access-Control-Allow-Origin", settings.getOrigin());
                response.setHeader("Vary", "Origin");
                try {
                    switch (determineAPIAction(request)) {
                    case API_ACTION_CLOSE:
                            response.setCode(HttpStatus.SC_OK);
                            closeWidget();
                            LOG.trace("Closing...");
                            response.close();
                            break;
                    case API_ACTION_DELETE:
                        requestFileName = getrequestFileName(request, API_ACTION_DELETE);
                        boolean result = deleteDocument(requestFileName);
                        if (result) {
                            response.setCode(HttpStatus.SC_SUCCESS);
                        } else {
                            response.setCode(HttpStatus.SC_NOT_FOUND);
                        }
                        break;
                    case API_ACTION_CREATE:
                        createAction(request, response);
                        break;
                    case API_ACTION_DETAIL:
                        requestFileName = getrequestFileName(request, API_ACTION_DETAIL);
                        retrieveDocument(requestFileName, response);
                        break;
                    case API_ACTION_CLEAN:
                        cleanDocuments();
                        response.setCode(HttpStatus.SC_SUCCESS);
                        break;
                    default:
                        response.setCode(HttpStatus.SC_NO_CONTENT);
                    }


                } catch (URISyntaxException e) {
                    LOG.error("Error URISyntaxException", e);
                    gui.showError(FirmadorUtils.getRootCause(e));
                    response.setCode(HttpStatus.SC_NO_CONTENT);
                } catch (Exception e) {
                    LOG.error("Error procesando petición", e);
                    e.printStackTrace();
                    response.setCode(HttpStatus.SC_NO_CONTENT);

                } catch (Throwable e) {
                    LOG.error("Error", e);
                    gui.showError(FirmadorUtils.getRootCause(e));
                    response.setCode(HttpStatus.SC_NO_CONTENT);
                }

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
