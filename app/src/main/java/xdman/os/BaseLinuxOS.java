package xdman.os;

public abstract class BaseLinuxOS extends OperationSystem {
    @Override
    String getFfmpegFileName() {
        return "ffmpeg";
    }

    @Override
    String getYoutubeDlFileName() {
        return "youtube-dl";
    }
}
