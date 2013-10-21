package jp.co.ntt.oss.jboss.byteman.editor.action.annotation;

import org.eclipse.core.resources.IFile;

/**
 * Implementation of {@link BMUnitAnnotation} for a single script.
 *
 */
public class BMScriptAnnotation extends BMUnitAnnotation {

	private IFile file;

	public BMScriptAnnotation(IFile file) {
		this.file = file;
	}

	@Override
	public String getAnnotationString() {
		return toBMScript(file);
	}
}
