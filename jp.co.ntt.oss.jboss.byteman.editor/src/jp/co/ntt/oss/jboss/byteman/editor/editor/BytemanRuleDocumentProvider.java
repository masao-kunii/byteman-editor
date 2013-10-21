package jp.co.ntt.oss.jboss.byteman.editor.editor;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentPartitioner;
import org.eclipse.jface.text.rules.FastPartitioner;
import org.eclipse.ui.editors.text.FileDocumentProvider;

/**
 * The document provider of {@link BytemanRuleEditor}.
 *
 */
public class BytemanRuleDocumentProvider extends FileDocumentProvider {

	protected IDocument createDocument(Object element) throws CoreException {
		IDocument document = super.createDocument(element);
		if (document != null) {
			IDocumentPartitioner partitioner = new FastPartitioner(
					new BytemanRulePartitionScanner(),
					new String[] {
						BytemanRulePartitionScanner.RULE_COMMENT,
						BytemanRulePartitionScanner.RULE_DEF_LINE
					});
			partitioner.connect(document);
			document.setDocumentPartitioner(partitioner);
		}
		return document;
	}
}