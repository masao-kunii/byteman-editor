package jp.co.ntt.oss.jboss.byteman.editor.editor.codeassist;

import java.util.ArrayList;
import java.util.List;

import jp.co.ntt.oss.jboss.byteman.editor.BytemanEditorPlugin;
import jp.co.ntt.oss.jboss.byteman.editor.BytemanEditorPluginImages;
import jp.co.ntt.oss.jboss.byteman.editor.util.StringUtils;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.contentassist.CompletionProposal;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.templates.DocumentTemplateContext;
import org.eclipse.jface.text.templates.Template;
import org.eclipse.jface.text.templates.TemplateContext;
import org.eclipse.jface.text.templates.TemplateContextType;
import org.eclipse.jface.text.templates.TemplateProposal;

/**
 * Provides code completion for keywords of Byteman rule.
 */
public class BytemanRuleKeywordAssistProcessor extends AbstractBytemanAssistProcessor {
	
	private static final String[] PROPOSAL_RULE_KEYWORDS = {
		"RULE ",
		"CLASS ",
		"INTERFACE ",
		"METHOD ",
		"HELPER ",
		"AT ENTRY",
		"AT EXIT",
		"AT LINE ",
		"AT READ ",
		"AT WRITE ",
		"AT INVOKE ",
		"AT SYNCHRONIZE",
		"AT THROW",
		"AFTER READ ",
		"AFTER WRITE ",
		"AFTER INVOKE ",
		"AFTER SYNCHRONIZE",
		"BIND",
		"IF ",
		"DO",
		"ENDRULE"
	};

	private static final String TEMPLATE_RULE_TEXT = "RULE ${rule_name}\n" +
													 "CLASS ${class_name}\n" +
													 "METHOD ${method_name}\n" +
													 "IF TRUE\n" +
													 "DO\n" +
													 "# TODO Auto-generated rule\n" +
													 "ENDRULE";

	private static final Template TEMPLATE_RULE = new Template("RULE", "rule template", "", TEMPLATE_RULE_TEXT, true);

	@Override
	public ICompletionProposal[] computeCompletionProposals(ITextViewer viewer, int offset) {
		try {
			String lastWord = getLastWord(viewer, offset).toUpperCase();

			List<ICompletionProposal> proposals = new ArrayList<ICompletionProposal>();
			if(TEMPLATE_RULE_TEXT.startsWith(lastWord) && !TEMPLATE_RULE_TEXT.equals(lastWord)) {
				proposals.add(createRuleTemplate(viewer.getDocument(), offset - lastWord.length()));
			}
			for(String keyword: PROPOSAL_RULE_KEYWORDS){
				if(keyword.startsWith(lastWord) && !keyword.equals(lastWord)){
					proposals.add(new CompletionProposal(
							keyword, offset - lastWord.length(), lastWord.length(), keyword.length()));
				}
			}
			return proposals.toArray(new ICompletionProposal[proposals.size()]);

		} catch(Exception e){
			BytemanEditorPlugin.logException(e);
		}
		return null;
	}

	protected String getLastWord(ITextViewer viewer, int offset) throws Exception {
		String source = viewer.getDocument().get(0, offset);
		return StringUtils.getLastWord(source);
	}

	protected ICompletionProposal createRuleTemplate(IDocument document, int offset) {
		TemplateContext context = new DocumentTemplateContext(new TemplateContextType(), document, offset, 0);
		TemplateProposal proposal = new TemplateProposal(TEMPLATE_RULE, context, new Region(offset, 0), BytemanEditorPluginImages.IMG_OBJS_TEMPLATE);
		return proposal;
	}
}
