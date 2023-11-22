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

package cr.libre.firmador.signers;

import java.lang.invoke.MethodHandles;
import java.util.List;

import eu.europa.esig.dss.enumerations.KeyUsageBit;
import eu.europa.esig.dss.model.x509.CertificateToken;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cr.libre.firmador.CertificateManager;
import cr.libre.firmador.Settings;
import cr.libre.firmador.SettingsManager;
import cr.libre.firmador.cards.CardSignInfo;
import cr.libre.firmador.gui.GUIInterface;

public class CRSigner {
    final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    public static final String TSA_URL = "http://tsa.sinpe.fi.cr/tsaHttp/";
    protected GUIInterface gui;
    protected Settings settings;

    CRSigner(GUIInterface gui) {
        this.gui = gui;
        settings = SettingsManager.getInstance().getAndCreateSettings();
    }

    public void setSettings(Settings settings) {
        this.settings = settings;

    }

    public void setGui(GUIInterface gui) {
        this.gui = gui;
    }

    protected DSSPrivateKeyEntry getPrivateKey(SignatureTokenConnection signingToken) {
        DSSPrivateKeyEntry privateKey = null;
        List<DSSPrivateKeyEntry> keys = null;
        try {
            keys = signingToken.getKeys();
        } catch (Throwable e) {
            Throwable te = FirmadorUtils.getRootCause(e);
            String msg = e.getCause().toString();
            LOG.error("Error " + te.getLocalizedMessage() + " obteniendo manejador de llaves privadas de la tarjeta", e);
            if (e.getCause().toString().contains("need 'arm64e'")) {
                gui.showMessage("El firmador ha detectado que estaría utilizando una versión de Java para ARM.\n" +
                    "Aunque su computadora disponga de procesador ARM, debe desinstalar la versión de Java para ARM e instalar Java para Intel.\n" +
                    "Esto es debido a que el fabricante de las tarjetas solo provee un controlador para Intel\n" +
                    "y la versión de Java instalada solo puede cargar un controlador de la misma arquitectura.\n\n" +
                    "Una vez haya desinstalado Java para ARM, instalado Java para Intel y reiniciado el firmador,\n" +
                    "el sistema operativo utilizará un emulador para Intel y el firmador y detectará la tarjeta.");
                return null;
            }
            if (te.getLocalizedMessage().equals("CKR_PIN_INCORRECT")) throw e;
            if (te.getLocalizedMessage().equals("CKR_GENERAL_ERROR") && e.getCause().toString().contains("Unable to instantiate PKCS11")) throw e;
            if (te.getLocalizedMessage().equals("CKR_TOKEN_NOT_RECOGNIZED")) {
                LOG.info(te.getLocalizedMessage() + " (dispositivo de firma no reconocido)", e);
                return null;
            } else {
                if (msg.contains("but token only has 0 slots")) throw e;
                gui.showError(FirmadorUtils.getRootCause(e));
            }
        }
        // FIXME: This uses first non-repudiation key available assuming there are no more, keys with the same purpose with the same token.
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
        SignatureTokenConnection signingToken = null;
        try {
            if (card.getCardType() == CardSignInfo.PKCS12TYPE) signingToken = new Pkcs12SignatureToken(card.getTokenSerialNumber(), card.getPin());
            else signingToken = new Pkcs11SignatureToken(getPkcs11Lib(), card.getPin() /* new PrefilledPasswordCallback(card.getPin()) */, card.getSlotID());
        } catch (Throwable e) {
            LOG.error("Error al obtener la conexión de firma", e);
            gui.showError(FirmadorUtils.getRootCause(e));
        }
        return signingToken;
    }

    public CertificateVerifier getCertificateVerifier() {
        CertificateSource trustedCertSource = new CommonTrustedCertificateSource();
        // Just for testing for now, it should be adviced this Root CA is not trusted and not a part of national official document format policy. It is just for tax office purposes
        //trustedCertSource.addCertificate(DSSUtils.loadCertificate(this.getClass().getClassLoader().getResourceAsStream("certs/CA RAIZ MINISTERIO DE HACIENDA.crt")));
        trustedCertSource.addCertificate(DSSUtils.loadCertificate(this.getClass().getClassLoader().getResourceAsStream("certs/CA RAIZ NACIONAL - COSTA RICA v2.crt")));
        trustedCertSource.addCertificate(DSSUtils.loadCertificate(this.getClass().getClassLoader().getResourceAsStream("certs/CA RAIZ NACIONAL COSTA RICA.cer")));
        // For AdES Baseline B signing without Internet connection for fetching intermediates from AIA.
        // Costa Rica smart card certificate store chip from SINPE don't include intermediate certificates. This has been reported. No feedback received so far.
        CertificateSource adjunctCertSource = new CommonCertificateSource();
        adjunctCertSource.addCertificate(DSSUtils.loadCertificate(this.getClass().getClassLoader().getResourceAsStream("certs/CA POLITICA PERSONA FISICA - COSTA RICA v2.crt")));
        adjunctCertSource.addCertificate(DSSUtils.loadCertificate(this.getClass().getClassLoader().getResourceAsStream("certs/CA POLITICA PERSONA JURIDICA - COSTA RICA v2.crt")));
        adjunctCertSource.addCertificate(DSSUtils.loadCertificate(this.getClass().getClassLoader().getResourceAsStream("certs/CA POLITICA SELLADO DE TIEMPO - COSTA RICA v2.crt")));
        adjunctCertSource.addCertificate(DSSUtils.loadCertificate(this.getClass().getClassLoader().getResourceAsStream("certs/CA SINPE - PERSONA FISICA v2.cer")));
        adjunctCertSource.addCertificate(DSSUtils.loadCertificate(this.getClass().getClassLoader().getResourceAsStream("certs/CA SINPE - PERSONA FISICA v2(1).crt")));
        adjunctCertSource.addCertificate(DSSUtils.loadCertificate(this.getClass().getClassLoader().getResourceAsStream("certs/CA SINPE - PERSONA FISICA v2(2).crt")));
        adjunctCertSource.addCertificate(DSSUtils.loadCertificate(this.getClass().getClassLoader().getResourceAsStream("certs/CA SINPE - PERSONA JURIDICA v2.cer")));
        adjunctCertSource.addCertificate(DSSUtils.loadCertificate(this.getClass().getClassLoader().getResourceAsStream("certs/CA SINPE - PERSONA JURIDICA v2(1).crt")));
        adjunctCertSource.addCertificate(DSSUtils.loadCertificate(this.getClass().getClassLoader().getResourceAsStream("certs/CA SINPE - PERSONA JURIDICA v2(2).crt")));
        adjunctCertSource.addCertificate(DSSUtils.loadCertificate(this.getClass().getClassLoader().getResourceAsStream("certs/TSA SINPE v2.cer")));
        CommonCertificateVerifier cv = new CommonCertificateVerifier();
        cv.setTrustedCertSources(trustedCertSource);
        cv.setAdjunctCertSources(adjunctCertSource);
        cv.setCrlSource(new OnlineCRLSource());
        cv.setOcspSource(new OnlineOCSPSource());
        cv.setAIASource(new DefaultAIASource());
        return cv;
    }

    public CertificateVerifier getCertificateVerifier(CertificateToken subjectCertificate) {
        CommonCertificateVerifier cv = new CommonCertificateVerifier();
        CertificateManager certmanager = new CertificateManager();
        CertificateSource trustedCertSource = certmanager.getTrustedCertificateSource(subjectCertificate);
        CertificateSource adjunctCertSource = certmanager.getAdjunctCertSource(subjectCertificate);
        cv.setTrustedCertSources(trustedCertSource);
        cv.setAdjunctCertSources(adjunctCertSource);
        cv.setCrlSource(new OnlineCRLSource());
        cv.setOcspSource(new OnlineOCSPSource());
        cv.setAIASource(new DefaultAIASource());
        return cv;
    }
    


}
