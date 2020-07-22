package lasercompiler.lexer.tokens;

import java.util.regex.Pattern;

public class TokenPlus extends Token {

	public TokenPlus() {
		super("TokenPlus");
	}

	
	public static Pattern getPattern() {
		return Pattern.compile("\\+");
	}

}
