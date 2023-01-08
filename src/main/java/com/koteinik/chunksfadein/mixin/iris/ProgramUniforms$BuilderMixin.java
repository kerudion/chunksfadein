package com.koteinik.chunksfadein.mixin.iris;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import net.coderbot.iris.Iris;
import net.coderbot.iris.IrisLogging;
import net.coderbot.iris.gl.program.ProgramUniforms;

@Pseudo
@Mixin(value = ProgramUniforms.Builder.class)
public class ProgramUniforms$BuilderMixin {
    @Redirect(method = "buildUniforms", at = @At(value = "INVOKE", target = "Lnet/coderbot/iris/IrisLogging;warn(Ljava/lang/String;)V"), remap = false)
    private void redirectUnsupportedUniformWarning(IrisLogging logging, String warning) {
        if (!warning.contains("Chunk_FadeDatas"))
            Iris.logger.warn(warning);
    }
}
