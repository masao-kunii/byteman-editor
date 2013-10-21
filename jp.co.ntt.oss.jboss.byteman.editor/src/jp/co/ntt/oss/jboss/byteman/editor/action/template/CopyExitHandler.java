package jp.co.ntt.oss.jboss.byteman.editor.action.template;

/**
 * Handler class for copying the rule template with the location identifier "AT EXIT" to the clipboard.
 *
 */
public class CopyExitHandler extends AbstractTemplateHandler {

	public CopyExitHandler() {
		super(Location.EXIT, false);
	}

}
