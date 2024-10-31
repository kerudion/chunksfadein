package com.koteinik.chunksfadein.mixin.iris;

import java.util.ArrayList;
import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.koteinik.chunksfadein.core.FadeShader;

import io.github.douira.glsl_transformer.ast.data.ChildNodeList;
import io.github.douira.glsl_transformer.ast.node.TranslationUnit;
import io.github.douira.glsl_transformer.ast.node.declaration.TypeAndInitDeclaration;
import io.github.douira.glsl_transformer.ast.node.expression.LiteralExpression;
import io.github.douira.glsl_transformer.ast.node.external_declaration.ExternalDeclaration;
import io.github.douira.glsl_transformer.ast.node.statement.CompoundStatement;
import io.github.douira.glsl_transformer.ast.node.statement.Statement;
import io.github.douira.glsl_transformer.ast.node.type.FullySpecifiedType;
import io.github.douira.glsl_transformer.ast.node.type.qualifier.LayoutQualifier;
import io.github.douira.glsl_transformer.ast.node.type.qualifier.LayoutQualifierPart;
import io.github.douira.glsl_transformer.ast.node.type.qualifier.NamedLayoutQualifierPart;
import io.github.douira.glsl_transformer.ast.node.type.qualifier.TypeQualifier;
import io.github.douira.glsl_transformer.ast.node.type.qualifier.TypeQualifierPart;
import io.github.douira.glsl_transformer.ast.node.type.specifier.BuiltinNumericTypeSpecifier;
import io.github.douira.glsl_transformer.ast.query.Root;
import io.github.douira.glsl_transformer.ast.transform.ASTInjectionPoint;
import io.github.douira.glsl_transformer.ast.transform.ASTParser;
import io.github.douira.glsl_transformer.ast.traversal.ASTListener;
import io.github.douira.glsl_transformer.ast.traversal.ASTWalker;
import io.github.douira.glsl_transformer.util.Type;
import net.irisshaders.iris.pipeline.transform.parameter.SodiumParameters;
import net.irisshaders.iris.pipeline.transform.transformer.SodiumTransformer;
import net.minecraft.util.Tuple;

@Mixin(value = SodiumTransformer.class, remap = false)
public class IrisSodiumTransformerMixin {
    @Inject(method = "transform", at = @At("TAIL"))
    private static void modifyTransform(ASTParser t, TranslationUnit tree, Root root, SodiumParameters parameters, CallbackInfo ci) {
        FadeShader shader = new FadeShader();

        switch (parameters.type.glShaderType) {
            case VERTEX:
                CompoundStatement vertInit = tree.getOneFunctionDefinitionBody("_vert_init");
                ExternalDeclaration vertInitParent = (ExternalDeclaration) vertInit.getParent();
                vertInitParent.detach();

                ChildNodeList<ExternalDeclaration> children = tree.getChildren();

                int i = children.indexOf(tree.getOneFunctionDefinitionBody("getVertexPosition").getParent());
                children.add(i + 1, vertInitParent);

                vertInit.getStatements().addAll(
                    parseStatements(t, root, shader
                        .newLine("vec3 position = getVertexPosition().xyz;")
                        .vertMod("_vert_position", "position", true, "_draw_id")
                        .dumpArray()));

                tree.parseAndInjectNodes(t, ASTInjectionPoint.BEFORE_FUNCTIONS, shader
                    .vertOutUniforms().dumpList().stream());
                break;

            case FRAGMENT:
                tree.parseAndInjectNodes(t, ASTInjectionPoint.BEFORE_FUNCTIONS, shader
                    .fragInVars().dumpList().stream());

                List<Tuple<Type, String>> layouts = findOutputColors(tree);
                Tuple<Type, String> first = layouts.get(0);

                tree.appendMainFunctionBody(parseStatements(t, root, shader
                    .fragColorMod(first.getB() + ".rgb", "iris_FogColor.rgb")
                    .dumpMultiline()));
                break;

            default:
                break;
        }
    }

    // please, forgive me for this mess
    private static List<Tuple<Type, String>> findOutputColors(TranslationUnit tree) {
        List<Tuple<Type, String>> colors = new ArrayList<>();

        ASTListener listener = new ASTListener() {
            @Override
            public void enterTypeAndInitDeclaration(TypeAndInitDeclaration declaration) {
                FullySpecifiedType fullType = declaration.getType();

                if (!(fullType.getTypeSpecifier() instanceof BuiltinNumericTypeSpecifier numSpecifier))
                    return;

                TypeQualifier typeQualifier = fullType.getTypeQualifier();
                if (typeQualifier == null)
                    return;

                for (TypeQualifierPart part : typeQualifier.getParts()) {
                    if (!(part instanceof LayoutQualifier qualifier))
                        continue;

                    for (LayoutQualifierPart layoutPart : qualifier.getParts()) {
                        if (!(layoutPart instanceof NamedLayoutQualifierPart namedPart))
                            continue;

                        if (!(namedPart.getExpression() instanceof LiteralExpression))
                            continue;

                        colors.add(new Tuple<>(numSpecifier.type, declaration.getMembers().get(0).getName().getName()));
                    }
                }
            }
        };

        ASTWalker.walk(listener, tree);

        return colors;
    }

    private static List<Statement> parseStatements(ASTParser t, Root root, String... input) {
        if (input.length == 0 || (input.length == 1 && input[0].isBlank()))
            return List.of();

        return t.parseStatements(root, input);
    }
}
