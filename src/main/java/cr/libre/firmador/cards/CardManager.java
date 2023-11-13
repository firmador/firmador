package cr.libre.firmador.cards;


import cr.libre.firmador.Settings;

public class CardManager {

    public static CardManagerInterface getCartdManager(CardSignInfo card, Settings settings) {
        CardManagerInterface cardmanager = null;
        if (CardSignInfo.PKCS12TYPE==card.getCardType()) {
            cardmanager = new PKCS12Manager();
            cardmanager.setSettings(settings);
        } else if (CardSignInfo.PKCS11TYPE == card.getCardType()) {
            cardmanager = new PKCS11Manager();
            cardmanager.setSettings(settings);
        }
        return cardmanager;
    }
}
