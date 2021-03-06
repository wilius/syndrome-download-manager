package xdman.ui.laf;

import xdman.ui.components.CustomButton;
import xdman.ui.components.DarkScrollBar;
import xdman.ui.res.ColorResource;
import xdman.ui.res.ImageResource;

import javax.swing.*;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicComboBoxUI;
import javax.swing.plaf.basic.BasicComboPopup;
import javax.swing.plaf.basic.ComboPopup;

public class XDMComboBoxUI extends BasicComboBoxUI {

    static XDMComboBoxUI buttonUI;

    JComponent c;

    public static ComponentUI createUI(JComponent c) {
        return new XDMComboBoxUI();
    }

    protected JButton createArrowButton() {
        JButton button = new CustomButton();
        button.setBackground(ColorResource.getDarkBgColor());
        button.setIcon(ImageResource.getIcon("down.png", 10, 10));
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setName("ComboBox.arrowButton");
        return button;
    }

    @Override
    public void installUI(JComponent c) {
        super.installUI(c);
        this.c = c;
    }

    @Override
    protected ComboPopup createPopup() {
        return new BasicComboPopup(comboBox) {
            /**
             *
             */
            private static final long serialVersionUID = -4232501153552563408L;

            @Override
            protected JScrollPane createScroller() {
                JScrollPane scroller = new JScrollPane(list, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                        JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
                scroller.setVerticalScrollBar(new DarkScrollBar(JScrollBar.VERTICAL));
                return scroller;
            }
        };
    }
}
