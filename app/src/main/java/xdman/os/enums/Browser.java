package xdman.os.enums;

import xdman.os.browser.AppLocation;
import xdman.os.browser.AppLocationBuilder;
import xdman.os.browser.NativeHostLocation;
import xdman.os.browser.NativeHostLocationBuilder;

import java.io.File;
import java.util.List;

public enum Browser {
    FIREFOX(
            new String[]{
                    "browser-mon@xdman.sourceforge.net"
            },

            "allowed_extensions",

            new AppLocationBuilder()
                    .windows(
                            new File[]{
                                    new File(System.getenv("ProgramW6432"), "Mozilla Firefox\\firefox.exe"),
                                    new File(System.getenv("PROGRAMFILES"), "Mozilla Firefox\\firefox.exe"),
                                    new File(System.getenv("PROGRAMFILES(X86)"), "Mozilla Firefox\\firefox.exe")
                            })
                    .mac(new File[]{new File("/Applications/Firefox.app")})
                    .linux(new File[]{new File("/usr/bin/firefox")})
                    .build(),

            new NativeHostLocationBuilder()
                    .mac("Library/Application Support/Mozilla/NativeMessagingHosts")
                    .linux(".mozilla/native-messaging-hosts")
                    .build(),

            "Software\\Mozilla\\NativeMessagingHosts",
            "xdmff.native_host"),

    CHROME(
            new String[]{
                    "chrome-extension://danmljfachfhpbfikjgedlfifabhofcj/",
                    "chrome-extension://dkckaoghoiffdbomfbbodbbgmhjblecj/"
            },

            "allowed_origins",
            new AppLocationBuilder()
                    .windows(new File[]{
                            new File(System.getenv("PROGRAMFILES"), "Google\\Chrome\\Application\\chrome.exe"),
                            new File(System.getenv("PROGRAMFILES(X86)"), "Google\\Chrome\\Application\\chrome.exe"),
                            new File(System.getenv("LOCALAPPDATA"), "Google\\Chrome\\Application\\chrome.exe")
                    })
                    .mac(new File[]{new File("/Applications/Google Chrome.app")})
                    .linux(new File[]{new File("/usr/bin/google-chrome")})
                    .build(),

            new NativeHostLocationBuilder()
                    .mac("Library/Application Support/Google/Chrome/NativeMessagingHosts")
                    .linux(".config/chromium/NativeMessagingHosts")
                    .build(),

            "Software\\Google\\Chrome\\NativeMessagingHosts",
            "xdm_chrome.native_host"),

    CHROMIUM(CHROME,
            new NativeHostLocationBuilder()
                    .mac("Library/Application Support/Chromium/NativeMessagingHosts")
                    .linux(".config/chromium/NativeMessagingHosts")
                    .build());

    private final List<String> extensionIds;
    private final String manifestKey;
    private final AppLocation appLocation;
    private final NativeHostLocation nativeHostLocation;
    private final String regeditPath;
    private final String regeditName;

    Browser(Browser from,
            NativeHostLocation nativeHostLocation) {
        this.extensionIds = from.extensionIds;
        this.manifestKey = from.manifestKey;
        this.appLocation = from.appLocation;
        this.regeditPath = from.regeditPath;
        this.regeditName = from.regeditName;
        this.nativeHostLocation = nativeHostLocation;
    }

    Browser(String[] extensionIds,
            String manifestKey,
            AppLocation appLocation,
            NativeHostLocation nativeHostLocation,
            String regeditPath,
            String regeditName) {
        this.extensionIds = List.of(extensionIds);
        this.manifestKey = manifestKey;
        this.appLocation = appLocation;
        this.nativeHostLocation = nativeHostLocation;
        this.regeditPath = regeditPath;
        this.regeditName = regeditName;
    }

    public List<String> getExtensionIds() {
        return extensionIds;
    }

    public String getManifestKey() {
        return manifestKey;
    }

    public AppLocation getAppLocation() {
        return appLocation;
    }

    public NativeHostLocation getNativeHostLocation() {
        return nativeHostLocation;
    }

    public String getRegeditPath() {
        return regeditPath;
    }

    public String getRegeditName() {
        return regeditName;
    }

    public File getManifestFile(File parent) {
        return new File(parent, getRegeditName() + ".json");
    }
}
