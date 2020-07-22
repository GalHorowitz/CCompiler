package lasercompiler.lexer.tokens;

import java.util.regex.Pattern;

public class TokenColon extends Token {

	public TokenColon() {
		super("TokenColon");
	}

	
	public static Pattern getPattern() {
		return Pattern.compile("\\:");
	}

}
