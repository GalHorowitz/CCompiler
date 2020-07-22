package lasercompiler.lexer.tokens;

import java.util.regex.Pattern;

public class TokenLessThan extends Token {

	public TokenLessThan() {
		super("TokenLessThan");
	}

	
	public static Pattern getPattern() {
		return Pattern.compile("<");
	}

}
