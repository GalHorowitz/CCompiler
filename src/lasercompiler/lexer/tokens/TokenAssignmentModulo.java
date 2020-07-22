package lasercompiler.lexer.tokens;

import java.util.regex.Pattern;

public class TokenAssignmentModulo extends Token {

	public TokenAssignmentModulo() {
		super("TokenAssignmentModulo");
	}

	
	public static Pattern getPattern() {
		return Pattern.compile("\\%\\=");
	}

}
