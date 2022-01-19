/* Firmador is a program to sign documents using AdES standards.

Copyright (C) 2021 Firmador authors.

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
package cr.libre.firmador.plugins;

import java.security.KeyStore;

import cr.libre.firmador.CRSigner;
import eu.europa.esig.dss.token.Pkcs11SignatureToken;

public class DummyPlugin implements Plugin {

	public void start() {
		System.out.println("Stating DummyPlugin");
		//KeyStore keyStore = KeyStore.getInstance(SUN_PKCS11_KEYSTORE_TYPE, getProvider());
		//signingToken = new Pkcs11SignatureToken(getPkcs11Lib(), "");
		
		CRSigner crsigner  = new CRSigner(null);
	}

	public void stop() {
		System.out.println("Stop DummyPlugin");
	}
}
