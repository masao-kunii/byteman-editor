package jp.co.ntt.oss.jboss.byteman.editor.action.template;

import jp.co.ntt.oss.jboss.byteman.editor.BytemanEditorPlugin;
import jp.co.ntt.oss.jboss.byteman.editor.util.JavaUtils;
import jp.co.ntt.oss.jboss.byteman.editor.util.UIUtils;
import jp.co.ntt.oss.jboss.byteman.editor.wizard.NewRuleWizard;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IInitializer;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchSite;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.texteditor.ITextEditor;

/**
 * The base handler class for the Byteman rule template.
 *
 */
public abstract class AbstractTemplateHandler extends AbstractHandler {

	private Location location;

	private boolean create;

	public AbstractTemplateHandler(Location location, boolean create) {
		this.location = location;
		this.create = create;
	}

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IMember member = null;
		try {
			member = getMember(event);
		} catch (JavaModelException e) {
			BytemanEditorPlugin.logException(e);
		}
		if(member != null) {
			RuleTemplate ruleTemplate = createRuleTemplate(event, member);
			if(ruleTemplate != null) {
				if(create) {
					openWizard(event, ruleTemplate, member.getJavaProject().getProject());
				} else {
					copyToClipboard(ruleTemplate);
				}
			}
		}
		return null;
	}

	protected void openWizard(ExecutionEvent event, RuleTemplate ruleTemplate, IProject project) {
		NewRuleWizard.openWizardDialog(getSite(event), ruleTemplate, project);
	}

	protected void copyToClipboard(RuleTemplate ruleTemplate) {
		UIUtils.copyToClipbord(ruleTemplate.toString());
	}

	/**
	 * Creates a {@link RuleTemplate} by specified {@code ExecutionEvent} and {@code IMember}.
	 *
	 * @param event an {@code ExecutionEvent}
	 * @param member the target element
	 * @return the {@link RuleTemplate}
	 */
	protected RuleTemplate createRuleTemplate(ExecutionEvent event, IMember member) {
		String locationStr = location.getKeyword();
		if(location == Location.LINE) {
			locationStr += " " + getTargetLine(event);
		}
		return new RuleTemplate(member, locationStr);
	}

	/**
	 * Returns the start line of the current selection or -1 if the selection is not {@code ITextSelection} instance.
	 *
	 * @param event the {@code ExecutionEvent}
	 * @return the start line of the current selection or -1 if the selection is not {@code ITextSelection} instance
	 */
	protected int getTargetLine(ExecutionEvent event) {
		ISelection selection = getSelection(event);
		if(selection instanceof ITextSelection) {
			ITextEditor editor = (ITextEditor) getEditor(event);
			ITextSelection textSelection = (ITextSelection) editor.getSelectionProvider().getSelection();
			return textSelection.getStartLine() + 1;
		}
		return -1;
	}

	/**
	 * Returns {@code IMethod} if the current selection is a method. When the selection is a static initializer returns {@code IInitializer}.
	 * If it is neither of them, returns null.
	 * @param event an {@code ExecutionEvent}
	 * @return {@code IMethod} or {@code IInitializer}
	 * @throws JavaModelException
	 */
	protected IMember getMember(ExecutionEvent event) throws JavaModelException {
		ISelection selection = getSelection(event);
		if(selection instanceof ITextSelection) {
			ITextEditor editor = (ITextEditor) getEditor(event);
			ITextSelection textSelection = (ITextSelection) editor.getSelectionProvider().getSelection();
			return JavaUtils.getMemberFromOffset(editor, textSelection.getOffset());
		} else {
			IStructuredSelection structuredSelection = (IStructuredSelection) selection;
			IMember member = (IMember) structuredSelection.getFirstElement();
			if(member instanceof IInitializer
					&& !Flags.isStatic(((IInitializer) member).getFlags())) {
				return null;
			}
			return member;
		}
	}

	protected ISelection getSelection(ExecutionEvent event) {
		return HandlerUtil.getCurrentSelection(event);
	}

	protected IEditorPart getEditor(ExecutionEvent event) {
		return HandlerUtil.getActiveEditor(event);
	}

	protected IWorkbenchSite getSite(ExecutionEvent event) {
		return HandlerUtil.getActiveSite(event);
	}

	/**
	 * Enum for Location specifiers.
	 */
	public static enum Location {
		ENTRY("AT ENTRY"), EXIT("AT EXIT"), LINE("AT LINE");

		private String keyword;

		private Location(String keyword) {
			this.keyword = keyword;
		}

		public String getKeyword() {
			return keyword;
		}
	}

	/**
	 * This class defines a template of Byteman rule.
	 */
	public static class RuleTemplate {

		private IMember member;

		private String location;

		public RuleTemplate(IMember member, String location) {
			this.member = member;
			this.location = location;
		}

		@Override
		public String toString() {
			try {
				IType type = member.getDeclaringType();
				StringBuilder sb = new StringBuilder();
				sb.append("RULE rule_name\n");
				if(type.isInterface()) {
					sb.append("INTERFACE ");
				} else {
					sb.append("CLASS ");
				}
				sb.append(type.getFullyQualifiedName()).append("\n");
				sb.append("METHOD ");
				if(member instanceof IInitializer) {
					sb.append("<clinit>");
				} else if(member instanceof IMethod) {
					sb.append(JavaUtils.createReplacementString((IMethod) member));
				}
				sb.append("\n");
				sb.append(location).append("\n");
				sb.append("IF TRUE\n");
				sb.append("DO\n");
				sb.append("# TODO Auto-generated rule\n");
				sb.append("ENDRULE");
				return sb.toString();
			} catch (JavaModelException e) {
				BytemanEditorPlugin.logException(e);
				return "";
			}
		}
	}
}
