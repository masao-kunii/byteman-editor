package jp.co.ntt.oss.jboss.byteman.editor.action.annotation;

import jp.co.ntt.oss.jboss.byteman.editor.BytemanEditorPlugin;
import jp.co.ntt.oss.jboss.byteman.editor.util.StringUtils;
import jp.co.ntt.oss.jboss.byteman.editor.util.UIUtils;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;

/**
 * This action class copies the BMUnit annotation to the clipboard.
 * The following annotations are created from the current selection.
 * <ul>
 * <li>BMRule</li>
 * <li>BMRules</li>
 * <li>BMScript</li>
 * <li>BMScripts</li>
 * </ul>
 */
public class CopyBMUnitAnnotationAction extends Action {

	private ISelectionProvider selectionProvider;

	public CopyBMUnitAnnotationAction(ISelectionProvider provider) {
		super(BytemanEditorPlugin.getResourceString("menu.copyBMUnit"));
		this.selectionProvider = provider;
	}

	@Override
	public void run() {
		BMUnitAnnotation annotation = getAnnotation(selectionProvider.getSelection());
		if(annotation != null) {
			copyToClipbord(annotation.getAnnotationString());
		}
	}

	protected BMUnitAnnotation getAnnotation(ISelection selection) {
		return BMUnitAnnotationFactory.create(selection);
	}

	protected void copyToClipbord(String annotation) {
		if(StringUtils.isNotEmpty(annotation)) {
			UIUtils.copyToClipbord(annotation);
		}
	}
}
