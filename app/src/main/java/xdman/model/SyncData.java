package xdman.model;

import java.util.List;

public class SyncData {
    private boolean enabled;
    private List<String> blockedHosts;
    private List<String> videoUrls;
    private List<String> fileExts;
    private List<String> vidExts;
    private List<VideoItem> vidList;
    private List<String> mimeList;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public List<String> getBlockedHosts() {
        return blockedHosts;
    }

    public void setBlockedHosts(List<String> blockedHosts) {
        this.blockedHosts = blockedHosts;
    }

    public List<String> getVideoUrls() {
        return videoUrls;
    }

    public void setVideoUrls(List<String> videoUrls) {
        this.videoUrls = videoUrls;
    }

    public List<String> getFileExts() {
        return fileExts;
    }

    public void setFileExts(List<String> fileExts) {
        this.fileExts = fileExts;
    }

    public List<String> getVidExts() {
        return vidExts;
    }

    public void setVidExts(List<String> vidExts) {
        this.vidExts = vidExts;
    }

    public List<VideoItem> getVidList() {
        return vidList;
    }

    public void setVidList(List<VideoItem> vidList) {
        this.vidList = vidList;
    }

    public List<String> getMimeList() {
        return mimeList;
    }

    public void setMimeList(List<String> mimeList) {
        this.mimeList = mimeList;
    }

    public static class VideoItem {
        private String id;
        private String text;
        private String info;

        public VideoItem() {
        }

        public VideoItem(String id, String text, String info) {
            this.id = id;
            this.text = text;
            this.info = info;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }

        public String getInfo() {
            return info;
        }

        public void setInfo(String info) {
            this.info = info;
        }
    }
}
