package jp.co.ntt.oss.jboss.byteman.editor.editor;

import jp.co.ntt.oss.jboss.byteman.editor.editor.codeassist.BytemanContentAssistProcessor;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextDoubleClickStrategy;
import org.eclipse.jface.text.contentassist.ContentAssistant;
import org.eclipse.jface.text.contentassist.IContentAssistant;
import org.eclipse.jface.text.presentation.IPresentationReconciler;
import org.eclipse.jface.text.presentation.PresentationReconciler;
import org.eclipse.jface.text.rules.DefaultDamagerRepairer;
import org.eclipse.jface.text.rules.RuleBasedScanner;
import org.eclipse.jface.text.source.DefaultAnnotationHover;
import org.eclipse.jface.text.source.IAnnotationHover;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.SourceViewerConfiguration;

/**
 * The configuration of {@link BytemanRuleEditor}.
 *
 */
public class BytemanRuleConfiguration extends SourceViewerConfiguration {

	private BytemanRuleDoubleClickStrategy doubleClickStrategy;

	private BytemanRuleScanner defaultScanner;
	private RuleBasedScanner commentScanner;
	private BytemanRuleScannerForDefLine defLineScanner;

	private ContentAssistant assistant;
	private TokenManager tokenManager;

	public BytemanRuleConfiguration(TokenManager tokenManager) {
		this.tokenManager = tokenManager;
	}

	@Override
	public String[] getConfiguredContentTypes(ISourceViewer sourceViewer) {
		return new String[] {
			IDocument.DEFAULT_CONTENT_TYPE,
			BytemanRulePartitionScanner.RULE_COMMENT,
			BytemanRulePartitionScanner.RULE_DEF_LINE
		};
	}

	@Override
	public ITextDoubleClickStrategy getDoubleClickStrategy(
		ISourceViewer sourceViewer,
		String contentType) {
		if (doubleClickStrategy == null){
			doubleClickStrategy = new BytemanRuleDoubleClickStrategy();
		}
		return doubleClickStrategy;
	}

	protected BytemanRuleScanner getDefaultBytemanRuleScanner() {
		if (defaultScanner == null) {
			defaultScanner = new BytemanRuleScanner(tokenManager);
			defaultScanner.setDefaultReturnToken(
					tokenManager.getToken(TokenManager.TOKEN_BYTEMAN_DEFAULT));

		}
		return defaultScanner;
	}

	protected RuleBasedScanner getBytemanRuleScannerForComment() {
		if (commentScanner == null) {
			commentScanner = new RuleBasedScanner();
			commentScanner.setDefaultReturnToken(
					tokenManager.getToken(TokenManager.TOKEN_JAVA_SINGLE_LINE_COMMENT));
		}
		return commentScanner;
	}

	protected RuleBasedScanner getBytemanRuleScannerForLine() {
		if (defLineScanner == null) {
			defLineScanner = new BytemanRuleScannerForDefLine(tokenManager);
			defLineScanner.setDefaultReturnToken(
					tokenManager.getToken(TokenManager.TOKEN_BYTEMAN_DEFAULT));
		}
		return defLineScanner;
	}

	/**
	 * Returns {@code IContentAssistant} instance. Only if the assistant is null, creates a new instance.
	 */
	public IContentAssistant getContentAssistant(ISourceViewer sourceViewer) {
		if(assistant==null){
			assistant = new ContentAssistant();
			assistant.setContextInformationPopupOrientation(IContentAssistant.CONTEXT_INFO_ABOVE);
			assistant.enableAutoInsert(true);

			assistant.setContentAssistProcessor(
					new BytemanContentAssistProcessor(), IDocument.DEFAULT_CONTENT_TYPE);
			assistant.setContentAssistProcessor(
					new BytemanContentAssistProcessor(), BytemanRulePartitionScanner.RULE_DEF_LINE);

			assistant.setInformationControlCreator(getInformationControlCreator(sourceViewer));
		}
		return assistant;
	}

	@Override
	public IPresentationReconciler getPresentationReconciler(ISourceViewer sourceViewer) {
		PresentationReconciler reconciler = new PresentationReconciler();

		DefaultDamagerRepairer dr = new DefaultDamagerRepairer(getDefaultBytemanRuleScanner());
		reconciler.setDamager(dr, IDocument.DEFAULT_CONTENT_TYPE);
		reconciler.setRepairer(dr, IDocument.DEFAULT_CONTENT_TYPE);

		dr = new DefaultDamagerRepairer(getBytemanRuleScannerForComment());
		reconciler.setDamager(dr, BytemanRulePartitionScanner.RULE_COMMENT);
		reconciler.setRepairer(dr, BytemanRulePartitionScanner.RULE_COMMENT);

		dr = new DefaultDamagerRepairer(getBytemanRuleScannerForLine());
		reconciler.setDamager(dr, BytemanRulePartitionScanner.RULE_DEF_LINE);
		reconciler.setRepairer(dr, BytemanRulePartitionScanner.RULE_DEF_LINE);

		return reconciler;
	}

	@Override
	public IAnnotationHover getAnnotationHover(ISourceViewer sourceViewer) {
		return new DefaultAnnotationHover(true);
	}
}