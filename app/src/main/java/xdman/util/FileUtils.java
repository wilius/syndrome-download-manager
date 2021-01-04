package xdman.util;

import xdman.Config;

import java.io.File;

public class FileUtils {
    private FileUtils() {
    }

    public static void deleteTempFile(String id) {
        Logger.log("Deleting metadata for " + id);
        File mf = new File(Config.getInstance().getMetadataFolder(), id);
        boolean deleted = mf.delete();
        Logger.log("Deleted manifest " + id + " " + deleted);
        File df = new File(Config.getInstance().getTemporaryFolder(), id);
        File[] files = df.listFiles();
        if (files != null && files.length > 0) {
            for (File f : files) {
                deleted = f.delete();
                Logger.log("Deleted tmp file " + id + " " + deleted);
            }
        }
        deleted = df.delete();
        Logger.log("Deleted tmp folder " + id + " " + deleted);
    }
}
