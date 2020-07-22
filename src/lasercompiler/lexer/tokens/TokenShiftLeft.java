package lasercompiler.lexer.tokens;

import java.util.regex.Pattern;

public class TokenShiftLeft extends Token {

	public TokenShiftLeft() {
		super("TokenShiftLeft");
	}

	
	public static Pattern getPattern() {
		return Pattern.compile("\\<\\<");
	}

}
