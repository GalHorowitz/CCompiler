package lasercompiler.lexer.tokens;

import java.util.regex.Pattern;

public class TokenParenOpen extends Token {

	public TokenParenOpen() {
		super("TokenParenOpen");
	}

	public static Pattern getPattern() {
		return Pattern.compile("\\(");
	}
	
}
