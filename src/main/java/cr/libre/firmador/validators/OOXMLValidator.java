package cr.libre.firmador.validators;

import java.lang.invoke.MethodHandles;
import java.security.cert.X509Certificate;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Iterator;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import cr.libre.firmador.Settings;
import cr.libre.firmador.SettingsManager;
import cr.libre.firmador.ooxml.DSSDocumentOXML;

import javax.naming.ldap.LdapName;
import javax.naming.ldap.Rdn;
import javax.xml.xpath.XPath;

import org.apache.poi.ooxml.util.XPathHelper;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.openxml4j.opc.PackageAccess;
import org.apache.poi.poifs.crypt.HashAlgorithm;
import org.apache.poi.poifs.crypt.dsig.SignatureConfig;
import org.apache.poi.poifs.crypt.dsig.SignatureInfo;
import org.apache.poi.poifs.crypt.dsig.SignaturePart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3.x2000.x09.xmldsig.SignatureDocument;
import org.w3.x2000.x09.xmldsig.SignatureType;

import eu.europa.esig.dss.model.DSSDocument;
import eu.europa.esig.dss.model.FileDocument;
import eu.europa.esig.dss.validation.reports.Reports;

public class OOXMLValidator implements Validator {
    final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private OPCPackage documentooxml = null;
    private boolean isSignedDocument = false;
    private SignatureConfig signatureConfig = null;
    private HashMap<SignaturePart, String> signParts;
    private SignatureInfo signatureInfo = null;
    private boolean isValid;



    @Override
    public DSSDocument loadDocumentPath(String fileName) {
        try {
            documentooxml = OPCPackage.open(fileName, PackageAccess.READ);
            signatureConfig = new SignatureConfig();
            signatureConfig.setSecureValidation(false); // Set false because pptx has problems to validate
            signatureConfig.setAllowCRLDownload(true);
            signatureConfig.setTspUrl(cr.libre.firmador.signers.CRSigner.TSA_URL);
            // signatureConfig.setTspOldProtocol(false);
            signatureConfig.setXadesDigestAlgo(HashAlgorithm.sha256);
            signatureConfig.setDigestAlgo(HashAlgorithm.sha256);
            signatureConfig.setAllowMultipleSignatures(true);
            signatureInfo = new SignatureInfo();
            signatureInfo.setOpcPackage(documentooxml);
            signatureInfo.setSignatureConfig(signatureConfig);

            isValid = signatureInfo.verifySignature();
            signParts = toList(signatureInfo.getSignatureParts());
            if (!signParts.isEmpty()) {
                isSignedDocument = true;
            }
            return new DSSDocumentOXML(documentooxml, fileName);
        } catch (Throwable e) {
            LOG.error("Documento no pudo ser analizado para su firma", e);

        }

        return null;
    }

    private HashMap<SignaturePart, String> toList(Iterable<SignaturePart> signatureParts) {
        HashMap<SignaturePart, String> list = new HashMap<SignaturePart, String>();
        Iterator<SignaturePart> iter = signatureParts.iterator();
        SignaturePart part;
        while (iter.hasNext()) {
            part = iter.next();
         
            list.put(part, part.validate() ? "Válido": "Inválido");
        }
        return list;
    }

    @Override
    public Reports getReports() {
        return null;
    }

    @Override
    public boolean isSigned() {
        return isSignedDocument;
    }

    @Override
    public boolean hasStringReport() {
        return true;
    }

    private String validaRevocacion(X509Certificate signer, SignatureType signatureType) throws Throwable {
        boolean tspvalido = true;
        XPath xpath = XPathHelper.getFactory().newXPath();
        return tspvalido ? "Válido" : "Inválido";
    }


    @Override
    public String getStringReport() throws Throwable {
        Settings settings = SettingsManager.getInstance().getAndCreateSettings();
        String report="";
        int position = 0;
        for (SignaturePart part : signParts.keySet()) {
            String signok = signParts.get(part);
            position += 1;
            String signdate = "";
            X509Certificate signer = part.getSigner();
            try {
                SignatureDocument signatureDocument = part.getSignatureDocument();
                //Node node = signatureDocument.getDomNode();
                Node signaturenode = signatureDocument.getSignature().getDomNode();

                 Document document = signaturenode.getOwnerDocument();
                 Element signtime = document.getElementById("idSignatureTime");
                 NodeList timevalue = signtime.getElementsByTagName("mdssi:Value");

                 if (timevalue.getLength() > 0) {
                     signdate = timevalue.item(0).getFirstChild().getNodeValue();
                     Instant signatureinstance = Instant.parse(signdate);
                     LocalDateTime local = LocalDateTime.from(signatureinstance.atZone(ZoneId.systemDefault()));
                     DateTimeFormatter dtf = DateTimeFormatter.ofPattern(settings.dateFormat);
                     signdate = dtf.format(local);
                 }


                 String revocaentiempo = validaRevocacion(signer, signatureDocument.getSignature());

            boolean[] keyUsage = signer.getKeyUsage();
            if (signer.getBasicConstraints() == -1 && keyUsage[0] && keyUsage[1]) {
                LdapName ldapName = new LdapName(signer.getSubjectX500Principal().getName("RFC1779"));
                String firstName = "", lastName = "", identification = "", commonName = "", organization = "";
                for (Rdn rdn : ldapName.getRdns()) {
                    if (rdn.getType().equals("OID.2.5.4.5")) identification = rdn.getValue().toString();
                    if (rdn.getType().equals("OID.2.5.4.4")) lastName = rdn.getValue().toString();
                    if (rdn.getType().equals("OID.2.5.4.42")) firstName = rdn.getValue().toString();
                    if (rdn.getType().equals("CN")) commonName = rdn.getValue().toString();
                    if (rdn.getType().equals("O")) organization = rdn.getValue().toString();
                }
            
                String expires = new SimpleDateFormat("yyyy-MM-dd").format(signer.getNotAfter());
                String name = firstName + " " + lastName;
                if (firstName.isEmpty() && lastName.isEmpty())
                    name = commonName;
                report += position + ". Firmante: " + name + " (" + identification + "), "
                        + organization + "<br>\n&nbsp;&nbsp;&nbsp; Fecha declarada de la firma: " + signdate
                        + "<br>&nbsp;&nbsp;&nbsp; Garantía de integridad y autenticidad: " + signok
                        + " (Certificado expira: " + expires + ")"
                        + "<br>&nbsp;&nbsp;&nbsp; Garantía de validez en el tiempo: " + revocaentiempo;
                report += "<br>";
            }

            } catch (Throwable e) {
                LOG.warn("Firma no pudo ser interpretada para mostrarse", e);
            }



        }
        return report;
    }

    @Override
    public int amountOfSignatures() {
        return signParts.size();
    }

}
