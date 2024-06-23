package cr.libre.firmador.remote;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.net.URISyntaxException;
import java.util.List;

import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.HttpException;
import org.apache.hc.core5.http.HttpStatus;
import org.apache.hc.core5.http.impl.bootstrap.HttpServer;
import org.apache.hc.core5.http.io.HttpRequestHandler;
import org.apache.hc.core5.http.io.entity.ByteArrayEntity;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cr.libre.firmador.Settings;
import cr.libre.firmador.documents.Document;
import cr.libre.firmador.gui.GUIInterface;
import cr.libre.firmador.signers.FirmadorUtils;
import eu.europa.esig.dss.model.DSSDocument;
import jakarta.mail.MessagingException;

public class RequestHandler implements HttpRequestHandler {
    final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    public final static int API_ACTION_CREATE = 1;
    public final static int API_ACTION_DETAIL = 2;
    public final static int API_ACTION_DELETE = 3;
    public final static int API_ACTION_CLOSE = 4;
    public final static int API_ACTION_CLEAN = 5;
    private String requestFileName;
    protected Settings settings;
    protected GUIInterface gui;
    private HttpServer server;

    public RequestHandler(GUIInterface gui, Settings settings) {
        super();
        this.gui = gui;
        this.settings = settings;
        gui.registerHttpServer(server);
        gui.registerCloseEvent(server);

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

    private boolean deleteDocument(String name) {
        return this.gui.deleteDocument(name);
    }

    private Document findDocument(String name) {
        return this.gui.findDocument(name);
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
        gui.requestCloseEvent();
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
        this.gui.cleanDocuments();
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
            requestFileName = getrequestFileName(request, API_ACTION_CREATE);
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

    private void publish() {
        // TODO Auto-generated method stub

    }

    public void handle(final ClassicHttpRequest request, final ClassicHttpResponse response, final HttpContext context)
            throws HttpException, IOException {
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