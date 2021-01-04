package xdman.ui.components;

import xdman.ui.res.ImageResource;

import javax.swing.*;
import java.awt.*;

public class SidePanel extends JPanel {
    /**
     *
     */
    private static final long serialVersionUID = 3821650643051584496L;
    Image imgBar;

    public SidePanel() {
        super();
        imgBar = ImageResource.getImage("bg_nav.png");
        this.setOpaque(false);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g2.drawImage(imgBar, 0, 0, this.getWidth(), this.getHeight(), this);// ,
        super.paintComponent(g2);
    }
}
