package lasercompiler.lexer.tokens;

import java.util.regex.Pattern;

public class TokenLessThanEq extends Token {

	public TokenLessThanEq() {
		super("TokenLessThanEq");
	}

	
	public static Pattern getPattern() {
		return Pattern.compile("<=");
	}

}
