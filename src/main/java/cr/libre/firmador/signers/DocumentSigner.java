package cr.libre.firmador.signers;

import java.util.List;

import cr.libre.firmador.Settings;
import cr.libre.firmador.cards.CardSignInfo;
import cr.libre.firmador.documents.Document;
import cr.libre.firmador.gui.GUIInterface;
import eu.europa.esig.dss.model.DSSDocument;

public interface DocumentSigner {
    void setGui(GUIInterface gui);

    void setSettings(Settings settings);

    DSSDocument sign(Document toSignDocument, CardSignInfo card);

    DSSDocument extend(DSSDocument document);

    void setDetached(List<DSSDocument> detacheddocs);
}
