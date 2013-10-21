package jp.co.ntt.oss.jboss.byteman.editor.editor.codeassist;

import java.util.ArrayList;
import java.util.List;

import jp.co.ntt.oss.jboss.byteman.editor.BytemanEditorPlugin;
import jp.co.ntt.oss.jboss.byteman.editor.BytemanEditorPluginImages;
import jp.co.ntt.oss.jboss.byteman.editor.util.JavaUtils;
import jp.co.ntt.oss.jboss.byteman.editor.util.RuleScripts.RuleScript;
import jp.co.ntt.oss.jboss.byteman.editor.util.StringUtils;

import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IInitializer;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.CompletionProposal;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.swt.graphics.Image;

/**
 * Provides code completion for method name and signature.
 */
public class BytemanMethodAssistProcessor extends AbstractBytemanAssistProcessor {

	@Override
	public ICompletionProposal[] computeCompletionProposals(ITextViewer viewer,
			int offset) {
		if(StringUtils.trimHead(getLineString(viewer, offset)).equals("METHOD")) {
			return null;
		}
		RuleScript ruleScript = getCurrentRuleScript();
		IJavaProject project = getActiveProject();
		if(ruleScript == null || project == null || !project.isOpen()){
			return null;
		}

		try {
			String source = viewer.getDocument().get(0, offset);
			String lastWord = StringUtils.getLastWord(source);
			List<ICompletionProposal> proposals = new ArrayList<ICompletionProposal>();

			IType type = project.findType(ruleScript.getTargetClass().getValue());
			if(type == null) {
				return null;
			}
			if(StringUtils.isEmpty(lastWord)) {
				appendStaticInitializer(proposals, type, offset);
			}
			// if there is no constructor, then append the default constructor.
			if(!JavaUtils.hasConstructor(type) && type.getElementName().startsWith(lastWord)) {
				appendDefaultConstructor(proposals, type, offset, lastWord);
			}
			appendMethods(proposals, type, offset, lastWord);
			return proposals.toArray(new ICompletionProposal[proposals.size()]);
		} catch (Exception e) {
			BytemanEditorPlugin.logException(e);
		}
		return null;
	}

	protected void appendStaticInitializer(List<ICompletionProposal> proposals, IType type, int offset) throws JavaModelException {
		for(IInitializer initializer : type.getInitializers()) {
			if(Flags.isStatic(initializer.getFlags())) {
				String replacement = "<clinit>";
				proposals.add(new CompletionProposal(replacement, offset, 0, replacement.length(),
						getStaticInitializerImage(), "static initializer", null, null));
				return;
			}
		}
	}

	protected void appendDefaultConstructor(List<ICompletionProposal> proposals, IType type, int offset, String lastWord) {
		String replacement = "<init>()";
		String display = type.getElementName() + "()" + DESC_CONSTRUCTOR;
		proposals.add(new CompletionProposal(replacement, offset - lastWord.length(),
				lastWord.length(), replacement.length(), getMethodImage(), display, null, null));
	}

	protected void appendMethods(List<ICompletionProposal> proposals, IType type, int offset, String lastWord) throws JavaModelException {
		for(IMethod method : type.getMethods()) {
			String replacement = JavaUtils.createReplacementString(method);
			if(StringUtils.isEmpty(lastWord)
					|| method.getElementName().toLowerCase().startsWith(lastWord.toLowerCase())
					|| (replacement.startsWith(lastWord) && !replacement.equals(lastWord))) {
				String display = createDisplayString(method);
				proposals.add(new CompletionProposal(
						replacement, offset - lastWord.length(), lastWord.length(),
						replacement.length(), getMethodImage(), display, null, null));
			}
		}
	}

	protected Image getMethodImage() {
		return BytemanEditorPluginImages.IMG_METHOD;
	}

	protected Image getStaticInitializerImage() {
		return BytemanEditorPluginImages.IMG_STATIC_INITIALIZER;
	}
}
