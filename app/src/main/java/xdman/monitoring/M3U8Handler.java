package xdman.monitoring;

import xdman.XDMApp;
import xdman.downloaders.hls.HlsPlaylist;
import xdman.downloaders.hls.HlsPlaylistItem;
import xdman.downloaders.hls.PlaylistParser;
import xdman.downloaders.metadata.HlsMetadata;
import xdman.model.ParsedHookData;
import xdman.util.StringUtils;
import xdman.util.XDMUtils;

import java.io.File;
import java.util.List;

public class M3U8Handler {
    public static boolean handle(File m3u8file, ParsedHookData data) {
        try {
            System.out.println("Handing manifest: ...");
            HlsPlaylist playlist = PlaylistParser.parse(m3u8file.getAbsolutePath(), data.getUrl());
            if (playlist == null) {
                System.out.println("Playlist empty");
                return true;
            }

            if (!playlist.isMaster()) {
                if (playlist.getItems() != null && playlist.getItems().size() > 0) {
                    HlsMetadata metadata = new HlsMetadata();
                    metadata.setUrl(data.getUrl());
                    metadata.setHeaders(data.getRequestHeadersCollection());
                    String file = data.getFile();
                    if (StringUtils.isNullOrEmptyOrBlank(file)) {
                        file = XDMUtils.getFileName(data.getUrl());
                    }
                    System.out.println("adding media");
                    XDMApp.getInstance().addMedia(metadata, file + ".ts", "HLS");
                }
            } else {
                List<HlsPlaylistItem> items = playlist.getItems();
                if (items != null) {
                    for (HlsPlaylistItem item : items) {
                        String url = item.getUrl();
                        HlsMetadata metadata = new HlsMetadata();
                        metadata.setUrl(url);
                        metadata.setHeaders(data.getRequestHeadersCollection());
                        String file = data.getFile();
                        if (StringUtils.isNullOrEmptyOrBlank(file)) {
                            file = XDMUtils.getFileName(data.getUrl());
                        }
                        StringBuilder infoStr = new StringBuilder();
                        if (!StringUtils.isNullOrEmptyOrBlank(item.getBandwidth())) {
                            infoStr.append(item.getBandwidth());
                        }
                        if (infoStr.length() > 0) {
                            infoStr.append(" ");
                        }
                        if (!StringUtils.isNullOrEmptyOrBlank(item.getResolution())) {
                            infoStr.append(item.getResolution());
                        }
                        System.out.println("adding media");
                        XDMApp.getInstance().addMedia(metadata, file + ".ts", infoStr.toString());
                    }
                }
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}
