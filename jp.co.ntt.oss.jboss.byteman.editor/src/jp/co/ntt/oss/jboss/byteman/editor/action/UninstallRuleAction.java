package jp.co.ntt.oss.jboss.byteman.editor.action;

import java.util.ArrayList;
import java.util.List;

import jp.co.ntt.oss.jboss.byteman.editor.BytemanEditorPlugin;
import jp.co.ntt.oss.jboss.byteman.editor.view.BytemanAgentView;
import jp.co.ntt.oss.jboss.byteman.editor.view.model.AgentModel.Agent;
import jp.co.ntt.oss.jboss.byteman.editor.view.model.AgentModel.Rule;

import org.eclipse.jface.action.Action;
import org.jboss.byteman.agent.submit.ScriptText;

/**
 * Action class for uninstalling a rule from a Byteman agent.
 *
 */
public class UninstallRuleAction extends Action {

	private BytemanAgentView view;

	public UninstallRuleAction(BytemanAgentView view) {
		super(BytemanEditorPlugin.getResourceString("menu.agentView.uninstall"));
		this.view = view;
	}

	@Override
	public void run() {
		Object element = view.getSelection().getFirstElement();
		if(element instanceof Rule) {
			Rule rule = (Rule) element;
			Agent agent = rule.getAgent();

			List<ScriptText> scriptTexts = new ArrayList<ScriptText>();
			scriptTexts.add(new ScriptText(rule.getFile(), rule.getRuleText()));

			execute(agent, rule.getRuleName(), scriptTexts);
		}
	}

	protected void execute(Agent agent, String ruleName, List<ScriptText> scriptTexts) {
		BytemanAgentView.uninstallRule(view, agent, ruleName, scriptTexts);
	}

}
