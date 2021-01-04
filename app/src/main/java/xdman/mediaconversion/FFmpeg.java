package xdman.mediaconversion;

import xdman.util.Logger;
import xdman.util.StringUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static xdman.os.OperationSystem.OS;

public class FFmpeg {
    public final static int FF_NOT_FOUND = 10, FF_LAUNCH_ERROR = 20, FF_CONVERSION_FAILED = 30, FF_SUCCESS = 0;
    private final MediaFormat outFormat;
    private final MediaConversionListener listener;
    private final boolean copy;
    private final List<String> inputFiles;
    private final String outputFile;
    private boolean hls;
    private long totalDuration = 0;
    private Process proc;
    private int ffExitCode;
    private String volume;
    private boolean useHwAccel;

    public FFmpeg(List<String> inputFiles, String outputFile, MediaConversionListener listener, MediaFormat outFormat,
                  boolean copy) {
        this.inputFiles = inputFiles;
        this.outputFile = outputFile;
        this.listener = listener;
        this.outFormat = outFormat;
        this.copy = copy;
    }

    public int convert() {
        try {
            Logger.log("Outformat: " + outFormat);
            File ffmpegFile = OS.getFFMPEGFile();
            if (ffmpegFile == null) {
                return FF_NOT_FOUND;
            }

            List<String> args = new ArrayList<>();
            args.add(ffmpegFile.getAbsolutePath());

            if (useHwAccel) {
                args.add("-hwaccel");
                args.add("auto");
            }

            if (hls) {
                args.add("-f");
                args.add("concat");
                args.add("-safe");
                args.add("0");
            }

            for (String inputFile : inputFiles) {
                args.add("-i");
                args.add(inputFile);
            }

            if (copy) {
                args.add("-acodec");
                args.add("copy");
                args.add("-vcodec");
                args.add("copy");
            } else {
                if (outFormat.getResolution() != null) {
                    args.add("-s");
                    args.add(outFormat.getResolution());
                }
                if (outFormat.getVideo_codec() != null) {
                    args.add("-vcodec");
                    args.add(outFormat.getVideo_codec());
                }
                if (outFormat.getVideo_bitrate() != null) {
                    args.add("-b:v");
                    args.add(outFormat.getVideo_bitrate());
                }
                if (outFormat.getFramerate() != null) {
                    args.add("-r");
                    args.add(outFormat.getFramerate());
                }
                if (outFormat.getAspectRatio() != null) {
                    args.add("-aspect");
                    args.add(outFormat.getAspectRatio());
                }
                if (outFormat.getVideo_param_extra() != null) {
                    String[] arr = outFormat.getVideo_param_extra().split(" ");
                    if (arr.length > 0) {
                        args.addAll(Arrays.asList(arr));
                    }
                } else {
                    if ("libx264".equals(outFormat.getVideo_codec())) {
                        args.add("-profile:v");
                        args.add("baseline");
                    }
                }

                if (outFormat.getAudio_codec() != null) {
                    args.add("-acodec");
                    args.add(outFormat.getAudio_codec());
                }
                if (outFormat.getAudio_bitrate() != null) {
                    args.add("-b:a");
                    args.add(outFormat.getAudio_bitrate());
                }
                if (isNumeric(outFormat.getSamplerate())) {
                    args.add("-ar");
                    args.add(outFormat.getSamplerate());
                }
                if (isNumeric(outFormat.getAudio_channel())) {
                    args.add("-ac");
                    args.add(outFormat.getAudio_channel());
                }
                if (outFormat.getAudio_extra_param() != null) {
                    String[] arr = outFormat.getAudio_extra_param().split(" ");
                    if (arr.length > 0) {
                        args.addAll(Arrays.asList(arr));
                    }
                }
                if (volume != null) {
                    args.add("-filter:a");
                    args.add("volume=" + volume);
                }
            }

            args.add(outputFile);
            args.add("-y");

            for (String s : args) {
                Logger.log("@ffmpeg_args: " + s);
            }

            ProcessBuilder pb = new ProcessBuilder(args);
            pb.redirectErrorStream(true);
            proc = pb.start();

            BufferedReader br = new BufferedReader(new InputStreamReader(proc.getInputStream()), 1024);
            while (true) {
                String ln = br.readLine();
                if (ln == null) {
                    break;
                }
                try {
                    String text = ln.trim();
                    processOutput(text);
                } catch (Exception e) {
                    Logger.log(e);
                }
            }

            ffExitCode = proc.waitFor();
            return ffExitCode == 0 ? FF_SUCCESS : FF_CONVERSION_FAILED;
        } catch (RuntimeException | InterruptedException | IOException e) {
            return FF_LAUNCH_ERROR;
        }
    }

    public void setHls(boolean hls) {
        this.hls = hls;
    }

    public void setHLSDuration(float totalDuration) {
        this.totalDuration = (long) totalDuration;
    }

    private long parseDuration(String dur) {
        long duration = 0;
        String[] arr = dur.split(":");
        String s = arr[0].trim();
        if (!StringUtils.isNullOrEmpty(s)) {
            duration = Integer.parseInt(s, 10) * 3600L;
        }
        s = arr[1].trim();
        if (!StringUtils.isNullOrEmpty(s)) {
            duration += Integer.parseInt(arr[1].trim(), 10) * 60L;
        }
        s = arr[2].split("\\.")[0].trim();
        if (!StringUtils.isNullOrEmpty(s)) {
            duration += Integer.parseInt(s, 10);
        }
        return duration;
    }

    private void processOutput(String text) {
        if (StringUtils.isNullOrEmpty(text)) {
            return;
        }
        if (totalDuration > 0) {
            if (text.startsWith("frame=") && text.contains("time=")) {
                int index1 = text.indexOf("time");
                index1 = text.indexOf('=', index1);
                int index2 = text.indexOf("bitrate=");
                String dur = text.substring(index1 + 1, index2).trim();
                Logger.log("Parsing duration: " + dur);
                long t = parseDuration(dur);
                Logger.log("Duration: " + t + " Total duration: " + totalDuration);
                int prg = (int) ((t * 100) / totalDuration);
                Logger.log("ffmpeg prg: " + prg);
                listener.progress(prg);
            }
        }

        if (totalDuration == 0) {
            if (text.startsWith("Duration:")) {
                try {
                    int index1 = text.indexOf("Duration");
                    index1 = text.indexOf(':', index1);
                    int index2 = text.indexOf(",", index1);
                    String dur = text.substring(index1 + 1, index2).trim();
                    Logger.log("Parsing duration: " + dur);
                    totalDuration = parseDuration(dur);
                    Logger.log("Total duration: " + totalDuration);
                } catch (Exception e) {
                    Logger.log(e);
                    totalDuration = -1;
                }
            }
        }
    }

    public void stop() {
        try {
            if (proc.isAlive()) {
                proc.destroy();
            }
        } catch (Exception ignored) {
        }
    }

    public int getFfExitCode() {
        return ffExitCode;
    }

    public String getVolume() {
        return volume;
    }

    public void setVolume(String volume) {
        this.volume = volume;
    }

    private boolean isNumeric(String s) {
        try {
            Double.parseDouble(s);
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    public void setUseHwAccel(boolean useHwAccel) {
        this.useHwAccel = useHwAccel;
    }
}
