package com.koteinik.chunksfadein.core;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class ShaderInjector {
    private final List<Function<String, String>> transformations = new ArrayList<>();

    public void addCode(int lineOffset, String... code) {
        transformations.add((src) -> {
            String toInsert = "\n" + String.join("\n", code);

            int newlineIdx = src.indexOf("\n");
            for (int i = 0; i < lineOffset; i++)
                newlineIdx = src.indexOf("\n", newlineIdx + 1);

            return insertAt(newlineIdx, src, toInsert);
        });
    }

    public static void main(String[] args) {
        ShaderInjector test = new ShaderInjector();

        // test.addCode(0, "float test code;", "float test code2;", "float test
        // code3;");
        test.appendToFunction("int main()",
                "float test code;",
                "float test code2;",
                "float test code3;");
        test.addToFunction("int main()",
                "float test code;",
                "float test code2;",
                "float test code3;");

        System.out.println(
                test.get("// test string\nint main() {\n    test = 3;\n    if (test) {\n        test = 4;\n    }\n}"));
    }

    public void appendToFunction(String function, String... code) {
        transformations.add((src) -> {
            String indentation = getIndentation(0);
            String toInsert = "\n" + indentation + String.join("\n" + indentation, code) + "\n";

            return insertToFunction(src, toInsert, function, -1);
        });
    }

    public void addToFunction(String function, String... code) {
        transformations.add((src) -> {
            String indentation = getIndentation(0);
            String toInsert = "\n" + indentation + String.join("\n" + indentation, code);

            return insertToFunction(src, toInsert, function, 1);
        });
    }

    private static String insertToFunction(String src, String code, String function, int offset) {
        int functionIdx = src.indexOf(function);
        if (functionIdx == -1)
            throw new IllegalStateException(
                    "Failed to append code, function '" + function + "' was not found!");

        int firstBracketIdx = src.indexOf('{', functionIdx);
        int bracketCount = 0;

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

    private static String getIndentation(int bracketCount) {
        String str = "";
        for (int i = 0; i < bracketCount + 1; i++)
            str += "    ";

        return str;
    }

    private static String insertAt(int i, String original, String target) {
        return original.substring(0, i) + target + original.substring(i);
    }
}