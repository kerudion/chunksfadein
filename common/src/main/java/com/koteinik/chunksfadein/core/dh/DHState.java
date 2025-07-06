package com.koteinik.chunksfadein.core.dh;

import com.koteinik.chunksfadein.core.Fader;
import com.seibel.distanthorizons.core.pos.DhSectionPos;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import org.joml.Vector2i;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DHState {
	public static final ThreadLocal<Long> sectionPosForCreatingBuffer = new ThreadLocal<>();

	private static final Map<Vector2i, Fader> faders = new ConcurrentHashMap<>();
	private static int lastLevel = 0;

	public synchronized static Fader getFader(long pos) {
		ClientLevel level = Minecraft.getInstance().level;
		if (level != null && lastLevel != level.hashCode()) {
			lastLevel = level.hashCode();
			faders.clear();
		}

		Vector2i blockPos = blockPos(pos);
		Fader fader = faders.get(blockPos);

		if (fader == null)
			populateDescendantFaders(pos, fader = new Fader(blockPos.x, blockPos.y));

		return fader;
	}

	private static void populateDescendantFaders(long pos, Fader fader) {
		faders.put(blockPos(pos), fader);

		if (DhSectionPos.getDetailLevel(pos) > 0)
			for (int i = 0; i < 4; i++)
				populateDescendantFaders(DhSectionPos.getChildByIndex(pos, i), fader);
	}

	private static Vector2i blockPos(long pos) {
		return new Vector2i(
			DhSectionPos.getMinCornerBlockX(pos),
			DhSectionPos.getMinCornerBlockZ(pos)
		);
	}
}
