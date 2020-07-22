package lasercompiler.lexer.tokens;

import java.util.regex.Pattern;

public class TokenAssignmentDivide extends Token {

	public TokenAssignmentDivide() {
		super("TokenAssignmentDivide");
	}

	
	public static Pattern getPattern() {
		return Pattern.compile("\\/\\=");
	}

}
