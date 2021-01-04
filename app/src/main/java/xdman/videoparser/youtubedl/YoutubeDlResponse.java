package xdman.videoparser.youtubedl;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import xdman.model.HttpHeader;
import xdman.util.FormatUtilities;
import xdman.util.Logger;
import xdman.util.StringUtils;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class YoutubeDlResponse {
    public static final int DASH_HTTP = 99;
    public static final int HTTP = 98;
    public static final int HLS = 97;
    public static final int HDS = 96;

    private static final int DASH_VIDEO_ONLY = 23;
    private static final int DASH_AUDIO_ONLY = 24;

    @SuppressWarnings("unchecked")
    public static ArrayList<YoutubeDlVideo> parse(InputStream in) throws Exception {
        JSONParser parser = new JSONParser();
        JSONObject obj = (JSONObject) parser.parse(new InputStreamReader(in, StandardCharsets.UTF_8));
        JSONArray entries = (JSONArray) obj.get("entries");
        if (entries == null) {
            // its a playlist
            Logger.log("no playlist entry");
            entries = new JSONArray();
            entries.add(obj);
        }
        ArrayList<YoutubeDlVideo> playList = new ArrayList<>();
        for (Object entry : entries) {
            JSONObject jsobj = (JSONObject) entry;
            if (jsobj != null) {
                YoutubeDlVideo v = getPlaylistEntry(jsobj);
                if (v != null) {
                    playList.add(v);
                } else {
                    Logger.log("Parsing failed");
                }
            }

        }
        Logger.log("Playlist size: " + playList.size());
        return playList;
    }

    public static YoutubeDlVideo getPlaylistEntry(JSONObject obj) {
        if (obj == null) {
            return null;
        }
        List<YoutubeDlFormat> formatList = new ArrayList<>();
        JSONArray formats = (JSONArray) obj.get("formats");
        if (formats != null) {
            for (Object o : formats) {
                Logger.log("Parsing format info");
                JSONObject formatObj = (JSONObject) o;
                String protocol = getString(formatObj.get("protocol"));
                YoutubeDlFormat format = new YoutubeDlFormat();
                format.protocol = protocol;
                format.url = getString(formatObj.get("url"));
                format.acodec = getString(formatObj.get("acodec"));
                format.vcodec = getString(formatObj.get("vcodec"));
                format.width = getInt(formatObj.get("width"));
                format.height = getInt(formatObj.get("height"));
                format.ext = getString(formatObj.get("ext"));
                if ("mpd".equalsIgnoreCase(format.ext)) {
                    continue;
                }
                format.formatNote = getString(formatObj.get("format_note"));
                format.format = getString(formatObj.get("format"));
                String sabr = formatObj.get("abr") + "";
                try {
                    format.abr = Integer.parseInt(sabr);
                } catch (Exception e) {
                    format.abr = -1;
                }

                JSONObject jsHeaders = (JSONObject) formatObj.get("http_headers");
                if (jsHeaders != null) {
                    format.headers = new ArrayList<>();
                    for (String key : (Iterable<String>) jsHeaders.keySet()) {
                        String value = (String) jsHeaders.get(key);
                        format.headers.add(new HttpHeader(key, value));
                    }
                }
                if (protocol.equals("http_dash_segments")) {
                    String baseUrl = (String) formatObj.get("fragment_base_url");
                    JSONArray fragmentArr = (JSONArray) formatObj.get("fragments");
                    String[] fragments = new String[fragmentArr.size()];
                    for (int j = 0; j < fragmentArr.size(); j++) {
                        JSONObject frag = (JSONObject) fragmentArr.get(j);
                        String url = (String) frag.get("url");
                        fragments[j] = Objects.requireNonNullElseGet(url, () -> baseUrl + frag.get("path"));
                    }
                    format.fragments = fragments;
                }
                formatList.add(format);
            }
        } else {
            String url = getString(obj.get("url"));
            if (url != null) {
                YoutubeDlFormat format = new YoutubeDlFormat();
                format.protocol = getString(obj.get("protocol"));
                format.url = url;
                format.acodec = getString(obj.get("acodec"));
                format.vcodec = getString(obj.get("vcodec"));
                try {
                    format.width = getInt(obj.get("width"));
                } catch (Exception e) {
                    format.width = -1;
                }
                try {
                    format.height = getInt(obj.get("height"));
                } catch (Exception e) {
                    format.width = -1;
                }

                format.ext = getString(obj.get("ext"));
                format.formatNote = getString(obj.get("format_note"));
                format.format = getString(obj.get("format"));
                String sabr = obj.get("abr") + "";
                try {
                    format.abr = Integer.parseInt(sabr);
                } catch (Exception e) {
                    format.abr = -1;
                }
                formatList.add(format);
            }
        }

        Logger.log("Format list count: " + formatList.size());

        ArrayList<YoutubeDlMediaFormat> mediaList = new ArrayList<>();

        for (int i = 0; i < formatList.size(); i++) {
            YoutubeDlFormat fmt = formatList.get(i);
            if (fmt.protocol.equals("http_dash_segments")) {
                continue;
            }
            int type = getVideoType(fmt);
            if (type == DASH_VIDEO_ONLY) {
                for (YoutubeDlFormat fmt2 : formatList) {
                    int type2 = getVideoType(fmt2);
                    if (type2 == DASH_AUDIO_ONLY) {
                        YoutubeDlMediaFormat media = new YoutubeDlMediaFormat();
                        media.type = DASH_HTTP;
                        if (fmt.protocol.equals(fmt2.protocol)) {
                            media.audioSegments = new String[1];
                            media.audioSegments[0] = fmt2.url;
                            media.abr = fmt2.abr;
                            media.videoSegments = new String[1];
                            media.videoSegments[0] = fmt.url;
                            if (fmt.headers != null) {
                                media.headers.addAll(fmt.headers);
                            }
                            if (fmt2.headers != null) {
                                media.headers2.addAll(fmt2.headers);
                            }

                            if (((fmt.ext + "").equals(fmt2.ext + ""))
                                    || ((fmt.ext + "").equals("mp4") && (fmt2.ext + "").equals("m4a"))) {
                                media.ext = fmt.ext;
                            } else {
                                media.ext = "mkv";
                            }
                            media.width = fmt.width;
                            media.height = fmt.height;
                            media.format = createFormat(media.ext, fmt2.acodec, fmt.vcodec,
                                    fmt.height, fmt2.abr);
                            System.out.println(media.format + " " + media.url);
                            checkAndAddMedia(media, mediaList);
                        }
                    }
                }
            } else if (type != DASH_AUDIO_ONLY) {
                YoutubeDlMediaFormat media = new YoutubeDlMediaFormat();
                if ("m3u8".equals(fmt.protocol) || "m3u8_native".equals(fmt.protocol)) {
                    media.type = HLS;
                } else if ("f4m".equals(fmt.protocol)) {
                    media.type = HDS;
                } else if ("http".equals(fmt.protocol) || "https".equals(fmt.protocol)) {
                    media.type = HTTP;
                } else {
                    Logger.log("unsupported protocol: " + fmt.protocol);
                    continue;
                }
                media.url = fmt.url;
                media.ext = fmt.ext;
                media.width = fmt.width;
                media.height = fmt.height;

                media.format = createFormat(media.ext, fmt.acodec, fmt.vcodec, fmt.height,
                        -1);
                System.out.println(media.format + " " + media.url);
                if (fmt.headers != null) {
                    media.headers.addAll(fmt.headers);
                }

                checkAndAddMedia(media, mediaList);
            }
        }
        Logger.log("VIDEO----" + obj.get("title"));
        for (YoutubeDlMediaFormat ydlMediaFormat : mediaList) {
            Logger.log(ydlMediaFormat.type + " " + ydlMediaFormat.format);
        }

        YoutubeDlVideo pl = new YoutubeDlVideo();
        pl.mediaFormats.addAll(mediaList);
        pl.mediaFormats.sort((o1, o2) -> {
            if (o1.width > o2.width) {
                return -1;
            }

            if (o1.width < o2.width) {
                return 1;
            }
            return Integer.compare(o2.abr, o1.abr);
        });
        String stitle = (String) obj.get("title");
        if (!StringUtils.isNullOrEmptyOrBlank(stitle)) {
            pl.title = stitle;
        }

        String thumbnail = (String) obj.get("thumbnail");
        if (thumbnail != null) {
            if (!(thumbnail.equals("none") || thumbnail.equals("null"))) {
                pl.thumbnail = thumbnail;
            }
        }

        if (pl.thumbnail == null) {
            JSONArray thumbnails = (JSONArray) obj.get("thumbnails");
            if (thumbnails != null) {
                for (Object o : thumbnails) {
                    Logger.log("Parsing thumbnails info");
                    JSONObject thumbnailObj = (JSONObject) o;
                    thumbnail = (String) thumbnailObj.get("url");
                    if (thumbnail != null) {
                        if (!(thumbnail.equals("none") || thumbnail.equals("null"))) {
                            pl.thumbnail = thumbnail;
                            break;
                        }
                    }
                }
            }
        }

        String sdur = (obj.get("duration") + "");
        if (!(sdur.equals("none") || sdur.equals("null"))) {
            try {
                pl.duration = Long.parseLong(sdur);
            } catch (Exception e) {
                pl.duration = -1;
            }
        }

        return pl;
    }

    private static void checkAndAddMedia(YoutubeDlMediaFormat fmt, ArrayList<YoutubeDlMediaFormat> mediaList) {
        for (YoutubeDlMediaFormat m : mediaList) {
            if (fmt.type == m.type) {
                if (fmt.type == DASH_HTTP) {
                    boolean sameAudio = false;
                    boolean sameVideo = false;
                    if (fmt.audioSegments == null) {
                        if (m.audioSegments == null) {
                            sameAudio = true;
                        }
                    } else {
                        if (m.audioSegments != null) {
                            if (fmt.audioSegments.length == m.audioSegments.length) {
                                sameAudio = true;
                                for (int j = 0; j < fmt.audioSegments.length; j++) {
                                    if (!fmt.audioSegments[j].equals(m.audioSegments[j])) {
                                        sameAudio = false;
                                        break;
                                    }
                                }
                            }
                        }
                    }
                    if (fmt.videoSegments == null) {
                        if (m.videoSegments == null) {
                            sameVideo = true;
                        }
                    } else {
                        if (m.videoSegments != null) {
                            if (fmt.videoSegments.length == m.videoSegments.length) {
                                sameVideo = true;
                                for (int j = 0; j < fmt.videoSegments.length; j++) {
                                    if (!fmt.videoSegments[j].equals(m.videoSegments[j])) {
                                        sameVideo = false;
                                        break;
                                    }
                                }
                            }
                        }
                    }
                    if (sameAudio && sameVideo) {
                        return;
                    }
                } else {
                    if (m.url.equals(fmt.url)) {
                        return;
                    }
                }
            }

        }

        mediaList.add(fmt);
    }

    private static int getVideoType(YoutubeDlFormat fmt) {

        String fmtNote = null;
        String acodec = null;
        String vcodec = null;
        if (fmt.formatNote != null) {
            fmtNote = fmt.formatNote.toLowerCase();
            if (fmtNote.equals("none") || fmtNote.length() < 1) {
                fmtNote = null;
            }
        }
        if (fmtNote == null) {
            fmtNote = "";
        }
        if (fmt.acodec != null) {
            acodec = fmt.acodec.toLowerCase();
            if (acodec.equals("none") || acodec.length() < 1) {
                acodec = null;
            }
        }
        if (fmt.vcodec != null) {
            vcodec = fmt.vcodec.toLowerCase();
            if (vcodec.equals("none") || vcodec.length() < 1) {
                vcodec = null;
            }
        }

        if (fmtNote.contains("dash audio")) {
            return DASH_AUDIO_ONLY;
        }
        if (fmtNote.contains("dash video")) {
            return DASH_VIDEO_ONLY;
        }
        if (acodec == null && vcodec == null) {
            return -1;
        }
        if (acodec != null && vcodec != null) {
            return -1;
        }
        if (acodec != null) {
            return DASH_AUDIO_ONLY;
        }
        return DASH_VIDEO_ONLY;
    }

    private static int getInt(Object obj) {
        if (obj == null) {
            return -1;
        }
        if (obj.toString().contains("none"))
            return -1;
        return Integer.parseInt(obj + "");
    }

    private static String getString(Object obj) {
        return (String) obj;
    }

    public static String nvl(String str) {
        if (str == null)
            return "";
        return str;
    }

    public static String createFormat(String ext, String acodec, String vcodec, int height, int abr) {
        StringBuilder sb = new StringBuilder();
        ext = nvl(ext);
        if (ext.length() > 0) {
            sb.append(ext.toUpperCase());
        }

        if (height > 0) {
            if (sb.length() > 0)
                sb.append(" ");
            sb.append(height).append("p");
        }

        if (abr > 0) {
            if (sb.length() > 0)
                sb.append(" ");
            sb.append(abr).append("k");
        }

        acodec = nvl(acodec);
        if (acodec.contains("none")) {
            acodec = "";
        }

        vcodec = nvl(vcodec);
        if (vcodec.contains("none")) {
            vcodec = "";
        }

        if (acodec.length() > 0) {
            if (sb.length() > 0)
                sb.append(" ");
            sb.append(FormatUtilities.getFriendlyCodec(acodec));
        }

        if (vcodec.length() > 0) {
            if (sb.length() > 0) {
                if (acodec.length() > 0) {
                    sb.append("/");
                } else {
                    sb.append(" ");
                }
            }
            sb.append(FormatUtilities.getFriendlyCodec(vcodec));
        }

        return sb.toString();
    }

}