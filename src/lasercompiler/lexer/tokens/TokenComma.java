package lasercompiler.lexer.tokens;

import java.util.regex.Pattern;

public class TokenComma extends Token {

	public TokenComma() {
		super("TokenComma");
	}

	
	public static Pattern getPattern() {
		return Pattern.compile("\\,");
	}

}
