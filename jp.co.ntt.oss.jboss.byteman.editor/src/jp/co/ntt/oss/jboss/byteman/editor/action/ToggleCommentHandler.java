package jp.co.ntt.oss.jboss.byteman.editor.action;

import jp.co.ntt.oss.jboss.byteman.editor.BytemanEditorPlugin;
import jp.co.ntt.oss.jboss.byteman.editor.util.EditorUtils;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.ui.texteditor.ITextEditor;

/**
 * This handler class comment the current line or all selected lines out or uncomment them.
 *
 */
public class ToggleCommentHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		ITextEditor editor = getActiveEditor();
		ITextSelection selection = (ITextSelection) editor.getSelectionProvider().getSelection();
		IDocument document = editor.getDocumentProvider().getDocument(editor.getEditorInput());

		try {
			String text;
			int offset = selection.getOffset();
			int line = document.getLineOfOffset(offset);
			int lineOffset = document.getLineOffset(line);
			int length = selection.getLength() + (offset - lineOffset);
			if(length == 0 && offset == lineOffset) {
				// trim line feed
				text = document.get(lineOffset, document.getLineLength(line)).trim();
				length = text.length();
			} else {
				text = document.get(lineOffset, length);
			}

			boolean isCommented = isCommented(text);

			if(isCommented){
				text = text.replaceAll("(^|\n)#", "$1");
			} else {
				text = text.replaceAll("\n", "\n#");
				text = "#" + text;

				if(length > 0 && text.endsWith("\n#")){
					text = text.substring(0, text.length() - 1);
				}
			}

			editor.getDocumentProvider().getDocument(editor.getEditorInput()).replace(lineOffset, length, text);
			if(selection.getLength() > 0) {
				int selectionOffset = offset;
				int selectionLength = text.length() - (offset - lineOffset);
				if(offset != lineOffset) {
					if(isCommented) {
						selectionOffset -= 1;
						selectionLength += 1;
					} else {
						selectionOffset += 1;
						selectionLength -= 1;
					}
				}
				editor.getSelectionProvider().setSelection(new TextSelection(selectionOffset, selectionLength));
			}
		} catch(BadLocationException ex){
			BytemanEditorPlugin.logException(ex);
		}

		return null;
	}

	protected boolean isCommented(String text){
		String[] lines = text.split("\n");
		if(lines.length == 0) {
			return false;
		}
		for(String line: lines){
			if(!line.startsWith("#")){
				return false;
			}
		}
		return true;
	}

	protected ITextEditor getActiveEditor() {
		return (ITextEditor) EditorUtils.getActiveEditor();
	}
}
