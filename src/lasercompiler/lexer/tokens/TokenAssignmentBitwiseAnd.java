package lasercompiler.lexer.tokens;

import java.util.regex.Pattern;

public class TokenAssignmentBitwiseAnd extends Token {

	public TokenAssignmentBitwiseAnd() {
		super("TokenAssignmentBitwiseAnd");
	}

	
	public static Pattern getPattern() {
		return Pattern.compile("\\&\\=");
	}

}
