package lasercompiler.lexer.tokens;

import java.util.regex.Pattern;

public class TokenBraceClose extends Token {

	public TokenBraceClose() {
		super("TokenBraceClose");
	}

	
	public static Pattern getPattern() {
		return Pattern.compile("\\}");
	}

}
