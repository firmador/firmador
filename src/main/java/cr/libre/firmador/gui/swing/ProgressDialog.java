package cr.libre.firmador.gui.swing;

import java.awt.BorderLayout;
import java.awt.FlowLayout;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.GroupLayout;
import javax.swing.ImageIcon;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JLabel;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.SwingConstants;
import java.awt.Font;
import java.awt.Image;

import javax.swing.JProgressBar;

public class ProgressDialog extends JDialog {
	protected Image image = new ImageIcon(this.getClass().getClassLoader().getResource("firmador.png")).getImage();
	private final JPanel contentPanel = new JPanel();
	private JLabel lbNotes;
	private JLabel lbtitle;
	private JProgressBar progressBar;
	private boolean isCanceled = false;
	/**
	 * Create the dialog.
	 */
	public ProgressDialog(String title, Integer min, Integer max) {
		setBounds(100, 100, 450, 300);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		setIconImage(image);
		setTitle("Progreso de firmado");
		
		lbtitle = new JLabel(title);
		lbtitle.setFont(new Font("Dialog", Font.BOLD, 14));
		lbtitle.setHorizontalAlignment(SwingConstants.CENTER);
		
		lbNotes = new JLabel("");
		
		progressBar = new JProgressBar();
		progressBar.setMinimum(min);
		progressBar.setMaximum(max);
		GroupLayout gl_contentPanel = new GroupLayout(contentPanel);
		gl_contentPanel.setHorizontalGroup(
			gl_contentPanel.createParallelGroup(Alignment.LEADING)
				.addGroup(Alignment.TRAILING, gl_contentPanel.createSequentialGroup()
					.addGroup(gl_contentPanel.createParallelGroup(Alignment.TRAILING)
						.addComponent(lbNotes, GroupLayout.DEFAULT_SIZE, 426, Short.MAX_VALUE)
						.addComponent(lbtitle, GroupLayout.DEFAULT_SIZE, 426, Short.MAX_VALUE)
						.addComponent(progressBar, Alignment.LEADING, GroupLayout.PREFERRED_SIZE, 426, GroupLayout.PREFERRED_SIZE))
					.addContainerGap())
		);
		gl_contentPanel.setVerticalGroup(
			gl_contentPanel.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_contentPanel.createSequentialGroup()
					.addComponent(lbtitle, GroupLayout.PREFERRED_SIZE, 35, GroupLayout.PREFERRED_SIZE)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(lbNotes, GroupLayout.DEFAULT_SIZE, 120, Short.MAX_VALUE)
					.addGap(18)
					.addComponent(progressBar, GroupLayout.PREFERRED_SIZE, 39, GroupLayout.PREFERRED_SIZE)
					.addContainerGap())
		);
		contentPanel.setLayout(gl_contentPanel);
		{
			JPanel buttonPane = new JPanel();
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			{
				JButton okButton = new JButton("Cerrar");
				okButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent arg0) {
						isCanceled = true;
					}
				});
				 
				okButton.setActionCommand("OK");
				buttonPane.add(okButton);
				getRootPane().setDefaultButton(okButton);
			}
		}
	}

	public void setProgress(Integer status) {
		progressBar.setValue(status);
	}
	public void setNote(String msg) {
		lbNotes.setText(msg);
	}
	public void setHeaderTitle(String msg) {
		lbtitle.setText(msg);
	}
	public boolean isCanceled() {
		return this.isCanceled;
	}
}
