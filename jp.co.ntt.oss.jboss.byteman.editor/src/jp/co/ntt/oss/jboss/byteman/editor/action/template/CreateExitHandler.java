package jp.co.ntt.oss.jboss.byteman.editor.action.template;

/**
 * Handler class for creating the rule template with the location identifier "AT EXIT" to the clipboard.
 *
 */
public class CreateExitHandler extends AbstractTemplateHandler {

	public CreateExitHandler() {
		super(Location.EXIT, true);
	}

}
