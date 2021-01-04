package xdman.util;

import xdman.downloaders.metadata.HttpMetadata;

import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class XDMUtils {
    //private static float dpiScale;
    // private static Map<Integer, String> categoryFolderMap;
    //
    // static {
    // categoryFolderMap = new HashMap<>();
    // categoryFolderMap.put(XDMConstants.DOCUMENTS, "Documents");
    // categoryFolderMap.put(XDMConstants.MUSIC, "Music");
    // categoryFolderMap.put(XDMConstants.VIDEO, "Videos");
    // categoryFolderMap.put(XDMConstants.PROGRAMS, "Programs");
    // categoryFolderMap.put(XDMConstants.COMPRESSED, "Compressed");
    // }
    //
    // public static String getFolderForCategory(int category) {
    // return categoryFolderMap.get(category);
    // }

//	static {
//		//Fixed issue with DPI scaling in Java 9 and higher
//		int dpi = Toolkit.getDefaultToolkit().getScreenResolution();
//		dpiScale = dpi / 96F;
//		//System.out.println("DPI init " + dpiScale);
//	}

    public static final int WINDOWS = 10, MAC = 20, LINUX = 30;
    private static final char[] invalid_chars = {'/', '\\', '"', '?', '*', '<',
            '>', ':', '|'};

    public static String decodeFileName(String str) {
        char[] ch = str.toCharArray();
        StringBuilder buf = new StringBuilder();
        for (int i = 0; i < ch.length; i++) {
            if (ch[i] == '/' || ch[i] == '\\' || ch[i] == '"' || ch[i] == '?'
                    || ch[i] == '*' || ch[i] == '<' || ch[i] == '>'
                    || ch[i] == ':')
                continue;
            if (ch[i] == '%') {
                if (i + 2 < ch.length) {
                    int c = Integer.parseInt(ch[i + 1] + "" + ch[i + 2], 16);
                    buf.append((char) c);
                    i += 2;
                    continue;
                }
            }
            buf.append(ch[i]);
        }
        return buf.toString();
    }

    public static String getFileName(String uri) {
        try {
            if (uri == null)
                return "FILE";
            if (uri.equals("/") || uri.length() < 1) {
                return "FILE";
            }
            int x = uri.lastIndexOf("/");
            String path = uri;
            if (x > -1) {
                path = uri.substring(x);
            }
            int qindex = path.indexOf("?");
            if (qindex > -1) {
                path = path.substring(0, qindex);
            }
            path = decodeFileName(path);
            if (path.length() < 1)
                return "FILE";
            if (path.equals("/"))
                return "FILE";
            return createSafeFileName(path);
        } catch (Exception e) {
            Logger.log(e);
            return "FILE";
        }
    }

    public static String createSafeFileName(String str) {
        String safe_name = str;
        for (char invalid_char : invalid_chars) {
            if (safe_name.indexOf(invalid_char) != -1) {
                safe_name = safe_name.replace(invalid_char, '_');
            }
        }
        return safe_name;
    }

    public static boolean validateURL(String url) {
        try {
            url = url.toLowerCase();
            if (url.startsWith("http://") || url.startsWith("https://")
                    || url.startsWith("ftp://")) {
                new URL(url);
                return true;
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    public static String appendArray2Str(String[] arr) {
        boolean first = true;
        StringBuilder buf = new StringBuilder();
        for (String s : arr) {
            if (!first) {
                buf.append(",");
            }
            buf.append(s);
            first = false;
        }
        return buf.toString();
    }

    public static String[] appendStr2Array(String str) {
        String[] arr = str.split(",");
        ArrayList<String> arrList = new ArrayList<>();
        for (String s : arr) {
            String txt = s.trim();
            if (txt.length() > 0) {
                arrList.add(txt);
            }
        }
        arr = new String[arrList.size()];
        return arrList.toArray(arr);
    }

    public static String getExtension(String file) {
        int index = file.lastIndexOf(".");
        if (index > 0) {
            return file.substring(index);
        } else {
            return null;
        }
    }

    public static String getFileNameWithoutExtension(String fileName) {
        int index = fileName.lastIndexOf(".");
        if (index > 0) {
            fileName = fileName.substring(0, index);
            return fileName;
        } else {
            return fileName;
        }
    }

    public static void copyStream(InputStream instream, OutputStream outstream,
                                  long size) throws Exception {
        byte[] b = new byte[8192];
        long rem = size;
        while (true) {
            int bs = (int) (size > 0 ? (rem > b.length ? b.length : rem)
                    : b.length);
            int x = instream.read(b, 0, bs);
            if (x == -1) {
                if (size > 0) {
                    throw new EOFException("Unexpected EOF");
                } else {
                    break;
                }
            }
            outstream.write(b, 0, x);
            rem -= x;
            if (size > 0) {
                if (rem <= 0)
                    break;
            }
        }
    }

    public static int detectOS() {
        String os = System.getProperty("os.name").toLowerCase(Locale.ENGLISH);
        if (os.contains("mac") || os.contains("darwin")
                || os.contains("os x")) {
            return MAC;
        } else if (os.contains("linux")) {
            return LINUX;
        } else if (os.contains("windows")) {
            return WINDOWS;
        } else {
            return -1;
        }
    }

    public static boolean exec(String args) {
        try {
            Logger.log("Launching: " + args);
            Runtime.getRuntime().exec(args);
        } catch (IOException e) {
            Logger.log(e);
            return false;
        }
        return true;
    }

    public static void mkdirs(String folder) {
        File outFolder = new File(folder);
        if (!outFolder.exists()) {
            outFolder.mkdirs();
        }
    }

    public static List<HttpMetadata> toMetadata(List<String> urls) {
        List<HttpMetadata> list = new ArrayList<>();
        for (String url : urls) {
            HttpMetadata md = new HttpMetadata();
            md.setUrl(url);
            list.add(md);
        }
        return list;
    }


    public static int getScaledInt(int value) {
        return value;
        //System.err.println("in: " + value);
//		if (dpiScale == 0.0f) {
//			int dpi = Toolkit.getDefaultToolkit().getScreenResolution();
//			dpiScale = dpi / 96;
//		}
        //System.err.println("out: " + (value * dpiScale) + " dpi: " + dpiScale);
        //return (int) (value * dpiScale);
    }

    /*
     * public static final int getScaledInt(int size) { detectScreenType();
     * return (int) (size * getScaleFactor()); }
     */

    public static String readLineSafe(BufferedReader r)
            throws IOException {
        String ln = r.readLine();
        if (ln == null) {
            throw new IOException("Unexpected EOF");
        }
        return ln;
    }

}