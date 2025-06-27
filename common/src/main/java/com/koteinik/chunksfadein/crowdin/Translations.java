package com.koteinik.chunksfadein.crowdin;

import com.koteinik.chunksfadein.Logger;

import java.io.File;

public class Translations {
	public static File getPackRootDir() {
		return new File("cache/cfi-translations");
	}

	public static File getLangRootDir() {
		return new File(getPackRootDir(), "assets/chunksfadein/lang");
	}

	public static void download() {
		TranslationsDownloader thread = new TranslationsDownloader();
		thread.start();

		try {
			thread.join(60000);
		} catch (InterruptedException e) {
			Logger.warn("Translations download timeout");
		}
	}
}
