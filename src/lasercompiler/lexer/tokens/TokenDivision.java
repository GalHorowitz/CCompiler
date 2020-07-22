package lasercompiler.lexer.tokens;

import java.util.regex.Pattern;

public class TokenDivision extends Token {

	public TokenDivision() {
		super("TokenDivision");
	}

	
	public static Pattern getPattern() {
		return Pattern.compile("\\/");
	}

}
