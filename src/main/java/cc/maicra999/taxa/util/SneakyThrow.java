package cc.maicra999.taxa.util;

public final class SneakyThrow {

    // Private constructor to prevent instantiation
    private SneakyThrow() {}

    @SuppressWarnings("unchecked")
    public static <T extends Throwable> void sneakyThrow(Throwable t) throws T {
        throw (T) t;
    }
}
