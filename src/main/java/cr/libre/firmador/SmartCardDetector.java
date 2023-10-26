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
            if (e.getCause().toString().contains("need 'arm64e'")) throw e;
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
        // TODO: Replace with PKCS11 Manager
        PKCS11Manager manager= new PKCS11Manager();
        String expires;
        String serialnumber;
        for(X509Certificate certificate: manager.getCertificates()) {
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
                expires = new SimpleDateFormat("yyyy-MM-dd").format(certificate.getNotAfter());
                serialnumber=new String(manager.getTokenByCert(certificate));
                LOG.debug(firstName + " " + lastName + " (" + identification + "), " + organization + ", " + certificate.getSerialNumber().toString(16) + " [Token serial number: " + serialnumber + "] (Expires: " + expires+ ")");
                cardinfo.add(new CardSignInfo(CardSignInfo.PKCS11TYPE,
                    identification,
                    firstName,
                    lastName,
                    commonName,
                    organization,
                    expires,
                    certificate.getSerialNumber().toString(16),
                    serialnumber,
                    manager.getSlotByCert(certificate)
                ));
            }
        	
        }
        return cardinfo;
    }

    @Override
    public void updateConfig() {}

}
