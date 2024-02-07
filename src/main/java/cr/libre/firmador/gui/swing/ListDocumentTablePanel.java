package cr.libre.firmador.gui.swing;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.lang.invoke.MethodHandles;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ScrollPaneConstants;

import com.opencsv.CSVReader;
import cr.libre.firmador.SettingsManager;
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

    private Path documentListSavePath = FileSystems.getDefault().getPath(SettingsManager.getInstance().getPath().getParent().toString(), "document_list.csv");

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

    private class PreviewAllAction implements ActionListener {
        private ListDocumentTablePanel panel;

        public PreviewAllAction(ListDocumentTablePanel panel) {
            this.panel = panel;
        }

        @Override
        public void actionPerformed(ActionEvent arg0) {
            if (this.panel.table.getRowCount() > 0) {
                ((GUISwing) this.panel.gui).ScheduleAllPreviewDocuments();
            } else {
                gui.showMessage(MessageUtils.t("list_document_previewall_empty_action"));
            }
        }
    }

    private class SignAllAction implements ActionListener {
        private ListDocumentTablePanel panel;

        public SignAllAction(ListDocumentTablePanel panel) {
            this.panel = panel;
        }

        @Override
        public void actionPerformed(ActionEvent arg0) {
            if (this.panel.table.getRowCount() > 0) {
                ((GUISwing) this.panel.gui).signAllDocuments();
            } else {
                gui.showMessage(MessageUtils.t("list_document_no_documents_to_sign"));
            }
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

        JButton savedoclistbtn = new JButton(MessageUtils.t("list_document_save_list"));
        savedoclistbtn.setToolTipText(MessageUtils.t("list_document_save_list"));
        savedoclistbtn.getAccessibleContext().setAccessibleDescription(MessageUtils.t("list_document_save_list"));

        JButton loaddoclistbtn = new JButton(MessageUtils.t("list_document_load_list"));
        loaddoclistbtn.setToolTipText(MessageUtils.t("list_document_load_list"));
        loaddoclistbtn.getAccessibleContext().setAccessibleDescription(MessageUtils.t("list_document_load_list"));

        JButton previewallbtn = new JButton(MessageUtils.t("list_document_previewall"));
        previewallbtn.setToolTipText(MessageUtils.t("list_document_previewall_tooltip"));
        previewallbtn.getAccessibleContext()
                .setAccessibleDescription(MessageUtils.t("list_document_previewall_tooltip_accesible"));

        signbtn.setMnemonic('S');
        cleanbtn.setMnemonic('C');
        savedoclistbtn.setMnemonic('D');
        loaddoclistbtn.setMnemonic('L');
        previewallbtn.setMnemonic('P');

        signbtn.addActionListener(new SignAllAction(this));
        cleanbtn.addActionListener(new CleanDocumentsAction(this));
        previewallbtn.addActionListener(new PreviewAllAction(this));

        savedoclistbtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                try {
                    if(table.getRowCount() > 0) {
                        saveDocumentList();
                    }else{
                        gui.showMessage(MessageUtils.t("list_document_no_documents_to_save"));
                    }
                } catch (Exception e) {
                    gui.showMessage(MessageUtils.t("list_document_error_during_save"));
                }
            }
        });

        loaddoclistbtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                try {
                    if(Files.exists(documentListSavePath)) {
                        loadDocumentList();
                    }else{
                        gui.showMessage(MessageUtils.t("list_document_no_file_to_load"));
                    }
                } catch (Exception e) {
                    gui.showMessage(MessageUtils.t("list_document_error_during_load"));
                }
            }
        });

        actionButtonsPanel.add(signbtn);
        actionButtonsPanel.add(savedoclistbtn);
        actionButtonsPanel.add(loaddoclistbtn);
        actionButtonsPanel.add(cleanbtn);
        actionButtonsPanel.add(previewallbtn);

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

    public void saveDocumentList() throws IOException {
        model.saveDocumentList(this.documentListSavePath.toString());
        gui.showMessage(MessageUtils.t("list_document_save_done") + " " + this.documentListSavePath.toString());
    }

    public void loadDocumentList() throws Exception{
        ArrayList<String > docsNotFound = new ArrayList<>();
        CSVReader reader = new CSVReader(new FileReader(this.documentListSavePath.toFile()));

        cleanDocuments();  // clear the list before loading what is saved

        String [] fileLine;
        reader.readNext();  // to get the header, ignore it
        while ((fileLine = reader.readNext()) != null) {
            String documentPath = fileLine[1];
            if(Files.exists(Paths.get(documentPath))) {  // load the document only if it exists in the filesystem
                Document document = gui.loadDocument(documentPath);

                // set the document settings, so it is as it was before saving/loading
                document.setSettings(SettingsManager.getInstance().loadDocumentSettings(fileLine[3]));

                // update the path to save in case the user selected something different from default before saving
                document.setPathToSave(fileLine[2]);
                updateDocument(document);
            }else {
                docsNotFound.add(documentPath);
            }
        }
        reader.close();

        if(!docsNotFound.isEmpty()){
            gui.showMessage(MessageUtils.t("list_document_files_not_found_on_load") + " " + String.join(", ", docsNotFound));
        }
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
        }
    }

    @Override
    public void signDone(Document document) {
        int position = model.findByDocument(document);
        if (position >= 0) {
            if(!document.getSignwithErrors()) {
                // remove the non-signed document and add the signed one to the list of documents
                removeDocument(document);
                gui.loadDocument(document.getPathToSave());
            }
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

    @Override
    public void previewAllDone() {
        // TODO Auto-generated method stub

    }

    @Override
    public void validateAllDone() {
        // TODO Auto-generated method stub

    }

    @Override
    public void signAllDone() {
        // TODO Auto-generated method stub

    }
}
