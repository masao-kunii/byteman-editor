package jp.co.ntt.oss.jboss.byteman.editor.util;

import java.io.Closeable;
import java.io.IOException;

/**
 * This class provides utility methods for I/O.
 *
 */
public class IOUtils {

	/**
	 * Closes the given {@code Closeable} object without an error.
	 * @param closeable  an object to close
	 */
	public static void closeQuietly(Closeable closeable) {
		if(closeable != null) {
			try {
				closeable.close();
			} catch (IOException e) {
				// ignore
			}
		}
	}

}
