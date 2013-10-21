package jp.co.ntt.oss.jboss.byteman.editor.editor.codeassist;

/**
 * This class provides code completion for the condition context.
 */
public class BytemanConditionAssistProcessor extends AbstractBytemanECAAssistProcessor {

	private static final String[] PROPOSAL_KEYWORDS = {
		// rule keywords
		"TRUE", "FALSE",
		// java keywords
		"new"
	};

	@Override
	protected String getNextKeyword() {
		return "DO";
	};

	@Override
	protected String[] getKeywords() {
		return PROPOSAL_KEYWORDS;
	}

}
