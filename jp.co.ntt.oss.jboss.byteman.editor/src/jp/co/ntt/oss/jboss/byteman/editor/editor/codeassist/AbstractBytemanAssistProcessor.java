package jp.co.ntt.oss.jboss.byteman.editor.editor.codeassist;

import jp.co.ntt.oss.jboss.byteman.editor.util.EditorUtils;
import jp.co.ntt.oss.jboss.byteman.editor.util.RuleScripts;
import jp.co.ntt.oss.jboss.byteman.editor.util.RuleScripts.RuleScript;

import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.ILocalVariable;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.ICompletionProposal;

/**
 * Base class for the code completion.
 */
public abstract class AbstractBytemanAssistProcessor {

	protected static final String DESC_CONSTRUCTOR = " - constructor";

	public abstract ICompletionProposal[] computeCompletionProposals(ITextViewer viewer, int offset);

	/**
	 * Returns the {@link RuleScript} which the current offset is in.
	 *
	 * @return the {@link RuleScript} of the current offset
	 */
	protected RuleScript getCurrentRuleScript() {
		return RuleScripts.get().getCurrentRuleScript();
	}

	/**
	 * Returns the active project.
	 *
	 * @return the active project
	 * @see EditorUtils#getActiveProject()
	 */
	protected IJavaProject getActiveProject() {
		return EditorUtils.getActiveProject();
	}

	/**
	 * Returns text of a line which the specified offset is in.
	 *
	 * @param viewer <code>ITextViewer</code>
	 * @param offset the offset
	 * @return the text of a line
	 */
	protected String getLineString(ITextViewer viewer, int offset) {
		IDocument document = viewer.getDocument();
		try {
			int lineOffset = document.getLineOffset(document.getLineOfOffset(offset));
			return document.get(lineOffset, offset - lineOffset);
		} catch (BadLocationException e) {
			return "";
		}
	}

	/**
	 * Creates string to display for the specified method.
	 */
	protected String createDisplayString(IMethod method) throws JavaModelException {
		StringBuilder sb = new StringBuilder();
		sb.append(method.getElementName()).append("(");
		ILocalVariable[] parameters = method.getParameters();
		if(parameters.length > 0) {
			StringBuilder variables = new StringBuilder();
			for(ILocalVariable variable : parameters) {
				if(variables.length() != 0) {
					variables.append(", ");
				}
				variables.append(
						Signature.toString(variable.getTypeSignature())).append(" ").append(variable.getElementName());
			}
			sb.append(variables);
		}
		sb.append(")");
		if(method.isConstructor()) {
			sb.append(DESC_CONSTRUCTOR);
		} else {
			sb.append(" : ").append(Signature.toString(method.getReturnType()));
		}
		return sb.toString();
	}
}
