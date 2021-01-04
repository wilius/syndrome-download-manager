package xdman.util;

import xdman.network.http.ChunkedInputStream;
import xdman.model.HeaderCollection;

import java.io.IOException;
import java.io.InputStream;
import java.util.zip.GZIPInputStream;

public class NetUtils {
    public static byte[] getBytes(String str) {
        return str.getBytes();
    }

    public static String readLine(InputStream in) throws IOException {
        StringBuilder buf = new StringBuilder();
        while (true) {
            int x = in.read();
            if (x == -1)
                throw new IOException(
                        "Unexpected EOF while reading header line");
            if (x == '\n')
                return buf.toString();
            if (x != '\r')
                buf.append((char) x);
        }
    }

    public static long getContentLength(HeaderCollection headers) {
        try {
            String contentLength = headers.getValue("content-length");
            if (contentLength != null) {
                return Long.parseLong(contentLength);
            } else {
                contentLength = headers.getValue("content-range");
                if (contentLength != null) {
                    String str = contentLength.split(" ")[1];
                    str = str.split("/")[0];
                    String[] arr = str.split("-");
                    return Long.parseLong(arr[1]) - Long.parseLong(arr[0]) + 1;
                } else {
                    return -1;
                }
            }
        } catch (Exception e) {
            return -1;
        }
    }

    public static InputStream getInputStream(HeaderCollection respHeaders,
                                             InputStream inStream) throws IOException {
        String transferEncoding = respHeaders.getValue("transfer-encoding");
        if (!StringUtils.isNullOrEmptyOrBlank(transferEncoding)) {
            inStream = new ChunkedInputStream(inStream);
        }
        String contentEncoding = respHeaders.getValue("content-encoding");
        Logger.log("Content-Encoding: " + contentEncoding);
        if (!StringUtils.isNullOrEmptyOrBlank(contentEncoding)) {
            if (contentEncoding.equalsIgnoreCase("gzip")) {
                inStream = new GZIPInputStream(inStream);
            } else if (!(contentEncoding.equalsIgnoreCase("none")
                    || contentEncoding.equalsIgnoreCase("identity"))) {
                throw new IOException(
                        "Content Encoding not supported: " + contentEncoding);
            }
        }
        return inStream;
    }

    private static String getExtendedContentDisposition(String header) {
        try {
            String[] arr = header.split(";");
            for (String str : arr) {
                if (str.contains("filename*")) {
                    int index = str.lastIndexOf("'");
                    if (index > 0) {
                        String st = str.substring(index + 1);
                        return XDMUtils.decodeFileName(st);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String getNameFromContentDisposition(String header) {
        try {
            if (header == null)
                return null;
            String headerLow = header.toLowerCase();
            if (headerLow.startsWith("attachment")
                    || headerLow.startsWith("inline")) {
                String name = getExtendedContentDisposition(header);
                if (name != null)
                    return name;
                String[] arr = header.split(";");
                for (String s : arr) {
                    String str = s.trim();
                    if (str.toLowerCase().startsWith("filename")) {
                        int index = str.indexOf('=');
                        String file = str.substring(index + 1).replace("\"", "")
                                .trim();
                        try {
                            return XDMUtils.decodeFileName(file);
                        } catch (Exception e) {
                            return file;
                        }

                    }
                }
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    public static String getCleanContentType(String contentType) {
        if (contentType == null || contentType.length() < 1)
            return contentType;
        int index = contentType.indexOf(";");
        if (index > 0) {
            contentType = contentType.substring(0, index).trim().toLowerCase();
        }
        return contentType;
    }
}
