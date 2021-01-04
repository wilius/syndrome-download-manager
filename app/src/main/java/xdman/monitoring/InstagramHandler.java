package xdman.monitoring;

import xdman.XDMApp;
import xdman.downloaders.metadata.HttpMetadata;
import xdman.model.ParsedHookData;
import xdman.util.Logger;
import xdman.util.StringUtils;
import xdman.util.XDMUtils;

import java.io.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class InstagramHandler {
    private static Pattern pattern;

    public static boolean handle(File tempFile, ParsedHookData data) {
        try {
            StringBuffer buf = new StringBuffer();
            InputStream in = new FileInputStream(tempFile);
            BufferedReader r = new BufferedReader(new InputStreamReader(in));
            while (true) {
                String ln = r.readLine();
                if (ln == null) {
                    break;
                }
                buf.append(ln).append("\n");
            }
            in.close();
            Logger.log("Parsing instagram page...");
            if (pattern == null) {
                pattern = Pattern.compile("\"video_url\"\\s*:\\s*\"(.*?)\"");
            }
            Matcher matcher = pattern.matcher(buf);
            if (matcher.find()) {
                String url = matcher.group(1);
                Logger.log("Url: " + url);
                HttpMetadata metadata = new HttpMetadata();
                metadata.setUrl(url);
                metadata.setHeaders(data.getRequestHeadersCollection());
                String file = data.getFile();
                if (StringUtils.isNullOrEmptyOrBlank(file)) {
                    file = XDMUtils.getFileName(data.getUrl());
                }
                String ext = XDMUtils.getExtension(XDMUtils.getFileName(url));
                if (ext != null) {
                    ext = ext.replace(".", "").toUpperCase();
                } else {
                    ext = "";
                }
                XDMApp.getInstance().addMedia(metadata, file + "." + ext, ext);
            }
            return true;
        } catch (Exception e) {
            Logger.log(e);
            return false;
        }
    }
}
