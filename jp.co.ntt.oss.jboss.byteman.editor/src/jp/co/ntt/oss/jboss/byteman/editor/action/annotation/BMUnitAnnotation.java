package jp.co.ntt.oss.jboss.byteman.editor.action.annotation;

import org.eclipse.core.resources.IFile;

import jp.co.ntt.oss.jboss.byteman.editor.util.StringUtils;
import jp.co.ntt.oss.jboss.byteman.editor.util.RuleScripts.RuleScript;

/**
 * The base class to create an annotation for BMUnit.
 *
 */
public abstract class BMUnitAnnotation {

	public abstract String getAnnotationString();

	/**
	 * Returns BMScript annotation string created from the specified file.
	 *
	 * @param file a rule file to convert to a BMScript annotation
	 * @return BMScript annotation string
	 */
	protected String toBMScript(IFile file) {
		StringBuilder sb = new StringBuilder();
		sb.append("@BMScript(")
		  .append("dir=\"").append(file.getParent().getProjectRelativePath().toString()).append("\"")
		  .append(",value=\"").append(file.getName()).append("\"")
		  .append(")");
		return sb.toString();
	}

	/**
	 * Returns BMRule annotation string created from the specified {@link RuleScript}.
	 *
	 * @param ruleScript a rule script to convert to a BMRule annotation
	 * @return BMRule annotation string
	 */
	protected String toBMRule(RuleScript ruleScript) {
		StringBuilder sb = new StringBuilder();
		sb.append("@BMRule(")
		  .append("name=\"").append(ruleScript.getRule().getValue()).append("\"")
		  .append(",targetClass=\"").append(ruleScript.getTargetClass().getValue()).append("\"")
		  .append(",targetMethod=\"").append(ruleScript.getTargetMethod().getValue()).append("\"");
		if(ruleScript.isInterface()) {
			sb.append(",isInterface=true");
		}
		if(ruleScript.isOverride()) {
			sb.append(",isOverriding=true");
		}
		if(StringUtils.isNotEmpty(ruleScript.getTargetLocation().getValue())) {
			sb.append(",targetLocation=\"").append(ruleScript.getTargetLocation().getValue()).append("\"");
		}
		if(StringUtils.isNotEmpty(ruleScript.getTargetHelper().getValue())) {
			sb.append(",helper=\"").append(ruleScript.getTargetHelper().getValue()).append("\"");
		}
		String bind = ruleScript.getBind();
		if(StringUtils.isNotEmpty(bind)) {
			sb.append(",binding=\"").append(escape(bind)).append("\"");
		}
		String condition = ruleScript.getCondition();
		if(StringUtils.isNotEmpty(condition)) {
			sb.append(",condition=\"").append(escape(condition)).append("\"");
		}
		String action = ruleScript.getAction();
		if(StringUtils.isNotEmpty(action)) {
			sb.append(",action=\"").append(escape(action)).append("\"");
		}
		sb.append(")");
		return sb.toString();
	}

	/**
	 * Escapes the double quotations in a specified {@code String}.
	 *
	 * @param target a target string
	 * @return the escaped string
	 */
	protected String escape(String target) {
		StringBuilder sb = new StringBuilder();
		for(int i = 0; i < target.length(); i++) {
			char c = target.charAt(i);
			if(c == '"') {
				sb.append("\\\"");
			} else if(c == '\\') {
				sb.append("\\\\");
			} else {
				sb.append(c);
			}
		}
		return sb.toString();
	}
}
