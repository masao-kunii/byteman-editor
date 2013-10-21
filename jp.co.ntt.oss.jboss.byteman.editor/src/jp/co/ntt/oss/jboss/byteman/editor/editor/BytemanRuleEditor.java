package jp.co.ntt.oss.jboss.byteman.editor.editor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import jp.co.ntt.oss.jboss.byteman.editor.BytemanEditorPlugin;
import jp.co.ntt.oss.jboss.byteman.editor.BytemanEditorPluginImages;
import jp.co.ntt.oss.jboss.byteman.editor.util.EditorUtils;
import jp.co.ntt.oss.jboss.byteman.editor.validator.BytemanRuleValidator;
import jp.co.ntt.oss.jboss.byteman.editor.view.BytemanRuleOutlinePage;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.internal.ui.viewsupport.IProblemChangedListener;
import org.eclipse.jdt.ui.PreferenceConstants;
import org.eclipse.jface.text.ITextListener;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.TextEvent;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.IVerticalRuler;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IStorageEditorInput;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.editors.text.TextEditor;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;

/**
 * This is a text editor to aid creating Byteman rules.
 *
 * This editor provides the following features.
 * <ul>
 * <li>Code completion</li>
 * <li>Keywords highlight</li>
 * <li>Outline view</li>
 * <li>Creating rule templates</li>
 * <li>Validating rules</li>
 * <li>Creating BMUnit annotation</li>
 * <li>Jumping to trigger point</li>
 * <li>Installing rules</li>
 * </ul>
 */
@SuppressWarnings("restriction")
public class BytemanRuleEditor extends TextEditor {

	private TokenManager tokenManager;
	private BytemanRuleOutlinePage ruleOutline;
	private Validator validator;

	private AbstractSelectionChangedListener selectionChangedListener;
	private IPropertyChangeListener propertyChangeListener = new IPropertyChangeListener() {
		@Override
		public void propertyChange(PropertyChangeEvent event) {
			tokenManager.refreshTokens();
			doValidate();
			getSourceViewer().invalidateTextPresentation();
		}
	};
	private ITextListener textListener = new ITextListener() {
		@Override
		public void textChanged(TextEvent event) {
			if(event.getDocumentEvent() != null) {
				validator.resetCount();
			}
		}
	};

	private IProblemChangedListener problemChangedListener = new IProblemChangedListener() {
		@Override
		public void problemsChanged(IResource[] arg0, boolean arg1) {
			if(ruleOutline != null) {
				ruleOutline.update();
			}
			refreshTitleIcon();
		}
	};


	public BytemanRuleEditor() {
		super();
		setEditorContextMenuId(BytemanEditorPlugin.CONTEXT_MENU_ID);
		tokenManager = new TokenManager();
		setSourceViewerConfiguration(new BytemanRuleConfiguration(tokenManager));
		setDocumentProvider(new BytemanRuleDocumentProvider());

		PreferenceConstants.getPreferenceStore().addPropertyChangeListener(propertyChangeListener);
		BytemanEditorPlugin.getDefault().getPreferenceStore().addPropertyChangeListener(propertyChangeListener);
		JavaPlugin.getDefault().getProblemMarkerManager().addListener(problemChangedListener);

		validator = new Validator(this);
	}

	@Override
	public void createPartControl(Composite parent) {
		super.createPartControl(parent);

		selectionChangedListener = new BytemanEditorSelectionChangedListener();
		selectionChangedListener.install(getSelectionProvider());
	}

	@Override
	public void dispose() {
		if(selectionChangedListener != null) {
			selectionChangedListener.uninstall(getSelectionProvider());
			selectionChangedListener = null;
		}
		PreferenceConstants.getPreferenceStore().removePropertyChangeListener(propertyChangeListener);
		BytemanEditorPlugin.getDefault().getPreferenceStore().removePropertyChangeListener(propertyChangeListener);
		propertyChangeListener = null;
		JavaPlugin.getDefault().getProblemMarkerManager().removeListener(problemChangedListener);
		problemChangedListener = null;
		getSourceViewer().removeTextListener(textListener);
		textListener = null;

		tokenManager.dispose();
		tokenManager = null;
		validator.stopThread();
		validator = null;
		super.dispose();
	}

	@Override
	public void doSave(IProgressMonitor progressMonitor) {
		EditorUtils.convertToLF(this);
		super.doSave(progressMonitor);
		doValidate();
	}

	@Override
	public void doSaveAs() {
		EditorUtils.convertToLF(this);
		super.doSaveAs();
		doValidate();
	}

	private void clearErrorMessage(){
		Display.getDefault().syncExec(new Runnable() {
			@Override
			public void run() {
				getEditorSite().getActionBars().getStatusLineManager().setErrorMessage("");
			}
		});
	}

	private void doValidate() {
		clearErrorMessage();
		IEditorInput input = getEditorInput();
		if(input instanceof IFileEditorInput) {
			IResource resource = ((IFileEditorInput)input).getFile();
			String ruleText = getDocumentProvider().getDocument(input).get();
			if(resource != null && ruleText != null) {
				BytemanRuleValidator.validate(resource, ruleText);
			}
		}
		if(ruleOutline != null) {
			ruleOutline.update();
		}
		refreshTitleIcon();
	}

	@Override
	protected void initializeKeyBindingScopes() {
		super.initializeKeyBindingScopes();
		String[] keyBinds = {"jp.co.ntt.oss.jboss.byteman.editor.context1"};
		setKeyBindingScopes(keyBinds);
	}

	@Override
	@SuppressWarnings("rawtypes")
	public Object getAdapter(Class adapter) {
		if(IContentOutlinePage.class.equals(adapter)) {
			if(ruleOutline == null) {
				ruleOutline = new BytemanRuleOutlinePage(this);
			}
			return ruleOutline;
		}
		return super.getAdapter(adapter);
	}

	protected IDocumentProvider createDocumentProvider(IEditorInput editorInput) {
		if(editorInput instanceof IFileEditorInput) {
			return new BytemanRuleTextDocumentProvider();
		} else if(editorInput instanceof IStorageEditorInput) {
			return new BytemanRuleDocumentProvider();
		} else {
			return new BytemanRuleTextDocumentProvider();
		}
	}

	private void refreshTitleIcon() {
		Display.getDefault().asyncExec(new Runnable() {

			@Override
			public void run() {
				IEditorInput input = getEditorInput();
				if(input instanceof IFileEditorInput) {
					Image image = null;
					try {
						int severity = ((IFileEditorInput)input).getFile().findMaxProblemSeverity(
								BytemanEditorPlugin.MARKER_ID, false, IResource.DEPTH_ZERO);
						switch (severity) {
						case IMarker.SEVERITY_ERROR:
							image = BytemanEditorPluginImages.IMG_BYTEMAN_ERROR;
							break;
						case IMarker.SEVERITY_WARNING:
							image = BytemanEditorPluginImages.IMG_BYTEMAN_WARNING;
							break;
						}
					} catch (CoreException e) {
						BytemanEditorPlugin.logException(e);
					}

					if(image == null) {
						image = BytemanEditorPluginImages.IMG_BYTEMAN;
					}

					setTitleImage(image);
				}
			}
		});
	}

	@Override
	protected final void doSetInput(IEditorInput input) throws CoreException {
		setDocumentProvider(createDocumentProvider(input));
		super.doSetInput(input);
	}

	@Override
	public void init(IEditorSite site, IEditorInput input) throws PartInitException {
		super.init(site, input);
		if(input instanceof IFileEditorInput) {
			IProject project = ((IFileEditorInput)input).getFile().getProject();
			if(project == null) {
				return;
			}
			try {
				IProjectDescription description = project.getDescription();
				List<String> natures = new ArrayList<String>(Arrays.asList(description.getNatureIds()));
				if(!natures.contains(BytemanEditorPlugin.NATURE_ID)) {
					natures.add(BytemanEditorPlugin.NATURE_ID);
					description.setNatureIds(natures.toArray(new String[natures.size()]));
					project.setDescription(description, null);
				}
			} catch (CoreException e) {
				BytemanEditorPlugin.logException(e);
			}
			doValidate();
		}
		validator.start();
	}

	@Override
	protected ISourceViewer createSourceViewer(Composite parent, IVerticalRuler ruler, int styles) {
		ISourceViewer sourceViewer = super.createSourceViewer(parent, ruler, styles);
		sourceViewer.addTextListener(textListener);
		return sourceViewer;
	}

	private class BytemanEditorSelectionChangedListener extends AbstractSelectionChangedListener {
		@Override
		public void selectionChanged(SelectionChangedEvent event) {
			try {
				ISelection selection = event.getSelection();
				ITextSelection textSelection = (ITextSelection) selection;

				// Highlight the selected word.
				EditorUtils.highlightWord(
						getSourceViewer().getTextWidget(),
						textSelection,
						getDocumentProvider().getDocument(getEditorInput()),
						Display.getDefault().getSystemColor(SWT.COLOR_CYAN));

				// Selects the rule in the outline view.
				if(ruleOutline != null){
					ruleOutline.selectRule(textSelection.getStartLine());
				}

			} catch (Exception e) {
				BytemanEditorPlugin.logException(e);
			}
		}
	}

	private static class Validator extends Thread {

		private BytemanRuleEditor editor;
		private int count;
		private static final int limit = 5;
		private boolean running;

		public Validator(BytemanRuleEditor editor) {
			super();
			this.count = 0;
			this.editor = editor;
		}

		@Override
		public void run() {
			while(running) {
				try {
					Thread.sleep(100);
					if(count == limit) {
						editor.doValidate();
						count++;
					} else if(count < limit) {
						count++;
					}
				} catch (InterruptedException e) {
					BytemanEditorPlugin.logException(e);
				}
			}
		}

		@Override
		public synchronized void start() {
			count = 0;
			running = true;
			super.start();
		}

		public void resetCount() {
			count = 0;
		}

		public void stopThread() {
			running = false;
		}
	}
}
