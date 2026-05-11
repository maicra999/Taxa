package cc.maicra999.taxa.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.jetbrains.annotations.NotNull;

public final class PathUtil {

    // Private constructor to prevent instantiation
    private PathUtil() {}

    public static @NotNull Path getOrCreateDirectory(Path path) {
        try {
            if (path.toFile().exists()) {
                if (path.toFile().isDirectory()) {
                    return path;
                } else {
                    throw new IllegalStateException("Path exists but is not a directory: " + path);
                }
            } else {
                return Files.createDirectories(path);
            }
        } catch (IOException e) {
            SneakyThrow.sneakyThrow(e);
            return null;
        }
    }

    public static @NotNull Path getOrCreateSubdirectory(Path parent, String child) {
        return getOrCreateDirectory(parent.resolve(child));
    }
}
