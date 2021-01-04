package xdman.os;

import com.sun.jna.platform.win32.Advapi32Util;
import com.sun.jna.platform.win32.WinReg;
import xdman.Config;
import xdman.XDMApp;
import xdman.os.enums.Browser;
import xdman.os.enums.OSBasedComponentFile;
import xdman.ui.components.MessageBox;
import xdman.util.Logger;
import xdman.win32.NativeMethods;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

class Windows extends OperationSystem {

    @Override
    public void keepAwakePing() {
        NativeMethods.getInstance().keepAwakePing();
    }

    @Override
    public boolean isAlreadyAutoStart() {
        String launchCmd = "\"" + System.getProperty("java.home") + "\\bin\\javaw.exe\" -Xmx1024m -jar \""
                + getJarFile().getAbsolutePath() + "\" -m";
        Logger.log("Launch CMD: " + launchCmd);
        return NativeMethods.getInstance().presentInStartup("XDM", launchCmd);
    }

    @Override
    public void removeFromStartup() {
        NativeMethods.getInstance().removeFromStartup("XDM");
    }

    @Override
    public void addToStartup() {
        String launchCmd = "\"" + System.getProperty("java.home") + "\\bin\\javaw.exe\" -Xmx1024m -jar \""
                + getJarFile().getAbsolutePath() + "\" -m";
        Logger.log("Launch CMD: " + launchCmd);
        NativeMethods.getInstance().addToStartup("XDM", launchCmd);
    }

    @Override
    public void installNativeMessagingHost(Browser browser) {
        String regeditPath = browser.getRegeditPath();
        String regeditName = browser.getRegeditName();

        String regeditKey = regeditPath + "\\" + regeditName;
        if (!Advapi32Util.registryKeyExists(WinReg.HKEY_CURRENT_USER,
                regeditKey)) {
            if (!Advapi32Util.registryCreateKey(WinReg.HKEY_CURRENT_USER, regeditPath,
                    regeditName)) {
                // TODO throw exception
                MessageBox.show(XDMApp.getInstance().getMainWindow(),
                        "Error: Unable to register native messaging host");
                return;
            }
        }

        File manifestFolder = new File(Config.getInstance().getDataFolder());
        File manifestFile = browser.getManifestFile(manifestFolder);
        File nativeHostFile = new File(getJarFile().getParentFile(), "native_host.exe");
        createNativeManifest(manifestFile, nativeHostFile, browser);
        try {
            Advapi32Util.registrySetStringValue(
                    WinReg.HKEY_CURRENT_USER,
                    regeditKey,
                    null,
                    manifestFile.getAbsolutePath());
        } catch (Exception e) {
            // TODO throw exception
            MessageBox.show(XDMApp.getInstance().getMainWindow(),
                    "Error: Unable to register native messaging host");
        }
    }

    @Override
    public void openFile(File f) throws FileNotFoundException {
        if (!f.exists()) {
            throw new FileNotFoundException();
        }

        try {
            ProcessBuilder builder = new ProcessBuilder();
            ArrayList<String> lst = new ArrayList<>();
            lst.add("rundll32");
            lst.add("url.dll,FileProtocolHandler");
            lst.add(f.getAbsolutePath());
            builder.command(lst);
            builder.start();
        } catch (IOException e) {
            Logger.log(e);
        }
    }

    @Override
    public void openFolder(String folder, String file) {
        if (file == null) {
            try {
                ProcessBuilder builder = new ProcessBuilder();
                ArrayList<String> lst = new ArrayList<>();
                lst.add("explorer");
                lst.add(folder);
                builder.command(lst);
                builder.start();
            } catch (Exception e) {
                Logger.log(e);
            }
            return;
        }

        try {
            File f = new File(folder, file);
            if (!f.exists()) {
                throw new FileNotFoundException();
            }
            ProcessBuilder builder = new ProcessBuilder();
            ArrayList<String> lst = new ArrayList<>();
            lst.add("explorer");
            lst.add("/select,");
            lst.add(f.getAbsolutePath());
            builder.command(lst);
            builder.start();
        } catch (IOException e) {
            Logger.log(e);
        }
    }

    @Override
    public void browseURL(String url) {
        try {
            ProcessBuilder builder = new ProcessBuilder();
            ArrayList<String> lst = new ArrayList<>();
            lst.add("rundll32");
            lst.add("url.dll,FileProtocolHandler");
            lst.add(url);
            builder.command(lst);
            builder.start();
        } catch (IOException e) {
            Logger.log(e);
        }
    }

    @Override
    public boolean launchBrowser(Browser browser, String args) {
        for (File file : browser.getAppLocation().getWindows()) {
            if (file.exists()) {
                String command = "\"" + file + "\" " + args;
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
        if (below7()) {
            return OSBasedComponentFile.XP;
        }

        return OSBasedComponentFile.WIN7;
    }

    @Override
    public void initShutdown() {
        try {
            ProcessBuilder builder = new ProcessBuilder();
            ArrayList<String> lst = new ArrayList<>();
            lst.add("shutdown");
            lst.add("-t");
            lst.add("30");
            lst.add("-s");
            builder.command(lst);
            builder.start();
        } catch (Exception e) {
            Logger.log(e);
        }
    }

    @Override
    String getFfmpegFileName() {
        return "ffmpeg.exe";
    }

    @Override
    String getYoutubeDlFileName() {
        return "youtube-dl.exe";
    }

    private boolean below7() {
        try {
            int version = Integer
                    .parseInt(System.getProperty("os.version").split("\\.")[0]);
            return (version < 6);
        } catch (Exception ignored) {
        }

        return false;
    }
}
