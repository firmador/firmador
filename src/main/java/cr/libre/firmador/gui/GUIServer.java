package cr.libre.firmador.gui;


import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

import org.apache.hc.core5.http.impl.bootstrap.HttpServer;
import org.apache.hc.core5.http.impl.bootstrap.ServerBootstrap;
import org.apache.hc.core5.util.TimeValue;
import org.apache.pdfbox.pdmodel.PDDocument;

import cr.libre.firmador.Settings;
import cr.libre.firmador.SettingsManager;
import cr.libre.firmador.cards.CardSignInfo;
import cr.libre.firmador.documents.Document;
import cr.libre.firmador.documents.DocumentChangeListener;
import cr.libre.firmador.documents.SupportedMimeTypeEnum;
import cr.libre.firmador.plugins.PluginManager;
import cr.libre.firmador.remote.RequestHandler;
public class GUIServer implements GUIInterface, DocumentChangeListener {
    private Document document;
    private HttpServer server;
    @Override
    public void loadGUI() {

        Settings settings = SettingsManager.getInstance().getAndCreateSettings();
        try {
            server = ServerBootstrap.bootstrap().setListenerPort(settings.portNumber)
                    .setLocalAddress(InetAddress.getLocalHost()).register("*", new RequestHandler(this, settings))
                    .create();

            server.start();
            server.awaitTermination(TimeValue.MAX_VALUE);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();

        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Override
    public void setArgs(String[] args) {
        System.out.println("setArgs");

    }

    @Override
    public void showError(Throwable error) {
        System.out.println("showError");

    }

    @Override
    public void showMessage(String message) {
        System.out.println("ShowMessage: " + message);

    }

    @Override
    public String getDocumentToSign() {
        System.out.println("getDocumentToSign");
        return null;
    }

    @Override
    public String getPathToSave(String extension) {
        System.out.println("getPathToSave: " + extension);
        return null;
    }

    @Override
    public CardSignInfo getPin() {
        System.out.println("getPin");
        return null;
    }

    @Override
    public void setPluginManager(PluginManager pluginManager) {
        System.out.println("setPluginManager");
    }

    @Override
    public void configurePluginManager() {
        System.out.println("configurePluginManager");

    }

    @Override
    public Document loadDocument(String fileName) {
        System.out.println("loadDocument:" + fileName);
        return null;
    }

    @Override
    public void loadDocument(SupportedMimeTypeEnum mimeType, PDDocument doc) {
        System.out.println("loadDocument");

    }

    @Override
    public void extendDocument() {
        System.out.println("extendDocument");

    }

    @Override
    public String getPathToSaveExtended(String extension) {
        System.out.println("getPathToSaveExtended: " + extension);
        return null;
    }

    @Override
    public boolean signDocuments() {
        System.out.println("signDocuments");
        return false;
    }

    @Override
    public void displayFunctionality(String functionality) {
        System.out.println("displayFunctionality: " + functionality);

    }

    @Override
    public void nextStep(String msg) {
        System.out.println("nextStep: " + msg);

    }

    @Override
    public Document loadDocument(Document document, boolean preview) {
        System.out.println("loadDocument");
        return null;
    }

    @Override
    public void previewDone(Document document) {
        System.out.println("previewDone");
    }

    @Override
    public void validateDone(Document document) {
        System.out.println("validateDone");

    }

    @Override
    public void signDone(Document document) {
        System.out.println("signDone");

    }

    @Override
    public void extendsDone(Document document) {
        System.out.println("extendsDone");

    }

    @Override
    public void doPreview(Document document) {
        System.out.println("doPreview");

    }

    @Override
    public void validateAllDone() {
        System.out.println("validateAllDone");

    }

    @Override
    public void signAllDone() {
        System.out.println("signAllDone");

    }

    @Override
    public void previewAllDone() {
        System.out.println("previewAllDone");

    }

    @Override
    public Settings getCurrentSettings() {
        System.out.println("getCurrentSettings");
        return null;
    }

    @Override
    public void signDocument(Document document) {
        System.out.println("signDocument");

    }

    @Override
    public void clearDone() {
        System.out.println("clearDone");
    }

    @Override
    public void registerHttpServer(HttpServer server) {
        System.out.println("registerHttpServer");

    }

    @Override
    public void registerCloseEvent(HttpServer server) {
        System.out.println("registerCloseEvent");

    }

    @Override
    public void requestCloseEvent() {
        // TODO Auto-generated method stub

    }

    @Override
    public Document findDocument(String name) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean deleteDocument(String name) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void cleanDocuments() {
        // TODO Auto-generated method stub

    }

}
