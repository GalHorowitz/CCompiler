package lasercompiler.lexer.tokens;

import java.util.regex.Pattern;

public class TokenMinus extends Token {

	public TokenMinus() {
		super("TokenMinus");
	}

	
	public static Pattern getPattern() {
		return Pattern.compile("\\-");
	}

}
