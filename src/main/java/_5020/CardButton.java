package _5020;

import javax.swing.*;
import java.awt.*;

// A JButton subclass representing a single card with the front and back icons, and a stable id used for matching.
public class CardButton extends JButton {
    private final ImageIcon front;
    private final Icon backIcon;
    private final Object id; // stable identifier for matching
    private boolean matched = false;

    // Backwards-compatible constructors
    public CardButton(ImageIcon front) {
        this(front, UIManager.getIcon("FileView.fileIcon"), null);
    }

    public CardButton(ImageIcon front, Icon backIcon) {
        this(front, backIcon, null);
    }

    // New constructor with id
    public CardButton(ImageIcon front, Icon backIcon, Object id) {
        this.front = front;
        this.backIcon = backIcon != null ? backIcon : UIManager.getIcon("FileView.fileIcon");
        this.id = id;
        setIcon(this.backIcon);
        setHorizontalAlignment(SwingConstants.CENTER);
        setVerticalAlignment(SwingConstants.CENTER);
    }

    // getters
    public ImageIcon getFrontIcon() { return front; }
    public Icon getBackIcon() { return backIcon; }
    public Object getId() { return id; }

    // matched state management
    public boolean isMatched() { return matched; }
    public void setMatched(boolean m) {
        matched = m;
        if (m) {
            setEnabled(false); // disable button when matched
            // keep front shown when matched
            if (front != null) setIcon(front);
        }
    }
    // methods to show the front or back side of the cards (flipping functionality)
    public void showFront() { setIcon(front); }
    public void showBack() { if (!matched) setIcon(backIcon); }
}
