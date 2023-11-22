package cr.libre.firmador.gui.swing;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ScrollPaneConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cr.libre.firmador.documents.Document;
import cr.libre.firmador.documents.DocumentChangeListener;
import cr.libre.firmador.gui.GUIInterface;
import cr.libre.firmador.gui.GUISwing;


@SuppressWarnings("serial")
public class ListDocumentTablePanel extends ScrollableJPanel implements DocumentChangeListener {
    final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private GUIInterface gui;
    private JTable table;
    private JPanel actionButtonsPanel;
    private ListDocumentTableModel model;
    public void setGUI(GUIInterface gui) {
        this.gui = gui;
    }

    public ListDocumentTablePanel() {
        super();
        this.setLayout(new BorderLayout());

        actionButtonsPanel = new JPanel();
        JButton signbtn = new JButton("Firmar todos los documentos");

        signbtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                ((GUISwing) gui).signAllDocuments();
            }
        });

        actionButtonsPanel.add(signbtn);
        model = new ListDocumentTableModel();
        table = new JTable(model);

        table.setShowHorizontalLines(true);
        // UIManager.put("Button.defaultButtonFollowsFocus", Boolean.TRUE);
        
        JPanel tablePanel = new JPanel();
        
        PanelCellRenderer panelCellRenderer = new PanelCellRenderer();
        // table.addMouseListener(panelCellRenderer);
        table.setDefaultRenderer(DocumentTableButton.class, panelCellRenderer);
        table.setDefaultEditor(DocumentTableButton.class, panelCellRenderer);
        table.setDefaultRenderer(Document.class, panelCellRenderer);
        table.setDefaultEditor(Document.class, panelCellRenderer);

        // table.setPreferredScrollableViewportSize(new Dimension(500, 70));
        table.setFillsViewportHeight(true);
        table.setBorder(BorderFactory.createLineBorder(Color.red));
        // add(table);
        tablePanel.setLayout(new BorderLayout());
        tablePanel.add(table.getTableHeader(), BorderLayout.PAGE_START);
        tablePanel.add(table, BorderLayout.CENTER);
        this.add(actionButtonsPanel, BorderLayout.PAGE_START);
        this.add(tablePanel, BorderLayout.CENTER);

    }

    public JScrollPane getListDocumentScrollPane() {
        JScrollPane listTableScrollPane = new JScrollPane();
        listTableScrollPane.setPreferredSize(listTableScrollPane.getPreferredSize());
        listTableScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        listTableScrollPane.setBorder(BorderFactory.createLineBorder(Color.black));
        listTableScrollPane.setViewportView(this);
        listTableScrollPane.setOpaque(false);
        listTableScrollPane.getViewport().setOpaque(false);
        listTableScrollPane.setVisible(true);
        return listTableScrollPane;
    }

    public void addDocument(Document document) {
        model.addData(document);
        model.fireTableDataChanged();
        document.registerListener(this);
        // table.setModel(model);
    }

    public void removeDocument(Document document) {

    }

    public void updateDocument(Document document) {
        model.fireTableDataChanged();
    }

    public List<Document> getSelectedDocuments() {
        Document doctoadd;
        List<Document> selectedDocument = new ArrayList<Document>();
        for (int row : table.getSelectedRows()) {
            doctoadd = (Document) ((DocumentTableButton) model.getValueAt(row,
                    ListDocumentTableModel.DOCUMENT_POSITION)).getDocument();
            selectedDocument.add(doctoadd);
        }
        return selectedDocument;
    }

    public Document getActiveDocument() {
        Document returnedDocument = null;
        int rowcount = model.getRowCount();
        if (rowcount > 0)
            returnedDocument = ((DocumentTableButton) model.getValueAt(0, ListDocumentTableModel.DOCUMENT_POSITION))
                    .getDocument();
        return returnedDocument;
    }

    public List<Document> getAllDocuments() {
        int rowcount = model.getRowCount();
        int position = 0;
        List<Document> selectedDocument = new ArrayList<Document>();

        while (position < rowcount) {
            selectedDocument.add((Document) ((DocumentTableButton) model.getValueAt(position,
                    ListDocumentTableModel.DOCUMENT_POSITION)).getDocument());
            position += 1;
        }
        return selectedDocument;
    }

    @Override
    public void previewDone(Document document) {
        int position = model.findByDocument(document);
        if (position >= 0) {
            DocumentTableButton btn = (DocumentTableButton) model.getValueAt(position,
                    ListDocumentTableModel.NUM_PAGES_POSITION);
            btn.setText("" + document.getNumberOfPages());
            // model.setValueAt(document.amountOfSignatures(), position,
            // ListDocumentTableModel.NUM_SIGNATURE_POSITION);
        }

    }

    @Override
    public void validateDone(Document document) {
        int position = model.findByDocument(document);
        if (position >= 0) {
            DocumentTableButton btn = (DocumentTableButton) model.getValueAt(position,
                    ListDocumentTableModel.NUM_SIGNATURE_POSITION);
            btn.setText("" + document.amountOfSignatures());
            // model.setValueAt(document.amountOfSignatures(), position,
            // ListDocumentTableModel.NUM_SIGNATURE_POSITION);
        } else {

        }
    }

    @Override
    public void signDone(Document document) {
        // TODO Auto-generated method stub

    }

    @Override
    public void extendsDone(Document document) {
        // TODO Auto-generated method stub

    }
}
