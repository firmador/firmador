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

import org.slf4j.LoggerFactory;

import com.google.common.base.Throwables;

import cr.libre.firmador.gui.GUIInterface;
import cr.libre.firmador.gui.GUISwing;

public class AboutLayout extends GroupLayout {
	private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(AboutLayout.class);

    private GUIInterface swinginterface;
    private Image image = new ImageIcon(this.getClass().getClassLoader().getResource("firmador.png")).getImage();

    public AboutLayout(Container host) {
        super(host);
        String versionstr = getClass().getPackage().getSpecificationVersion();
        if(versionstr == null) versionstr = "Desarrollo";
        JLabel iconLabel = new JLabel(new ImageIcon(image.getScaledInstance(128, 128, Image.SCALE_SMOOTH)));
        JLabel descriptionLabel = new JLabel("<html><p align='center'><b>Firmador</b><br><br>" +
            "Versión " + versionstr  + "<br><br>" +
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
            	LOG.error("Error abriendo url", e);
                this.swinginterface.showError(Throwables.getRootCause(e));
            }
        }
    }

}
