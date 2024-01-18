package cr.libre.firmador.plugins;

import cr.libre.firmador.documents.Document;

public interface DocumentInteractInterface {
    public void registerDocument(Document document);

    public void unregisterDocument(Document document);
}
