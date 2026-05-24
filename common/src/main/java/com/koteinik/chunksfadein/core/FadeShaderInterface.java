package com.koteinik.chunksfadein.core;

import com.koteinik.chunksfadein.compat.sodium.ext.*;
import org.joml.Matrix3f;

public class FadeShaderInterface {
	private GlUniformBlockExt uniformFadeDatas;
	private GlUniformFloat2vExt screenSize;
	private GlUniformMatrix3fExt worldInView;

	public FadeShaderInterface(ShaderBindingContextExt context) {
		this.uniformFadeDatas = context.bindUniformBlock("cfi_ubo_ChunkFadeDatas");
		this.screenSize = context.bindUniformFloat2v("cfi_screenSize");
		this.worldInView = context.bindUniformMat3f("cfi_worldInView");
	}

	public void bindUniforms(GlMutableBufferExt fadeDataBuffer) {
		if (uniformFadeDatas != null)
			uniformFadeDatas.bindBuffer(fadeDataBuffer);

		if (screenSize != null)
			screenSize.set(Utils.mainTargetWidth(), Utils.mainTargetHeight());

		if (worldInView != null)
			worldInView.set(new Matrix3f().rotation(Utils.cameraViewRot()));
	}
}
