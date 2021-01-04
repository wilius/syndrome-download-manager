package xdman.os.browser;

import xdman.os.OSBasedConfig;

import java.io.File;
import java.util.List;

public class AppLocation extends OSBasedConfig<List<File>> {
    AppLocation(File[] windows,
                File[] mac,
                File[] linux) {
        super(List.of(windows), List.of(mac), List.of(linux));
    }
}
