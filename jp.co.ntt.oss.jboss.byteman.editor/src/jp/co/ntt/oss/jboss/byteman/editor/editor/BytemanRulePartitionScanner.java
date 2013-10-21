package jp.co.ntt.oss.jboss.byteman.editor.editor;

import java.util.ArrayList;
import java.util.List;

import jp.co.ntt.oss.jboss.byteman.editor.BytemanEditorPlugin;

import org.eclipse.jface.text.rules.EndOfLineRule;
import org.eclipse.jface.text.rules.ICharacterScanner;
import org.eclipse.jface.text.rules.IPredicateRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.RuleBasedPartitionScanner;
import org.eclipse.jface.text.rules.Token;

/**
 * The partition scanner for {@link BytemanRuleEditor}.
 *
 */
public class BytemanRulePartitionScanner extends RuleBasedPartitionScanner {

	public final static String RULE_DEF_LINE = "__rule_def_line";
	public final static String RULE_COMMENT = "__rule_comment";

	public BytemanRulePartitionScanner() {

		List<IPredicateRule> rules = new ArrayList<IPredicateRule>();

		rules.add(new BytemaCommentRule());

		for(String keyword: BytemanEditorPlugin.RULE_KEYWORDS){
			rules.add(new BytemanSingleLineRule(keyword));
		}

		setPredicateRules(rules.toArray(new IPredicateRule[rules.size()]));
	}

	/**
	 * The rule of the comment partition for {@link BytemanRuleEditor}.
	 * This class extends {@code org.eclipse.jface.text.rules.EndOfLineRule}
	 * for the comment part of the rule texts.
	 *
	 */
	private static class BytemaCommentRule extends EndOfLineRule {

		public BytemaCommentRule() {
			super("#", new Token(RULE_COMMENT));
		}

		public IToken evaluate(ICharacterScanner scanner) {
			if(scanner.getColumn() != 0){
				scanner.unread();
				char c = (char) scanner.read();
				if(c == '$'){
					return Token.UNDEFINED;
				}
			}
			return super.evaluate(scanner);
		}
	}

	private static class BytemanSingleLineRule extends EndOfLineRule {

		public BytemanSingleLineRule(String word){
			super(word, new Token(RULE_DEF_LINE));
		}

		public IToken evaluate(ICharacterScanner scanner) {
			int count = 0;
			try {
				char c = 0;
				while(scanner.getColumn() != 0){
					scanner.unread();
					c = (char) scanner.read();
					if(c != ' ' && c != '\t'){
						break;
					}
					scanner.unread();
					count++;
				}
				if(c != '\n' && scanner.getColumn() != 0){
					return Token.UNDEFINED;
				}
			} finally {
				for(int i = 0; i < count; i++){
					scanner.read();
				}
			}
			return super.evaluate(scanner);
		}

	}
}
