package jp.co.ntt.oss.jboss.byteman.editor.action;

import jp.co.ntt.oss.jboss.byteman.editor.BytemanEditorPlugin;
import jp.co.ntt.oss.jboss.byteman.editor.BytemanOutlineInfomationControl;
import jp.co.ntt.oss.jboss.byteman.editor.util.EditorUtils;
import jp.co.ntt.oss.jboss.byteman.editor.util.JavaUtils;
import jp.co.ntt.oss.jboss.byteman.editor.util.RuleScripts;
import jp.co.ntt.oss.jboss.byteman.editor.util.RuleScripts.RuleScript;
import jp.co.ntt.oss.jboss.byteman.editor.util.StringUtils;
import jp.co.ntt.oss.jboss.byteman.editor.util.UIUtils;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;

/**
 * This handler open the source of a class or method which is the current caret location in the Byteman editor.
 */
public class JumpToTargetHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IJavaProject project = getJavaProject();
		if(project == null){
			displayErrorMessage();
			return null;
		}
		RuleScript ruleScript = getRuleScript();
		if(ruleScript == null || ruleScript.getTargetClass().getValue() == null){
			displayErrorMessage();
			return null;
		}

		try {
			IType type = project.findType(ruleScript.getTargetClass().getValue());
			if(type == null) {
				displayErrorMessage();
				return null;
			}
			if(StringUtils.isEmpty(ruleScript.getTargetMethod().getValue())) {
				displayErrorMessage();
				return null;
			}

			String targetMethodSpec = ruleScript.getTargetMethod().getValue();
			IJavaElement targetElement = findTarget(type, targetMethodSpec);

			if(targetElement != null) {
				openInEditor(targetElement);

			} else if(targetMethodSpec.indexOf('(') >= 0 || targetMethodSpec.indexOf(')') >= 0){
				displayErrorMessage();
				return null;

			} else {
				String filterString = "";
				String methodName = JavaUtils.getMethodName(targetMethodSpec);
				if(methodName.equals("<init>")) {
					methodName = type.getElementName();
				}
				if(!JavaUtils.hasMethodByName(type, methodName)) {
					displayErrorMessage();
					return null;
				}
				filterString = methodName + "(*)";
				displayList(type, filterString);
			}
		} catch (JavaModelException e) {
			BytemanEditorPlugin.logException(e);
		}

		return null;
	}

	protected IJavaElement findTarget(IType type, String targetMethod) throws JavaModelException {
		if(targetMethod.startsWith("<clinit>")) {
			return type;
		}
		return JavaUtils.findMethod(type, targetMethod);
	}

	protected IJavaProject getJavaProject(){
		return EditorUtils.getActiveProject();
	}

	protected RuleScript getRuleScript(){
		return RuleScripts.get().getCurrentRuleScript();
	}

	protected void displayErrorMessage(){
		EditorUtils.getActiveEditor().getEditorSite().getActionBars().
			getStatusLineManager().setErrorMessage(BytemanEditorPlugin.getResourceString("error.jump"));
	}

	protected void openInEditor(IJavaElement targetElement){
		EditorUtils.getActiveEditor().getEditorSite().getActionBars().getStatusLineManager().setErrorMessage("");
		UIUtils.openInEditor(targetElement);
	}

	protected void displayList(IJavaElement element, String filterString) {
		EditorUtils.getActiveEditor().getEditorSite().getActionBars().getStatusLineManager().setErrorMessage("");
		BytemanOutlineInfomationControl.open(element, filterString);
	}

}
