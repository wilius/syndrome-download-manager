package xdman;

import xdman.enums.FileCategory;
import xdman.util.FormatUtilities;
import xdman.util.StringUtils;

public class DownloadEntry {
    private String id, file, folder;
    private int state;
    private FileCategory category;
    private long size, downloaded;
    private long date;
    private int progress;
    private String dateStr;
    private String queueId;
    private boolean startedByUser;
    private int outputFormatIndex;// 0 orginal
    private String tempFolder;

    public DownloadEntry() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDateStr() {
        return dateStr;
    }

    public final void setDateStr(String dateStr) {
        this.dateStr = dateStr;
    }

    public String getFile() {
        return file;
    }

    public void setFile(String file) {
        this.file = file;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public FileCategory getCategory() {
        return category;
    }

    public void setCategory(FileCategory category) {
        this.category = category;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public long getDownloaded() {
        return downloaded;
    }

    public void setDownloaded(long downloaded) {
        this.downloaded = downloaded;
    }

    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
        this.dateStr = FormatUtilities.formatDate(date);
    }

    public int getProgress() {
        return progress;
    }

    public void setProgress(int progress) {
        this.progress = progress;
    }

    String getFolder() {
        return folder;
    }

    public void setFolder(String folder) {
        this.folder = folder;
    }

    public final String getQueueId() {
        return queueId;
    }

    public final void setQueueId(String queueId) {
        this.queueId = queueId;
    }

    public final boolean isStartedByUser() {
        return startedByUser;
    }

    public final void setStartedByUser(boolean startedByUser) {
        this.startedByUser = startedByUser;
    }

    public final int getOutputFormatIndex() {
        return outputFormatIndex;
    }

    public final void setOutputFormatIndex(int outputFormatIndex) {
        this.outputFormatIndex = outputFormatIndex;
    }

    public String getTempFolder() {
        if (StringUtils.isNullOrEmptyOrBlank(tempFolder)) {
            tempFolder = Config.getInstance().getTemporaryFolder();
        }
        return tempFolder;
    }

    public void setTempFolder(String tempFolder) {
        this.tempFolder = tempFolder;
    }
}
