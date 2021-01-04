package xdman;

import xdman.downloaders.metadata.HttpMetadata;
import xdman.util.Logger;
import xdman.util.StringUtils;
import xdman.util.XDMUtils;

import java.net.URL;

import static xdman.os.OperationSystem.OS;

public class ClipboardMonitor implements Runnable {

    private static ClipboardMonitor _this;
    private String lastContent;
    private Thread t;

    private ClipboardMonitor() {

    }

    public static ClipboardMonitor getInstance() {
        if (_this == null) {
            _this = new ClipboardMonitor();
        }
        return _this;
    }

    public void startMonitoring() {
        try {
            if (t == null) {
                t = new Thread(this);
                t.start();
            }
        } catch (Exception e) {
            Logger.log(e);
        }

    }

    public void stopMonitoring() {
        try {
            if (t != null && t.isAlive()) {
                t.interrupt();
                t = null;
            }
        } catch (Exception e) {
            Logger.log(e);
        }

    }

    @Override
    public void run() {
        try {
            while (true) {
                String clipboard = OS.clipboard();
                if (StringUtils.isNullOrEmptyOrBlank(clipboard)) {
                    return;
                }

                if (!clipboard.equals(lastContent)) {
                    Logger.log("New content: " + clipboard);
                    lastContent = clipboard;
                    try {
                        new URL(clipboard);
                        HttpMetadata md = new HttpMetadata();
                        md.setUrl(clipboard);
                        String file = XDMUtils.getFileName(clipboard);
                        String ext = XDMUtils.getExtension(file);
                        if (!StringUtils.isNullOrEmptyOrBlank(ext)) {
                            ext = ext.toUpperCase().replace(".", "");
                        }

                        String[] arr = Config.getInstance().getFileExts();
                        boolean found = false;
                        for (int i = 0; i < arr.length; i++) {
                            if (arr[i].contains(ext)) {
                                found = true;
                                break;
                            }
                        }
                        if (found) {
                            XDMApp.getInstance().addDownload(md, file);
                        }
                    } catch (Exception e) {
                    }

                }
                Thread.sleep(1000);
            }
        } catch (Exception e) {
            Logger.log(e);
        }
    }

}
