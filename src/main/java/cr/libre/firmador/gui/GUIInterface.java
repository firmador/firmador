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

package cr.libre.firmador.gui;

import org.apache.pdfbox.pdmodel.PDDocument;

import cr.libre.firmador.Settings;
import cr.libre.firmador.cards.CardSignInfo;
import cr.libre.firmador.documents.Document;
import cr.libre.firmador.documents.SupportedMimeTypeEnum;
import cr.libre.firmador.plugins.PluginManager;

public interface GUIInterface {

    void loadGUI();
    void setArgs(String[] args);
    void showError(Throwable error);
    void showMessage(String message);
    String getDocumentToSign();
    String getPathToSave(String extension);
    CardSignInfo getPin();
    void setPluginManager(PluginManager pluginManager);
    public Document loadDocument(String fileName);

    public void loadDocument(SupportedMimeTypeEnum mimeType, PDDocument doc);
    public void extendDocument();
    String getPathToSaveExtended(String extension);
    public boolean signDocuments();
    public void displayFunctionality(String functionality);
    public void nextStep(String msg);

    void previewDone(Document document);

    void validateDone(Document document);

    void signDone(Document document);

    void extendsDone(Document document);

    void doPreview(Document document);
    void validateAllDone();

    void signAllDone();

    Settings getCurrentSettings();

    void signDocument(Document document);

    void clearDone();
}
