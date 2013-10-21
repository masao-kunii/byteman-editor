package jp.co.ntt.oss.jboss.byteman.editor.view.model;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jp.co.ntt.oss.jboss.byteman.editor.BytemanEditorPluginImages;
import jp.co.ntt.oss.jboss.byteman.editor.util.RuleScripts;
import jp.co.ntt.oss.jboss.byteman.editor.util.RuleScripts.LineInfo;
import jp.co.ntt.oss.jboss.byteman.editor.util.RuleScripts.RuleScript;
import jp.co.ntt.oss.jboss.byteman.editor.view.BytemanAgentView;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.jboss.byteman.agent.submit.Submit;

/**
 * Model object for {@link BytemanAgentView}.
 *
 */
public class AgentModel {

	private List<Agent> agents;

	public AgentModel() {
		agents = new ArrayList<Agent>();
	}

	public Object[] getChildren() {
		return agents.toArray(new Agent[agents.size()]);
	}

	public void restoreAgents() {
		for(String agentInfo : AgentsStore.getAgents()) {
			String[] agent = agentInfo.split(AgentsStore.SEPARATOR);
			addAgent(agent[0], Integer.parseInt(agent[1]), false);
		}
	}

	public Agent getAgent(String address, int port) {
		for(Agent agent : agents) {
			if(agent.getAddress().equals(address) && agent.getPort() == port) {
				return agent;
			}
		}
		return null;
	}

	public void addAgent(String address, int port) {
		addAgent(address, port, true);
	}

	public void addAgent(String address, int port, boolean store) {
		addAgent(new Agent(address, port), store);
	}

	protected void addAgent(Agent agent, boolean store) {
		if(!agents.contains(agent)) {
			boolean ret = agents.add(agent.update());
			if(ret && store) {
				storeAgents();
			}
		}
	}

	public void editAgent(Agent agent, String address, int port) {
		agent.setAddress(address);
		agent.setPort(port);
		agent.update();
		storeAgents();
	}


	public void removeAgent(Agent agent) {
		agents.remove(agent);
		storeAgents();
	}

	public void updateAllAgents() {
		for(Agent agent : agents) {
			agent.update();
		}
	}

	protected void storeAgents() {
		AgentsStore.setAgents(agents);
	}

	/**
	 * The model object of a Byteman agent.
	 *
	 */
	public static class Agent extends AgentViewModel {

		private String address;

		private int port;

		private boolean connected = false;

		private List<Rule> rules;

		public Agent(String address, int port) {
			this.address = address;
			this.port = port;
			rules = new ArrayList<Rule>();
		}

		public Agent update() {
			connect();
			rules = new ArrayList<AgentModel.Rule>();
			if(connected) {
				Submit submit = new Submit(address, port);
				try {
					String allRules = submit.listAllRules();
					for(RuleScript script : RuleScripts.get(allRules).getRules()) {
						addRule(script);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			return this;
		}

		@Override
		public String getText() {
			return address + ":" + port + " [" + (connected ? "Connected" : "Disconnected") + "]";
		}

		@Override
		public Color getForeground() {
			if(!connected) {
				return Display.getDefault().getSystemColor(SWT.COLOR_RED);
			} else {
				return null;
			}
		}

		@Override
		public Image getImage() {
			if(connected) {
				return BytemanEditorPluginImages.IMG_SERVER_CONNECTED;
			} else {
				return BytemanEditorPluginImages.IMG_SERVER_DISCONNECTED;
			}
		}

		@Override
		public Object[] getChildren() {
			return rules.toArray(new Rule[rules.size()]);
		}

		@Override
		public boolean equals(Object obj) {
			if(obj instanceof Agent) {
				Agent agent = (Agent) obj;
				return address.equals(agent.getAddress()) && port == agent.getPort();
			} else {
				return false;
			}
		}

		public void addRule(RuleScript script) {
			Rule rule = new Rule(this, script);
			rule.parse(script.getOtherLines());
			rules.add(rule);
		}

		public void addErrorRule(String ruleName, String errorMessage) {
			rules.add(new Rule(this, ruleName, errorMessage));
		}

		public String getAddress() {
			return address;
		}

		public void setAddress(String address) {
			this.address = address;
		}

		public int getPort() {
			return port;
		}

		public void setPort(int port) {
			this.port = port;
		}

		protected void connect() {
			Socket socket = new Socket();
			try {
				socket.connect(new InetSocketAddress(address, port));
				connected = true;
			} catch (IOException e) {
				connected = false;
			} finally {
				try {
					socket.close();
				} catch (IOException e) {
				}
			}
		}
	}

	/**
	 * The model object of a Byteman rule.
	 */
	public static class Rule extends AgentViewModel {

		private static final Pattern PATTERN_TRIGGER_METHOD = Pattern.compile("(.*)\\(.*\\) .*");
		private static final String PREFIX_METHOD = "trigger method: ";
		private Agent agent;
		private String file;
		private String ruleName;
		private String ruleText;
		private Map<String, Class> classMap = new LinkedHashMap<String, AgentModel.Class>();
		private String errorMessage;

		public Rule(Agent agent, RuleScript script) {
			this.agent = agent;
			file = script.getFile();
			ruleName = script.getRule().getValue();
			ruleText = script.getScriptText();
		}

		public Rule(Agent agent, String ruleName, String errorMessage) {
			this.agent = agent;
			this.ruleName = ruleName;
			this.errorMessage = errorMessage;
		}

		@Override
		public String getText() {
			String text = ruleName;
			if(errorMessage != null) {
				text = text + " [cause: " + errorMessage + "]";
			}
			return text;
		}

		@Override
		public Color getForeground() {
			if(errorMessage != null) {
				return Display.getDefault().getSystemColor(SWT.COLOR_RED);
			}
			return null;
		}

		@Override
		public Image getImage() {
			return BytemanEditorPluginImages.IMG_RULE;
		}

		@Override
		public Object[] getChildren() {
			return classMap.values().toArray(new Class[classMap.size()]);
		}

		@Override
		public boolean equals(Object obj) {
			if(obj instanceof Rule) {
				Rule rule = (Rule) obj;
				return agent.equals(rule.getAgent()) && ruleName.equals(rule.getRuleName());
			} else{
				return false;
			}
		}

		protected void parse(List<LineInfo> lines) {
			for(LineInfo line : lines) {
				if(line.getLineText().startsWith(PREFIX_METHOD)) {
					String method = line.getLineText().substring(PREFIX_METHOD.length());
					addMethod(method);
				}
			}
		}

		protected void addMethod(String triggerMethod) {
			Matcher matcher = PATTERN_TRIGGER_METHOD.matcher(triggerMethod);
			if(!matcher.find()) {
				return;
			}
			String qualifiedMethodName = matcher.group(1);
			String className = qualifiedMethodName.substring(0, qualifiedMethodName.lastIndexOf("."));
			Class clazz = null;
			if(classMap.containsKey(className)) {
				clazz = classMap.get(className);
			} else {
				clazz = new Class(this, className);
				classMap.put(className, clazz);
			}
			String methodName = triggerMethod.substring(className.length() + 1);
			clazz.addMethod(methodName);
		}

		public Agent getAgent() {
			return agent;
		}

		public String getFile() {
			return file;
		}

		public String getRuleName() {
			return ruleName;
		}

		public String getRuleText() {
			return ruleText;
		}

	}

	/**
	 * The model object of a class which a rule is applied.
	 */
	public static class Class extends AgentViewModel {

		private Rule rule;

		private String className;

		private List<Method> methods;

		public Class(Rule rule, String className) {
			this.rule = rule;
			this.className = className;
			methods = new ArrayList<Method>();
		}

		@Override
		public String getText() {
			return className;
		}

		@Override
		public Image getImage() {
			return BytemanEditorPluginImages.IMG_CLASS;
		}

		@Override
		public Object[] getChildren() {
			return methods.toArray(new Method[methods.size()]);
		}

		@Override
		public boolean equals(Object obj) {
			if(obj instanceof Class) {
				Class clazz = (Class) obj;
				return rule.equals(clazz.getRule()) && className.equals(clazz.getClassName());
			} else {
				return false;
			}
		}

		public void addMethod(String methodName) {
			Method method = new Method(this, methodName);
			// Submit#listAllSciripst returns duplicate method.
			if(!methods.contains(method)) {
				methods.add(method);
			}
		}

		public Rule getRule() {
			return rule;
		}

		public String getClassName() {
			return className;
		}
	}

	/**
	 * The model object of a method which a rule is applied.
	 *
	 */
	public static class Method extends AgentViewModel {

		private String className;
		private String methodName;

		public Method(Class clazz, String methodName) {
			className = clazz.getClassName();
			this.methodName = methodName;
		}

		@Override
		public String getText() {
			return methodName;
		}

		@Override
		public Image getImage() {
			return BytemanEditorPluginImages.IMG_METHOD;
		}

		@Override
		public Object[] getChildren() {
			return null;
		}

		@Override
		public boolean equals(Object obj) {
			if(obj instanceof Method) {
				Method method = (Method) obj;
				return methodName.equals(method.getMethodName());
			} else {
				return false;
			}
		}

		public String getClassName() {
			return className;
		}

		public String getMethodName() {
			return methodName;
		}
	}
}
