package cr.libre.firmador.documents;

import java.io.File;
import java.io.IOException;

import org.apache.tika.Tika;

import eu.europa.esig.dss.model.DSSDocument;

public class MimeTypeDetector {

    public static SupportedMimeTypeEnum detect(String fileName) {
           File file = new File(fileName);
           Tika tika = new Tika();
           String mimeType= null;
           String current_mimetype;
           SupportedMimeTypeEnum selected = SupportedMimeTypeEnum.BINARY;
           try {
                mimeType = tika.detect(file);
           } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
           }
           for (SupportedMimeTypeEnum supportedmimetype : SupportedMimeTypeEnum.values()) {
               current_mimetype=supportedmimetype.getMimeTypeString();
               if (current_mimetype.contentEquals(mimeType)) {
                   selected = supportedmimetype;
                   break;
               }
           }
        return selected;
    }
    public static SupportedMimeTypeEnum detect(byte [] data,  String name) {
        SupportedMimeTypeEnum selected = SupportedMimeTypeEnum.BINARY;
         Tika tika = new Tika();
           String mimeType= null;
           String current_mimetype;
           mimeType = tika.detect(data, name);
           for (SupportedMimeTypeEnum supportedmimetype : SupportedMimeTypeEnum.values()) {
               current_mimetype=supportedmimetype.getMimeTypeString();
               if (current_mimetype.contentEquals(mimeType)) {
                   selected = supportedmimetype;
                   break;
               }
           }
        return selected;
    }

    public static SupportedMimeTypeEnum detect(DSSDocument toSignDocument) {
        /**
         * Una reimplementación será necesaria en un futuro próximo ya que la forma de detectar a este punto es un poco arcaica
         * */

        if (toSignDocument.getName().endsWith(".xlsx") || toSignDocument.getName().endsWith(".XLSX")) {
            return  SupportedMimeTypeEnum.XLSX;
        }
        if (toSignDocument.getName().endsWith(".docx") || toSignDocument.getName().endsWith(".DOCX")) {
            return SupportedMimeTypeEnum.DOCX;
        }
        if (toSignDocument.getName().endsWith(".pptx") || toSignDocument.getName().endsWith(".PPTX")) {
            return SupportedMimeTypeEnum.PPTX;
        }

        String mimeType = toSignDocument.getMimeType().getMimeTypeString();
        String current_mimetype;
        SupportedMimeTypeEnum selected = SupportedMimeTypeEnum.BINARY;
        for (SupportedMimeTypeEnum supportedmimetype : SupportedMimeTypeEnum.values()) {
            current_mimetype = supportedmimetype.getMimeTypeString();
            if (current_mimetype.contentEquals(mimeType)) {
                selected = supportedmimetype;
                break;
            }
        }
        return selected;
    }

}
