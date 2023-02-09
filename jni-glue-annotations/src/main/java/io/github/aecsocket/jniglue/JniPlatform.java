package io.github.aecsocket.jniglue;

import java.util.Locale;
import java.util.function.Function;

public enum JniPlatform {
    LINUX       ((x) -> "lib" + x + ".so"),
    WINDOWS     ((x) -> x + ".dll"),
    MACOS       ((x) -> "lib" + x + ".dylib"),
    MACOS_ARM64 ((x) -> "lib" + x + ".dylib");

    private final Function<String, String> libraryNameMapper;

    JniPlatform(Function<String, String> libNameMapper) {
        this.libraryNameMapper = libNameMapper;
    }

    public String mapLibraryName(String value) { return libraryNameMapper.apply(value); }

    public static JniPlatform get() {
        String osName = System.getProperty("os.name").toLowerCase(Locale.ROOT);
        String osArch = System.getProperty("os.arch");

        if (osName.contains("windows")) {
            return WINDOWS;
        } else if (osName.contains("linux")) {
            return LINUX;
        } else if (osName.contains("mac os x") || osName.contains("darwin")) {
            if (osArch != null && osArch.equals("aarch64")) {
                return MACOS_ARM64;
            } else {
                return MACOS;
            }
        } else {
            throw new IllegalStateException("Unsupported OS " + osName);
        }
    }
}
