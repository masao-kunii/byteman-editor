package jp.co.ntt.oss.jboss.byteman.editor.action.template;

/**
 * Handler class for creating the rule template with the location identifier "AT LINE" to the clipboard.
 *
 */
public class CreateLineHandler extends AbstractTemplateHandler {

	public CreateLineHandler() {
		super(Location.LINE, true);
	}

}
