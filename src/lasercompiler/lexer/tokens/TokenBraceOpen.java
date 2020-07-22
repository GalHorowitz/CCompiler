package lasercompiler.lexer.tokens;

import java.util.regex.Pattern;

public class TokenBraceOpen extends Token {

	public TokenBraceOpen() {
		super("TokenBraceOpen");
	}

	public static Pattern getPattern() {
		return Pattern.compile("\\{");
	}
	
}
