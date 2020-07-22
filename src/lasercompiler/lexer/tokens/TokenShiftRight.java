package lasercompiler.lexer.tokens;

import java.util.regex.Pattern;

public class TokenShiftRight extends Token {

	public TokenShiftRight() {
		super("TokenShiftRight");
	}

	
	public static Pattern getPattern() {
		return Pattern.compile("\\>\\>");
	}

}
