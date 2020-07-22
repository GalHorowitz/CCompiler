package lasercompiler.lexer.tokens;

import java.util.regex.Pattern;

public class TokenParenClose extends Token {

	public TokenParenClose() {
		super("TokenParenClose");
	}
	
	public static Pattern getPattern() {
		return Pattern.compile("\\)");
	}

}
