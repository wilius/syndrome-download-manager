package xdman.ui.components;

import xdman.Config;
import xdman.XDMApp;
import xdman.ui.res.FontResource;
import xdman.ui.res.ImageResource;
import xdman.ui.res.StringResource;
import xdman.util.Logger;
import xdman.util.XDMUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

public class TrayHandler {
    public static void createTray() {
        if (!SystemTray.isSupported()) {
            Logger.log("SystemTray is not supported");
            return;
        }

        Image img;

        if (XDMUtils.detectOS() == XDMUtils.LINUX) {
            if (Config.getInstance().isHideTray()) {
                return;
            }

            img = ImageResource.getImage("icon_linux.png");
        } else {
            img = ImageResource.getImage("icon.png");
        }

        if (img == null) {
            return;
        }

        final PopupMenu popup = new PopupMenu();
        final TrayIcon trayIcon = new TrayIcon(img);
        trayIcon.setImageAutoSize(true);
        final SystemTray tray = SystemTray.getSystemTray();

        // Create a pop-up menu components
        MenuItem addUrlItem = new MenuItem(StringResource.get("MENU_ADD_URL"));
        addUrlItem.setFont(FontResource.getBigFont());
        addUrlItem.addActionListener(TrayHandler::act);
        addUrlItem.setName("ADD_URL");
        MenuItem addVidItem = new MenuItem(StringResource.get("MENU_VIDEO_DWN"));
        addVidItem.setFont(FontResource.getBigFont());
        addVidItem.addActionListener(TrayHandler::act);
        addVidItem.setName("ADD_VID");
        MenuItem addBatchItem = new MenuItem(StringResource.get("MENU_BATCH_DOWNLOAD"));
        addBatchItem.setFont(FontResource.getBigFont());
        addBatchItem.addActionListener(TrayHandler::act);
        addBatchItem.setName("ADD_BAT");
        MenuItem addClipItem = new MenuItem(StringResource.get("MENU_CLIP_ADD_MENU"));
        addClipItem.setFont(FontResource.getBigFont());
        addClipItem.addActionListener(TrayHandler::act);
        addClipItem.setName("ADD_CLIP");
        MenuItem restoreItem = new MenuItem(StringResource.get("MSG_RESTORE"));
        restoreItem.setFont(FontResource.getBigFont());
        restoreItem.addActionListener(TrayHandler::act);
        restoreItem.setName("RESTORE");
        CheckboxMenuItem monitoringItem = new CheckboxMenuItem(StringResource.get("BROWSER_MONITORING"));
        monitoringItem.addItemListener(e -> {
            Logger.log("monitoring change");
            Config.getInstance().enableMonitoring(!Config.getInstance().isBrowserMonitoringEnabled());

        });
        monitoringItem.setFont(FontResource.getBigFont());
        monitoringItem.setState(Config.getInstance().isBrowserMonitoringEnabled());
        monitoringItem.addActionListener(TrayHandler::act);
        monitoringItem.setName("MONITORING");
        MenuItem throttleItem = new MenuItem(StringResource.get("MENU_SPEED_LIMITER"));
        throttleItem.setFont(FontResource.getBigFont());
        throttleItem.addActionListener(TrayHandler::act);
        throttleItem.setName("THROTTLE");
        MenuItem exitItem = new MenuItem(StringResource.get("MENU_EXIT"));
        exitItem.setFont(FontResource.getBigFont());
        exitItem.addActionListener(TrayHandler::act);
        exitItem.setName("EXIT");

        // Add components to pop-up menu
        popup.add(addUrlItem);
        popup.add(addVidItem);
        popup.add(addBatchItem);
        popup.add(addClipItem);
        popup.add(monitoringItem);
        popup.add(restoreItem);
        popup.add(throttleItem);
        popup.add(exitItem);
        trayIcon.setToolTip(XDMApp.XDM_WINDOW_TITLE);
        trayIcon.setPopupMenu(popup);

        trayIcon.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON1 || e.getClickCount() == 2) {
                    XDMApp.getInstance().showMainWindow();
                }
            }
        });

        try {
            tray.add(trayIcon);
        } catch (AWTException e) {
            Logger.log("TrayIcon could not be added.");
        }

        Config.getInstance().addConfigListener(() -> monitoringItem.setState(Config.getInstance().isBrowserMonitoringEnabled()));
    }

    private static void act(ActionEvent e) {
        MenuItem c = (MenuItem) e.getSource();
        String name = c.getName();
        if ("ADD_URL".equals(name)) {
            XDMApp.getInstance().addDownload(null, null);
        } else if ("RESTORE".equals(name)) {
            XDMApp.getInstance().showMainWindow();
        } else if ("EXIT".equals(name)) {
            XDMApp.getInstance().exit();
        } else if ("THROTTLE".equals(name)) {
            int ret = SpeedLimiter.getSpeedLimit();
            if (ret >= 0) {
                Config.getInstance().setSpeedLimit(ret);
            }
        } else if ("ADD_VID".equals(name)) {
            MediaDownloaderWindow wnd = new MediaDownloaderWindow();
            wnd.setVisible(true);
        } else if ("ADD_BAT".equals(name)) {
            new BatchPatternDialog().setVisible(true);
        } else if ("ADD_CLIP".equals(name)) {
            List<String> urlList = BatchDownloadWnd.getUrls();
            if (urlList.size() > 0) {
                new BatchDownloadWnd(XDMUtils.toMetadata(urlList)).setVisible(true);
            } else {
                JOptionPane.showMessageDialog(XDMApp.getInstance().getMainWindow(), StringResource.get("LBL_BATCH_EMPTY_CLIPBOARD"));
            }
        }
    }
}
