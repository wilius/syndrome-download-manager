package xdman.monitoring;

import xdman.XDMApp;
import xdman.downloaders.metadata.HttpMetadata;
import xdman.model.ParsedHookData;
import xdman.util.Logger;
import xdman.util.StringUtils;
import xdman.util.XDMUtils;

import java.io.*;

public class VimeoHandler {

    public static boolean handle(File tempFile, ParsedHookData data) {
        try {
            StringBuilder buf = new StringBuilder();
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
            String keyword = "\"progressive\"";
            int index = buf.indexOf(keyword);
            if (index < 0) {
                return false;
            }
            index += keyword.length();
            index = buf.indexOf(":", index);
            if (index < 0) {
                return false;
            }
            index++;
            index = buf.indexOf("[", index);
            if (index < 0) {
                return false;
            }
            index++;
            int start = index;
            index = buf.indexOf("]", index);
            if (index < 0) {
                return false;
            }
            String str = buf.substring(start, index);
            index = 0;
            while (index != -1) {
                index = str.indexOf("{", index);
                if (index > -1) {
                    index++;
                    start = index;
                    index = str.indexOf("}", index);
                    if (index > -1) {
                        String s = str.substring(start, index);
                        processString(s, data);
                    }
                }
            }
        } catch (Exception e) {
            Logger.log(e);
        }
        return false;
    }

    private static void processString(String str, ParsedHookData data) {
        String quality = "", type = "", url = "";
        String[] arr = str.split(",");
        for (String s : arr) {
            int index = s.indexOf(":");
            if (index > 0) {
                String key = s.substring(0, index).replace("\"", "");
                String val = s.substring(index + 1).replace("\"", "");
                if (key.equals("url")) {
                    url = val;
                    Logger.log(url);
                }
                if (key.equals("quality")) {
                    quality = val;
                    Logger.log(quality);
                }
                if (key.equals("mime")) {
                    type = val;
                    Logger.log(type);
                }
            }
        }
        String ext = "mp4";
        if (type.contains("video/mp4")) {
            ext = "mp4";
        } else if (type.contains("video/webm")) {
            ext = "webm";
        }
        HttpMetadata metadata = new HttpMetadata();
        metadata.setUrl(url);
        metadata.setHeaders(data.getRequestHeadersCollection());
        String file = data.getFile();
        if (StringUtils.isNullOrEmptyOrBlank(file)) {
            file = XDMUtils.getFileName(data.getUrl());
        }
        XDMApp.getInstance().addMedia(metadata, file + "." + ext, ext.toUpperCase() + " " + quality);
    }
}
