package jp.co.ntt.oss.jboss.byteman.editor.editor;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentPartitioner;
import org.eclipse.jface.text.rules.FastPartitioner;
import org.eclipse.ui.editors.text.TextFileDocumentProvider;
/**
 * Implementation of {@code TextFileDocumentProvider} for {@link BytemanRuleEditor}.
 */
public class BytemanRuleTextDocumentProvider extends TextFileDocumentProvider {

	@Override
	protected FileInfo createFileInfo(Object element) throws CoreException {
		FileInfo info = super.createFileInfo(element);
		if(info == null) {
			info = createEmptyFileInfo();
		}
		IDocument document = info.fTextFileBuffer.getDocument();
		if(document != null) {
			IDocumentPartitioner partitioner = new FastPartitioner(
					new BytemanRulePartitionScanner(),
					new String[] {
						BytemanRulePartitionScanner.RULE_COMMENT,
						BytemanRulePartitionScanner.RULE_DEF_LINE
					});
			partitioner.connect(document);
			document.setDocumentPartitioner(partitioner);
		}
		return info;
	}

}
