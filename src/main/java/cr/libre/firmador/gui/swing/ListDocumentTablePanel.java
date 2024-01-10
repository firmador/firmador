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

import cr.libre.firmador.MessageUtils;
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

    private class CleanDocumentsAction implements ActionListener {
        private ListDocumentTablePanel panel;

        public CleanDocumentsAction(ListDocumentTablePanel panel) {
            this.panel = panel;
        }

        @Override
        public void actionPerformed(ActionEvent arg0) {
            this.panel.cleanDocuments();
            this.panel.gui.clearDone();
        }
    }

    public ListDocumentTablePanel() {
        super();
        this.setLayout(new BorderLayout());

        actionButtonsPanel = new JPanel();
        JButton signbtn = new JButton(MessageUtils.t("list_document_signall"));
        signbtn.setToolTipText(MessageUtils.t("list_document_signall"));
        signbtn.getAccessibleContext().setAccessibleDescription(MessageUtils.t("list_document_signall"));

        JButton cleanbtn = new JButton(MessageUtils.t("list_document_clear"));
        cleanbtn.setToolTipText(MessageUtils.t("list_document_clear"));
        cleanbtn.getAccessibleContext().setAccessibleDescription(MessageUtils.t("list_document_clear"));

        signbtn.setMnemonic('S');
        cleanbtn.setMnemonic('C');
        signbtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                if(table.getRowCount() > 0) {
                    ((GUISwing) gui).signAllDocuments();
                }else {
                    gui.showMessage(MessageUtils.t("list_document_no_documents_to_sign"));
                }
            }
        });

        cleanbtn.addActionListener(new CleanDocumentsAction(this));

        actionButtonsPanel.add(signbtn);
        actionButtonsPanel.add(cleanbtn);

        model = new ListDocumentTableModel();
        table = new JTable(model);

        table.setShowHorizontalLines(true);

        JPanel tablePanel = new JPanel();

        PanelCellRenderer panelCellRenderer = new PanelCellRenderer();
        table.setDefaultRenderer(DocumentTableButton.class, panelCellRenderer);
        table.setDefaultEditor(DocumentTableButton.class, panelCellRenderer);
        table.setDefaultRenderer(Document.class, panelCellRenderer);
        table.setDefaultEditor(Document.class, panelCellRenderer);

        table.setFillsViewportHeight(true);
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

    public void addDocuments(List<Document> docs) {
        for (Document doc : docs) {
            model.addData(doc);
            doc.registerListener(this);
        }
        model.fireTableDataChanged();
    }

    public void removeDocument(Document document) {
        model.removeData(document);
        model.fireTableDataChanged();
    }

    public void cleanDocuments() {
        model.cleanDocuments();
        model.fireTableDataChanged();
    }

    public void updateDocument(Document document) {
        model.updateDocument(document);
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
            model.fireTableDataChanged();

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
            model.fireTableDataChanged();

            // model.setValueAt(document.amountOfSignatures(), position,
            // ListDocumentTableModel.NUM_SIGNATURE_POSITION);
        }
    }

    @Override
    public void signDone(Document document) {
        int position = model.findByDocument(document);
        if (position >= 0) {

            DocumentTableButton btn = (DocumentTableButton) model.getValueAt(position,
                    ListDocumentTableModel.NUM_SIGNATURE_POSITION);
            btn.setText("" + document.amountOfSignatures());
            model.fireTableDataChanged();

            // model.setValueAt(document.amountOfSignatures(), position,
            // ListDocumentTableModel.NUM_SIGNATURE_POSITION);
        }
    }

    @Override
    public void extendsDone(Document document) {
        // TODO Auto-generated method stub

    }

    @Override
    public void clearDone() {
        // TODO Auto-generated method stub

    }

    public ListDocumentTableModel getModel() {
        return model;
    }
}
