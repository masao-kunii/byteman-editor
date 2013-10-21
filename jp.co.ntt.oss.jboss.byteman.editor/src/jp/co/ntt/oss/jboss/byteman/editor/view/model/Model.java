package jp.co.ntt.oss.jboss.byteman.editor.view.model;

import org.eclipse.swt.graphics.Image;

/**
 * Interface of model objects for this plug-in's view.
 *
 */
public interface Model {

	/**
	 * Returns the text to display.
	 * @return the text to display
	 */
	public String getText();

	/**
	 * Returns the image object of icon.
	 * @return the image object of icon
	 */
	public Image getImage();

	/**
	 * Returns child objects.
	 * @return the child objects
	 */
	public Object[] getChildren();

}
