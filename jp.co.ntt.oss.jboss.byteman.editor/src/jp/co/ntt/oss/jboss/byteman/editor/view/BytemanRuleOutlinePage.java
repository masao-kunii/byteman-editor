package jp.co.ntt.oss.jboss.byteman.editor.view;

import jp.co.ntt.oss.jboss.byteman.editor.BytemanEditorPlugin;
import jp.co.ntt.oss.jboss.byteman.editor.action.InstallRuleActionItems;
import jp.co.ntt.oss.jboss.byteman.editor.action.annotation.CopyBMUnitAnnotationAction;
import jp.co.ntt.oss.jboss.byteman.editor.editor.BytemanRuleEditor;
import jp.co.ntt.oss.jboss.byteman.editor.view.model.BytemanContentProvider;
import jp.co.ntt.oss.jboss.byteman.editor.view.model.BytemanLabelProvider;
import jp.co.ntt.oss.jboss.byteman.editor.view.model.OutlineModel;
import jp.co.ntt.oss.jboss.byteman.editor.view.model.RuleOutlineModel;
import jp.co.ntt.oss.jboss.byteman.editor.view.model.RuleOutlineModel.Rule;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.views.contentoutline.ContentOutlinePage;

/**
 * The content outline page of {@link BytemanRuleEditor}.
 *
 */
public class BytemanRuleOutlinePage extends ContentOutlinePage {

	private BytemanRuleEditor editor;
	private RuleOutlineModel ruleOutlineModel;
	private boolean selectEditor = true;
	private boolean selectViewer = true;

	public BytemanRuleOutlinePage(BytemanRuleEditor editor) {
		this.editor = editor;
	}

	public void update() {
		Display.getDefault().asyncExec(new Runnable() {

			@Override
			public void run() {
				IDocumentProvider provider = editor.getDocumentProvider();
				IEditorInput input = editor.getEditorInput();
				if(provider == null) {
					return;
				}
				RuleOutlineModel newRuleOutlineModel;
				if(input instanceof IFileEditorInput) {
					newRuleOutlineModel = new RuleOutlineModel(((IFileEditorInput)input).getFile(),
							provider.getDocument(input).get());
				} else {
					newRuleOutlineModel = new RuleOutlineModel(null,
							provider.getDocument(input).get());
				}

				TreeViewer treeViewer = getTreeViewer();

				if(ruleOutlineModel == null){
					ruleOutlineModel = newRuleOutlineModel;
					treeViewer.setInput(ruleOutlineModel);
				} else {
					ruleOutlineModel.merge(newRuleOutlineModel);
				}

				treeViewer.refresh();
			}
		});
	}

	public void createControl(Composite parent) {
		super.createControl(parent);
		TreeViewer treeViewer = getTreeViewer();
		treeViewer.setContentProvider(new BytemanRuleOutlineContentProvider());
		treeViewer.setLabelProvider(new BytemanLabelProvider());
		treeViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				if(!selectEditor) {
					return;
				}
				IStructuredSelection selection = (IStructuredSelection)event.getSelection();
				Object element = selection.getFirstElement();
				if(element instanceof OutlineModel && selectViewer) {
					selectViewer = false;
					try {
						IDocument document = editor.getDocumentProvider().getDocument(editor.getEditorInput());
						int offset = document.getLineOffset(((OutlineModel)element).getLineNumber())
										+ ((OutlineModel)element).getLineOfOffset();
						editor.selectAndReveal(offset, ((OutlineModel)element).getLength());
					} catch (BadLocationException e) {
						BytemanEditorPlugin.logException(e);
					}
					setSelection(selection);
				}
			}
		});
		IEditorInput input = editor.getEditorInput();
		if(input instanceof IFileEditorInput) {
			ruleOutlineModel = new RuleOutlineModel(((IFileEditorInput)input).getFile(),
					editor.getDocumentProvider().getDocument(input).get());
		} else {
			ruleOutlineModel = new RuleOutlineModel(null,
					editor.getDocumentProvider().getDocument(input).get());
		}
		treeViewer.setInput(ruleOutlineModel);
		treeViewer.expandAll();

		// create context menu
		final IContributionItem installRuleAction = new InstallRuleActionItems();
		final IAction copyBMUnitAction = new CopyBMUnitAnnotationAction(getTreeViewer());

		MenuManager menuManager = new MenuManager();
		menuManager.setRemoveAllWhenShown(true);
		menuManager.addMenuListener(new IMenuListener() {
			@Override
			public void menuAboutToShow(IMenuManager menu) {
				ITreeSelection selection = (ITreeSelection) getTreeViewer().getSelection();
				if(selection.getFirstElement() instanceof Rule) {
					if(selection.size() == 1) {
						MenuManager mm = new MenuManager(BytemanEditorPlugin.getResourceString("menu.installRule"));
						mm.add(installRuleAction);
						menu.add(mm);
					}
					menu.add(copyBMUnitAction);
				}
			}
		});
		Menu menu = menuManager.createContextMenu(treeViewer.getControl());
		treeViewer.getControl().setMenu(menu);
		getSite().registerContextMenu(BytemanEditorPlugin.PLUGIN_ID + ".outline", menuManager, treeViewer);
	}

	public void selectRule(int line){
		if(ruleOutlineModel == null || !selectViewer) {
			selectViewer = true;
			return;
		}
		OutlineModel model = ruleOutlineModel.getModelOfLine(line, false);
		if(model != null) {
			selectEditor = false;
			getTreeViewer().setExpandedState(ruleOutlineModel.getModelOfLine(line, true), true);
			setSelection(new StructuredSelection(model));
			selectEditor = true;
		}
	}

	private static class BytemanRuleOutlineContentProvider extends BytemanContentProvider {

		@Override
		public Object[] getChildren(Object parentElement) {
			if(parentElement instanceof RuleOutlineModel) {
				return ((RuleOutlineModel) parentElement).getChildren();
			} else if(parentElement instanceof OutlineModel) {
				return ((OutlineModel) parentElement).getChildren();
			}
			return new Object[0];
		}
	}
}
