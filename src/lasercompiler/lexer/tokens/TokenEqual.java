package lasercompiler.lexer.tokens;

import java.util.regex.Pattern;

public class TokenEqual extends Token {

	public TokenEqual() {
		super("TokenEqual");
	}

	
	public static Pattern getPattern() {
		return Pattern.compile("==");
	}

}
