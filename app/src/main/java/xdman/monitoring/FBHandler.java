package xdman.monitoring;

import xdman.XDMApp;
import xdman.downloaders.metadata.HttpMetadata;
import xdman.model.ParsedHookData;
import xdman.util.Logger;
import xdman.util.StringUtils;
import xdman.util.XDMUtils;

import java.io.*;
import java.util.ArrayList;

public class FBHandler {
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
            Logger.log("Parsing facebook page...");
            ArrayList<String> sdUrls1 = findURL("sd_src", buf);
            ArrayList<String> sdUrls2 = findURL("sd_src_no_ratelimit", buf);
            ArrayList<String> hdUrls1 = findURL("hd_src", buf);
            ArrayList<String> hdUrls2 = findURL("hd_src_no_ratelimit", buf);
            for (String s : sdUrls1) {
                HttpMetadata metadata = new HttpMetadata();
                metadata.setUrl(s);
                metadata.setHeaders(data.getRequestHeadersCollection());
                String file = data.getFile();
                if (StringUtils.isNullOrEmptyOrBlank(file)) {
                    file = XDMUtils.getFileName(data.getUrl());
                }
                XDMApp.getInstance().addMedia(metadata, file + ".mp4", "MP4 LOW");
            }
            for (String s : sdUrls2) {
                HttpMetadata metadata = new HttpMetadata();
                metadata.setUrl(s);
                metadata.setHeaders(data.getRequestHeadersCollection());
                String file = data.getFile();
                if (StringUtils.isNullOrEmptyOrBlank(file)) {
                    file = XDMUtils.getFileName(data.getUrl());
                }
                XDMApp.getInstance().addMedia(metadata, file + ".mp4", "MP4 MEDIUM");
            }
            for (String s : hdUrls1) {
                HttpMetadata metadata = new HttpMetadata();
                metadata.setUrl(s);
                metadata.setHeaders(data.getRequestHeadersCollection());
                String file = data.getFile();
                if (StringUtils.isNullOrEmptyOrBlank(file)) {
                    file = XDMUtils.getFileName(data.getUrl());
                }
                XDMApp.getInstance().addMedia(metadata, file + ".mp4", "MP4 HD");
            }
            for (String s : hdUrls2) {
                HttpMetadata metadata = new HttpMetadata();
                metadata.setUrl(s);
                metadata.setHeaders(data.getRequestHeadersCollection());
                String file = data.getFile();
                if (StringUtils.isNullOrEmptyOrBlank(file)) {
                    file = XDMUtils.getFileName(data.getUrl());
                }
                XDMApp.getInstance().addMedia(metadata, file + ".mp4", "MP4 HQ");
            }
            return true;
        } catch (Exception e) {
            Logger.log(e);
            return false;
        }
    }

    private static ArrayList<String> findURL(String keyword, StringBuffer buf) {
        int index = 0;
        ArrayList<String> urlList = new ArrayList<>();
        while (true) {
            index = buf.indexOf(keyword, index);
            if (index < 0)
                break;
            index += keyword.length();
            index = buf.indexOf(":", index);
            if (index < 0) {
                break;
            }
            index += 1;

            while (true) {
                char ch = buf.charAt(index);
                if (ch == ' ' || ch == '\n' || ch == '\r' || ch == '\t')
                    index++;
                else
                    break;
            }

            char ch = buf.charAt(index);
            if (ch == '"') {
                index++;
                int index3 = buf.indexOf("\"", index);
                String url = decodeJSONEscape(buf.substring(index, index3).trim().replace("\"", ""));
                Logger.log(keyword + ": " + url);
                urlList.add(url);
            }
        }
        return urlList;
    }

    private static String decodeJSONEscape(String json) {
        StringBuilder buf = new StringBuilder();
        int pos = 0;
        while (true) {
            int index = json.indexOf("\\u", pos);
            if (index < 0) {
                if (pos < json.length()) {
                    buf.append(json.substring(pos));
                }
                break;
            }
            buf.append(json, pos, index);
            pos = index;
            String code = json.substring(pos + 2, pos + 2 + 4);
            int char_code = Integer.parseInt(code, 16);
            buf.append((char) char_code);
            pos += 6;
        }
        return buf.toString().replace("\\", "");
    }
}
