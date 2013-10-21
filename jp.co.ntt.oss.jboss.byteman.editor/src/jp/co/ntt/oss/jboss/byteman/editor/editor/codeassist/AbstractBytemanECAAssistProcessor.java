package jp.co.ntt.oss.jboss.byteman.editor.editor.codeassist;

import java.io.StringReader;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import jp.co.ntt.oss.jboss.byteman.editor.BytemanEditorPlugin;
import jp.co.ntt.oss.jboss.byteman.editor.BytemanEditorPluginImages;
import jp.co.ntt.oss.jboss.byteman.editor.util.JavaUtils;
import jp.co.ntt.oss.jboss.byteman.editor.util.RuleScripts.RuleScript;
import jp.co.ntt.oss.jboss.byteman.editor.util.StringUtils;

import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.ILocalVariable;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.contentassist.CompletionProposal;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.templates.DocumentTemplateContext;
import org.eclipse.jface.text.templates.Template;
import org.eclipse.jface.text.templates.TemplateContext;
import org.eclipse.jface.text.templates.TemplateContextType;
import org.eclipse.jface.text.templates.TemplateProposal;
import org.eclipse.swt.graphics.Image;
import org.jboss.byteman.java_cup.runtime.Symbol;
import org.jboss.byteman.rule.grammar.ECAGrammarParser;
import org.jboss.byteman.rule.grammar.ECATokenLexer;
import org.jboss.byteman.rule.grammar.ParseNode;
import org.jboss.byteman.rule.helper.Helper;

/**
 * Base class of {@link AbstractBytemanAssistProcessor} for the Bind, Condition, and Action context.
 *
 */
public abstract class AbstractBytemanECAAssistProcessor extends AbstractBytemanAssistProcessor {

	protected static final String[] SPECIAL_VARIABLES = {
		"$!", "$^", "$#", "$*", "$@"
	};

	@Override
	public ICompletionProposal[] computeCompletionProposals(ITextViewer viewer, int offset) {
		RuleScript ruleScript = getCurrentRuleScript();
		if(ruleScript == null){
			return null;
		}

		try {
			String lastWord = StringUtils.getLastWord(viewer.getDocument().get(0, offset), true);

			List<ICompletionProposal> proposals = new ArrayList<ICompletionProposal>();
			computeKeywordCompletionProposals(proposals, viewer, lastWord, offset);
			computeBindCompletionProposals(proposals, ruleScript, lastWord, offset);
			computeVariableProposals(proposals, lastWord, offset);
			computeHelperCompletionProposals(proposals, viewer.getDocument(), getActiveProject(), ruleScript, lastWord, offset);
			return proposals.toArray(new ICompletionProposal[proposals.size()]);
		} catch (Exception e) {
			BytemanEditorPlugin.logException(e);
		}

		return null;
	}

	protected String[] getKeywords() {
		return new String[0];
	}

	protected abstract String getNextKeyword();

	protected void computeKeywordCompletionProposals(
			List<ICompletionProposal> proposals, ITextViewer viewer, String lastWord, int offset) {
		String line = getLineString(viewer, offset).trim();
		String nextKeyword = getNextKeyword();
		if(nextKeyword.startsWith(line.toUpperCase())) {
			proposals.add(new CompletionProposal(nextKeyword, offset - lastWord.length(), lastWord.length(), nextKeyword.length()));
		}
		for(String keyword : getKeywords()) {
			if((keyword.startsWith(lastWord) || keyword.startsWith(lastWord.toUpperCase())) && !keyword.equals(lastWord)) {
				proposals.add(new CompletionProposal(keyword, offset - lastWord.length(), lastWord.length(), keyword.length()));
			}
		}
	}

	protected void computeBindCompletionProposals(
			List<ICompletionProposal> proposals, RuleScript ruleScript, String lastWord, int offset) {
		String fullText = "BIND\n" + ruleScript.getBind() + "\nIF TRUE DO NOTHING";
		ECATokenLexer lexer = new ECATokenLexer(new StringReader(fullText));
		ECAGrammarParser parser = new ECAGrammarParser(lexer);
		// see org.jboss.byteman.rule.Event#createBindings
		Symbol symbol = null;
		try {
			symbol = parser.parse();
		} catch (Throwable t) {
			// ignore
			return;
		}
		ParseNode node = (ParseNode) symbol.value;
		if(node == null || node.getTag() == ParseNode.NOTHING || !(node.getChild(0) instanceof ParseNode)) {
			return;
		}
		ICompletionProposal equalsProposal = null;
		List<ICompletionProposal> bindProposals = new ArrayList<ICompletionProposal>();
		ParseNode eventTree = (ParseNode) node.getChild(0);
		while(eventTree != null) {
			String var = null;
			switch(eventTree.getTag()) {
			case ParseNode.COMMA:
				var = ((ParseNode) ((ParseNode) eventTree.getChild(0)).getChild(0)).getText();
				eventTree = (ParseNode) eventTree.getChild(1);
				break;
			case ParseNode.ASSIGN:
				var = ((ParseNode) eventTree.getChild(0)).getText();
				eventTree = null;
				break;
			default:
				eventTree = null;
			}
			if(var != null && var.startsWith(lastWord)) {
				ICompletionProposal proposal = new CompletionProposal(
						var, offset - lastWord.length(), lastWord.length(),
						var.length(), getBindImage(), null, null, null);
				if(var.equals(lastWord)) {
					equalsProposal = proposal;
				} else {
					bindProposals.add(proposal);
				}
			}
		}
		if(bindProposals.size() > 0) {
			if(equalsProposal != null) {
				proposals.add(equalsProposal);
			}
			proposals.addAll(bindProposals);
		}
	}

	protected void computeVariableProposals(List<ICompletionProposal> proposals, String lastWord, int offset) {
		if(lastWord.equals("$")) {
			for(String variable : SPECIAL_VARIABLES) {
				proposals.add(new CompletionProposal(
						variable, offset - lastWord.length(), lastWord.length(), variable.length(), null,
						variable + " - " + BytemanEditorPlugin.getResourceString("proposal.description.var." + variable), null, null));
			}
		}
	}

	protected void computeHelperCompletionProposals(
			List<ICompletionProposal> proposals, IDocument document, IJavaProject project,
			RuleScript ruleScript, String lastWord, int offset) throws JavaModelException {
		if(project == null || !project.isOpen()) {
			return;
		}
		String helperClass = ruleScript.getTargetHelper().getValue();
		if(StringUtils.isEmpty(helperClass)) {
			helperClass = Helper.class.getName();
		}
		IType type = project.findType(helperClass);
		List<MethodWrapper> methods = new ArrayList<MethodWrapper>();
		if(type != null) {
			for(IMethod method : getMethods(project, type)) {
				methods.add(new MethodWrapper(method));
			}
		} else if(helperClass.equals(Helper.class.getName())) {
			for(Method method : Helper.class.getDeclaredMethods()) {
				methods.add(new MethodWrapper(method));
			}
		}
		Collections.sort(methods, new Comparator<MethodWrapper>() {
			@Override
			public int compare(MethodWrapper o1, MethodWrapper o2) {
				try {
					return o1.createDisplayString().compareTo(o2.createDisplayString());
				} catch (JavaModelException e) {
					return 0;
				}
			}
		});
		Set<String> added = new HashSet<String>();
		for(MethodWrapper method : methods) {
			if(method.getElementName().startsWith(lastWord) && method.isComplementedHelperMethod()) {
				String replacement = method.createReplacementString();
				String display = method.createDisplayString();
				if(added.contains(display)) {
					continue;
				}
				added.add(display);
				TemplateContext context = new DocumentTemplateContext(new TemplateContextType(), document, offset - lastWord.length(), 0);
				proposals.add(new TemplateProposal(
						new Template(display, method.getClassName(), "", replacement, true), context,
						new Region(offset - lastWord.length(), 0), getHelperMethodImage()));
			}
		}
	}

	protected List<IMethod> getMethods(IJavaProject project, IType type) throws JavaModelException {
		List<IMethod> methods = new ArrayList<IMethod>();
		while(type != null && !type.getFullyQualifiedName().equals("java.lang.Object")) {
			methods.addAll(Arrays.asList(type.getMethods()));
			type = JavaUtils.findSuperclass(project, type);
		}
		return methods;
	}

	protected Image getBindImage() {
		return BytemanEditorPluginImages.IMG_FIELD_PRIVATE;
	}

	protected Image getHelperMethodImage() {
		return BytemanEditorPluginImages.IMG_METHOD;
	}

	protected class MethodWrapper {

		private IMethod iMethod;
		private Method method;

		private String displayString;

		protected MethodWrapper(IMethod method) {
			this.iMethod = method;
		}

		protected MethodWrapper(Method method) {
			this.method = method;
		}

		protected boolean isComplementedHelperMethod() throws JavaModelException {
			if(iMethod != null) {
				return !iMethod.isConstructor()
						&& Flags.isPublic(iMethod.getFlags())
						&& !Flags.isStatic(iMethod.getFlags());
			} else {
				return Flags.isPublic(method.getModifiers())
						&& !Flags.isStatic(method.getModifiers());
			}
		}

		protected String getElementName() {
			if(iMethod != null) {
				return iMethod.getElementName();
			} else {
				return method.getName();
			}
		}

		protected List<String> getParameterNames() throws JavaModelException {
			List<String> result = new ArrayList<String>();
			if(iMethod != null) {
				for(ILocalVariable variable : iMethod.getParameters()) {
					result.add(variable.getElementName());
				}
			} else {
				for(int i = 0; i < method.getParameterTypes().length; i++) {
					result.add("arg" + i);
				}
			}
			return result;
		}

		protected String createReplacementString() throws JavaModelException {
			StringBuilder sb = new StringBuilder();
			sb.append(getElementName()).append("(");
			List<String> parameters = getParameterNames();
			if(parameters.size() > 0) {
				StringBuilder variables = new StringBuilder();
				for(String variable : parameters) {
					if(variables.length() != 0) {
						variables.append(", ");
					}
					variables.append(String.format("${%s}", variable));
				}
				sb.append(variables.toString());
			}
			sb.append(")");
			return sb.toString();
		}

		protected String createDisplayString() throws JavaModelException {
			if(displayString == null) {
				if(iMethod != null) {
					displayString = AbstractBytemanECAAssistProcessor.this.createDisplayString(iMethod);
				} else {
					StringBuilder sb = new StringBuilder();
					sb.append(method.getName()).append("(");
					Class<?>[] parameters = method.getParameterTypes();
					if(parameters.length > 0) {
						StringBuilder variables = new StringBuilder();
						for(int i = 0; i < parameters.length; i++) {
							if(i != 0) {
								variables.append(", ");
							}
							variables.append(parameters[i].getCanonicalName()).append(" ").append("arg" + i);
						}
						sb.append(variables);
					}
					sb.append(")").append(" : ").append(method.getReturnType().getCanonicalName());
					displayString = sb.toString();
				}
			}
			return displayString;
		}

		protected String getClassName() {
			if(iMethod != null) {
				return iMethod.getDeclaringType().getFullyQualifiedName();
			} else {
				return method.getDeclaringClass().getCanonicalName();
			}
		}
	}

}
