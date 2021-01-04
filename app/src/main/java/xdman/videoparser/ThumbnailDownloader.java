package xdman.videoparser;

import xdman.Config;
import xdman.network.http.JavaHttpClient;
import xdman.util.Logger;
import xdman.util.XDMUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ThumbnailDownloader implements Runnable {
    private final String[] thumbnails;
    private boolean stop;
    private ThumbnailListener listener;
    private final long instanceKey;

    public void download() {
        new Thread(this).start();
    }

    public ThumbnailDownloader(ArrayList<String> list, ThumbnailListener listener, long instanceKey) {
        this.thumbnails = new String[list.size()];
        int i = 0;
        for (String str : list) {
            this.thumbnails[i++] = str;
        }
        this.listener = listener;
        this.instanceKey = instanceKey;
    }

    public void stop() {
        stop = true;
        this.listener = null;
    }

    @Override
    public void run() {
        List<String> list = new ArrayList<>();
        try {
            if (thumbnails == null) {
                return;
            }

            for (String thumbnail : thumbnails) {
                if (stop) {
                    return;
                }

                String file = downloadReal(thumbnail);
                if (stop)
                    return;
                if (file != null) {
                    if (listener != null) {
                        listener.thumbnailsLoaded(instanceKey, thumbnail, file);
                    }
                    list.add(file);
                }
            }
        } catch (Exception e) {
            Logger.log(e);
        } finally {
            if (stop) {
                for (String file : list) {
                    new File(file).delete();
                }
            }
        }
    }

    private String downloadReal(String url) {
        JavaHttpClient client = null;
        File tmpFile = new File(Config.getInstance().getTemporaryFolder(), UUID.randomUUID().toString());
        FileOutputStream out = null;
        try {
            client = new JavaHttpClient(url);
            client.setFollowRedirect(true);
            client.connect();
            int resp = client.getStatusCode();
            if (stop) {
                return null;
            }
            Logger.log("manifest download response: " + resp);
            if (resp == 200 || resp == 206) {
                InputStream in = client.getInputStream();
                long len = client.getContentLength();
                out = new FileOutputStream(tmpFile);
                XDMUtils.copyStream(in, out, len);
                Logger.log("thumbnail download successfull");
                return tmpFile.getAbsolutePath();
            }
        } catch (Exception e) {
            Logger.log(e);
        } finally {
            try {
                client.dispose();
            } catch (Exception ignored) {
            }
            try {
                if (out != null) {
                    out.close();
                }
            } catch (Exception ignored) {
            }

            if (stop) {
                tmpFile.delete();
            }
        }
        return null;
    }
}
