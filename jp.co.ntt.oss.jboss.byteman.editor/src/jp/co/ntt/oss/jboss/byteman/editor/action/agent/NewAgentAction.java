package jp.co.ntt.oss.jboss.byteman.editor.action.agent;

import jp.co.ntt.oss.jboss.byteman.editor.BytemanEditorPlugin;
import jp.co.ntt.oss.jboss.byteman.editor.view.BytemanAgentView;

/**
 * Action class to register a Byteman agent.
 *
 */
public class NewAgentAction extends AbstractAgentAction {

	public NewAgentAction(BytemanAgentView view) {
		super(view, BytemanEditorPlugin.getResourceString("menu.agentView.agent"));
	}

	@Override
	public void run() {
		openAgentWizard(null);
	}

}
