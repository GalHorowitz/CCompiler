package lasercompiler.lexer.tokens;

import java.util.regex.Pattern;

public class TokenIncrement extends Token {

	public TokenIncrement() {
		super("TokenIncrement");
	}

	
	public static Pattern getPattern() {
		return Pattern.compile("\\+\\+");
	}

}
