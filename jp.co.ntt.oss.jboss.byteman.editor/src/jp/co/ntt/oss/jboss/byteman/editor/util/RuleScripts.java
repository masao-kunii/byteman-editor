package jp.co.ntt.oss.jboss.byteman.editor.util;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jp.co.ntt.oss.jboss.byteman.editor.BytemanEditorPlugin;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.ui.texteditor.ITextEditor;
import org.jboss.byteman.agent.LocationType;

/**
 * The model object for a Byteman rule file.
 * This object contains a list of {@link RuleScript}.
 *
 */
public class RuleScripts {

	private static final Pattern PATTERN_FILE = Pattern.compile("^# File (.*) line .*$");
	private static final Pattern PATTERN_RULE = Pattern.compile("^RULE(.*)$");
	private static final Pattern PATTERN_CLASS = Pattern.compile("^CLASS(.*)$");
	private static final Pattern PATTERN_INTERFACE = Pattern.compile("^INTERFACE(.*)$");
	private static final Pattern PATTERN_METHOD = Pattern.compile("^METHOD(.*)$");
	private static final Pattern PATTERN_HELPER = Pattern.compile("^HELPER(.*)$");
	private static final String KEYWORD_AT = "AT";
	private static final String KEYWORD_AFTER = "AFTER";
	private static final String KEYWORD_BIND = "BIND";
	private static final String KEYWORD_CONDITION = "IF";
	private static final String KEYWORD_ACTION = "DO";
	private static final String KEYWORD_ENDRULE = "ENDRULE";

	public static final LineInfo EMPTY_LINE = new LineInfo(null, -1);

	private ITextEditor editor;

	private String fileName;
	private List<RuleScript> rules = new ArrayList<RuleScripts.RuleScript>();
	private List<LineInfo> defaultHelpers = new ArrayList<RuleScripts.LineInfo>();
	/** Comment, empty, and grammatically invalid lines.  */
	private List<LineInfo> otherLines = new ArrayList<RuleScripts.LineInfo>();
	// for all lines
	private List<LineInfo> allLines = new ArrayList<RuleScripts.LineInfo>();

	/**
	 * Returns the {@link RuleScripts} object which is created from the current active file and editor.
	 * @return the {@link RuleScripts} object which is created from the current active file and editor
	 * @see EditorUtils#getActiveFile()
	 * @see EditorUtils#getActiveEditor()
	 */
	public static RuleScripts get() {
		return new RuleScripts((ITextEditor) EditorUtils.getActiveEditor());
	}

	/**
	 * Returns the {@link RuleScripts} object which is created from the specified file.
	 * @param file the file
	 * @return the {@link RuleScripts} object which is created from the specified file
	 */
	public static RuleScripts get(IFile file) {
		return new RuleScripts(file);
	}

	/**
	 * Returns the {@link RuleScripts} object which is created from the specified rule text.
	 * @param ruleText the rule text
	 * @return the {@link RuleScripts} object which is created from the specified rule text
	 */
	public static RuleScripts get(String ruleText) {
		return new RuleScripts(ruleText);
	}

	private RuleScripts(ITextEditor editor) {
		this.editor = editor;
		fileName = editor.getEditorInput().getName();
		parse(editor.getDocumentProvider().getDocument(editor.getEditorInput()).get().split("\n", -1));
	}

	private RuleScripts(IFile file) {
		fileName = file.getName();
		parse(EditorUtils.getLines(file));
	}

	private RuleScripts(String ruleText) {
		parse(ruleText.split("\n", -1));
	}

	/**
	 * Returns the {@link RuleScript} object of the current cursor position.
	 *
	 * @return the {@link RuleScript} object of the current cursor position
	 */
	public RuleScript getCurrentRuleScript() {
		if(editor != null) {
			try {
				IDocument document = editor.getDocumentProvider().getDocument(editor.getEditorInput());
				ITextSelection selection = (ITextSelection) editor.getSelectionProvider().getSelection();
				int currentLine = document.getLineOfOffset(selection.getOffset());
				return getRuleScript(currentLine);
			} catch (BadLocationException e) {
				BytemanEditorPlugin.logException(e);
			}
		}
		return null;
	}

	/**
	 * Returns the {@link RuleScript} object of the specified line number.
	 *
	 * @return the {@link RuleScript} object of the specified line number
	 */
	protected RuleScript getRuleScript(int lineNumber) {
		for(RuleScript ruleScript : rules) {
			if(ruleScript.getRule().getLineNumber() <= lineNumber
					&& lineNumber <= ruleScript.getLineOfEnd()) {
				return ruleScript;
			}
		}
		return null;
	}

	/**
	 * Returns the list of all {@link RuleScript} in the file.
	 * @return the list of all {@link RuleScript} in the file.
	 */
	public List<RuleScript> getRules() {
		return rules;
	}

	/**
	 * Returns the list of default helper in the file.
	 * @return the list of default helper in the file
	 */
	public List<LineInfo> getDefaultHelpers() {
		return defaultHelpers;
	}

	/**
	 * Returns Comment, empty, and grammatically invalid lines.
	 * @return the Comment, empty, and grammatically invalid lines.
	 */
	public List<LineInfo> getOtherLines() {
		return otherLines;
	}

	/**
	 * Returns the list of all lines in the file.
	 * @return the list of all lines in the file
	 */
	public List<LineInfo> getAllLines() {
		return allLines;
	}

	private void parse(String[] lines) {
		RuleScript rule = null;
		boolean inRule = false;
		for(int i = 0; i < lines.length; i++) {
			LineInfo prev = null;
			if(i != 0) {
				prev = allLines.get(i - 1);
			}
			LineInfo line = new LineInfo(lines[i], i, prev);
			allLines.add(line);
			if(line.getType() == LineType.COMMENT) {
				// get file name when lines were gotten from agent.
				Matcher matcher = PATTERN_FILE.matcher(line.getValue());
				if(matcher.find()) {
					fileName = matcher.group(1);
				}
				// ignore comment
				continue;
			}
			if(line.getType() == LineType.RULE) {
				if(rule != null && !rule.isEnd()) {
					rules.add(rule);
				}
				LineInfo defaultHelper = defaultHelpers.size() > 0 ? defaultHelpers.get(defaultHelpers.size() - 1) : EMPTY_LINE;
				rule = new RuleScript(fileName, defaultHelper);
				rule.add(line);
				inRule = true;
			} else if(!inRule && line.getType() == LineType.HELPER) {
				// empty classanme resets to the default
				if(line.getValue().length() == 0) {
					line.setValue(null);
				}
				defaultHelpers.add(line);
			} else if(line.getType() == LineType.ENDRULE && rule != null && !rule.isEnd) {
				rule.add(line);
				rules.add(rule);
				inRule = false;
			} else if(rule != null) { // for option text ex. trigger method
				rule.add(line);
			} else {
				otherLines.add(line);
			}
		}
		if(rule != null && !rule.isEnd()) {
			rules.add(rule);
		}
	}

	private static String getLineValue(Pattern pattern, String line) {
		if(line != null) {
			line = line.trim();
		}
		Matcher matcher = pattern.matcher(line);
		if(matcher.find()) {
			return matcher.group(1);
		} else {
			return null;
		}
	}

	/**
	 * The model object for a Byteman rule.
	 *
	 */
	public static class RuleScript {
		private boolean isEnd;
		private int lineOfEnd;
		private String file;
		private LineInfo rule;
		private boolean isInterface;
		private boolean isOverride;
		private List<LineInfo> targetClasses;
		private List<LineInfo> targetMethods;
		private List<LineInfo> targetLocations;
		private List<LineInfo> targetHelpers;
		private LineInfo defaultHelper;
		private List<LineInfo> binds;
		private List<LineInfo> conditions;
		private List<LineInfo> actions;
		// option text ex. trigger method
		private List<LineInfo> otherLines;

		private StringBuilder scriptText;

		public RuleScript(String file, LineInfo defaultHelper) {
			this.file = file;
			this.defaultHelper = defaultHelper;
			targetClasses = new ArrayList<LineInfo>();
			targetMethods = new ArrayList<LineInfo>();
			targetLocations = new ArrayList<LineInfo>();
			targetHelpers = new ArrayList<LineInfo>();
			binds = new ArrayList<LineInfo>();
			conditions = new ArrayList<LineInfo>();
			actions = new ArrayList<LineInfo>();
			otherLines = new ArrayList<LineInfo>();
			scriptText = new StringBuilder();
		}

		private void add(LineInfo line) {
			if(isEnd) {
				otherLines.add(line);
				return;
			}
			switch (line.getType()) {
			case RULE:
				rule = line;
				break;
			case CLASS:
			case INTERFACE:
				if(line.getValue().startsWith("^")) {
					line.setValue(line.getValue().substring(1));
					isOverride = true;
				} else {
					isOverride = false;
				}
				targetClasses.add(line);
				isInterface = line.getType() == LineType.INTERFACE;
				break;
			case METHOD:
				targetMethods.add(line);
				break;
			case HELPER:
				targetHelpers.add(line);
				break;
			case LOCATION:
				targetLocations.add(line);
				break;
			case BIND:
				binds.add(line);
				break;
			case CONDITION:
				conditions.add(line);
				break;
			case ACTION:
				actions.add(line);
				break;
			case ENDRULE:
				isEnd = true;
				break;
			default:
				otherLines.add(line);
				break;
			}
			addLine(line.getLineText());
			setLineOfEnd(line.getLineNumber());
		}

		private void addLine(String line) {
			if(scriptText.length() > 0) {
				scriptText.append("\n");
			}
			scriptText.append(line);
		}

		public String getFile() {
			return file;
		}

		public LineInfo getRule() {
			return rule;
		}

		public LineInfo getTargetClass() {
			if(targetClasses.isEmpty()) {
				return EMPTY_LINE;
			} else {
				return targetClasses.get(targetClasses.size() - 1);
			}
		}

		public LineInfo getTargetMethod() {
			if(targetMethods.isEmpty()) {
				return EMPTY_LINE;
			} else {
				return targetMethods.get(targetMethods.size() - 1);
			}
		}

		public LineInfo getTargetLocation() {
			if(targetLocations.isEmpty()) {
				return EMPTY_LINE;
			} else {
				return targetLocations.get(targetLocations.size() - 1);
			}
		}

		public LineInfo getTargetHelper() {
			if(targetHelpers.isEmpty()) {
				return defaultHelper;
			} else {
				return targetHelpers.get(targetHelpers.size() - 1);
			}
		}

		public boolean isInterface() {
			return isInterface;
		}

		public boolean isOverride() {
			return isOverride;
		}

		public String getBind() {
			StringBuilder bind = new StringBuilder();
			for(LineInfo line : binds) {
				if(bind.length() > 0) bind.append("\n");
				bind.append(line.getValue());
			}
			return bind.toString();
		}

		public String getCondition() {
			StringBuilder condition = new StringBuilder();
			for(LineInfo line : conditions) {
				if(condition.length() > 0) condition.append("\n");
				condition.append(line.getValue());
			}
			return condition.toString();
		}

		public String getAction() {
			StringBuilder action = new StringBuilder();
			for(LineInfo line : actions) {
				if(action.length() > 0) action.append("\n");
				action.append(line.getValue());
			}
			return action.toString();
		}

		public String getScriptText() {
			return scriptText.toString();
		}

		public int getLineOfEnd() {
			return lineOfEnd;
		}

		public void setLineOfEnd(int lineOfEnd) {
			this.lineOfEnd = lineOfEnd;
		}

		public List<LineInfo> getTargetClasses() {
			return targetClasses;
		}

		public List<LineInfo> getTargetMethods() {
			return targetMethods;
		}

		public List<LineInfo> getTargetHelpers() {
			return targetHelpers;
		}

		public List<LineInfo> getTargetLocations() {
			return targetLocations;
		}

		public List<LineInfo> getBinds() {
			return binds;
		}

		public List<LineInfo> getConditions() {
			return conditions;
		}

		public List<LineInfo> getActions() {
			return actions;
		}

		public List<LineInfo> getOtherLines() {
			return otherLines;
		}

		public boolean isEnd() {
			return isEnd;
		}
	}

	/**
	 * The model object for the information of the line of the rule file.
	 *
	 */
	public static class LineInfo {

		private String lineText;
		private String value;
		private int lineNumber;
		private LineType type;

		public LineInfo(String lineText, int lineNumber) {
			this(lineText, lineNumber, null);
		}

		public LineInfo(String lineText, int lineNumber, LineInfo prev) {
			this.lineText = lineText;
			this.lineNumber = lineNumber;
			type = (prev == null) ? LineType.type(lineText) : LineType.type(lineText, prev.getType());
			initValue();
		}

		private void initValue() {
			String lineText = getLineText();
			switch (type) {
			case RULE:
				value = getLineValue(PATTERN_RULE, lineText);
				break;
			case CLASS:
				value = getLineValue(PATTERN_CLASS, lineText);
				break;
			case INTERFACE:
				value = getLineValue(PATTERN_INTERFACE, lineText);
				break;
			case METHOD:
				value = getLineValue(PATTERN_METHOD, lineText);
				break;
			case HELPER:
				value = getLineValue(PATTERN_HELPER, lineText);
				break;
			case BIND:
				if(lineText.startsWith(KEYWORD_BIND)) {
					value = lineText.substring(KEYWORD_BIND.length());
				} else {
					value = lineText;
				}
				break;
			case CONDITION:
				if(lineText.startsWith(KEYWORD_CONDITION)) {
					value = lineText.substring(KEYWORD_CONDITION.length());
				} else {
					value = lineText;
				}
				break;
			case ACTION:
				if(lineText.startsWith(KEYWORD_ACTION)) {
					value = lineText.substring(KEYWORD_ACTION.length());
				} else {
					value = lineText;
				}
				break;
			case LOCATION:
			case COMMENT:
			case ENDRULE:
			case OTHER:
				value = lineText;
				break;
			}
		}

		private void setValue(String value) {
			this.value = value;
		}

		public String getLineText() {
			return getLineText(true);
		}

		public String getLineText(boolean trim) {
			if(trim && lineText != null) {
				return lineText.trim();
			} else {
				return lineText;
			}
		}

		public String getValue() {
			return getValue(true);
		}

		public String getValue(boolean trim) {
			if(trim && value != null) {
				return value.trim();
			} else {
				return value;
			}
		}

		public int getLineNumber() {
			return lineNumber;
		}

		public LineType getType() {
			return type;
		}
	}

	public enum LineType {
		COMMENT,
		RULE,
		CLASS,
		INTERFACE,
		METHOD,
		HELPER,
		LOCATION,
		BIND,
		CONDITION,
		ACTION,
		ENDRULE,
		OTHER;

		private static LineType type(String line) {
			if(StringUtils.isEmpty(line)) {
				return OTHER;
			}
			line = line.trim();
			if(line.startsWith("#")) {
				return COMMENT;
			} else if(PATTERN_RULE.matcher(line).matches()) {
				return RULE;
			} else if(PATTERN_CLASS.matcher(line).matches()) {
				return CLASS;
			} else if(PATTERN_INTERFACE.matcher(line).matches()) {
				return INTERFACE;
			} else if(PATTERN_METHOD.matcher(line).matches()) {
				return METHOD;
			} else if(PATTERN_HELPER.matcher(line).matches()) {
				return HELPER;
			} else if(LocationType.type(line) != null
						|| line.toUpperCase().startsWith(KEYWORD_AT)
						|| line.toUpperCase().startsWith(KEYWORD_AFTER)) {
				return LOCATION;
			} else if(line.startsWith(KEYWORD_BIND)) {
				return BIND;
			} else if(line.startsWith(KEYWORD_CONDITION)) {
				return CONDITION;
			} else if(line.startsWith(KEYWORD_ACTION)) {
				return ACTION;
			} else if(line.startsWith(KEYWORD_ENDRULE)) {
				return ENDRULE;
			} else {
				return OTHER;
			}
		}

		private static LineType type(String line, LineType prev) {
			LineType type = type(line);
			if((type == OTHER || type == COMMENT) && prev != null && (prev == BIND || prev == CONDITION || prev == ACTION)) {
				return prev;
			} else {
				return type;
			}
		}
	}
}
