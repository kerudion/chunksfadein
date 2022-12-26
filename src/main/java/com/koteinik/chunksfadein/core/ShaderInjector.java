package com.koteinik.chunksfadein.core;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

import io.github.douira.glsl_transformer.ast.node.TranslationUnit;
import io.github.douira.glsl_transformer.ast.node.statement.CompoundStatement;
import io.github.douira.glsl_transformer.ast.print.PrintType;
import io.github.douira.glsl_transformer.ast.query.Root;
import io.github.douira.glsl_transformer.ast.transform.ASTInjectionPoint;
import io.github.douira.glsl_transformer.ast.transform.SingleASTTransformer;
import io.github.douira.glsl_transformer.job_parameter.NonFixedJobParameters;

public class ShaderInjector {
    private final SingleASTTransformer<NonFixedJobParameters> transformer = new SingleASTTransformer<>();
    private final List<BiConsumer<TranslationUnit, Root>> transformations = new ArrayList<>();

    public ShaderInjector() {
        transformer.setPrintType(PrintType.INDENTED);
    }

    public void injectTo(ASTInjectionPoint point, String... code) {
        transformations.add((translationUnit, root) -> {
            translationUnit.injectNodes(point, transformer.parseExternalDeclarations(translationUnit, code));
        });
    }

    public void appendToFunction(String functionName, String... code) {
        transformations.add((translationUnit, root) -> {
            CompoundStatement body = translationUnit.getFunctionDefinitionBody(functionName);
            body.getStatements().addAll(transformer.parseStatements(translationUnit, code));
        });
    }

    public void addToFunction(String functionName, String... code) {
        transformations.add((translationUnit, root) -> {
            CompoundStatement body = translationUnit.getFunctionDefinitionBody(functionName);
            body.getStatements().addAll(0, transformer.parseStatements(translationUnit, code));
        });
    }

    public void commit() {
        transformer.setTransformation((translationUnit, root) -> {
            for (BiConsumer<TranslationUnit, Root> transformation : transformations)
                transformation.accept(translationUnit, root);
        });
    }

    public String get(String code) {
        return transformer.transform(code);
    }
}