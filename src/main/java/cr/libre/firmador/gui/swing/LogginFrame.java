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

import javax.swing.GroupLayout;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;

public class LogginFrame extends ScrollableJPanel {
	private static final long serialVersionUID = 2015584665968200047L;
	private JTextArea logtext;
	public LogginFrame() {
		super();
		logtext = new JTextArea();
		GroupLayout validateLayout = new GroupLayout(this);
		validateLayout.setAutoCreateGaps(true);
		validateLayout.setAutoCreateContainerGaps(true);
		validateLayout.setHorizontalGroup(
				validateLayout.createParallelGroup().addComponent(logtext));
		validateLayout.setVerticalGroup(
				validateLayout.createSequentialGroup().addComponent(logtext));
		this.setLayout(validateLayout);
		this.setOpaque(false);
		logtext.setOpaque(false);
	}
	public void showInfo(String message) {
		this.logtext.append("\n"+message);

	}

	public JScrollPane getLogScrollPane() {
		JScrollPane logScrollPane = new JScrollPane();
		logScrollPane.setPreferredSize(logScrollPane.getPreferredSize());
		logScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		logScrollPane.setBorder(null);
		logScrollPane.setViewportView(this);
		logScrollPane.setOpaque(false);
		logScrollPane.getViewport().setOpaque(false);
		logScrollPane.setVisible(true);
		return logScrollPane;
	}

}
