package jp.co.ntt.oss.jboss.byteman.editor.action.annotation;

import jp.co.ntt.oss.jboss.byteman.editor.util.RuleScripts.RuleScript;

/**
 * Implementation of {@link BMUnitAnnotation} for a single rule.
 *
 */
public class BMRuleAnnotation extends BMUnitAnnotation {

	private RuleScript ruleScript;

	public BMRuleAnnotation(RuleScript ruleScript) {
		this.ruleScript = ruleScript;
	}

	@Override
	public String getAnnotationString() {
		if(ruleScript != null) {
			return toBMRule(ruleScript);
		} else {
			return null;
		}
	}

}
