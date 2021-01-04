package xdman.ui.components;

import xdman.ui.res.StringResource;

import javax.swing.*;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.text.JTextComponent;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import static xdman.os.OperationSystem.OS;

public class PopupAdapter extends MouseAdapter implements ActionListener {
    private JTextComponent txt;
    private JPopupMenu popup;

    public PopupAdapter(JTextComponent txt) {
        init();
        txt.addMouseListener(this);
        this.txt = txt;
    }

    public void uninstall() {
        this.txt.removeMouseListener(this);
        this.txt = null;
    }

    private void init() {
        popup = new JPopupMenu();
        popup.addPopupMenuListener(new PopupMenuListener() {

            @Override
            public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
            }

            @Override
            public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
            }

            @Override
            public void popupMenuCanceled(PopupMenuEvent e) {
            }
        });
        JMenuItem menuCut = new JMenuItem(StringResource.get("CTX_CUT"));
        menuCut.addActionListener(this);
        menuCut.setName("MENU_CUT");
        JMenuItem menuCopy = new JMenuItem(StringResource.get("CTX_COPY"));
        menuCopy.addActionListener(this);
        menuCopy.setName("MENU_COPY");
        JMenuItem menuPaste = new JMenuItem(StringResource.get("CTX_PASTE"));
        menuPaste.setName("MENU_PASTE");
        menuPaste.addActionListener(this);
        JMenuItem menuSelect = new JMenuItem(StringResource.get("CTX_SELECT_ALL"));
        menuSelect.addActionListener(this);
        menuSelect.setName("MENU_SELECT_ALL");
        popup.add(menuCut);
        popup.add(menuCopy);
        popup.add(menuPaste);
        popup.add(menuSelect);
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON3 ||
                SwingUtilities.isRightMouseButton(e) ||
                OS.isPopupTrigger(e)) {

            if (e.getSource() instanceof JTextComponent) {
                popup.show(txt, e.getX(), e.getY());
            }
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (txt == null)
            return;
        System.out.println(txt);
        String name = ((JComponent) e.getSource()).getName();
        if ("MENU_CUT".equals(name)) {
            txt.cut();
        } else if ("MENU_COPY".equals(name)) {
            txt.copy();
        } else if ("MENU_SELECT_ALL".equals(name)) {
            txt.selectAll();
        } else if ("MENU_PASTE".equals(name)) {
            txt.paste();
        }
    }
}
