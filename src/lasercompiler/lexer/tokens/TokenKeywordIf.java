package lasercompiler.lexer.tokens;

import java.util.regex.Pattern;

public class TokenKeywordIf extends Token {

	public TokenKeywordIf() {
		super("TokenKeywordIf");
	}
	
	public static Pattern getPattern() {
		return Pattern.compile("if(?:\\b)");
	}


}
