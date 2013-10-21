package jp.co.ntt.oss.jboss.byteman.editor.editor;

import jp.co.ntt.oss.jboss.byteman.editor.BytemanEditorPlugin;

import org.eclipse.jface.text.*;

/**
 * Implementation of {@code ITextDoubleClickStrategy} for {@link BytemanRuleEditor}.
 * When double-clicked on the editor, the word which is at the cursor position will be selected.
 * If the cursor position is in a double quotation, all contents in the double quotation will be selected.
 *
 */
public class BytemanRuleDoubleClickStrategy implements ITextDoubleClickStrategy {

	protected ITextViewer fText;

	public void doubleClicked(ITextViewer part) {
		int pos = part.getSelectedRange().x;

		if (pos < 0){
			return;
		}

		fText = part;

		if (!selectComment(pos)) {
			selectWord(pos);
		}
	}

	protected boolean selectComment(int caretPos) {
		IDocument doc = fText.getDocument();
		int startPos, endPos;

		try {
			int pos = caretPos;
			char c = ' ';

			while (pos >= 0) {
				c = doc.getChar(pos);
				if (c == '\"' && pos > 0 && doc.getChar(pos - 1) == '\\') {
					pos -= 2;
					continue;
				}
				if (c == '\n' || c == '\"'){
					break;
				}
				--pos;
			}

			if (c != '\"' || !inComment(pos)){
				return false;
			}

			startPos = pos;

			pos = caretPos;
			int length = doc.getLength();
			c = ' ';

			while (pos < length) {
				c = doc.getChar(pos);
				if(c == '\\'){
					pos += 2;
					continue;
				}
				if (c == '\n' || c == '\"'){
					break;
				}
				++pos;
			}
			if (c != '\"'){
				return false;
			}

			endPos = pos;

			selectRange(startPos, endPos);
			return true;

		} catch (BadLocationException ex) {
			BytemanEditorPlugin.logException(ex);
		}

		return false;
	}

	protected boolean inComment(int pos) throws BadLocationException {
		IDocument doc = fText.getDocument();
		int lineOffset = doc.getLineOffset(doc.getLineOfOffset(pos));
		char c = ' ';
		int cnt = 0;
		while(pos >= lineOffset) {
			c = doc.getChar(pos);
			if (c == '\"' && pos > 0 && doc.getChar(pos - 1) == '\\') {
				pos -= 2;
				continue;
			}
			if (c == '\"'){
				cnt++;
			}
			--pos;
		}
		return cnt % 2 == 1;
	}

	protected boolean selectWord(int caretPos) {

		IDocument doc = fText.getDocument();
		int startPos, endPos;

		try {

			int pos = caretPos;
			char c;

			while (pos >= 0) {
				c = doc.getChar(pos);
				if (!Character.isJavaIdentifierPart(c)){
					break;
				}
				--pos;
			}

			startPos = pos;

			pos = caretPos;
			int length = doc.getLength();

			while (pos < length) {
				c = doc.getChar(pos);
				if (!Character.isJavaIdentifierPart(c)){
					break;
				}
				++pos;
			}

			endPos = pos;
			selectRange(startPos, endPos);
			return true;

		} catch (BadLocationException ex) {
			BytemanEditorPlugin.logException(ex);
		}

		return false;
	}

	private void selectRange(int startPos, int stopPos) {
		int offset = startPos + 1;
		int length = stopPos - offset;
		fText.setSelectedRange(offset, length);
	}
}