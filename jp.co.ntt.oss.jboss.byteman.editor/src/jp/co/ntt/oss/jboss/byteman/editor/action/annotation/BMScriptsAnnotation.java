package jp.co.ntt.oss.jboss.byteman.editor.action.annotation;

import java.util.List;

import org.eclipse.core.resources.IFile;

/**
 * Implementation of {@link BMUnitAnnotation} for multiple scripts.
 *
 */
public class BMScriptsAnnotation extends BMUnitAnnotation {

	private List<IFile> files;

	public BMScriptsAnnotation(List<IFile> files) {
		this.files = files;
	}

	@Override
	public String getAnnotationString() {
		StringBuilder sb = new StringBuilder();
		sb.append("@BMScripts(scripts={");
		for(int i = 0; i < files.size(); i++) {
			if(i != 0) {
				sb.append(",");
			}
			sb.append(toBMScript(files.get(i)));
		}
		sb.append("})");
		return sb.toString();
	}
}
