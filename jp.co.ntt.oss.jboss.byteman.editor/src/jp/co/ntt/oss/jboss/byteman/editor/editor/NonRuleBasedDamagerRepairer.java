package jp.co.ntt.oss.jboss.byteman.editor.editor;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITypedRegion;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.TextPresentation;
import org.eclipse.jface.text.presentation.IPresentationDamager;
import org.eclipse.jface.text.presentation.IPresentationRepairer;
import org.eclipse.core.runtime.Assert;
import org.eclipse.swt.custom.StyleRange;

/**
 * Implementation of {@code IPresentationDamager} and {@code IPresentationRepairer}.
 *
 */
public class NonRuleBasedDamagerRepairer
	implements IPresentationDamager, IPresentationRepairer {

	/** The document this object works on */
	protected IDocument fDocument;
	/** The default text attribute if non is returned as data by the current token */
	protected TextAttribute fDefaultTextAttribute;

	/**
	 * Constructor with {@code TextAttribute}.
	 */
	public NonRuleBasedDamagerRepairer(TextAttribute defaultTextAttribute) {
		Assert.isNotNull(defaultTextAttribute);

		fDefaultTextAttribute = defaultTextAttribute;
	}

	/**
	 * @see IPresentationRepairer#setDocument(IDocument)
	 */
	public void setDocument(IDocument document) {
		fDocument = document;
	}

	/**
	 * Returns offset of the end of the line which contains the specified offset.
	 * When the specified offset indicates the middle of newline characters(between CR and LF),
	 * returns offset of the end of next line.
	 * @param offset offset from head of the document
	 * @return offset of the end of the line from the head of the document
	 * @exception BadLocationException if offset is invalid in the current document
	 */
	protected int endOfLineOf(int offset) throws BadLocationException {

		IRegion info = fDocument.getLineInformationOfOffset(offset);
		if (offset <= info.getOffset() + info.getLength())
			return info.getOffset() + info.getLength();

		int line = fDocument.getLineOfOffset(offset);
		try {
			info = fDocument.getLineInformation(line + 1);
			return info.getOffset() + info.getLength();
		} catch (BadLocationException x) {
			return fDocument.getLength();
		}
	}

	/**
	 * @see IPresentationDamager#getDamageRegion(ITypedRegion, DocumentEvent, boolean)
	 */
	public IRegion getDamageRegion(
		ITypedRegion partition,
		DocumentEvent event,
		boolean documentPartitioningChanged) {
		if (!documentPartitioningChanged) {
			try {

				IRegion info =
					fDocument.getLineInformationOfOffset(event.getOffset());
				int start = Math.max(partition.getOffset(), info.getOffset());

				int end =
					event.getOffset()
						+ (event.getText() == null
							? event.getLength()
							: event.getText().length());

				if (info.getOffset() <= end
					&& end <= info.getOffset() + info.getLength()) {
					// optimize the case of the same line
					end = info.getOffset() + info.getLength();
				} else
					end = endOfLineOf(end);

				end =
					Math.min(
						partition.getOffset() + partition.getLength(),
						end);
				return new Region(start, end - start);

			} catch (BadLocationException x) {
			}
		}

		return partition;
	}

	/**
	 * @see IPresentationRepairer#createPresentation(TextPresentation, ITypedRegion)
	 */
	public void createPresentation(
		TextPresentation presentation,
		ITypedRegion region) {
		addRange(
			presentation,
			region.getOffset(),
			region.getLength(),
			fDefaultTextAttribute);
	}

	/**
	 * Adds style information to the given text presentation.
	 *
	 * @param presentation the text presentation to be extended
	 * @param offset the offset of the range to be styled
	 * @param length the length of the range to be styled
	 * @param attr the attribute describing the style of the range
	 */
	protected void addRange(
		TextPresentation presentation,
		int offset,
		int length,
		TextAttribute attr) {
		if (attr != null)
			presentation.addStyleRange(
				new StyleRange(
					offset,
					length,
					attr.getForeground(),
					attr.getBackground(),
					attr.getStyle()));
	}
}