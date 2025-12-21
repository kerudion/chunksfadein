package com.koteinik.chunksfadein;

import org.slf4j.LoggerFactory;

import java.util.StringJoiner;

public class Logger {
	private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger("chunksfadein");

	public static void info(Object... objects) {
		LOGGER.info(build(objects));
	}

	public static void warn(Object... objects) {
		LOGGER.warn(build(objects));
	}

	public static void error(Object... objects) {
		LOGGER.error(build(objects));
	}

	private static String build(Object... objects) {
		StringJoiner builder = new StringJoiner(" ");
		for (Object object : objects)
			builder.add(object == null ? "null" : object.toString());

		return builder.toString();
	}
}
