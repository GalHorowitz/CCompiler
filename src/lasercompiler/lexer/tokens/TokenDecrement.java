package lasercompiler.lexer.tokens;

import java.util.regex.Pattern;

public class TokenDecrement extends Token {

	public TokenDecrement() {
		super("TokenDecrement");
	}

	
	public static Pattern getPattern() {
		return Pattern.compile("\\-\\-");
	}

}
