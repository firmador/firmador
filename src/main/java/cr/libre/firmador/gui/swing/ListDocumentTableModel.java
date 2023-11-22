package cr.libre.firmador.gui.swing;

import java.util.ArrayList;
import java.util.List;

import javax.swing.table.AbstractTableModel;
import cr.libre.firmador.documents.Document;

@SuppressWarnings("serial")
public class ListDocumentTableModel extends AbstractTableModel {
    public static int DOCUMENT_POSITION = 6;
    public static int NUM_SIGNATURE_POSITION = 3;
    public static int NUM_PAGES_POSITION = 4;
    private String[] columnNames = { "Nombre", "Ruta de salida", "Formato", "# Firmas", "# Páginas", "Firmar",
            "Quitar" };
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
        return data.get(row)[col];
    }

    public void setValueAt(Object value, int row, int col) {
        data.get(row)[col] = value;
        fireTableCellUpdated(row, col);
    }

    public Class getColumnClass(int c) {
        return getValueAt(0, c).getClass();
    }

    public boolean isCellEditable(int row, int col) {
        return true;
    }

    public void addData(Document document) {
        Object[] datadocument = {
                document.getName(),
                new DocumentTableButton(document, document.getPathToSaveName(),
                        DocumentTableButton.CHOOSE_SAVE_FILENAME),
                new DocumentTableButton(document, document.getMimeType().getExtension(),
                        DocumentTableButton.CHANGE_FORMAT),
                new DocumentTableButton(document, "en cola", DocumentTableButton.GO_TO_VALIDATE),

                new DocumentTableButton(document, "en cola", DocumentTableButton.GO_TO_SIGN),
                new DocumentTableButton(document, "Firmar", DocumentTableButton.SIGN_DOCUMENT),
                new DocumentTableButton(document, "x", DocumentTableButton.REMOVE_DOCUMENT)
                
        };
        int lastsize = data.size();
        data.add(0, datadocument);
        fireTableRowsInserted(lastsize - 1, data.size() - 1);
    }
    
    public int findByDocument(Document doc) {
        int position = -1;
        int currentPosition = 0;
        while (currentPosition < data.size()) {
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


}
