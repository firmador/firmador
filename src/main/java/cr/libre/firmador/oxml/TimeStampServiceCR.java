package cr.libre.firmador.oxml;

import java.io.InputStream;
import java.math.BigInteger;
import java.net.URL;
import java.security.PrivateKey;
import java.security.Provider;
import java.security.cert.CertificateFactory;
import java.security.cert.X509CRL;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import org.apache.poi.poifs.crypt.dsig.SignatureConfig;
import org.apache.poi.poifs.crypt.dsig.SignatureInfo;
import org.apache.poi.poifs.crypt.dsig.services.RevocationData;
import org.apache.poi.poifs.crypt.dsig.services.RevocationDataService;
import org.apache.poi.poifs.crypt.dsig.services.TSPTimeStampService;
import org.apache.poi.util.LocaleUtil;
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
import org.bouncycastle.cert.X509CertificateHolder;
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
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.DigestCalculator;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.operator.jcajce.JcaDigestCalculatorProviderBuilder;

import cr.libre.firmador.PKCS11Manager;
import eu.europa.esig.dss.crl.CRLValidity;
import eu.europa.esig.dss.model.x509.CertificateToken;
import eu.europa.esig.dss.model.x509.revocation.crl.CRL;
import eu.europa.esig.dss.model.x509.revocation.ocsp.OCSP;
import eu.europa.esig.dss.service.NonceSource;
import eu.europa.esig.dss.service.SecureRandomNonceSource;
import eu.europa.esig.dss.service.crl.OnlineCRLSource;
import eu.europa.esig.dss.service.http.commons.CommonsDataLoader;
import eu.europa.esig.dss.service.ocsp.OnlineOCSPSource;
import eu.europa.esig.dss.spi.client.http.Protocol;
import eu.europa.esig.dss.spi.x509.CertificateSource;
import eu.europa.esig.dss.spi.x509.revocation.RevocationToken;
import eu.europa.esig.dss.spi.x509.revocation.OnlineRevocationSource.RevocationTokenAndUrl;
import eu.europa.esig.dss.spi.x509.revocation.RevocationSource;
import eu.europa.esig.dss.spi.x509.revocation.crl.CRLToken;
import eu.europa.esig.dss.validation.CRLFirstRevocationDataLoadingStrategy;
import eu.europa.esig.dss.validation.CertificateVerifier;
import eu.europa.esig.dss.validation.RevocationDataLoadingStrategy;
import eu.europa.esig.dss.validation.RevocationDataLoadingStrategyFactory;

public class TimeStampServiceCR extends TSPTimeStampService {
	private Provider provider;
	private List<X509Certificate> certchain;
    PKCS11Manager pkcs11manager;
    private CertificateVerifier verifier;

    private static Calendar nowtime = LocaleUtil.getLocaleCalendar(TimeZone.getTimeZone("America/Costa_Rica"));

    public TimeStampServiceCR(Provider provider, List<X509Certificate> chain, PKCS11Manager pkcs11manager,
            CertificateVerifier verifier) {
		this.provider=provider;
		this.certchain=chain;
        this.pkcs11manager = pkcs11manager;
        this.verifier = verifier;
	}
	
    @Override
    @SuppressWarnings({"squid:S2647"})
    public byte[] timeStamp(SignatureInfo signatureInfo, byte[] data, RevocationData revocationData) throws Exception {
		byte[] datastamped = super.timeStamp(signatureInfo, data,  revocationData);

        SignatureConfig signatureConfig = signatureInfo.getSignatureConfig();
        AddTPSRevocation(signatureConfig, revocationData);

        List<X509Certificate> revchain;
        try {
            revchain = this.pkcs11manager.getCertificateChain(revocationData.getX509chain().get(0));
            for (X509Certificate c : revchain)
                revocationData.addCertificate(c);
        } catch (Throwable e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return datastamped;
    }

    public void AddTPSRevocation(SignatureConfig signatureConfig, RevocationData revocationData) {

        List<X509Certificate> chain = signatureConfig.getSigningCertificateChain();

        pkcs11manager.getCertByCN(null);
        RevocationSource<OCSP> oscpsource = verifier.getOcspSource();
        RevocationToken<OCSP> token = oscpsource.getRevocationToken(new CertificateToken(chain.get(0)),
                new CertificateToken(chain.get(1)));
        revocationData.addOCSP(token.getEncoded());
        //AddTPSRevocation(signatureConfig, revocationData);
        RevocationSource<CRL> crlsource = verifier.getCrlSource();

        CertificateToken certificate;
        CertificateToken issuerCertificate;
        RevocationToken<CRL> crltoken;

        for (int pos = certchain.size() - 1; pos > 0; pos--) {
            certificate = new CertificateToken(certchain.get(pos));
            issuerCertificate = new CertificateToken(certchain.get(pos - 1));
            try {
                crltoken = crlsource.getRevocationToken(certificate, issuerCertificate);
                revocationData.addCRL(crltoken.getEncoded());

            } catch (Throwable e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

//    public void AddTPSRevocation(SignatureConfig signatureConfig, RevocationData revocationData) {
//        List<X509Certificate> chain = signatureConfig.getSigningCertificateChain();
//        X509Certificate certificate;
//        X509Certificate issuerCertificate;
//        List<CertificateToken> chaindss = new ArrayList<>();
//        for (X509Certificate x : chain) {
//            chaindss.add(new CertificateToken(x));
//        }
//
//        for (int pos = certchain.size() - 1; pos > 0; pos--) {
//            certificate = certchain.get(pos);
//            issuerCertificate = certchain.get(pos - 1);
//            try {
//                getCRLList(certificate, issuerCertificate, revocationData);
//
//            } catch (Throwable e) {
//                // TODO Auto-generated catch block
//                e.printStackTrace();
//            }
//        }
//
//        // issuerCertificate = this.pkcs11manager.getCertByCN("CA SINPE - PERSONA FISICA
//        // v2");
//        try {
//
//            certificate = chain.get(0);
//            issuerCertificate = chain.get(1);
//            createOcspResp(certificate, issuerCertificate, revocationData);
//
//            List<X509Certificate> revchain = this.pkcs11manager.getCertificateChain(issuerCertificate);
//            for (X509Certificate c : revchain)
//                revocationData.addCertificate(c);
//        } catch (Throwable e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        }
//
//    }

    public void AddCertificateRevocation(SignatureConfig signatureConfig, RevocationData revocationData)
            throws Throwable {
        AddTPSRevocation(signatureConfig, revocationData);
        // for (X509Certificate c : certchain)
        // revocationData.addCertificate(c);

    }
//    private void getCRLList(X509Certificate certificate, X509Certificate issuerCertificate,
//            RevocationData revocationData) throws Throwable {
//        OnlineCRLSource onlineCRLSource = new OnlineCRLSource();
//
//        CertificateToken certificateToken = new CertificateToken(certificate);
//        CertificateToken issuerToken=new CertificateToken(issuerCertificate);
//
//
//        RevocationTokenAndUrl<CRL> source = onlineCRLSource.getRevocationTokenAndUrl(certificateToken, issuerToken);
//        if (source != null)
//            revocationData.addCRL(source.getRevocationToken().getEncoded());
//    }

    public void createOcspResp(X509Certificate certificate, X509Certificate issuerCertificate,
            RevocationData revocationData) throws Throwable {

        CertificateToken certToken = new CertificateToken(certificate);
        CertificateToken issuerToken = new CertificateToken(issuerCertificate);
        OnlineOCSPSource oos = new OnlineOCSPSource();
        // NonceSource nonceSource = new SecureRandomNonceSource();
        // oos.setNonceSource(nonceSource);

        final RevocationToken<?> onlineRevocationToken = oos.getRevocationToken(certToken, issuerToken);

        byte[] data = onlineRevocationToken.getEncoded();
        if (data != null)
            revocationData.addOCSP(data);

    }
}
