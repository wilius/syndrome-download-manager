package xdman.ui.components;

import xdman.ui.res.ColorResource;
import xdman.ui.res.FontResource;
import xdman.ui.res.ImageResource;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

import static xdman.util.XDMUtils.getScaledInt;

public class SimpleCheckboxRender extends JCheckBox implements ListCellRenderer<Object> {

    /**
     *
     */
    private static final long serialVersionUID = 2719764994839662332L;

    public SimpleCheckboxRender() {
        setForeground(Color.WHITE);
        setFont(FontResource.getNormalFont());
        setOpaque(true);
        setPreferredSize(new Dimension(getScaledInt(100), getScaledInt(30)));
        setBorder(new EmptyBorder(getScaledInt(0), getScaledInt(5), 0, 0));
        setIcon(ImageResource.getIcon("unchecked.png", 16, 16));
        setSelectedIcon(ImageResource.getIcon("checked.png", 16, 16));
    }

    @Override
    public Component getListCellRendererComponent(JList<? extends Object> list, Object value, int index,
                                                  boolean isSelected, boolean cellHasFocus) {
        if (isSelected) {
            setBackground(ColorResource.getSelectionColor());
        } else {
            setBackground(ColorResource.getDarkerBgColor());
        }
        if (value != null) {
            setSelected(((BatchItem) value).selected);
            setText(value == null ? "" : value.toString());
        }
        return this;
    }

}
