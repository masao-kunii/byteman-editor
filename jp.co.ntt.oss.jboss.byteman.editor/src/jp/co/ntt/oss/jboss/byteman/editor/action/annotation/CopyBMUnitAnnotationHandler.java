package jp.co.ntt.oss.jboss.byteman.editor.action.annotation;

import jp.co.ntt.oss.jboss.byteman.editor.util.StringUtils;
import jp.co.ntt.oss.jboss.byteman.editor.util.UIUtils;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * This handler class copies the BMUnit annotation to the clipboard.
 * The following annotations are created from the current selection.
 * <ul>
 * <li>BMRule</li>
 * <li>BMRules</li>
 * <li>BMScript</li>
 * <li>BMScripts</li>
 * </ul>
 */
public class CopyBMUnitAnnotationHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		BMUnitAnnotation annotation = getAnnotation(event);
		if(annotation != null) {
			copyToClipbord(annotation.getAnnotationString());
		}
		return null;
	}

	protected BMUnitAnnotation getAnnotation(ExecutionEvent event) {
		return BMUnitAnnotationFactory.create(HandlerUtil.getCurrentSelection(event));
	}

	protected void copyToClipbord(String annotation) {
		if(StringUtils.isNotEmpty(annotation)) {
			UIUtils.copyToClipbord(annotation);
		}
	}
}
