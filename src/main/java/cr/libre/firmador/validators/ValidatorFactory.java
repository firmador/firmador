package cr.libre.firmador.validators;

import cr.libre.firmador.documents.MimeTypeDetector;
import cr.libre.firmador.documents.SupportedMimeTypeEnum;

public class ValidatorFactory {
    public static Validator getValidator(String fileName) {
        SupportedMimeTypeEnum mimetype = MimeTypeDetector.detect(fileName);
        Validator validator = new GeneralValidator();
        if (mimetype.isOpenxmlformats()) {
            validator = new OOXMLValidator();
        }
        validator.loadDocumentPath(fileName);
        return validator;
    }
}
