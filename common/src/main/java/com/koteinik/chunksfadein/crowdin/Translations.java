package com.koteinik.chunksfadein.crowdin;

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
			thread.join(10000);
		} catch (InterruptedException ex) {
			ex.printStackTrace();
		}
	}
}
