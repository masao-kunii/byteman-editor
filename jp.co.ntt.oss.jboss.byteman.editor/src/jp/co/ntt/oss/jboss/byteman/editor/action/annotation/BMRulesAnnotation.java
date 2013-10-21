package jp.co.ntt.oss.jboss.byteman.editor.action.annotation;

import java.util.List;

import jp.co.ntt.oss.jboss.byteman.editor.util.RuleScripts.RuleScript;

/**
 * Implementation of {@link BMUnitAnnotation} for multiple rules.
 *
 */
public class BMRulesAnnotation extends BMUnitAnnotation {

	private List<RuleScript> ruleScripts;

	public BMRulesAnnotation(List<RuleScript> ruleScripts) {
		this.ruleScripts = ruleScripts;
	}

	@Override
	public String getAnnotationString() {
		StringBuilder sb = new StringBuilder();
		sb.append("@BMRules(rules={");
		for(int i = 0; i < ruleScripts.size(); i++) {
			if(i != 0) {
				sb.append(",");
			}
			sb.append(toBMRule(ruleScripts.get(i)));
		}
		sb.append("})");
		return sb.toString();
	}
}
