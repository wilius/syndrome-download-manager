package xdman.os.browser;

import xdman.os.OSBasedConfig;

public class NativeHostLocation extends OSBasedConfig<String> {
    public NativeHostLocation(String windows, String mac, String linux) {
        super(windows, mac, linux);
    }
}
