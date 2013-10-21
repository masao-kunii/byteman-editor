package jp.co.ntt.oss.jboss.byteman.editor.action;

import java.util.ArrayList;
import java.util.List;

import jp.co.ntt.oss.jboss.byteman.editor.view.BytemanAgentView;
import jp.co.ntt.oss.jboss.byteman.editor.view.model.AgentsStore;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.ui.actions.CompoundContributionItem;

/**
 * Implementation of {@code ContributionItem} to create menus for registered Byteman agents.
 * Byteman agents can be registered from {@link BytemanAgentView}.
 *
 */
public class InstallRuleActionItems extends CompoundContributionItem {

	@Override
	protected IContributionItem[] getContributionItems() {
		List<IContributionItem> list = new ArrayList<IContributionItem>();
		for(String agentInfo : getAgents()) {
			String[] agent = agentInfo.split(AgentsStore.SEPARATOR);
			InstallRuleAction action = new InstallRuleAction(agent[0], Integer.parseInt(agent[1]));
			list.add(new ActionContributionItem(action));
		}
		if(list.size() == 0) {
			list.add(new ActionContributionItem(new NoAgentAction()));
		}
		return list.toArray(new IContributionItem[list.size()]);
	}

	protected List<String> getAgents() {
		return AgentsStore.getAgents();
	}

	static class NoAgentAction extends Action {
		public NoAgentAction() {
			super("(none)");
			setEnabled(false);
		}
	}
}
