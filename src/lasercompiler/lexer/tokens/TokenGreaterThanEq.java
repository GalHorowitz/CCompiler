package lasercompiler.lexer.tokens;

import java.util.regex.Pattern;

public class TokenGreaterThanEq extends Token {

	public TokenGreaterThanEq() {
		super("TokenGreaterThanEq");
	}

	
	public static Pattern getPattern() {
		return Pattern.compile(">=");
	}

}
