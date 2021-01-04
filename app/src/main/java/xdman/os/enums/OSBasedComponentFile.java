package xdman.os.enums;

public enum OSBasedComponentFile {
    XP("xp.zip", "xp.zip.xz"),
    WIN7("win.zip", "win.zip.xz"),
    MAC("mac.zip", "mac.zip.xz"),
    LINUX32("linux32.zip", "linux86.zip.xz"),
    LINUX64("linux64.zip", "linux64.zip.xz");

    private final String ffmpeg;
    private final String component;

    OSBasedComponentFile(String ffmpeg, String component) {
        this.ffmpeg = ffmpeg;
        this.component = component;
    }

    public String getFfmpeg() {
        return ffmpeg;
    }

    public String getComponent() {
        return component;
    }
}
