package xdman.os;

import xdman.os.enums.Browser;
import xdman.os.enums.OSBasedComponentFile;
import xdman.util.Logger;

import java.io.*;

class Linux extends BaseLinuxOS {
    @Override
    public void keepAwakePing() {
        try {
            Runtime.getRuntime().exec(
                    "dbus-send --print-reply --type=method_call --dest=org.freedesktop.ScreenSaver /ScreenSaver org.freedesktop.ScreenSaver.SimulateUserActivity");
        } catch (Exception e) {
            Logger.log(e);
        }
    }

    @Override
    public boolean isAlreadyAutoStart() {
        return isAlreadyAutoStart(".config/autostart/xdman.desktop");
    }

    @Override
    public void removeFromStartup() {
        new File(System.getProperty("user.home"), ".config/autostart/xdman.desktop").delete();
    }

    @Override
    public void addToStartup() {
        addToStartup(".config/autostart", "xdman.desktop", getDesktopFileString());
    }

    @Override
    public void installNativeMessagingHost(Browser browser) {
        File manifestFolder = new File(System.getProperty("user.home"), browser.getNativeHostLocation().getLinux());
        if (!manifestFolder.exists()) {
            manifestFolder.mkdirs();
        }
        File nativeHostFile = new File(OS.getJarFile().getParentFile(), "native_host");
        createNativeManifest(browser.getManifestFile(manifestFolder), nativeHostFile, browser);
    }

    @Override
    public void openFile(File f) throws FileNotFoundException {
        if (!f.exists()) {
            throw new FileNotFoundException();
        }

        try {
            ProcessBuilder pb = new ProcessBuilder();
            pb.command("xdg-open", f.getAbsolutePath());
            pb.start();// .waitFor();
        } catch (Exception e) {
            Logger.log(e);
        }
    }

    @Override
    public void openFolder(String folder, String file) throws FileNotFoundException {
        openFile(new File(folder, file));
    }

    @Override
    public void browseURL(String url) {
        try {
            ProcessBuilder pb = new ProcessBuilder();
            pb.command("xdg-open", url);
            pb.start();// .waitFor();
        } catch (Exception e) {
            Logger.log(e);
        }
    }

    @Override
    public String getDownloadsFolder() {
        String path = getXDGDownloadDir();
        if (path != null) {
            return path;
        }

        return super.getDownloadsFolder();
    }

    @Override
    public boolean launchBrowser(Browser browser, String args) {
        for (File file : browser.getAppLocation().getLinux()) {
            if (file.exists()) {
                String command = file + " " + args;
                try {
                    Runtime.getRuntime().exec(command);
                } catch (IOException e) {
                    Logger.log(e);
                    return false;
                }

                return true;
            }
        }
        return false;
    }

    @Override
    public OSBasedComponentFile getComponentFile() {
        if (getOsArch() == 32) {
            return OSBasedComponentFile.LINUX32;
        }

        return OSBasedComponentFile.LINUX64;
    }

    @Override
    public void initShutdown() {
        String[] commands = new String[]{
                "dbus-send --system --print-reply --dest=org.freedesktop.login1 /org/freedesktop/login1 \"org.freedesktop.login1.Manager.PowerOff\" boolean:true",
                "dbus-send --system --print-reply --dest=\"org.freedesktop.ConsoleKit\" /org/freedesktop/ConsoleKit/Manager org.freedesktop.ConsoleKit.Manager.Stop",
                "systemctl poweroff"
        };

        for (String command : commands) {
            try {
                Process proc = Runtime.getRuntime().exec(command);
                int ret = proc.waitFor();
                if (ret == 0)
                    break;
            } catch (Exception e) {
                Logger.log(e);
            }
        }
    }

    private String getXDGDownloadDir() {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(new File(System.getProperty("user.home"), ".config/user-dirs.dirs"))))) {
            while (true) {
                String line = br.readLine();
                if (line == null) {
                    break;
                }
                if (line.startsWith("XDG_DOWNLOAD_DIR")) {
                    int index = line.indexOf("=");
                    if (index != -1) {
                        String path = line.substring(index + 1).trim();
                        path = path.replace("$HOME", System.getProperty("user.home"));
                        File f = new File(path);
                        if (f.exists()) {
                            return f.getAbsolutePath();
                        }
                    }
                }
            }
        } catch (Exception e) {
            Logger.log(e);
        }
        return null;
    }

    private String getDesktopFileString() {
        String str = "" +
                "[Desktop Entry]\r\n" +
                "Encoding=UTF-8\r\n" +
                "Version=1.0\r\n" +
                "Type=Application\r\n" +
                "Terminal=false\r\n" +
                "Exec=\"%sbin/java\" -Xmx1024m -jar \"%s\" -m\r\n" +
                "Name=Xtreme Download Manager\r\n" +
                "Comment=Xtreme Download Manager\r\n" +
                "Categories=Network;\r\n" +
                "Icon=/opt/xdman/icon.png";
        String s1 = getProperPath(System.getProperty("java.home"));
        String s2 = getJarFile().getAbsolutePath();
        return String.format(str, s1, s2);
    }
}
