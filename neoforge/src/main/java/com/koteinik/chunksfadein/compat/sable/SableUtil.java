package com.koteinik.chunksfadein.compat.sable;

import com.koteinik.chunksfadein.config.Config;
import dev.ryanhcode.sable.sublevel.ClientSubLevel;
import dev.ryanhcode.sable.sublevel.SubLevel;
import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3dc;

public class SableUtil {
	public static float curvatureOffset(SubLevel subLevel) {
		if (subLevel == null || !Config.isModEnabled || !Config.isCurvatureEnabled)
			return 0f;

		Vec3 cam = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();
		Vector3dc pos = ((ClientSubLevel) subLevel).renderPose().position();
		double dx = pos.x() - cam.x;
		double dz = pos.z() - cam.z;

		return -(float) ((dx * dx + dz * dz) / Config.worldCurvature);
	}
}
