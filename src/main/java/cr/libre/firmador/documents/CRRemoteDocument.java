package cr.libre.firmador.documents;

import java.util.List;

import eu.europa.esig.dss.ws.dto.RemoteDocument;
import eu.europa.esig.dss.ws.dto.SignatureValueDTO;
import eu.europa.esig.dss.ws.dto.ToBeSignedDTO;
import eu.europa.esig.dss.ws.signature.common.RemoteMultipleDocumentsSignatureService;
import eu.europa.esig.dss.ws.signature.dto.DataToSignMultipleDocumentsDTO;
import eu.europa.esig.dss.ws.signature.dto.ExtendDocumentDTO;
import eu.europa.esig.dss.ws.signature.dto.SignMultipleDocumentDTO;
import eu.europa.esig.dss.ws.signature.dto.TimestampMultipleDocumentDTO;
import eu.europa.esig.dss.ws.signature.rest.RestMultipleDocumentSignatureServiceImpl;
import eu.europa.esig.dss.ws.signature.rest.client.RestMultipleDocumentSignatureService;
import eu.europa.esig.dss.ws.validation.dto.DataToValidateDTO;
import eu.europa.esig.dss.ws.validation.dto.WSReportsDTO;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

public class CRRemoteDocument {
    private RestMultipleDocumentSignatureService service = null;
    private byte[] previewDocument = null;
    private List<byte[]> previewImages = null;
    private byte[] document = null;
    private String name;

    CRRemoteDocument() {
        // fixed creating rservice
        RemoteMultipleDocumentsSignatureService rservice = null;
        service = new RestMultipleDocumentSignatureServiceImpl();
        ((RestMultipleDocumentSignatureServiceImpl) service).setService(rservice);
    }

    CRRemoteDocument(byte[] doc, String name) {
        this.document = doc;
        this.name = name;
    }
    RemoteDocument getRemoteDocument() {
        return new RemoteDocument(this.document, this.name);

    }

    void loadDocument(byte[] doc, String name) {
        this.document = doc;
        this.name = name;
    }
    byte[] getPDFPreview() {
        return previewDocument;
    }

    List<byte[]> getPreviewImages() {
        return previewImages;
    }


    public ToBeSignedDTO getDataToSign(DataToSignMultipleDocumentsDTO dataToSignDto) {
        // return service.getDataToSign(dataToSignDto.getToSignDocuments(),
        // dataToSignDto.getParameters());
        return null;
    }


    public CRRemoteDocument signDocument(SignMultipleDocumentDTO signDocumentDto) {
        // return service.signDocument(signDocumentDto.getToSignDocuments(),
        // signDocumentDto.getParameters(),
        // signDocumentDto.getSignatureValue());
        return null;
    }

    public CRRemoteDocument extendDocument(ExtendDocumentDTO extendDocumentDto) {
        // return service.extendDocument(extendDocumentDto.getToExtendDocument(),
        // extendDocumentDto.getParameters());
        return null;
    }


    public CRRemoteDocument timestampDocuments(TimestampMultipleDocumentDTO timestampDocument) {
        // return service.timestamp(timestampDocument.getToTimestampDocuments(),
        // timestampDocument.getTimestampParameters());
        return null;
    }

    public WSReportsDTO validateSignature(DataToValidateDTO dataToValidate) {
        // return validationService.validateDocument(dataToValidate);
        return null;
    }


}
