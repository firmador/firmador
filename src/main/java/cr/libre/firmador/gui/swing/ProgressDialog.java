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

@SuppressWarnings("serial")
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
        contentPanel.setOpaque(false);
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
        GroupLayout glContentPane = new GroupLayout(contentPanel);
        glContentPane.setHorizontalGroup(
            glContentPane.createParallelGroup(Alignment.LEADING)
                .addGroup(Alignment.TRAILING, glContentPane.createSequentialGroup()
                    .addGroup(glContentPane.createParallelGroup(Alignment.TRAILING)
                        .addComponent(lbNotes, GroupLayout.DEFAULT_SIZE, 426, Short.MAX_VALUE)
                        .addComponent(lbtitle, GroupLayout.DEFAULT_SIZE, 426, Short.MAX_VALUE)
                        .addComponent(progressBar, Alignment.LEADING, GroupLayout.PREFERRED_SIZE, 426, GroupLayout.PREFERRED_SIZE))
                    .addContainerGap())
        );
        glContentPane.setVerticalGroup(
            glContentPane.createParallelGroup(Alignment.LEADING)
                .addGroup(glContentPane.createSequentialGroup()
                    .addComponent(lbtitle, GroupLayout.PREFERRED_SIZE, 35, GroupLayout.PREFERRED_SIZE)
                    .addPreferredGap(ComponentPlacement.RELATED)
                    .addComponent(lbNotes, GroupLayout.DEFAULT_SIZE, 120, Short.MAX_VALUE)
                    .addGap(18)
                    .addComponent(progressBar, GroupLayout.PREFERRED_SIZE, 39, GroupLayout.PREFERRED_SIZE)
                    .addContainerGap())
        );
        contentPanel.setLayout(glContentPane);
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
                okButton.setOpaque(false);
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
