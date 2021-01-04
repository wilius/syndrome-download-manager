package xdman.os.browser;

import xdman.os.OSBasedConfigBuilder;

public class NativeHostLocationBuilder extends OSBasedConfigBuilder<String, NativeHostLocation> {
    public NativeHostLocation build() {
        return new NativeHostLocation(windows, mac, linux);
    }
}