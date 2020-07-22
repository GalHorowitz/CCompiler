package lasercompiler.lexer.tokens;

import java.util.regex.Pattern;

public class TokenKeywordElse extends Token {

	public TokenKeywordElse() {
		super("TokenKeywordElse");
	}
	
	public static Pattern getPattern() {
		return Pattern.compile("else(?:\\b)");
	}


}
