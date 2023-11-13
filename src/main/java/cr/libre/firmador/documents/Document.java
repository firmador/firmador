package cr.libre.firmador.documents;

import eu.europa.esig.dss.model.DSSDocument;

public class Document {
    private SupportedMimeTypeEnum mimeType;
    private String pathname;
    private DSSDocument document;

    public Document(String pathname) {
        this.pathname = pathname;
    }

    public Document(byte[] data, String name) {

    }

}
