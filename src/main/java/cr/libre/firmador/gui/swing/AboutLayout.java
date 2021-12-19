package cr.libre.firmador.gui.swing;

import java.awt.Container;
import java.awt.Desktop;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URI;

import javax.swing.GroupLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;

import com.google.common.base.Throwables;

import cr.libre.firmador.gui.GUIInterface;

public class AboutLayout extends GroupLayout {

	private GUIInterface swinginterface;
    private Image image = new ImageIcon(this.getClass().getClassLoader().getResource("firmador.png")).getImage();

	public AboutLayout(Container host) {
		super(host); 
        JLabel iconLabel = new JLabel(new ImageIcon(image.getScaledInstance(128, 128, Image.SCALE_SMOOTH)));
        JLabel descriptionLabel = new JLabel("<html><p align='center'><b>Firmador</b><br><br>" +
            "Versión " + getClass().getPackage().getSpecificationVersion() + "<br><br>" +
            "Herramienta para firmar documentos digitalmente.<br><br>" +
            "Los documentos firmados con esta herramienta cumplen con la<br>" +
            "Política de Formatos Oficiales de los Documentos Electrónicos<br>" +
            "Firmados Digitalmente de Costa Rica.<br><br></p></html>", JLabel.CENTER);
        JButton websiteButton = new JButton("Visitar sitio web del proyecto");
        websiteButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                openProjectWebsite();
            }
        });
				
		
        this.setAutoCreateGaps(true);
        this.setAutoCreateContainerGaps(true);
        this.setHorizontalGroup(
            this.createParallelGroup(GroupLayout.Alignment.CENTER)
                .addComponent(iconLabel)
                .addComponent(descriptionLabel)
                .addComponent(websiteButton)
        );
        this.setVerticalGroup(
            this.createSequentialGroup()
                .addComponent(iconLabel)
                .addComponent(descriptionLabel)
                .addComponent(websiteButton)
        );
	}
	public void setInterface(GUIInterface swinginterface){
		this.swinginterface = swinginterface;
	}
    private void openProjectWebsite() {
        if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
            try {
                Desktop.getDesktop().browse(new URI("https://firmador.libre.cr"));
            } catch (Exception e) {
            	this.swinginterface.showError(Throwables.getRootCause(e));
            }
        }
    }

}
