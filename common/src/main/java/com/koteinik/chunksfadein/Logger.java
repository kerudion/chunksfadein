package com.koteinik.chunksfadein;

import org.slf4j.LoggerFactory;

public class Logger {
    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger("chunksfadein");

    public static void info(Object object) {
        LOGGER.info(object == null ? "null" : object.toString());
    }

    public static void warn(Object object) {
        LOGGER.warn(object == null ? "null" : object.toString());
    }

    public static void error(Object object) {
        LOGGER.error(object == null ? "null" : object.toString());
    }
}
