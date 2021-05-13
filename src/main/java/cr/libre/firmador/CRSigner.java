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
import java.util.ArrayList;
import java.util.List;

import cr.libre.firmador.gui.GUIInterface;
import cr.libre.firmador.token.Token;
import cr.libre.firmador.token.Utils;
import com.google.common.base.Throwables;
import eu.europa.esig.dss.enumerations.KeyUsageBit;
import eu.europa.esig.dss.model.SerializableSignatureParameters;
import eu.europa.esig.dss.model.x509.CertificateToken;
import eu.europa.esig.dss.service.crl.OnlineCRLSource;
import eu.europa.esig.dss.service.http.commons.CommonsDataLoader;
import eu.europa.esig.dss.service.ocsp.OnlineOCSPSource;
import eu.europa.esig.dss.spi.DSSUtils;
import eu.europa.esig.dss.token.DSSPrivateKeyEntry;
import eu.europa.esig.dss.token.Pkcs11SignatureToken;
import eu.europa.esig.dss.token.SignatureTokenConnection;
import eu.europa.esig.dss.validation.CertificateVerifier;
import eu.europa.esig.dss.validation.CommonCertificateVerifier;

public class CRSigner {

    public static final String TSA_URL = "http://tsa.sinpe.fi.cr/tsaHttp/";
    protected GUIInterface gui;
    public int selectedSlot;

    public CRSigner(GUIInterface gui) {
        this.gui = gui;
        selectedSlot = -1;
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
        if (this.selectedSlot != -1) signingToken = new Pkcs11SignatureToken(getPkcs11Lib(), pin, (int)selectedSlot);
        else {
            Token token = new Token();
            long[] slots = null;
            try {
                slots = token.getSlots();
            } catch (Exception|Error e) {
                gui.showError(Throwables.getRootCause(e));
            }
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
        return signingToken;
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
        CommonCertificateVerifier commonCertificateVerifier = new CommonCertificateVerifier();
        // AIA
        CommonsDataLoader commonsHttpDataLoader = new CommonsDataLoader();
        // CRL
        OnlineCRLSource onlineCRLSource = new OnlineCRLSource();
        onlineCRLSource.setDataLoader(commonsHttpDataLoader);
        commonCertificateVerifier.setCrlSource(onlineCRLSource);
        // OCSP
        OnlineOCSPSource onlineOCSPSource = new OnlineOCSPSource();
        onlineOCSPSource.setDataLoader(commonsHttpDataLoader);
        commonCertificateVerifier.setOcspSource(onlineOCSPSource);
        return commonCertificateVerifier;
    }

    public List<CertificateToken> getCertificateChain(CertificateVerifier commonCertificateVerifier, SerializableSignatureParameters parameters) {
        List<CertificateToken> certificateChain = new ArrayList<CertificateToken>();
        List<CertificateToken> cert = new ArrayList<CertificateToken>(DSSUtils.loadPotentialIssuerCertificates(parameters.getSigningCertificate(), commonCertificateVerifier.getDataLoader()));
        if (cert != null && !cert.isEmpty()) {
            certificateChain.add(cert.get(0));
            do {
                cert = new ArrayList<CertificateToken>(DSSUtils.loadPotentialIssuerCertificates(cert.get(0), commonCertificateVerifier.getDataLoader()));
                if (!cert.isEmpty()) certificateChain.add(cert.get(0));
            } while (!cert.isEmpty());
        } else {
            // Failed to fetch from AIA (e.g. offline), fallback to resources
            certificateChain.add(DSSUtils.loadCertificate(this.getClass().getClassLoader().getResourceAsStream("CA SINPE - PERSONA FISICA v2.cer")));
            certificateChain.add(DSSUtils.loadCertificate(this.getClass().getClassLoader().getResourceAsStream("CA POLITICA PERSONA FISICA - COSTA RICA v2.crt")));
            certificateChain.add(DSSUtils.loadCertificate(this.getClass().getClassLoader().getResourceAsStream("CA RAIZ NACIONAL - COSTA RICA v2.crt")));
        }
        return certificateChain;
    }

    public void setSlot(int slot) {
        this.selectedSlot = slot;
    }

}
