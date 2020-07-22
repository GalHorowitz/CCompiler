package lasercompiler.lexer.tokens;

import java.util.regex.Pattern;

public class TokenMultiplication extends Token {

	public TokenMultiplication() {
		super("TokenMultiplication");
	}

	
	public static Pattern getPattern() {
		return Pattern.compile("\\*");
	}

}
