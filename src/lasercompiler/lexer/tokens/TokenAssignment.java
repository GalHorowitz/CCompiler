package lasercompiler.lexer.tokens;

import java.util.regex.Pattern;

public class TokenAssignment extends Token {

	public TokenAssignment() {
		super("TokenAssignment");
	}

	
	public static Pattern getPattern() {
		return Pattern.compile("\\=");
	}

}
