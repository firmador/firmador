package cr.libre.firmador.gui.swing;

import java.awt.Component;
import java.util.EventObject;

import javax.swing.AbstractCellEditor;
import javax.swing.JButton;
import javax.swing.JTable;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

import cr.libre.firmador.documents.Document;

public class PanelCellRenderer extends AbstractCellEditor
        implements TableCellRenderer, TableCellEditor {

    private static final long serialVersionUID = 1L;
    private DocumentCellPanel renderer = new DocumentCellPanel();


    public PanelCellRenderer() {
    }

    @Override
    public boolean isCellEditable(EventObject anEvent) {
        return true;
    }

    public boolean isCellEditable(int row, int col) {
        return true;
    }

    @Override
    public boolean shouldSelectCell(EventObject anEvent) {
        return true;
    }

    // JTable table, Object value, boolean isSelected, boolean hasFocus, int row,
    // int column
    @Override
    public Component getTableCellRendererComponent(JTable table, Object arg1, boolean isSelected, boolean arg3,
            int arg4, int arg5) {
        JButton tabelButton = renderer.getRemoveButton();
        renderer.setDocument((Document) arg1);
        renderer.setForeground(table.getSelectionForeground());
        renderer.setBackground(table.getSelectionBackground());

        return renderer;
    }




    @Override
    public Component getTableCellEditorComponent(JTable arg0, Object arg1, boolean arg2, int arg3, int arg4) {
        return renderer;
    }

    @Override
    public Object getCellEditorValue() {
        return renderer.getDocument();
    }

}
