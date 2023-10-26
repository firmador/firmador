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

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.invoke.MethodHandles;
import java.math.BigInteger;
import java.net.URL;
import java.security.Key;
import java.security.KeyStore;
import java.security.KeyStore.PasswordProtection;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.cert.X509CRL;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPrivateKey;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import cr.libre.firmador.gui.GUIInterface;
import cr.libre.firmador.oxml.DSSDocumentOXML;
import cr.libre.firmador.oxml.TimeStampServiceCR;
import eu.europa.esig.dss.alert.exception.AlertException;
import eu.europa.esig.dss.enumerations.DigestAlgorithm;
import eu.europa.esig.dss.enumerations.SignatureLevel;
import eu.europa.esig.dss.enumerations.SignaturePackaging;

import eu.europa.esig.dss.model.DSSDocument;
import eu.europa.esig.dss.model.DSSException;

import eu.europa.esig.dss.model.SignatureValue;
import eu.europa.esig.dss.model.ToBeSigned;
import eu.europa.esig.dss.model.x509.CertificateToken;
import java.security.PrivateKey;
import java.security.Provider;
import java.security.Security;
import java.security.Signature;

import eu.europa.esig.dss.cades.CAdESSignatureParameters;

import eu.europa.esig.dss.cades.signature.CAdESService;

import eu.europa.esig.dss.service.tsp.OnlineTSPSource;
import eu.europa.esig.dss.spi.x509.CertificateSource;
import eu.europa.esig.dss.token.DSSPrivateKeyEntry;
import eu.europa.esig.dss.token.SignatureTokenConnection;

import eu.europa.esig.dss.validation.CertificateVerifier;

import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.openxml4j.opc.PackageAccess;
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
import org.apache.poi.poifs.crypt.dsig.services.TSPTimeStampService;
import org.apache.poi.poifs.crypt.dsig.services.TimeStampServiceValidator;
import org.apache.poi.util.LocaleUtil;
import org.bouncycastle.asn1.ASN1InputStream;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.DERIA5String;
import org.bouncycastle.asn1.DEROctetString;
import org.bouncycastle.asn1.ocsp.OCSPObjectIdentifiers;
import org.bouncycastle.asn1.x509.CRLDistPoint;
import org.bouncycastle.asn1.x509.DistributionPoint;
import org.bouncycastle.asn1.x509.DistributionPointName;
import org.bouncycastle.asn1.x509.Extension;
import org.bouncycastle.asn1.x509.Extensions;
import org.bouncycastle.asn1.x509.GeneralName;
import org.bouncycastle.asn1.x509.GeneralNames;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.cert.X509CRLHolder;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaX509CRLConverter;
import org.bouncycastle.cert.jcajce.JcaX509ExtensionUtils;
import org.bouncycastle.cert.ocsp.BasicOCSPResp;
import org.bouncycastle.cert.ocsp.BasicOCSPRespBuilder;
import org.bouncycastle.cert.ocsp.CertificateID;
import org.bouncycastle.cert.ocsp.CertificateStatus;
import org.bouncycastle.cert.ocsp.OCSPReq;
import org.bouncycastle.cert.ocsp.OCSPReqBuilder;
import org.bouncycastle.cert.ocsp.OCSPResp;
import org.bouncycastle.cert.ocsp.OCSPRespBuilder;
import org.bouncycastle.cert.ocsp.Req;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.DigestCalculator;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.operator.jcajce.JcaDigestCalculatorProviderBuilder;
import org.bouncycastle.util.io.pem.PemObject;
import org.bouncycastle.util.io.pem.PemReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FirmadorOpenXmlFormat extends CRSigner {
    final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private static Calendar nowtime = LocaleUtil.getLocaleCalendar(TimeZone.getTimeZone("America/Costa_Rica"));
    CAdESSignatureParameters parameters;
    SignatureConfig signatureConfig;
    private Settings settings;

    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

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
        // System.setProperty("jsr105Provider",
        // "org.jcp.xml.dsig.internal.dom.XMLDSigRI");
        Security.removeProvider("SunRsaSign");
        DSSDocument signedDocument = null;
        SignatureTokenConnection token = null;
        PKCS11Manager pkcs11manager = new PKCS11Manager();
        String tokenstr = card.getTokenSerialNumber();
        // String tokenstr="920de173-e5ce-4039-2f3d3eaab373db2d";

        KeyStore keystore = pkcs11manager.getKeyStore(card.getSlotLongID(), card.getPin());
        Key key = keystore.getKey(tokenstr, card.getPin().getPassword());

        signatureConfig = new SignatureConfig();

        X509Certificate x509 = (X509Certificate) keystore.getCertificate(tokenstr);
        signatureConfig.setKey((PrivateKey) key);
        // signatureConfig.setSigningCertificateChain(Collections.singletonList(x509));
        // signatureConfig.addSignatureFacet(new EnvelopedSignatureFacet());
        // signatureConfig.addSignatureFacet(new KeyInfoSignatureFacet());


        signatureConfig.addSignatureFacet(new OOXMLSignatureFacet());
        signatureConfig.addSignatureFacet(new EnvelopedSignatureFacet());
        signatureConfig.addSignatureFacet(new KeyInfoSignatureFacet());
        signatureConfig.addSignatureFacet(new XAdESSignatureFacet());
        signatureConfig.addSignatureFacet(new Office2010SignatureFacet());
        signatureConfig.addSignatureFacet(new XAdESXLSignatureFacet());

        signatureConfig.setSignatureDescription("Esto es una firma con firmador libre");

        TimeStampServiceCR timestampService = new TimeStampServiceCR(pkcs11manager.getProvider(),
                pkcs11manager.getCertificateChainTSA(), pkcs11manager);
//
        signatureConfig.setTspService(timestampService);
        signatureConfig.setTspUrl(TSA_URL);
        signatureConfig.setTspOldProtocol(false);
        signatureConfig.setIncludeEntireCertificateChain(false);
        signatureConfig.setIncludeIssuerSerial(false);
        signatureConfig.setSecureValidation(true);
        signatureConfig.setAllowCRLDownload(true);
        signatureConfig.setExecutionTime(nowtime.getTime());

        // signatureConfig.setXadesDigestAlgo(HashAlgorithm.sha256);
        // signatureConfig.setXadesRole("Xades Reviewer");

        signatureConfig.setXadesDigestAlgo(HashAlgorithm.sha256);
        signatureConfig.setDigestAlgo(HashAlgorithm.sha256);

        // List<X509Certificate> certchain = Collections.singletonList(x509);
        // pkcs11manager.getCertificateChain(false);
        List<X509Certificate> certchain = pkcs11manager.getCertificateChain(x509);
        // List<X509Certificate> certchain = pkcs11manager.getSignCertificates();

        // List<X509Certificate> certchain = new ArrayList<X509Certificate>();

        // certchain.add(0, x509);
        signatureConfig.setSigningCertificateChain(certchain);

        // RevocationDataService revocationservice =
        // signatureConfig.getRevocationDataService();
        RevocationData revocationData = new RevocationData();
        // timestampService.AddCertificateRevocation(signatureConfig, revocationData);

        RevocationDataService revocationDataService = revocationChain -> revocationData;
        signatureConfig.setRevocationDataService(revocationDataService);
        OPCPackage pkg = OPCPackage.open(toSignDocument.openStream());

        // adding the signature document to the package
        SignatureInfo si = new SignatureInfo();

        si.setOpcPackage(pkg);
        si.setSignatureConfig(signatureConfig);
        si.confirmSignature();
        // optionally verify the generated signature
        for (SignaturePart sp : si.getSignatureParts()) {
            assert sp.validate();
        }
        assert si.verifySignature();
        // write the changes back to disc

        // pkg.save(null);
        // pkg.close();

        return new DSSDocumentOXML(pkg);
    }

    public DSSDocument extend(DSSDocument document) {
        DSSDocument extendedDocument = null;
        RevocationData revocationData = new RevocationData();
//        revocationData.addCRL(crl1);
//        RevocationDataService revocationDataService = revocationChain -> revocationData;
//        signatureConfig.setRevocationDataService(revocationDataService);

        return document;
    }

}
