package lasercompiler.lexer.tokens;

import java.util.regex.Pattern;

public class TokenLogicalAnd extends Token {

	public TokenLogicalAnd() {
		super("TokenLogicalAnd");
	}

	
	public static Pattern getPattern() {
		return Pattern.compile("\\&\\&");
	}

}
