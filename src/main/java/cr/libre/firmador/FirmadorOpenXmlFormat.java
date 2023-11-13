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

import java.lang.invoke.MethodHandles;
import java.security.Key;
import java.security.KeyStore;
import java.security.cert.X509Certificate;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

import cr.libre.firmador.cards.CardManager;
import cr.libre.firmador.cards.CardManagerInterface;
import cr.libre.firmador.cards.CardSignInfo;
import cr.libre.firmador.gui.GUIInterface;
import cr.libre.firmador.ooxml.DSSDocumentOXML;
import cr.libre.firmador.ooxml.TimeStampServiceCR;
import eu.europa.esig.dss.model.DSSDocument;
import eu.europa.esig.dss.model.x509.CertificateToken;

import java.security.PrivateKey;
import java.security.Provider;
import java.security.Security;
import eu.europa.esig.dss.cades.CAdESSignatureParameters;

import eu.europa.esig.dss.validation.CertificateVerifier;

import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.poifs.crypt.HashAlgorithm;
import org.apache.poi.poifs.crypt.dsig.SignatureConfig;
import org.apache.poi.poifs.crypt.dsig.SignatureInfo;
import org.apache.poi.poifs.crypt.dsig.SignaturePart;
import org.apache.poi.poifs.crypt.dsig.facets.EnvelopedSignatureFacet;
import org.apache.poi.poifs.crypt.dsig.facets.KeyInfoSignatureFacet;
import org.apache.poi.poifs.crypt.dsig.facets.OOXMLSignatureFacet;
import org.apache.poi.poifs.crypt.dsig.facets.Office2010SignatureFacet;
import org.apache.poi.poifs.crypt.dsig.facets.XAdESSignatureFacet;
import org.apache.poi.poifs.crypt.dsig.facets.XAdESXLSignatureFacet;

import org.apache.poi.poifs.crypt.dsig.services.RevocationData;
import org.apache.poi.poifs.crypt.dsig.services.RevocationDataService;
import org.apache.poi.util.LocaleUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FirmadorOpenXmlFormat extends CRSigner {
    final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private static Calendar nowtime = LocaleUtil.getLocaleCalendar(TimeZone.getTimeZone("America/Costa_Rica"));
    CAdESSignatureParameters parameters;
    SignatureConfig signatureConfig;
    private Settings settings;

    public FirmadorOpenXmlFormat(GUIInterface gui) {
        super(gui);
        settings = SettingsManager.getInstance().getAndCreateSettings();
    }

    public DSSDocument sign(DSSDocument toSignDocument, CardSignInfo card) {
        DSSDocument signedDocument = null;
        try {
            signedDocument = this.sign_ooxlm(toSignDocument, card);
        } catch (Throwable e) {
            LOG.error("Error al intentar firmar documento OOXML ", e);
            gui.showError(FirmadorUtils.getRootCause(e));
        }
        return signedDocument;
    }

    private DSSDocument sign_ooxlm(DSSDocument toSignDocument, CardSignInfo card) throws Throwable {
        CertificateManager certManager = new CertificateManager();

        Provider provider = Security.getProvider("SunRsaSign");
        if (card.getCardType() == CardSignInfo.PKCS11TYPE && provider != null) {
            Security.removeProvider("SunRsaSign");
        }
        CardManagerInterface cardmanager = CardManager.getCartdManager(card, settings);
        cardmanager.setSerialNumber(card.getTokenSerialNumber());
        KeyStore keystore = cardmanager.getKeyStore(card.getSlotLongID(), card.getPin());
        card = cardmanager.loadTokens(card, keystore);
        String tokenstr = card.getTokenSerialNumber(); // second change get real aliases on pkcs1
        Key key = keystore.getKey(tokenstr, card.getPin().getPassword());

        signatureConfig = new SignatureConfig();

        X509Certificate x509 = (X509Certificate) keystore.getCertificate(tokenstr);
        CertificateToken certificate = new CertificateToken(x509); 
        
        signatureConfig.setKey((PrivateKey) key);
        signatureConfig.addSignatureFacet(new OOXMLSignatureFacet());
        signatureConfig.addSignatureFacet(new EnvelopedSignatureFacet());
        signatureConfig.addSignatureFacet(new KeyInfoSignatureFacet());
        signatureConfig.addSignatureFacet(new XAdESSignatureFacet());
        signatureConfig.addSignatureFacet(new Office2010SignatureFacet());
        signatureConfig.addSignatureFacet(new XAdESXLSignatureFacet());

        signatureConfig.setSignatureDescription("Esto es una firma con firmador libre https://firmador.libre.cr/");

        CertificateVerifier verifier = this.getCertificateVerifier(certificate);
        TimeStampServiceCR timestampService = new TimeStampServiceCR(cardmanager, verifier);

        signatureConfig.setTspService(timestampService);
        signatureConfig.setTspUrl(TSA_URL);
        signatureConfig.setTspOldProtocol(false);
        signatureConfig.setIncludeEntireCertificateChain(false);
        signatureConfig.setIncludeIssuerSerial(false);
        signatureConfig.setSecureValidation(true);
        signatureConfig.setAllowCRLDownload(true);
        signatureConfig.setExecutionTime(nowtime.getTime());
        signatureConfig.setXadesDigestAlgo(HashAlgorithm.sha256);
        signatureConfig.setDigestAlgo(HashAlgorithm.sha256);
        
        List<X509Certificate> certchain = certManager.getX509CertificateChain(certificate);
        certchain.add(0, x509);
        signatureConfig.setSigningCertificateChain(certchain);
        RevocationData revocationData = new RevocationData();
        timestampService.AddCertificateRevocation(signatureConfig, revocationData);

        RevocationDataService revocationDataService = revocationChain -> revocationData;
        signatureConfig.setRevocationDataService(revocationDataService);
        OPCPackage pkg = OPCPackage.open(toSignDocument.openStream());
        SignatureInfo si = new SignatureInfo();
        si.setOpcPackage(pkg);
        si.setSignatureConfig(signatureConfig);
        si.confirmSignature();
        // optionally verify the generated signature
        for (SignaturePart sp : si.getSignatureParts()) {
            assert sp.validate();
        }
        assert si.verifySignature();
        // pkg.save(null);
        // pkg.close();
        // Insert again "SunRsaSign" when pkcs11 card
        if (card.getCardType() == CardSignInfo.PKCS11TYPE && provider != null) {
            Security.addProvider(provider);
        }
        return new DSSDocumentOXML(pkg);
    }

    public DSSDocument extend(DSSDocument document) {
        return document;
    }

}
