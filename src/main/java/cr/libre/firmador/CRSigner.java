/* Firmador is a program to sign documents using AdES standards.

Copyright (C) 2021 Firmador authors.

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

import java.security.KeyStore.PasswordProtection;

import cr.libre.firmador.gui.GUIInterface;
import cr.libre.firmador.token.Token;
import cr.libre.firmador.token.Utils;
import com.google.common.base.Throwables;
import eu.europa.esig.dss.enumerations.KeyUsageBit;
import eu.europa.esig.dss.service.crl.OnlineCRLSource;
import eu.europa.esig.dss.service.ocsp.OnlineOCSPSource;
import eu.europa.esig.dss.spi.DSSUtils;
import eu.europa.esig.dss.spi.x509.aia.DefaultAIASource;
import eu.europa.esig.dss.spi.x509.CertificateSource;
import eu.europa.esig.dss.spi.x509.CommonCertificateSource;
import eu.europa.esig.dss.spi.x509.CommonTrustedCertificateSource;
import eu.europa.esig.dss.token.DSSPrivateKeyEntry;
import eu.europa.esig.dss.token.Pkcs11SignatureToken;
import eu.europa.esig.dss.token.Pkcs12SignatureToken;
import eu.europa.esig.dss.token.SignatureTokenConnection;
import eu.europa.esig.dss.validation.CertificateVerifier;
import eu.europa.esig.dss.validation.CommonCertificateVerifier;

public class CRSigner {

    public static final String TSA_URL = "http://tsa.sinpe.fi.cr/tsaHttp/";
    protected GUIInterface gui;
    public int selectedSlot;
    public String selectedP12;

    public CRSigner(GUIInterface gui) {
        this.gui = gui;
        selectedSlot = -1;
        selectedP12 = "";
    }

    protected DSSPrivateKeyEntry getPrivateKey(SignatureTokenConnection signingToken) {
        /*
         * Uses first non-repudiation key available assuming there are no more,
         * keys with the same purpose with the same token.
         * This should work fine with unmodified Firma Digital smart cards
         * but it would be convenient checking there are no corner cases and
         * verify there are no more keys available to allow selecting them.
         */
        DSSPrivateKeyEntry privateKey = null;
        for (DSSPrivateKeyEntry candidatePrivateKey : signingToken.getKeys()) {
            if (candidatePrivateKey.getCertificate().checkKeyUsage(KeyUsageBit.NON_REPUDIATION)) {
                    privateKey = candidatePrivateKey;
                    break;
            }
        }
        return privateKey;
    }

    private String getPkcs11Lib() {
        return Utils.getPKCS11Lib();
    }

    public SignatureTokenConnection getSignatureConnection(PasswordProtection pin) {
        /*
         * There should be other ways to find alternative PKCS#11 module
         * configuration settings in the future, operating system specific,
         * to support other hardware vendors apart of Athena/NXP (there are
         * some homologated devices already for Sello Electrónico).
         */
        SignatureTokenConnection signingToken = null;
        try {
            if (!this.selectedP12.isEmpty()) signingToken = new Pkcs12SignatureToken(selectedP12, pin);
            else if (this.selectedSlot != -1) signingToken = new Pkcs11SignatureToken(getPkcs11Lib(), pin, (int)selectedSlot);
            else {
                Token token = new Token();
                long[] slots = null;
                slots = token.getSlots();
                if (slots != null) {
                    if (slots.length > 0) {
                        if (slots.length == 1) signingToken = new Pkcs11SignatureToken(getPkcs11Lib(), pin);
                        else {
                            selectedSlot = getSelectedSlot(token, slots);
                            signingToken = new Pkcs11SignatureToken(getPkcs11Lib(), pin, selectedSlot);
                        }
                    }
                }
            }
        } catch (Exception|Error e) {
            gui.showError(Throwables.getRootCause(e));
        }
        return signingToken;
    }

    public void selectP12() {
        selectedP12 = gui.getPkcs12file();
    }

    public void selectSlot() {
        Token token = new Token();
        long[] slots = null;
        try {
            slots = token.getSlots();
        } catch (Exception|Error e) {
            gui.showError(Throwables.getRootCause(e));
        }
        if (slots.length == 1) selectedSlot = 0;
        else selectedSlot = getSelectedSlot(token, slots);
    }

    private int getSelectedSlot(Token token, long[] slots) {
        String[] owners = new String[slots.length];
        for (int x = 0; x < slots.length; x++) owners[x] = token.getOwner(slots[x]);
        return gui.getSelection(owners);
    }

    public CertificateVerifier getCertificateVerifier() {
        CertificateSource trustedCertSource = new CommonTrustedCertificateSource();
        trustedCertSource.addCertificate(DSSUtils.loadCertificate(this.getClass().getClassLoader().getResourceAsStream("CA RAIZ NACIONAL - COSTA RICA v2.crt")));
        trustedCertSource.addCertificate(DSSUtils.loadCertificate(this.getClass().getClassLoader().getResourceAsStream("CA RAIZ NACIONAL COSTA RICA.cer")));
        // For AdES Baseline B signing without Internet connection for fetching intermediates from AIA.
        // Costa Rica smart card certificate store chip from SINPE don't include intermediate certificates. This has been reported. No feedback received so far.
        CertificateSource adjunctCertSource = new CommonCertificateSource();
        adjunctCertSource.addCertificate(DSSUtils.loadCertificate(this.getClass().getClassLoader().getResourceAsStream("CA SINPE - PERSONA FISICA v2.cer")));
        adjunctCertSource.addCertificate(DSSUtils.loadCertificate(this.getClass().getClassLoader().getResourceAsStream("CA POLITICA PERSONA FISICA - COSTA RICA v2.crt")));
        adjunctCertSource.addCertificate(DSSUtils.loadCertificate(this.getClass().getClassLoader().getResourceAsStream("CA RAIZ NACIONAL - COSTA RICA v2.crt")));
        CommonCertificateVerifier cv = new CommonCertificateVerifier();
        cv.setTrustedCertSources(trustedCertSource);
        cv.setAdjunctCertSources(adjunctCertSource);
        cv.setCrlSource(new OnlineCRLSource());
        cv.setOcspSource(new OnlineOCSPSource());
        cv.setAIASource(new DefaultAIASource());
        return cv;
    }

    public void setSlot(int slot) {
        this.selectedSlot = slot;
    }

}
