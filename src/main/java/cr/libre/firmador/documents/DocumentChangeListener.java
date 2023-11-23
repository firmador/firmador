package cr.libre.firmador.documents;

public interface DocumentChangeListener {
    void previewDone(Document document);

    void validateDone(Document document);

    void signDone(Document document);

    void extendsDone(Document document);
}
