package xdman.videoparser.youtubedl;

import xdman.model.HttpHeader;

import java.util.ArrayList;

class YoutubeDlFormat {
    public String url;
    public String format;
    public String[] fragments;
    public String formatNote;
    public int width;
    public int height;
    public String protocol;
    public String ext;
    public String acodec;
    public String vcodec;
    public int abr;
    public ArrayList<HttpHeader> headers;
}
