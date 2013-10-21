package jp.co.ntt.oss.jboss.byteman.editor.action.template;

/**
 * Handler class for copying the rule template with the location identifier "AT ENTRY" to the clipboard.
 *
 */
public class CopyEntryHandler extends AbstractTemplateHandler {

	public CopyEntryHandler() {
		super(Location.ENTRY, false);
	}

}
