package xdman;

import xdman.downloaders.Downloader;
import xdman.downloaders.dash.DashDownloader;
import xdman.downloaders.ftp.FtpDownloader;
import xdman.downloaders.hds.HdsDownloader;
import xdman.downloaders.hls.HlsDownloader;
import xdman.downloaders.http.HttpDownloader;
import xdman.downloaders.metadata.DashMetadata;
import xdman.downloaders.metadata.HdsMetadata;
import xdman.downloaders.metadata.HlsMetadata;
import xdman.downloaders.metadata.HttpMetadata;
import xdman.enums.FileCategory;
import xdman.monitoring.BrowserMonitor;
import xdman.network.http.HttpContext;
import xdman.os.enums.Browser;
import xdman.ui.components.*;
import xdman.ui.laf.XDMLookAndFeel;
import xdman.ui.res.StringResource;
import xdman.util.*;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.PasswordAuthentication;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.*;

import static xdman.os.OperationSystem.OS;

public class XDMApp implements DownloadListener, DownloadWindowListener, Comparator<String> {
    public static final String GLOBAL_LOCK_FILE = ".xdm-global-lock";
    public static final String APP_VERSION = "7.2.11";
    public static final String XDM_WINDOW_TITLE = "XDM 2020";
    public static final String APP_UPDAT_URL = "https://api.github.com/repos/subhra74/xdm/releases/latest";
    public static final String APP_UPDATE_CHK_URL = "https://subhra74.github.io/xdm/update-checker.html?v=";
    public static final String APP_WIKI_URL = "https://github.com/subhra74/xdm/wiki";
    public static final String APP_HOME_URL = "https://github.com/subhra74/xdm";
    public static final String APP_TWITTER_URL = "https://twitter.com/XDM_subhra74";
    public static final String APP_FACEBOOK_URL = "https://www.facebook.com/XDM.subhra74/";
    public static final String[] ZOOM_LEVEL_STRINGS = {
            "Default", "50%", "75%", "100%", "125%", "150%", "200%", "250%",
            "300%", "350%", "400%", "450%", "500%"
    };

    public static final double[] ZOOM_LEVEL_VALUES = {
            -1, 0.5, 0.75, 1.0, 1.25, 1.5, 2.0, 2.5, 3.0, 3.5, 4.0, 4.5, 5.0
    };

    private static HashMap<String, String> paramMap;
    private static XDMApp _this;

    private final ArrayList<VideoPopupItem> itemList = new ArrayList<>();
    private final ArrayList<ListChangeListener> listChangeListeners = new ArrayList<>();
    private final Map<String, DownloadEntry> downloads = new HashMap<>();
    private final HashMap<String, Downloader> downloaderMap = new HashMap<>();
    private final HashMap<String, DownloadWindow> downloadWindows = new HashMap<>();
    private final QueueManager qMgr = QueueManager.getInstance();
    /*
        this buffer is used when there
        is a limit on maximum
        simultaneous downloads and
        more downloads are started
        than permissible limit. If
        queues are also running then
        this buffer will be processed
        first
     */
    private final ArrayList<String> pendingDownloads = new ArrayList<>();

    private LinkRefreshCallback refreshCallback;
    private MainWindow mainWindow;

    /*
     if main window in not created
     notification is stored in this
     variable
     */
    private int pendingNotification = -1;
    private long lastSaved;

    private XDMApp() {
        Logger.log("Init app");
        try {
            UIManager.setLookAndFeel(new XDMLookAndFeel());
        } catch (Exception e) {
            Logger.log(e);
        }
        Config.getInstance().setAutoShutdown(false);
        loadDownloadList();
        lastSaved = System.currentTimeMillis();
        qMgr.fixCorruptEntries(getDownloadIds(), this);

        QueueScheduler.getInstance().start();
        HttpContext.getInstance().init();

        if (Config.getInstance().isMonitorClipboard()) {
            ClipboardMonitor.getInstance().startMonitoring();
        }
    }

    public static void instanceStarted() {
        Logger.log("instance starting...");
        final XDMApp app = XDMApp.getInstance();
        EventQueue.invokeLater(() -> {
            if (!paramMap.containsKey("background")) {
                Logger.log("showing main window.");
                app.showMainWindow();
            }
            TrayHandler.createTray();
        });

        if (Config.getInstance().isFirstRun()) {
            OS.addToStartup();
        }

        for (Browser browser : Browser.values()) {
            OS.installNativeMessagingHost(browser);
        }
        Logger.log("instance started.");
    }

    public static void instanceAlreadyRunning() {
        Logger.log("instance already runninng");
        ParamUtils.sendParam(paramMap);
        System.exit(0);
    }

    public static void start(String[] args) {

        Config.getInstance().load();
        if (Config.getInstance().getZoomLevelIndex() > 0) {
            double zoom = XDMApp.ZOOM_LEVEL_VALUES[Config.getInstance().getZoomLevelIndex()];
            System.out.println("Zoom index; " + Config.getInstance().getZoomLevelIndex() + " " + zoom);

            System.setProperty("sun.java2d.uiScale.enabled", "true");
            System.setProperty("sun.java2d.uiScale", String.format("%.2f", zoom));
        }

        paramMap = new HashMap<>();
        boolean expect = false;
        boolean winInstall = false;
        String key = null;
        for (String arg : args) {
            if (expect) {
                paramMap.put(key, arg);
                expect = false;
                continue;
            }

            if ("-u".equals(arg)) {
                key = "url";
                expect = true;
            } else if ("-m".equals(arg)) {
                paramMap.put("background", "true");
                expect = false;
            } else if ("-i".equals(arg)) {
                paramMap.put("installer", "true");
                expect = false;
                winInstall = true;
            } else if ("-s".equals(arg)) {
                key = "screen";
                expect = true;
            } else if ("-o".equals(arg) || "--output".equals(arg)) {
                key = "output";
                expect = true;
            } else if ("-q".equals(arg) || "--quiet".equals(arg)) {
                paramMap.put("quiet", "true");
                expect = false;
            }
        }

        if (winInstall) {
            try {
                SwingUtilities.invokeAndWait(() -> {
                    System.out.println("wininstall");
                    if (UpdateChecker.getComponentVersion() == null) {
                        new ComponentInstaller().setVisible(true);
                    }

                });
            } catch (Exception ignored) {
            }
        }

        Logger.log("starting monitoring...");
        BrowserMonitor.getInstance().startMonitoring();
    }

    public void showMainWindow() {
        if (mainWindow == null) {
            mainWindow = new MainWindow();
        }
        mainWindow.setVisible(true);
        mainWindow.toFront();
    }

    public void downloadFinished(String id) {
        DownloadEntry ent = downloads.get(id);
        ent.setState(XDMConstants.FINISHED);
        Downloader d = downloaderMap.remove(id);
        if (d != null && d.getSize() < 0) {
            ent.setSize(d.getDownloaded());
        }
        DownloadWindow wnd = downloadWindows.get(id);
        if (wnd != null) {
            wnd.close(XDMConstants.FINISHED, 0);
            downloadWindows.remove(id);
            if (ent.isStartedByUser()) {
                if (Config.getInstance().showDownloadCompleteWindow()) {
                    new DownloadCompleteWnd(ent.getFile(), getFolder(ent)).setVisible(true);
                }
            }
        }
        notifyListeners(null);
        saveDownloadList();
        if (Config.getInstance().isExecAntivir()) {
            if (!StringUtils.isNullOrEmptyOrBlank(Config.getInstance().getAntivirExe())) {
                execAntivir();
            }
        }

        processNextItem(id);
        if (isAllFinished()) {
            if (Config.getInstance().isAutoShutdown()) {
                OS.initShutdown();
            }

            if (Config.getInstance().isExecCmd()) {
                execCmd();
            }
        }
    }

    public void downloadFailed(String id) {
        Downloader d = downloaderMap.remove(id);
        if (id == null) {
            Logger.log("Download failed, id null");
            return;
        }
        DownloadWindow wnd = downloadWindows.get(id);
        if (wnd != null) {
            wnd.close(XDMConstants.FAILED, d.getErrorCode());
            downloadWindows.remove(id);
        } else {
            Logger.log("Wnd is null!!!");
        }
        DownloadEntry ent = downloads.get(id);
        ent.setState(XDMConstants.PAUSED);
        notifyListeners(id);
        saveDownloadList();
        Logger.log("removed");
        processNextItem(id);
    }

    public void downloadStopped(String id) {
        downloaderMap.remove(id);
        DownloadWindow wnd = downloadWindows.get(id);
        if (wnd != null) {
            wnd.close(XDMConstants.PAUSED, 0);
            downloadWindows.remove(id);
        }
        DownloadEntry ent = downloads.get(id);
        ent.setState(XDMConstants.PAUSED);
        notifyListeners(id);
        saveDownloadList();
        processNextItem(id);
    }

    public void downloadConfirmed(String id) {
        Logger.log("confirmed " + id);
        Downloader d = downloaderMap.get(id);
        DownloadEntry ent = downloads.get(id);
        ent.setSize(d.getSize());
        if (d.isFileNameChanged()) {
            ent.setFile(d.getNewFile());
            ent.setCategory(FileCategory.find(d.getNewFile()));
            updateFileName(ent);
        }

        // if (isSameFile(ent.getFolder(),
        // Config.getInstance().getDownloadFolder())) {
        // if (ent.getCategory() != XDMConstants.OTHER) {
        // File folderNew = new File(Config.getInstance().getDownloadFolder(),
        // XDMUtils.getFolderForCategory(ent.getCategory()));
        // if (!folderNew.exists()) {
        // folderNew.mkdirs();
        // }
        // ent.setFolder(folderNew.getAbsolutePath());
        // }
        // }

        DownloadWindow wnd = downloadWindows.get(id);
        if (wnd != null) {
            wnd.update(d, ent.getFile());
        }
        notifyListeners(id);
        saveDownloadList();
    }

    public void downloadUpdated(String id) {
        try {
            DownloadEntry ent = downloads.get(id);
            Downloader d = downloaderMap.get(id);
            if (d == null) {
                Logger.log("################# sync error ##############");
                return;
            }
            ent.setSize(d.getSize());
            ent.setDownloaded(d.getDownloaded());
            ent.setProgress(d.getProgress());
            ent.setState(d.isAssembling() ? XDMConstants.ASSEMBLING : XDMConstants.DOWNLOADING);
            DownloadWindow wnd = downloadWindows.get(id);
            if (wnd != null) {
                wnd.update(d, ent.getFile());
            }
        } finally {
            notifyListeners(id);
            long now = System.currentTimeMillis();
            if (now - lastSaved > 5000) {
                saveDownloadList();
                lastSaved = now;
            }
        }
    }

    public synchronized static XDMApp getInstance() {
        if (_this == null) {
            _this = new XDMApp();
        }
        return _this;
    }

    public void addLinks(final List<HttpMetadata> list) {
        SwingUtilities.invokeLater(() -> {
            BatchDownloadWnd wnd = new BatchDownloadWnd(list);
            wnd.setVisible(true);
        });
    }

    public void addDownload(final HttpMetadata metadata, final String file) {
        if (refreshCallback != null) {
            if (refreshCallback.isValidLink(metadata)) {
                return;
            }
        }

        SwingUtilities.invokeLater(() -> {
            String fileName;
            String folderPath;

            if (StringUtils.isNullOrEmptyOrBlank(file)) {
                if (metadata != null) {
                    fileName = XDMUtils.getFileName(metadata.getUrl());
                } else {
                    fileName = null;
                }
                folderPath = null;
            } else {
                fileName = Paths.get(file).getFileName().toString();

                String downloadFolderPath;
                if (Config.getInstance().isForceSingleFolder()) {
                    downloadFolderPath = Config.getInstance().getDownloadFolder();
                } else {
                    var category = FileCategory.find(file);
                    downloadFolderPath = XDMApp.getInstance().getFolder(category);
                }

                folderPath = downloadFolderPath;
            }

            if (metadata != null
                    && (Config.getInstance().isQuietMode() || Config.getInstance().isDownloadAutoStart())) {
                createDownload(fileName, folderPath, metadata, true, "", 0, 0);
                return;
            }

            new NewDownloadWindow(metadata, fileName, folderPath).setVisible(true);
        });
    }

    public void addVideo(final HttpMetadata metadata, final String file) {
        if (refreshCallback != null) {
            if (refreshCallback.isValidLink(metadata)) {
                return;
            }
        }
        SwingUtilities.invokeLater(() -> {
            if (!OS.isFFMPEGInstalled()) {
                if (JOptionPane.showConfirmDialog(getMainWindow(), StringResource.get("MSG_INSTALL_ADDITIONAL_COMPONENTS"),
                        StringResource.get("MSG_COMPONENT_TITLE"),
                        JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                    FFmpegDownloader fd = new FFmpegDownloader();
                    fd.start();
                }
                return;
            }
            new VideoDownloadWindow(metadata, file).setVisible(true);
        });
    }

    public void addMedia(final HttpMetadata metadata, final String file, final String info) {
        System.out.println("video notification: " + Config.getInstance().isShowVideoNotification());
        if (Config.getInstance().isShowVideoNotification()) {
            SwingUtilities.invokeLater(() -> {
                VideoPopup.getInstance().addVideo(metadata, file, info);
                BrowserMonitor.getInstance().updateSettingsAndStatus();
            });
        }
    }

    public void youtubeVideoTitleUpdated(String url, String title) {
        if (Config.getInstance().isShowVideoNotification()) {
            SwingUtilities.invokeLater(() -> {
                if (VideoPopup.hasInstance()) {
                    VideoPopup.getInstance().updateYoutubeTitle(url, title);
                    BrowserMonitor.getInstance().updateSettingsAndStatus();
                }
            });
        }
    }

    public void createDownload(String file, String folder, HttpMetadata metadata, boolean now, String queueId,
                               int formatIndex, int streamIndex) {
        metadata.save();
        DownloadEntry ent = new DownloadEntry();
        ent.setId(metadata.getId());
        ent.setOutputFormatIndex(formatIndex);
        ent.setState(XDMConstants.PAUSED);
        ent.setFile(file);
        ent.setFolder(folder);
        ent.setTempFolder(Config.getInstance().getTemporaryFolder());
        ent.setCategory(FileCategory.find(file));
        ent.setDate(System.currentTimeMillis());
        putInQueue(queueId, ent);
        ent.setStartedByUser(now);
        downloads.put(metadata.getId(), ent);
        saveDownloadList();
        if (!now) {
            DownloadQueue q = qMgr.getQueueById(queueId);
            if (q != null && q.isRunning()) {
                Logger.log("Queue is running, if no pending download pickup next available download");
                q.next();
            }
        }
        if (now) {
            startDownload(metadata.getId(), metadata, ent, streamIndex);
        }
        notifyListeners(null);
    }

    // could be new or resume
    private void startDownload(String id, HttpMetadata metadata, DownloadEntry ent, int streams) {
        if (!checkAndBufferRequests(id)) {
            Logger.log("starting " + id + " with: " + metadata + " is dash: " + (metadata instanceof DashMetadata));
            Downloader d = null;

            if (metadata instanceof DashMetadata) {
                Logger.log("Dash download with stream: " + streams);
                if (streams == 1) {
                    DashMetadata dm = (DashMetadata) metadata;
                    dm.setUrl(dm.getUrl2());// set video url as main url
                    dm.setUrl2(null);
                } else if (streams == 2) {
                    DashMetadata dm = (DashMetadata) metadata;
                    dm.setUrl2(null);
                } else {
                    Logger.log("Dash download created");
                    // create dash downloader
                    DashMetadata dm = (DashMetadata) metadata;
                    d = new DashDownloader(id, ent.getTempFolder(), dm);
                }
            }
            if (metadata instanceof HlsMetadata) {
                Logger.log("Hls download created");
                d = new HlsDownloader(id, ent.getTempFolder(), (HlsMetadata) metadata);
            }
            if (metadata instanceof HdsMetadata) {
                Logger.log("Hds download created");
                d = new HdsDownloader(id, ent.getTempFolder(), (HdsMetadata) metadata);
            }
            if (d == null) {
                if (metadata.getType() == XDMConstants.FTP) {
                    d = new FtpDownloader(id, ent.getTempFolder(), metadata);
                } else {
                    d = new HttpDownloader(id, ent.getTempFolder(), metadata);
                }
            }

            d.setOuputMediaFormat(ent.getOutputFormatIndex());
            downloaderMap.put(id, d);
            d.registerListener(this);
            ent.setState(XDMConstants.DOWNLOADING);
            d.start();

            if (!Config.getInstance().isQuietMode() && Config.getInstance().showDownloadWindow()) {
                DownloadWindow wnd = new DownloadWindow(id, this);
                downloadWindows.put(id, wnd);
                wnd.setVisible(true);
            }
        } else {
            Logger.log(id + ": Maximum download limit reached, queueing request");
        }
    }

    public void pauseDownload(String id) {
        Downloader d = downloaderMap.get(id);
        if (d != null) {
            d.stop();
            d.unregisterListener();
        }
    }

    public void resumeDownload(String id, boolean startedByUser) {
        DownloadEntry ent = downloads.get(id);
        ent.setStartedByUser(startedByUser);
        if (ent.getState() == XDMConstants.PAUSED || ent.getState() == XDMConstants.FAILED) {
            if (!checkAndBufferRequests(id)) {
                ent.setState(XDMConstants.DOWNLOADING);
                HttpMetadata metadata = HttpMetadata.load(id);
                if (!Config.getInstance().isQuietMode() && Config.getInstance().showDownloadWindow()
                        && ent.isStartedByUser()) {
                    DownloadWindow wnd = new DownloadWindow(id, this);
                    downloadWindows.put(id, wnd);
                    wnd.setVisible(true);
                }
                Downloader d = null;
                if (metadata instanceof DashMetadata) {
                    DashMetadata dm = (DashMetadata) metadata;
                    Logger.log("Dash download- url1: " + dm.getUrl() + " url2: " + dm.getUrl2());
                    d = new DashDownloader(id, ent.getTempFolder(), dm);
                }
                if (metadata instanceof HlsMetadata) {
                    HlsMetadata hm = (HlsMetadata) metadata;
                    Logger.log("HLS download- url1: " + hm.getUrl());
                    d = new HlsDownloader(id, ent.getTempFolder(), hm);
                }
                if (metadata instanceof HdsMetadata) {
                    HdsMetadata hm = (HdsMetadata) metadata;
                    Logger.log("HLS download- url1: " + hm.getUrl());
                    d = new HdsDownloader(id, ent.getTempFolder(), hm);
                }
                if (d == null) {
                    Logger.log("normal download");
                    if (metadata.getType() == XDMConstants.FTP) {
                        d = new FtpDownloader(id, ent.getTempFolder(), metadata);
                    } else {
                        d = new HttpDownloader(id, ent.getTempFolder(), metadata);
                    }
                }
                downloaderMap.put(id, d);
                d.setOuputMediaFormat(ent.getOutputFormatIndex());
                d.registerListener(this);
                d.resume();

            } else {
                Logger.log(id + ": Maximum download limit reached, queueing request");
            }
            notifyListeners(null);
        }
    }

    public void restartDownload(String id) {
        DownloadEntry ent = downloads.get(id);
        if (ent.getState() == XDMConstants.PAUSED || ent.getState() == XDMConstants.FAILED
                || ent.getState() == XDMConstants.FINISHED) {
            ent.setState(XDMConstants.PAUSED);
            clearData(ent);
            resumeDownload(id, true);
        }
    }

    synchronized public void addListener(ListChangeListener listener) {
        listChangeListeners.add(listener);
    }

    synchronized public void removeListener(ListChangeListener listener) {
        listChangeListeners.remove(listener);
    }

    private void notifyListeners(String id) {
        for (ListChangeListener listChangeListener : listChangeListeners) {
            if (id != null) {
                listChangeListener.listItemUpdated(id);
            } else {
                listChangeListener.listChanged();
            }
        }
    }

    public DownloadEntry getEntry(String id) {
        return downloads.get(id);
    }

    public ArrayList<String> getDownloadList(FileCategory category, int state, String searchText, String queueId) {
        ArrayList<String> idList = new ArrayList<>();
        for (String key : downloads.keySet()) {
            DownloadEntry ent = downloads.get(key);
            if (state == XDMConstants.ALL || state == (ent.getState() == XDMConstants.FINISHED ? XDMConstants.FINISHED
                    : XDMConstants.UNFINISHED)) {
                if (category == null || category.equals(ent.getCategory())) {
                    boolean matched = false;
                    if (!"ALL".equals(queueId)) {
                        if (queueId != null) {
                            if (!queueId.equals(ent.getQueueId())) {
                                continue;
                            }
                        }
                    }

                    if (searchText != null && searchText.length() > 0) {
                        if (ent.getFile().contains(searchText)) {
                            matched = true;
                        }
                    } else {
                        matched = true;
                    }
                    if (matched) {
                        idList.add(ent.getId());
                    }
                }
            }
        }
        return idList;
    }

    private void clearData(DownloadEntry ent) {
        if (ent == null)
            return;
        File folder = new File(ent.getTempFolder(), ent.getId());
        File[] files = folder.listFiles();
        if (files != null) {
            for (File file : files) {
                file.delete();
            }
        }
        folder.delete();
    }

    @Override
    public String getOutputFolder(String id) {
        DownloadEntry ent = downloads.get(id);
        if (ent == null) {
            return Config.getInstance().getCategoryOther();
        } else {
            return getFolder(ent);
        }
    }

    @Override
    public String getOutputFile(String id, boolean update) {
        DownloadEntry ent = downloads.get(id);
        if (update) {
            updateFileName(ent);
        }
        return ent.getFile();
    }

    public String getFolder(DownloadEntry ent) {
        if (ent.getFolder() != null) {
            return ent.getFolder();
        }
        if (Config.getInstance().isForceSingleFolder()) {
            return Config.getInstance().getDownloadFolder();
        }

        return this.getFolder(ent.getCategory());
    }

    public String getFolder(FileCategory category) {
        switch (category) {
            case DOCUMENT:
                return Config.getInstance().getCategoryDocuments();
            case MUSIC:
                return Config.getInstance().getCategoryMusic();
            case VIDEO:
                return Config.getInstance().getCategoryVideos();
            case APP:
                return Config.getInstance().getCategoryPrograms();
            case COMPRESSED:
                return Config.getInstance().getCategoryCompressed();
            default:
                return Config.getInstance().getCategoryOther();
        }
    }

    private void loadDownloadList() {
        File file = new File(Config.getInstance().getDataFolder(), "downloads.txt");
        loadDownloadList(file);
    }

    public void loadDownloadList(File file) {
        if (!file.exists()) {
            return;
        }
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        // BufferedReader reader = null;
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
            // reader = new BufferedReader(new InputStreamReader(new
            // FileInputStream(file),
            // Charset.forName("UTF-8")));
            String line = reader.readLine();
            if (line == null) {
                throw new NullPointerException("Unexpected EOF");
            }
            int count = Integer.parseInt(line.trim());
            for (int i = 0; i < count; i++) {
                int fieldCount = Integer.parseInt(XDMUtils.readLineSafe(reader).trim());
                DownloadEntry ent = new DownloadEntry();
                for (int j = 0; j < fieldCount; j++) {
                    String ln = reader.readLine();
                    if (ln == null) {
                        return;
                    }
                    int index = ln.indexOf(":");
                    if (index > 0) {
                        String key = ln.substring(0, index).trim();
                        String val = ln.substring(index + 1).trim();
                        if (key.equals("id")) {
                            ent.setId(val);
                        }
                        if (key.equals("file")) {
                            ent.setFile(val);
                        }
                        if (key.equals("category")) {
                            ent.setCategory(FileCategory.valueOf(val));
                        }
                        if (key.equals("state")) {
                            int state = Integer.parseInt(val);
                            ent.setState(state == XDMConstants.FINISHED ? state : XDMConstants.PAUSED);
                        }
                        if (key.equals("folder")) {
                            ent.setFolder(val);
                        }
                        if (key.equals("date")) {
                            ent.setDate(dateFormat.parse(val).getTime());
                        }
                        if (key.equals("downloaded")) {
                            ent.setDownloaded(Long.parseLong(val));
                        }
                        if (key.equals("size")) {
                            ent.setSize(Long.parseLong(val));
                        }
                        if (key.equals("progress")) {
                            ent.setProgress(Integer.parseInt(val));
                        }
                        if (key.equals("queueid")) {
                            ent.setQueueId(val);
                        }
                        if (key.equals("formatIndex")) {
                            ent.setOutputFormatIndex(Integer.parseInt(val));
                        }
                        if (key.equals("tempfolder")) {
                            ent.setTempFolder(val);
                        }
                    }
                }
                downloads.put(ent.getId(), ent);
            }
        } catch (Exception e) {
            Logger.log(e);
        }

    }

    private void saveDownloadList() {
        File file = new File(Config.getInstance().getDataFolder(), "downloads.txt");
        saveDownloadList(file);
    }

    public void saveDownloadList(File file) {
        int count = downloads.size();
        BufferedWriter writer = null;
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String newLine = System.getProperty("line.separator");
        try {
            writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8));
            writer.write(count + "");
            writer.newLine();
            for (String key : downloads.keySet()) {
                DownloadEntry ent = downloads.get(key);
                int c = 0;
                StringBuilder sb = new StringBuilder();
                sb.append("id: ").append(ent.getId()).append(newLine);
                c++;
                sb.append("file: ").append(ent.getFile()).append(newLine);
                c++;
                sb.append("category: ").append(ent.getCategory()).append(newLine);
                c++;
                sb.append("state: ").append(ent.getState()).append(newLine);
                c++;
                if (ent.getFolder() != null) {
                    sb.append("folder: ").append(ent.getFolder()).append(newLine);
                    c++;
                }
                sb.append("date: ").append(dateFormat.format(new Date(ent.getDate()))).append(newLine);
                c++;
                sb.append("downloaded: ").append(ent.getDownloaded()).append(newLine);
                c++;
                sb.append("size: ").append(ent.getSize()).append(newLine);
                c++;
                sb.append("progress: ").append(ent.getProgress()).append(newLine);
                c++;
                if (ent.getTempFolder() != null) {
                    sb.append("tempfolder: ").append(ent.getTempFolder()).append(newLine);
                    c++;
                }
                if (ent.getQueueId() != null) {
                    sb.append("queueid: ").append(ent.getQueueId()).append(newLine);
                    c++;
                }
                sb.append("formatIndex: ").append(ent.getOutputFormatIndex()).append(newLine);
                c++;
                writer.write(c + newLine);
                writer.write(sb.toString());

            }
            writer.close();
        } catch (Exception e) {
            Logger.log(e);
            try {
                if (writer != null)
                    writer.close();
            } catch (Exception ignored) {
            }
        }
    }

    public void hidePrgWnd(String id) {
        DownloadWindow wnd = downloadWindows.get(id);
        if (wnd != null) {
            downloadWindows.remove(id);
            wnd.close(XDMConstants.PAUSED, 0);
        }
    }

    private synchronized int getActiveDownloadCount() {
        int count = 0;
        for (String key : downloads.keySet()) {
            DownloadEntry ent = downloads.get(key);
            int state = ent.getState();
            if (state == XDMConstants.FINISHED || state == XDMConstants.PAUSED || state == XDMConstants.FAILED)
                continue;
            count++;
        }
        return count;
    }

    private synchronized boolean checkAndBufferRequests(String id) {
        int actCount = getActiveDownloadCount();
        if (Config.getInstance().getMaxDownloads() > 0 && actCount >= Config.getInstance().getMaxDownloads()) {
            Logger.log("active: " + actCount + " max: " + Config.getInstance().getMaxDownloads());
            if (!pendingDownloads.contains(id)) {
                pendingDownloads.add(id);
            }
            return true;
        }
        return false;
    }

    private synchronized void processNextItem(String lastId) {
        processPendingRequests();
        if (lastId == null)
            return;
        DownloadEntry ent = getEntry(lastId);
        if (ent == null) {
            return;
        }
        DownloadQueue queue;
        if ("".equals(ent.getQueueId())) {
            queue = qMgr.getDefaultQueue();
        } else {
            queue = qMgr.getQueueById(ent.getQueueId());
        }
        if (queue != null && queue.isRunning()) {
            queue.next();
        }
    }

    private void processPendingRequests() {
        int activeCount = getActiveDownloadCount();
        int maxDownloadCount = Config.getInstance().getMaxDownloads();
        List<String> tobeStartedIds = new ArrayList<>();
        if (maxDownloadCount - activeCount > 0) {
            for (int i = 0; i < Math.min(maxDownloadCount, pendingDownloads.size()); i++) {
                String ent = pendingDownloads.get(i);
                tobeStartedIds.add(ent);
            }
        }
        if (tobeStartedIds.size() > 0) {
            for (String id : tobeStartedIds) {
                pendingDownloads.remove(id);
                DownloadEntry ent = getEntry(id);
                if (ent != null) {
                    resumeDownload(id, ent.isStartedByUser());
                }
            }
        }
    }

    public boolean queueItemPending(String queueId) {
        if (queueId != null) {
            for (String id : pendingDownloads) {
                DownloadEntry ent = getEntry(id);
                if (ent == null || ent.getQueueId() == null) {
                    continue;
                }

                if (ent.getQueueId().equals(queueId)) {
                    return true;
                }
            }
        }
        return false;
    }

    public ArrayList<DownloadQueue> getQueueList() {
        return qMgr.getQueueList();
    }

    public DownloadQueue getQueueById(String queueId) {
        return qMgr.getQueueById(queueId);
    }

    private void putInQueue(String queueId, DownloadEntry ent) {
        DownloadQueue q = getQueueById(queueId);
        String id = ent.getId();
        if (q == null) {
            Logger.log("No queue found for: '" + queueId + "'");
            return;
        }
        String qid = ent.getQueueId();
        DownloadQueue oldQ = getQueueById(qid);
        Logger.log("Adding to: '" + queueId + "'");
        if (!q.getQueueId().equals(qid)) {
            if (oldQ != null) {
                // remove from previous queue
                oldQ.removeFromQueue(id);
            }
            ent.setQueueId(queueId);
            q.addToQueue(id);
        }
    }

    @Override
    public int compare(String key1, String key2) {
        DownloadEntry ent1 = getEntry(key1);
        DownloadEntry ent2 = getEntry(key2);
        if (ent1 == null)
            return -1;
        if (ent2 == null)
            return 1;
        return Long.compare(ent1.getDate(), ent2.getDate());
    }

    private Iterator<String> getDownloadIds() {
        return downloads.keySet().iterator();
    }

    public boolean isAllFinished() {
        if (getActiveDownloadCount() != 0) {
            return false;
        }
        if (pendingDownloads.size() != 0) {
            return false;
        }
        for (int i = 0; i < QueueManager.getInstance().getQueueList().size(); i++) {
            DownloadQueue q = QueueManager.getInstance().getQueueList().get(i);
            if (q.hasPendingItems()) {
                return false;
            }
        }
        return true;
    }

    public int deleteDownloads(ArrayList<String> ids, boolean outflie) {
        int c = 0;
        for (String id : ids) {
            DownloadEntry ent = getEntry(id);
            if (ent != null) {
                if (ent.getState() == XDMConstants.FINISHED || ent.getState() == XDMConstants.PAUSED
                        || ent.getState() == XDMConstants.FAILED) {
                    this.downloads.remove(id);
                    pendingDownloads.remove(id);
                    String qId = ent.getQueueId();
                    if (qId != null) {
                        DownloadQueue q = getQueueById(qId);
                        if (q != null) {
                            if (q.getQueueId().length() > 0) {
                                q.removeFromQueue(id);
                            }
                        }
                    }
                    deleteFiles(ent, outflie);
                    c++;
                }
            }
        }
        saveDownloadList();
        notifyListeners(null);
        return ids.size() - c;
    }

    private void deleteFiles(DownloadEntry ent, boolean outfile) {
        // DownloadEntry ent = getEntry(id);
        if (ent == null)
            return;
        String id = ent.getId();
        Logger.log("Deleting metadata for " + id);
        File mf = new File(Config.getInstance().getMetadataFolder(), id);
        boolean deleted = mf.delete();
        Logger.log("Deleted manifest " + id + " " + deleted);
        File df = new File(ent.getTempFolder(), id);
        File[] files = df.listFiles();
        if (files != null && files.length > 0) {
            for (File f : files) {
                deleted = f.delete();
                Logger.log("Deleted tmp file " + id + " " + deleted);
            }
        }
        deleted = df.delete();
        Logger.log("Deleted tmp folder " + id + " " + deleted);
        if (outfile) {
            File f = new File(XDMApp.getInstance().getFolder(ent), ent.getFile());
            f.delete();
        }
    }

    public String getURL(String id) {
        try {
            HttpMetadata metadata = HttpMetadata.load(id);
            if (metadata instanceof DashMetadata) {
                DashMetadata dm = (DashMetadata) metadata;
                return dm.getUrl() + "\n" + dm.getUrl2();
            } else {
                return metadata.getUrl();
            }
        } catch (Exception e) {
            Logger.log(e);
        }
        return "";
    }

    public void registerRefreshCallback(LinkRefreshCallback callback) {
        this.refreshCallback = callback;
    }

    public void unregisterRefreshCallback() {
        this.refreshCallback = null;
    }

    public void deleteCompleted() {
        Iterator<String> allIds = downloads.keySet().iterator();
        ArrayList<String> idList = new ArrayList<>();
        while (allIds.hasNext()) {
            String id = allIds.next();
            DownloadEntry ent = downloads.get(id);
            if (ent.getState() == XDMConstants.FINISHED) {
                idList.add(id);
            }
        }
        deleteDownloads(idList, false);
    }

    public boolean promptCredential(String id, String msg, boolean proxy) {
        DownloadEntry ent = getEntry(id);
        if (ent == null)
            return false;
        if (!ent.isStartedByUser())
            return false;
        PasswordAuthentication pauth = getCredential(msg, proxy);
        if (pauth == null) {
            return false;
        }
        if (proxy) {
            Config.getInstance().setProxyUser(pauth.getUserName());
            if (pauth.getPassword() != null) {
                Config.getInstance().setProxyPass(new String(pauth.getPassword()));
            }
        } else {
            Logger.log("saving password for: " + msg);
            CredentialManager.getInstance().addCredentialForHost(msg, pauth);
        }
        return true;
    }

    private PasswordAuthentication getCredential(String msg, boolean proxy) {
        JTextField user = new JTextField(30);
        JPasswordField pass = new JPasswordField(30);

        String prompt = proxy ? StringResource.get("PROMPT_PROXY")
                : String.format(StringResource.get("PROMPT_SERVER"), msg);

        Object[] obj = new Object[5];
        obj[0] = prompt;
        obj[1] = StringResource.get("DESC_USER");
        obj[2] = user;
        obj[3] = StringResource.get("DESC_PASS");
        obj[4] = pass;

        if (JOptionPane.showOptionDialog(null, obj, StringResource.get("PROMPT_CRED"), JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE, null, null, null) == JOptionPane.OK_OPTION) {
            return new PasswordAuthentication(user.getText(), pass.getPassword());
        }
        return null;
    }

    private void execCmd() {
        if (!StringUtils.isNullOrEmptyOrBlank(Config.getInstance().getCustomCmd())) {
            XDMUtils.exec(Config.getInstance().getCustomCmd());
        }
    }

    private void execAntivir() {
        XDMUtils.exec(Config.getInstance().getAntivirExe() + " "
                + (Config.getInstance().getAntivirCmd() == null ? "" : Config.getInstance().getAntivirCmd()));
    }

    private void updateFileName(DownloadEntry ent) {
        if (Config.getInstance().getDuplicateAction() == XDMConstants.DUP_ACT_OVERWRITE) {
            return;
        }
        Logger.log("checking for same named file on disk...");
        String id = ent.getId();
        String outputFolder = getOutputFolder(id);
        File f = new File(outputFolder, ent.getFile());
        int c = 1;
        String ext = XDMUtils.getExtension(f.getAbsolutePath());
        if (ext == null) {
            ext = "";
        }
        String f2 = XDMUtils.getFileNameWithoutExtension(ent.getFile());
        while (f.exists()) {
            f = new File(outputFolder, f2 + "_" + c + ext);
            c++;
        }
        Logger.log("Updating file name- old: " + ent.getFile() + " new: " + f.getName());
        ent.setFile(f.getName());
    }

    public void importList(File file) {
        loadDownloadList(file);
    }

    public void exportList(File file) {
        saveDownloadList(file);
    }

    public void notifyComponentUpdate() {
        pendingNotification = UpdateChecker.COMP_UPDATE_AVAILABLE;
        if (mainWindow != null) {
            mainWindow.showNotification();
        }
    }

    public void notifyComponentInstall() {
        pendingNotification = UpdateChecker.COMP_NOT_INSTALLED;
        if (mainWindow != null) {
            mainWindow.showNotification();
        }
    }

    public void notifyAppUpdate() {
        pendingNotification = UpdateChecker.APP_UPDATE_AVAILABLE;
        if (mainWindow != null) {
            mainWindow.showNotification();
        }
    }

    public void clearNotifications() {
        pendingNotification = -1;
        if (mainWindow != null) {
            mainWindow.showNotification();
        }
    }

    public int getNotification() {
        return pendingNotification;
    }

    private void openTempFolder(String id) {
        DownloadEntry ent = getEntry(id);
        if (ent == null)
            return;
        File df = new File(ent.getTempFolder(), id);
        try {
            OS.openFolder(null, df.getAbsolutePath());
        } catch (Exception e) {
            Logger.log(e);
        }
    }

    public void openPreview(String id) {
        DownloadEntry ent = XDMApp.getInstance().getEntry(id);
        if (ent != null && (FileCategory.VIDEO.equals(ent.getCategory()) || FileCategory.MUSIC.equals(ent.getCategory()))) {
            if (OS.isFFMPEGInstalled()) {
                XDMApp.getInstance().openPreviewPlayer(id);
            } else {
                JOptionPane.showMessageDialog(getMainWindow(), StringResource.get("LBL_COMPONENT_MISSING"));
            }
        } else if (JOptionPane.showConfirmDialog(getMainWindow(), StringResource.get("LBL_NOT_A_VIDEO"), "Preview",
                JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            XDMApp.getInstance().openTempFolder(id);
        }
    }

    private void openPreviewPlayer(String id) {
        OS.browseURL("http://127.0.0.1:9614/preview/media/" + id);
    }

    public void showPrgWnd(String id) {
        DownloadEntry ent = getEntry(id);
        if (ent == null) {
            return;
        }
        if (ent.getState() == XDMConstants.FINISHED || ent.getState() == XDMConstants.PAUSED
                || ent.getState() == XDMConstants.FAILED) {
            return;
        }
        DownloadWindow wnd = downloadWindows.get(id);
        if (wnd == null) {
            wnd = new DownloadWindow(id, this);
            downloadWindows.put(id, wnd);
            wnd.setVisible(true);
        }
    }

    public void fileNameChanged(String id) {
        notifyListeners(id);
    }

    public ArrayList<VideoPopupItem> getVideoItemsList() {
        return itemList;
    }

    public MainWindow getMainWindow() {
        return mainWindow;
    }

    public void exit() {
        saveDownloadList();
        qMgr.saveQueues();
        Config.getInstance().save();
        System.exit(0);
    }

}