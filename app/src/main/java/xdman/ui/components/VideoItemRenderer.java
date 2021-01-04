package xdman.ui.components;

import xdman.ui.res.ColorResource;
import xdman.ui.res.FontResource;
import xdman.ui.res.ImageResource;
import xdman.videoparser.youtubedl.YoutubeDlVideo;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.TableCellRenderer;
import java.awt.*;

import static xdman.util.XDMUtils.getScaledInt;

public class VideoItemRenderer implements TableCellRenderer {
    private final JPanel panel;
    private final JPanel component;
    private final JLabel lbl;
    private final JLabel lblIcon;
    private final JComboBox<String> cmb;
    private final DefaultComboBoxModel<String> cmbModel;
    private final JLabel lblBorder;
    private final JCheckBox chk;
    private final MediaImageSource imgSource;
    private final Icon ico;

    public VideoItemRenderer(MediaImageSource imgSource) {
        component = new JPanel(new BorderLayout(getScaledInt(5), getScaledInt(5)));
        component.setBorder(new EmptyBorder(0, getScaledInt(5), getScaledInt(5), getScaledInt(5)));
        panel = new JPanel(new BorderLayout());
        lblIcon = new JLabel();
        lblIcon.setOpaque(true);
        lblIcon.setPreferredSize(new Dimension(getScaledInt(119), getScaledInt(92)));
        lblIcon.setMinimumSize(new Dimension(getScaledInt(119), getScaledInt(92)));
        lblIcon.setMaximumSize(new Dimension(getScaledInt(119), getScaledInt(92)));
        lblIcon.setHorizontalAlignment(JLabel.CENTER);

        ico = ImageResource.getIcon("videoplay.png", 94, 92);
        lblIcon.setIcon(ico);
        // lblIcon.setBorder(new EmptyBorder(12, 5, 5, 5));
        lblIcon.setVerticalAlignment(JLabel.CENTER);
        // lblIcon.setPreferredSize(new Dimension(53, 53));

        JPanel p1 = new JPanel(new BorderLayout());
        p1.setOpaque(false);
        p1.add(lblIcon);
        chk = new JCheckBox("");
        chk.setOpaque(false);
        chk.setIcon(ImageResource.getIcon("unchecked.png", 16, 16));
        chk.setSelectedIcon(ImageResource.getIcon("checked.png", 16, 16));
        p1.add(chk, BorderLayout.WEST);
        p1.setBorder(new EmptyBorder(getScaledInt(12), 0, getScaledInt(5), getScaledInt(5)));
        component.add(p1, BorderLayout.WEST);
        // component.add(lblIcon, BorderLayout.WEST);
        lbl = new JLabel();
        lbl.setVerticalAlignment(JLabel.CENTER);
        lbl.setVerticalTextPosition(JLabel.CENTER);
        lbl.setFont(FontResource.getBigFont());
        panel.add(lbl);
        cmbModel = new DefaultComboBoxModel<>();
        cmb = new JComboBox<>(cmbModel);
        cmb.setPreferredSize(new Dimension(getScaledInt(200), getScaledInt(30)));

        cmb.setOpaque(false);
        cmb.setBorder(null);
        panel.add(cmb, BorderLayout.SOUTH);
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(0, 0, getScaledInt(5), getScaledInt(5)));
        component.add(panel);
        lblBorder = new JLabel();
        lblBorder.setPreferredSize(new Dimension(getScaledInt(100), 1));
        lblBorder.setMaximumSize(new Dimension(getScaledInt(100), 1));
        lblBorder.setBackground(ColorResource.getDarkerBgColor());
        component.add(lblBorder, BorderLayout.NORTH);
        component.setOpaque(false);
        this.imgSource = imgSource;
        // component.setBackground(ColorResource.getSelectionColor());
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
                                                   int row, int column) {
        VideoItemWrapper wrapper = (VideoItemWrapper) value;
        YoutubeDlVideo obj = wrapper.videoItem;
        lbl.setText(obj.title);
        // stat.setText(obj.mediaFormats.get(obj.index) + "");
        cmbModel.removeAllElements();
        cmbModel.addElement(obj.mediaFormats.get(obj.index) + "");
        lblBorder.setOpaque(row != 0);
        // component.setBackground(isSelected ? ColorResource.getSelectionColor() :
        // ColorResource.getDarkestBgColor());
        lblIcon.setIcon(ico);
        chk.setSelected(wrapper.checked);
        if (obj.thumbnail != null) {
            if (imgSource != null) {
                ImageIcon icon = imgSource.getImage(obj.thumbnail);
                if (icon != null) {
                    lblIcon.setIcon(icon);
                } else {
                    System.out.println("null");
                }
            }
        }

        return component;
    }
}