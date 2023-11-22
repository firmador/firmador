package cr.libre.firmador.gui.swing;

import javax.swing.JButton;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import cr.libre.firmador.documents.Document;
import cr.libre.firmador.gui.GUISwing;

@SuppressWarnings("serial")
public class DocumentTableButton extends JButton {
    public static final int CHOOSE_SAVE_FILENAME = 1;
    public static final int CHANGE_FORMAT = 2;
    public static final int GO_TO_VALIDATE = 3;
    public static final int GO_TO_SIGN = 4;
    public static final int SIGN_DOCUMENT = 5;
    public static final int REMOVE_DOCUMENT = 6;

    private int selectedAction = 0;
    private Document document;

    private class goToSignActionListener implements ActionListener {

        private Document document;

        goToSignActionListener(Document document) {
            this.document = document;
        }
        @Override
        public void actionPerformed(ActionEvent arg0) {
            // TODO Auto-generated method stub
            System.out.print(document.getName());
            GUISwing gui = (GUISwing) document.getGUI();
            gui.loadActiveDocument(document);
            gui.displayFunctionality("sign");
        }

    }

    private class ChooseSaveFileActionListener implements ActionListener {

        private Document document;

        ChooseSaveFileActionListener(Document document) {
            this.document = document;
        }

        @Override
        public void actionPerformed(ActionEvent arg0) {
            // TODO Auto-generated method stub
            System.out.print(document.getName());
            GUISwing gui = (GUISwing) document.getGUI();
            String savefile = gui.showSaveDialog(document.getName(), document.getExtension());
            document.setPathToSaveName(savefile);
            ListDocumentTablePanel docpanel = gui.getListDocumentTablePanel();
            docpanel.updateDocument(document);
        }

    }

    private class ChangeFormatActionListener implements ActionListener {

        private Document document;

        ChangeFormatActionListener(Document document) {
            this.document = document;
        }

        @Override
        public void actionPerformed(ActionEvent arg0) {
            // TODO Auto-generated method stub
            System.out.print("Change document format");

        }

    }

    private class goToValidateActionListener implements ActionListener {


        private Document document;

        goToValidateActionListener(Document document) {
            this.document = document;
        }

        @Override
        public void actionPerformed(ActionEvent arg0) {
            // TODO Auto-generated method stub
            System.out.print(document.getName());
            GUISwing gui = (GUISwing) document.getGUI();
            gui.loadActiveDocument(document);
            gui.displayFunctionality("validate");
        }

    }

    private class SignActionListener implements ActionListener {

        private Document document;

        SignActionListener(Document document) {
            this.document = document;
        }

        @Override
        public void actionPerformed(ActionEvent arg0) {
            // TODO Auto-generated method stub
            System.out.print(document.getName());
            GUISwing gui = (GUISwing) document.getGUI();
            gui.signDocument(document);
            // gui.displayFunctionality("sign");
        }

    }

    private class removeActionListener implements ActionListener {

        private Document document;

        removeActionListener(Document document) {
            this.document = document;
        }

        @Override
        public void actionPerformed(ActionEvent arg0) {
            GUISwing gui = (GUISwing) document.getGUI();
            ListDocumentTablePanel docpanel = gui.getListDocumentTablePanel();
            docpanel.removeDocument(document);
        }

    }

    public DocumentTableButton(String message) {
        super(message);
    }

    public DocumentTableButton(Document document, String message, int selectedAction) {
        super(message);
        this.document = document;
        this.selectedAction = selectedAction;

        switch (selectedAction) {
        case CHOOSE_SAVE_FILENAME:
            this.addActionListener(new ChooseSaveFileActionListener(document));
            break;
        case CHANGE_FORMAT:
            this.addActionListener(new ChangeFormatActionListener(document));
            break;
        case GO_TO_VALIDATE:
            this.addActionListener(new goToValidateActionListener(document));
            break;
        case GO_TO_SIGN:
            this.addActionListener(new goToSignActionListener(document));
            break;
        case SIGN_DOCUMENT:
            this.addActionListener(new SignActionListener(document));
            break;
        case REMOVE_DOCUMENT:
            this.addActionListener(new removeActionListener(document));
            break;
        }
    }

    public void setDocument(Document document) {
        this.document = document;
    }

    public Document getDocument() {
        return document;
    }
}
