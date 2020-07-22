package lasercompiler.lexer.tokens;

import java.util.regex.Pattern;

public class TokenAssignmentAdd extends Token {

	public TokenAssignmentAdd() {
		super("TokenAssignmentAdd");
	}

	
	public static Pattern getPattern() {
		return Pattern.compile("\\+\\=");
	}

}
