package com.koteinik.chunksfadein.compat.monocle;

import com.koteinik.chunksfadein.compat.iris.IrisPatcher;
import io.github.douira.glsl_transformer.ast.node.TranslationUnit;
import io.github.douira.glsl_transformer.ast.node.Version;
import io.github.douira.glsl_transformer.ast.print.ASTPrinter;
import io.github.douira.glsl_transformer.ast.query.Root;
import io.github.douira.glsl_transformer.ast.query.RootSupplier;
import io.github.douira.glsl_transformer.ast.transform.ASTParser;
import io.github.douira.glsl_transformer.ast.transform.ASTTransformer;
import io.github.douira.glsl_transformer.ast.transform.JobParameters;
import net.irisshaders.iris.pipeline.transform.PatchShaderType;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MonoclePatcher {
	private static final Pattern versionPattern = Pattern.compile("#version\\s+(\\d+)", Pattern.DOTALL);
	private static final ASTTransformer<Parameters, String> transformer;

	static {
		transformer = new ASTTransformer<>() {
			{
				setRootSupplier(RootSupplier.PREFIX_UNORDERED_ED_EXACT);
			}

			@Override
			public TranslationUnit parseTranslationUnit(Root rootInstance, String input) {
				Matcher matcher = versionPattern.matcher(input);
				if (matcher.find())
					transformer.getLexer().version = Version.fromNumber(Integer.parseInt(matcher.group(1)));

				return super.parseTranslationUnit(rootInstance, input);
			}

			@Override
			public String transform(RootSupplier rootSupplier, String input) {
				TranslationUnit tree = parseTranslationUnit(rootSupplier, input);
				Root root = tree.getRoot();

				root.indexBuildSession(() -> patch(
					transformer,
					tree,
					root,
					getJobParameters()
				));

				return ASTPrinter.print(getPrintType(), tree);
			}
		};
	}

	public static String patch(PatchShaderType type, String source) {
		transformer.setJobParameters(new Parameters(type));

		return transformer.transform(source);
	}

	private static void patch(ASTParser t, TranslationUnit tree, Root root, Parameters parameters) {
		IrisPatcher.injectModAndAPI(t, tree, root, parameters.type.glShaderType);
	}

	private record Parameters(PatchShaderType type) implements JobParameters {
		@Override
		public boolean equals(Object obj) {
			if (obj instanceof Parameters other)
				return this.type == other.type;

			return false;
		}
	}
}
