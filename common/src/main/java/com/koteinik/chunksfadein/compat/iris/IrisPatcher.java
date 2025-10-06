package com.koteinik.chunksfadein.compat.iris;

import com.koteinik.chunksfadein.core.FadeShader;
import io.github.douira.glsl_transformer.ast.data.ChildNodeList;
import io.github.douira.glsl_transformer.ast.node.Identifier;
import io.github.douira.glsl_transformer.ast.node.TranslationUnit;
import io.github.douira.glsl_transformer.ast.node.Version;
import io.github.douira.glsl_transformer.ast.node.declaration.InterfaceBlockDeclaration;
import io.github.douira.glsl_transformer.ast.node.declaration.TypeAndInitDeclaration;
import io.github.douira.glsl_transformer.ast.node.expression.Expression;
import io.github.douira.glsl_transformer.ast.node.expression.LiteralExpression;
import io.github.douira.glsl_transformer.ast.node.expression.ReferenceExpression;
import io.github.douira.glsl_transformer.ast.node.expression.binary.AssignmentExpression;
import io.github.douira.glsl_transformer.ast.node.expression.unary.FunctionCallExpression;
import io.github.douira.glsl_transformer.ast.node.expression.unary.MemberAccessExpression;
import io.github.douira.glsl_transformer.ast.node.external_declaration.DeclarationExternalDeclaration;
import io.github.douira.glsl_transformer.ast.node.external_declaration.ExternalDeclaration;
import io.github.douira.glsl_transformer.ast.node.external_declaration.FunctionDefinition;
import io.github.douira.glsl_transformer.ast.node.statement.Statement;
import io.github.douira.glsl_transformer.ast.node.type.FullySpecifiedType;
import io.github.douira.glsl_transformer.ast.node.type.qualifier.*;
import io.github.douira.glsl_transformer.ast.node.type.specifier.BuiltinNumericTypeSpecifier;
import io.github.douira.glsl_transformer.ast.print.ASTPrinter;
import io.github.douira.glsl_transformer.ast.query.Root;
import io.github.douira.glsl_transformer.ast.query.RootSupplier;
import io.github.douira.glsl_transformer.ast.transform.ASTInjectionPoint;
import io.github.douira.glsl_transformer.ast.transform.ASTParser;
import io.github.douira.glsl_transformer.ast.transform.ASTTransformer;
import io.github.douira.glsl_transformer.ast.transform.JobParameters;
import io.github.douira.glsl_transformer.ast.traversal.ASTListener;
import io.github.douira.glsl_transformer.ast.traversal.ASTWalker;
import io.github.douira.glsl_transformer.util.Type;
import net.irisshaders.iris.pipeline.transform.PatchShaderType;
import net.irisshaders.iris.pipeline.transform.parameter.SodiumParameters;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// this class is just a mess, don't look here please
public class IrisPatcher {
	public static ThreadLocal<String> currentShaderName = ThreadLocal.withInitial(() -> null);
	private static final Set<String> sorterWhitelist = new HashSet<>() {
		{
			add("getVertexPosition");
			add("u_RegionOffset");
			add("_get_draw_translation");
			add("_get_relative_chunk_coord");
		}
	};
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

				root.indexBuildSession(() -> internalInjectVarsAndDummyAPI(
					transformer,
					tree,
					getJobParameters()
				));

				return ASTPrinter.print(getPrintType(), tree);
			}
		};
	}

	private static void internalInjectVarsAndDummyAPI(ASTParser t, TranslationUnit tree, Parameters parameters) {
		if (hasFn(tree, "_cfi_injected"))
			return;

		FadeShader shader = new FadeShader();

		boolean inject = !hasFn(tree, "_cfi_noInjectMarker");

		switch (parameters.type) {
			case VERTEX:
				tree.parseAndInjectNodes(
					t, ASTInjectionPoint.BEFORE_FUNCTIONS, shader
						.vertInVars().flushList().stream()
				);

				tree.parseAndInjectNodes(
					t, ASTInjectionPoint.BEFORE_FUNCTIONS,
					shader.dummyApiVertGetFadeData().flushSingleLine(),
					shader.dummyApiVertCalculateDisplacement().flushSingleLine(),
					shader.dummyApiVertCalculateDisplacement2().flushSingleLine(),
					shader.dummyApiVertCalculateCurvature().flushSingleLine(),
					shader.dummyApiVertCalculateCurvature2().flushSingleLine()
				);

				if (!inject)
					break;

				tree.parseAndInjectNodes(
					t, ASTInjectionPoint.BEFORE_FUNCTIONS, shader
						.vertOutVars().flushList().stream()
				);
				break;

			case FRAGMENT:
				if (!inject)
					break;

				tree.parseAndInjectNodes(
					t, ASTInjectionPoint.BEFORE_FUNCTIONS,
					shader.dummyApiFragCalculateFade().flushSingleLine(),
					shader.dummyApiFragApplyFade().flushSingleLine(),
					shader.dummyApiFragApplyFogFade().flushSingleLine(),
					shader.dummyApiFragApplySkyLodFade().flushSingleLine(),
					shader.dummyApiFragSampleSkyLodTexture().flushSingleLine()
				);

				tree.parseAndInjectNodes(
					t, ASTInjectionPoint.BEFORE_FUNCTIONS, shader
						.fragInVars().flushList().stream()
				);
				break;

			default:
				return;
		}

		tree.parseAndInjectNodes(t, ASTInjectionPoint.END, "void _cfi_injected() {}");
	}

	public static String injectVarsAndDummyAPI(PatchShaderType type, String source) {
		if (source.contains("_cfi_ignoreMarker")
			|| (!source.contains("cfi_") && !source.contains("CFI_") && !source.contains("CHUNKS_FADE_IN_")))
			return source;

		transformer.setJobParameters(new Parameters(type));

		return transformer.transform(source);
	}

	public static void injectModAndAPI(ASTParser t, TranslationUnit tree, Root root, SodiumParameters parameters) {
		FadeShader shader = new FadeShader();

		boolean injected = hasFn(tree, "_cfi_injected");

		boolean inject = !hasFn(tree, "_cfi_noInjectMarker");
		boolean injectMod = !hasFn(tree, "_cfi_noInjectModMarker");
		boolean injectFragMod = injectMod && !hasFn(tree, "_cfi_noInjectFragModMarker");
		boolean injectVertMod = injectMod && !hasFn(tree, "_cfi_noInjectVertModMarker");
		boolean injectCurvature = injectMod && !hasFn(tree, "_cfi_noCurvatureMarker");

		switch (parameters.type.glShaderType) {
			case VERTEX:
				removeFn(tree, "cfi_getFadeData");
				removeFn(tree, "cfi_calculateDisplacement");
				removeFn(tree, "cfi_calculateCurvature");

				if (!injected)
					tree.parseAndInjectNodes(
						t, ASTInjectionPoint.BEFORE_FUNCTIONS, shader.vertInVars().flushList().stream()
					);

				tree.injectNodes(
					ASTInjectionPoint.BEFORE_FUNCTIONS,
					parseDeclarations(
						t, root,
						shader.apiVertGetFadeData("_draw_id").flushSingleLine(),
						shader.apiVertCalculateDisplacement().flushSingleLine(),
						shader.apiVertCalculateDisplacement2().flushSingleLine(),
						shader.apiVertCalculateCurvature().flushSingleLine(),
						shader.apiVertCalculateCurvature2().flushSingleLine()
					)
				);

				if (!inject)
					break;

				shader
					.newLine("vec3 position = getVertexPosition().xyz;")
					.vertInitOutVarsDrawId("_vert_position", "_draw_id");

				if (injectVertMod)
					shader.vertInitMod("_vert_position", "position", true, "vec3(_draw_id)", injectCurvature);

				tree.appendFunctionBody("_vert_init", parseStatements(t, root, shader.flushArray()));

				if (!injected)
					tree.parseAndInjectNodes(
						t, ASTInjectionPoint.BEFORE_FUNCTIONS, shader
							.vertOutVars().flushList().stream()
					);

				break;

			case FRAGMENT:
				if (!inject)
					return;

				removeFn(tree, "cfi_sampleSkyLodTexture");
				removeFn(tree, "cfi_applySkyLodFade");
				removeFn(tree, "cfi_applyFogFade");
				removeFn(tree, "cfi_applyFade");
				removeFn(tree, "cfi_calculateFade");

				tree.injectNodes(
					ASTInjectionPoint.BEFORE_FUNCTIONS,
					parseDeclarations(
						t, root,
						shader.apiFragCalculateFade().flushSingleLine(),
						shader.apiFragApplyFade().flushSingleLine(),
						shader.apiFragApplyFogFade().flushSingleLine(),
						shader.apiFragApplySkyLodFade().flushSingleLine(),
						shader.apiFragSampleSkyLodTexture().flushSingleLine()
					)
				);

				if (!injected)
					tree.parseAndInjectNodes(
						t, ASTInjectionPoint.BEFORE_FUNCTIONS, shader
							.fragInVars().flushList().stream()
					);

				if (injectFragMod) {
					injectFragMod(t, tree, root);
				}

				break;

			default:
				break;
		}

		sortUses(tree);
	}

	public static void injectFragMod(ASTParser t, TranslationUnit tree, Root root) {
		List<Layout> layouts = findOutputColors(tree);
		if (layouts.isEmpty())
			return;

		FadeShader shader = new FadeShader();

		Layout first = layouts.getFirst();
		Type type = first.type;
		String name = first.name;

		List<String> packers = root.identifierIndex.index.keySet()
			.stream()
			.filter(s -> s.contains("pack"))
			.toList();
		if (packers.isEmpty()) {
			if (type == Type.FLOAT32)
				tree.appendMainFunctionBody(parseStatements(
					t, root, shader
						.calculateFade("float fade = ")
						.newLine(name + " *= fade;")
						.flushMultiline()
				));

			if (type == Type.F32VEC3 || type == Type.F32VEC4)
				tree.appendMainFunctionBody(parseStatements(
					t, root, shader
						.fragColorMod(name + ".rgb")
						.flushMultiline()
				));

			return;
		}

		Map<FunctionDefinition, Map<String, MixVar>> vars = new HashMap<>();

		packers.forEach(packer -> root.identifierIndex.get(packer)
			.forEach(i -> {
				AssignmentExpression assignment = i.getAncestor(AssignmentExpression.class);

				if (assignment == null)
					return;

				if (!(assignment.getLeft() instanceof MemberAccessExpression memberAccess))
					return;

				if (!(memberAccess.getOperand() instanceof ReferenceExpression ref))
					return;

				if (!(name.equals(ref.getIdentifier().getName())))
					return;

				String member = memberAccess.getMember().getName();
				if (!"xyzwrgba".contains(member))
					return;

				FunctionCallExpression call = i.getAncestor(FunctionCallExpression.class);
				if (call == null)
					return;

				for (Expression param : call.getParameters()) {
					if (!(param instanceof MemberAccessExpression paramAccess))
						continue;

					if (!(paramAccess.getOperand() instanceof ReferenceExpression paramRef))
						continue;

					String varName = paramRef.getIdentifier().getName();
					if (!varName.contains("color")) // possibly more robust color detection?
						continue;

					String paramMember = paramAccess.getMember().getName();
					if (!"xyzwrgba".contains(paramMember))
						continue;

					FunctionDefinition fn = assignment.getAncestor(FunctionDefinition.class);
					ChildNodeList<Statement> body = fn.getBody().getStatements();

					int idx = body.indexOf(assignment.getAncestor(Statement.class));

					Map<String, MixVar> fnVars = vars.computeIfAbsent(fn, k -> new HashMap<>());

					MixVar var = fnVars.get(varName);
					if (var == null) {
						fnVars.put(varName, new MixVar(varName, paramMember, idx));
					} else {
						var.components += paramMember;
						var.firstUse = Math.min(var.firstUse, idx);
					}
				}
			}));

		vars.forEach(
			(fn, vs) -> {
				ChildNodeList<Statement> body = fn.getBody().getStatements();

				vs.values().forEach(v -> body.addAll(
					v.firstUse, parseStatements(
						t,
						root,
						shader
							.fragColorMod(v.name + "." + v.components, "iris_FogColor." + v.components)
							.flushMultiline()
					)
				));
			}
		);
	}

	public static void sortUses(TranslationUnit tree) {
		// run two times because of some edge cases
		for (int a = 0; a < 2; a++)
			tree.getRoot().identifierIndex.index.entrySet().stream()
				.filter(e -> sorterWhitelist.contains(e.getKey()) ||
					e.getKey().startsWith("_cfi_") ||
					(!e.getKey().startsWith("_") && e.getKey().contains("cfi_")))
				.forEach(e -> {
					ChildNodeList<ExternalDeclaration> children = tree.getChildren();

					ExternalDeclaration declaration = null;
					int firstUseIdx = -1;

					for (Identifier id : e.getValue()) {
						FunctionDefinition fnDefinition = id.getBranchAncestor(
							FunctionDefinition.class,
							FunctionDefinition::getFunctionPrototype
						);
						DeclarationExternalDeclaration externalDeclaration = id.getBranchAncestor(
							DeclarationExternalDeclaration.class,
							DeclarationExternalDeclaration::getDeclaration
						);
						if (externalDeclaration != null && e.getKey()
							.equals("cfi_ChunkFadeData"))
							if (externalDeclaration.getDeclaration() instanceof InterfaceBlockDeclaration intDeclaration) {
								if (!intDeclaration.getBlockName()
									.getName()
									.equals(e.getKey())) {
									externalDeclaration = null;
								}
							}

						if (fnDefinition == null && externalDeclaration == null) {
							ExternalDeclaration child = id.getAncestor(
								FunctionDefinition.class);
							if (child == null)
								child = id.getAncestor(DeclarationExternalDeclaration.class);

							int i = children.indexOf(child);

							if (i != -1 && (firstUseIdx == -1 || firstUseIdx > i))
								firstUseIdx = i;
						}

						if (declaration == null) {
							if (fnDefinition != null)
								declaration = fnDefinition;
							else if (externalDeclaration != null)
								declaration = externalDeclaration;
						}
					}

					if (firstUseIdx == -1)
						return;

					if (declaration != null && children.indexOf(declaration) > firstUseIdx) {
						declaration.detach();
						children.add(firstUseIdx, declaration);
					}
				});
	}

	private static List<Layout> findOutputColors(TranslationUnit tree) {
		List<Layout> colors = new ArrayList<>();

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

						if (!(namedPart.getExpression() instanceof LiteralExpression idxExpr))
							continue;

						int idx = (int) idxExpr.getInteger();
						if (idx >= colors.size())
							colors.addAll(Collections.nCopies(idx - colors.size() + 1, null));

						colors.add(
							idx, new Layout(
								numSpecifier.type,
								declaration.getMembers().getFirst().getName().getName()
							)
						);
					}
				}
			}
		};

		ASTWalker.walk(listener, tree);

		return colors;
	}

	private static void removeFn(TranslationUnit tree, String name) {
		try {
			for (int i = 0; i < 2; i++)
				tree.getOneFunctionDefinitionBody(name)
					.getParent()
					.detachAndDelete();
		} catch (Exception e) {
		}
	}

	public static boolean hasFn(TranslationUnit tree, String name) {
		try {
			tree.getOneFunctionDefinitionBody(name);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	public static List<ExternalDeclaration> parseDeclarations(ASTParser t, Root root, String... input) {
		if (input.length == 0 || Arrays.stream(input).allMatch(String::isBlank))
			return List.of();

		return t.parseExternalDeclarations(root, input);
	}

	public static List<Statement> parseStatements(ASTParser t, Root root, String... input) {
		if (input.length == 0 || Arrays.stream(input).allMatch(String::isBlank))
			return List.of();

		return t.parseStatements(root, input);
	}

	private record Parameters(PatchShaderType type) implements JobParameters {
		@Override
		public boolean equals(Object obj) {
			if (obj instanceof Parameters other)
				return this.type == other.type;

			return false;
		}
	}

	private record Layout(Type type, String name) {}

	private static class MixVar {
		private String name;
		private String components;
		private int firstUse;

		private MixVar(String name, String components, int firstUse) {
			this.name = name;
			this.components = components;
			this.firstUse = firstUse;
		}

		@Override
		public String toString() {
			return "MixVar{" +
				"name='" + name + '\'' +
				", components='" + components + '\'' +
				", firstUse=" + firstUse +
				'}';
		}
	}
}
