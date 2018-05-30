package cr.fran.gui;

import java.awt.Dialog;
import java.awt.FileDialog;
import java.awt.TextField;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FilenameFilter;
import java.nio.file.Paths;
import java.security.KeyStore.PasswordProtection;
import java.util.ArrayList;
import java.util.List;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

public class GUISwing implements GUIInterface {

    private static Dialog pinDialog;
    private static FileDialog loadDialog;
    private static PasswordProtection pin;
    private String documenttosign = null;
    private String documenttosave = null;

    public String getDocumentToSign() {
        if (documenttosign != null) {
            return documenttosign;
        }
        String fileName = null;

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException e) {
            // TODO
        } catch (InstantiationException e) {
            // TODO
        } catch (IllegalAccessException e) {
            // TODO
        } catch (UnsupportedLookAndFeelException e) {
        }

        loadDialog = new FileDialog(loadDialog,
                "Seleccionar documento a firmar");
            loadDialog.setFilenameFilter(new FilenameFilter() {
                public boolean accept(File dir, String name) {
                    return name.endsWith(".pdf") || name.endsWith(".PDF");
                }
            });
        loadDialog.setFile("*.pdf");
        loadDialog.setLocationRelativeTo(null);
        loadDialog.setVisible(true);
        loadDialog.dispose();
        if (loadDialog.getFile() == null) {
            System.out.println("Sintaxis: firmador.jar fichero.pdf");
            System.exit(1);
        } else {
            fileName = loadDialog.getDirectory() + loadDialog.getFile();
        }

        return fileName;
    }

    public String getPathToSave() {
        if (documenttosave != null) {
            return documenttosave;
        }

        String fileName = null;
        FileDialog saveDialog = null;
        saveDialog = new FileDialog(saveDialog,
            "Guardar documento", FileDialog.SAVE);
        String saveDirectory = null;
        String saveFileName = null;

        saveDirectory = loadDialog.getDirectory();
        saveFileName = loadDialog.getFile();

        saveDialog.setDirectory(saveDirectory);
        saveDialog.setFile(saveFileName.substring(0,
            saveFileName.lastIndexOf(".")) + "-firmado.pdf");
        saveDialog.setFilenameFilter(loadDialog.getFilenameFilter());
        saveDialog.setLocationRelativeTo(null);
        saveDialog.setVisible(true);
        saveDialog.dispose();
        if (saveDialog.getFile() == null) {
            System.exit(1);
        } else {
            fileName = saveDialog.getDirectory() + saveDialog.getFile();
        }

        return fileName;
    }

    public PasswordProtection getPin() {
        pinDialog = new Dialog(pinDialog, "Ingresar PIN", true);
        pinDialog.setLocationRelativeTo(null);
        final TextField pinField = new TextField(17);
        pinField.setEchoChar('●');
        pinField.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_ENTER:
                        pin = new PasswordProtection(pinField.getText()
                            .toCharArray());
                        pinDialog.dispose();
                        break;
                    case KeyEvent.VK_ESCAPE:
                        System.exit(1);
                }
            }
        });
        pinDialog.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent windowEvent) {
                System.exit(1);
            }
        });
        pinDialog.add(pinField);
        pinDialog.pack();
        pinDialog.setVisible(true);

        return pin;
    }

    public void setArgs(String[] args) {
        List<String> arguments = new ArrayList<String>();
        for (String params : args) {
            if (!params.startsWith("-")) {
                arguments.add(params);
            }
        }
        if (arguments.size() > 1) {
            documenttosign = Paths.get(arguments.get(0)).toAbsolutePath()
                .toString();
        }
        if (arguments.size() > 2) {
            documenttosave = Paths.get(arguments.get(1)).toAbsolutePath()
                .toString();
        }
    }

}
