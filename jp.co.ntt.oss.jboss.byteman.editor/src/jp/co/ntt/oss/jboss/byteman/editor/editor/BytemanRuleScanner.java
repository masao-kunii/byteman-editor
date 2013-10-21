package jp.co.ntt.oss.jboss.byteman.editor.editor;

import java.util.ArrayList;
import java.util.List;

import jp.co.ntt.oss.jboss.byteman.editor.BytemanEditorPlugin;

import org.eclipse.jdt.internal.ui.text.JavaWordDetector;
import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.RuleBasedScanner;
import org.eclipse.jface.text.rules.SingleLineRule;
import org.eclipse.jface.text.rules.WhitespaceRule;
import org.eclipse.jface.text.rules.WordRule;

/**
 * The default implementation of {@code RuleBasedScanner}.
 */
@SuppressWarnings("restriction")
public class BytemanRuleScanner extends RuleBasedScanner {

	public BytemanRuleScanner(TokenManager manager) {
		List<IRule> rules = createRules(manager);
		setRules(rules.toArray(new IRule[rules.size()]));
	}

	protected List<IRule> createRules(TokenManager manager){
		IToken ruleString = manager.getToken(TokenManager.TOKEN_JAVA_STRING);

		List<IRule> rules = new ArrayList<IRule>();

		rules.add(createWordRule(manager));
		rules.add(new SingleLineRule("\"", "\"", ruleString, '\\'));
		rules.add(new SingleLineRule("'", "'", ruleString, '\\'));
		rules.add(new WhitespaceRule(new BytemanRuleWhitespaceDetector()));

		return rules;
	}

	protected WordRule createWordRule(TokenManager manager){
		IToken keywordToken = manager.getToken(TokenManager.TOKEN_BYTEMAN_KEYWORD);
		IToken javaKeywordToken = manager.getToken(TokenManager.TOKEN_JAVA_KEYWORD);
		IToken returnToken = manager.getToken(TokenManager.TOKEN_JAVA_KEYWORD_RETURN);

		WordRule rule = new BytemanWordRule(new JavaWordDetector(), false);
		for(String word: BytemanEditorPlugin.RULE_EXPRESSION_KEYWORDS){
			rule.addWord(word, keywordToken);
		}

		for(String word: BytemanEditorPlugin.JAVA_KEYWORDS) {
			if(word.equals("return")) {
				rule.addWord(word, returnToken);
			} else {
				rule.addWord(word, javaKeywordToken);
			}
		}

		return rule;
	}

}
