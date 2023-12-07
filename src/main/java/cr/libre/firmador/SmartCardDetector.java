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

package cr.libre.firmador;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.lang.invoke.MethodHandles;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import javax.naming.ldap.LdapName;
import javax.naming.ldap.Rdn;
import sun.security.pkcs11.wrapper.CK_ATTRIBUTE;
import sun.security.pkcs11.wrapper.CK_C_INITIALIZE_ARGS;
import sun.security.pkcs11.wrapper.CK_INFO;
import sun.security.pkcs11.wrapper.CK_SLOT_INFO;
import sun.security.pkcs11.wrapper.CK_TOKEN_INFO;
import sun.security.pkcs11.wrapper.PKCS11;
import sun.security.pkcs11.wrapper.PKCS11Exception;
import static sun.security.pkcs11.wrapper.PKCS11Constants.CKA_CLASS;
import static sun.security.pkcs11.wrapper.PKCS11Constants.CKA_ID;
import static sun.security.pkcs11.wrapper.PKCS11Constants.CKA_VALUE;
import static sun.security.pkcs11.wrapper.PKCS11Constants.CKF_OS_LOCKING_OK;
import static sun.security.pkcs11.wrapper.PKCS11Constants.CKF_SERIAL_SESSION;
import static sun.security.pkcs11.wrapper.PKCS11Constants.CKF_TOKEN_PRESENT;
import static sun.security.pkcs11.wrapper.PKCS11Constants.CKO_CERTIFICATE;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("restriction")
public class SmartCardDetector implements  ConfigListener {
    final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    protected Settings settings;
    private String lib;

    public SmartCardDetector() {
        settings = SettingsManager.getInstance().getAndCreateSettings();
        //settings.addListener(this);
    }
    public void updateLib() {
        lib = CRSigner.getPkcs11Lib();
    }

    public List<CardSignInfo> readSaveListSmartCard() throws Throwable {
        List<CardSignInfo> cards;
        try {
            cards = readListSmartCard();
        } catch (Throwable e) {
            LOG.info("readListSmartCard thrown", e);
            if (e.getMessage().toString().contains("incompatible architecture")) {
                throw new UnsupportedArchitectureException("Java para ARM detectado. Debe instalar Java para Intel para usar tarjetas de Firma Digital.", e);
            }
            cards = new ArrayList<CardSignInfo>();
        }
        File f;
        for (String pkcs12 : settings.pKCS12File) {
            f = new File(pkcs12);
            if(f.exists()) cards.add(new CardSignInfo(CardSignInfo.PKCS12TYPE, pkcs12, f.getName()));
        }
        return cards;
    }

    public List<CardSignInfo> readListSmartCard() throws Throwable {
        List<CardSignInfo> cardinfo = new ArrayList<CardSignInfo>();
        this.updateLib();
        String functionList = "C_GetFunctionList";
        CK_C_INITIALIZE_ARGS pInitArgs = new CK_C_INITIALIZE_ARGS();
        PKCS11 pkcs11;
        try {
            pInitArgs.flags = CKF_OS_LOCKING_OK;
            pkcs11 = PKCS11.getInstance(lib, functionList, pInitArgs, false);
        } catch (PKCS11Exception e) {
            LOG.debug("C_GetFunctionList didn't like CKF_OS_LOCKING_OK on pInitArgs", e);
            pInitArgs.flags = 0;
            pkcs11 = PKCS11.getInstance(lib, functionList, pInitArgs, false);
        }
        CK_INFO info = pkcs11.C_GetInfo();
        LOG.info("Interface: " + new String(info.libraryDescription).trim());
        Boolean tokenPresent = true;
        for (long slotID : pkcs11.C_GetSlotList(tokenPresent)) {
            CK_SLOT_INFO slotInfo = pkcs11.C_GetSlotInfo(slotID);
            LOG.debug("Slot " + slotID + ": " + new String(slotInfo.slotDescription).trim());
            if ((slotInfo.flags & CKF_TOKEN_PRESENT) != 0) { // Not required if tokenPresent = true, condition could be removed if true, it's just for testing empty slot enumeration
                try { // FIXME: slotID may be reused after switching card! try CK_SESSION_INFO sessionInfo = pkcs11.C_GetSessionInfo(hSession); and catch PCKCS11Exception meaning invalid session instead!
                    CK_TOKEN_INFO tokenInfo = pkcs11.C_GetTokenInfo(slotID);
                    LOG.info("Token: " + new String(tokenInfo.label).trim() + " (" + new String(tokenInfo.serialNumber).trim() + ")");
                    CK_ATTRIBUTE[] pTemplate = { new CK_ATTRIBUTE(CKA_CLASS, CKO_CERTIFICATE) };
                    long ulMaxObjectCount = 32;
                    long hSession = pkcs11.C_OpenSession(slotID, CKF_SERIAL_SESSION, null, null); // FIXME: verify slot session just after getting PIN but just before login
                    pkcs11.C_FindObjectsInit(hSession, pTemplate);
                    long[] phObject = pkcs11.C_FindObjects(hSession, ulMaxObjectCount);
                    pkcs11.C_FindObjectsFinal(hSession);
                    for (long object : phObject) {
                        CK_ATTRIBUTE[] pTemplate2 = { new CK_ATTRIBUTE(CKA_VALUE), new CK_ATTRIBUTE(CKA_ID) }; // if you add more attributes, update the iterator jump
                        pkcs11.C_GetAttributeValue(hSession, object, pTemplate2);
                        for (int i = 0; i < pTemplate2.length; i = i + 2) { // iterator jump value to read just certificates at pTemplate[0], pTemplate[2]... FIXME: better use pValue filtering
                            X509Certificate certificate = (X509Certificate)CertificateFactory.getInstance("X.509").generateCertificate(new ByteArrayInputStream((byte[])pTemplate2[i].pValue));
                            boolean[] keyUsage = certificate.getKeyUsage();
                            if (certificate.getBasicConstraints() == -1 && keyUsage[0] && keyUsage[1]) {
                                LdapName ldapName = new LdapName(certificate.getSubjectX500Principal().getName("RFC1779"));
                                String firstName = "", lastName = "", identification = "", commonName = "", organization = "";
                                for (Rdn rdn : ldapName.getRdns()) {
                                    if (rdn.getType().equals("OID.2.5.4.5")) identification = rdn.getValue().toString();
                                    if (rdn.getType().equals("OID.2.5.4.4")) lastName = rdn.getValue().toString();
                                    if (rdn.getType().equals("OID.2.5.4.42")) firstName = rdn.getValue().toString();
                                    if (rdn.getType().equals("CN")) commonName = rdn.getValue().toString();
                                    if (rdn.getType().equals("O")) organization = rdn.getValue().toString();
                                }
                                String expires = new SimpleDateFormat("yyyy-MM-dd").format(certificate.getNotAfter());
                                LOG.debug(firstName + " " + lastName + " (" + identification + "), " + organization + ", " + certificate.getSerialNumber().toString(16) + " [Token serial number: " + new String(tokenInfo.serialNumber) + "] (Expires: " + expires+ ")");
                                Object keyIdentifier = pTemplate2[i + 1]/* .pValue */; // FIXME: use pValue to get the value for comparison when using it to match with private key!
                                LOG.debug("Public/Private key pair identifier: " + keyIdentifier); // After logging in with PIN, find the matching private key pValue. NOTE: Old certificates didn't use "LlaveDeFirma" id/label.
                                cardinfo.add(new CardSignInfo(CardSignInfo.PKCS11TYPE,
                                    identification,
                                    firstName,
                                    lastName,
                                    commonName,
                                    organization,
                                    expires,
                                    certificate.getSerialNumber().toString(16),
                                    new String(tokenInfo.serialNumber),
                                    slotID
                                ));
                            }
                            // FIXME: Don't assume there's a single valid certificate per token (Persona Jurídica keystores might contain more than 1 usable certificate per token as they are handmade)
                        }
                    }
                    pkcs11.C_CloseSession(hSession);
                } catch (PKCS11Exception e) {
                    if (e.getLocalizedMessage().equals("CKR_TOKEN_NOT_RECOGNIZED")) {
                        LOG.info("Slot reports token is present but not recognized by the cryptoki library", e);
                    } else throw e;
                }
            } else LOG.info("No token present in this slot"); // Condition could be removed
        }
        return cardinfo;
    }

    @Override
    public void updateConfig() {}

}
