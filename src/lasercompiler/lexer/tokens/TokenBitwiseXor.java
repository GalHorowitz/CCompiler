package lasercompiler.lexer.tokens;

import java.util.regex.Pattern;

public class TokenBitwiseXor extends Token {

	public TokenBitwiseXor() {
		super("TokenBitwiseXor");
	}

	
	public static Pattern getPattern() {
		return Pattern.compile("\\^");
	}

}
