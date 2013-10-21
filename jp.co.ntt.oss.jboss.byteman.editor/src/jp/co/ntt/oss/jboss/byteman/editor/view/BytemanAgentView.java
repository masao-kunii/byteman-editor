package jp.co.ntt.oss.jboss.byteman.editor.view;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import jp.co.ntt.oss.jboss.byteman.editor.BytemanEditorPlugin;
import jp.co.ntt.oss.jboss.byteman.editor.action.UninstallRuleAction;
import jp.co.ntt.oss.jboss.byteman.editor.action.agent.AgentViewRefreshAction;
import jp.co.ntt.oss.jboss.byteman.editor.action.agent.EditAgentAction;
import jp.co.ntt.oss.jboss.byteman.editor.action.agent.NewAgentAction;
import jp.co.ntt.oss.jboss.byteman.editor.action.agent.RefreshAgentAction;
import jp.co.ntt.oss.jboss.byteman.editor.action.agent.RemoveAgentAction;
import jp.co.ntt.oss.jboss.byteman.editor.util.EditorUtils;
import jp.co.ntt.oss.jboss.byteman.editor.util.JavaUtils;
import jp.co.ntt.oss.jboss.byteman.editor.util.RuleScripts;
import jp.co.ntt.oss.jboss.byteman.editor.util.RuleScripts.RuleScript;
import jp.co.ntt.oss.jboss.byteman.editor.util.UIUtils;
import jp.co.ntt.oss.jboss.byteman.editor.view.model.AgentModel;
import jp.co.ntt.oss.jboss.byteman.editor.view.model.AgentModel.Agent;
import jp.co.ntt.oss.jboss.byteman.editor.view.model.AgentModel.Class;
import jp.co.ntt.oss.jboss.byteman.editor.view.model.AgentModel.Method;
import jp.co.ntt.oss.jboss.byteman.editor.view.model.AgentModel.Rule;
import jp.co.ntt.oss.jboss.byteman.editor.view.model.AgentViewModel;
import jp.co.ntt.oss.jboss.byteman.editor.view.model.BytemanContentProvider;
import jp.co.ntt.oss.jboss.byteman.editor.view.model.BytemanLabelProvider;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.ViewerDropAdapter;
import org.eclipse.jface.window.ToolTip;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.dnd.TransferData;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.ViewPart;
import org.jboss.byteman.agent.submit.ScriptText;
import org.jboss.byteman.agent.submit.Submit;

/**
 * This view displays a list of registered agents, installed rules to the agents,
 * and methods which the rules applied. When a method displayed is double-clicked,
 * jumps to the source code if it found. Rules can be installed by drag-and-drop
 * into this view and can be uninstalled.
 *
 *
 *
 */
public class BytemanAgentView extends ViewPart {

	public static final String ID = "jp.co.ntt.oss.jboss.byteman.editor.view.agent";

	protected TreeViewer viewer;

	private IAction refreshAction;

	protected AgentModel agents;

	private IDoubleClickListener doubleClickListener = new BytemanAgentViewDoubleClickListener();

	private ISelectionChangedListener selectionChangedListener = new ISelectionChangedListener() {
		private ISelection previous;
		@Override
		public void selectionChanged(SelectionChangedEvent event) {
			if(!event.getSelection().equals(previous)) {
				BytemanAgentView.this.getViewSite().getActionBars().getStatusLineManager().setErrorMessage("");
			}
			previous = event.getSelection();
		}
	};

	@Override
	public void createPartControl(Composite parent) {
		viewer = new TreeViewer(parent);
		viewer.setContentProvider(new BytemanAgentContentProvider());
		viewer.setLabelProvider(new BytemanAgentLabelProvider());
		viewer.addDoubleClickListener(doubleClickListener);
		viewer.addSelectionChangedListener(selectionChangedListener);
		viewer.addDropSupport(
				DND.DROP_MOVE,
				new Transfer[]{TextTransfer.getInstance(), FileTransfer.getInstance()},
				new BytemanDropTargetListener());
		BytemanAgentViewToolTip.enableFor(viewer);
		agents = new AgentModel();
		restoreAgents();
		viewer.setInput(agents);
		registerContextMenu(viewer);
	}

	@Override
	public void dispose() {
		viewer.removeDoubleClickListener(doubleClickListener);
		viewer.removeSelectionChangedListener(selectionChangedListener);
		super.dispose();
	}

	@Override
	public void setFocus() {
		viewer.getTree().setFocus();
	}

	@Override
	public void init(IViewSite site) throws PartInitException {
		super.init(site);
		refreshAction = new AgentViewRefreshAction(this);
		IActionBars bars = site.getActionBars();
		bars.getToolBarManager().add(refreshAction);
	}

	public IStructuredSelection getSelection() {
		return (IStructuredSelection) viewer.getSelection();
	}

	private void registerContextMenu(final TreeViewer viewer) {
		final MenuManager newMenu = new MenuManager(BytemanEditorPlugin.getResourceString("category.new"));
		final IAction newAction = new NewAgentAction(this);
		final IAction editAction = new EditAgentAction(this);
		final IAction deleteAction = new RemoveAgentAction(this);
		final IAction refreshAction = new RefreshAgentAction(this);
		final IAction uninstallAction = new UninstallRuleAction(this);
		MenuManager menuManager = new MenuManager(BytemanEditorPlugin.getResourceString("category.byteman"));
		menuManager.setRemoveAllWhenShown(true);
		menuManager.addMenuListener(new IMenuListener() {
			@Override
			public void menuAboutToShow(IMenuManager manager) {
				TreeSelection selection = (TreeSelection) viewer.getSelection();
				newMenu.add(newAction);
				manager.add(newMenu);
				if(selection.getFirstElement() instanceof Agent) {
					manager.add(editAction);
					manager.add(deleteAction);
					manager.add(refreshAction);
				} else if(selection.getFirstElement() instanceof Rule) {
					manager.add(uninstallAction);
				}
			}
		});
		Menu menu = menuManager.createContextMenu(viewer.getControl());
		viewer.getControl().setMenu(menu);
		getSite().registerContextMenu(menuManager, viewer);
	}

	public void updateAllAgents() {
		agents.updateAllAgents();
		refresh();
	}

	public void refresh() {
		List<Object> expandedElements = Arrays.asList(viewer.getExpandedElements());
		viewer.refresh();
		restoreExpanded(agents.getChildren(), expandedElements);
	}

	protected void restoreExpanded(Object[] models, List<Object> expandedElements) {
		if(models == null) return;
		for(Object o : models) {
			if(expandedElements.contains(o)) {
				viewer.expandToLevel(o, 1);
				restoreExpanded(((AgentViewModel) o).getChildren(), expandedElements);
			}
		}
	}

	public void restoreAgents() {
		agents.restoreAgents();
	}

	public void addAgent(String address, int port) {
		agents.addAgent(address, port);
	}

	public void editAgent(Agent agent, String address, int port) {
		agents.editAgent(agent, address, port);
	}

	public boolean confirmRemoveAgent(Agent agent) {
		return MessageDialog.openConfirm(
				Display.getDefault().getActiveShell(),
				BytemanEditorPlugin.getResourceString("dialog.removeAgent.title"),
				BytemanEditorPlugin.getResourceString("dialog.removeAgent.message", agent.getAddress() + ":" + agent.getPort()));
	}

	public void removeAgent(Agent agent) {
		agents.removeAgent(agent);
	}

	public void addErrorRule(String address, int port, String ruleName, String errorMessage) {
		for(Object o : agents.getChildren()) {
			Agent agent = (Agent) o;
			if(agent.getAddress().equals(address) && agent.getPort() == port) {
				agent.addErrorRule(ruleName, errorMessage);
			}
		}
	}
	/**
	 * Content provider of Byteman agent.
	 */
	protected static class BytemanAgentContentProvider extends BytemanContentProvider {

		@Override
		public Object[] getChildren(Object parentElement) {
			if(parentElement instanceof AgentModel) {
				return ((AgentModel) parentElement).getChildren();
			} else {
				return super.getChildren(parentElement);
			}
		}

	}

	/**
	 * Label provider of Byteman agent.
	 */
	protected static class BytemanAgentLabelProvider extends BytemanLabelProvider {

		@Override
		public Color getForeground(Object element) {
			if(element instanceof AgentViewModel) {
				return ((AgentViewModel) element).getForeground();
			}
			return super.getForeground(element);
		}

		@Override
		public Font getFont(Object element) {
			if(element instanceof AgentViewModel) {
				return ((AgentViewModel) element).getFont();
			}
			return super.getFont(element);
		}

		@Override
		public String getToolTipText(Object element) {
			if(element instanceof Rule) {
				return ((Rule) element).getRuleText();
			}
			return super.getToolTipText(element);
		}

	}

	/**
	 * Tool tip in BytemanAgentView.
	 */
	private static class BytemanAgentViewToolTip extends ColumnViewerToolTipSupport {

		protected BytemanAgentViewToolTip(ColumnViewer viewer, int style,
				boolean manualActivation) {
			super(viewer, style, manualActivation);
			setHideOnMouseDown(false);
		}

		public static void enableFor(ColumnViewer viewer) {
			new BytemanAgentViewToolTip(viewer, ToolTip.NO_RECREATE, false);
		}

		@Override
		protected Composite createToolTipContentArea(Event event, Composite parent) {
			Composite composite = new Composite(parent, SWT.NONE);
			GridLayout l = new GridLayout(1, false);
			l.horizontalSpacing = 0;
			l.marginWidth = 0;
			l.marginHeight = 0;
			l.verticalSpacing = 0;
			composite.setLayout(l);

			Text text = new Text(composite, SWT.READ_ONLY | SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL);
			String tooltipText = getText(event);
			if(tooltipText != null) {
				text.setText(tooltipText);
			}
			Color bgColor = getBackgroundColor(event);
			if(bgColor != null) {
				text.setBackground(bgColor);
			}
			text.clearSelection();
			text.setLayoutData(new GridData(300, 150));

			return parent;
		}
	}

	/**
	 * {@code IDoubleClickListener} implementation for BytemanAgentView.
	 */
	protected class BytemanAgentViewDoubleClickListener implements IDoubleClickListener {

		@Override
		public void doubleClick(DoubleClickEvent event) {
			String targetClass = null;
			String targetMethod = null;
			Object element = ((IStructuredSelection) event.getSelection()).getFirstElement();
			if(element instanceof Class) {
				targetClass = ((Class) element).getClassName();
			} else if(element instanceof Method) {
				Method method = (Method) element;
				targetClass = method.getClassName();
				targetMethod = method.getMethodName();
			} else {
				return;
			}
			IJavaElement targetElement = findJumpTarget(targetClass, targetMethod);
			if(targetElement != null) {
				openInEditor(targetElement);
			} else {
				BytemanAgentView.this.getViewSite().getActionBars().getStatusLineManager()
					.setErrorMessage(BytemanEditorPlugin.getResourceString("error.jump"));
			}
		}

		protected void openInEditor(IJavaElement element) {
			UIUtils.openInEditor(element);
		}
	}

	/**
	 * Returns the target to jump specified by the class name and method name.
	 * @param targetClass the target class name
	 * @param targetMethod the target method name
	 * @return the target to jump
	 */
	protected IJavaElement findJumpTarget(String targetClass, String targetMethod) {
		for(IJavaProject project : getJavaProjects()) {
			if(!project.exists()) {
				continue;
			}
			try {
				IType type = project.findType(targetClass);
				if(type == null) {
					continue;
				}
				if(targetMethod == null || targetMethod.equals("<clinit>() void")) {
					return type;
				}

				int index = targetMethod.lastIndexOf(' ');
				if(index >= 0){
					targetMethod = targetMethod.substring(0, index);
				}
				return JavaUtils.findMethod(type, targetMethod);
			} catch (Exception e) {
				BytemanEditorPlugin.logException(e);
			}
		}
		return null;
	}

	protected List<IJavaProject> getJavaProjects() {
		List<IJavaProject> projects = new ArrayList<IJavaProject>();
		for(IProject project : ResourcesPlugin.getWorkspace().getRoot().getProjects()) {
			projects.add(JavaCore.create(project));
		}
		return projects;
	}

	/**
	 * Drop listener for rule install.
	 */
	private class BytemanDropTargetListener extends ViewerDropAdapter {

		private Agent agent;

		protected BytemanDropTargetListener() {
			super(BytemanAgentView.this.viewer);
		}

		@Override
		public boolean performDrop(Object data) {
			InstallRuleJob job = new InstallRuleJob(BytemanAgentView.this, agent);
			if(data instanceof String[]) {
				for(String file : (String[]) data) {
					job.addFile(file);
				}
			} else {
				job.setFile(EditorUtils.getActiveFile());
				for(RuleScript rule : RuleScripts.get(data.toString()).getRules()) {
					job.addRule(rule);
				}
			}
			job.schedule();
			return false;
		}

		@Override
		public boolean validateDrop(Object target, int operation, TransferData transferType) {
			if(target instanceof Agent) {
				agent = (Agent) target;
				return true;
			} else {
				return false;
			}
		}
	}

	public static void installRule(IFile file, String address, int port){
		InstallRuleJob job = createInstallJob(address, port);
		if(job == null) return;
		job.addFile(file.getLocation().toString());
		job.schedule();
	}

	public static void installRule(RuleScript script, String address, int port) {
		InstallRuleJob job = createInstallJob(address, port);
		if(job == null) return;
		job.addRule(script);
		job.setFile(EditorUtils.getActiveFile());
		job.schedule();
	}

	public static void uninstallRule(BytemanAgentView view, Agent agent, String ruleName, List<ScriptText> scriptTexts) {
		if(MessageDialog.openConfirm(
				Display.getDefault().getActiveShell(),
				BytemanEditorPlugin.getResourceString("dialog.uninstallRule.title"),
				BytemanEditorPlugin.getResourceString("dialog.uninstallRule.message", ruleName))) {
			new UninstallRuleJob(view, agent, scriptTexts).schedule();
		}
	}

	private static InstallRuleJob createInstallJob(String address, int port) {
		BytemanAgentView view = openAgentView();
		if(view == null) return null;
		Agent agent = view.agents.getAgent(address, port);
		if(agent == null) return null;
		return new InstallRuleJob(view, agent);
	}

	private static BytemanAgentView openAgentView() {
		try {
			return (BytemanAgentView) BytemanEditorPlugin.getDefault().getWorkbench()
						.getActiveWorkbenchWindow().getActivePage().showView(BytemanAgentView.ID);
		} catch (PartInitException e) {
			BytemanEditorPlugin.logException(e);
			return null;
		}
	}

	/**
	 * The thread class for submitting to Byteman agent and refreshing the {@link BytemanAgentView}.
	 */
	private static class InstallRuleJob extends Job {

		private BytemanAgentView view;
		private Agent agent;
		private List<String> files;
		private List<RuleScript> rules;
		private IFile file;

		public InstallRuleJob(BytemanAgentView view, Agent agent) {
			super("InstallRuleJob");
			this.view = view;
			this.agent = agent;
		}

		@Override
		protected IStatus run(IProgressMonitor monitor) {
			final Map<String, String> errorRules = new LinkedHashMap<String, String>();
			Submit submit = new Submit(agent.getAddress(), agent.getPort());
			if(files != null) {
				for(String file : files) {
					try {
						submit.addRulesFromFiles(Arrays.asList(file));
					} catch (Exception e) {
						for(String name : EditorUtils.findRuleNames(file)) {
							errorRules.put(name, e.getMessage());
						}
					}
				}
			} else if(rules != null) {
				for(RuleScript rule : rules) {
					try {
						String name = rule.getFile();
						if(name == null && file != null) {
							name = file.getName();
						}
						submit.addScripts(Arrays.asList(new ScriptText(name, rule.getScriptText())));
					} catch (Exception e) {
						errorRules.put(rule.getRule().getValue(), e.getMessage());
					}
				}
			}
			Display.getDefault().asyncExec(new Runnable() {
				@Override
				public void run() {
					agent.update();
					for(Entry<String, String> entry : errorRules.entrySet()) {
						view.addErrorRule(agent.getAddress(), agent.getPort(), entry.getKey(), entry.getValue());
					}
					view.refresh();
				}
			});
			return Status.OK_STATUS;
		}

		private void addFile(String path) {
			if(files == null) {
				files = new ArrayList<String>();
			}
			files.add(path);
		}

		private void addRule(RuleScript rule) {
			if(rules == null) {
				rules = new ArrayList<RuleScript>();
			}
			rules.add(rule);
		}

		private void setFile(IFile file) {
			this.file = file;
		}
	}

	/**
	 * The thread class for uninstalling to Byteman agent and refreshing the {@link BytemanAgentView}.
	 */
	private static class UninstallRuleJob extends Job {

		private BytemanAgentView view;
		private Agent agent;
		private List<ScriptText> scriptTexts;

		public UninstallRuleJob(BytemanAgentView view, Agent agent, List<ScriptText> scriptTexts) {
			super("UninstallRuleJob");
			this.view = view;
			this.agent = agent;
			this.scriptTexts = scriptTexts;
		}

		@Override
		protected IStatus run(IProgressMonitor monitor) {
			try {
				new Submit(agent.getAddress(), agent.getPort()).deleteScripts(scriptTexts);
			} catch (Exception e) {
				BytemanEditorPlugin.logException(e);
				return new Status(Status.ERROR, BytemanEditorPlugin.PLUGIN_ID, "Failed to uninstall rule.", e);
			}
			Display.getDefault().asyncExec(new Runnable() {
				@Override
				public void run() {
					agent.update();
					view.refresh();
				}
			});
			return Status.OK_STATUS;
		}

	}

}
