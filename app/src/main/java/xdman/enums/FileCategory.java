package xdman.enums;

import java.util.List;

public enum FileCategory {
    DOCUMENT(".doc", ".docx", ".txt", ".pdf", ".rtf", ".xml",
            ".c", ".cpp", ".java", ".cs", ".vb", ".html", ".htm",
            ".chm", ".xls", ".xlsx", ".ppt", ".pptx", ".js", ".css"),
    COMPRESSED(".7z", ".zip", ".rar", ".gz", ".tgz", ".tbz2", ".bz2",
            ".lzh", ".sit", ".z"),
    MUSIC(".mp3", ".wma", ".ogg", ".aiff", ".au", ".mid", ".midi",
            ".mp2", ".mpa", ".wav", ".aac", ".oga", ".ogx", ".ogm",
            ".spx", ".opus"),
    VIDEO(".mpg", ".mpeg", ".avi", ".flv", ".asf", ".mov", ".mpe",
            ".wmv", ".mkv", ".mp4", ".3gp", ".divx", ".vob", ".webm",
            ".ts"),
    APP(".exe", ".msi", ".bin", ".sh", ".deb", ".cab", ".cpio",
            ".dll", ".jar", "rpm", ".run", ".py"),
    OTHER();

    private final List<String> extensions;

    FileCategory(String... extensions) {
        this.extensions = List.of(extensions);
    }

    public static FileCategory find(String filename) {
        String file = filename.toLowerCase();
        for (FileCategory value : values()) {
            for (String extension : value.extensions) {
                if (file.endsWith(extension)) {
                    return value;
                }
            }
        }

        return OTHER;
    }
}
