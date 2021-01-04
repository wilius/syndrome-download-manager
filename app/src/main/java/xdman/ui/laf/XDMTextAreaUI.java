package xdman.ui.laf;

import xdman.ui.components.PopupAdapter;

import javax.swing.*;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicTextAreaUI;
import javax.swing.text.JTextComponent;

public class XDMTextAreaUI extends BasicTextAreaUI {
    PopupAdapter popupAdapter;

    public static ComponentUI createUI(JComponent c) {
        return new XDMTextAreaUI();
    }

    @Override
    public void installUI(JComponent c) {
        super.installUI(c);
        popupAdapter = new PopupAdapter((JTextComponent) c);
    }

    @Override
    public void uninstallUI(JComponent c) {
        super.uninstallUI(c);
        this.popupAdapter.uninstall();
    }
}
