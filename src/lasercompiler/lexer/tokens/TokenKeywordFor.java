package lasercompiler.lexer.tokens;

import java.util.regex.Pattern;

public class TokenKeywordFor extends Token {

	public TokenKeywordFor() {
		super("TokenKeywordFor");
	}
	
	public static Pattern getPattern() {
		return Pattern.compile("for(?:\\b)");
	}


}
