package com.koteinik.chunksfadein.mixin.iris;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.koteinik.chunksfadein.config.Config;
import com.koteinik.chunksfadein.core.FadeTypes;
import com.koteinik.chunksfadein.core.IrisOutputColorDeclaration;

import io.github.douira.glsl_transformer.ast.node.TranslationUnit;
import io.github.douira.glsl_transformer.ast.node.declaration.TypeAndInitDeclaration;
import io.github.douira.glsl_transformer.ast.node.expression.LiteralExpression;
import io.github.douira.glsl_transformer.ast.node.external_declaration.DeclarationExternalDeclaration;
import io.github.douira.glsl_transformer.ast.node.statement.Statement;
import io.github.douira.glsl_transformer.ast.node.type.FullySpecifiedType;
import io.github.douira.glsl_transformer.ast.node.type.qualifier.LayoutQualifier;
import io.github.douira.glsl_transformer.ast.node.type.qualifier.LayoutQualifierPart;
import io.github.douira.glsl_transformer.ast.node.type.qualifier.NamedLayoutQualifierPart;
import io.github.douira.glsl_transformer.ast.node.type.qualifier.TypeQualifier;
import io.github.douira.glsl_transformer.ast.node.type.qualifier.TypeQualifierPart;
import io.github.douira.glsl_transformer.ast.node.type.specifier.BuiltinNumericTypeSpecifier;
import io.github.douira.glsl_transformer.ast.query.Root;
import io.github.douira.glsl_transformer.ast.query.index.ExternalDeclarationIndex.DeclarationEntry;
import io.github.douira.glsl_transformer.ast.transform.ASTInjectionPoint;
import io.github.douira.glsl_transformer.ast.transform.ASTParser;
import io.github.douira.glsl_transformer.util.Type;
import net.irisshaders.iris.pipeline.transform.parameter.SodiumParameters;
import net.irisshaders.iris.pipeline.transform.transformer.SodiumTransformer;

@Mixin(value = SodiumTransformer.class, remap = false)
public class IrisSodiumTransformerMixin {
    @Inject(method = "transform", at = @At("TAIL"))
    private static void modifyTransform(ASTParser t, TranslationUnit tree, Root root, SodiumParameters parameters,
        CallbackInfo ci) {
        boolean isLined = Config.fadeType == FadeTypes.LINED;
        boolean isCurvatureEnabled = Config.isCurvatureEnabled;

        switch (parameters.type.glShaderType) {
            case VERTEX:
                List<String> vertInitTail = new ArrayList<>();
                vertInitTail.add("fadeCoeff = Chunk_FadeDatas[_draw_id].fadeData.w;");
                vertInitTail.add("_vert_position += Chunk_FadeDatas[_draw_id].fadeData.xyz;");

                if (isLined)
                    vertInitTail.add("localHeight = _vert_position.y;");

                tree.appendFunctionBody("_vert_init", parseStatements(t, root, vertInitTail));

                if (isCurvatureEnabled) {
                    List<String> mainTail = new ArrayList<>();

                    mainTail.add("gl_Position.y -= dot(gl_Position, gl_Position) / %s;".formatted(Config.worldCurvature));

                    tree.appendMainFunctionBody(parseStatements(t, root, mainTail));
                }

                List<String> vDefines = new ArrayList<>();
                vDefines.add("out float fadeCoeff;");
                vDefines.add("struct ChunkFadeData { vec4 fadeData; };");
                vDefines.add("layout(std140) uniform ubo_ChunkFadeDatas { ChunkFadeData Chunk_FadeDatas[256]; };");

                if (isLined)
                    vDefines.add("out float localHeight;");

                tree.parseAndInjectNodes(t, ASTInjectionPoint.BEFORE_FUNCTIONS, vDefines.stream());
                break;

            case FRAGMENT:
                List<String> fDefines = new ArrayList<>();
                fDefines.add("in float fadeCoeff;");

                if (isLined)
                    fDefines.add("in float localHeight;");

                tree.parseAndInjectNodes(t, ASTInjectionPoint.BEFORE_FUNCTIONS, fDefines.stream());
                List<String> mainTail = new ArrayList<>();

                if (isLined)
                    mainTail.add("float fadeLineY = fadeCoeff * 16.0;");

                IrisOutputColorDeclaration layout0 = findOutputColorVariable(root);
                boolean isVec3 = layout0.type == Type.F32VEC3;

                String fadeLogic = "";
                fadeLogic += "if(fadeCoeff >= 0.0 && fadeCoeff < 1.0) {";

                fadeLogic += layout0.name + " = mix(" + layout0.name + ", iris_FogColor";
                if (isVec3)
                    fadeLogic += ".xyz";

                fadeLogic += ", ";

                if (isLined)
                    fadeLogic += "localHeight <= fadeLineY ? 0.0 : 1.0";
                else
                    fadeLogic += "1.0 - fadeCoeff";
                fadeLogic += ");";

                fadeLogic += "}";

                mainTail.add(fadeLogic);

                tree.appendMainFunctionBody(t, mainTail.toArray(String[]::new));
                break;

            default:
                break;
        }
    }

    // please, forgive me for this mess
    @SuppressWarnings("unchecked")
    private static IrisOutputColorDeclaration findOutputColorVariable(Root root) {
        for (Entry<String, ?> entry : root.externalDeclarationIndex.index.entrySet())
            try {
                String key = entry.getKey();

                HashSet<DeclarationEntry> entries = (HashSet<DeclarationEntry>) entry.getValue();
                DeclarationEntry declarationEntry = entries.stream().findAny().get();

                DeclarationExternalDeclaration externalDeclaration = (DeclarationExternalDeclaration) declarationEntry
                    .declaration();
                TypeAndInitDeclaration declaration = (TypeAndInitDeclaration) externalDeclaration.getDeclaration();
                FullySpecifiedType type = declaration.getType();

                BuiltinNumericTypeSpecifier typeSpecifier = (BuiltinNumericTypeSpecifier) type.getTypeSpecifier();
                Type variableType = typeSpecifier.type;

                TypeQualifier typeQualifier = type.getTypeQualifier();

                for (TypeQualifierPart part : typeQualifier.getParts()) {
                    if (part instanceof LayoutQualifier qualifier)
                        for (LayoutQualifierPart layoutPart : qualifier.getParts())
                            if (layoutPart instanceof NamedLayoutQualifierPart namedPart)
                                if (namedPart.getExpression() instanceof LiteralExpression expression) {
                                    int value = expression.getNumber().intValue();
                                    if (value == 0)
                                        return new IrisOutputColorDeclaration(key, variableType);
                                }
                }

            } catch (Exception e) {
                // ignored
            }

        return new IrisOutputColorDeclaration("iris_FragData0", Type.F32VEC4);
    }

    private static List<Statement> parseStatements(ASTParser t, Root root, List<String> lines) {
        return parseStatements(t, root, lines.toArray(String[]::new));
    }

    private static List<Statement> parseStatements(ASTParser t, Root root, String... lines) {
        return t.parseStatements(root, lines);
    }

}
