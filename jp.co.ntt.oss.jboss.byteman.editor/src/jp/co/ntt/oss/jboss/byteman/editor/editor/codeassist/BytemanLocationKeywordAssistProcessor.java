package jp.co.ntt.oss.jboss.byteman.editor.editor.codeassist;

import org.eclipse.jface.text.ITextViewer;

/**
 * Provides code completion for the location keywords.
 *
 */
public class BytemanLocationKeywordAssistProcessor extends BytemanRuleKeywordAssistProcessor {

	@Override
	protected String getLastWord(ITextViewer viewer, int offset) throws Exception {
		return getLineString(viewer, offset);
	}
}
