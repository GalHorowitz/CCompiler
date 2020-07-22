package lasercompiler.lexer.tokens;

import java.util.regex.Pattern;

public class TokenAssignmentShiftLeft extends Token {

	public TokenAssignmentShiftLeft() {
		super("TokenAssignmentShiftLeft");
	}

	
	public static Pattern getPattern() {
		return Pattern.compile("\\<\\<\\=");
	}

}
