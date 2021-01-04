package xdman.os.browser;

import xdman.os.OSBasedConfigBuilder;

import java.io.File;

public class AppLocationBuilder extends OSBasedConfigBuilder<File[], AppLocation> {
    public AppLocation build() {
        return new AppLocation(windows, mac, linux);
    }
}