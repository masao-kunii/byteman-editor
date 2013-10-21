package jp.co.ntt.oss.jboss.byteman.editor.util;

/**
 * Provides utility methods related to {@code String} handling.
 *
 */
public class StringUtils {

	/**
	 * Returns the last word of the specified text.
	 * Java identifiers and dots are regarded as part of the word.
	 *
	 * @param source a text
	 * @return the last word of the specified text.
	 */
	public static String getLastWord(String source){
		return getLastWord(source, false);
	}

	/**
	 * Returns the last word in the specified text.
	 * Java identifiers and dots are regarded as part of the word.
	 * If allowSpecialCharacter is {@code true}, the special characters, '!', '^', '#',  '*' and '@', are also regarded as part of the word.
	 *
	 * @param source a text
	 * @param allowSpecialCharacter {@code true} if allow the special characters as parts of the word, otherwise false.
	 * @return the last word of the specified text.
	 */
	public static String getLastWord(String source, boolean allowSpecialCharacter) {
		StringBuilder sb = new StringBuilder();
		for(int i = source.length() - 1; i >= 0; i--){
			char c = source.charAt(i);
			if(Character.isJavaIdentifierPart(c) || c == '.'
					|| (allowSpecialCharacter && (c == '!' || c == '^' || c == '#' || c == '*' || c == '@'))){
				sb.insert(0, c);
			} else {
				break;
			}
		}
		return sb.toString();
	}

	/**
	 * Tests whether a string is empty or not. Returns {@code true} if null or the trimmed string length is {@literal 0}.
	 * @param target the target value
	 * @return {@code true} if the string value is empty, otherwise false
	 */
	public static boolean isEmpty(String target) {
		return target == null || target.trim().length() == 0;
	}

	/**
	 * Tests whether a string is empty or not. Returns {@code true} if it is not empty.
	 * This method returns opposite result of {@link #isEmpty(String)} returns.
	 * @param target the target value
	 * @return {@code true} if it is not empty, otherwise false
	 */
	public static boolean isNotEmpty(String target) {
		return !isEmpty(target);
	}

	/**
	 * Removes the leading white space.
	 * @param target the target value
	 * @return the value which is removed the leading white space.
	 */
	public static String trimHead(String target) {
		if(isEmpty(target)) {
			return target == null ? target : target.trim();
		}
		if(target.charAt(0) == ' ' || target.charAt(0) == '\t') {
			return trimHead(target.substring(1));
		} else {
			return target;
		}
	}
}
