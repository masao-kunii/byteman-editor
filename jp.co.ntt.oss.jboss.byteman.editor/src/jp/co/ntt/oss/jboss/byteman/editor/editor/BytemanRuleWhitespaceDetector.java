package jp.co.ntt.oss.jboss.byteman.editor.editor;

import org.eclipse.jface.text.rules.IWhitespaceDetector;

/**
 * The implementation of {@code IWhitespaceDetector} for {@link BytemanRuleEditor}.
 *
 */
public class BytemanRuleWhitespaceDetector implements IWhitespaceDetector {

	public boolean isWhitespace(char c) {
		return (c == ' ' || c == '\t' || c == '\n' || c == '\r');
	}
}
