package jp.co.ntt.oss.jboss.byteman.editor.action.agent;

import org.eclipse.jface.resource.ImageDescriptor;

import jp.co.ntt.oss.jboss.byteman.editor.BytemanEditorPlugin;
import jp.co.ntt.oss.jboss.byteman.editor.BytemanEditorPluginImages;
import jp.co.ntt.oss.jboss.byteman.editor.view.BytemanAgentView;
import jp.co.ntt.oss.jboss.byteman.editor.view.model.AgentModel.Agent;

/**
 * Action class to refresh the information of a registered agent.
 *
 */
public class RefreshAgentAction extends AbstractAgentAction {

	public RefreshAgentAction(BytemanAgentView view) {
		super(view, BytemanEditorPlugin.getResourceString("menu.agentView.refresh"));
	}

	@Override
	public void run() {
		Agent agent = getAgent();
		if(agent == null) {
			return;
		}
		agent.update();
		view.refresh();
	}

	@Override
	public ImageDescriptor getImageDescriptor() {
		return BytemanEditorPluginImages.DESC_REFRESH;
	}
}
