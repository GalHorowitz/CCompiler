package lasercompiler.lexer.tokens;

import java.util.regex.Pattern;

public class TokenAssignmentMultiply extends Token {

	public TokenAssignmentMultiply() {
		super("TokenAssignmentMultiply");
	}

	
	public static Pattern getPattern() {
		return Pattern.compile("\\*\\=");
	}

}
