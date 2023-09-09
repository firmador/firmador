/* Firmador is a program to sign documents using AdES standards.

Copyright (C) Firmador authors.

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

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FileDialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Collections;
import java.util.List;
import javax.swing.DefaultListModel;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;

public class Pkcs12ConfigPanel extends JPanel {
    private static final long serialVersionUID = -2813831810835064245L;
    private JList<String> pk12list;
    private DefaultListModel<String> pk12Model;

    public List<String> getList(){
        return Collections.list(pk12Model.elements());
    }

    public void setList(List<String> data) {
        for (String element : data) {
            pk12Model.addElement(element);
        }
    }

    public String getFilePath() {
        String dev = null;

        FileDialog loadDialog = new FileDialog((JFrame) SwingUtilities.getAncestorOfClass(JFrame.class, this), "Seleccionar un archivo");
        loadDialog.setMultipleMode(false);
        loadDialog.setLocationRelativeTo(null);
        loadDialog.setVisible(true);
        loadDialog.dispose();

        File[] files = loadDialog.getFiles();

        if(files.length>=1) {
            dev=files[0].toString();
        }
        return dev;

    }

    public Pkcs12ConfigPanel() {

        setPreferredSize(new Dimension(450, 200));
        setLayout(new BorderLayout(0, 0));
        Border margin = new EmptyBorder(20, 10,20,10);
        setBorder(margin);
        setOpaque(false);

        pk12Model = new DefaultListModel<String>();
        pk12list  = new JList<String>(pk12Model);

        JLabel ltitle = new JLabel("Archivos PKCS12");

        ltitle.setHorizontalAlignment(SwingConstants.CENTER);
        add(ltitle, BorderLayout.NORTH);

        JPanel panel = new JPanel();
        panel.setOpaque(false);
        add(panel, BorderLayout.EAST);

        JButton addbtn = new JButton("+");
        addbtn.setOpaque(false);

        JButton rmbtn = new JButton("-");
        rmbtn.setOpaque(false);
        GroupLayout glPanel = new GroupLayout(panel);
        glPanel.setHorizontalGroup(
            glPanel.createParallelGroup(Alignment.LEADING)
                .addGroup(glPanel.createSequentialGroup()
                    .addGap(5)
                    .addGroup(glPanel.createParallelGroup(Alignment.LEADING)
                        .addComponent(rmbtn)
                        .addComponent(addbtn))
                    .addGap(44))
        );
        glPanel.setVerticalGroup(
            glPanel.createParallelGroup(Alignment.LEADING)
                .addGroup(glPanel.createSequentialGroup()
                    .addGap(5)
                    .addComponent(addbtn)
                    .addPreferredGap(ComponentPlacement.RELATED)
                    .addComponent(rmbtn)
                    .addContainerGap(242, Short.MAX_VALUE))
        );
        panel.setLayout(glPanel);

        JScrollPane scrollPane = new JScrollPane();
        scrollPane.setViewportView(pk12list);
        pk12list.setLayoutOrientation(JList.VERTICAL);
        pk12list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        add(scrollPane, BorderLayout.CENTER);

        rmbtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                List<String> selectedValues = pk12list.getSelectedValuesList();
                for(String item: selectedValues) {
                    pk12Model.removeElement(item);

                }
            }
        });
        addbtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                String path = getFilePath();
                if(path != null && !path.isEmpty() ) {
                    //pk12text.setText(path);
                    pk12Model.addElement(path);
                }
            }
        });

    }

}
