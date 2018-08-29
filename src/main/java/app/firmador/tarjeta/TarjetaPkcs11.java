/* Firmador is a program to sign documents using AdES standards.

Copyright (C) 2018 Firmador authors.

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

package app.firmador.tarjeta;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.Principal;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import sun.security.pkcs11.wrapper.CK_ATTRIBUTE;
import sun.security.pkcs11.wrapper.PKCS11;
import sun.security.pkcs11.wrapper.PKCS11Exception;

import com.google.common.base.Splitter;

public class TarjetaPkcs11 {

    private PKCS11 pkcs11 = null;

    public PKCS11 getPkcs11Manager(String rutaLibreriaSmartcard)
        throws IOException, PKCS11Exception {
        if (pkcs11 == null)
        pkcs11 = PKCS11.getInstance(rutaLibreriaSmartcard,
            "C_GetFunctionList", null, false);

        return pkcs11;
    }

    public List<X509Certificate> getCertificates(long numeroDeSlot)
        throws IOException, PKCS11Exception, CertificateException {
        List<X509Certificate> certificates = new ArrayList<X509Certificate>();
        PKCS11 pkcs11 = getPkcs11Manager(Utils.getPKCS11Lib());
        long sessionHandle = pkcs11.C_OpenSession(numeroDeSlot, 4L, null,
            null);
        CK_ATTRIBUTE[] attrs = new CK_ATTRIBUTE[1];
        CK_ATTRIBUTE attr = new CK_ATTRIBUTE();
        attr.type = 0L;
        attr.pValue = Long.valueOf(1L);
        attrs[0] = attr;

        pkcs11.C_FindObjectsInit(sessionHandle, attrs);

        long[] objectHandles = pkcs11.C_FindObjects(sessionHandle, 2L);
        pkcs11.C_FindObjectsFinal(sessionHandle);

        for (long i : objectHandles) {
            CK_ATTRIBUTE attrPriv = new CK_ATTRIBUTE();
            CK_ATTRIBUTE[] attrsP = new CK_ATTRIBUTE[2];
            attrPriv.type = 0L;
            attrPriv.pValue = Long.valueOf(2L);

            byte[] p2Value = get_pkcs11_certificate_attr(sessionHandle, i,
                pkcs11);

            if (p2Value != null) {
                attrsP[0] = attrPriv;
                attrsP[1] = attr;
                pkcs11.C_FindObjectsInit(sessionHandle, attrsP);
                certificates.add(extrae_certs_pkcs11(sessionHandle, i,
                    pkcs11));
                pkcs11.C_FindObjectsFinal(sessionHandle);
            }
        }

        return certificates;
    }

    private byte[] get_pkcs11_certificate_attr(long session, long oHandle,
        PKCS11 pkcs11) throws PKCS11Exception {
        CK_ATTRIBUTE[] atributos = {
            new CK_ATTRIBUTE(258L)
        };

        pkcs11.C_GetAttributeValue(session, oHandle, atributos);

        return atributos[0].getByteArray();
     }

    private X509Certificate extrae_certs_pkcs11(long session, long oHandle,
        PKCS11 pkcs11) throws PKCS11Exception, CertificateException {
        CK_ATTRIBUTE[] attributos = { new CK_ATTRIBUTE(17L) };
        pkcs11.C_GetAttributeValue(session, oHandle, attributos);

        byte[] bytes = attributos[0].getByteArray();

        if (bytes == null) {
            throw new CertificateException("Array de certificados null");
        }
        CertificateFactory certificateFactory = CertificateFactory
            .getInstance("X.509");

        X509Certificate certificado = (X509Certificate) certificateFactory
            .generateCertificate(new ByteArrayInputStream(bytes));

        return certificado;
    }

    public long[] getSlots(PKCS11 pkcs11) throws PKCS11Exception, IOException {
        if (pkcs11 == null) {
            pkcs11 = getPkcs11Manager(Utils.getPKCS11Lib());
        }

        return pkcs11.C_GetSlotList(true);
    }

    public long[] getSlots() throws PKCS11Exception, IOException {
        return getSlots(null);
    }

    public String getPropietario(long slot) {
        String dev = null;

        try {
            List<X509Certificate> certs = getCertificates(slot);
            X509Certificate cert = certs.get(0);
            Principal subjectDN = cert.getSubjectDN();
            Map<String, String> params = Splitter.on(", ")
                .withKeyValueSeparator("=").split(subjectDN.getName());
            dev = params.get("GIVENNAME") + " " + params.get("SURNAME")+" (";

            SimpleDateFormat dtformat = new SimpleDateFormat("dd/MM/yyyy");

            dev += "Vence: " + dtformat.format(cert.getNotAfter()) + ")";
        } catch (CertificateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (PKCS11Exception e) {
            e.printStackTrace();
        }

        return dev;
    }

}
