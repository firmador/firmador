/* Firmador is a program to sign documents using AdES standards.

Copyright (C) 2018, 2022 Firmador authors.

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

import java.util.List;

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
//import eu.europa.esig.dss.token.PrefilledPasswordCallback;
import eu.europa.esig.dss.token.Pkcs11SignatureToken;
import eu.europa.esig.dss.token.Pkcs12SignatureToken;
import eu.europa.esig.dss.token.SignatureTokenConnection;
import eu.europa.esig.dss.validation.CertificateVerifier;
import eu.europa.esig.dss.validation.CommonCertificateVerifier;
import org.slf4j.LoggerFactory;

import cr.libre.firmador.gui.GUIInterface;

public class CRSigner {
    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(CRSigner.class);
    public static final String TSA_URL = "http://tsa.sinpe.fi.cr/tsaHttp/";
    protected GUIInterface gui;

    public CRSigner(GUIInterface gui) {
        this.gui = gui;
    }

    protected DSSPrivateKeyEntry getPrivateKey(SignatureTokenConnection signingToken) {
        DSSPrivateKeyEntry privateKey = null;
        List<DSSPrivateKeyEntry> keys = null;
        try {
            keys = signingToken.getKeys();
        } catch (Throwable e) {
            Throwable te = Throwables.getRootCause(e);
            String msg = e.getCause().toString();
            LOG.error("Error " + te.getLocalizedMessage() + " obteniendo manejador de llaves privadas de la tarjeta", e);
            if (te.getLocalizedMessage().equals("CKR_PIN_INCORRECT")) throw e;
            if (te.getLocalizedMessage().equals("CKR_GENERAL_ERROR") && e.getCause().toString().contains("Unable to instantiate PKCS11")) throw e;
            if (te.getLocalizedMessage().equals("CKR_TOKEN_NOT_RECOGNIZED")) return null;
            else {
                if (msg.contains("but token only has 0 slots")) throw e;
                gui.showError(Throwables.getRootCause(e));
            }
        }
        // Uses first non-repudiation key available assuming there are no more, keys with the same purpose with the same token.
        // This should work fine with unmodified Firma Digital smart cards but it would be convenient checking there are no corner cases
        // and verify there are no more keys available to allow selecting them.
        if (keys != null) {
            for (DSSPrivateKeyEntry candidatePrivateKey : keys) {
                if (candidatePrivateKey.getCertificate().checkKeyUsage(KeyUsageBit.NON_REPUDIATION)) {
                    privateKey = candidatePrivateKey;
                    break;
                }
            }
        }
        return privateKey;
    }

    public static String getPkcs11Lib() {
        String osName = System.getProperty("os.name").toLowerCase();
        Settings settings = SettingsManager.getInstance().getAndCreateSettings();
        if (settings.extraPKCS11Lib != null && !settings.extraPKCS11Lib.isEmpty()) return settings.extraPKCS11Lib;
        if (osName.contains("mac")) return "/Library/Application Support/Athena/libASEP11.dylib";
        else if (osName.contains("linux")) return "/usr/lib/x64-athena/libASEP11.so";
        else if (osName.contains("windows")) return System.getenv("SystemRoot") + "\\System32\\asepkcs.dll";
        return "";
    }

    public SignatureTokenConnection getSignatureConnection(CardSignInfo card) {
        // There should be other ways to find alternative PKCS#11 module configuration settings in the future, operating system specific,
        // to support other hardware vendors apart of Athena/NXP (mainly  hardware devices for Sello Electrónico).
        SignatureTokenConnection signingToken = null;
        try {
            if (card.getCardType() == CardSignInfo.PKCS12TYPE) signingToken = new Pkcs12SignatureToken(card.getTokenSerialNumber(), card.getPin());
            else signingToken = new Pkcs11SignatureToken(getPkcs11Lib(), card.getPin() /* new PrefilledPasswordCallback(card.getPin()) */, card.getSlotID());
        } catch (Throwable e) {
            LOG.error("Error al obtener la conexión de firma", e);
            gui.showError(Throwables.getRootCause(e));
        }
        return signingToken;
    }

    public CertificateVerifier getCertificateVerifier() {
        CertificateSource trustedCertSource = new CommonTrustedCertificateSource();
        trustedCertSource.addCertificate(DSSUtils.loadCertificate(this.getClass().getClassLoader().getResourceAsStream("certs/CA RAIZ NACIONAL - COSTA RICA v2.crt")));
        trustedCertSource.addCertificate(DSSUtils.loadCertificate(this.getClass().getClassLoader().getResourceAsStream("certs/CA RAIZ NACIONAL COSTA RICA.cer")));
        // Just for testing for now, it should be adviced this Root CA is not trusted and not a part of national official document format policy. It is just for tax office purposes
        //trustedCertSource.addCertificate(DSSUtils.loadCertificate(this.getClass().getClassLoader().getResourceAsStream("certs/CA RAIZ MINISTERIO DE HACIENDA.crt")));
        // For AdES Baseline B signing without Internet connection for fetching intermediates from AIA.
        // Costa Rica smart card certificate store chip from SINPE don't include intermediate certificates. This has been reported. No feedback received so far.
        CertificateSource adjunctCertSource = new CommonCertificateSource();
        adjunctCertSource.addCertificate(DSSUtils.loadCertificate(this.getClass().getClassLoader().getResourceAsStream("certs/CA SINPE - PERSONA FISICA v2.cer")));
        adjunctCertSource.addCertificate(DSSUtils.loadCertificate(this.getClass().getClassLoader().getResourceAsStream("certs/CA SINPE - PERSONA FISICA v2(1).crt")));
        adjunctCertSource.addCertificate(DSSUtils.loadCertificate(this.getClass().getClassLoader().getResourceAsStream("certs/CA SINPE - PERSONA JURIDICA v2.cer")));
        adjunctCertSource.addCertificate(DSSUtils.loadCertificate(this.getClass().getClassLoader().getResourceAsStream("certs/CA SINPE - PERSONA JURIDICA v2(1).crt")));
        adjunctCertSource.addCertificate(DSSUtils.loadCertificate(this.getClass().getClassLoader().getResourceAsStream("certs/TSA SINPE v2.cer")));
        adjunctCertSource.addCertificate(DSSUtils.loadCertificate(this.getClass().getClassLoader().getResourceAsStream("certs/CA POLITICA PERSONA FISICA - COSTA RICA v2.crt")));
        adjunctCertSource.addCertificate(DSSUtils.loadCertificate(this.getClass().getClassLoader().getResourceAsStream("certs/CA POLITICA PERSONA JURIDICA - COSTA RICA v2.crt")));
        adjunctCertSource.addCertificate(DSSUtils.loadCertificate(this.getClass().getClassLoader().getResourceAsStream("certs/CA POLITICA SELLADO DE TIEMPO - COSTA RICA v2.crt")));
        CommonCertificateVerifier cv = new CommonCertificateVerifier();
        cv.setTrustedCertSources(trustedCertSource);
        cv.setAdjunctCertSources(adjunctCertSource);
        cv.setCrlSource(new OnlineCRLSource());
        cv.setOcspSource(new OnlineOCSPSource());
        cv.setAIASource(new DefaultAIASource());
        return cv;
    }

}
