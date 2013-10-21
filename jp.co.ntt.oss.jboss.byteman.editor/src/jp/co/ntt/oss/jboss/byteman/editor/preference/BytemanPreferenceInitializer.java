package jp.co.ntt.oss.jboss.byteman.editor.preference;

import jp.co.ntt.oss.jboss.byteman.editor.BytemanEditorPlugin;
import jp.co.ntt.oss.jboss.byteman.editor.editor.BytemanRuleEditor;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.StringConverter;
import org.eclipse.swt.graphics.RGB;

/**
 * The default implementation of {@code AbstractPreferenceInitializer} for {@link BytemanRuleEditor}.
 *
 */
public class BytemanPreferenceInitializer extends AbstractPreferenceInitializer {

	public static boolean CLASSCHECK = true;

	@Override
	public void initializeDefaultPreferences() {
		IPreferenceStore store = BytemanEditorPlugin.getDefault().getPreferenceStore();
		store.setDefault(BytemanEditorPlugin.PREF_VALIDATION_CLASS_CHECK, false);
		store.setDefault(BytemanEditorPlugin.PREF_VALIDATION_JAVA_LANG_CHECK, true);
		store.setDefault(BytemanEditorPlugin.PREF_VALIDATION_METHOD_CHECK, true);
		store.setDefault(BytemanEditorPlugin.PREF_DEFAULT_COLOR, StringConverter.asString(new RGB(0, 0, 0)));
		store.setDefault(BytemanEditorPlugin.PREF_DEFAULT_BOLD, false);
		store.setDefault(BytemanEditorPlugin.PREF_DEFAULT_ITALIC, false);
		store.setDefault(BytemanEditorPlugin.PREF_DEFAULT_STRIKE, false);
		store.setDefault(BytemanEditorPlugin.PREF_DEFAULT_UNDERLINE, false);
		store.setDefault(BytemanEditorPlugin.PREF_KEYWORD_COLOR, StringConverter.asString(new RGB(255, 0, 0)));
		store.setDefault(BytemanEditorPlugin.PREF_KEYWORD_BOLD, false);
		store.setDefault(BytemanEditorPlugin.PREF_KEYWORD_ITALIC, false);
		store.setDefault(BytemanEditorPlugin.PREF_KEYWORD_STRIKE, false);
		store.setDefault(BytemanEditorPlugin.PREF_KEYWORD_UNDERLINE, false);
		store.setDefault(BytemanEditorPlugin.PREF_ASSIST_JAVA_LANG_PACKAGE, false);
	}

}
