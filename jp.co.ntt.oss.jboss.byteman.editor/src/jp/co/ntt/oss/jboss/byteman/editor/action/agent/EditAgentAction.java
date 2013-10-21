package jp.co.ntt.oss.jboss.byteman.editor.action.agent;

import jp.co.ntt.oss.jboss.byteman.editor.BytemanEditorPlugin;
import jp.co.ntt.oss.jboss.byteman.editor.view.BytemanAgentView;
import jp.co.ntt.oss.jboss.byteman.editor.view.model.AgentModel.Agent;

/**
 * Action class to edit an agent settings.
 *
 */
public class EditAgentAction extends AbstractAgentAction {

	public EditAgentAction(BytemanAgentView view) {
		super(view, BytemanEditorPlugin.getResourceString("menu.agentView.edit"));
	}

	@Override
	public void run() {
		Agent agent = getAgent();
		if(agent == null) {
			return;
		}
		openAgentWizard(agent);
	}
}
