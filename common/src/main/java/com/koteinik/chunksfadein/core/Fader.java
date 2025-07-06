package com.koteinik.chunksfadein.core;

import org.joml.Vector3f;

import com.koteinik.chunksfadein.MathUtils;
import com.koteinik.chunksfadein.config.Config;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

public class Fader {
	public static final float[] ZERO_OFFSET = new float[3];
	private static final Vector3f UP = new Vector3f(0, 1, 0);

	private long lastFrameTime = 0L;
	private boolean hasRenderedBefore;
	private float fadeCoeff = 0f;
	private float animationProgress = 0f;

	private final float[] offset = new float[3];

	private final int chunkX;
	private final int chunkZ;

	private final float fadeChangePerMs;
	private final float animationChangePerMs;

	public Fader(int chunkX, int chunkZ) {
		this.chunkX = chunkX;
		this.chunkZ = chunkZ;
		this.fadeChangePerMs = Config.fadeChangePerMs;
		this.animationChangePerMs = Config.animationChangePerMs;
	}

	public Fader(int chunkX, int chunkZ, float fadeChangePerMs, float animationChangePerMs) {
		this.chunkX = chunkX;
		this.chunkZ = chunkZ;
		this.fadeChangePerMs = fadeChangePerMs;
		this.animationChangePerMs = animationChangePerMs;
	}

	public boolean hasRenderedBefore() {
		return hasRenderedBefore;
	}

	public void setRenderedBefore() {
		hasRenderedBefore = true;
	}

	public float incrementFadeCoeff(long delta, boolean nearPlayer) {
		if (fadeCoeff == 1f)
			return fadeCoeff;

		fadeCoeff += delta * fadeChangePerMs;

		if (fadeCoeff > 1f)
			fadeCoeff = 1f;

		if (!hasRenderedBefore() && !Config.fadeNearPlayer && nearPlayer)
			fadeCoeff = 1f;

		return fadeCoeff;
	}

	public float[] incrementAnimationOffset(long delta, boolean nearPlayer) {
		if (animationProgress == 1f)
			return offset;

		animationProgress += delta * animationChangePerMs;
		if (animationProgress > 1f)
			animationProgress = 1f;

		if (!hasRenderedBefore() && !Config.animateNearPlayer && nearPlayer)
			animationProgress = 1f;

		float progress = Config.animationCurve.calculate(animationProgress);
		if (Config.animationType == AnimationType.JAGGED) {
			offset[1] = MathUtils.lerp(-Config.animationFactor * 16, 0, progress);
		} else if (Config.animationType == AnimationType.DISPLACEMENT || Config.animationType == AnimationType.SCALE) {
			offset[1] = MathUtils.lerp(Config.animationFactor, 0, progress);
		} else {
			if (Config.animationAngle == 0) {
				offset[1] = MathUtils.lerp(Config.animationOffset, 0, progress);
			} else {
				Vec3 thisPos = new Vec3(chunkX * 16 + 8, 0, chunkZ * 16 + 8);

				Vec3 camPos = getCameraPosition();
				camPos = new Vec3(camPos.x, 0, camPos.z);

				Vector3f direction = camPos.toVector3f().sub(thisPos.toVector3f()).normalize();
				Vector3f axis = new Vector3f(direction).cross(UP);

				direction.rotateAxis((float) Math.toRadians(90 - Config.animationAngle), axis.x, axis.y, axis.z)
				         .mul(Config.animationOffset)
				         .lerp(new Vector3f(), progress);

				if (Config.animationOffset > 0)
					direction.rotateY((float) Math.PI);

				offset[0] = direction.x;
				offset[1] = direction.y;
				offset[2] = direction.z;
			}
		}

		return offset;
	}

	public long calculateAndGetDelta() {
		long currentFrameTime = System.currentTimeMillis();
		long delta = lastFrameTime == 0L ? 0L : currentFrameTime - lastFrameTime;

		lastFrameTime = currentFrameTime;

		return delta;
	}

	public float[] getAnimationOffset() {
		return offset;
	}

	public float getFadeCoeff() {
		return fadeCoeff;
	}

	private static Vec3 getCameraPosition() {
		Minecraft client = Minecraft.getInstance();
		Entity camera = client.cameraEntity;

		if (camera == null)
			return new Vec3(0, 0, 0);

		return camera.position();
	}
}
