package xdman.win32;

import xdman.util.Logger;

import java.io.File;

import static xdman.os.OperationSystem.OS;

public class NativeMethods {
    private static NativeMethods _me;

    private NativeMethods() {
        String dllPath = new File(OS.getJarFile().getParentFile(), "xdm_native.dll").getAbsolutePath();
        try {
            System.load(dllPath);
        } catch (Exception e) {
            Logger.log(e);
        }
    }

    public static NativeMethods getInstance() {
        if (_me == null) {
            _me = new NativeMethods();
        }
        return _me;
    }

    public final native void keepAwakePing();

    public final native void addToStartup(String key, String value);

    public final native boolean presentInStartup(String key, String value);

    public final native void removeFromStartup(String key);

    public final native String getDownloadsFolder();

    public final native String stringTest(String str);
}
