package cr.libre.firmador;

import java.io.File;
import java.io.IOException;

import org.apache.tika.Tika;

public class MimeTypeDetector {
	
	public static SupportedMimeTypeEnum detect(String fileName) {
		   File file = new File(fileName);
		   Tika tika = new Tika();
		   String mimeType= null;
		   String current_mimetype;
		   SupportedMimeTypeEnum selected=null;
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

}
