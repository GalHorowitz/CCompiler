package lasercompiler.lexer.tokens;

import java.util.regex.Pattern;

public class TokenKeywordReturn extends Token {

	public TokenKeywordReturn() {
		super("TokenKeywordReturn");
	}
	
	public static Pattern getPattern() {
		return Pattern.compile("return(?:\\b)");
	}


}
