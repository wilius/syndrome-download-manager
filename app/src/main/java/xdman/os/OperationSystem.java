package xdman.os;

import xdman.Config;
import xdman.Main;
import xdman.os.enums.Browser;
import xdman.os.enums.OSBasedComponentFile;
import xdman.util.Logger;

import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.event.MouseEvent;
import java.io.*;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

public abstract class OperationSystem {
    public static final OperationSystem OS;

    static {
        OS = detectOS();
    }

    private static OperationSystem detectOS() {
        String os = System.getProperty("os.name").toLowerCase(Locale.ENGLISH);
        if (os.contains("mac") || os.contains("darwin")
                || os.contains("os x")) {
            return new Mac();
        } else if (os.contains("linux")) {
            return new Linux();
        } else if (os.contains("windows")) {
            return new Windows();
        } else {
            // TODO
            throw new RuntimeException("Unsupported Operation System");
        }
    }

    public abstract void keepAwakePing();

    boolean isAlreadyAutoStart(String path) {
        File f = new File(System.getProperty("user.home"), path);
        if (!f.exists()) {
            return false;
        }

        byte[] buf = new byte[(int) f.length()];
        try (FileInputStream in = new FileInputStream(f)) {
            if (in.read(buf) != f.length()) {
                return false;
            }
        } catch (Exception e) {
            // TODO
            Logger.log(e);
        }
        String str = new String(buf);
        String s1 = getProperPath(System.getProperty("java.home"));
        String s2 = getJarFile().getAbsolutePath();
        return str.contains(s1) && str.contains(s2);
    }

    String getProperPath(String path) {
        if (path.endsWith("/"))
            return path;
        return path + "/";
    }

    void addToStartup(String path, String filename, String content) {
        File dir = new File(System.getProperty("user.home"), path);
        dir.mkdirs();
        File f = new File(dir, filename);
        try (FileOutputStream fs = new FileOutputStream(f)) {
            fs.write(content.getBytes());
        } catch (Exception e) {
            Logger.log(e);
        }
        f.setExecutable(true);
    }

    // TODO change it to a enum
    public int getOsArch() {
        if (System.getProperty("os.arch").contains("64")) {
            return 64;
        } else {
            return 32;
        }
    }

    public abstract boolean isAlreadyAutoStart();

    public abstract void removeFromStartup();

    public abstract void addToStartup();

    public abstract void installNativeMessagingHost(Browser browser);

    public abstract void openFile(File f) throws FileNotFoundException;

    public abstract void openFolder(String folder, String file) throws FileNotFoundException;

    public abstract void browseURL(String url);

    public String getDownloadsFolder() {
        return new File(System.getProperty("user.home"), "Downloads").getAbsolutePath();
    }

    public final long getFreeSpace(String folder) {
        if (folder == null) {
            return new File(Config.getInstance().getTemporaryFolder())
                    .getFreeSpace();
        }

        return new File(folder).getFreeSpace();
    }

    public final File getJarFile() {
        File file = new File("/opt/xdman/xdman.jar");
        if (file.exists()) {
            return file;
        }

        try {
            return new File(Main.class.getProtectionDomain().getCodeSource()
                    .getLocation().toURI().getPath());
        } catch (URISyntaxException e) {
            // TODO
            throw new RuntimeException(e);
        }
    }

    public final void copyURL(String url) {
        try {
            Toolkit.getDefaultToolkit().getSystemClipboard()
                    .setContents(new StringSelection(url), null);
        } catch (Exception e) {
            Logger.log(e);
        }
    }

    public boolean isComponentsInstalled() {
        return isFFMPEGInstalled() && isYoutubeDLInstalled();
    }

    public boolean isPopupTrigger(MouseEvent e) {
        return e.isPopupTrigger();
    }

    public String clipboard() {
        try {
            return (String) Toolkit.getDefaultToolkit().getSystemClipboard()
                    .getData(DataFlavor.stringFlavor);
        } catch (Exception e) {
            Logger.log(e);
        }
        return null;
    }

    final void createNativeManifest(File manifestFile, File nativeHostFile, Browser browser) {
        try (OutputStream out = new FileOutputStream(manifestFile)) {
            String json = String.format("" +
                            "{" +
                            "  \"name\": \"%s\", " +
                            "  \"description\": \"Native messaging host for Xtreme Download Manager\", " +
                            "  \"path\": \"%s\", " +
                            "  \"type\": \"stdio\", " +
                            "  \"%s\": [ \"%s\" ] "
                            + "}",
                    browser.getRegeditName(),
                    nativeHostFile.getAbsolutePath().replace("\\", "\\\\"),
                    browser.getManifestKey(),
                    String.join("\", \"", browser.getExtensionIds()));

            out.write(json.getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public abstract boolean launchBrowser(Browser browser, String args);

    public abstract OSBasedComponentFile getComponentFile();

    public final boolean isFFMPEGInstalled() {
        return getFFMPEGFile() != null;
    }

    public final boolean isYoutubeDLInstalled() {
        return getYoutubeDlFile() != null;
    }

    public abstract void initShutdown();

    abstract String getFfmpegFileName();

    abstract String getYoutubeDlFileName();

    public File getFFMPEGFile() {
        return getFile(getFfmpegFileName());
    }

    public File getYoutubeDlFile() {
        return getFile(getYoutubeDlFileName());
    }

    private File getFile(String fileName) {
        File file = new File(Config.getInstance().getDataFolder(), fileName);
        if (file.exists()) {
            return file;
        }
        file = new File(OS.getJarFile().getParentFile(), fileName);
        if (file.exists()) {
            return file;
        }

        return null;
    }
}
