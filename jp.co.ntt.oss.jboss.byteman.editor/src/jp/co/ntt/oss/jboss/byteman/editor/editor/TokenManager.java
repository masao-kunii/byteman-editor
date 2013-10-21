package jp.co.ntt.oss.jboss.byteman.editor.editor;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import jp.co.ntt.oss.jboss.byteman.editor.BytemanEditorPlugin;

import org.eclipse.jdt.ui.PreferenceConstants;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;

/**
 * Manager of the {@code IToken} for the {@link BytemanRuleEditor}.
 */
public class TokenManager {

	// Token keys
	public static final String TOKEN_BYTEMAN_KEYWORD = "BYTEMAN_KEYWORD";
	public static final String TOKEN_BYTEMAN_DEFAULT = "BYTEMAN_DEFAULT";
	public static final String TOKEN_JAVA_KEYWORD = "JAVA_KEYWORD";
	public static final String TOKEN_JAVA_KEYWORD_RETURN = "JAVA_KEYWORD_RETURN";
	public static final String TOKEN_JAVA_STRING = "JAVA_STRING";
	public static final String TOKEN_JAVA_SINGLE_LINE_COMMENT = "JAVA_SINGLE_LINE_COMMENT";

	private Map<RGB, Color> fColorTable = new HashMap<RGB, Color>();
	private Map<String, TokenInfo> tokenMap = new HashMap<String, TokenInfo>();
	private Map<String, TokenInfo> jdtTokenMap = new HashMap<String, TokenInfo>();

	public TokenManager(){
		registerToken(TOKEN_BYTEMAN_DEFAULT, new TokenManager.TokenInfo(
				BytemanEditorPlugin.PREF_DEFAULT_COLOR,
				BytemanEditorPlugin.PREF_DEFAULT_BOLD,
				BytemanEditorPlugin.PREF_DEFAULT_ITALIC,
				BytemanEditorPlugin.PREF_DEFAULT_STRIKE,
				BytemanEditorPlugin.PREF_DEFAULT_UNDERLINE));

		registerToken(TOKEN_BYTEMAN_KEYWORD, new TokenManager.TokenInfo(
				BytemanEditorPlugin.PREF_KEYWORD_COLOR,
				BytemanEditorPlugin.PREF_KEYWORD_BOLD,
				BytemanEditorPlugin.PREF_KEYWORD_ITALIC,
				BytemanEditorPlugin.PREF_KEYWORD_STRIKE,
				BytemanEditorPlugin.PREF_KEYWORD_UNDERLINE));

		registerJdtToken(TOKEN_JAVA_KEYWORD, new TokenManager.TokenInfo(
				PreferenceConstants.EDITOR_JAVA_KEYWORD_COLOR,
				PreferenceConstants.EDITOR_JAVA_KEYWORD_BOLD,
				PreferenceConstants.EDITOR_JAVA_KEYWORD_ITALIC,
				PreferenceConstants.EDITOR_JAVA_KEYWORD_STRIKETHROUGH,
				PreferenceConstants.EDITOR_JAVA_KEYWORD_UNDERLINE));

		registerJdtToken(TOKEN_JAVA_KEYWORD_RETURN, new TokenManager.TokenInfo(
				PreferenceConstants.EDITOR_JAVA_KEYWORD_RETURN_COLOR,
				PreferenceConstants.EDITOR_JAVA_KEYWORD_RETURN_BOLD,
				PreferenceConstants.EDITOR_JAVA_KEYWORD_RETURN_ITALIC,
				PreferenceConstants.EDITOR_JAVA_KEYWORD_RETURN_STRIKETHROUGH,
				PreferenceConstants.EDITOR_JAVA_KEYWORD_RETURN_UNDERLINE));

		registerJdtToken(TOKEN_JAVA_STRING, new TokenManager.TokenInfo(
				PreferenceConstants.EDITOR_STRING_COLOR,
				PreferenceConstants.EDITOR_STRING_BOLD,
				PreferenceConstants.EDITOR_STRING_ITALIC,
				PreferenceConstants.EDITOR_STRING_STRIKETHROUGH,
				PreferenceConstants.EDITOR_STRING_UNDERLINE));

		registerJdtToken(TOKEN_JAVA_SINGLE_LINE_COMMENT, new TokenManager.TokenInfo(
				PreferenceConstants.EDITOR_SINGLE_LINE_COMMENT_COLOR,
				PreferenceConstants.EDITOR_SINGLE_LINE_COMMENT_BOLD,
				PreferenceConstants.EDITOR_SINGLE_LINE_COMMENT_ITALIC,
				PreferenceConstants.EDITOR_SINGLE_LINE_COMMENT_STRIKETHROUGH,
				PreferenceConstants.EDITOR_SINGLE_LINE_COMMENT_UNDERLINE));

		registerJdtToken(TOKEN_JAVA_SINGLE_LINE_COMMENT, new TokenManager.TokenInfo(
				PreferenceConstants.EDITOR_SINGLE_LINE_COMMENT_COLOR,
				PreferenceConstants.EDITOR_SINGLE_LINE_COMMENT_BOLD,
				PreferenceConstants.EDITOR_SINGLE_LINE_COMMENT_ITALIC,
				PreferenceConstants.EDITOR_SINGLE_LINE_COMMENT_STRIKETHROUGH,
				PreferenceConstants.EDITOR_SINGLE_LINE_COMMENT_UNDERLINE));
	}

	public void dispose() {
		Iterator<Color> e = fColorTable.values().iterator();
		while (e.hasNext()){
			 e.next().dispose();
		}
	}

	public IToken getToken(String key){
		if(tokenMap.containsKey(key)){
			return tokenMap.get(key).token;

		} else if(jdtTokenMap.containsKey(key)){
			return jdtTokenMap.get(key).token;
		}

		throw new IllegalArgumentException(String.format("'%s' is invalid token key.", key));
	}

	public void refreshTokens(){
		for(TokenInfo tokenInfo: tokenMap.values()){
			updateToken(BytemanEditorPlugin.getDefault().getPreferenceStore(), tokenInfo);
		}
		for(TokenInfo tokenInfo: jdtTokenMap.values()){
			updateToken(PreferenceConstants.getPreferenceStore(), tokenInfo);
		}
	}

	private Color getColor(RGB rgb) {
		Color color = fColorTable.get(rgb);
		if (color == null) {
			color = new Color(Display.getCurrent(), rgb);
			fColorTable.put(rgb, color);
		}
		return color;
	}

	private IToken registerToken(String key, TokenInfo tokenInfo){
		updateToken(BytemanEditorPlugin.getDefault().getPreferenceStore(), tokenInfo);
		tokenMap.put(key, tokenInfo);
		return tokenInfo.token;
	}

	private IToken registerJdtToken(String key, TokenInfo tokenInfo){
		updateToken(PreferenceConstants.getPreferenceStore(), tokenInfo);
		jdtTokenMap.put(key, tokenInfo);
		return tokenInfo.token;
	}

	private void updateToken(IPreferenceStore store, TokenInfo tokenInfo){
		TextAttribute attr = getTextAttribute(
				store,
				tokenInfo.colorKey,
				tokenInfo.boldKey,
				tokenInfo.italicKey,
				tokenInfo.strikeKey,
				tokenInfo.underlineKey);

		if(tokenInfo.token == null){
			tokenInfo.token = new Token(attr);
		} else {
			((Token) tokenInfo.token).setData(attr);
		}
	}

	private TextAttribute getTextAttribute(IPreferenceStore store,
			String colorKey, String boldKey, String italicKey, String strikeKey, String underlineKey){

		RGB rgb = PreferenceConverter.getColor(store, colorKey);

		int style = SWT.NORMAL;
		if(store.getBoolean(boldKey)){
			style = style | SWT.BOLD;
		}
		if(store.getBoolean(italicKey)){
			style = style | SWT.ITALIC;
		}
		if(store.getBoolean(strikeKey)){
			style = style | TextAttribute.STRIKETHROUGH;
		}
		if(store.getBoolean(underlineKey)){
			style = style | TextAttribute.UNDERLINE;
		}

		return new TextAttribute(getColor(rgb), null, style);
	}



	private static class TokenInfo {
		public String colorKey;
		public String boldKey;
		public String italicKey;
		public String strikeKey;
		public String underlineKey;
		public IToken token;

		public TokenInfo(String colorKey, String boldKey, String italicKey,
				String strikeKey, String underlineKey){
			this.colorKey = colorKey;
			this.boldKey = boldKey;
			this.italicKey = italicKey;
			this.strikeKey = strikeKey;
			this.underlineKey = underlineKey;
		}
	}
}
