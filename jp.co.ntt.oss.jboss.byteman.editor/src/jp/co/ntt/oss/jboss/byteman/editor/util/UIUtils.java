package jp.co.ntt.oss.jboss.byteman.editor.util;

import jp.co.ntt.oss.jboss.byteman.editor.BytemanEditorPlugin;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;

/**
 * Provides utility methods for UI components.
 *
 */
public class UIUtils {

	/**
	 * Creates a label control with the given text.
	 *
	 * @param parent the parent control
	 * @param text the label text
	 * @return the created label
	 */
	public static Label createLabel(Composite parent, String text){
		Label label = new Label(parent, SWT.NULL);
		label.setText(text);
		return label;
	}

	/**
	 * Creates a {@code GridData} with the given horizontalSpan.
	 *
	 * @param horizontalSpan the horizontal span
	 * @return the created {@code GridData}
	 */
	public static GridData createGridData(int horizontalSpan){
		GridData gd = new GridData();
		gd.horizontalSpan = horizontalSpan;
		return gd;
	}

	/**
	 * Creates a checkbox control with the given text.
	 *
	 * @param parent the parent control
	 * @param text the label text
	 * @return the created checkbox
	 */
	public static Button createCheckbox(Composite parent, String text){
		Button button = new Button(parent, SWT.CHECK);
		button.setText(text);
		return button;
	}

	/**
	 * Opens a {@code JavaEditor} with the given element.
	 *
	 * @param element a target element
	 */
	public static void openInEditor(IJavaElement element) {
		if(element != null) {
			try {
				JavaUI.openInEditor(element);
			} catch (Exception e) {
				BytemanEditorPlugin.logException(e);
			}
		}
	}

	/**
	 * Copies the given content to a clipboard.
	 *
	 * @param content content to copy
	 */
	public static void copyToClipbord(String content) {
		Clipboard clipboard = new Clipboard(Display.getDefault());
		clipboard.setContents(new Object[]{content}, new Transfer[]{TextTransfer.getInstance()});
	}

	/**
	 * Displays an error dialog.
	 *
	 * @param title the title
	 * @param message the message to display
	 */
	public static void displayErrorDialog(String title, String message) {
		MessageDialog.openError(BytemanEditorPlugin.getDefault().getWorkbench().getDisplay().getActiveShell(), title, message);
	}
}
