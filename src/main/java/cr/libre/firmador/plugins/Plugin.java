/* Firmador is a program to sign documents using AdES standards.

Copyright (C) Firmador authors.

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

import cr.libre.firmador.documents.Document;

public interface Plugin {

    public boolean isrunnable = false;

    // Es el primer método que se llama en el plugin.
    public void start();

    // Permite iniciar el bitacoreo
    public void startLogging();

    // Es el último método que se llama al cerrar la aplicación
    public void stop();

    public boolean getIsRunnable();

    public boolean interactWithDocuments();

}
