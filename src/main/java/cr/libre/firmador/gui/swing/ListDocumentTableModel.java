package cr.libre.firmador.gui.swing;

import java.io.*;
import java.nio.file.FileSystems;
import java.util.ArrayList;
import java.util.List;

import javax.swing.table.AbstractTableModel;

import com.opencsv.CSVWriter;
import cr.libre.firmador.MessageUtils;
import cr.libre.firmador.Settings;
import cr.libre.firmador.SettingsManager;
import cr.libre.firmador.documents.Document;
import org.apache.commons.io.FileUtils;

@SuppressWarnings("serial")
public class ListDocumentTableModel extends AbstractTableModel {
    public static int NAME_POSITION = 0;
    public static int SAVE_PATH = 1;
    public static int DOCUMENT_MIMETYPE = 2;
    public static int NUM_SIGNATURE_POSITION = 3;
    public static int NUM_PAGES_POSITION = 4;
    public static int SIGNATURE_BTN = 5;
    public static int DOCUMENT_POSITION = 6;

    private String[] columnNames = {
            MessageUtils.t("list_document_table_name"),
            MessageUtils.t("list_document_save_path"),
            MessageUtils.t("list_document_mimetype"),
            MessageUtils.t("list_document_numsign"),
            MessageUtils.t("list_document_numpages"),
            MessageUtils.t("list_document_dosign"),
            MessageUtils.t("list_document_delete") };
    private List<Object[]> data = new ArrayList<>();

    ListDocumentTableModel() {
    }

    @Override
    public int getColumnCount() {
        return columnNames.length;

    }

    public int getRowCount() {
        return data.size();
    }

    public String getColumnName(int col) {
        return columnNames[col];
    }


    public Object getValueAt(int row, int col) {
        if (row < data.size() && row >= 0) {
            return data.get(row)[col];
        }
        return null;
    }

    public void setValueAt(Object value, int row, int col) {
        if (row < data.size() && row >= 0) {
            data.get(row)[col] = value;
            fireTableCellUpdated(row, col);
        }
    }

    public Class getColumnClass(int c) {
        return getValueAt(0, c).getClass();
    }

    public boolean isCellEditable(int row, int col) {
        return true;
    }

    public void addData(Document document) {
        String savedpath = document.getPathToSaveName();
        if (document.getIsremote()) {
            savedpath = "";
        }
        Object[] datadocument = {
                document.getName(),
                new DocumentTableButton(document, savedpath,
                        DocumentTableButton.CHOOSE_SAVE_FILENAME),
                new DocumentTableButton(document, document.getMimeType().getExtension(),
                        DocumentTableButton.CHANGE_FORMAT),
                new DocumentTableButton(document, MessageUtils.t("list_document_enqueue"),
                        DocumentTableButton.GO_TO_VALIDATE),
                new DocumentTableButton(document, MessageUtils.t("list_document_preview"),
                        DocumentTableButton.GO_TO_SIGN),
                new DocumentTableButton(document, MessageUtils.t("list_document_sign"),
                        DocumentTableButton.SIGN_DOCUMENT),
                new DocumentTableButton(document, MessageUtils.t("list_document_delete"),
                        DocumentTableButton.REMOVE_DOCUMENT)

        };
        int lastsize = data.size();
        data.add(0, datadocument);
        fireTableRowsInserted(lastsize - 1, data.size() - 1);
    }

    public void removeData(Document document) {
        int position = findByDocument(document);
        if (position >= 0) {
            data.remove(position);
        }
    }

    public int findByDocument(Document doc) {
        int position = -1;
        int currentPosition = 0;
        int datasize=data.size();
        while (currentPosition < datasize) {
            Object[] obj = data.get(currentPosition);
            Document listDoc = ((DocumentTableButton) obj[DOCUMENT_POSITION]).getDocument();
            if (listDoc == doc) {
                position = currentPosition;
                break;
            }
            currentPosition += 1;
        }
        return position;
    }

    public void cleanDocuments() {
        data.clear();
    }

    public void saveDocumentList(String savePath) throws IOException {
        // delete previous saved settings since documents are being saved again
        File settingsDir = new File(FileSystems.getDefault().getPath(SettingsManager.getInstance().getConfigDir().toString(), "docSettings").toString());
        if(settingsDir.exists())  FileUtils.forceDelete(settingsDir);

        CSVWriter writer = new CSVWriter(new FileWriter(savePath));
        writer.writeNext(new String[]{"name", "pathName", "pathToSave", "settingsPath"});  // write the header for the document

        for(Object[] objList : data) {
            Document document = ((DocumentTableButton) objList[1]).getDocument();
            String settingsPath = SettingsManager.getInstance().saveDocumentSettings(document.getSettings(), document.getName());
            String[] dataToSave = {document.getName(), document.getPathName(), document.getPathToSave(), settingsPath};
            writer.writeNext(dataToSave);
        }
        writer.close();
    }

    public void updateDocument(Document document) {
        int position = findByDocument(document);
        if (position >= 0) {
            Object[] docbtn = data.get(position);
            docbtn[ListDocumentTableModel.NAME_POSITION] = document.getName();
            ((DocumentTableButton) docbtn[ListDocumentTableModel.SAVE_PATH]).setText("" + document.getPathToSaveName());
            ((DocumentTableButton) docbtn[ListDocumentTableModel.DOCUMENT_MIMETYPE])
                    .setText("" + document.getExtension());
            ((DocumentTableButton) docbtn[ListDocumentTableModel.NUM_SIGNATURE_POSITION])
                    .setText("" + document.amountOfSignatures());
            ((DocumentTableButton) docbtn[ListDocumentTableModel.NUM_PAGES_POSITION])
                    .setText("" + document.getNumberOfPages());
            data.set(position, docbtn);
        }

    }

    public void updateSaveDirDocuments(String directory) {
        int currentPosition = 0;
        int datasize = data.size();
        while (currentPosition < datasize) {
            Object[] obj = data.get(currentPosition);
            Document doc = ((DocumentTableButton) obj[DOCUMENT_POSITION]).getDocument();
            doc.setPathToSave(directory + File.separatorChar + doc.getName());
            ((DocumentTableButton) obj[ListDocumentTableModel.SAVE_PATH]).setText("" + doc.getPathToSaveName());
            data.set(currentPosition, obj);
            currentPosition += 1;
        }
    }

    public void updateDocumentBySettings(Settings currentSettings) {
        int currentPosition = 0;
        int datasize = data.size();
        while (currentPosition < datasize) {
            Object[] obj = data.get(currentPosition);
            Document doc = ((DocumentTableButton) obj[DOCUMENT_POSITION]).getDocument();
            doc.setSettings(currentSettings);
            data.set(currentPosition, obj);
            currentPosition += 1;
        }
    }

}
