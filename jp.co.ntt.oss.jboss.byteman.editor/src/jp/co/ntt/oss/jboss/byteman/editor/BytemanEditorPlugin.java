package jp.co.ntt.oss.jboss.byteman.editor;

import static jp.co.ntt.oss.jboss.byteman.editor.BytemanEditorPluginImages.*;

import java.util.ResourceBundle;

import jp.co.ntt.oss.jboss.byteman.editor.BytemanEditorPluginImages.BytemanErrorIconImageDescriptor;
import jp.co.ntt.oss.jboss.byteman.editor.BytemanEditorPluginImages.BytemanWarningIconImageDescriptor;
import jp.co.ntt.oss.jboss.byteman.editor.BytemanEditorPluginImages.ClassErrorImageDescriptor;
import jp.co.ntt.oss.jboss.byteman.editor.BytemanEditorPluginImages.ClassWarningImageDescriptor;
import jp.co.ntt.oss.jboss.byteman.editor.BytemanEditorPluginImages.InterfaceErrorImageDescriptor;
import jp.co.ntt.oss.jboss.byteman.editor.BytemanEditorPluginImages.InterfaceWarningImageDescriptor;
import jp.co.ntt.oss.jboss.byteman.editor.BytemanEditorPluginImages.LocationErrorImageDescriptor;
import jp.co.ntt.oss.jboss.byteman.editor.BytemanEditorPluginImages.LocationWarningImageDescriptor;
import jp.co.ntt.oss.jboss.byteman.editor.BytemanEditorPluginImages.MethodErrorImageDescriptor;
import jp.co.ntt.oss.jboss.byteman.editor.BytemanEditorPluginImages.MethodWarningImageDescriptor;
import jp.co.ntt.oss.jboss.byteman.editor.BytemanEditorPluginImages.RuleErrorImageDescriptor;
import jp.co.ntt.oss.jboss.byteman.editor.BytemanEditorPluginImages.RuleWarningImageDescriptor;
import jp.co.ntt.oss.jboss.byteman.editor.BytemanEditorPluginImages.ServerConnectedImageDescriptor;
import jp.co.ntt.oss.jboss.byteman.editor.BytemanEditorPluginImages.ServerDisconnectedImageDescriptor;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class of the Byteman editor plug-in.
 */
public class BytemanEditorPlugin extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "jp.co.ntt.oss.jboss.byteman.editor"; //$NON-NLS-1$

	// The marker ID
	public static final String MARKER_ID = PLUGIN_ID + ".BytemanRuleMarker";

	// Context menu ID
	public static final String CONTEXT_MENU_ID = "#BytemanRuleEditorContext";

	// Nature ID
	public static final String NATURE_ID = PLUGIN_ID + ".BytemanEditorPluginNature";

	// Builder ID
	public static final String BUILDER_ID = PLUGIN_ID + ".BytemanEditorPluginBuilder";

	// Image icon relative path
	private static final String ICON_RULE = "icons/rule.gif";
	private static final String ICON_SERVER = "icons/server.gif";
	private static final String ICON_REFRESH = "icons/refresh.gif";
	private static final String ICON_LOCATION = "icons/location.gif";
	private static final String ICON_OVL_STOP = "icons/ovr_stop.gif";
	private static final String ICON_BYTEMAN = "icons/byteman.gif";

	// Preference key
	public static final String PREF_KEYWORD_COLOR = "pref_byteman_keyword_color";
	public static final String PREF_KEYWORD_BOLD = "pref_byteman_keyword_bold";
	public static final String PREF_KEYWORD_ITALIC = "pref_byteman_keyword_italic";
	public static final String PREF_KEYWORD_STRIKE = "pref_byteman_keyword_strike";
	public static final String PREF_KEYWORD_UNDERLINE = "pref_byteman_keyword_underline";
	public static final String PREF_DEFAULT_COLOR = "pref_byteman_default_color";
	public static final String PREF_DEFAULT_BOLD = "pref_byteman_default_bold";
	public static final String PREF_DEFAULT_ITALIC = "pref_byteman_default_italic";
	public static final String PREF_DEFAULT_STRIKE = "pref_byteman_default_strike";
	public static final String PREF_DEFAULT_UNDERLINE = "pref_byteman_default_underline";
	public static final String PREF_AGENTS = "pref_byteman_agents";
	public static final String PREF_ASSIST_JAVA_LANG_PACKAGE = "pref_byteman_assist_java_lang_package";
	public static final String PREF_VALIDATION_CLASS_CHECK = "pref_byteman_validation_class_check";
	public static final String PREF_VALIDATION_JAVA_LANG_CHECK = "pref_byteman_validation_java_lang_check";
	public static final String PREF_VALIDATION_METHOD_CHECK = "pref_byteman_validation_method_check";

	// Keywords
	public static final String[] RULE_KEYWORDS = {
		"RULE", "CLASS", "INTERFACE", "METHOD", "HELPER"
	};

	public static final String[] RULE_EXPRESSION_KEYWORDS = {
		"AT", "AFTER", "ENTRY", "EXIT", "LINE", "READ", "WRITE", "INVOKE", "SYNCHRONIZE", "THROW",
		"BIND", "IF", "DO", "ENDRULE", "AND", "OR", "TRUE", "NOT"
	};

	public static final String[] JAVA_KEYWORDS = {
		"new", "null", "return", "throw", "true", "false"
	};

	// The shared instance
	private static BytemanEditorPlugin plugin;

	protected static ResourceBundle resourceBundle = ResourceBundle.getBundle("jp.co.ntt.oss.jboss.byteman.editor.BytemanEditorPlugin");

	protected static BytemanEditorPluginLogger logger = new BytemanEditorPluginLoggerImpl();

	/**
	 * The constructor
	 */
	public BytemanEditorPlugin() {
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	@Override
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	@Override
	protected void initializeImageRegistry(ImageRegistry registry) {
		super.initializeImageRegistry(registry);
		registry.put(KEY_IMG_RULE, createImageDescriptor(ICON_RULE));
		registry.put(KEY_IMG_SERVER, createImageDescriptor(ICON_SERVER));
		registry.put(KEY_IMG_SERVER_CONNECTED, new ServerConnectedImageDescriptor());
		registry.put(KEY_IMG_SERVER_DISCONNECTED, new ServerDisconnectedImageDescriptor());
		registry.put(KEY_IMG_REFRESH, createImageDescriptor(ICON_REFRESH));
		registry.put(KEY_IMG_LOCATION, createImageDescriptor(ICON_LOCATION));
		registry.put(KEY_IMG_OVR_STOP, createImageDescriptor(ICON_OVL_STOP));
		registry.put(KEY_IMG_BYTEMAN, createImageDescriptor(ICON_BYTEMAN));
		registry.put(KEY_IMG_BYTEMAN_ERROR, new BytemanErrorIconImageDescriptor());
		registry.put(KEY_IMG_BYTEMAN_WARNING, new BytemanWarningIconImageDescriptor());
		registry.put(KEY_IMG_RULE_ERROR, new RuleErrorImageDescriptor());
		registry.put(KEY_IMG_RULE_WARNING, new RuleWarningImageDescriptor());
		registry.put(KEY_IMG_CLASS_ERROR, new ClassErrorImageDescriptor());
		registry.put(KEY_IMG_CLASS_WARNING, new ClassWarningImageDescriptor());
		registry.put(KEY_IMG_INTERFACE_ERROR, new InterfaceErrorImageDescriptor());
		registry.put(KEY_IMG_INTERFACE_WARNING, new InterfaceWarningImageDescriptor());
		registry.put(KEY_IMG_METHOD_ERROR, new MethodErrorImageDescriptor());
		registry.put(KEY_IMG_METHOD_WARNING, new MethodWarningImageDescriptor());
		registry.put(KEY_IMG_LOCATION_ERROR, new LocationErrorImageDescriptor());
		registry.put(KEY_IMG_LOCATION_WARNING, new LocationWarningImageDescriptor());
	}

	/**
	 * Returns an image descriptor for the image file specified by
	 * a plug-in relative path.
	 *
	 * @param path relative path to the file
	 * @return the image descriptor
	 */
	private static ImageDescriptor createImageDescriptor(String path) {
		return imageDescriptorFromPlugin(PLUGIN_ID, path);
	}

	/**
	 * Returns the shared instance.
	 *
	 * @return the shared instance
	 */
	public static BytemanEditorPlugin getDefault() {
		return plugin;
	}

	/**
	 * Returns an image descriptor for the image file specified by a key.
	 *
	 * @param key key of the image file
	 * @return the image descriptor
	 */
	public static ImageDescriptor getImageDescriptor(String key) {
		return getDefault().getImageRegistry().getDescriptor(key);
	}

	public static Image getImage(String key) {
		return getDefault().getImageRegistry().get(key);
	}

	public static String getResourceString(String key) {
		return resourceBundle.getString(key);
	}

	public static String getResourceString(String key, String... words) {
		String result = getResourceString(key);
		if(words.length == 0) {
			return result;
		}
		for(int i = 0; i < words.length; i++) {
			result = result.replace(String.format("{%d}", i), words[i]);
		}
		return result;
	}

	/**
	 * Outputs an exception to a log file.
	 *
	 * @param cause an exception to log.
	 */
	public static void logException(Throwable cause) {
		logger.log(new Status(IStatus.ERROR, PLUGIN_ID, "Error", cause));
	}

	private static class BytemanEditorPluginLoggerImpl implements BytemanEditorPluginLogger {
		@Override
		public void log(IStatus status) {
			getDefault().getLog().log(status);
		}
	}
}
