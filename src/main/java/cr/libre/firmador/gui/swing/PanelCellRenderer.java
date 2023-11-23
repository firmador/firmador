package cr.libre.firmador.gui.swing;

import java.awt.Component;
import java.util.EventObject;

import javax.swing.AbstractCellEditor;
import javax.swing.Icon;
import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

public class PanelCellRenderer extends AbstractCellEditor
        implements TableCellRenderer, TableCellEditor {

    private static final long serialVersionUID = 1L;
    private DocumentCellPanel renderer = new DocumentCellPanel();
    private DocumentTableButton btn;

    static class TextRenderer extends DefaultTableCellRenderer {

        public TextRenderer() {
            super();
        }

        public void setValue(Object value) {

            setText(((DocumentTableButton) value).getText());
        }
    }

    public PanelCellRenderer() {
    }

    @Override
    public boolean isCellEditable(EventObject anEvent) {
        return true;
    }

    public boolean isCellEditable(int row, int col) {
        if (col == 0)
            return false;
        return true;
    }

    @Override
    public boolean shouldSelectCell(EventObject anEvent) {
        return true;
    }

    // JTable table, Object value, boolean isSelected, boolean hasFocus, int row,
    // int column
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
            int row, int column) {
        
        DocumentTableButton btn = (DocumentTableButton) value;
        renderer.setDocument(btn.getDocument());
        TextRenderer render = new TextRenderer();
        if (column == ListDocumentTableModel.DOCUMENT_POSITION) {
            Icon icon = UIManager.getIcon("InternalFrame.closeIcon");
            // Icon icon = new
            // ImageIcon(this.getClass().getClassLoader().getResource("firmador.png"));
            render.setIcon(icon);

        } else
        render.setText(btn.getText());

        return render;
    }




    @Override
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        btn = (DocumentTableButton) value;
        return btn;
    }

    @Override
    public Object getCellEditorValue() {
        return btn;
    }

}
