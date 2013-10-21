package jp.co.ntt.oss.jboss.byteman.editor.action.agent;

import jp.co.ntt.oss.jboss.byteman.editor.view.BytemanAgentView;
import jp.co.ntt.oss.jboss.byteman.editor.view.model.AgentModel.Agent;
import jp.co.ntt.oss.jboss.byteman.editor.wizard.AgentWizard;

import org.eclipse.jface.action.Action;

/**
 * The base action class for the Byteman agent view.
 *
 */
public abstract class AbstractAgentAction extends Action {

	protected BytemanAgentView view;

	public AbstractAgentAction(BytemanAgentView view, String text) {
		super(text);
		this.view = view;
	}

	/**
	 * Returns {@link Agent} object of the current selection. Returns null if the selection is not {@link Agent}.
	 * @return {@link Agent} if the current selection is a {@link Agent}. Otherwise null.
	 */
	protected Agent getAgent() {
		Object element = view.getSelection().getFirstElement();
		if(element instanceof Agent) {
			return (Agent) element;
		} else {
			return null;
		}
	}

	/**
	 * Opens the agent wizard with specified {@link Agent}.
	 * @param agent an agent to register
	 * @see AgentWizard
	 */
	protected void openAgentWizard(Agent agent) {
		AgentWizard.openWizardDialog(view, agent);
	}
}
