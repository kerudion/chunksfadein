package com.koteinik.chunksfadein.crowdin;

import com.google.gson.JsonObject;
import com.koteinik.chunksfadein.Logger;
import com.koteinik.chunksfadein.NetworkUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Code taken and modified from https://github.com/gbl/CrowdinTranslate
 */
public class TranslationsDownloader extends Thread {
	private static final String CROWDIN_ID = "593943";
	private static final String CROWDIN_TOKEN = "2afc5ca9fa1d43622545f8115ea72ddb795b49c4a70aa5b845f1f76ccb6ee052e89a8b78b07bd282";

	private static final String API_LINK = "https://api.crowdin.com/api/v2";
	private static final String LIST_BUILDS = API_LINK + "/projects/" + CROWDIN_ID + "/translations/builds";
	private static final String GET_BUILD = LIST_BUILDS + "/%s/download";

	private static final Map<String, String> mcCodetoCrowdinCode = new HashMap<>();

	static {
		add("af_za", "af");
		add("ar_sa", "ar");
		add("ast_es", "ast");
		add("az_az", "az");
		add("ba_ru", "ba");
		add("bar", "bar"); // Bavaria
		add("be_by", "be");
		add("bg_bg", "bg");
		add("br_fr", "br-FR");
		add("brb", "brb"); // Brabantian
		add("bs_ba", "bs");
		add("ca_es", "ca");
		add("cs_cz", "cs");
		add("cy_gb", "cy");
		add("da_dk", "da");
		add("de_at", "de-AT,de");
		add("de_ch", "de-CH,de");
		add("de_de", "de");
		add("el_gr", "el");
		add("en_au", "en-AU,en-GB,en-US");
		add("en_ca", "en-CA,en-GB,en-US");
		add("en_gb", "en-GB,en-US");
		add("en_nz", "en-NZ,en-GB,en-US");
		add("en_pt", "en-PT,en-GB,en-US");
		add("en_ud", "en-UD,en-GB,en-US");
		add("en_us", "en-US");
		add("enp", "enp"); // Anglish
		add("en_ws", "en-WS");
		add("en_7s", "en-PT");
		add("en_ud", "en-UD");
		add("eo_uy", "eo");
		add("es_ar", "es-AR,es-ES");
		add("es_cl", "es-CL,es-ES");
		add("es_ec", "es-EC,es-ES");
		add("es_es", "es-ES");
		add("es_mx", "es-MX,es-ES");
		add("es_uy", "es-UY,es-ES");
		add("es_ve", "es-VE,es-ES");
		add("esan", "esan"); // Andalusian
		add("et_ee", "et");
		add("eu_es", "eu");
		add("fa_ir", "fa");
		add("fi_fi", "fi");
		add("fil_ph", "fil");
		add("fo_fo", "fo");
		add("fr_ca", "fr-CA,fr");
		add("fr_fr", "fr");
		add("fra_de", "fra-DE");
		add("fy_nl", "fy-NL");
		add("ga_ie", "ga-IE");
		add("gd_gb", "gd");
		add("gl_es", "gl");
		add("haw_us", "haw");
		add("he_il", "he");
		add("hi_in", "hi");
		add("hr_hr", "hr");
		add("hu_hu", "hu");
		add("hy_am", "hy-AM");
		add("id_id", "id");
		add("ig_ng", "ig");
		add("io_en", "ido");
		add("is_is", "is");
		add("isv", "isv"); // Interslavic
		add("it_it", "it");
		add("ja_jp", "ja");
		add("jbo_en", "jbo");
		add("ka_ge", "ka");
		add("kk_kz", "kk");
		add("kn_in", "kn");
		add("ko_kr", "ko");
		add("ksh", "ksh"); // Ripuarian
		add("kw_gb", "kw");
		add("la_la", "la-LA");
		add("lb_lu", "lb");
		add("li_li", "li");
		add("lol_us", "lol");
		add("lt_lt", "lt");
		add("lv_lv", "lv");
		add("lzh", "lzh"); // Classical Chinese
		add("mi_NZ", "mi");
		add("mk_mk", "mk");
		add("mn_mn", "mn");
		add("ms_my", "ms");
		add("mt_mt", "mt");
		add("nds_de", "nds");
		add("nl_be", "nl-BE,nl");
		add("nl_nl", "nl");
		add("nn_no", "nn-NO,no");
		add("no_no", "no,nb");
		add("oc_fr", "oc");
		add("ovd", "ovd"); // Elfdalian
		add("pl_pl", "pl");
		add("pt_br", "pt-BR,pt-PT");
		add("pt_pt", "pt-PT,pt-BR");
		add("qya_aa", "qya-AA");
		add("ro_ro", "ro");
		add("rpr", "rpr"); // Russian (pre-revolutionary)
		add("ru_ru", "ru");
		add("se_no", "se");
		add("sk_sk", "sk");
		add("sl_si", "sl");
		add("so_so", "so");
		add("sq_al", "sq");
		add("sr_sp", "sr");
		add("sv_se", "sv-SE");
		add("sxu", "sxu"); // Upper Saxon German
		add("szl", "szl"); // Silesian
		add("ta_in", "ta");
		add("th_th", "th");
		add("tl_ph", "tl");
		add("tlh_aa", "tlh-AA");
		add("tr_tr", "tr");
		add("tt_ru", "tt-RU");
		add("uk_ua", "uk");
		add("val_es", "val-ES");
		add("vec_it", "vec");
		add("vi_vn", "vi");
		add("yi_de", "yi");
		add("yo_ng", "yo");
		add("zh_cn", "zh-CN,zh-HK");
		add("zh_hk", "zh-HK,zh-CN");
		add("zh_tw", "zh-TW");
	}

	private static void add(String mc, String ci) {
		mcCodetoCrowdinCode.put(mc, ci);
	}

	@Override
	public void run() {
		Map<String, byte[]> translations;
		try {
			translations = getCrowdinTranslations(CROWDIN_ID);
		} catch (IOException e) {
			Logger.warn("Failed to get crowdin translations:", e);
			return;
		}

		File langDir = Translations.getLangRootDir();
		langDir.mkdirs();

		for (Map.Entry<String, String> entry : mcCodetoCrowdinCode.entrySet()) {
			String[] sourcesByPreference = entry.getValue().split(",");

			for (String attemptingSource : sourcesByPreference) {
				byte[] buffer = translations.get(attemptingSource);
				if (buffer == null)
					continue;

				saveBufferToFile(buffer, new File(langDir, entry.getKey() + ".json"));
				break;
			}
		}
	}

	private Map<String, byte[]> getCrowdinTranslations(String projectName) throws IOException {
		ZipInputStream zis = null;
		Pattern pattern = Pattern.compile("^([a-z]{2}(-[A-Z]{2})?)/(.+\\.json)$");
		Map<String, byte[]> zipContents = new HashMap<>();

		try {
			String lastBuildId = get(LIST_BUILDS).getAsJsonArray("data").asList().stream()
			                                     .map(e -> e.getAsJsonObject().getAsJsonObject("data"))
			                                     .filter(e -> e.get("status").getAsString().equals("finished"))
			                                     .map(e -> e.get("id").getAsString())
			                                     .findFirst()
			                                     .get();

			String buildUrl = get(GET_BUILD.formatted(lastBuildId))
				.getAsJsonObject("data")
				.get("url")
				.getAsString();

			URL url = URI.create(buildUrl).toURL();

			zis = new ZipInputStream(url.openStream());

			ZipEntry entry;
			while ((entry = zis.getNextEntry()) != null) {
				String path = entry.getName();
				Matcher matcher = pattern.matcher(path);

				if (matcher.matches()) {
					String crowdinLang = matcher.group(1);
					String origFileName = matcher.group(3);

					if (entry.getSize() > 10_000_000)
						throw new IOException("file too large: " + entry.getName() + ": " + entry.getSize());

					byte[] zipFileContent = getZipStreamContent(zis, (int) entry.getSize());
					if (zipContents.containsKey(crowdinLang)) {
						System.err.println("More than one file for " + crowdinLang + ", ignoring " + origFileName);
						continue;
					}

					zipContents.put(matcher.group(1), zipFileContent);
				}
			}
		} catch (IOException e) {
			if (zis != null) {
				try {
					zis.close();
				} catch (IOException e1) {
				}
			}

			throw e;
		}
		return zipContents;
	}

	private JsonObject get(String url) {
		return NetworkUtils.executeGet(url, Map.of("Authorization", "Bearer " + CROWDIN_TOKEN), Map.of())
		                   .getAsJsonObject();
	}

	private byte[] getZipStreamContent(InputStream is, int size) throws IOException {
		byte[] buf = new byte[size];
		int toRead = size;
		int totalRead = 0, readNow;

		while (toRead > 0) {
			if ((readNow = is.read(buf, totalRead, toRead)) <= 0) {
				throw new IOException("premature end of stream");
			}

			totalRead += readNow;
			toRead -= readNow;
		}
		return buf;
	}

	private void saveBufferToFile(byte[] buffer, File file) {
		try (FileOutputStream stream = new FileOutputStream(file)) {
			stream.write(buffer);
		} catch (IOException e) {
			e.printStackTrace(System.err);
		}
	}
}
