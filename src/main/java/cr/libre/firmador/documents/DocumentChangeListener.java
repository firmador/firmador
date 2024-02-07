package cr.libre.firmador.documents;

public interface DocumentChangeListener {
    void previewDone(Document document);
    void previewAllDone();
    void validateDone(Document document);
    void validateAllDone();
    void signDone(Document document);
    void signAllDone();

    void extendsDone(Document document);

    void clearDone();
}
