package jp.co.ntt.oss.jboss.byteman.editor.action;

import jp.co.ntt.oss.jboss.byteman.editor.BytemanEditorPlugin;
import jp.co.ntt.oss.jboss.byteman.editor.util.RuleScripts;
import jp.co.ntt.oss.jboss.byteman.editor.util.RuleScripts.RuleScript;
import jp.co.ntt.oss.jboss.byteman.editor.view.BytemanAgentView;
import jp.co.ntt.oss.jboss.byteman.editor.view.model.RuleOutlineModel.Rule;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;

/**
 * Action class for installing a rule to a Byteman agent.
 *
 */
public class InstallRuleAction extends Action {

	private String address;

	private int port;

	public InstallRuleAction(String address, int port) {
		super(address + ":" + port);
		this.address = address;
		this.port = port;
	}

	@Override
	public void run() {
		ISelection selection = getSelection();
		if(selection instanceof IStructuredSelection) {
			Object element = ((IStructuredSelection) selection).getFirstElement();
			if(element instanceof IFile) {
				execute((IFile) element);
			} else if(element instanceof Rule) {
				execute(((Rule) element).getRuleScript());
			}
		} else if(selection instanceof ITextSelection) {
			RuleScript rule = getCurrentRuleScript();
			if(rule == null) {
				return;
			}
			execute(rule);
		}
	}

	protected ISelection getSelection() {
		return BytemanEditorPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow().getActivePage().getSelection();
	}

	protected RuleScript getCurrentRuleScript() {
		return RuleScripts.get().getCurrentRuleScript();
	}

	protected void execute(IFile file) {
		BytemanAgentView.installRule(file, address, port);
	}

	protected void execute(RuleScript script) {
		BytemanAgentView.installRule(script, address, port);
	}

}
