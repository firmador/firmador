/* Firmador is a program to sign documents using AdES standards.

Copyright (C) 2018 Firmador authors.

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

package app.firmador;

import java.security.KeyStore.PasswordProtection;
import java.util.ArrayList;
import java.util.List;

import com.google.common.base.Throwables;

import app.firmador.gui.GUIInterface;
import app.firmador.tarjeta.TarjetaPkcs11;
import app.firmador.tarjeta.Utils;
import eu.europa.esig.dss.AbstractSignatureParameters;
import eu.europa.esig.dss.DSSUtils;
import eu.europa.esig.dss.client.crl.OnlineCRLSource;
import eu.europa.esig.dss.client.http.commons.CommonsDataLoader;
import eu.europa.esig.dss.client.ocsp.OnlineOCSPSource;
import eu.europa.esig.dss.token.DSSPrivateKeyEntry;
import eu.europa.esig.dss.token.Pkcs11SignatureToken;
import eu.europa.esig.dss.token.SignatureTokenConnection;
import eu.europa.esig.dss.tsl.KeyUsageBit;
import eu.europa.esig.dss.validation.CertificateVerifier;
import eu.europa.esig.dss.validation.CommonCertificateVerifier;
import eu.europa.esig.dss.x509.CertificateToken;


public class CRSigner {

    public static final String TSA_URL = "http://tsa.sinpe.fi.cr/tsaHttp/";
    protected GUIInterface gui;
    public int selectedSlot;

    public CRSigner(GUIInterface gui) {
        this.gui = gui;
        selectedSlot = -1;
    }

    protected DSSPrivateKeyEntry
        getPrivateKey(SignatureTokenConnection signingToken) {
        /*
         * Uses first non-repudiation key available assuming there are no more,
         * keys with the same purpose with the same token.
         * This should work fine with unmodified Firma Digital smart cards
         * but it would be convenient checking there are no corner cases and
         * verify there are no more keys available to allow selecting them.
         */
        DSSPrivateKeyEntry privateKey = null;
        try {
            for (DSSPrivateKeyEntry candidatePrivateKey
                : signingToken.getKeys()) {
                if (candidatePrivateKey.getCertificate().checkKeyUsage(
                    KeyUsageBit.nonRepudiation)) {
                        privateKey = candidatePrivateKey;
                        break;
                }
            }
        } catch (Exception|Error e) {
            gui.showError(Throwables.getRootCause(e));
        }
        return privateKey;
    }

    private String getPkcs11Lib() {
        return Utils.getPKCS11Lib();
    }

    public SignatureTokenConnection
        get_signatureConnection(PasswordProtection pin) {
        /*
         * There should be other ways to find alternative PKCS#11 module
         * configuration settings in the future, operating system specific,
         * to support other hardware vendors apart of Athena/NXP (there are
         * some homologated devices already for Sello Electrónico).
         */
        SignatureTokenConnection signingToken = null;
        if (this.selectedSlot != -1) {
            signingToken = new Pkcs11SignatureToken(
                getPkcs11Lib(), pin, (int) selectedSlot);
        } else {
            TarjetaPkcs11 tarjeta = new TarjetaPkcs11();
            long[] slots = null;

            try {
                slots = tarjeta.getSlots();
            } catch (Exception|Error e) {
                gui.showError(Throwables.getRootCause(e));
            }
            if (slots != null) {
                if (slots.length > 0) {
                    if (slots.length == 1) {
                        signingToken = new Pkcs11SignatureToken(
                            getPkcs11Lib(), pin);
                    } else {
                        selectedSlot = getSelectedSlot(tarjeta, slots);
                        signingToken = new Pkcs11SignatureToken(
                                getPkcs11Lib(), pin, selectedSlot);
                    }
                }
            }
        }

        return signingToken;
    }

    public void selectSlot() {
        TarjetaPkcs11 tarjeta = new TarjetaPkcs11();
        long[] slots = null;

        try {
            slots = tarjeta.getSlots();
        } catch (Exception|Error e) {
            gui.showError(Throwables.getRootCause(e));
        }
        if (slots.length == 1) {
            selectedSlot = 0;
        } else {
            selectedSlot = getSelectedSlot(tarjeta, slots);
        }
    }

    private int getSelectedSlot(TarjetaPkcs11 tarjeta, long[] slots) {
        String[] propietarios = new String[slots.length];
        for (int x = 0; x < slots.length; x++) {
            propietarios[x] = tarjeta.getPropietario(slots[x]);
        }

        return gui.getSelection(propietarios);
    }

    public CertificateVerifier getCertificateVerifier() {
        CommonCertificateVerifier commonCertificateVerifier =
                new CommonCertificateVerifier();

        // CRLs
        CommonsDataLoader commonsHttpDataLoader = new CommonsDataLoader();
        OnlineCRLSource onlineCRLSource = new OnlineCRLSource();
        onlineCRLSource.setDataLoader(commonsHttpDataLoader);

        // OSCP
        commonCertificateVerifier.setCrlSource(onlineCRLSource);
        OnlineOCSPSource onlineOCSPSource = new OnlineOCSPSource();
        onlineOCSPSource.setDataLoader(commonsHttpDataLoader);
        commonCertificateVerifier.setOcspSource(onlineOCSPSource);

        return commonCertificateVerifier;
    }

    public List<CertificateToken> getCertificateChain(
        CertificateVerifier commonCertificateVerifier,
        AbstractSignatureParameters parameters) {

        List<CertificateToken> certificateChain =
            new ArrayList<CertificateToken>();

        List<CertificateToken> cert = new ArrayList<CertificateToken>(
            DSSUtils.loadPotentialIssuerCertificates(
                parameters.getSigningCertificate(),
                commonCertificateVerifier.getDataLoader()));
        if (cert != null && !cert.isEmpty()) {
            // FIXME add certificates for B level in app resources when offline
            certificateChain.add(cert.get(0));

            do {
                cert = new ArrayList<CertificateToken>(
                    DSSUtils.loadPotentialIssuerCertificates(cert.get(0),
                        commonCertificateVerifier.getDataLoader()));
                if (!cert.isEmpty()) {
                    certificateChain.add(cert.get(0));
                }
            } while (!cert.isEmpty());
        }

        return certificateChain;
    }

    public void setSlot(int slot) {
        this.selectedSlot = slot;
    }

}
