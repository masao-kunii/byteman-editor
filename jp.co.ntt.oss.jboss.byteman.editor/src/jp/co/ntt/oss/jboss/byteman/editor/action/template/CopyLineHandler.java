package jp.co.ntt.oss.jboss.byteman.editor.action.template;

/**
 * Handler class for copying the rule template with the location identifier "AT LINE" to the clipboard.
 *
 */
public class CopyLineHandler extends AbstractTemplateHandler {

	public CopyLineHandler() {
		super(Location.LINE, false);
	}

}
