package lasercompiler.lexer.tokens;

import java.util.regex.Pattern;

public class TokenAssignmentBitwiseOr extends Token {

	public TokenAssignmentBitwiseOr() {
		super("TokenAssignmentBitwiseOr");
	}

	
	public static Pattern getPattern() {
		return Pattern.compile("\\|\\=");
	}

}
