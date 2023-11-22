package cr.libre.firmador.gui.swing;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.invoke.MethodHandles;

import javax.swing.Action;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cr.libre.firmador.documents.Document;
import cr.libre.firmador.documents.DocumentChangeListener;

class DocumentCellPanel extends JPanel implements ActionListener {
    final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private static final long serialVersionUID = 1L;

    private class JButtonDoc extends JButton {
        private Document document;

        public JButtonDoc(String message) {
            super(message);
        }

        public void setDocument(Document document) {
            this.document=document;
        }

        public void validateDocument() {
            try {
                this.document.validate();
            } catch (Throwable e) {
                LOG.error("Validando documento", e);
                e.printStackTrace();
            }
        }

        public void sign() {
            try {
                this.document.setPrincipal();
            } catch (Throwable e) {
                LOG.error("Error haciendo documento princial", e);
                e.printStackTrace();
            }
        }
    }

    final private JButtonDoc viewSignButton = new JButtonDoc("Ver");
    final private JButtonDoc validateButton = new JButtonDoc("Validar");
    final private JButtonDoc removeButton = new JButtonDoc("X");

    private Document document;
    ActionListener deleteAction = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            JButtonDoc deleteAction = (JButtonDoc) e.getSource();
            int modelRow = Integer.valueOf(e.getActionCommand());
            System.out.println("Action DELETE");
        }
    };
    ActionListener viewPreviewAction = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            JButtonDoc viewSignbtn = (JButtonDoc) e.getSource();

            viewSignbtn.sign();
            System.out.println("Action VIEW PREVIEW");
        }
    };
    ActionListener validateAction = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            JButtonDoc validatebtn = (JButtonDoc) e.getSource();
            validatebtn.validateDocument();
            System.out.println("Action Validate");
        }
    };

    DocumentCellPanel() {
        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));

        // removeButton.setIcon(new
        // ImageIcon(this.getClass().getClassLoader().getResource("refresh.png")));
        removeButton.addActionListener(deleteAction);
        validateButton.addActionListener(validateAction);
        viewSignButton.addActionListener(viewPreviewAction);

        add(viewSignButton);
        add(validateButton);
        add(removeButton);


    }

    public JButton getRemoveButton() {
        return removeButton;
    }

    public void setDocument(Document document) {

        this.document = document;
        viewSignButton.setDocument(document);
        validateButton.setDocument(document);
        removeButton.setDocument(document);
        
    }

    public Document getDocument() {
        return document;
    }

    @Override
    public void actionPerformed(ActionEvent arg0) {
        System.out.println(arg0);

    }

}