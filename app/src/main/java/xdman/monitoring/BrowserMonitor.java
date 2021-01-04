package xdman.monitoring;

import xdman.Config;
import xdman.XDMApp;
import xdman.model.SyncData;
import xdman.util.JsonUtil;
import xdman.util.Logger;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.EnumSet;
import java.util.List;
import java.util.stream.Collectors;

public class BrowserMonitor implements Runnable {
    private static BrowserMonitor _this;

    public static BrowserMonitor getInstance() {
        if (_this == null) {
            _this = new BrowserMonitor();
        }
        return _this;
    }

    public void startMonitoring() {
        updateSettingsAndStatus();
        Thread t = new Thread(this);
        t.start();
    }

    public void updateSettingsAndStatus() {
        try {
            Path homePath = Paths.get(System.getProperty("user.home"), ".xdman");
            if (!Files.exists(homePath)) {
                Files.createDirectories(homePath);
            }

            Files.write(Paths.get(System.getProperty("user.home"), ".xdman", "settings.json"), JsonUtil.writeValues(getSettingsData(Config.getInstance().getVidMime())));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static SyncData getSyncJSON() {
        return getSettingsData("video/", "audio/", "mpegurl", "f4m", "m3u8");
    }

    private static SyncData getSettingsData(String... mimeTypes) {

        Config config = Config.getInstance();

        SyncData data = new SyncData();
        data.setEnabled(config.isBrowserMonitoringEnabled());
        data.setBlockedHosts(List.of(config.getBlockedHosts()));
        data.setVideoUrls(List.of(config.getVidUrls()));
        data.setFileExts(List.of(config.getFileExts()));
        data.setVidExts(List.of(config.getVidExts()));
        data.setMimeList(List.of(mimeTypes));
        List<SyncData.VideoItem> videoItems = XDMApp.getInstance().getVideoItemsList()
                .stream()
                .map(item -> new SyncData.VideoItem(
                                item.getMetadata().getId(),
                                encode(item.getFile()),
                                item.getInfo()
                        )
                ).collect(Collectors.toList());
        data.setVidList(videoItems);
        return data;
    }

    private static String encode(String str) {
        StringBuilder sb = new StringBuilder();
        int count = 0;
        for (char ch : str.toCharArray()) {
            if (count > 0)
                sb.append(",");
            sb.append((int) ch);
            count++;
        }
        return sb.toString();
    }

    public void run() {
        ServerSocket serverSock = null;
        try {
            serverSock = new ServerSocket();
            serverSock.bind(new InetSocketAddress(InetAddress.getLoopbackAddress(), 9614));
            XDMApp.instanceStarted();
            acquireGlobalLock();
            while (true) {
                Socket sock = serverSock.accept();
                MonitoringSession session = new MonitoringSession(sock);
                session.start();
            }
        } catch (Exception e) {
            Logger.log(e);
            XDMApp.instanceAlreadyRunning();
        }
        if (serverSock != null) {
            try {
                serverSock.close();
            } catch (Exception ignored) {
            }
        }
    }

    private void acquireGlobalLock() {
        try {
            FileChannel fc = FileChannel.open(Paths.get(System.getProperty("user.home"), XDMApp.GLOBAL_LOCK_FILE),
                    EnumSet.of(StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.READ,
                            StandardOpenOption.WRITE));
            int maxRetry = 10;
            for (int i = 0; i < maxRetry; i++) {
                FileLock fileLock = fc.tryLock();
                if (fileLock != null) {
                    Logger.log("Lock acquired...");
                    return;
                }

                // if lock is already acquired by some other process wait
                // and retry for at most 5 sec, after that throw error and
                // exit
                Thread.sleep(500);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}
