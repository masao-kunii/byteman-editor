package jp.co.ntt.oss.jboss.byteman.editor.editor.codeassist;

/**
 * This class provides code completion for the bind context.
 *
 */
public class BytemanBindAssistProcessor extends AbstractBytemanECAAssistProcessor {

	private static final String[] PROPOSAL_KEYWORDS = {
		// java keywords
		"new"
	};

	@Override
	protected String getNextKeyword() {
		return "IF";
	}

	@Override
	protected String[] getKeywords() {
		return PROPOSAL_KEYWORDS;
	}

}
