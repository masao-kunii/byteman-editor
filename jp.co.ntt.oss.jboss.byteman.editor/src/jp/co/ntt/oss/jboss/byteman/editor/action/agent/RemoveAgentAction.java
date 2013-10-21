package jp.co.ntt.oss.jboss.byteman.editor.action.agent;

import jp.co.ntt.oss.jboss.byteman.editor.BytemanEditorPlugin;
import jp.co.ntt.oss.jboss.byteman.editor.view.BytemanAgentView;
import jp.co.ntt.oss.jboss.byteman.editor.view.model.AgentModel.Agent;

/**
 * Action class to remove a registered agent from the Byteman agent view.
 *
 */
public class RemoveAgentAction extends AbstractAgentAction {

	public RemoveAgentAction(BytemanAgentView view) {
		super(view, BytemanEditorPlugin.getResourceString("menu.agentView.remove"));
	}

	@Override
	public void run() {
		Agent agent = getAgent();
		if(agent == null || !view.confirmRemoveAgent(agent)) {
			return;
		}
		view.removeAgent(agent);
		view.refresh();
	}

}
