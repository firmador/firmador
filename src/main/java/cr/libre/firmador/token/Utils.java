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

package cr.libre.firmador.token;

public class Utils {

    public static String getPKCS11Lib() {
        String osName = System.getProperty("os.name").toLowerCase();
        if (osName.contains("mac")) return "/Library/Application Support/Athena/libASEP11.dylib";
        else if (osName.contains("linux")) return "/usr/lib/x64-athena/libASEP11.so";
        else if (osName.contains("windows")) return System.getenv("SystemRoot") + "\\System32\\asepkcs.dll";
        return "";
    }

}
