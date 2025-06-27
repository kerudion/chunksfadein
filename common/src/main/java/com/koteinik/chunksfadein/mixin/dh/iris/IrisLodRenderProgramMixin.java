package com.koteinik.chunksfadein.mixin.dh.iris;

import com.koteinik.chunksfadein.config.Config;
import com.koteinik.chunksfadein.core.Utils;
import com.koteinik.chunksfadein.core.dh.LodMaskTexture;
import com.koteinik.chunksfadein.extensions.DhRenderProgramExt;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.irisshaders.iris.compat.dh.IrisLodRenderProgram;
import net.irisshaders.iris.gl.program.ProgramSamplers;
import net.irisshaders.iris.gl.texture.TextureType;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4fc;
import org.lwjgl.opengl.GL30;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Set;

@Mixin(value = IrisLodRenderProgram.class, remap = false)
public abstract class IrisLodRenderProgramMixin implements DhRenderProgramExt {
	@Unique
	private int chunkFadeData;
	@Unique
	private int lodMask;
	@Unique
	private int lodMaskDim;
	@Unique
	private int lodMaskMaxDist;
	@Unique
	private int lodMaskOrigin;
	@Unique
	private int lodMaskMinY;

	@Inject(method = "<init>", at = @At("RETURN"))
	private void modifyConstructor(CallbackInfo ci) {
		this.chunkFadeData = tryGetUniformLocation2("cfi_chunkFadeData");
		this.lodMask = tryGetUniformLocation2("cfi_lodMask");
		this.lodMaskDim = tryGetUniformLocation2("cfi_lodMaskDim");
		this.lodMaskMaxDist = tryGetUniformLocation2("cfi_lodMaskMaxDist");
		this.lodMaskOrigin = tryGetUniformLocation2("cfi_lodMaskOrigin");
		this.lodMaskMinY = tryGetUniformLocation2("cfi_lodMaskMinY");
	}

	@WrapOperation(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/irisshaders/iris/gl/program/ProgramSamplers;builder(ILjava/util/Set;)Lnet/irisshaders/iris/gl/program/ProgramSamplers$Builder;"))
	private ProgramSamplers.Builder modifyBuilder(int program, Set<Integer> reservedTextureUnits, Operation<ProgramSamplers.Builder> original) {
		ProgramSamplers.Builder builder = original.call(program, reservedTextureUnits);
		builder.addDynamicSampler(TextureType.TEXTURE_3D, LodMaskTexture::getId, null, "cfi_lodMask");
		return builder;
	}

	@Inject(method = "fillUniformData", at = @At(value = "TAIL"))
	private void modifyFillUniformData(Matrix4fc projection, Matrix4fc modelView, int worldYOffset, float partialTicks, CallbackInfo ci) {
		if (!Config.isModEnabled || !Config.isFadeEnabled)
			return;

		LodMaskTexture.createAndUpdate();

		LodMaskTexture texture = LodMaskTexture.getInstance();
		if (texture != null) {
			if (lodMaskDim != -1)
				GL30.glUniform3f(
					lodMaskDim,
					texture.sizeX, texture.sizeY, texture.sizeZ
				);
			if (lodMaskMaxDist != -1)
				GL30.glUniform3f(
					lodMaskMaxDist,
					(float) texture.sizeX * 8 + 16,
					(float) texture.sizeY * 8 + 16,
					(float) texture.sizeZ * 8 + 16
				);
			if (lodMaskOrigin != -1) {
				Vec3 cameraPos = Utils.cameraPosition();
				GL30.glUniform3f(
					lodMaskOrigin,
					(float) Math.floor(cameraPos.x / 16),
					(float) Math.floor(cameraPos.y / 16),
					(float) Math.floor(cameraPos.z / 16)
				);
			}
			if (lodMaskMinY != -1)
				GL30.glUniform1f(
					lodMaskMinY,
					texture.minY
				);
		}
	}

	@Override
	public void bindUniforms(float x, float y, float z, float w) {
		if (chunkFadeData != -1)
			GL30.glUniform4f(chunkFadeData, x, y, z, w);
	}

	@Shadow
	public int tryGetUniformLocation2(CharSequence name) {
		return -1;
	}
}
