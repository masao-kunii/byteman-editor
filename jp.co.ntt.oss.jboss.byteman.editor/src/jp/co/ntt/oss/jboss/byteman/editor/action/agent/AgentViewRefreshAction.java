package jp.co.ntt.oss.jboss.byteman.editor.action.agent;

import jp.co.ntt.oss.jboss.byteman.editor.BytemanEditorPlugin;
import jp.co.ntt.oss.jboss.byteman.editor.BytemanEditorPluginImages;
import jp.co.ntt.oss.jboss.byteman.editor.view.BytemanAgentView;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;

/**
 * Action class to refresh byteman agent view.
 *
 */
public class AgentViewRefreshAction extends Action {

	private BytemanAgentView view;

	public AgentViewRefreshAction(BytemanAgentView view) {
		setToolTipText(BytemanEditorPlugin.getResourceString("tooltip.agentView.refresh"));
		this.view = view;
	}

	@Override
	public void run() {
		view.updateAllAgents();
	}

	@Override
	public ImageDescriptor getImageDescriptor() {
		return BytemanEditorPluginImages.DESC_REFRESH;
	}
}
