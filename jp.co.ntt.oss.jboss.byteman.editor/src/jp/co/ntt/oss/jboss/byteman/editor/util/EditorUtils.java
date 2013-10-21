package jp.co.ntt.oss.jboss.byteman.editor.util;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import jp.co.ntt.oss.jboss.byteman.editor.BytemanEditorPlugin;
import jp.co.ntt.oss.jboss.byteman.editor.editor.BytemanRuleEditor;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Color;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.texteditor.ITextEditor;

/**
 * This class provides utility methods for text contents of {@link BytemanRuleEditor}.
 *
 */
public class EditorUtils {

	/**
	 * Returns the active editor part in the workbench.
	 *
	 * @return an instance of the active editor part
	 */
	public static IEditorPart getActiveEditor(){
		IWorkbench workbench = PlatformUI.getWorkbench();
		IWorkbenchWindow window = workbench.getActiveWorkbenchWindow();
		IWorkbenchPage page = window.getActivePage();
		IEditorPart editorPart = page.getActiveEditor();
		return editorPart;
	}

	/**
	 * Returns {@code IFile} of the active editor.
	 * <p>
	 * If no editor is active or the editor input of the active editor is not {@code IFile},
	 * This method returns {@code null}.
	 *
	 * @return {@code IFile} or {@code null} if not found {@code IFile} to return.
	 */
	public static IFile getActiveFile(){
		IEditorInput input = getActiveEditor().getEditorInput();
		if(input instanceof IFileEditorInput){
			return ((IFileEditorInput) input).getFile();
		}
		return null;
	}

	/**
	 * Returns {@code IJavaProject} for the active editor.
	 * <p>
	 * If no editor is active or the editor input of the active editor is not {@code IFile},
	 * This method returns {@code null}.
	 *
	 * @return {@code IJavaProject} or {@code null} if not found {@code IJavaProject} to return.
	 */
	public static IJavaProject getActiveProject(){
		IFile file = getActiveFile();
		if(file != null){
			return JavaCore.create(file.getProject());
		}
		return null;
	}

	/**
	 * Returns contents of the specified file.
	 *
	 * @param file the file to get the lines
	 * @return lines of the contents
	 */
	public static String[] getLines(IFile file) {
		try {
			return getLines(file.getContents(), file.getCharset());
		} catch (CoreException e) {
			BytemanEditorPlugin.logException(e);
			return new String[0];
		}
	}

	/**
	 * Returns the contents of a file specified by the file path.
	 *
	 * @param path the file path
	 * @return lines of the contents
	 */
	public static String[] getLines(String path) {
		try {
			return getLines(new FileInputStream(path), "UTF-8");
		} catch (FileNotFoundException e) {
			BytemanEditorPlugin.logException(e);
			return new String[0];
		}
	}

	private static String[] getLines(InputStream stream, String charset) {
		List<String> lines = new ArrayList<String>();
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new InputStreamReader(stream, charset));
			String line = null;
			while((line = reader.readLine()) != null) {
				lines.add(line);
			}
		} catch (Exception e) {
			BytemanEditorPlugin.logException(e);
		} finally {
			IOUtils.closeQuietly(reader);
		}
		return lines.toArray(new String[lines.size()]);
	}

	/**
	 * Returns a list of rule name which are contained in the specified file.
	 *
	 * @param file the file
	 * @return a list of rule name
	 */
	public static List<String> findRuleNames(IFile file) {
		return findRuleNames(getLines(file));
	}

	/**
	 * Returns a list of rule name which are contained in the file specified by the file path.
	 *
	 * @param path the file path
	 * @return a list of rule name
	 */
	public static List<String> findRuleNames(String path) {
		return findRuleNames(getLines(path));
	}

	private static List<String> findRuleNames(String[] lines) {
		List<String> ruleNames = new ArrayList<String>();
		for(String line : lines) {
			if(line.startsWith("RULE")) {
				ruleNames.add(line.substring("RULE".length()).trim());
			}
		}
		return ruleNames;
	}

	/**
	 * Converts newline characters to LF.
	 * It does nothing if there are only LF as newline characters.
	 *
	 * @param editor the editor
	 */
	public static void convertToLF(ITextEditor editor) {
		IDocument document = editor.getDocumentProvider().getDocument(editor.getEditorInput());
		String text = document.get();
		// finish if CR is not exists.
		if(text.indexOf("\r") == -1) {
			return;
		}
		ITextSelection selection = (ITextSelection) editor.getSelectionProvider().getSelection();
		if(text.indexOf("\r\n") != -1) {
			selection = adjustSelection(document, selection);
		}
		document.set(text.replaceAll("\r\n", "\n").replaceAll("\r", "\n"));
		editor.getSelectionProvider().setSelection(selection);
	}

	/**
	 * Returns text selection of the editor.
	 * If CRLF is contained in editor input, then the offset and the length are adjusted.
	 *
	 * @param document the document object of the editor
	 * @param selection the selection
	 * @return new selection
	 */
	private static ITextSelection adjustSelection(IDocument document, ITextSelection selection) {
		int offset = selection.getOffset();
		int length = selection.getLength();
		try {
			if(length > 0) {
				// adjust length
				String selectedText = document.get(offset, length);
				length = length - (selectedText.split("\r\n").length - 1);
			}
			// adjust offset
			offset = offset - document.get(0, offset).split("\r\n", -1).length + 1;
		} catch (BadLocationException e) {
			BytemanEditorPlugin.logException(e);
		}
		return new TextSelection(offset, length);
	}

	/**
	 * Highlights same words as the selected word.
	 *
	 * @param styledText the text widget of the editor
	 * @param selection the selection
	 * @param document the document
	 * @param color highlight color
	 * @throws BadLocationException
	 */
	public static void highlightWord(
			StyledText styledText, ITextSelection selection, IDocument document, Color color) throws BadLocationException {
		// clear highlight
		for(StyleRange range: styledText.getStyleRanges()){
			if(range.background != null && range.background.equals(color)){
				range.background = null;
				styledText.setStyleRange(range);
			}
		}
		// highlight selected word.
		String text = selection.getText();
		String source = document.get();
		int offset = selection.getOffset();
		int length = selection.getLength();
		if(StringUtils.isEmpty(text)
				|| !isWord(text)
				|| !Character.isJavaIdentifierPart(text.charAt(0))
				|| !Character.isJavaIdentifierPart(text.charAt(text.length() - 1))
				|| offset > 0 && Character.isJavaIdentifierPart(document.getChar(offset - 1))
				|| offset + length < source.length() && Character.isJavaIdentifierPart(document.getChar(offset + length))) {
			return;
		}
		int index = 0;
		int lastIndex = 0;
		while((index = source.indexOf(text, lastIndex)) >= 0) {
			if(index != offset
					&& (index == 0 || !Character.isJavaIdentifierPart(document.getChar(index - 1)))
					&& (index + text.length() == source.length()
							|| !Character.isJavaIdentifierPart(document.getChar(index + text.length())))) {
				StyleRange original = styledText.getStyleRangeAtOffset(index);
				original.length = text.length();
				original.background = color;
				styledText.setStyleRange(original);
			}
			lastIndex = index + 1;
		}
	}

	private static boolean isWord(String target) {
		for(int i = 0; i < target.length(); i++) {
			if(!Character.isJavaIdentifierPart(target.charAt(i))) {
				return false;
			}
		}
		return true;
	}
}
