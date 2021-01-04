package xdman.ui.components;

import xdman.ui.res.ColorResource;
import xdman.ui.res.FontResource;
import xdman.ui.res.ImageResource;
import xdman.ui.res.StringResource;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.MouseInputAdapter;
import java.awt.*;
import java.awt.event.*;
import java.util.Vector;

import static xdman.util.XDMUtils.getScaledInt;

public class MessageBox extends JPanel implements ActionListener {
    public static final int OK = 10, YES = 20, NO = 30;
    public static final int OK_OPTION = 10, YES_NO_OPTION = 20;
    /**
     *
     */
    private static final long serialVersionUID = 8432219586855665330L;
    private static MessageBox msgBox;
    private static MsgBoxFocusTraversalPolicy focusPolicy;
    private JTextArea txtMessage;
    private JLabel lblTitle;
    private JCheckBox chkOption;
    private int diffx, diffy;
    private XDMFrame parent;
    private CustomButton cbBtnOk, cbBtnYes, cbBtnNo;
    private JPanel panel2, panel3;
    private int res;
    private int defaultButton;
    private JScrollPane jsp;

    private MessageBox() {
        setLayout(null);
        MouseInputAdapter ma = new MouseInputAdapter() {
        };
        addMouseListener(ma);
        addMouseMotionListener(ma);
        init();
    }

    public static int show(XDMFrame parent, String msg) {
        return show(parent, "XDM", msg, MessageBox.OK_OPTION, MessageBox.OK);
    }

    public static int show(XDMFrame parent, String title, String msg) {
        return show(parent, title, msg, MessageBox.OK_OPTION, MessageBox.OK);
    }

    public static int show(XDMFrame parent, String title, String msg, int buttons, int defaultButton) {
        return show(parent, title, msg, buttons, defaultButton, null);
    }

    public static int show(XDMFrame parent, String title, String msg, int buttons, int defaultButton,
                           String checkText) {
        if (msgBox == null) {
            msgBox = new MessageBox();
        }

        msgBox.parent = parent;
        msgBox.lblTitle.setText(title);
        msgBox.txtMessage.setText(msg);
        msgBox.setLocation((parent.getWidth() - getScaledInt(350)) / 2, (parent.getHeight() - getScaledInt(210)) / 2);

        if (buttons == OK_OPTION) {
            msgBox.panel2.setVisible(false);
            msgBox.panel3.setVisible(true);
        } else {
            msgBox.panel2.setVisible(true);
            msgBox.panel3.setVisible(false);
        }
        msgBox.defaultButton = defaultButton;
        if (checkText != null) {
            msgBox.chkOption.setSelected(false);
            msgBox.chkOption.setVisible(true);
            msgBox.jsp.setBounds(0, getScaledInt(54), getScaledInt(350), getScaledInt(106) - getScaledInt(30));
        } else {
            msgBox.chkOption.setSelected(false);
            msgBox.chkOption.setVisible(false);
            msgBox.jsp.setBounds(0, getScaledInt(54), getScaledInt(350), getScaledInt(106));
        }
        parent.showModal(msgBox);
        return msgBox.res;
    }

    public static MsgBoxFocusTraversalPolicy getFocusPolicy() {
        return focusPolicy;
    }

    public static void setFocusPolicy(MsgBoxFocusTraversalPolicy fp) {
        focusPolicy = fp;
    }

    public static boolean isChecked() {
        return msgBox != null && msgBox.chkOption.isSelected();
    }

    public void selectDefaultButton() {
        if (defaultButton == YES) {
            msgBox.cbBtnYes.requestFocusInWindow();
        } else if (defaultButton == NO) {
            msgBox.cbBtnNo.requestFocusInWindow();
        } else if (defaultButton == OK) {
            msgBox.cbBtnOk.requestFocusInWindow();
        }
    }

    private void init() {
        lblTitle = new JLabel();
        txtMessage = new JTextArea();
        txtMessage.setWrapStyleWord(true);
        txtMessage.setLineWrap(true);
        txtMessage.setBackground(ColorResource.getDarkerBgColor());
        txtMessage.setForeground(Color.WHITE);
        txtMessage.setBorder(
                new EmptyBorder(new Insets(getScaledInt(10), getScaledInt(10), getScaledInt(10), getScaledInt(30))));
        txtMessage.setEditable(false);

        setBackground(ColorResource.getDarkerBgColor());
        setBounds(0, 0, getScaledInt(350), getScaledInt(210));

        lblTitle.setBounds(getScaledInt(25), getScaledInt(15), getScaledInt(300), getScaledInt(30));
        lblTitle.setFont(FontResource.getItemFont());
        lblTitle.setForeground(ColorResource.getSelectionColor());

        JLabel lineLbl = new JLabel();
        lineLbl.setBackground(ColorResource.getSelectionColor());
        lineLbl.setBounds(0, getScaledInt(52), getScaledInt(350), getScaledInt(2));
        lineLbl.setOpaque(true);

        jsp = new JScrollPane(txtMessage);

        JScrollBar sc1 = new DarkScrollBar(JScrollBar.VERTICAL);
        // sc1.putClientProperty("Scrollbar.darkMode", new Integer(1));

        jsp.setVerticalScrollBar(sc1);

        jsp.setBounds(0, getScaledInt(54), getScaledInt(350), getScaledInt(106));
        jsp.setBorder(null);

        chkOption = new JCheckBox(StringResource.get("LBL_DELETE_FILE"));
        chkOption.setIcon(ImageResource.getIcon("unchecked.png", 16, 16));
        chkOption.setSelectedIcon(ImageResource.getIcon("checked.png", 16, 16));
        chkOption.setBounds(getScaledInt(15), getScaledInt(130), getScaledInt(320), getScaledInt(30));
        chkOption.setOpaque(false);
        chkOption.setFocusPainted(false);
        chkOption.setForeground(Color.WHITE);

        panel2 = new JPanel(null);
        panel2.setBounds(0, getScaledInt(160), getScaledInt(350), getScaledInt(50));
        panel2.setBackground(ColorResource.getDarkBgColor());

        panel3 = new JPanel(null);
        panel3.setBounds(0, getScaledInt(160), getScaledInt(350), getScaledInt(50));
        panel3.setBackground(ColorResource.getDarkBgColor());

        cbBtnOk = new CustomButton(StringResource.get("MB_OK"));
        cbBtnYes = new CustomButton(StringResource.get("MB_YES"));
        cbBtnNo = new CustomButton(StringResource.get("MB_NO"));

        cbBtnOk.setBounds(0, 1, getScaledInt(350), getScaledInt(50));
        cbBtnOk.setName("MB_OK");
        applyStyle(cbBtnOk);

        cbBtnYes.setBounds(0, 1, getScaledInt(174), getScaledInt(50));
        cbBtnYes.setName("MB_YES");
        cbBtnYes.addFocusListener(new FocusListener() {

            @Override
            public void focusLost(FocusEvent e) {
                cbBtnYes.setMnemonic(-1);
            }

            @Override
            public void focusGained(FocusEvent e) {
                cbBtnYes.setMnemonic(KeyEvent.VK_Y);
            }
        });
        applyStyle(cbBtnYes);

        cbBtnNo.setBounds(getScaledInt(175), 1, getScaledInt(175), getScaledInt(50));
        cbBtnNo.setName("MB_NO");
        cbBtnNo.addFocusListener(new FocusListener() {

            @Override
            public void focusLost(FocusEvent e) {
                cbBtnNo.setMnemonic(-1);
            }

            @Override
            public void focusGained(FocusEvent e) {
                cbBtnNo.setMnemonic(KeyEvent.VK_N);
            }
        });
        applyStyle(cbBtnNo);

        panel3.add(cbBtnOk);
        panel2.add(cbBtnYes);
        panel2.add(cbBtnNo);

        add(lblTitle);
        add(lineLbl);
        add(jsp);
        add(panel2);
        add(panel3);
        add(chkOption);

        registerMouseListener();
        Vector<Component> order = new Vector<>();
        order.add(cbBtnYes);
        order.add(cbBtnNo);
        focusPolicy = new MsgBoxFocusTraversalPolicy(order);
        setFocusCycleRoot(true);
        setFocusTraversalPolicyProvider(true);
        setFocusTraversalPolicy(focusPolicy);
    }

    void applyStyle(JButton btn) {
        btn.addActionListener(this);
        btn.setBackground(ColorResource.getDarkerBgColor());// );
        btn.setForeground(Color.WHITE);
        btn.setFocusable(true);
        // btn.setForeground(Color.WHITE);
        btn.setFont(FontResource.getBigFont());
        btn.setBorderPainted(false);
        btn.setMargin(new Insets(0, 0, 0, 0));
        // btn.setFocusPainted(false);
        btn.getInputMap(WHEN_FOCUSED).put(KeyStroke.getKeyStroke("ENTER"), "pressed");
        btn.getInputMap(WHEN_FOCUSED).put(KeyStroke.getKeyStroke("released ENTER"), "released");
    }

    public void registerMouseListener() {
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent me) {
                diffx = me.getXOnScreen() - getLocationOnScreen().x + parent.getLocationOnScreen().x;
                diffy = me.getYOnScreen() - getLocationOnScreen().y + parent.getLocationOnScreen().y;
                // diffx = me.getX(); // - panel.getLocation().x;
                // diffy = me.getY(); // - panel.getLocation().y;
            }
        });

        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent me) {
                int left = me.getXOnScreen() - diffx;
                int top = me.getYOnScreen() - diffy;
                int right = left + getWidth();
                int bottom = top + getHeight();
                if (parent.contains(left, top) && parent.contains(right, bottom)) {
                    setLocation(left, top);
                }
            }
        });
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == cbBtnOk) {
            res = OK;
        } else if (e.getSource() == cbBtnYes) {
            res = YES;
        } else {
            res = NO;
        }
        parent.hideModal(msgBox);
    }

    public static class MsgBoxFocusTraversalPolicy extends FocusTraversalPolicy {
        Vector<Component> order;

        public MsgBoxFocusTraversalPolicy(Vector<Component> order) {
            this.order = new Vector<Component>(order.size());
            this.order.addAll(order);
        }

        public Component getComponentAfter(Container focusCycleRoot, Component aComponent) {
            int idx = (order.indexOf(aComponent) + 1) % order.size();
            return order.get(idx);
        }

        public Component getComponentBefore(Container focusCycleRoot, Component aComponent) {
            int idx = order.indexOf(aComponent) - 1;
            if (idx < 0) {
                idx = order.size() - 1;
            }
            return order.get(idx);
        }

        public Component getDefaultComponent(Container focusCycleRoot) {
            return order.get(0);
        }

        public Component getLastComponent(Container focusCycleRoot) {
            return order.lastElement();
        }

        public Component getFirstComponent(Container focusCycleRoot) {
            return order.get(0);
        }
    }

}
