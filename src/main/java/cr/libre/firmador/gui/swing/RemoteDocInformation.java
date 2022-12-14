/* Firmador is a program to sign documents using AdES standards.

Copyright (C) 2018, 2022 Firmador authors.

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

package cr.libre.firmador.gui.swing;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

public class RemoteDocInformation {
    String name;
    ByteArrayOutputStream data;
    InputStream inputdata;
    int status;

    public RemoteDocInformation(String name, ByteArrayOutputStream data, int status) {
        super();
        this.name = name;
        this.data = data;
        this.status = status;

    }

    public InputStream getInputdata() {
        return inputdata;
    }

    public void setInputdata(InputStream inputdata) {
        this.inputdata = inputdata;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ByteArrayOutputStream getData() {
        return data;
    }

    public void setData(ByteArrayOutputStream data) {
        this.data = data;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

}
