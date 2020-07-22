package lasercompiler.lexer.tokens;

import java.util.regex.Pattern;

public class TokenNotEqual extends Token {

	public TokenNotEqual() {
		super("TokenNotEqual");
	}

	
	public static Pattern getPattern() {
		return Pattern.compile("!=");
	}

}
