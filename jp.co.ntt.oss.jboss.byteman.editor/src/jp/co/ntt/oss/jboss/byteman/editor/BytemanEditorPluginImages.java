package jp.co.ntt.oss.jboss.byteman.editor;

import static jp.co.ntt.oss.jboss.byteman.editor.BytemanEditorPlugin.getImage;
import static jp.co.ntt.oss.jboss.byteman.editor.BytemanEditorPlugin.getImageDescriptor;

import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.internal.ui.JavaPluginImages;
import org.eclipse.jdt.internal.ui.viewsupport.JavaElementImageProvider;
import org.eclipse.jdt.ui.JavaElementImageDescriptor;
import org.eclipse.jface.resource.CompositeImageDescriptor;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.ui.internal.ide.IDEWorkbenchPlugin;

/**
 * This class provides images used in this plug-in.
 *
 */
@SuppressWarnings("restriction")
public class BytemanEditorPluginImages {

	// image keys
	static final String KEY_IMG_RULE = "img_rule";
	static final String KEY_IMG_RULE_ERROR = "img_rule_error";
	static final String KEY_IMG_RULE_WARNING = "img_rule_warning";
	static final String KEY_IMG_BYTEMAN = "img_byteman";
	static final String KEY_IMG_BYTEMAN_ERROR = "img_byteman_error";
	static final String KEY_IMG_BYTEMAN_WARNING = "img_byteman_warning";
	static final String KEY_IMG_SERVER = "img_server";
	static final String KEY_IMG_SERVER_CONNECTED = "img_server_connected";
	static final String KEY_IMG_SERVER_DISCONNECTED = "img_server_disconnected";
	static final String KEY_IMG_OVR_STOP = "img_ovr_stop";
	static final String KEY_IMG_REFRESH = "img_refresh";
	static final String KEY_IMG_CLASS_ERROR = "img_class_error";
	static final String KEY_IMG_CLASS_WARNING = "img_class_warning";
	static final String KEY_IMG_INTERFACE_ERROR = "img_interface_error";
	static final String KEY_IMG_INTERFACE_WARNING = "img_interface_warning";
	static final String KEY_IMG_METHOD_ERROR = "img_method_error";
	static final String KEY_IMG_METHOD_WARNING = "img_method_warning";
	static final String KEY_IMG_LOCATION = "img_location";
	static final String KEY_IMG_LOCATION_ERROR = "img_location_error";
	static final String KEY_IMG_LOCATION_WARNING = "img_location_warning";

	// image descriptor
	public static ImageDescriptor DESC_REFRESH = getImageDescriptor(KEY_IMG_REFRESH);
	public static ImageDescriptor DESC_NEWFILE_WIZARD = IDEWorkbenchPlugin.getIDEImageDescriptor("wizban/newfile_wiz.png");

	// plugin image
	public static Image IMG_RULE = getImage(KEY_IMG_RULE);
	public static Image IMG_RULE_WARNING = getImage(KEY_IMG_RULE_WARNING);
	public static Image IMG_RULE_ERROR = getImage(KEY_IMG_RULE_ERROR);
	public static Image IMG_BYTEMAN = getImage(KEY_IMG_BYTEMAN);
	public static Image IMG_BYTEMAN_WARNING = getImage(KEY_IMG_BYTEMAN_WARNING);
	public static Image IMG_BYTEMAN_ERROR = getImage(KEY_IMG_BYTEMAN_ERROR);
	public static Image IMG_LOCATION = getImage(KEY_IMG_LOCATION);
	public static Image IMG_LOCATION_WARNING = getImage(KEY_IMG_LOCATION_WARNING);
	public static Image IMG_LOCATION_ERROR = getImage(KEY_IMG_LOCATION_ERROR);
	public static Image IMG_SERVER_CONNECTED = getImage(KEY_IMG_SERVER_CONNECTED);
	public static Image IMG_SERVER_DISCONNECTED = getImage(KEY_IMG_SERVER_DISCONNECTED);

	// java image
	public static Image IMG_STATIC_INITIALIZER = new JavaElementImageDescriptor(JavaPluginImages.DESC_MISC_PRIVATE, JavaElementImageDescriptor.STATIC, JavaElementImageProvider.SMALL_SIZE).createImage();
	public static Image IMG_CLASS = JavaPlugin.getImageDescriptorRegistry().get(JavaPluginImages.DESC_OBJS_CLASS);
	public static Image IMG_CLASS_WARNING = getImage(KEY_IMG_CLASS_WARNING);
	public static Image IMG_CLASS_ERROR = getImage(KEY_IMG_CLASS_ERROR);
	public static Image IMG_INTERFACE = JavaPlugin.getImageDescriptorRegistry().get(JavaPluginImages.DESC_OBJS_INTERFACE);
	public static Image IMG_INTERFACE_WARNING = getImage(KEY_IMG_INTERFACE_WARNING);
	public static Image IMG_INTERFACE_ERROR = getImage(KEY_IMG_INTERFACE_ERROR);
	public static Image IMG_OBJS_TEMPLATE = JavaPlugin.getImageDescriptorRegistry().get(JavaPluginImages.DESC_OBJS_TEMPLATE);
	public static Image IMG_METHOD = JavaPlugin.getImageDescriptorRegistry().get(JavaPluginImages.DESC_MISC_PUBLIC);
	public static Image IMG_METHOD_WARNING = getImage(KEY_IMG_METHOD_WARNING);
	public static Image IMG_METHOD_ERROR = getImage(KEY_IMG_METHOD_ERROR);
	public static Image IMG_FIELD_PRIVATE = JavaPlugin.getImageDescriptorRegistry().get(JavaPluginImages.DESC_FIELD_PRIVATE);

	private static abstract class BaseCompositeImageDescriptor extends CompositeImageDescriptor {
		@Override protected Point getSize() {
			return new Point(16, 16);
		}
	}

	static class ServerConnectedImageDescriptor extends BaseCompositeImageDescriptor {
		@Override protected void drawCompositeImage(int width, int height) {
			drawImage(getImage(KEY_IMG_SERVER).getImageData(), 0, 0);
			drawImage(JavaPluginImages.DESC_OVR_RUN.getImageData(), 8, 8);
		}
	}

	static class ServerDisconnectedImageDescriptor extends BaseCompositeImageDescriptor {
		@Override protected void drawCompositeImage(int width, int height) {
			drawImage(getImage(KEY_IMG_SERVER).getImageData(), 0, 0);
			drawImage(getImage(KEY_IMG_OVR_STOP).getImageData(), 8, 8);
		}
	}

	static class BytemanErrorIconImageDescriptor extends BaseCompositeImageDescriptor {
		@Override protected void drawCompositeImage(int width, int height) {
			drawImage(IMG_BYTEMAN.getImageData(), 0, 0);
			drawImage(JavaPluginImages.DESC_OVR_ERROR.getImageData(), 0, 8);
		}
	}

	static class BytemanWarningIconImageDescriptor extends BaseCompositeImageDescriptor {
		@Override protected void drawCompositeImage(int width, int height) {
			drawImage(IMG_BYTEMAN.getImageData(), 0, 0);
			drawImage(JavaPluginImages.DESC_OVR_WARNING.getImageData(), 0, 8);
		}
	}

	static class RuleErrorImageDescriptor extends BaseCompositeImageDescriptor {
		@Override protected void drawCompositeImage(int width, int height) {
			drawImage(IMG_RULE.getImageData(), 0, 0);
			drawImage(JavaPluginImages.DESC_OVR_ERROR.getImageData(), 0, 8);
		}
	}

	static class RuleWarningImageDescriptor extends BaseCompositeImageDescriptor {
		@Override protected void drawCompositeImage(int width, int height) {
			drawImage(IMG_RULE.getImageData(), 0, 0);
			drawImage(JavaPluginImages.DESC_OVR_WARNING.getImageData(), 0, 8);
		}
	}

	static class ClassErrorImageDescriptor extends BaseCompositeImageDescriptor {
		@Override protected void drawCompositeImage(int width, int height) {
			drawImage(IMG_CLASS.getImageData(), 0, 0);
			drawImage(JavaPluginImages.DESC_OVR_ERROR.getImageData(), 0, 8);
		}
	}

	static class ClassWarningImageDescriptor extends BaseCompositeImageDescriptor {
		@Override protected void drawCompositeImage(int width, int height) {
			drawImage(IMG_CLASS.getImageData(), 0, 0);
			drawImage(JavaPluginImages.DESC_OVR_WARNING.getImageData(), 0, 8);
		}
	}

	static class InterfaceErrorImageDescriptor extends BaseCompositeImageDescriptor {
		@Override protected void drawCompositeImage(int width, int height) {
			drawImage(IMG_INTERFACE.getImageData(), 0, 0);
			drawImage(JavaPluginImages.DESC_OVR_ERROR.getImageData(), 0, 8);
		}
	}

	static class InterfaceWarningImageDescriptor extends BaseCompositeImageDescriptor {
		@Override protected void drawCompositeImage(int width, int height) {
			drawImage(IMG_INTERFACE.getImageData(), 0, 0);
			drawImage(JavaPluginImages.DESC_OVR_WARNING.getImageData(), 0, 8);
		}
	}

	static class MethodErrorImageDescriptor extends BaseCompositeImageDescriptor {
		@Override protected void drawCompositeImage(int width, int height) {
			drawImage(IMG_METHOD.getImageData(), 0, 0);
			drawImage(JavaPluginImages.DESC_OVR_ERROR.getImageData(), 0, 8);
		}
	}

	static class MethodWarningImageDescriptor extends BaseCompositeImageDescriptor {
		@Override protected void drawCompositeImage(int width, int height) {
			drawImage(IMG_METHOD.getImageData(), 0, 0);
			drawImage(JavaPluginImages.DESC_OVR_WARNING.getImageData(), 0, 8);
		}
	}

	static class LocationErrorImageDescriptor extends BaseCompositeImageDescriptor {
		@Override protected void drawCompositeImage(int width, int height) {
			drawImage(IMG_LOCATION.getImageData(), 0, 0);
			drawImage(JavaPluginImages.DESC_OVR_ERROR.getImageData(), 0, 8);
		}
	}

	static class LocationWarningImageDescriptor extends BaseCompositeImageDescriptor {
		@Override protected void drawCompositeImage(int width, int height) {
			drawImage(IMG_LOCATION.getImageData(), 0, 0);
			drawImage(JavaPluginImages.DESC_OVR_WARNING.getImageData(), 0, 8);
		}
	}
}
