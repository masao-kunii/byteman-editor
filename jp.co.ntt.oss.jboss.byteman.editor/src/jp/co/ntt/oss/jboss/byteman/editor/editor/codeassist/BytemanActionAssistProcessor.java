package jp.co.ntt.oss.jboss.byteman.editor.editor.codeassist;

/**
 * This class provides code completion for the action context.
 *
 */
public class BytemanActionAssistProcessor extends AbstractBytemanECAAssistProcessor {

	private static final String[] PROPOSAL_KEYWORDS = {
		// rule keywords
		"NOTHING",
		// java keywords
		"new", "return", "throw"
	};

	@Override
	protected String getNextKeyword() {
		return "ENDRULE";
	}

	@Override
	protected String[] getKeywords() {
		return PROPOSAL_KEYWORDS;
	}
}
