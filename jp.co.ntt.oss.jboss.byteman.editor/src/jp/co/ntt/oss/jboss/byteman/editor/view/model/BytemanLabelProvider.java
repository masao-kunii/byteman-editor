package jp.co.ntt.oss.jboss.byteman.editor.view.model;


import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.swt.graphics.Image;

/**
 * The default implementation of the label provider for this plug-in's view.
 *
 */
public class BytemanLabelProvider extends ColumnLabelProvider {

	@Override
	public String getText(Object element) {
		if(element instanceof Model) {
			return ((Model) element).getText();
		}
		return super.getText(element);
	}

	@Override
	public Image getImage(Object element) {
		if(element instanceof Model) {
			return ((Model) element).getImage();
		}
		return super.getImage(element);
	}
}
