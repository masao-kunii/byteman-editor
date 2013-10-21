package jp.co.ntt.oss.jboss.byteman.editor.wizard;

import jp.co.ntt.oss.jboss.byteman.editor.BytemanEditorPlugin;
import jp.co.ntt.oss.jboss.byteman.editor.view.BytemanAgentView;
import jp.co.ntt.oss.jboss.byteman.editor.view.model.AgentModel.Agent;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;

/**
 * Wizard for registering an agent to {@link BytemanAgentView}.
 *
 */
public class AgentWizard extends Wizard implements INewWizard {

	private BytemanAgentView view;

	private Agent agent;

	private AgentWizardPage page;

	public AgentWizard(BytemanAgentView view) {
		this(view, null);
	}

	public AgentWizard(BytemanAgentView view, Agent agent) {
		super();
		this.view = view;
		this.agent = agent;
	}

	@Override
	public void init(IWorkbench workbench, IStructuredSelection selection) {
		if(agent == null) {
			setWindowTitle(BytemanEditorPlugin.getResourceString("wizard.agent.new.title"));
		} else {
			setWindowTitle(BytemanEditorPlugin.getResourceString("wizard.agent.edit.title"));
		}
	}

	@Override
	public void addPages() {
		page = new AgentWizardPage(agent);
		addPage(page);
	}

	@Override
	public boolean performFinish() {
		if(agent == null) {
			view.addAgent(page.getAddress(), page.getPort());
		} else {
			view.editAgent(agent, page.getAddress(), page.getPort());
		}
		view.refresh();
		return true;
	}

	/**
	 * Opens {@link AgentWizard} for the specified {@link Agent}.
	 * @param view an agent view to open
	 * @param agent an agent to open
	 */
	public static void openWizardDialog(BytemanAgentView view, Agent agent){
		AgentWizard wizard = new AgentWizard(view, agent);
		wizard.init(view.getSite().getWorkbenchWindow().getWorkbench(), view.getSelection());
		WizardDialog dialog = new WizardDialog(view.getSite().getShell(), wizard);
		dialog.setHelpAvailable(false);
		dialog.create();
		dialog.open();
	}

}
