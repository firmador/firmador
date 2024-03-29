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

import java.awt.Dimension;
import java.awt.Rectangle;
import javax.swing.JPanel;
import javax.swing.Scrollable;

@SuppressWarnings("serial")
public class ScrollableJPanel extends JPanel implements Scrollable {
    private boolean widthTrackScrollable = true;
    private boolean heigthTrackScrollable = false;

    public ScrollableJPanel() {
        super();
        this.setOpaque(false);
    }

    public ScrollableJPanel(boolean widthTrackScrollable, boolean heigthTrackScrollable) {
        super();
        this.widthTrackScrollable = widthTrackScrollable;
        this.heigthTrackScrollable = heigthTrackScrollable;
        this.setOpaque(false);
    }

    public Dimension getPreferredScrollableViewportSize() {
        return this.getPreferredSize();
    }

    public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
        return 50;
    }

    public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
        return 80;
    }

    public boolean getScrollableTracksViewportWidth() {
        return this.widthTrackScrollable;
    }

    public boolean getScrollableTracksViewportHeight() {
        return this.heigthTrackScrollable;
    }
}
