package com.koteinik.chunksfadein.compat.dh;

import com.koteinik.chunksfadein.core.Fader;
import com.seibel.distanthorizons.core.pos.DhSectionPos;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import org.joml.Vector2i;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DHState {
	public static final ThreadLocal<Long> sectionPosForCreatingBuffer = new ThreadLocal<>();

	private static final Map<Byte, Map<Vector2i, Fader>> faders = new ConcurrentHashMap<>();
	private static int lastLevel = 0;

	public synchronized static Fader getFader(long pos) {
		ClientLevel level = Minecraft.getInstance().level;
		if (level != null && lastLevel != level.hashCode()) {
			lastLevel = level.hashCode();
			faders.clear();
		}

		Map<Vector2i, Fader> faders = fadersAtLevel(detailLevel(pos));

		Vector2i blockPos = blockPos(pos);
		Fader fader = faders.get(blockPos);
		if (fader != null) return fader;

		populateDescendantFaders(
			pos,
			fader = new Fader(
				(int) Math.floor(blockPos.x / 16.0),
				(int) Math.floor(blockPos.y / 16.0)
			)
		);

		return fader;
	}

	private static void populateDescendantFaders(long pos, Fader fader) {
		fadersAtLevel(detailLevel(pos)).put(blockPos(pos), fader);

		if (detailLevel(pos) > 0)
			for (int i = 0; i < 4; i++)
				populateDescendantFaders(DhSectionPos.getChildByIndex(pos, i), fader);
	}

	private static Map<Vector2i, Fader> fadersAtLevel(byte level) {
		return faders.computeIfAbsent(level, k -> new ConcurrentHashMap<>());
	}

	private static byte detailLevel(long pos) {
		return DhSectionPos.getDetailLevel(pos);
	}

	private static Vector2i blockPos(long pos) {
		return new Vector2i(
			DhSectionPos.getMinCornerBlockX(pos),
			DhSectionPos.getMinCornerBlockZ(pos)
		);
	}
}
