package jp.co.ntt.oss.jboss.byteman.editor.editor;

import java.util.ArrayList;
import java.util.List;

import jp.co.ntt.oss.jboss.byteman.editor.BytemanEditorPlugin;

import org.eclipse.jdt.internal.ui.text.JavaWordDetector;
import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.RuleBasedScanner;
import org.eclipse.jface.text.rules.WhitespaceRule;
import org.eclipse.jface.text.rules.WordRule;

/**
 * Implementation of {@code RuleBasedScanner} for lines of the specific rule keywords.
 * These keywords clauses need to be processed by a different way from the others.
 */
@SuppressWarnings("restriction")
public class BytemanRuleScannerForDefLine extends RuleBasedScanner {

	public BytemanRuleScannerForDefLine(TokenManager manager) {
		List<IRule> rules = createRules(manager);
		setRules(rules.toArray(new IRule[rules.size()]));
	}

	protected List<IRule> createRules(TokenManager manager){
		List<IRule> rules = new ArrayList<IRule>();

		rules.add(createWordRule(manager));
		rules.add(new WhitespaceRule(new BytemanRuleWhitespaceDetector()));

		return rules;
	}

	protected WordRule createWordRule(TokenManager manager){
		IToken keywordToken = manager.getToken(TokenManager.TOKEN_BYTEMAN_KEYWORD);

		WordRule rule = new BytemanWordRule(new JavaWordDetector(), true);
		for(String word: BytemanEditorPlugin.RULE_KEYWORDS){
			rule.addWord(word, keywordToken);
		}

		return rule;
	}

}
