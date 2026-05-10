package com.koteinik.chunksfadein.compat.dh.mixin.blaze;

import com.mojang.blaze3d.opengl.GlProgram;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static com.koteinik.chunksfadein.compat.dh.DHBlazeState.*;

@Mixin(value = GlProgram.class)
public abstract class GlProgramMixin {
	@Shadow
	@Final
	private int programId;

	@Shadow
	@Final
	private String debugLabel;

	@Inject(method = "setupUniforms", at = @At("TAIL"))
	private void cfi_setupCfiUniforms(CallbackInfo ci) {
		if (debugLabel == null || !debugLabel.equals("distanthorizons:terrain")) return;

		int prevProgram = GL11.glGetInteger(GL20.GL_CURRENT_PROGRAM);
		GL20.glUseProgram(programId);

		int skyLoc = GL20.glGetUniformLocation(programId, "cfi_sky");
		if (skyLoc != -1) GL20.glUniform1i(skyLoc, SKY_LOC);

		int lodMaskLoc = GL20.glGetUniformLocation(programId, "cfi_lodMask");
		if (lodMaskLoc != -1) GL20.glUniform1i(lodMaskLoc, LOD_MASK_LOC);

		registerProgram(programId);

		if (prevProgram != programId) GL20.glUseProgram(prevProgram);
	}
}
