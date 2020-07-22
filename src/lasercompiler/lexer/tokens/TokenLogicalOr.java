package lasercompiler.lexer.tokens;

import java.util.regex.Pattern;

public class TokenLogicalOr extends Token {

	public TokenLogicalOr() {
		super("TokenLogicalOr");
	}

	
	public static Pattern getPattern() {
		return Pattern.compile("\\|\\|");
	}

}
