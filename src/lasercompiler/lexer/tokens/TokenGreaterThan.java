package lasercompiler.lexer.tokens;

import java.util.regex.Pattern;

public class TokenGreaterThan extends Token {

	public TokenGreaterThan() {
		super("TokenGreaterThan");
	}

	
	public static Pattern getPattern() {
		return Pattern.compile(">");
	}

}
