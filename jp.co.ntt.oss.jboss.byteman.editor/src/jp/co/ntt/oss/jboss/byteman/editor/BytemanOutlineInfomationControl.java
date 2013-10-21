package jp.co.ntt.oss.jboss.byteman.editor;

import jp.co.ntt.oss.jboss.byteman.editor.util.EditorUtils;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.internal.ui.text.JavaOutlineInformationControl;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 * Show rule outlines.
 */
@SuppressWarnings("restriction")
public class BytemanOutlineInfomationControl extends JavaOutlineInformationControl {

	private Text filterText;

	public BytemanOutlineInfomationControl(Shell parent) {
		super(parent, SWT.NONE, SWT.SINGLE | SWT.VERTICAL, "jp.co.ntt.oss.jboss.byteman.editor.action.jump.JumpToTargetCommand");
		super.setInfoText("Jump to selected method.");
		super.setSize(400, 300);
	}

	@Override
	protected TreeViewer createTreeViewer(Composite composite, int style) {
		TreeViewer treeViewer = super.createTreeViewer(composite, style);
		return treeViewer;
	}

	@Override
	protected String getId() {
		return "jp.co.ntt.oss.jboss.byteman.editor.action.JumpToTargetCommand";
	}

	protected Text createFilterText(Composite composite) {
		filterText = super.createFilterText(composite);
		filterText.setEditable(false);
		return filterText;
	}

	protected Text getFilterText() {
		return filterText;
	}

	public void setFilterText(String filterString) {
		filterText.setText(filterString);
	}

	public static void open(IJavaElement element, String filterString) {
		Shell parent = EditorUtils.getActiveEditor().getSite().getShell();
		BytemanOutlineInfomationControl control = new BytemanOutlineInfomationControl(parent);
		control.setInput(element);
		control.setFilterText(filterString);
		control.setVisible(true);
	}

}
