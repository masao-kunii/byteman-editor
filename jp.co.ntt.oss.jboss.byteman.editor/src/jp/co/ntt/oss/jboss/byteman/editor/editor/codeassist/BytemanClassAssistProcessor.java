package jp.co.ntt.oss.jboss.byteman.editor.editor.codeassist;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import jp.co.ntt.oss.jboss.byteman.editor.BytemanEditorPlugin;
import jp.co.ntt.oss.jboss.byteman.editor.util.JavaUtils;
import jp.co.ntt.oss.jboss.byteman.editor.util.StringUtils;

import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.compiler.util.SuffixConstants;
import org.eclipse.jdt.internal.core.DefaultWorkingCopyOwner;
import org.eclipse.jdt.internal.ui.text.java.AbstractJavaCompletionProposal;
import org.eclipse.jdt.internal.ui.text.java.JavaCompletionProposal;
import org.eclipse.jdt.internal.ui.text.java.LazyJavaTypeCompletionProposal;
import org.eclipse.jdt.ui.text.java.CompletionProposalCollector;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposal;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.ICompletionProposal;

/**
 * This class provides code completion for the class name.
 */
@SuppressWarnings("restriction")
public class BytemanClassAssistProcessor extends AbstractBytemanAssistProcessor {

	private static final Pattern PATTERN_JAVA_LANG = Pattern.compile(".* - java\\.lang$|.* - java\\.lang\\..*");
	private static final Pattern PATTERN_BYTEMAN = Pattern.compile(".* - org\\.jboss\\.byteman$|.* - org\\.jboss\\.byteman\\..*");

	@Override
	public ICompletionProposal[] computeCompletionProposals(ITextViewer viewer, int offset) {
		String lineWord = StringUtils.trimHead(getLineString(viewer, offset));
		if(lineWord.equals("CLASS")
				|| lineWord.equals("INTERFACE")) {
			return null;
		}
		return computeCompletionProposals(viewer, offset, false);
	}

	protected ICompletionProposal[] computeCompletionProposals(ITextViewer viewer, int offset, boolean isHelper) {
		IJavaProject project = getActiveProject();
		if(project == null || !project.isOpen()){
			return null;
		}

		try {
			String source = viewer.getDocument().get(0, offset);
			String lastWord = StringUtils.getLastWord(source);

			IJavaCompletionProposal[] proposals = getJavaCompletionProposals(project, lastWord);

			List<ICompletionProposal> result = new ArrayList<ICompletionProposal>();

			for(ICompletionProposal proposal : proposals) {
				if(proposal.getImage() != null){
					if(proposal instanceof LazyJavaTypeCompletionProposal ||
							proposal instanceof JavaCompletionProposal){
						AbstractJavaCompletionProposal p = (AbstractJavaCompletionProposal) proposal;
						if("_xxx".equals(p.getDisplayString())
								|| (PATTERN_BYTEMAN.matcher(p.getDisplayString()).matches() && !isHelper)) {
							continue;
						}
						if(PATTERN_JAVA_LANG.matcher(p.getDisplayString()).matches()) {
							if(!isAssistJavaLangPackage()) {
								continue;
							}
							if(!p.getReplacementString().startsWith("java.lang.")) {
								p.setReplacementString("java.lang." + p.getReplacementString());
							}
						}

						String replacementString = p.getReplacementString();
						IType type = project.findType(replacementString);
						if(type != null) {
							p.setReplacementString(convert4InnerClass(type, replacementString));
						}

						p.setReplacementOffset(offset - lastWord.length());
						p.setReplacementLength(lastWord.length());
						p.setCursorPosition(p.getReplacementString().length());
						result.add(p);
					}
				}
			}

			return result.toArray(new ICompletionProposal[result.size()]);

		} catch(Exception ex){
			BytemanEditorPlugin.logException(ex);
		}

		return null;
	}

	protected IJavaCompletionProposal[] getJavaCompletionProposals(IJavaProject project, String lastWord) throws JavaModelException {
		CompletionProposalCollector collector = new CompletionProposalCollector(project);
		ICompilationUnit cu = JavaUtils.getTemporaryCompilationUnit(project);

		String dummySource = "public class _xxx { public static void hoge(){ " + lastWord + "}}";

		JavaUtils.setContentsToCU(cu, dummySource);
		cu.codeComplete(dummySource.length() - 2, collector, DefaultWorkingCopyOwner.PRIMARY);

		return collector.getJavaCompletionProposals();
	}

	protected String convert4InnerClass(IType type, String replacementString) {
		if(type != null) {
			IJavaElement parent = type.getParent();
			if(parent instanceof IClassFile) {
				String name = parent.getElementName().substring(0, parent.getElementName().length() - SuffixConstants.SUFFIX_STRING_class.length());
				if(name.indexOf("$") > 0) {
					replacementString = replacementString.substring(0, replacementString.length() - name.length()) + name;
				}
			} else {
				while(parent instanceof IType) {
					int lastDotPos = replacementString.lastIndexOf(".");
					replacementString = replacementString.substring(0, lastDotPos) + "$" + replacementString.substring(lastDotPos + 1);
					parent = parent.getParent();
				}
			}
		}
		return replacementString;
	}

	protected boolean isAssistJavaLangPackage() {
		return BytemanEditorPlugin.getDefault().getPreferenceStore().getBoolean(
				BytemanEditorPlugin.PREF_ASSIST_JAVA_LANG_PACKAGE);
	}
}
