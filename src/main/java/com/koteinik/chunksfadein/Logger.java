package com.koteinik.chunksfadein;

import org.slf4j.LoggerFactory;

public class Logger {
    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger("chunksfadein");

    public static void info(Object object) {
        LOGGER.info(object.toString());
    }
}
