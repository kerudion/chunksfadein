package com.koteinik.chunksfadein.compat.dh;

import com.koteinik.chunksfadein.core.Fader;
import it.unimi.dsi.fastutil.bytes.Byte2ObjectMap;
import it.unimi.dsi.fastutil.bytes.Byte2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;

import java.util.Map;

import static com.seibel.distanthorizons.core.pos.DhSectionPos.*;

public class DHState {
	public static final ThreadLocal<Long> sectionPosForCreatingBuffer = new ThreadLocal<>();

	private static final Byte2ObjectMap<Long2ObjectMap<Fader>> faders = new Byte2ObjectOpenHashMap<>();
	private static byte maxDetailLevel = 0;
	private static int lastLevel = 0;

	public synchronized static Fader getFader(long pos) {
		ClientLevel level = Minecraft.getInstance().level;
		if (level != null && lastLevel != level.hashCode()) {
			lastLevel = level.hashCode();
			maxDetailLevel = 0;
			faders.clear();
		}

		byte detailLevel = getDetailLevel(pos);
		if (maxDetailLevel < detailLevel) maxDetailLevel = detailLevel;

		for (byte i = detailLevel; i <= maxDetailLevel; i++) {
			Map<Long, Fader> faders = fadersAtLevel(i);
			if (faders == null) continue;

			Fader fader = faders.get(convertToDetailLevel(pos, i));
			if (fader != null)
				return fader;
		}

		Fader fader;
		faders.computeIfAbsent(detailLevel, k -> new Long2ObjectOpenHashMap<>())
			.put(
				pos, fader = new Fader(
					(int) Math.floor(getMinCornerBlockX(pos) / 16.0),
					(int) Math.floor(getMinCornerBlockZ(pos) / 16.0)
				)
			);

		return fader;
	}

	private static Long2ObjectMap<Fader> fadersAtLevel(byte level) {
		return faders.get(level);
	}
}
