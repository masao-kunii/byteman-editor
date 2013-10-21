package jp.co.ntt.oss.jboss.byteman.editor.editor;

import org.eclipse.jface.text.rules.ICharacterScanner;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.IWordDetector;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.jface.text.rules.WordRule;

/**
 * This class extends {@code WordRule} for {@link BytemanRuleEditor}.
 *
 *
 */
public class BytemanWordRule extends WordRule {

	private boolean headOfLine;

	public BytemanWordRule(IWordDetector detector, boolean headOfLine) {
		super(detector);
		this.headOfLine = headOfLine;
	}

	public IToken evaluate(ICharacterScanner scanner) {
		if(headOfLine){
			int count = 0;
			try {
				char c = 0;
				while(scanner.getColumn() != 0){
					scanner.unread();
					c = (char) scanner.read();
					if(c != ' ' && c != '\t'){
						break;
					}
					scanner.unread();
					count++;
				}
				if(c != '\n' && scanner.getColumn() != 0){
					return Token.UNDEFINED;
				}
			} finally {
				for(int i = 0; i < count; i++){
					scanner.read();
				}
			}
		}
		scanner.unread();
		char c = (char) scanner.read();
		if(Character.isJavaIdentifierPart(c) || c == '.') {
			return Token.UNDEFINED;
		}
		return super.evaluate(scanner);
	}
}
