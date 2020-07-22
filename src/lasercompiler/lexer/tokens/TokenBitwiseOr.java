package lasercompiler.lexer.tokens;

import java.util.regex.Pattern;

public class TokenBitwiseOr extends Token {

	public TokenBitwiseOr() {
		super("TokenBitwiseOr");
	}

	
	public static Pattern getPattern() {
		return Pattern.compile("\\|");
	}

}
