/* Firmador is a program to sign documents using AdES standards.

Copyright (C) 2018, 2022 Firmador authors.

This file is part of Firmador.

Firmador is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

Firmador is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with Firmador.  If not, see <http://www.gnu.org/licenses/>.  */

package cr.libre.firmador.gui;

import java.awt.Image;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.SpinnerNumberModel;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.slf4j.LoggerFactory;
import com.apple.eawt.Application;
import com.google.common.base.Throwables;

import cr.libre.firmador.CardSignInfo;
import cr.libre.firmador.FirmadorCAdES;
import cr.libre.firmador.FirmadorOpenDocument;
import cr.libre.firmador.FirmadorPAdES;
import cr.libre.firmador.FirmadorXAdES;
import cr.libre.firmador.Report;
import cr.libre.firmador.Settings;
import cr.libre.firmador.SettingsManager;
import cr.libre.firmador.Validator;
import cr.libre.firmador.gui.swing.CopyableJLabel;
import cr.libre.firmador.gui.swing.ExecutorWorkerInterface;
import cr.libre.firmador.gui.swing.LogHandler;
import cr.libre.firmador.gui.swing.LogginFrame;
import cr.libre.firmador.gui.swing.RequestPinWindow;
import cr.libre.firmador.gui.swing.SignPanel;
import cr.libre.firmador.gui.swing.SwingMainWindowFrame;
import cr.libre.firmador.gui.swing.ValidatePanel;
import cr.libre.firmador.plugins.PluginManager;
import eu.europa.esig.dss.model.DSSDocument;
import eu.europa.esig.dss.model.MimeType;

public class BaseSwing {
    protected Settings settings;
	private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(BaseSwing.class);
    protected Image image = new ImageIcon(this.getClass().getClassLoader().getResource("firmador.png")).getImage();
    protected DSSDocument toSignDocument;
    protected DSSDocument signedDocument;
    protected SwingMainWindowFrame mainFrame;
    protected PDFRenderer renderer;
    protected SignPanel signPanel;
    protected ValidatePanel validatePanel;
    protected GUIInterface gui;
	protected ExecutorWorkerInterface worker = null;
	private JScrollPane logginPane;
	private  Integer tabnumber=4;




    public Integer getTabnumber() {
		return tabnumber;
	}

	public void setTabnumber(Integer tabnumber) {
		this.tabnumber = tabnumber;
	}

	public SwingMainWindowFrame getMainFrame() {
		return mainFrame;
	}

	public void setMainFrame(SwingMainWindowFrame mainFrame) {
		this.mainFrame = mainFrame;
	}

	public void loadGUI() {

		try {
			Application.getApplication().setDockIconImage(image);
		} catch (RuntimeException | IllegalAccessError e) {
			/* macOS dock icon support specific code. */ }
		try {
			try {
				UIManager.setLookAndFeel("com.sun.java.swing.plaf.gtk.GTKLookAndFeel");
			} catch (UnsupportedLookAndFeelException | ClassNotFoundException e) {
				UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			}
		} catch (Exception e) {
			LOG.error("Error cargando GUI", e);
			e.printStackTrace();
			this.showError(Throwables.getRootCause(e));
		}
    	settings = SettingsManager.getInstance().get_and_create_settings();
		LogginFrame loggingFrame = new LogginFrame();
		LogHandler handler = LogHandler.getInstance();
		handler.setWritter(loggingFrame);
		handler.register();
		logginPane = loggingFrame.getLogScrollPane();
	}

	protected void showLogs(JTabbedPane frameTabbedPane) {
		frameTabbedPane.addTab("Bitácoras", logginPane);
		frameTabbedPane.setToolTipTextAt(this.tabnumber,
				"<html>En esta estaña se muestra las bitácoras de ejecución<br> de este programa.</html>");
	}
	protected void hideLogs(JTabbedPane frameTabbedPane) {
		frameTabbedPane.remove(logginPane);
	}

	public ByteArrayOutputStream extendDocument(DSSDocument toExtendDocument, boolean asbytes, String fileName ) {
            if(toExtendDocument == null) return null;
			DSSDocument extendedDocument = null;
            ByteArrayOutputStream outdoc = null;
            MimeType mimeType = toExtendDocument.getMimeType();
            if (mimeType == MimeType.PDF) {
                FirmadorPAdES firmador = new FirmadorPAdES(gui);
                extendedDocument = firmador.extend(toExtendDocument);
            } else if (mimeType == MimeType.ODG || mimeType == MimeType.ODP || mimeType == MimeType.ODS || mimeType == MimeType.ODT) {
                FirmadorOpenDocument firmador = new FirmadorOpenDocument(gui);
                extendedDocument = firmador.extend(toExtendDocument);
            } else if (mimeType == MimeType.XML) {
                FirmadorXAdES firmador = new FirmadorXAdES(gui);
                extendedDocument = firmador.extend(toExtendDocument);
            }else {
            	FirmadorCAdES firmador = new FirmadorCAdES(gui);
            	extendedDocument = firmador.extend(toExtendDocument);
            }
            if (extendedDocument != null) {
            	if(asbytes) {
            		outdoc = new ByteArrayOutputStream();
            		try {
						extendedDocument.writeTo(outdoc);
					} catch (IOException e) {
						LOG.error("Error extendiendo documento", e);
						e.printStackTrace();
						showError(Throwables.getRootCause(e));
					}
            	}else {
            		if(fileName==null) {
            			fileName = gui.getPathToSaveExtended("");
            		}
	                if (fileName != null) {
	                    try {
	                        extendedDocument.save(fileName);
	                        showMessage("Documento guardado satisfactoriamente en<br>" + fileName);
	                        gui.loadDocument(fileName);

	                    } catch (IOException e) {
	            			LOG.error("Error guardando extendido", e);
	                        e.printStackTrace();
	                        showError(Throwables.getRootCause(e));
	                    }
	                }
            	}
            }
            return outdoc;
        }

    public Boolean validateDocument(Validator validator){
    	Boolean ok=false;
    	if (validator.isSigned()) {
            validatePanel.extendButton.setEnabled(true);

            gui.displayFunctionality("validator");
            ok=true;
        } else {
        	validatePanel.reportLabel.setText("");
        	validatePanel.extendButton.setEnabled(false);
        	gui.displayFunctionality("sign");
        	return false;
        }

    	try {
            Report report = new Report(validator.getReports());
            validatePanel.reportLabel.setText(report.getReport());
        } catch (Exception e) {
			LOG.error("Validando documento", e);
            e.printStackTrace();
            validatePanel.reportLabel.setText("Error al generar reporte.<br>" +
                "Agradeceríamos que informara sobre este inconveniente<br>" +
                "a los desarrolladores de la aplicación para repararlo.");
            ok=false;
        }
    	return ok;
    }



    public void validateDocument(String fileName) {
        Validator validator = null;
        try {
            validator = new Validator(fileName);
            if (validator != null) {
            	validateDocument(validator);
            }

        } catch (UnsupportedOperationException e) {
			LOG.error("Error documento inválido "+fileName, e);
			showError(e);
		} catch (Exception e) {
			LOG.error("Error validando documento desde archivo "+fileName, e);
            e.printStackTrace();
            validatePanel.reportLabel.setText("Error al validar documento.<br>" +
                "Agradeceríamos que informara sobre este inconveniente<br>" +
                "a los desarrolladores de la aplicación para repararlo.");
            validatePanel.reportLabel.setText("");
            validatePanel.extendButton.setEnabled(false);
            gui.displayFunctionality("sign");
        }
    }

    public void loadDocumentPDF(PDDocument doc) throws IOException {
    	signPanel.getSignButton().setEnabled(true);
    	signPanel.docHideButtons();
    	int pages = doc.getNumberOfPages();
        renderer = signPanel.getRender(doc);
        if (pages > 0) {
            SpinnerNumberModel model = ((SpinnerNumberModel)signPanel.getPageSpinner().getModel());
            model.setMinimum(1);
            model.setMaximum(pages);
            if (settings.pagenumber <= pages && settings.pagenumber > 0) {
            	signPanel.getPageSpinner().setValue(settings.pagenumber);
            } else {
            	signPanel.getPageSpinner().setValue(1);
            }
            signPanel.paintPDFViewer();
        }
        signPanel.showSignButtons();
    }

    public void loadDocument(MimeType mimeType, PDDocument doc) {
    	signPanel.setDoc(doc);
    	signPanel.getSignButton().setEnabled(true);
        try {
            signPanel.docHideButtons();
            if (mimeType == MimeType.PDF) {
            	loadDocumentPDF(doc);
            } else if (mimeType == MimeType.XML || mimeType == MimeType.ODG || mimeType == MimeType.ODP || mimeType == MimeType.ODS || mimeType == MimeType.ODT) {
            } else {
            	signPanel.shownonPDFButtons();
            }
            mainFrame.pack();
            mainFrame.setMinimumSize(mainFrame.getSize());
        } catch (Exception e) {
			LOG.error("Error cargando Documento con mimeType", e);
            e.printStackTrace();
            gui.showError(Throwables.getRootCause(e));
        }

    }


    protected void signDocument(CardSignInfo card, Boolean visibleSignature) {
    	signedDocument = null;
    	MimeType mimeType = toSignDocument.getMimeType();
        if (mimeType == MimeType.PDF) {
            FirmadorPAdES firmador = new FirmadorPAdES(gui);
            firmador.setVisibleSignature(visibleSignature);
            firmador.addVisibleSignature((int)signPanel.getPageSpinner().getValue(),
            		signPanel.calculateSignatureRectangle());
            signedDocument = firmador.sign(toSignDocument, card, signPanel.getReasonField().getText(), signPanel.getLocationField().getText(),
            		signPanel.getContactInfoField().getText(), System.getProperty("jnlp.signatureImage"), Boolean.getBoolean("jnlp.hideSignatureAdvice"));
        } else if (mimeType == MimeType.ODG || mimeType == MimeType.ODP || mimeType == MimeType.ODS || mimeType == MimeType.ODT) {
            FirmadorOpenDocument firmador = new FirmadorOpenDocument(gui);
            signedDocument = firmador.sign(toSignDocument, card);
        } else if (mimeType == MimeType.XML || signPanel.getAdESFormatButtonGroup().getSelection().getActionCommand().equals("XAdES")) {
            FirmadorXAdES firmador = new FirmadorXAdES(gui);
            signedDocument = firmador.sign(toSignDocument, card);
        } else {
            FirmadorCAdES firmador = new FirmadorCAdES(gui);
            signedDocument = firmador.sign(toSignDocument, card);

        }
    }

    protected void signDocument(CardSignInfo card,
    		 Boolean visibleSignature, Boolean destroyPin){

        if (card.isValid()) {
        	gui.nextStep("Inicio del proceso de firmado");
        	signDocument(card,  visibleSignature);
            if(destroyPin) {
            	gui.nextStep("Destroyendo el pin");
	            card.destroyPin();
            }
        }
    }

    public void showMessage(String message) {
        JOptionPane.showMessageDialog(null, new CopyableJLabel(message), "Mensaje de Firmador", JOptionPane.INFORMATION_MESSAGE);
    }
    public void showError(Throwable error) {
    	showError(error, false);
    }

    public void showError(Throwable error, boolean closed) {
        error.printStackTrace();
        String message = error.getLocalizedMessage();
        int messageType = JOptionPane.ERROR_MESSAGE;
        String className = error.getClass().getName();
        switch (className) {
            case "java.lang.NoSuchMethodError":
                message = "Esta aplicación es actualmente incompatible con versiones superiores a Java 8<br>" +
                    "cuando se ejecuta desde Java Web Start.<br>" +
                    "Este inconveniente se corregirá en próximas versiones. Disculpe las molestias.";
                break;
            case "java.security.ProviderException":
                message = "No se ha encontrado ninguna dispositivo de firma.<br>" +
                    "Asegúrese de que la tarjeta y el lector están conectados de forma correcta<br>" +
                    "y de que los controladores están instalados y ha reiniciado el sistema tras su instalación.";
                break;
            case "java.security.NoSuchAlgorithmException":
                message = "No se ha encontrado ninguna tarjeta conectada.<br>" +
                    "Asegúrese de que la tarjeta y el lector están conectados de forma correcta.";
                break;
            case "sun.security.pkcs11.wrapper.PKCS11Exception":
                switch (message) {
                case "CKR_GENERAL_ERROR":
                    message = "No se ha podido contactar con el servicio del lector de tarjetas.<br>" +
                        "¿Está correctamente instalado o configurado?";
                    break;
                case "CKR_SLOT_ID_INVALID":
                    message = "No se ha podido encontrar ningún lector conectado o el controlador del lector no está instalado.";
                    break;
                case "CKR_PIN_INCORRECT":
                    messageType = JOptionPane.WARNING_MESSAGE;
                    message = "¡PIN INCORRECTO!<br><br>" +
                        "ADVERTENCIA: si se ingresa un PIN incorrecto varias veces sin acertar,<br>" +
                        "el dispositivo de firma se bloqueará.";
                    break;
                case "CKR_PIN_LOCKED":
                    message = "PIN BLOQUEADO<br><br>" +
                        "Lo sentimos, el dispositivo de firma no se puede utilizar porque está bloqueado.<br>" +
                        "Contacte con su proveedor para desbloquearlo.";
                    break;
                default:
                    message = "Error: " + className + "<br>" +
                        "Detalle: " + message + "<br>" +
                        "Agradecemos que comunique este mensaje de error a los autores del programa<br>" +
                        "para detallar mejor el posible motivo de este error en próximas versiones.";
                    break;
                }
                break;
            case "java.io.IOException":
                if (message.contains("asepkcs") || message.contains("libASEP11")) {
                    message = "No se ha encontrado la librería de Firma Digital en el sistema.<br>" +
                        "¿Están instalados los controladores?";
                }
                break;
            default:
                message = "Error: " + className + "<br>" +
                    "Detalle: " + message + "<br>" +
                    "Agradecemos que comunique este mensaje de error a los autores del programa<br>" +
                    "para detallar mejor el posible motivo de este error en próximas versiones.";
                break;
        }

        JOptionPane.showMessageDialog(null, new CopyableJLabel(message), "Mensaje de Firmador", messageType);
        if(closed) if (messageType == JOptionPane.ERROR_MESSAGE) System.exit(0);
    }

    public CardSignInfo getPin() {
    	RequestPinWindow pinrequestwindow = new RequestPinWindow();
        int action = pinrequestwindow.showandwait();

        if (action == 0) return pinrequestwindow.getCardInfo();
        else return null;
    }

	public void setPluginManager(PluginManager pluginManager) {
		pluginManager.start_loggin();
		mainFrame.addWindowListener(new WindowAdapter() {
				@Override
				public void windowClosing(WindowEvent arg0) {
					pluginManager.stop();

				}
	        });

	}
	public void nextStep(String msg) {
		if(worker != null) worker.nextStep(msg);
	}
}
