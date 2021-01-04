package xdman.videoparser.youtubedl;

import xdman.model.HttpHeader;

import java.util.ArrayList;

public class YoutubeDlMediaFormat {
    public int type;
    public String url;
    public String[] audioSegments, videoSegments;
    public String format;
    public String ext;
    public ArrayList<HttpHeader> headers = new ArrayList<>();
    public ArrayList<HttpHeader> headers2 = new ArrayList<>();
    public int width, height;
    public int abr;

    @Override
    public String toString() {
        return format;
    }
}
