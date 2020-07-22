package lasercompiler.lexer.tokens;

import java.util.regex.Pattern;

public class TokenKeywordWhile extends Token {

	public TokenKeywordWhile() {
		super("TokenKeywordWhile");
	}
	
	public static Pattern getPattern() {
		return Pattern.compile("while(?:\\b)");
	}


}
