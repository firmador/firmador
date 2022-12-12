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

import java.awt.HeadlessException;
import java.awt.Image;
import java.awt.datatransfer.DataFlavor;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTabbedPane;

import org.slf4j.LoggerFactory;

import cr.libre.firmador.Settings;
import cr.libre.firmador.SettingsManager;
import cr.libre.firmador.gui.GUIInterface;
import cr.libre.firmador.gui.GUISwing;

public class SwingMainWindowFrame extends JFrame {
	private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(SwingMainWindowFrame.class);

	public static final long serialVersionUID = -7495851994719690589L;

	public JTabbedPane optionsTabbedPane;
	protected JPopupMenu menu;
	protected Settings settings;
	public GUIInterface gui;

	protected Image image = new ImageIcon(this.getClass().getClassLoader().getResource("firmador.png")).getImage();

	public void setGUIInterface(GUIInterface gui) {
		this.gui = gui;
	}

	public void startInterface() {

		JPanel pdfOptionsPanel = new JPanel();
		JPanel advancedOptionsPanel = new JPanel();

		optionsTabbedPane = new JTabbedPane();
		optionsTabbedPane.addTab("Opciones PDF", pdfOptionsPanel);
		optionsTabbedPane.setToolTipTextAt(0,
				"<html>En esta pestaña se muestran opciones específicas<br>para documentos en formato PDF.</html>");
		optionsTabbedPane.addTab("Opciones avanzadas", advancedOptionsPanel);
		optionsTabbedPane.setToolTipTextAt(1,
				"<html>En esta pestaña se muestran opciones avanzadas<br>relacionadas con la creación de la firma.</html>");

	}

	@SuppressWarnings("serial")
	public void loadGUI() {
		settings = SettingsManager.getInstance().getAndCreateSettings();
		menu = new JPopupMenu();
		JMenuItem mAll = new JMenuItem("Deseleccionar modo remoto");
		menu.add(mAll);
		mAll.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				settings.startserver = false;
				SettingsManager.getInstance().setSettings(settings, true);
				gui.showMessage("Debe reiniciar la aplicación para que los cambios tengan efecto");
			}
		});
		this.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				if (e.getButton() == MouseEvent.BUTTON3) {
					// Aparece el menú contextual
					menu.show(null, e.getX(), e.getY());
				}
			}
		});

		this.setIconImage(image.getScaledInstance(256, 256, Image.SCALE_SMOOTH));
		this.setDropTarget(new DropTarget() {
			public synchronized void drop(DropTargetDropEvent e) {
				try {
					e.acceptDrop(DnDConstants.ACTION_COPY);
					@SuppressWarnings("unchecked")
					List<File> droppedFiles = (List<File>) e.getTransferable()
							.getTransferData(DataFlavor.javaFileListFlavor);

					File[] files = new File[droppedFiles.size()];
					droppedFiles.toArray(files);
					if(files.length>1) {
						GUISwing ggui = (GUISwing) gui;
						ggui.signMultipleDocuments(files);
					}else if( files.length==1) {

						gui.loadDocument(files[0].toString());

					}


				} catch (Exception ex) {
					LOG.error("Error cerrando archivo", ex);
					ex.printStackTrace();
				}
			}
		});

		startInterface();

	}

	public SwingMainWindowFrame(String name) throws HeadlessException {
		super(name);
	}


}
