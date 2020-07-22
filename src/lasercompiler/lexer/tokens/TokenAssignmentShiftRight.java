package lasercompiler.lexer.tokens;

import java.util.regex.Pattern;

public class TokenAssignmentShiftRight extends Token {

	public TokenAssignmentShiftRight() {
		super("TokenAssignmentShiftRight");
	}

	
	public static Pattern getPattern() {
		return Pattern.compile("\\>\\>\\=");
	}

}
