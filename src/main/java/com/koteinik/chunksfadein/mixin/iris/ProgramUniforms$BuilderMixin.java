package com.koteinik.chunksfadein.mixin.iris;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import net.irisshaders.iris.Iris;
import net.irisshaders.iris.IrisLogging;
import net.irisshaders.iris.gl.program.ProgramUniforms;

@Pseudo
@Mixin(value = ProgramUniforms.Builder.class)
public class ProgramUniforms$BuilderMixin {
    @Redirect(method = "buildUniforms", at = @At(value = "INVOKE", target = "Lnet/coderbot/iris/IrisLogging;warn(Ljava/lang/String;)V"), remap = false, require = 0)
    private void redirectUnsupportedUniformWarning(IrisLogging logging, String warning) {
        if (!warning.contains("Chunk_FadeDatas"))
            Iris.logger.warn(warning);
    }
}
