package com.koteinik.chunksfadein.core;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class ShaderInjector {
    private final List<Function<String, String>> transformations = new ArrayList<>();

    public void insertAfterDefines(String... code) {
        insertAfterDefines(false, code);
    }

    public void insertAfterUniforms(String... code) {
        insertAfterUniforms(false, code);
    }

    public void insertAfterInVars(String... code) {
        insertAfterInVars(false, code);
    }

    public void insertAfterOutVars(String... code) {
        insertAfterOutVars(false, code);
    }

    public void insertAfterVariable(String variable, String... code) {
        insertAfterVariable(variable, false, code);
    }

    public void insertAfterDefines(boolean first, String... code) {
        transformations.add(insertAfter("#define ", first, code));
    }

    public void insertAfterUniforms(boolean first, String... code) {
        transformations.add(insertAfter("uniform ", first, code));
    }

    public void insertAfterInVars(boolean first, String... code) {
        transformations.add(insertAfter("in ", first, code));
    }

    public void insertAfterOutVars(boolean first, String... code) {
        transformations.add(insertAfter("out ", first, code));
    }

    public void insertAfterVariable(String variable, boolean first, String... code) {
        transformations.add(insertAfter(variable, first, code));
    }

    public void appendToFunction(String function, String... code) {
        transformations.add((src) -> {
            String indentation = getIndentation(0);
            String toInsert = applyIndentation(indentation, code);

            return insertToFunction(src, toInsert, function, -1);
        });
    }

    public void addToFunction(String function, String... code) {
        transformations.add((src) -> {
            String indentation = getIndentation(0);
            String toInsert = applyIndentation(indentation, code);

            return insertToFunction(src, toInsert, function, 1);
        });
    }

    private static String applyIndentation(String indentation, String... code) {
        return "\n" + indentation + String.join("\n" + indentation, code);
    }

    private static Function<String, String> insertAfter(String what, boolean first, String... code) {
        return (src) -> {
            int lastIdx = first ? src.indexOf(what) : src.lastIndexOf(what);

            String indentation = getIndentationForLine(src, lastIdx);
            int newlineIdx = src.indexOf("\n", lastIdx);

            String toInsert = applyIndentation(indentation, code);

            toInsert = replaceParts(src, toInsert);
            return insertAt(newlineIdx, src, toInsert);
        };
    }

    private static String insertToFunction(String src, String code, String function, int offset) {
        int functionIdx = src.indexOf(function);
        if (functionIdx == -1)
            throw new IllegalStateException(
                    "Failed to append code, function '" + function + "' was not found!");

        int firstBracketIdx = src.indexOf('{', functionIdx);
        int bracketCount = 0;

        code = replaceParts(src, code);

        if (offset > 0)
            return insertAt(firstBracketIdx + offset, src, code);
        else if (offset < 0)
            for (int i = firstBracketIdx; i < src.length(); i++) {
                char symbol = src.charAt(i);
                if (symbol == '{')
                    bracketCount++;
                else if (symbol == '}')
                    bracketCount--;

                if (bracketCount == 0)
                    return insertAt(i + offset, src, code);
            }

        throw new IllegalStateException(
                "Failed to append code, end of function '" + function + "' was not found!");
    }

    public String get(String code) {
        for (Function<String, String> function : transformations)
            code = function.apply(code);

        return code;
    }

    public void copyFrom(ShaderInjector injector) {
        transformations.addAll(injector.transformations);
    }

    private static String getIndentationForLine(String str, int pos) {
        String indentation = "";

        for (int i = pos; i >= 0; i--) {
            char ch = str.charAt(i);
            if (ch == '\n')
                break;

            if (ch == ' ' || ch == '\t')
                indentation += ch;
        }

        return indentation;
    }

    private static String getIndentation(int bracketCount) {
        String str = "";
        for (int i = 0; i < bracketCount + 1; i++)
            str += "    ";

        return str;
    }

    private static String insertAt(int i, String original, String target) {
        return original.substring(0, i) + target + original.substring(i);
    }

    private static UniformData getUniformAtLayout(String code, int uniform) {
        int index = code.indexOf("layout(location = " + uniform + ")");
        if (index == -1)
            return new UniformData("vec4", "iris_FragData0");
        String line = "";
        for (int i = index; i < code.length(); i++)
            if (code.charAt(i) == '\n')
                break;
            else
                line += code.charAt(i);

        line = line.replaceAll(";", "");
        String[] splitted = line.split(" ");

        return new UniformData(splitted[splitted.length - 2], splitted[splitted.length - 1]);
    }

    private static String replaceParts(String shaderCode, String toInject) {
        if (toInject.contains("${uniform_0_prefix}")
                || toInject.contains("${uniform_0_postfix}")
                || toInject.contains("${uniform_0}")) {
            UniformData uniform = getUniformAtLayout(shaderCode, 0);
            if (!uniform.type.equals("uvec4") && !uniform.type.equals("vec4"))
                return "";

            boolean isUvec = uniform.type.equals("uvec4");

            toInject = toInject.replaceAll("\\$\\{uniform_0_prefix\\}",
                    isUvec ? "uvec4(" : "");
            toInject = toInject.replaceAll("\\$\\{uniform_0_postfix\\}",
                    isUvec ? ")" : "");
            toInject = toInject.replaceAll("\\$\\{uniform_0\\}",
                    uniform.name);
        }
        return toInject;
    }
}