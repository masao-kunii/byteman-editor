package jp.co.ntt.oss.jboss.byteman.editor.util;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jp.co.ntt.oss.jboss.byteman.editor.BytemanEditorPlugin;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IBuffer;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IImportDeclaration;
import org.eclipse.jdt.core.IInitializer;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.ILocalVariable;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeParameter;
import org.eclipse.jdt.core.ITypeRoot;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.Initializer;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.NodeFinder;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jdt.ui.SharedASTProvider;
import org.eclipse.ui.texteditor.ITextEditor;

/**
 * Provides utility methods for JDT.
 *
 */
public class JavaUtils {

	private static final Pattern PATTERN_METHOD_WITH_PARAM = Pattern.compile("^([A-Za-z0-9<>$]+)\\((.*)\\)$");
	private static final Pattern PATTERN_METHOD_WITH_RETURN = Pattern.compile("^[A-Za-z0-9$.]+ ([A-Za-z0-9<>$]+)\\((.*)\\)$");

	private static Map<IJavaProject, ICompilationUnit>
		unitMap = new HashMap<IJavaProject, ICompilationUnit>();

	/**
	 * Creates the {@code ICompilationUnit} to use temporary.
	 *
	 * @param project the java project
	 * @return the temporary {@code ICompilationUnit}
	 * @throws JavaModelException
	 */
	public synchronized static ICompilationUnit getTemporaryCompilationUnit(
			IJavaProject project) throws JavaModelException {

		if(unitMap.get(project) != null){
			return unitMap.get(project);
		}

		IPackageFragment root = project.getPackageFragments()[0];
		ICompilationUnit unit = root.getCompilationUnit("_xxx.java").getWorkingCopy(
				new NullProgressMonitor());

		unitMap.put(project, unit);

		return unit;
	}


	/**
	 * Set contents of the compilation unit to the translated JSP text.
	 *
	 * @param unit the ICompilationUnit on which to set the buffer contents
	 * @param value Java source code
	 */
	public static void setContentsToCU(ICompilationUnit unit, String value){
		if (unit == null){
			return;
		}

		synchronized (unit) {
			IBuffer buffer;
			try {
				buffer = unit.getBuffer();

			} catch (JavaModelException e) {
				BytemanEditorPlugin.logException(e);
				buffer = null;
			}

			if (buffer != null){
				buffer.setContents(value);
			}
		}
	}

	/**
	 * Returns the line number of the declaration position of the specified method.
	 *
	 * @param member a method
	 * @return the line number
	 * @throws JavaModelException
	 */
	public static int getLineNumberOfMethod(IMember member) throws JavaModelException {
		IType type = member.getDeclaringType();
		String source = type.getCompilationUnit().getSource();
		String sourceUpToMethod = source.substring(0, member.getSourceRange().getOffset());
		Pattern lineEnd = Pattern.compile("$", Pattern.MULTILINE | Pattern.DOTALL);
		return lineEnd.split(sourceUpToMethod).length;
	}

	/**
	 * Returns true if the specified type has constructor.
	 *
	 * @param type the type
	 * @return true if the specified type has constructor, otherwise false
	 * @throws JavaModelException
	 */
	public static boolean hasConstructor(IType type) throws JavaModelException {
		for(IMethod method : type.getMethods()) {
			if(method.isConstructor()) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Returns the method name from the method character string.
	 * The format of the method character string is one of the followings:<br/>
	 * <ul>
	 * <li>[A-Za-z0-9<>$]+\\(.*\\)</li>
	 * <li>[A-Za-z0-9$.]+ [A-Za-z0-9<>$]+</li>
	 * </ul>
	 *
	 * @param targetMethodSpec the method character string
	 * @return the method name
	 */
	public static String getMethodName(String targetMethodSpec) {
		Matcher matcher = PATTERN_METHOD_WITH_PARAM.matcher(targetMethodSpec);
		if(matcher.find()) {
			return matcher.group(1);
		} else {
			Matcher retMatcher = PATTERN_METHOD_WITH_RETURN.matcher(targetMethodSpec);
			if(retMatcher.find()) {
				return retMatcher.group(1);
			} else {
				return targetMethodSpec;
			}
		}
	}

	/**
	 * Returns true if the specified type contains a method of the specified name.
	 *
	 * @param type the type
	 * @param methodName the method name
	 * @return true if the specified type contains a method of the specified name, otherwise false
	 * @throws JavaModelException
	 */
	public static boolean hasMethodByName(IType type, String methodName) throws JavaModelException {
		for(IMethod method : type.getMethods()) {
			if(method.getElementName().equals(methodName)
					|| (method.isConstructor() && "<init>".equals(methodName))) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Returns a list of method which is matched with the given target method specification.
	 *
	 * The format to specify the target method is [methodName(param1, ... paramN)].
	 * e.g. write(String,int)
	 *
	 * @param targetMethodSpec the target method specification
	 * @return matched methods
	 */
	public static IJavaElement findMethod(IType type, String targetMethodSpec) throws JavaModelException {
		Matcher matcher = PATTERN_METHOD_WITH_PARAM.matcher(targetMethodSpec);

		String targetMethodName = null;
		String[] targetParameteres = null;

		if(matcher.find()) {
			// targetMethodSpec has parameters
			targetMethodName = matcher.group(1);
			targetParameteres = StringUtils.isNotEmpty(matcher.group(2)) ? matcher.group(2).split(",") : new String[0];
		} else {
			matcher = PATTERN_METHOD_WITH_RETURN.matcher(targetMethodSpec);
			if(matcher.find()) {
				// targetMethodSpec has return type and parameters
				targetMethodName = matcher.group(1);
				targetParameteres = StringUtils.isNotEmpty(matcher.group(2)) ? matcher.group(2).split(",") : new String[0];
			} else {
				// targetMethodSpec has no parameters or invalid format
				targetMethodName = targetMethodSpec;
				targetParameteres = null;
			}
		}

		// for default constructor
		if(targetMethodName.equals("<init>") &&
				(targetParameteres == null || targetParameteres.length == 0) && !JavaUtils.hasConstructor(type)){
			return type;
		}

		IMethod result = null;

		METHOD:
		for(IMethod method : type.getMethods()) {
			String methodName = method.getElementName();
			if(method.isConstructor()) {
				methodName = "<init>";
			}

			if(targetParameteres == null){
				if(methodName.equals(targetMethodName)){
					if(result != null){
						result = null;
						break METHOD;
					}
					result = method;
				}
			} else {
				ILocalVariable[] parameters = method.getParameters();
				if(methodName.equals(targetMethodName) && parameters.length == targetParameteres.length) {
					for(int i = 0; i < targetParameteres.length; i++) {
						if(!matchParameterType(type, targetParameteres[i].trim(), Signature.toString(parameters[i].getTypeSignature()))) {
							continue METHOD;
						}
					}
					result = method;
					break METHOD;
				}
			}
		}
		return result;
	}

	private static final boolean matchParameterType(IType type, String parameter, String targetParameter) throws JavaModelException {
		// FQCN or primitive
		if(parameter.equals(targetParameter)) {
			return true;
		}
		// inner class
		if(parameter.indexOf("$") != -1) {
			if(parameter.replace("$", ".").equals(targetParameter)) {
				return true;
			}
			if(parameter.substring(parameter.indexOf("$") + 1).equals(targetParameter)) {
				return true;
			}
		}
		// generics type parameter
		if(targetParameter.indexOf("<") != -1 && parameter.equals(targetParameter.substring(0, targetParameter.indexOf("<")))) {
			return true;
		}
		// targetParameter is not FQCN
		if(parameter.startsWith("java.lang.") && parameter.equals("java.lang." + targetParameter)) {
			return true;
		}
		// same package
		if(parameter.equals(type.getPackageFragment().getElementName() + "." + targetParameter)) {
			return true;
		}
		ICompilationUnit unit = type.getCompilationUnit();
		if(unit != null) {
			for(IImportDeclaration imp : unit.getImports()) {
				String importClass = imp.getElementName();
				if(importClass.endsWith("." + targetParameter) && parameter.equals(importClass)) {
					return true;
				} else if(importClass.endsWith(".*") && parameter.equals(importClass.substring(0, importClass.length() - 1) + targetParameter)) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Creates the method character string for the rule of Byteman from the specified method.
	 *
	 * @param method the method
	 * @return the method character string
	 * @throws JavaModelException
	 */
	public static String createReplacementString(IMethod method) throws JavaModelException {
		Map<String, String> typeParameterMap = getTypeParameterMap(method);
		StringBuilder sb = new StringBuilder();
		if(method.isConstructor()) {
			sb.append("<init>");
		} else {
			sb.append(method.getElementName());
		}
		sb.append("(");
		ILocalVariable[] parameters = method.getParameters();
		if(parameters.length > 0) {
			StringBuilder variables = new StringBuilder();
			for(ILocalVariable variable : parameters) {
				if(variables.length() != 0) {
					variables.append(", ");
				}
				String signiture = Signature.toString(variable.getTypeSignature());
				if(typeParameterMap.containsKey(signiture)) {
					signiture = typeParameterMap.get(signiture);
				}
				int typePos = signiture.indexOf("<");
				if(typePos > 0) {
					signiture = signiture.substring(0, typePos);
				}
				variables.append(signiture);
			}
			sb.append(variables);
		}
		sb.append(")");
		return sb.toString();
	}

	private static Map<String, String> getTypeParameterMap(IMethod method) throws JavaModelException {
		Map<String, String> result = new HashMap<String, String>();
		for(ITypeParameter typeParameter : method.getTypeParameters()) {
			if(typeParameter.getBounds().length > 0) {
				result.put(typeParameter.getElementName(), typeParameter.getBounds()[0]);
			} else {
				result.put(typeParameter.getElementName(), "Object");
			}
		}
		for(ITypeParameter typeParameter : method.getDeclaringType().getTypeParameters()) {
			if(typeParameter.getBounds().length > 0) {
				result.put(typeParameter.getElementName(), typeParameter.getBounds()[0]);
			} else {
				result.put(typeParameter.getElementName(), "Object");
			}
		}
		return result;
	}

	/**
	 * Returns the super type of the specified type.
	 *
	 * @param project the project of the type
	 * @param type the type
	 * @return the super type of the specified type or null if no super type found.
	 * @throws JavaModelException
	 */
	public static IType findSuperclass(IJavaProject project, IType type) throws JavaModelException {
		if(type == null || type.getSuperclassName() == null) {
			return null;
		}
		IType ret = project.findType(type.getSuperclassName());
		if(ret != null) {
			return ret;
		}
		ICompilationUnit unit = type.getCompilationUnit();
		if(unit != null) {
			// search import full qualified class.
			for(IImportDeclaration dec : unit.getImports()) {
				String importClass = dec.getElementName();
				if(importClass.endsWith("." + type.getSuperclassName())) {
					return project.findType(importClass);
				}
			}
			// search same package
			ret = project.findType(type.getPackageFragment().getElementName(), type.getSuperclassName());
			if(ret != null) {
				return ret;
			}
			// search import use wild card
			for(IImportDeclaration dec : unit.getImports()) {
				String importClass = dec.getElementName();
				if(importClass.endsWith(".*")) {
					ret = project.findType(importClass.substring(0, importClass.length() - 1) + type.getSuperclassName());
					if(ret != null) {
						return ret;
					}
				}
			}
		}
		return null;
	}

	public static IMember getMemberFromOffset(ITextEditor javaEditor, int offset) throws JavaModelException {
		ITypeRoot element= JavaUI.getEditorInputTypeRoot(javaEditor.getEditorInput());
		CompilationUnit ast = SharedASTProvider.getAST(element, SharedASTProvider.WAIT_YES, null);
		NodeFinder finder= new NodeFinder(ast, offset, 0);
		ASTNode node= finder.getCoveringNode();

		while(node != null) {
			if(node instanceof Initializer) {
				TypeDeclaration typeDeclaration = (TypeDeclaration) node.getParent();
				IType type = (IType) typeDeclaration.resolveBinding().getJavaElement();
				for(IInitializer initializer : type.getInitializers()) {
					if(node.getStartPosition() == initializer.getSourceRange().getOffset()
							&& Flags.isStatic(initializer.getFlags())) {
						return initializer;
					}
				}
			}
			if(node instanceof MethodDeclaration) {
				IMethodBinding binding = ((MethodDeclaration) node).resolveBinding();
				return (IMethod) binding.getJavaElement();
			}
			node = node.getParent();
		}
		return null;
	}

}
