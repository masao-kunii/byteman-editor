package jp.co.ntt.oss.jboss.byteman.editor.validator;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import jp.co.ntt.oss.jboss.byteman.editor.BytemanEditorPlugin;
import jp.co.ntt.oss.jboss.byteman.editor.editor.BytemanRuleEditor;
import jp.co.ntt.oss.jboss.byteman.editor.util.JavaUtils;
import jp.co.ntt.oss.jboss.byteman.editor.util.RuleScripts;
import jp.co.ntt.oss.jboss.byteman.editor.util.RuleScripts.LineInfo;
import jp.co.ntt.oss.jboss.byteman.editor.util.RuleScripts.RuleScript;
import jp.co.ntt.oss.jboss.byteman.editor.util.StringUtils;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IInitializer;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.swt.widgets.Display;
import org.jboss.byteman.agent.Location;
import org.jboss.byteman.agent.LocationType;
import org.jboss.byteman.rule.exception.ParseException;
import org.jboss.byteman.rule.grammar.ECAGrammarParser;
import org.jboss.byteman.rule.grammar.ECATokenLexer;
/**
 * This class validates {@link BytemanRuleEditor} contents.
 */
public class BytemanRuleValidator {

	private static final Pattern PATTERN_JAVA_LANG = Pattern.compile("^java\\.lang$|^java\\.lang\\..*");
	private static final Pattern PATTERN_BYTEMAN = Pattern.compile("^org\\.jboss\\.byteman$|^org\\.jboss\\.byteman\\..*");
	private static final Pattern PATTERN_METHOD = Pattern.compile("^([A-z][A-z0-9._]*\\s+)?[A-z_][A-z0-9_]*\\(\\s*([A-z0-9._]*(\\s*,\\s*)?)*\\s*\\)$");
	private static final Pattern PATTERN_METHOD_NAMEONLY = Pattern.compile("^[A-z_][A-z0-9_]*$|^<init>$");
	private static final Pattern PATTERN_CONSTRUCTOR = Pattern.compile("^<init>(\\(\\s*([A-z0-9._]*(\\s*,\\s*)?)*\\s*\\))?$");
	private static final Pattern PATTERN_INITIALIZER = Pattern.compile("^<clinit>(\\(\\s*\\))?$");

	private static final String LOCATION_USAGE =
			"\n" +
			"\n" +
			"Valid location specifiers are:\n" +
			"  AT ENTRY\n" +
			"  AT EXIT\n" +
			"  AT LINE number\n" +
			"  AT READ [type.]field [count|ALL]\n" +
			"  AT READ $var [count|ALL]\n" +
			"  AFTER READ [type.]field [count|ALL]\n" +
			"  AFTER READ $var [count|ALL]\n" +
			"  AT WRITE [type.]field [count|ALL]\n" +
			"  AT WRITE $var [count|ALL]\n" +
			"  AFTER WRITE [type.]field [count|ALL]\n" +
			"  AFTER WRITE $var [count|ALL]\n" +
			"  AT INVOKE [type.]method [(argtypes)][count|ALL]\n" +
			"  AFTER INVOKE [type.]method [(argtypes)][count|ALL]\n" +
			"  AT SYNCHRONIZE [count|ALL]\n" +
			"  AFTER SYNCHRONIZE [count|ALL]\n" +
			"  AT THROW [count|ALL]";

	private IResource resource;
	private String ruleText;
	private RuleScripts rules;

	/**
	 * Constructor.
	 * @param resource {@code IResource} object for a rule file
	 * @param ruleText Byteman rule texts
	 */
	public BytemanRuleValidator(IResource resource, String ruleText) {
		this.resource = resource;
		this.ruleText = ruleText;
		this.rules = RuleScripts.get(ruleText);
	}

	protected boolean isClassCheck() {
		IJavaProject project = JavaCore.create(resource.getProject());
		return project.exists() && BytemanEditorPlugin.getDefault().getPreferenceStore().getBoolean(BytemanEditorPlugin.PREF_VALIDATION_CLASS_CHECK);
	}

	protected boolean isMethodCheck() {
		return isClassCheck() && BytemanEditorPlugin.getDefault().getPreferenceStore().getBoolean(BytemanEditorPlugin.PREF_VALIDATION_METHOD_CHECK);
	}

	protected boolean isJavaLangCheck() {
		return BytemanEditorPlugin.getDefault().getPreferenceStore().getBoolean(BytemanEditorPlugin.PREF_VALIDATION_JAVA_LANG_CHECK);
	}

	protected IType findType(String name) throws JavaModelException {
		IJavaProject project = JavaCore.create(resource.getProject());
		return project.findType(name);
	}

	protected IJavaElement findMethod(IType type, String methodString) throws JavaModelException {
		return JavaUtils.findMethod(type, methodString);
	}

	protected boolean hasMethodByName(IType type, String methodString) throws JavaModelException {
		return JavaUtils.hasMethodByName(type, JavaUtils.getMethodName(methodString));
	}

	public static boolean validate(IResource resource, String ruleText) {
		BytemanRuleValidator validator = new BytemanRuleValidator(resource, ruleText);
		return validator.validate();
	}

	/**
	 * Validates the rule.
	 * @return {@code true} if no error or warning, otherwise {@code false}
	 * @see jp.co.ntt.oss.jboss.byteman.editor.util.RuleScripts#getOtherLines()
	 */
	public boolean validate() {

		boolean ret = true;
		deleteMarker(resource);

		// validate helpers
		for(LineInfo info : rules.getDefaultHelpers()) {
			ret = validateHelper(info) && ret;
		}

		// validate all rules
		ret = validateRules() && ret;
		// validate other lines
		ret = validateOtherLines(rules.getOtherLines()) && ret;

		return ret;
	}

	/**
	 * Validates the other than actual rule lines. These lines must be empty.
	 * @param otherLines {@link jp.co.ntt.oss.jboss.byteman.editor.util.RuleScripts.LineInfo} to be validated
	 * @return {@code true} if no error or warning, otherwise {@code false}
	 */
	public boolean validateOtherLines(List<LineInfo> otherLines) {
		boolean ret = true;
		for(LineInfo info : otherLines) {
			if(!StringUtils.isEmpty(info.getLineText())) {
				setErrorMarker(BytemanEditorPlugin.getResourceString("error.validate.invalidLines"), info);
				ret = false;
			}
		}
		return ret;
	}

	/**
	 * Validates the rules.
	 * @return {@code true} if no error or warning, otherwise {@code false}
	 */
	public boolean validateRules() {
		boolean ret = true;
		ArrayList<LineInfo> ruleNames = new ArrayList<LineInfo>();
		ArrayList<LineInfo> duplicateRules = new ArrayList<LineInfo>();
		for(RuleScript rule : rules.getRules()) {
			if(!ruleNames.isEmpty()) {
				for(LineInfo info : ruleNames) {
					if(info.getValue().equals(validatedValue(rule.getRule(), "RULE", false))) {
						if(!duplicateRules.contains(info)) {
							duplicateRules.add(info);
						}
						if(!duplicateRules.contains(rule.getRule())) {
							duplicateRules.add(rule.getRule());
						}
					}
				}
			}
			if(!StringUtils.isEmpty(validatedValue(rule.getRule(), "RULE", false))) {
				ruleNames.add(rule.getRule());
			}
			try {
				ret = validateRule(rule) && ret;
				try {
					ret = validateClass(rule) && ret;
					ret = validateMethod(rule) && ret;
				} catch (Exception e) {
					ret = false;
				}
				ret = validateHelpers(rule) && ret;
				ret = validateLocation(rule) && ret;
				ret = validateBind(rule) && ret;
				ret = validateCondition(rule) && ret;
				ret = validateAction(rule) && ret;
			} catch (Exception e) {
				ret = false;
			}
			ret = validateOtherLines(rule.getOtherLines()) && ret;
		}
		for(LineInfo info : duplicateRules) {
			setWarningMarkerAtValue(BytemanEditorPlugin.getResourceString("error.validate.duplicateRule"), info);
			ret = false;
		}
		return ret;
	}

	/**
	 * Validates duplication.
	 * @param list {@link LineInfo} to be validated
	 * @param keyword a keyword
	 * @return {@code true} if no error or warning, otherwise {@code false}
	 */
	public boolean validateOnlyValue(List<LineInfo> list, String keyword) {
		boolean ret = true;
		if(list.size() > 1) {
			for(int index = 0; index < list.size() - 1; index++) {
				setWarningMarker(BytemanEditorPlugin.getResourceString("error.validate.duplicateClause", keyword), list.get(index));
				ret = false;
			}
		}
		return ret;
	}

	/**
	 * Validates a value and returns a trimmed value.
	 * @param info {@link LineInfo} to be validated
	 * @param keyword a keyword
	 * @return the trimmed value. Returns null if the value is invalid
	 */
	public String validatedValue(LineInfo info, String keyword, boolean setMarker) {
		String value = info.getValue(false);
		if(StringUtils.isEmpty(value)) {
			return "";
		} else if(value.charAt(0) != ' ') {
			if(setMarker) {
				setErrorMarker(BytemanEditorPlugin.getResourceString("error.validate.syntaxErrorNoSpace", info.getLineText(), keyword), info);
			}
			return null;
		}
		return value.trim();
	}

	public String validatedValue(LineInfo info, String keyword) {
		return validatedValue(info, keyword, true);
	}

	/**
	 * Validates the rule.
	 * @param rule {@link RuleScript} to be validated
	 * @return {@code true} if no error or warning, otherwise {@code false}
	 * @throws Exception When validation fails
	 */
	public boolean validateRule(RuleScript rule) throws Exception {
		boolean ret = true;
		LineInfo info = rule.getRule();
		String ruleName = validatedValue(info, "RULE");
		if(ruleName == null) {
			return false;
		} else if(StringUtils.isEmpty(ruleName)) {
			setErrorMarker(BytemanEditorPlugin.getResourceString("error.validate.notSpecifiedRule"), info);
			ret = false;
		}

		String[] ruleTextLines = ruleText.split("\n");
		String lastLine = "";
		if(ruleTextLines.length - 1 < rule.getLineOfEnd()) {
			lastLine = ruleTextLines[ruleTextLines.length - 1].trim();
		} else {
			lastLine = ruleTextLines[rule.getLineOfEnd()].trim();
		}

		if(lastLine.startsWith("ENDRULE")) {
			if(!lastLine.equals("ENDRULE")) {
				setWarningMarker(BytemanEditorPlugin.getResourceString("error.validate.invalidLines"), new LineInfo(lastLine, rule.getLineOfEnd()));
				ret = false;
			}
		} else {
			setErrorMarker(BytemanEditorPlugin.getResourceString("error.validate.invalidLines"), info);
			throw new Exception();
		}
		return ret;
	}

	/**
	 * Validates the helpers.
	 * @param rule {@link RuleScript} to be validated
	 * @return {@code true} if no error or warning, otherwise {@code false}
	 */
	public boolean validateHelpers(RuleScript rule) {
		boolean ret = true;
		if(rule.getTargetHelpers().isEmpty()) {
			return true;
		}
		ret = validateOnlyValue(rule.getTargetHelpers(), "HELPER");
		LineInfo info = rule.getTargetHelper();
		if(!StringUtils.isEmpty(info.getLineText())) {
			ret = validateHelper(info) && ret;
		}
		return ret;
	}

	/**
	 * Validates the helper.
	 * @param info {@link LineInfo} to be validated
	 * @return {@code true} if no error or warning, otherwise {@code false}
	 */
	public boolean validateHelper(LineInfo info) {
		String helperName = validatedValue(info, "HELPER");
		if(helperName == null) {
			return false;
		} else if(StringUtils.isEmpty(helperName)) {
			setErrorMarker(BytemanEditorPlugin.getResourceString("error.validate.notSpecifiedHelper"), info);
			return false;
		}

		if(isClassCheck()) {
			try {
				IType type = findType(helperName);
				if(type == null) {
					setErrorMarkerAtValue(BytemanEditorPlugin.getResourceString("error.validate.notFoundClass", helperName), info);
					return false;
				}
				if(!type.isClass() || Flags.isAbstract(type.getFlags())) {
					setErrorMarkerAtValue(BytemanEditorPlugin.getResourceString("error.validate.cannotInstantiate", helperName), info);
					return false;
				}
			} catch (JavaModelException e) {
				BytemanEditorPlugin.logException(e);
				return false;
			}
		}
		return true;
	}

	/**
	 * Validates the class.
	 * @param rule {@link jp.co.ntt.oss.jboss.byteman.editor.util.RuleScripts.RuleScript} to be validated
	 * @return {@code true} if no error or warning, otherwise {@code false}
	 * @throws Exception When validation fails
	 */
	public boolean validateClass(RuleScript rule) throws Exception {

		boolean ret = true;

		// Class is not specified.
		if(rule.getTargetClasses().isEmpty()) {
			setErrorMarker(BytemanEditorPlugin.getResourceString("error.validate.notSpecifiedClassInterface"), rule.getRule());
			return false;
		}

		ret = validateOnlyValue(rule.getTargetClasses(), "CLASS/INTERFACE");
		LineInfo info = rule.getTargetClass();
		if(rule.isOverride()) {
			info = new LineInfo(info.getLineText(), info.getLineNumber(), null);
		}

		String className;
		if(rule.isInterface()) {
			className = validatedValue(info, "INTERFACE");
 		} else {
			className = validatedValue(info, "CLASS");
 		}

		if(className == null) {
			return false;
		} else if(StringUtils.isEmpty(className)) {
			if(rule.isInterface()) {
				setErrorMarker(BytemanEditorPlugin.getResourceString("error.validate.notSpecifiedInterfaceName"), info);
			} else {
				setErrorMarker(BytemanEditorPlugin.getResourceString("error.validate.notSpecifiedClassName"), info);
			}
			return false;
		}

		if(rule.isOverride()) {
			className = className.substring(1);
		}

		if(PATTERN_BYTEMAN.matcher(className).matches()) {
			setWarningMarkerAtValue(BytemanEditorPlugin.getResourceString("error.validate.specifyBytemanPackage"), info);
			return false;
		}

		if(isClassCheck()) {
			try {
				IType type = findType(className);
				if(type == null || (rule.isInterface() && !type.isInterface()) || (!rule.isInterface() && !type.isClass())) {
					if(rule.isInterface()) {
						setWarningMarkerAtValue(BytemanEditorPlugin.getResourceString("error.validate.notFoundInterface", className), info);
					} else {
						setWarningMarkerAtValue(BytemanEditorPlugin.getResourceString("error.validate.notFoundClass", className), info);
					}
					throw new Exception();
				} else {
					if(isJavaLangCheck() &&
							PATTERN_JAVA_LANG.matcher(className).matches()) {
						setWarningMarkerAtValue(BytemanEditorPlugin.getResourceString("error.validate.specifyJavalangPackage"), info);
						return false;
					}
				}
			} catch (JavaModelException e) {
				BytemanEditorPlugin.logException(e);
				throw new Exception();
			}
		}
		return ret;
	}

	/**
	 * Validates the method.
	 * @param rule {@link RuleScript} to be validated
	 * @return {@code true} if no error or warning, otherwise {@code false}
	 */
	public boolean validateMethod(RuleScript rule) {

		boolean ret = true;

		// Method is not specified.
		if(rule.getTargetMethods().isEmpty()) {
			setErrorMarker(BytemanEditorPlugin.getResourceString("error.validate.notSpecifiedMethod"), rule.getRule());
			return false;
		}

		ret = validateOnlyValue(rule.getTargetMethods(), "METHOD");
		LineInfo info = rule.getTargetMethod();

		String methodString = validatedValue(info, "METHOD");
		if(methodString == null) {
			return false;
		} else if(StringUtils.isEmpty(methodString)) {
			setErrorMarker(BytemanEditorPlugin.getResourceString("error.validate.notSpecifiedMethodName"), info);
			return false;
		}
		if(!PATTERN_METHOD.matcher(methodString).matches() &&
				!PATTERN_METHOD_NAMEONLY.matcher(methodString).matches() &&
				!PATTERN_CONSTRUCTOR.matcher(methodString).matches() &&
				!PATTERN_INITIALIZER.matcher(methodString).matches()) {
			setErrorMarkerAtValue(BytemanEditorPlugin.getResourceString("error.validate.invalidMethodSpec"), info);
			return false;
		}

		if(isMethodCheck()) {
			String className;
			String errorType;
			if(rule.isInterface()) {
				className = validatedValue(rule.getTargetClass(), "INTERFACE", false);
				errorType = "interface";
	 		} else {
				className = validatedValue(rule.getTargetClass(), "CLASS", false);
				errorType = "class";
	 		}
			if(StringUtils.isEmpty(className)) {
				return false;
			}

			if(rule.isOverride()) {
				className = className.substring(1);
			}

			try {
				IType type = findType(className);
				if(type == null) {
					return false;
				}
				IJavaElement element = findMethod(type, methodString);
				if(element != null) {
					ret = true && ret;
				} else {
					if(PATTERN_METHOD_NAMEONLY.matcher(methodString).matches()) {
						if(!hasMethodByName(type, methodString)) {
							setWarningMarkerAtValue(BytemanEditorPlugin.getResourceString("error.validate.notFoundMethod", errorType), info);
							return false;
						}
					} else if(PATTERN_INITIALIZER.matcher(methodString).matches()) {
						boolean hasInitializer = false;
						for(IInitializer initializer : type.getInitializers()) {
							if(Flags.isStatic(initializer.getFlags())) {
								hasInitializer = true;
								break;
							}
						}
						if(!hasInitializer) {
							setWarningMarkerAtValue(BytemanEditorPlugin.getResourceString("error.validate.notFoundMethod", errorType), info);
							return false;
						}
					} else {
						setWarningMarkerAtValue(BytemanEditorPlugin.getResourceString("error.validate.notFoundMethod", errorType), info);
						return false;
					}
					ret = true && ret;
				}
			} catch (JavaModelException e) {
				BytemanEditorPlugin.logException(e);
				return false;
			}
		}
		return ret;
	}

	/**
	 * Validated the location.
	 * @param rule {@link RuleScript} to be validated
	 * @return {@code true} if no error or warning, otherwise {@code false}
	 */
	public boolean validateLocation(RuleScript rule) {

		boolean ret = true;
		List<LineInfo> locations = rule.getTargetLocations();

		// Location is not specified.
		if(locations.isEmpty()) {
			return true;
		}

		if(locations.size() > 1) {
			for(int index = 0; index < locations.size() - 1; index++) {
				setWarningMarker(BytemanEditorPlugin.getResourceString("error.validate.duplicateLocation"), locations.get(index));
			}
			ret = false;
		}

		LineInfo info = rule.getTargetLocation();
		String parameters = LocationType.parameterText(info.getLineText());
		LocationType locationType = LocationType.type(info.getLineText());
		if(locationType == null) {
			setErrorMarker(BytemanEditorPlugin.getResourceString("error.validate.invalidLocation", LOCATION_USAGE), info);
			return false;
		}
		Location location = Location.create(locationType, parameters);
		if(location == null) {
			setErrorMarker(BytemanEditorPlugin.getResourceString("error.validate.invalidLocation", LOCATION_USAGE), info);
			return false;
		}
		return ret;
	}

	/**
	 * Validates the binding.
	 * @param rule {@link RuleScript} to be validated
	 * @return {@code true} if no error or warning, otherwise {@code false}
	 */
	public boolean validateBind(RuleScript rule) {

		boolean ret = true;

		if(rule.getBinds().isEmpty()) {
			return true;
		}

		// check of order keywords
		if(!rule.getConditions().isEmpty() && rule.getConditions().get(0).getLineNumber() < rule.getBinds().get(0).getLineNumber()) {
			setErrorMarkerMultiLine(BytemanEditorPlugin.getResourceString("error.validate.InvalidKeywordOrder"), rule.getConditions());
			ret = false;
		}
		if(!rule.getActions().isEmpty() && rule.getActions().get(0).getLineNumber() < rule.getBinds().get(0).getLineNumber()) {
			setErrorMarkerMultiLine(BytemanEditorPlugin.getResourceString("error.validate.InvalidKeywordOrder"), rule.getActions());
			ret = false;
		}

		// check of duplication of binds
		List<LineInfo> bindings = new ArrayList<LineInfo>();
		for(LineInfo info : rule.getBinds()) {
			if(info.getLineText().startsWith("BIND")) {
				bindings.add(info);
			}
		}
		if(bindings.size() > 1) {
			for(LineInfo info : bindings) {
				setErrorMarker(BytemanEditorPlugin.getResourceString("error.validate.duplicateBind"), info);
			}
			return false;
		}

		if(StringUtils.isEmpty(rule.getBind())) {
			setErrorMarkerMultiLine(BytemanEditorPlugin.getResourceString("error.validate.noRuleExpression"), rule.getBinds());
			return false;
		}

		// text of binds parse by byteman
		StringBuilder script = new StringBuilder();
		script.append("BIND ").append(rule.getBind()).append("\n");
		script.append("IF TRUE").append("\n");
		script.append("DO NOTHING");
		try {
			validateScript(rule, script.toString());
		} catch(ParseException e) {
			setErrorMarkerMultiLine(BytemanEditorPlugin.getResourceString("error.validate.invalidBind"), rule.getBinds());
			ret = false;
		}
		return ret;
	}

	/**
	 * Validates the condition.
	 * @param rule {@link RuleScript} to be validated
	 * @return {@code true} if no error or warning, otherwise {@code false}
	 */
	public boolean validateCondition(RuleScript rule) {
		boolean ret = true;
		// condition is not specified
		if(rule.getConditions().isEmpty()) {
			setErrorMarker(BytemanEditorPlugin.getResourceString("error.validate.notSpecifiedIf"), rule.getRule());
			return false;
		}

		// check of order keywords
		if(!rule.getActions().isEmpty() && rule.getActions().get(0).getLineNumber() < rule.getConditions().get(0).getLineNumber()) {
			setErrorMarkerMultiLine(BytemanEditorPlugin.getResourceString("error.validate.InvalidKeywordOrder"), rule.getActions());
			ret = false;
		}

		// check of duplication of conditions
		List<LineInfo> conditions = new ArrayList<LineInfo>();
		for(LineInfo info : rule.getConditions()) {
			if(info.getLineText().startsWith("IF")) {
				conditions.add(info);
			}
		}
		if(conditions.size() > 1) {
			for(LineInfo info : conditions) {
				setErrorMarker(BytemanEditorPlugin.getResourceString("error.validate.duplicateIf"), info);
			}
			return false;
		}

		// condition is empty
		if(StringUtils.isEmpty(rule.getCondition())) {
			setErrorMarkerMultiLine(BytemanEditorPlugin.getResourceString("error.validate.noRuleExpression"), rule.getConditions());
			return false;
		}

		// text of conditions parse by byteman
		StringBuilder script = new StringBuilder();
		script.append("BIND NOTHING").append("\n");
		script.append("IF ").append(rule.getCondition()).append("\n");
		script.append("DO NOTHING");
		try {
			validateScript(rule, script.toString());
		} catch(ParseException e) {
			setErrorMarkerMultiLine(BytemanEditorPlugin.getResourceString("error.validate.invalidIf"), rule.getConditions());
			ret = false;
		}
		return ret;
	}

	/**
	 * Validates for the actions.
	 * @param rule {@link RuleScript} to be validated
	 * @return {@code true} if no error or warning, otherwise {@code false}
	 */
	public boolean validateAction(RuleScript rule) {

		boolean ret = true;

		// action is not specified
		if(rule.getActions().isEmpty()) {
			setErrorMarker(BytemanEditorPlugin.getResourceString("error.validate.notSpecifiedDo"), rule.getRule());
			return false;
		}

		// check of duplication of actions
		// check of duplication of conditions
		List<LineInfo> actions = new ArrayList<LineInfo>();
		for(LineInfo info : rule.getActions()) {
			if(info.getLineText().startsWith("DO")) {
				actions.add(info);
			}
		}
		if(actions.size() > 1) {
			for(LineInfo info : actions) {
				setErrorMarker(BytemanEditorPlugin.getResourceString("error.validate.duplicateDo"), info);
			}
			return false;
		}

		// action is empty
		if(StringUtils.isEmpty(rule.getAction())) {
			setErrorMarkerMultiLine(BytemanEditorPlugin.getResourceString("error.validate.noRuleExpression"), rule.getActions());
			return false;
		}

		// text of actions parse by byteman
		StringBuilder script = new StringBuilder();
		script.append("BIND NOTHING").append("\n");
		script.append("IF TRUE").append("\n");
		script.append("DO ").append(rule.getAction());
		try {
			validateScript(rule, script.toString());
		} catch(ParseException e) {
			setErrorMarkerMultiLine(BytemanEditorPlugin.getResourceString("error.validate.invalidDo"), rule.getActions());
			ret = false;
		}
		return ret;
	}

	protected void validateScript(RuleScript rule, String scriptText) throws ParseException {
        ECAGrammarParser parser = null;
        try {
            String file = rule.getFile();
            ECATokenLexer lexer = new ECATokenLexer(new StringReader(scriptText));
            lexer.setStartLine(0);
            lexer.setFile(file);
            parser = new ECAGrammarParser(lexer);
            parser.setFile(file);
            parser.parse();
            if (parser.getErrorCount() != 0) {
                throw new ParseException(parser.getErrors());
            }
        } catch (ParseException pe) {
            throw pe;
        } catch (Throwable th) {
            throw new ParseException(th.getMessage());
        }
	}

	protected int getOffset(LineInfo info) {
		String[] source = ruleText.split("\n");
		int offset = -1;
		for(int cnt = 0; cnt < info.getLineNumber(); cnt++) {
			offset += source[cnt].length() + 1;
		}
		return offset + 1;
	}

	protected void setErrorMarkerMultiLine(String message, List<LineInfo> lines) {
		if(lines == null || lines.isEmpty()) {
			return;
		}
		if(lines.size() > 1) {
			LineInfo firstInfo = lines.get(0);
			LineInfo lastInfo = lines.get(lines.size() - 1);
			int length = getOffset(lastInfo) - getOffset(firstInfo) + lastInfo.getLineText(false).length();
			setMarker(resource, message, firstInfo.getLineNumber(), getOffset(firstInfo), length, IMarker.SEVERITY_ERROR);
		} else {
			setErrorMarker(message, lines.get(0));
		}
	}

	protected void setErrorMarker(String message, LineInfo info) {
		if(info == null || info.getLineText() == null) {
			return;
		}
		setMarker(resource, message, info.getLineNumber(), getOffset(info), info.getLineText(false).length(), IMarker.SEVERITY_ERROR);
	}

	protected void setWarningMarker(String message, LineInfo info) {
		if(info == null || info.getLineText() == null) {
			return;
		}
		setMarker(resource, message, info.getLineNumber(), getOffset(info), info.getLineText(false).length(), IMarker.SEVERITY_WARNING);
	}

	protected void setErrorMarkerAtValue(String message, LineInfo info) {
		if(info == null || info.getLineText() == null || info.getValue() == null) {
			return;
		}
		int offset = getOffset(info) + info.getLineText(false).length() - info.getValue().length();
		setMarker(resource, message, info.getLineNumber(), offset, info.getValue().length(), IMarker.SEVERITY_ERROR);
	}

	protected void setWarningMarkerAtValue(String message, LineInfo info) {
		if(info == null || info.getLineText() == null || info.getValue() == null) {
			return;
		}
		int offset = getOffset(info) + info.getLineText(false).length() - info.getValue().length();
		setMarker(resource, message, info.getLineNumber(), offset, info.getValue().length(), IMarker.SEVERITY_WARNING);
	}

	/**
	 * Sets the {@code IMarker} for the {@code IResource}.
	 * @param resource {@code IResource} object
	 * @param message the messages
	 * @param lineNum the line number
	 * @param charOffset the offset of the text
	 * @param length the length
	 * @param severity the severity of {@code IMarker}
	 */
	public static void setMarker(final IResource resource, final String message, final int lineNum, final int charOffset, final int length, final int severity) {
		if(resource == null) {
			return;
		}
		Display.getDefault().asyncExec(new Runnable() {
			@Override
			public void run() {
				try {
					IMarker marker = resource.createMarker(BytemanEditorPlugin.MARKER_ID);
					marker.setAttribute(IMarker.LINE_NUMBER, lineNum + 1);
					marker.setAttribute(IMarker.CHAR_START, charOffset);
					marker.setAttribute(IMarker.CHAR_END, charOffset + length);
					marker.setAttribute(IMarker.SEVERITY, severity);
					marker.setAttribute(IMarker.MESSAGE, message);
				} catch (CoreException e) {
					BytemanEditorPlugin.logException(e);
				}
			}
		});
	}

	/**
	 * Deletes all {@code IMarker}.
	 * @param resource {@code IResource}.
	 */
	public static void deleteMarker(final IResource resource) {
		if(resource == null) {
			return;
		}
		Display.getDefault().asyncExec(new Runnable() {
			@Override
			public void run() {
				try {
					resource.deleteMarkers(BytemanEditorPlugin.MARKER_ID, false, IResource.DEPTH_ZERO);
				} catch (CoreException e) {
					BytemanEditorPlugin.logException(e);
				}
			}
		});
	}
}
