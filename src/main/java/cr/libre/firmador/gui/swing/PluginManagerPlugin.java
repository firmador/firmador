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
import java.awt.Color;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.border.LineBorder;

import cr.libre.firmador.Settings;
import cr.libre.firmador.SettingsManager;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.awt.event.ActionEvent;

public class PluginManagerPlugin extends JPanel {
    private static final long serialVersionUID = -834388170590651815L;
    private JList<String> listActive;
    private JList<String> listAvailable;
    private DefaultListModel<String> activeModel;
    private DefaultListModel<String> availableModel;

    private JButton addactive;
    private JButton rmactive;
    private Settings settings;


    public void loadPlugins(Settings settings) {
        if(settings==null)  settings=this.settings;

        activeModel.clear();
        availableModel.clear();
        for(String item: settings.activePlugins){
            activeModel.addElement(item);
        }
        for(String item: settings.availablePlugins) {
            if(!activeModel.contains(item)) {
                availableModel.addElement(item);
            }
        }
    }

    public List<String> getActivePlugin(){
        List<String> active= new ArrayList<String>();
        for(int i = 0; i< activeModel.getSize();i++){
            active.add(activeModel.getElementAt(i));
        }
        return active;
    }

    public PluginManagerPlugin() {
        super(new BorderLayout(2, 2));
        //this.setBorder(new LineBorder(new Color(0, 0, 0)));

        settings = SettingsManager.getInstance().getAndCreateSettings();
        activeModel = new DefaultListModel<String>();
        availableModel = new DefaultListModel<String>();
        loadPlugins(settings);

        JPanel mainpavailable = new JPanel(new BorderLayout(2, 2));
        JPanel pavailable = new JPanel(new BorderLayout(2, 2));
        JLabel lavailable = new JLabel("Plugins disponibles");
        lavailable.setAlignmentX(CENTER_ALIGNMENT);
        pavailable.add(lavailable, BorderLayout.CENTER);
        listAvailable = new JList<String>(availableModel);
        listAvailable.setBorder(new LineBorder(new Color(0, 0, 0)));
        addactive = new JButton(">>");
        pavailable.add(addactive, BorderLayout.LINE_END);
        mainpavailable.add(pavailable, BorderLayout.NORTH);
        mainpavailable.add(listAvailable, BorderLayout.CENTER);

        JPanel mainpactive = new JPanel(new BorderLayout(2, 2));
        JPanel pactive = new JPanel(new BorderLayout(2, 2));
        JLabel lactive = new JLabel("   Plugins activos");
        pactive.add(lactive, BorderLayout.CENTER);
        rmactive = new JButton("<<");

        pactive.add(rmactive, BorderLayout.LINE_START);
        listActive = new JList<String>(activeModel);
        listActive.setBorder(new LineBorder(new Color(0, 0, 0)));
        mainpactive.add(pactive, BorderLayout.NORTH);
        mainpactive.add(listActive, BorderLayout.CENTER);

        JSplitPane sp = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, mainpavailable, mainpactive);
        sp.setOneTouchExpandable(true);
        sp.setResizeWeight(0.5);
        this.add(sp, BorderLayout.CENTER);
        this.setOpaque(false);
        rmactive.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                List<String> selectedValues = listActive.getSelectedValuesList();
                for(String item: selectedValues) {
                    activeModel.removeElement(item);
                    availableModel.addElement(item);
                }
            }
        });
        addactive.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                List<String> selectedValues = listAvailable.getSelectedValuesList();
                for(String item: selectedValues) {
                    availableModel.removeElement(item);
                    activeModel.addElement(item);
                }
            }
        });

    }
}
