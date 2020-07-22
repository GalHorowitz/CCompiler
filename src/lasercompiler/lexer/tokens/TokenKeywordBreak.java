package lasercompiler.lexer.tokens;

import java.util.regex.Pattern;

public class TokenKeywordBreak extends Token {

	public TokenKeywordBreak() {
		super("TokenKeywordBreak");
	}
	
	public static Pattern getPattern() {
		return Pattern.compile("break(?:\\b)");
	}

}
