package app.firmador.gui.swing;

import java.awt.Dimension;
import javax.swing.JPanel;
import javax.swing.Scrollable;

class ScrollableJPanel extends JPanel implements Scrollable {

    public Dimension getPreferredScrollableViewportSize() {
        return this.getPreferredSize();
    }

    public int getScrollableUnitIncrement(Rectangle visibleRect,
        int orientation, int direction) {
        return 50;
    }

    public int getScrollableBlockIncrement(Rectangle visibleRect,
        int orientation, int direction) {
        return 80;
    }

    public boolean getScrollableTracksViewportWidth() {
        return true;
    }

    public boolean getScrollableTracksViewportHeight() {
        return false;
    }

}
