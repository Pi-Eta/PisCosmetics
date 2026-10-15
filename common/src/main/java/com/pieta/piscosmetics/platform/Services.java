package com.pieta.piscosmetics.platform;

import java.util.ServiceLoader;

/**
 * Finds the loader's implementation of {@link PlatformHelper}, one for each loader
 */
public final class Services {

    public static final PlatformHelper PLATFORM = load(PlatformHelper.class);

    private Services() {}

    private static <T> T load(Class<T> type) {
        return ServiceLoader.load(type, Services.class.getClassLoader()).findFirst()
                .orElseThrow(() -> new IllegalStateException("No implementation of " + type.getName()
                        + " found. Check the META-INF/services file in the loader's resources."));
    }
}
