package jp.co.ntt.oss.jboss.byteman.editor.preference;

import java.util.ArrayList;

import jp.co.ntt.oss.jboss.byteman.editor.BytemanEditorPlugin;
import jp.co.ntt.oss.jboss.byteman.editor.editor.BytemanRuleEditor;
import jp.co.ntt.oss.jboss.byteman.editor.util.UIUtils;

import org.eclipse.jface.preference.ColorSelector;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.resource.StringConverter;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.List;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

/**
 * Preference page of {@link BytemanRuleEditor}.
 *
 */
public class BytemanPreferencePage extends PreferencePage implements IWorkbenchPreferencePage {

	private Button validateClassCheck;
	private Button validateJavaLangCheck;
	private Button validateMethodCheck;
	private Button javalangCheck;

	private List styleList;
	private ColorSelector colorSelector;
	private Button boldCheck;
	private Button italicCheck;
	private Button strikeCheck;
	private Button underlineCheck;

	private java.util.List<StyleInfo> styles = new ArrayList<StyleInfo>();

	private class StyleInfo {

		public RGB color;
		public boolean bold;
		public boolean italic;
		public boolean strike;
		public boolean underline;

		public String colorKey;
		public String boldKey;
		public String italicKey;
		public String strikeKey;
		public String underlineKey;

		/**
		 * Loads from the preference store.
		 */
		public void load(){
			IPreferenceStore store = getPreferenceStore();

			this.color = StringConverter.asRGB(store.getString(colorKey));
			this.bold = store.getBoolean(boldKey);
			this.italic = store.getBoolean(italicKey);
			this.strike = store.getBoolean(strikeKey);			this.underline = store.getBoolean(underlineKey);
		}

		/**
		 * Saves to the preference store.
		 */
		public void save(){
			IPreferenceStore store = getPreferenceStore();

			store.setValue(colorKey, StringConverter.asString(color));
			store.setValue(boldKey, bold);
			store.setValue(italicKey, italic);
			store.setValue(strikeKey, strike);
			store.setValue(underlineKey, underline);
		}

		/**
		 * Loads the default values from the preference store.
		 */
		public void loadDefaults(){
			IPreferenceStore store = getPreferenceStore();

			this.color = StringConverter.asRGB(store.getDefaultString(colorKey));
			this.bold = store.getDefaultBoolean(boldKey);
			this.italic = store.getDefaultBoolean(italicKey);
			this.strike = store.getDefaultBoolean(strikeKey);
			this.underline = store.getDefaultBoolean(underlineKey);
		}
	}

	@Override
	public void init(IWorkbench workbench) {
		IPreferenceStore store = BytemanEditorPlugin.getDefault().getPreferenceStore();
		setPreferenceStore(store);

		{
			StyleInfo styleInfo = new StyleInfo();
			styleInfo.colorKey = BytemanEditorPlugin.PREF_DEFAULT_COLOR;
			styleInfo.boldKey = BytemanEditorPlugin.PREF_DEFAULT_BOLD;
			styleInfo.italicKey = BytemanEditorPlugin.PREF_DEFAULT_ITALIC;
			styleInfo.strikeKey = BytemanEditorPlugin.PREF_DEFAULT_STRIKE;
			styleInfo.underlineKey = BytemanEditorPlugin.PREF_DEFAULT_UNDERLINE;
			styleInfo.load();
			styles.add(styleInfo);
		}
		{
			StyleInfo styleInfo = new StyleInfo();
			styleInfo.colorKey = BytemanEditorPlugin.PREF_KEYWORD_COLOR;
			styleInfo.boldKey = BytemanEditorPlugin.PREF_KEYWORD_BOLD;
			styleInfo.italicKey = BytemanEditorPlugin.PREF_KEYWORD_ITALIC;
			styleInfo.strikeKey = BytemanEditorPlugin.PREF_KEYWORD_STRIKE;
			styleInfo.underlineKey = BytemanEditorPlugin.PREF_KEYWORD_UNDERLINE;
			styleInfo.load();
			styles.add(styleInfo);
		}
	}

	@Override
	protected Control createContents(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);

		GridLayout layout = new GridLayout(1, true);
		composite.setLayout(layout);

		UIUtils.createLabel(composite, BytemanEditorPlugin.getResourceString("preference.summary"));

		// Validation group
		Group validationGroup = new Group(composite, SWT.NULL);
		validationGroup.setText(BytemanEditorPlugin.getResourceString("preference.label.validate"));
		validationGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		validationGroup.setLayout(new GridLayout(1, false));

		validateClassCheck = UIUtils.createCheckbox(validationGroup, BytemanEditorPlugin.getResourceString("preference.label.validate.class"));
		validateClassCheck.setSelection(getPreferenceStore().getBoolean(BytemanEditorPlugin.PREF_VALIDATION_CLASS_CHECK));
		validateClassCheck.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				setEnableForValidateButtons();
			}
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				setEnableForValidateButtons();
			}
		});
		GridData gridData = new GridData();
		gridData.horizontalIndent += 15;
		validateJavaLangCheck = UIUtils.createCheckbox(validationGroup, BytemanEditorPlugin.getResourceString("preference.label.validate.javalang"));
		validateJavaLangCheck.setSelection(getPreferenceStore().getBoolean(BytemanEditorPlugin.PREF_VALIDATION_JAVA_LANG_CHECK));
		validateJavaLangCheck.setLayoutData(gridData);
		validateMethodCheck = UIUtils.createCheckbox(validationGroup, BytemanEditorPlugin.getResourceString("preference.label.validate.method"));
		validateMethodCheck.setSelection(getPreferenceStore().getBoolean(BytemanEditorPlugin.PREF_VALIDATION_METHOD_CHECK));
		validateMethodCheck.setLayoutData(gridData);

		// Appearance group
		Group appearanceGroup = new Group(composite, SWT.NULL);
		appearanceGroup.setText(BytemanEditorPlugin.getResourceString("preference.label.style"));
		appearanceGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		appearanceGroup.setLayout(new GridLayout(2, false));

		styleList = new List(appearanceGroup, SWT.H_SCROLL | SWT.BORDER);
		styleList.add(BytemanEditorPlugin.getResourceString("preference.style.default"));
		styleList.add(BytemanEditorPlugin.getResourceString("preference.style.keyword"));

		GridData gd = new GridData();
		gd.widthHint = 200;
		gd.verticalAlignment = SWT.TOP;
		styleList.setLayoutData(gd);

		styleList.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				updateStatus();
			}
		});

		Composite stylePanel = new Composite(appearanceGroup, SWT.NULL);
		stylePanel.setLayout(new GridLayout(2, false));

		UIUtils.createLabel(stylePanel, BytemanEditorPlugin.getResourceString("preference.style.attr.color"));
		colorSelector = new ColorSelector(stylePanel);
		colorSelector.addListener(new IPropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent event) {
				getSelectedStyleInfo().color = colorSelector.getColorValue();
			}
		});

		boldCheck = UIUtils.createCheckbox(stylePanel, BytemanEditorPlugin.getResourceString("preference.style.attr.bold"));
		boldCheck.setLayoutData(UIUtils.createGridData(2));
		boldCheck.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				getSelectedStyleInfo().bold = boldCheck.getSelection();
			}
		});

		italicCheck = UIUtils.createCheckbox(stylePanel, BytemanEditorPlugin.getResourceString("preference.style.attr.italic"));
		italicCheck.setLayoutData(UIUtils.createGridData(2));
		italicCheck.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				getSelectedStyleInfo().italic = italicCheck.getSelection();
			}
		});

		strikeCheck = UIUtils.createCheckbox(stylePanel, BytemanEditorPlugin.getResourceString("preference.style.attr.strikethrough"));
		strikeCheck.setLayoutData(UIUtils.createGridData(2));
		strikeCheck.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				getSelectedStyleInfo().strike = strikeCheck.getSelection();
			}
		});

		underlineCheck = UIUtils.createCheckbox(stylePanel, BytemanEditorPlugin.getResourceString("preference.style.attr.underline"));
		underlineCheck.setLayoutData(UIUtils.createGridData(2));
		underlineCheck.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				getSelectedStyleInfo().underline = underlineCheck.getSelection();
			}
		});

		// CodeAssist Group
		Group contentAssistGroup = new Group(composite, SWT.NULL);
		contentAssistGroup.setText(BytemanEditorPlugin.getResourceString("preference.label.assist"));
		contentAssistGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		contentAssistGroup.setLayout(new FillLayout());

		javalangCheck = UIUtils.createCheckbox(contentAssistGroup, BytemanEditorPlugin.getResourceString("preference.label.assist.javalang"));
		javalangCheck.setSelection(getPreferenceStore().getBoolean(BytemanEditorPlugin.PREF_ASSIST_JAVA_LANG_PACKAGE));

		updateStatus();

		return composite;
	}

	private StyleInfo getSelectedStyleInfo(){
		int index = styleList.getSelectionIndex();
		if(index < 0){
			return null;
		} else {
			return styles.get(index);
		}
	}

	private void updateStatus(){
		StyleInfo styleInfo = getSelectedStyleInfo();

		if(styleInfo == null){
			colorSelector.setEnabled(false);
			boldCheck.setEnabled(false);
			italicCheck.setEnabled(false);
			strikeCheck.setEnabled(false);
			underlineCheck.setEnabled(false);

		} else {
			colorSelector.setColorValue(styleInfo.color);
			boldCheck.setSelection(styleInfo.bold);
			italicCheck.setSelection(styleInfo.italic);
			strikeCheck.setSelection(styleInfo.strike);
			underlineCheck.setSelection(styleInfo.underline);

			colorSelector.setEnabled(true);
			boldCheck.setEnabled(true);
			italicCheck.setEnabled(true);
			strikeCheck.setEnabled(true);
			underlineCheck.setEnabled(true);
		}
		setEnableForValidateButtons();
	}

	private void setEnableForValidateButtons() {
		if(validateClassCheck.getSelection()) {
			validateJavaLangCheck.setEnabled(true);
			validateMethodCheck.setEnabled(true);
		} else {
			validateJavaLangCheck.setEnabled(false);
			validateMethodCheck.setEnabled(false);
		}
	}

	@Override
	public boolean performOk() {
		for(StyleInfo styleInfo: styles){
			styleInfo.save();
		}
		getPreferenceStore().setValue(BytemanEditorPlugin.PREF_ASSIST_JAVA_LANG_PACKAGE, javalangCheck.getSelection());
		getPreferenceStore().setValue(BytemanEditorPlugin.PREF_VALIDATION_CLASS_CHECK, validateClassCheck.getSelection());
		getPreferenceStore().setValue(BytemanEditorPlugin.PREF_VALIDATION_JAVA_LANG_CHECK, validateJavaLangCheck.getSelection());
		getPreferenceStore().setValue(BytemanEditorPlugin.PREF_VALIDATION_METHOD_CHECK, validateMethodCheck.getSelection());
		return true;
	}

	public void performDefaults() {
		for(StyleInfo styleInfo: styles){
			styleInfo.loadDefaults();
		}
		javalangCheck.setSelection(getPreferenceStore().getDefaultBoolean(BytemanEditorPlugin.PREF_ASSIST_JAVA_LANG_PACKAGE));
		validateClassCheck.setSelection(getPreferenceStore().getDefaultBoolean(BytemanEditorPlugin.PREF_VALIDATION_CLASS_CHECK));
		validateJavaLangCheck.setSelection(getPreferenceStore().getDefaultBoolean(BytemanEditorPlugin.PREF_VALIDATION_JAVA_LANG_CHECK));
		validateMethodCheck.setSelection(getPreferenceStore().getDefaultBoolean(BytemanEditorPlugin.PREF_VALIDATION_METHOD_CHECK));
		updateStatus();
	}

	public boolean performCancel() {
		return true;
	}
}
