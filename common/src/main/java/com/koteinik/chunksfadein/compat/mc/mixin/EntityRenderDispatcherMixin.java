package com.koteinik.chunksfadein.compat.mc.mixin;

import com.koteinik.chunksfadein.compat.sodium.ext.SodiumWorldRendererExt;
import com.koteinik.chunksfadein.config.Config;
import com.koteinik.chunksfadein.core.Utils;
import com.mojang.blaze3d.vertex.PoseStack;
import me.jellysquid.mods.sodium.client.render.SodiumWorldRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = EntityRenderDispatcher.class)
public class EntityRenderDispatcherMixin {
	@Inject(
		method = "render(Lnet/minecraft/world/entity/Entity;DDDFFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
		at = @At(
			value = "INVOKE",
			target = "Lcom/mojang/blaze3d/vertex/PoseStack;translate(DDD)V",
			shift = At.Shift.AFTER,
			ordinal = 0
		)
	)
	private void modifyRender(
		Entity entity,
		double x, double y, double z, float rotationYaw, float partialTicks,
		PoseStack matrices, MultiBufferSource buffer, int packedLight,
		CallbackInfo ci
	) {
		if (!Config.isModEnabled || (!Config.isAnimationEnabled && !Config.isCurvatureEnabled)
			|| entity.level() == null
			|| entity.level().getEntity(entity.getId()) == null)
			return;

		SodiumWorldRendererExt ext = ((SodiumWorldRendererExt) SodiumWorldRenderer.instance());
		if (ext.getRenderSectionManager() == null)
			return;

		Quaternionf viewRot = Utils.cameraViewRot();

		Vector3f origin = matrices.last().pose().getTranslation(new Vector3f());
		viewRot.transformInverse(origin);

		Vec3 pos = Utils.cameraPosition().add(origin.x, origin.y, origin.z);

		float[] offset = ext.getAnimationOffset(pos);
		if (offset == null)
			return;

		Vector3f worldShift = new Vector3f(offset[0], offset[1], offset[2]);
		viewRot.transform(worldShift);
		matrices.last().pose().translateLocal(worldShift.x, worldShift.y, worldShift.z);
	}
}
