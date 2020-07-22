package lasercompiler.lexer.tokens;

import java.util.regex.Pattern;

public class TokenTypeInteger extends Token {

	public TokenTypeInteger() {
		super("TokenTypeInteger");
	}

	public static Pattern getPattern() {
		return Pattern.compile("int\\b");
	}

	
}
