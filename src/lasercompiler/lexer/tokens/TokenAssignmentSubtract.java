package lasercompiler.lexer.tokens;

import java.util.regex.Pattern;

public class TokenAssignmentSubtract extends Token {

	public TokenAssignmentSubtract() {
		super("TokenAssignmentSubtract");
	}

	
	public static Pattern getPattern() {
		return Pattern.compile("\\-\\=");
	}

}
