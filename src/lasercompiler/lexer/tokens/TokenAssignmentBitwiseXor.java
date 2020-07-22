package lasercompiler.lexer.tokens;

import java.util.regex.Pattern;

public class TokenAssignmentBitwiseXor extends Token {

	public TokenAssignmentBitwiseXor() {
		super("TokenAssignmentBitwiseXor");
	}

	
	public static Pattern getPattern() {
		return Pattern.compile("\\^\\=");
	}

}
