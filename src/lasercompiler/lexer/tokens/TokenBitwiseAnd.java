package lasercompiler.lexer.tokens;

import java.util.regex.Pattern;

public class TokenBitwiseAnd extends Token {

	public TokenBitwiseAnd() {
		super("TokenBitwiseAnd");
	}

	
	public static Pattern getPattern() {
		return Pattern.compile("\\&");
	}

}
