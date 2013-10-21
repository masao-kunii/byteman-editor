package jp.co.ntt.oss.jboss.byteman.editor.view.model;

import java.util.ArrayList;
import java.util.List;

import jp.co.ntt.oss.jboss.byteman.editor.BytemanEditorPlugin;
import jp.co.ntt.oss.jboss.byteman.editor.util.StringUtils;
import jp.co.ntt.oss.jboss.byteman.editor.view.model.AgentModel.Agent;

/**
 * This class stores agents information into the preference store and restores from the store.
 *
 */
public class AgentsStore {

	public static final String SEPARATOR = ":";

	private static String get() {
		return BytemanEditorPlugin.getDefault().getPreferenceStore().getString(BytemanEditorPlugin.PREF_AGENTS);
	}

	/**
	 * Returns a list of agent information from the preference store.
	 * @return list of agent information
	 */
	public static List<String> getAgents() {
		return getAgents(get());
	}

	/**
	 * Sets the agent information into the preference store.
	 * @param agents a list of agent information
	 */
	public static void setAgents(List<Agent> agents) {
		BytemanEditorPlugin.getDefault().getPreferenceStore().setValue(
					BytemanEditorPlugin.PREF_AGENTS, formatAgents(agents));
	}

	/**
	 * Tests whether an agent with the specified address and port exists.
	 * @param address an address of agent
	 * @param port a port number of agent
	 * @return {@code true} if the agent with the specified address and port exists, otherwise false
	 */
	public static boolean exists(String address, int port) {
		return exists(address, port, getAgents());
	}

	protected static List<String> getAgents(String agents) {
		List<String> result = new ArrayList<String>();
		if(StringUtils.isNotEmpty(agents)) {
			for(String agent : agents.split(",")) {
				result.add(agent);
			}
		}
		return result;
	}

	protected static String formatAgents(List<Agent> agents) {
		StringBuilder sb = new StringBuilder();
		for(Agent agent : agents) {
			if(sb.length() > 0) {
				sb.append(",");
			}
			sb.append(agent.getAddress()).append(":").append(agent.getPort());
		}
		return sb.toString();
	}

	protected static boolean exists(String address, int port, List<String> agents) {
		for(String agent : agents) {
			if(agent.equals(address + SEPARATOR + port)) {
				return true;
			}
		}
		return false;
	}
}
