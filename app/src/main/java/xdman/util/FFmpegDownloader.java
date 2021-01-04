package xdman.util;

import xdman.Config;
import xdman.DownloadListener;
import xdman.DownloadWindowListener;
import xdman.XDMConstants;
import xdman.downloaders.http.HttpDownloader;
import xdman.downloaders.metadata.HttpMetadata;
import xdman.os.OperationSystem;
import xdman.os.enums.OSBasedComponentFile;
import xdman.ui.components.DownloadWindow;
import xdman.ui.components.FFmpegExtractorWnd;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class FFmpegDownloader implements DownloadListener, DownloadWindowListener, FFExtractCallback {
    private final String url;
    private final String tmpFile;
    FFmpegExtractorWnd wnd2;
    private HttpDownloader downloader;
    private DownloadWindow downloadWindow;

    public FFmpegDownloader() {
        OSBasedComponentFile file = OperationSystem.OS.getComponentFile();

        this.url = "http://xdman.sourceforge.net/components/" + file.getFfmpeg();
        this.tmpFile = UUID.randomUUID().toString();
    }

    public void start() {
        HttpMetadata metadata = new HttpMetadata();
        metadata.setUrl(url);
        System.out.println(url);

        this.downloader = new HttpDownloader(metadata.getId(), Config.getInstance().getTemporaryFolder(), metadata);
        this.downloader.registerListener(this);
        this.downloader.start();

        this.downloadWindow = new DownloadWindow(metadata.getId(), this);
        this.downloadWindow.setVisible(true);
    }

    @Override
    public void downloadFinished(String id) {
        extractFFmpeg();
        downloadWindow.close(XDMConstants.FINISHED, 0);
    }

    @Override
    public void downloadFailed(String id) {
        downloadWindow.close(XDMConstants.FAILED, downloader.getErrorCode());
        deleteTmpFiles(id);
    }

    @Override
    public void downloadStopped(String id) {
        downloadWindow.close(XDMConstants.PAUSED, 0);
        deleteTmpFiles(id);
    }

    @Override
    public void downloadConfirmed(String id) {

    }

    @Override
    public void downloadUpdated(String id) {
        downloadWindow.update(downloader, "Components");
    }

    @Override
    public String getOutputFolder(String id) {
        return Config.getInstance().getTemporaryFolder();
    }

    @Override
    public String getOutputFile(String id, boolean update) {
        return tmpFile;
    }

    @Override
    public void pauseDownload(String id) {
        if (downloader != null) {
            downloader.stop();
            downloader.unregisterListener();
        }
    }

    @Override
    public void hidePrgWnd(String id) {

    }

    private void deleteTmpFiles(String id) {
        FileUtils.deleteTempFile(id);
    }

    private void extractFFmpeg() {
        ZipInputStream zipIn = null;
        OutputStream out = null;
        wnd2 = new FFmpegExtractorWnd(this);
        wnd2.setVisible(true);
        try {
            String versionFile = null;
            File input = new File(Config.getInstance().getTemporaryFolder(), tmpFile);
            zipIn = new ZipInputStream(new FileInputStream(input));

            while (true) {
                ZipEntry ent = zipIn.getNextEntry();
                if (ent == null)
                    break;
                String name = ent.getName();
                if (name.endsWith(".version")) {
                    versionFile = name;
                }
                File outFile = new File(Config.getInstance().getDataFolder(), name);
                out = new FileOutputStream(outFile);
                byte[] buf = new byte[8192];
                while (true) {
                    int x = zipIn.read(buf);
                    if (x == -1)
                        break;
                    out.write(buf, 0, x);
                }
                out.close();
                out = null;
                outFile.setExecutable(true);
            }

            // remove old x.version files if exists
            try {
                if (Config.getInstance().getDataFolder() != null) {
                    File[] files = new File(Config.getInstance().getDataFolder()).listFiles();
                    if (files != null) {
                        for (File f : files) {
                            if (f.getName().endsWith(".version") && (!f.getName().equals(versionFile))) {
                                f.delete();
                            }
                        }
                    }
                }
            } catch (Exception e) {
                Logger.log(e);
            }

            input.delete();
            wnd2.dispose();
        } catch (Exception e) {
            Logger.log(e);
        } finally {
            try {
                zipIn.close();
                if (out != null)
                    out.close();
            } catch (Exception e) {
                Logger.log(e);
            }
        }
    }

    public void stop() {
        if (wnd2 != null)
            wnd2.dispose();
    }

}
