package jp.co.ntt.oss.jboss.byteman.editor.action.annotation;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import jp.co.ntt.oss.jboss.byteman.editor.util.RuleScripts;
import jp.co.ntt.oss.jboss.byteman.editor.util.RuleScripts.RuleScript;
import jp.co.ntt.oss.jboss.byteman.editor.view.model.RuleOutlineModel.Rule;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;

/**
 * This factory class creates an instance of {@link BMUnitAnnotation}.
 *
 */
public class BMUnitAnnotationFactory {

	/**
	 * Creates a instance of {@link BMUnitAnnotation} by specified {@code ISelection}.
	 *
	 * @param selection an {@code ISelection} object to create {@link BMUnitAnnotation} instance
	 * @return {@link BMUnitAnnotation} instance
	 */
	public static BMUnitAnnotation create(ISelection selection) {
		if(selection instanceof ITextSelection) {
			return new BMRuleAnnotation(RuleScripts.get().getCurrentRuleScript());
		} else if(selection instanceof IStructuredSelection) {
			List<IFile> files = new ArrayList<IFile>();
			List<RuleScript> rules = new ArrayList<RuleScript>();
			@SuppressWarnings("rawtypes")
			Iterator iterator = ((IStructuredSelection) selection).iterator();
			while(iterator.hasNext()) {
				Object element = iterator.next();
				if(element instanceof IFile) {
					files.add((IFile) element);
				} else if(element instanceof Rule) {
					rules.add(((Rule) element).getRuleScript());
				}
			}
			if(files.size() > 0) {
				if(files.size() == 1) {
					return new BMScriptAnnotation(files.get(0));
				} else {
					return new BMScriptsAnnotation(files);
				}
			} else if(rules.size() > 0) {
				if(rules.size() == 1) {
					return new BMRuleAnnotation(rules.get(0));
				} else {
					return new BMRulesAnnotation(rules);
				}
			}
		}
		return null;
	}

}
