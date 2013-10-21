package jp.co.ntt.oss.jboss.byteman.editor.editor.codeassist;

import jp.co.ntt.oss.jboss.byteman.editor.BytemanEditorPlugin;
import jp.co.ntt.oss.jboss.byteman.editor.util.RuleScripts;
import jp.co.ntt.oss.jboss.byteman.editor.util.RuleScripts.LineInfo;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.text.contentassist.IContextInformationValidator;

/**
 * This class provides code completion for the Byteman rule editor.
 * Actual code completion processing is delegated to implementations of {@link AbstractBytemanAssistProcessor}.
 *
 * @see BytemanRuleKeywordAssistProcessor
 * @see BytemanClassAssistProcessor
 * @see BytemanMethodAssistProcessor
 * @see BytemanHelperAssistProcessor
 * @see BytemanLocationKeywordAssistProcessor
 * @see BytemanBindAssistProcessor
 * @see BytemanConditionAssistProcessor
 * @see BytemanActionAssistProcessor
 */
public class BytemanContentAssistProcessor implements IContentAssistProcessor {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ICompletionProposal[] computeCompletionProposals(ITextViewer viewer, int offset) {
		try {
			AbstractBytemanAssistProcessor assistProcessor = createAssistProcessor(getLine(viewer, offset));
			if(assistProcessor != null) {
				return assistProcessor.computeCompletionProposals(viewer, offset);
			}
		} catch (BadLocationException e) {
			BytemanEditorPlugin.logException(e);
		}
		return null;
	}

	@Override
	public IContextInformation[] computeContextInformation(ITextViewer viewer, int offset) {
		return null;
	}

	@Override
	public char[] getCompletionProposalAutoActivationCharacters() {
		return null;
	}

	@Override
	public char[] getContextInformationAutoActivationCharacters() {
		return null;
	}

	@Override
	public String getErrorMessage() {
		return null;
	}

	@Override
	public IContextInformationValidator getContextInformationValidator() {
		return null;
	}

	/**
	 * Creates instance of {@link AbstractBytemanAssistProcessor} for the specified {@link LineInfo} type.
	 *
	 * @param line the {@link LineInfo} to create a {@link AbstractBytemanAssistProcessor}
	 * @return instance of {@link AbstractBytemanAssistProcessor}
	 * @throws BadLocationException
	 */
	protected AbstractBytemanAssistProcessor createAssistProcessor(LineInfo line) throws BadLocationException {
		switch (line.getType()) {
		case COMMENT:
		case RULE:
		case ENDRULE:
			return null;
		case CLASS:
		case INTERFACE:
			return new BytemanClassAssistProcessor();
		case HELPER:
			return new BytemanHelperAssistProcessor();
		case METHOD:
			return new BytemanMethodAssistProcessor();
		case LOCATION:
			return new BytemanLocationKeywordAssistProcessor();
		case BIND:
			return new BytemanBindAssistProcessor();
		case CONDITION:
			return new BytemanConditionAssistProcessor();
		case ACTION:
			return new BytemanActionAssistProcessor();
		default:
			return new BytemanRuleKeywordAssistProcessor();
		}
	}

	/**
	 * Returns the {@link LineInfo} for the specified offset.
	 *
	 * @param viewer the <code>ITextViewer</code>
	 * @param offset the offset
	 * @return the {@link LineInfo}
	 * @throws BadLocationException
	 */
	protected LineInfo getLine(ITextViewer viewer, int offset) throws BadLocationException {
		IDocument document = viewer.getDocument();
		int currentLine = document.getLineOfOffset(offset);
		for(LineInfo line : RuleScripts.get(document.get()).getAllLines()) {
			if(line.getLineNumber() == currentLine) {
				return line;
			}
		}
		return RuleScripts.EMPTY_LINE;
	}
}
