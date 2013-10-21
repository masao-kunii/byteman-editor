package jp.co.ntt.oss.jboss.byteman.editor.view.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import jp.co.ntt.oss.jboss.byteman.editor.BytemanEditorPlugin;
import jp.co.ntt.oss.jboss.byteman.editor.BytemanEditorPluginImages;
import jp.co.ntt.oss.jboss.byteman.editor.util.RuleScripts;
import jp.co.ntt.oss.jboss.byteman.editor.util.RuleScripts.LineInfo;
import jp.co.ntt.oss.jboss.byteman.editor.util.RuleScripts.RuleScript;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.graphics.Image;

/**
 * Model object for {@link jp.co.ntt.oss.jboss.byteman.editor.view.BytemanRuleOutlinePage}.
 *
 */
public class RuleOutlineModel {

	private String ruleText;
	private IMarker[] markers;
	private ArrayList<Rule> rules = new ArrayList<Rule>();

	public RuleOutlineModel(IResource resource, String ruleText) {
		if(resource != null) {
			try {
				this.markers = resource.findMarkers(BytemanEditorPlugin.MARKER_ID, false, IResource.DEPTH_ZERO);
			} catch (CoreException e) {
				BytemanEditorPlugin.logException(e);
			}
		} else {
			this.markers = new IMarker[0];
		}
		if(ruleText != null) {
			this.ruleText = ruleText;
			parse();
		}
	}

	/**
	 * Returns a rule or its child model specified by line number.
	 *
	 * @param line a line number to specify the model.
	 * @param isRule {@code true} if to be returned a {@code Rule} model. Returns a child model of the {@code Rule} model if {@code false} is specified.
	 * @return the model specified by the line number. Returns null if the model not found.
	 */
	public OutlineModel getModelOfLine(int line, boolean isRule){
		Rule find = null;
		for(Rule rule: rules){
			if(rule.getLineNumber() > line){
				break;
			}
			if(rule.getLineNumber() <= line && rule.ruleScript.getLineOfEnd() >= line){
				find = rule;
			}
		}
		if(find != null && !isRule){
			for(Object child: find.getChildren()){
				if(((OutlineModel) child).getLineNumber() == line){
					return (OutlineModel) child;
				}
			}
		}
		return find;
	}

	/**
	 * Merges the specified {@link RuleOutlineModel} with this instance.
	 *
	 * @param other a {@link RuleOutlineModel} object to be merged with this instance
	 */
	public void merge(RuleOutlineModel other){
		ruleText = other.ruleText;
		markers = other.markers;

		List<Rule> removeRules = new ArrayList<Rule>();

		for(Rule rule: rules){
			Rule mergeRule = null;
			for(Rule otherRule: other.rules){
				if(rule.getText().equals(otherRule.getText())) {
					mergeRule = otherRule;
					break;
				}
			}
			if(mergeRule != null){
				rule.clazz = mergeRule.clazz;
				rule.location = mergeRule.location;
				rule.marker = mergeRule.marker;
				rule.method = mergeRule.method;
				rule.ruleInfo = mergeRule.ruleInfo;
				rule.ruleScript = mergeRule.ruleScript;

				other.rules.remove(mergeRule);
			} else {
				removeRules.add(rule);
			}
		}

		rules.removeAll(removeRules);

		for(Rule rule: other.rules){
			rules.add(rule);
		}

		Collections.sort(rules, new Comparator<Rule>() {
			@Override
			public int compare(Rule o1, Rule o2) {
				int line1 = o1.getLineNumber();
				int line2 = o2.getLineNumber();
				if(line1 == line2){
					return 0;
				} else if(line1 > line2){
					return 1;
				} else {
					return -1;
				}
			}
		});
	}

	public Object[] getChildren() {
		return rules.toArray(new Rule[rules.size()]);
	}

	protected void parse() {
		for(RuleScript script : RuleScripts.get(ruleText).getRules()) {
			rules.add(new Rule(script, markers));
		}
	}
	/**
	 * Model class for the rule identifier.
	 */
	public static class Rule implements OutlineModel {
		private RuleScript ruleScript;
		private LineInfo ruleInfo;
		private Class clazz = null;
		private Method method = null;
		private Location location = null;
		private IMarker marker;
		private IMarker[] markers;

		public Rule(RuleScript script, IMarker[] markers) {
			this.markers = markers;
			this.ruleScript = script;
			this.ruleInfo = script.getRule();
			if(!script.getTargetClass().equals(RuleScripts.EMPTY_LINE)) {
				clazz = new Class(script.getTargetClass(), script.isInterface(), script.isOverride(), findMarker(script.getTargetClass()));
			}
			if(!script.getTargetMethod().equals(RuleScripts.EMPTY_LINE)) {
				method = new Method(script.getTargetMethod(), findMarker(script.getTargetMethod()));
			}
			if(!script.getTargetLocation().equals(RuleScripts.EMPTY_LINE)) {
				location = new Location(script.getTargetLocation(), findMarker(script.getTargetLocation()));
			}
			setMarker();
		}

		private IMarker findMarker(LineInfo info) {
			for(IMarker marker : markers) {
				if(marker.getAttribute(IMarker.LINE_NUMBER, -1) == info.getLineNumber() + 1) {
					return marker;
				}
			}
			return null;
		}

		private void setMarker() {
			for(IMarker marker : markers) {
				int lineNum = marker.getAttribute(IMarker.LINE_NUMBER, -1) - 1;
				if(ruleInfo.getLineNumber() <= lineNum && lineNum <= ruleScript.getLineOfEnd()) {
					if(this.marker == null) {
						this.marker = marker;
					} else if(marker.getAttribute(IMarker.SEVERITY, -1) == IMarker.SEVERITY_ERROR) {
						this.marker = marker;
					}
				}
			}
		}

		@Override
		public String getText() {
			return ruleInfo.getValue() + " : RULE";
		}

		@Override
		public Image getImage() {
			if(marker == null) {
				return BytemanEditorPluginImages.IMG_RULE;
			} else if(marker.getAttribute(IMarker.SEVERITY, -1) == IMarker.SEVERITY_ERROR) {
				return BytemanEditorPluginImages.IMG_RULE_ERROR;
			} else if(marker.getAttribute(IMarker.SEVERITY, -1) == IMarker.SEVERITY_WARNING) {
				return BytemanEditorPluginImages.IMG_RULE_WARNING;
			} else {
				return BytemanEditorPluginImages.IMG_RULE;
			}
		}
		@Override
		public Object[] getChildren() {
			ArrayList<OutlineModel> children = new ArrayList<OutlineModel>();
			if(clazz != null) children.add(clazz);
			if(method != null) children.add(method);
			if(location != null) children.add(location);
			return children.toArray(new OutlineModel[children.size()]);
		}

		@Override
		public int getLineNumber() {
			return ruleInfo.getLineNumber();
		}

		@Override
		public int getLineOfOffset() {
			return ruleInfo.getLineText(false).indexOf("RULE") +
					(ruleInfo.getLineText().startsWith("RULE ") ? "RULE ".length() : "RULE".length());
		}

		@Override
		public int getLength() {
			return ruleInfo.getValue().length();
		}

		public RuleScript getRuleScript() {
			return ruleScript;
		}
	}

	/**
	 * Model class for the class identifier.
	 */
	public static class Class implements OutlineModel {
		private LineInfo classInfo;
		private boolean isInterface;
		private boolean isOverride;
		private IMarker marker;

		public Class(LineInfo classInfo, boolean isInterface, boolean isOverride, IMarker marker) {
			this.classInfo = classInfo;
			this.isInterface = isInterface;
			this.isOverride = isOverride;
			this.marker = marker;
		}

		@Override
		public String getText() {
			if(isInterface) {
				return classInfo.getValue() + " : INTERFACE";
			} else {
				return classInfo.getValue() + " : CLASS";
			}
		}

		@Override
		public Image getImage() {
			if(isInterface) {
				if(marker == null) {
					return BytemanEditorPluginImages.IMG_INTERFACE;
				} else if(marker.getAttribute(IMarker.SEVERITY, -1) == IMarker.SEVERITY_ERROR) {
					return BytemanEditorPluginImages.IMG_INTERFACE_ERROR;
				} else if(marker.getAttribute(IMarker.SEVERITY, -1) == IMarker.SEVERITY_WARNING) {
					return BytemanEditorPluginImages.IMG_INTERFACE_WARNING;
				} else {
					return BytemanEditorPluginImages.IMG_INTERFACE;
				}
			} else {
				if(marker == null) {
					return BytemanEditorPluginImages.IMG_CLASS;
				} else if(marker.getAttribute(IMarker.SEVERITY, -1) == IMarker.SEVERITY_ERROR) {
					return BytemanEditorPluginImages.IMG_CLASS_ERROR;
				} else if(marker.getAttribute(IMarker.SEVERITY, -1) == IMarker.SEVERITY_WARNING) {
					return BytemanEditorPluginImages.IMG_CLASS_WARNING;
				} else {
					return BytemanEditorPluginImages.IMG_CLASS;
				}
			}
		}

		@Override
		public Object[] getChildren() {
			return null;
		}

		@Override
		public int getLineNumber() {
			return classInfo.getLineNumber();
		}

		@Override
		public int getLineOfOffset() {
			int overrideOffset = isOverride ? 1 : 0;
			if(isInterface) {
				return classInfo.getLineText(false).indexOf("INTERFACE") +
						(classInfo.getLineText().startsWith("INTERFACE ") ? "INTERFACE ".length() : "INTERFACE".length()) + overrideOffset;
			} else {
				return classInfo.getLineText(false).indexOf("CLASS") +
						(classInfo.getLineText().startsWith("CLASS ") ? "CLASS ".length() : "CLASS".length()) + overrideOffset;
			}
		}

		@Override
		public int getLength() {
			return classInfo.getValue().length();
		}
	}

	/**
	 * Model class for the method identifier.
	 */
	public static class Method implements OutlineModel {
		private LineInfo methodInfo;
		private IMarker marker;

		public Method(LineInfo methodInfo, IMarker marker) {
			this.methodInfo = methodInfo;
			this.marker = marker;
		}

		@Override
		public String getText() {
			return methodInfo.getValue() + " : METHOD";
		}

		@Override
		public Image getImage() {
			if(marker == null) {
				return BytemanEditorPluginImages.IMG_METHOD;
			} else if(marker.getAttribute(IMarker.SEVERITY, -1) == IMarker.SEVERITY_ERROR) {
				return BytemanEditorPluginImages.IMG_METHOD_ERROR;
			} else if(marker.getAttribute(IMarker.SEVERITY, -1) == IMarker.SEVERITY_WARNING) {
				return BytemanEditorPluginImages.IMG_METHOD_WARNING;
			} else {
				return BytemanEditorPluginImages.IMG_METHOD;
			}
		}

		@Override
		public Object[] getChildren() {
			return null;
		}

		@Override
		public int getLineNumber() {
			return methodInfo.getLineNumber();
		}

		@Override
		public int getLineOfOffset() {
			return methodInfo.getLineText(false).indexOf("METHOD") +
					(methodInfo.getLineText().startsWith("METHOD ") ? "METHOD ".length() : "METHOD".length());
		}

		@Override
		public int getLength() {
			return methodInfo.getValue().length();
		}
	}

	/**
	 * Model class for the location identifier.
	 */
	public static class Location implements OutlineModel {
		private LineInfo locationInfo;
		private IMarker marker;

		public Location(LineInfo locationInfo, IMarker marker) {
			this.locationInfo = locationInfo;
			this.marker = marker;
		}

		@Override
		public String getText() {
			return locationInfo.getLineText() + " : LOCATION";
		}

		@Override
		public Image getImage() {
			if(marker == null) {
				return BytemanEditorPluginImages.IMG_LOCATION;
			} else if(marker.getAttribute(IMarker.SEVERITY, -1) == IMarker.SEVERITY_ERROR) {
				return BytemanEditorPluginImages.IMG_LOCATION_ERROR;
			} else if(marker.getAttribute(IMarker.SEVERITY, -1) == IMarker.SEVERITY_WARNING) {
				return BytemanEditorPluginImages.IMG_LOCATION_WARNING;
			} else {
				return BytemanEditorPluginImages.IMG_LOCATION;
			}
		}

		@Override
		public Object[] getChildren() {
			return null;
		}

		@Override
		public int getLineNumber() {
			return locationInfo.getLineNumber();
		}

		@Override
		public int getLineOfOffset() {
			return 0;
		}

		@Override
		public int getLength() {
			return locationInfo.getLineText().length();
		}
	}
}