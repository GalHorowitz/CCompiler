package lasercompiler.lexer.tokens;

import java.util.regex.Pattern;

public class TokenLogicalNegation extends Token {

	public TokenLogicalNegation() {
		super("TokenLogicalNegation");
	}

	
	public static Pattern getPattern() {
		return Pattern.compile("!");
	}

}
