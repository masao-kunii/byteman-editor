package jp.co.ntt.oss.jboss.byteman.editor.action.template;

/**
 * Handler class for creating the rule template with the location identifier "AT ENTRY" to the clipboard.
 *
 */
public class CreateEntryHandler extends AbstractTemplateHandler {

	public CreateEntryHandler() {
		super(Location.ENTRY, true);
	}

}
