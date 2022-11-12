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

package cr.libre.firmador.gui.swing;

import java.awt.Component;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.security.KeyStore.PasswordProtection;
//import javax.swing.ComboBoxModel;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import java.util.List;

import org.slf4j.LoggerFactory;

import cr.libre.firmador.CardSignInfo;
import cr.libre.firmador.SmartCardDetector;

public class RequestPinWindow extends JFrame {

	private static final long serialVersionUID = -8464569433812264362L;
	private JPanel contentPane;
	private JPasswordField pinField = new JPasswordField(50);
	protected CardSignInfo card;
	private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(RequestPinWindow.class);
	protected JComboBox<String> comboBox;
	private JLabel label;
	private JLabel lblNewLabel;
	private final JLabel infotext = new JLabel("");
	protected List<CardSignInfo> cards;

	public RequestPinWindow() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 651, 262);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);


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


		lblNewLabel = new JLabel("Seleccione el certificado: ");
		comboBox = new JComboBox<String>();
		comboBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				updateSelected();
			}
		});
		comboBox.setBounds(121, 111, 315, 36);

		label = new JLabel("Ingrese su PIN:");

		JButton btnNewButton = new JButton("");
		btnNewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					inspectCardInfo();
				} catch (Throwable er) {
					LOG.error("Error leyendo tarjetas", er);
					er.printStackTrace();
				}
			}
		});
		btnNewButton.setIcon(new ImageIcon(this.getClass().getClassLoader().getResource("refresh.png")));
		btnNewButton.setToolTipText("Refrescar tarjetas");
		GroupLayout gl_contentPane = new GroupLayout(contentPane);
		gl_contentPane.setHorizontalGroup(
			gl_contentPane.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_contentPane.createSequentialGroup()
					.addGroup(gl_contentPane.createParallelGroup(Alignment.LEADING)
						.addComponent(lblNewLabel, GroupLayout.PREFERRED_SIZE, 189, GroupLayout.PREFERRED_SIZE)
						.addComponent(label))
					.addPreferredGap(ComponentPlacement.RELATED)
					.addGroup(gl_contentPane.createParallelGroup(Alignment.LEADING)
						.addGroup(Alignment.TRAILING, gl_contentPane.createSequentialGroup()
							.addComponent(comboBox, GroupLayout.PREFERRED_SIZE, 372, GroupLayout.PREFERRED_SIZE)
							.addPreferredGap(ComponentPlacement.UNRELATED)
							.addComponent(btnNewButton, GroupLayout.PREFERRED_SIZE, 44, GroupLayout.PREFERRED_SIZE))
						.addComponent(pinField, GroupLayout.DEFAULT_SIZE, 426, Short.MAX_VALUE))
					.addContainerGap())
				.addGroup(gl_contentPane.createSequentialGroup()
					.addComponent(infotext, GroupLayout.DEFAULT_SIZE, 627, Short.MAX_VALUE)
					.addContainerGap())
		);
		gl_contentPane.setVerticalGroup(
			gl_contentPane.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_contentPane.createSequentialGroup()
					.addGroup(gl_contentPane.createParallelGroup(Alignment.LEADING, false)
						.addGroup(gl_contentPane.createSequentialGroup()
							.addGap(43)
							.addGroup(gl_contentPane.createParallelGroup(Alignment.BASELINE)
								.addComponent(lblNewLabel, GroupLayout.PREFERRED_SIZE, 63, GroupLayout.PREFERRED_SIZE)
								.addComponent(comboBox, GroupLayout.PREFERRED_SIZE, 41, GroupLayout.PREFERRED_SIZE)))
						.addGroup(Alignment.TRAILING, gl_contentPane.createSequentialGroup()
							.addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
							.addComponent(btnNewButton, GroupLayout.PREFERRED_SIZE, 36, GroupLayout.PREFERRED_SIZE)
							.addGap(18)))
					.addGroup(gl_contentPane.createParallelGroup(Alignment.LEADING)
						.addGroup(gl_contentPane.createSequentialGroup()
							.addGap(15)
							.addComponent(pinField, GroupLayout.PREFERRED_SIZE, 42, GroupLayout.PREFERRED_SIZE))
						.addGroup(gl_contentPane.createSequentialGroup()
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(label, GroupLayout.PREFERRED_SIZE, 62, GroupLayout.PREFERRED_SIZE)))
					.addGap(18)
					.addComponent(infotext, GroupLayout.DEFAULT_SIZE, 28, Short.MAX_VALUE))
		);
		contentPane.setLayout(gl_contentPane);
	}


	public int showandwait() {
		try {
			inspectCardInfo();
		} catch (Throwable er) {
			LOG.error("Error leyendo tarjetas", er);
			er.printStackTrace();
		}
		pinField.grabFocus();
		int action =0;
		boolean ok=false;
		while(!ok) {
			action = JOptionPane.showConfirmDialog(null, contentPane, "Ingresar PIN", JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
			if(action==JOptionPane.OK_OPTION) {
				if (pinField.getPassword().length > 0 && this.card != null) {
					PasswordProtection password = new PasswordProtection(pinField.getPassword());
					pinField.setText(""); // However, https://stackoverflow.com/a/36828836
					this.card.setPin(password);
					password.destroy();
					ok=true;
				}else {
					JOptionPane.showMessageDialog(null, "Debe seleccionar una tarjeta y un pin", "Ocurrió un error procesando su solicitud", JOptionPane.WARNING_MESSAGE);
				}
			}else if(action==JOptionPane.CANCEL_OPTION || action==JOptionPane.CLOSED_OPTION) {
				ok=true;
			}
		}
        return action;
	}


	public void updateSelected() {
		int index = comboBox.getSelectedIndex();
		if (index>=0) {
			this.card = cards.get(index);
			infotext.setText(card.getDisplayInfo());
		}else {
			this.card=null;
			infotext.setText("Debe seleccionar al menos una tarjeta");
		}
	}

	public void inspectCardInfo() throws Throwable {
		SmartCardDetector cardd = new SmartCardDetector();
		cards= cardd.readSaveListSmartCard();
		comboBox.removeAllItems();
		//ComboBoxModel model = comboBox.getModel();

		for(int x=0; x<cards.size(); x++ ) {
			comboBox.addItem(cards.get(x).getDisplayInfo());
		}

		updateSelected();
	}
	public PasswordProtection getPassword() {
		return new PasswordProtection(pinField.getPassword());
	}

	public CardSignInfo getCardInfo() {
		return this.card;
	}
}
