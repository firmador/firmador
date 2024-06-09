package cr.libre.firmador.remote;

import jakarta.mail.BodyPart;
import jakarta.mail.MessagingException;
import jakarta.mail.Multipart;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMultipart;

import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.HttpEntity;

import cr.libre.firmador.documents.Document;
import cr.libre.firmador.gui.GUIInterface;

import jakarta.activation.DataSource;
import jakarta.activation.DataHandler;
import jakarta.activation.FileDataSource;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class MultipartParser {

    public static List<Document> parseMultipart(HttpEntity httpEntity, GUIInterface gui)
            throws IOException, MessagingException {
        // Obtén el tipo de contenido del HttpEntity
        String contentType = ContentType.MULTIPART_FORM_DATA.toString();
        List<Document> documentlist = new ArrayList<Document>();
            // Crea un objeto Multipart a partir del contenido del HttpEntity
            Multipart multipart = new MimeMultipart(new ByteArrayDataSource(httpEntity.getContent(), contentType));

            // Itera sobre cada parte del multipart
            for (int i = 0; i < multipart.getCount(); i++) {
                BodyPart bodyPart = multipart.getBodyPart(i);

                // Verifica si la parte es un archivo adjunto
                if (bodyPart instanceof MimeBodyPart) {
                    MimeBodyPart mimeBodyPart = (MimeBodyPart) bodyPart;
                    String fileName = mimeBodyPart.getFileName();
                    documentlist.add(new Document(gui, mimeBodyPart.getInputStream().readAllBytes(), fileName,
                            Document.STATUS_TOSIGN));
                } else {
                    // La parte es un contenido en texto plano o HTML
                    String text = bodyPart.getContent().toString();
                    System.out.println(text);
                }
            }
            return documentlist;

    }

    // Clase de utilidad para convertir un InputStream en un DataSource
    private static class ByteArrayDataSource implements DataSource {
        private byte[] data;
        private String contentType;

        public ByteArrayDataSource(InputStream inputStream, String contentType) throws IOException {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                baos.write(buffer, 0, bytesRead);
            }
            this.data = baos.toByteArray();
            this.contentType = contentType;
        }

        @Override
        public InputStream getInputStream() throws IOException {
            return new ByteArrayInputStream(data);
        }

        @Override
        public OutputStream getOutputStream() throws IOException {
            throw new UnsupportedOperationException("Not supported");
        }

        @Override
        public String getContentType() {
            return contentType;
        }

        @Override
        public String getName() {
            throw new UnsupportedOperationException("Not supported");
        }
    }
}
