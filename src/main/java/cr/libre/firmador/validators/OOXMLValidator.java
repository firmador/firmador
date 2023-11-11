package cr.libre.firmador.validators;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.exceptions.InvalidOperationException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.openxml4j.opc.PackageAccess;
import org.apache.poi.poifs.crypt.dsig.SignatureConfig;
import org.apache.poi.poifs.crypt.dsig.SignatureInfo;
import org.apache.poi.poifs.crypt.dsig.SignaturePart;

import eu.europa.esig.dss.validation.reports.Reports;

public class OOXMLValidator implements Validator {
    private OPCPackage documentooxml = null;
    private boolean isSignedDocument = false;
    private SignatureConfig signatureConfig = null;
    private List<SignaturePart> signParts;
    private SignatureInfo signatureInfo = null;



    @Override
    public void loadDocumentPath(String fileName) {
        try {
            documentooxml = OPCPackage.open(fileName, PackageAccess.READ);
            signatureConfig = new SignatureConfig();
            signatureInfo = new SignatureInfo();
            signatureInfo.setOpcPackage(documentooxml);
            signatureInfo.setSignatureConfig(signatureConfig);
            signParts = toList(signatureInfo.getSignatureParts());
            if (!signParts.isEmpty()) {
                isSignedDocument = true;
            }
        } catch (InvalidOperationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (InvalidFormatException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    private List<SignaturePart> toList(Iterable<SignaturePart> signatureParts) {
        List<SignaturePart> list = new ArrayList<SignaturePart>();
        Iterator<SignaturePart> iter = signatureParts.iterator();
        while (iter.hasNext()) {
            list.add(iter.next());
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

    @Override
    public String getStringReport() {
        // TODO Auto-generated method stub
        return "Salen reportes";
    }

}
