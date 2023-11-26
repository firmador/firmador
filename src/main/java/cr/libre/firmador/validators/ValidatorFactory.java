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
        try {
        validator.loadDocumentPath(fileName);
        } catch (java.lang.UnsupportedOperationException e) {
            return null; // format is not detected by dss
        }
        return validator;
    }

    public static Validator getValidator(byte[] data, String name) {
        return null;
    }

}
