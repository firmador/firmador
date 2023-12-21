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

package cr.libre.firmador.cards;

import java.lang.invoke.MethodHandles;
import java.security.KeyStore.PasswordProtection;

import cr.libre.firmador.MessageUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CardSignInfo {
    final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    public static int PKCS11TYPE=1;
    public static int PKCS12TYPE=2;
    public static int ONLYPIN=3;
    private String identification;
    private String firstName;
    private String lastName;
    private String commonName;
    private String organization;
    private String expires;
    //private String certSerialNumber;
    // On pkcs12 use tokenSerialNumber to store pkcs12 file path
    private String tokenSerialNumber;
    private long slotID = -1;
    private PasswordProtection pin;
    private int cardType;

    public CardSignInfo(int cardType, String identification, String firstName, String lastName, String commonName, String organization, String expires,
            String certSerialNumber, String tokenSerialNumber, long slotID) {
        super();
        this.cardType=cardType;
        this.identification = identification;
        this.firstName = firstName;
        this.lastName = lastName;
        this.commonName = commonName;
        this.organization = organization;
        this.expires = expires;
        //this.certSerialNumber = certSerialNumber;
        this.tokenSerialNumber = tokenSerialNumber;
        this.slotID=slotID;
    }

    public CardSignInfo(int cardType, String path, String identification) {
        this.cardType = cardType;
        this.tokenSerialNumber=path;
        this.identification = identification;
        firstName=MessageUtils.t("cardsigninfo_name");
        lastName=MessageUtils.t("cardsigninfo_of_the_person");
        commonName=MessageUtils.t("cardsigninfo_name_person");
        organization=MessageUtils.t("cardsigninfo_type_person");
        expires="";
    }

    public CardSignInfo(PasswordProtection password) {
        this.setPin(password);
        this.cardType=ONLYPIN;
    }

    public int getCardType() {
        return cardType;
    }

    public void setCardType(int cardType) {
        this.cardType = cardType;
    }

    public String getTokenSerialNumber() {
        return tokenSerialNumber;
    }

    public void setTokenSerialNumber(String tokenSerialNumber) {
        this.tokenSerialNumber = tokenSerialNumber;
    }

    public int getSlotID() {
        return (int) slotID;
    }

    public Long getSlotLongID() {
        return  slotID;
    }

    public void setSlotID(long slotID) {
        this.slotID = slotID;
    }

    public void setPin(PasswordProtection pin) {
        this.pin = pin;
    }

    public String getDisplayInfo() {

        if(this.cardType == PKCS11TYPE)
            return firstName + " " + lastName + " (" + identification + MessageUtils.t("cardsigninfo_expires") + expires+ ")";
    //+ this.certSerialNumber+ " [Token serial number: " + this.tokenSerialNumber + "] (Expires: " + expires+ ")";
        return this.identification;
    }

    public PasswordProtection getPin() {
        return this.pin;
    }

    public void destroyPin() {
        try {
            pin.destroy();
        } catch (Exception e) {
            LOG.error(MessageUtils.t("cardsigninfo_error_destroying_pin"), e);
            e.printStackTrace();
        }
    }

    public boolean isValid() {
        return pin.getPassword() != null && pin.getPassword().length != 0;
    }

    public String getIdentification() {
        return identification;
    }

    public void setIdentification(String identification) {
        this.identification = identification;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getCommonName() {
        return commonName;
    }

    public void setCommonName(String commonName) {
        this.commonName = commonName;
    }

    public String getOrganization() {
        return organization;
    }

    public void setOrganization(String organization) {
        this.organization = organization;
    }

}
