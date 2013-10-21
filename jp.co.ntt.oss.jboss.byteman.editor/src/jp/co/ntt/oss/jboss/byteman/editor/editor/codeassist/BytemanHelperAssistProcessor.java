package jp.co.ntt.oss.jboss.byteman.editor.editor.codeassist;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import jp.co.ntt.oss.jboss.byteman.editor.BytemanEditorPlugin;
import jp.co.ntt.oss.jboss.byteman.editor.BytemanEditorPluginImages;
import jp.co.ntt.oss.jboss.byteman.editor.util.StringUtils;

import org.eclipse.jdt.internal.ui.text.java.AbstractJavaCompletionProposal;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.CompletionProposal;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.swt.graphics.Image;
import org.jboss.byteman.rule.helper.Helper;

/**
 * This class provides code completion for the class name at the helper context.
 *
 */
@SuppressWarnings("restriction")
public class BytemanHelperAssistProcessor extends BytemanClassAssistProcessor {

	@Override
	public ICompletionProposal[] computeCompletionProposals(ITextViewer viewer, int offset) {
		if(StringUtils.trimHead(getLineString(viewer, offset)).equals("HELPER")) {
			return null;
		}
		ICompletionProposal[] parentProposals = computeCompletionProposals(viewer, offset, true);
		if(existsDefaultHelper(parentProposals)) {
			return parentProposals;
		}
		List<ICompletionProposal> proposals = new ArrayList<ICompletionProposal>();
		try {
			String source = viewer.getDocument().get(0, offset);
			String lastWord = StringUtils.getLastWord(source);
			if(StringUtils.isNotEmpty(lastWord)
					&& !Helper.class.getCanonicalName().equals(lastWord)
					&& (Helper.class.getSimpleName().toLowerCase().startsWith(lastWord.toLowerCase())
							|| Helper.class.getCanonicalName().toLowerCase().startsWith(lastWord.toLowerCase()))) {
				proposals.add(new CompletionProposal(
						Helper.class.getCanonicalName(), offset - lastWord.length(), lastWord.length(),
						Helper.class.getCanonicalName().length(), getClassImage(),
						Helper.class.getSimpleName() + " - " + Helper.class.getPackage().getName(), null, null));
			}
		} catch (Exception e) {
			BytemanEditorPlugin.logException(e);
		}
		if(parentProposals != null) {
			proposals.addAll(Arrays.asList(parentProposals));
		}
		return proposals.toArray(new ICompletionProposal[proposals.size()]);
	}

	protected boolean existsDefaultHelper(ICompletionProposal[] proposals) {
		if(proposals == null) {
			return false;
		}
		for(ICompletionProposal p : proposals) {
			if(p instanceof AbstractJavaCompletionProposal) {
				if(((AbstractJavaCompletionProposal) p).getReplacementString().equals(Helper.class.getCanonicalName())) {
					return true;
				}
			}
		}
		return false;
	}

	protected Image getClassImage() {
		return BytemanEditorPluginImages.IMG_CLASS;
	}
}
