package com.koteinik.chunksfadein.compat.dh;

import com.koteinik.chunksfadein.core.SkyFBO;
import com.koteinik.chunksfadein.core.Utils;
import com.mojang.blaze3d.buffers.GpuBuffer;
import com.seibel.distanthorizons.api.enums.config.EDhApiMcRenderingFadeMode;
import com.seibel.distanthorizons.core.util.RenderUtil;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.lwjgl.system.MemoryStack;

import java.nio.ByteBuffer;

import static com.mojang.blaze3d.buffers.GpuBuffer.*;
import static com.mojang.blaze3d.systems.RenderSystem.getDevice;
import static org.lwjgl.system.MemoryStack.stackPush;

public class DHBlazeUniforms {
	public static GpuBuffer buffer = getDevice().createBuffer(
		() -> "Chunks Fade In DH uniform buffer",
		USAGE_UNIFORM | USAGE_COPY_DST | USAGE_MAP_WRITE,
		64
	);

	private static final Vector3f lodMaskDim = new Vector3f();
	private static float lodMaskMinY = 0;
	private static final Vector3f lodMaskMaxDist = new Vector3f();
	private static float dhStartFadeBlockDistanceSq = 0;
	private static final Vector3f lodMaskOrigin = new Vector3f();
	private static boolean dhFadeActive = false;
	private static final Vector2f screenSize = new Vector2f();

	public static void setUniforms() {
		float fadeStartDistanceSq = 0f;
		if (dhFadeEnabled()) {
			float dhNearClipDistance = RenderUtil.getNearClipPlaneInBlocks();
			dhNearClipDistance += 16f;

			float fadeStartDistance = dhNearClipDistance * 1.5f;

			fadeStartDistanceSq = fadeStartDistance * fadeStartDistance;
		}

		LodMaskTexture texture = LodMaskTexture.getInstance();
		Vec3 cameraPos = texture != null ? Utils.cameraPosition() : null;

		if (dhFadeEnabled()) {
			dhFadeActive = true;
			dhStartFadeBlockDistanceSq = fadeStartDistanceSq;
		} else {
			dhFadeActive = false;
			dhStartFadeBlockDistanceSq = 0;
		}

		if (texture != null) {
			lodMaskDim.set(texture.sizeX, texture.sizeY, texture.sizeZ);
			lodMaskMinY = texture.minY;

			lodMaskMaxDist.set(
				(float) texture.sizeX * 8 + 16,
				(float) texture.sizeY * 8 + 16,
				(float) texture.sizeZ * 8 + 16
			);
			lodMaskOrigin.set(
				(float) Math.floor(cameraPos.x / 16),
				(float) Math.floor(cameraPos.y / 16),
				(float) Math.floor(cameraPos.z / 16)
			);
		}

		screenSize.set(SkyFBO.getWidth(), SkyFBO.getHeight());

		upload();
	}

	private static void upload() {
		try (MemoryStack stack = stackPush()) {
			ByteBuffer data = stack.malloc(64);
			data.putFloat(lodMaskDim.x).putFloat(lodMaskDim.y).putFloat(lodMaskDim.z);
			data.putFloat(lodMaskMinY);
			data.putFloat(lodMaskMaxDist.x).putFloat(lodMaskMaxDist.y).putFloat(lodMaskMaxDist.z);
			data.putFloat(dhStartFadeBlockDistanceSq);
			data.putFloat(lodMaskOrigin.x).putFloat(lodMaskOrigin.y).putFloat(lodMaskOrigin.z);
			data.putInt(dhFadeActive ? 1 : 0);
			data.putFloat(screenSize.x).putFloat(screenSize.y);
			data.putFloat(0).putFloat(0);
			data.flip();

			getDevice().createCommandEncoder()
				.writeToBuffer(buffer.slice(), data);
		}
	}

	private static boolean dhFadeEnabled() {
		return com.seibel.distanthorizons.core.config.Config.Client.Advanced.Graphics.Quality.vanillaFadeMode.get()
			!= EDhApiMcRenderingFadeMode.NONE;
	}
}
