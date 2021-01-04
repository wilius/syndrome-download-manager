package xdman.videoparser;

import xdman.Config;
import xdman.network.ProxyResolver;
import xdman.network.http.WebProxy;
import xdman.util.Logger;
import xdman.util.StringUtils;
import xdman.videoparser.youtubedl.YoutubeDlResponse;
import xdman.videoparser.youtubedl.YoutubeDlVideo;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static xdman.os.OperationSystem.OS;

public class YoutubeDLHandler {
    private Process proc;
    private final String url;
    private final String youtubeDl;
    private final String user;
    private final String pass;

    private final ArrayList<YoutubeDlVideo> videos;

    public YoutubeDLHandler(String url, String user, String pass) {
        this.url = url;
        this.videos = new ArrayList<>();
        this.youtubeDl = OS.getYoutubeDlFile().getAbsolutePath();
        this.user = user;
        this.pass = pass;
    }

    public void start() {
        File tmpError = new File(Config.getInstance().getTemporaryFolder(), UUID.randomUUID().toString());
        File tmpOutput = new File(Config.getInstance().getTemporaryFolder(), UUID.randomUUID().toString());
        InputStream in = null;
        try {
            List<String> args = new ArrayList<>();
            args.add(youtubeDl);
            args.add("--no-warnings");
            args.add("-q");
            args.add("-i");
            args.add("-J");
            if (!(StringUtils.isNullOrEmptyOrBlank(user) || StringUtils.isNullOrEmptyOrBlank(pass))) {
                args.add("-u");
                args.add(user);
                args.add("-p");
                args.add(pass);
            }

            WebProxy webproxy = ProxyResolver.resolve(url);
            if (webproxy != null) {
                StringBuilder sb = new StringBuilder();
                String user = Config.getInstance().getProxyUser();
                String pass = Config.getInstance().getProxyPass();
                if (!(StringUtils.isNullOrEmptyOrBlank(user) || StringUtils.isNullOrEmptyOrBlank(pass))) {
                    sb.append(user).append(":").append(pass);
                }
                String proxy = "http://" + webproxy.getHost();
                int port = webproxy.getPort();
                if (port > 0 && port != 80) {
                    sb.append(":").append(port);
                }
                if (sb.length() > 0) {
                    sb.append("@");
                }
                sb.append(proxy);
                args.add("--proxy");
                args.add(sb.toString());
            }
            args.add(url);

            ProcessBuilder pb = new ProcessBuilder(args);
            for (String arg : args) {
                Logger.log(arg);
            }

            Logger.log("Writing JSON to: " + tmpOutput);

            pb.redirectError(tmpError);
            pb.redirectOutput(tmpOutput);
            proc = pb.start();
            in = new FileInputStream(tmpOutput);
            videos.addAll(YoutubeDlResponse.parse(in));
            Logger.log("video found: " + videos.size());
        } catch (Exception e) {
            Logger.log(e);
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (Exception ignored) {

            }
            tmpError.delete();
            tmpOutput.delete();
        }
    }

    public ArrayList<YoutubeDlVideo> getVideos() {
        return videos;
    }

    public void stop() {
        try {
            proc.destroy();
        } catch (Exception e) {
            Logger.log(e);
        }
    }
}
