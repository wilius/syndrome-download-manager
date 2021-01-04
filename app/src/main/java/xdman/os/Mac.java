package xdman.os;

import xdman.os.enums.Browser;
import xdman.os.enums.OSBasedComponentFile;
import xdman.util.Logger;

import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;

class Mac extends BaseLinuxOS {
    @Override
    public void keepAwakePing() {
        try {
            Runtime.getRuntime().exec("caffeinate -i -t 3");
        } catch (Exception e) {
            Logger.log(e);
        }
    }

    @Override
    public boolean isAlreadyAutoStart() {
        return isAlreadyAutoStart("Library/LaunchAgents/org.sdg.xdman.plist");
    }

    @Override
    public void removeFromStartup() {
        new File(System.getProperty("user.home"), "Library/LaunchAgents/org.sdg.xdman.plist").delete();
    }

    @Override
    public void addToStartup() {
        addToStartup("Library/LaunchAgents", "org.sdg.xdman.plist", getStartupPlist());
    }

    @Override
    public void installNativeMessagingHost(Browser browser) {
        File manifestFolder = new File(System.getProperty("user.home"), browser.getNativeHostLocation().getMac());
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
            pb.command("open", f.getAbsolutePath());
            if (pb.start().waitFor() != 0) {
                throw new FileNotFoundException();
            }
        } catch (Exception e) {
            Logger.log(e);
        }
    }

    @Override
    public void openFolder(String folder, String file) throws FileNotFoundException {
        if (file == null) {
            try {
                ProcessBuilder builder = new ProcessBuilder();
                ArrayList<String> lst = new ArrayList<>();
                lst.add("open");
                lst.add(folder);
                builder.command(lst);
                builder.start();
            } catch (Exception e) {
                Logger.log(e);
            }
            return;
        }
        File f = new File(folder, file);
        if (!f.exists()) {
            throw new FileNotFoundException();
        }
        try {
            ProcessBuilder pb = new ProcessBuilder();
            Logger.log("Opening folder: " + f.getAbsolutePath());
            pb.command("open", "-R", f.getAbsolutePath());
            if (pb.start().waitFor() != 0) {
                throw new FileNotFoundException();
            }
        } catch (Exception e) {
            Logger.log(e);
        }
    }

    @Override
    public void browseURL(String url) {
        try {
            ProcessBuilder pb = new ProcessBuilder();
            pb.command("open", url);
            pb.start();// .waitFor();
        } catch (Exception e) {
            Logger.log(e);
        }
    }

    @Override
    public boolean launchBrowser(Browser browser, String args) {
        for (File file : browser.getAppLocation().getMac()) {
            if (launchApp(file.getAbsolutePath(), args)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public OSBasedComponentFile getComponentFile() {
        return OSBasedComponentFile.MAC;
    }

    public boolean launchApp(String app, String args) {
        try {
            ProcessBuilder pb = new ProcessBuilder();
            pb.command("open", "-n", "-a", app, "--args", args);
            if (pb.start().waitFor() != 0) {
                throw new FileNotFoundException();
            }
            // Runtime.getRuntime().exec(new String[] { "open \"" + app + "\" " + args });
            return true;
        } catch (Exception e) {
            Logger.log(e);
            return false;
        }
    }

    @Override
    public boolean isPopupTrigger(MouseEvent e) {
        return super.isPopupTrigger(e) ||
                (e.getModifiersEx() & InputEvent.BUTTON1_DOWN_MASK) != 0
                        && (e.getModifiersEx() & InputEvent.CTRL_DOWN_MASK) != 0;
        // return (e.getModifiers() & InputEvent.BUTTON1_MASK) != 0 &&
        // (e.getModifiers()
        // & InputEvent.CTRL_MASK) != 0;;
    }

    @Override
    public void initShutdown() {
        try {
            ProcessBuilder builder = new ProcessBuilder();
            ArrayList<String> lst = new ArrayList<String>();
            lst.add("osascript");
            lst.add("-e");
            lst.add("tell app \"System Events\" to shut down");
            builder.command(lst);
            builder.start();
        } catch (Exception e) {
            Logger.log(e);
        }
    }

    private String getStartupPlist() {
        String str = "" +
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<!DOCTYPE plist PUBLIC \"-//Apple Computer//DTD PLIST 1.0//EN\" \"http://www.apple.com/DTDs/PropertyList-1.0.dtd\">" +
                "<plist version=\"1.0\">" +
                "	<dict>" +
                "		<key>Label</key>" +
                "		<string>org.sdg.xdman</string>" +
                "		<key>ProgramArguments</key>" +
                "		<array>" +
                "			<string>%sbin/java</string>" +
                "			<string>-Xmx1024m</string>" +
                "			<string>-Xdock:name=XDM</string>" +
                "			<string>-jar</string>" +
                "			<!-- MODIFY THIS TO POINT TO YOUR EXECUTABLE JAR FILE -->" +
                "			<string>%s</string>" +
                "			<string>-m</string>" +
                "		</array>" +
                "		<key>OnDemand</key>" +
                "		<true />" +
                "		<key>RunAtLoad</key>" +
                "		<true />" +
                "		<key>KeepAlive</key>" +
                "		<false />" +
                "	</dict>" +
                "</plist>";
        String s1 = getProperPath(System.getProperty("java.home"));
        String s2 = getJarFile().getAbsolutePath();
        return String.format(str, s1, s2);
    }
}
