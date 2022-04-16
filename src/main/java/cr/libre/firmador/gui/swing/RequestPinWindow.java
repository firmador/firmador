package cr.libre.firmador.gui.swing;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Window;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.security.KeyStore.PasswordProtection;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;

import org.slf4j.LoggerFactory;

import cr.libre.firmador.CardSignInfo;
import cr.libre.firmador.SmardCardDetector;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.Box;
import javax.swing.ComboBoxModel;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

public class RequestPinWindow extends JDialog {

	private final JPanel contentPanel = new JPanel();
	private JPasswordField pinField = new JPasswordField(14);
	protected JPanel buttonPane;
	protected CardSignInfo card;
	private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(RequestPinWindow.class);
	protected JComboBox comboBox;
	/**
	 * Create the dialog.
	 */
	public RequestPinWindow() {
		setBounds(100, 100, 450, 300);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		this.setTitle("Ingresar PIN");
		this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		pinField.setBounds(124, 34, 312, 36);
		pinField.addHierarchyListener(new HierarchyListener() {
            public void hierarchyChanged(HierarchyEvent event) {
                final Component component = event.getComponent();
                if (component.isShowing() && (event.getChangeFlags() & HierarchyEvent.SHOWING_CHANGED) != 0) {
                    Window toplevel = SwingUtilities.getWindowAncestor(component);
                    toplevel.addWindowFocusListener(new WindowAdapter() {
                        public void windowGainedFocus(WindowEvent event) {
                            component.requestFocus();
                        }
                    });
                }
            }
        });
		pinField.grabFocus();
		contentPanel.setLayout(null);
		contentPanel.add(pinField);
		
		
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		
		comboBox = new JComboBox();
		comboBox.setBounds(121, 111, 315, 36);
		contentPanel.add(comboBox);
		
		JLabel lblNewLabel = new JLabel("Pin:");
		lblNewLabel.setBounds(12, 55, 70, 15);
		contentPanel.add(lblNewLabel);
		
		JLabel lblCertificado = new JLabel("Certificado:");
		lblCertificado.setBounds(12, 111, 91, 24);
		contentPanel.add(lblCertificado);
		{
			buttonPane = new JPanel();
			FlowLayout fl_buttonPane = new FlowLayout(FlowLayout.RIGHT);
			fl_buttonPane.setVgap(10);
			fl_buttonPane.setHgap(10);
			buttonPane.setLayout(fl_buttonPane);
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			
			JButton btnBuscarTarjetas = new JButton("Buscar tarjetas");
			btnBuscarTarjetas.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					try {
						inspectCardInfo();
					} catch (Throwable e) {
						LOG.error("Error leyendo tarjetas", e);
						e.printStackTrace();
					}
				}
			});
			buttonPane.add(btnBuscarTarjetas);
			
			Component horizontalStrut = Box.createHorizontalStrut(40);
			buttonPane.add(horizontalStrut);
			{
				JButton okButton = new JButton("OK");
				okButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						checkPassword();
					}
				});
				okButton.setActionCommand("OK");
				buttonPane.add(okButton);
				getRootPane().setDefaultButton(okButton);
			}
			{
				JButton cancelButton = new JButton("Cancel");
				cancelButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						cancel();
					}
				});
				cancelButton.setActionCommand("Cancel");
				buttonPane.add(cancelButton);
			     
				
			}
		}
		try {
			inspectCardInfo();
		} catch (Throwable e) {
			LOG.error("Error leyendo tarjetas", e);
			e.printStackTrace();
		}
	}
	
	public void checkPassword() {
		
	}
	public void cancel() {
		this.dispose();
		this.setVisible(false);
		 
	}
	
	public int showandwait() {

		this.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		this.setVisible(true);
		//Object selectedValue = buttonPane.getValue();
		return 0;
		 
	}
	
	public void inspectCardInfo() throws Throwable {
		SmardCardDetector cardd = new SmardCardDetector();
		List<CardSignInfo> cards= cardd.readListSmartCard();
		comboBox.removeAllItems();
		ComboBoxModel model = comboBox.getModel();
		 
		for(int x=0; x<cards.size(); x++ ) {
			comboBox.addItem(cards.get(x).getDisplayInfo());
		}
	}
	public PasswordProtection getPassword() {
		return new PasswordProtection(pinField.getPassword());
	}
	
	public CardSignInfo getCardInfo() {
		return this.card;
	}
}
