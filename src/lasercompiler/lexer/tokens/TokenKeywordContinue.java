package lasercompiler.lexer.tokens;

import java.util.regex.Pattern;

public class TokenKeywordContinue extends Token {

	public TokenKeywordContinue() {
		super("TokenKeywordContinue");
	}
	
	public static Pattern getPattern() {
		return Pattern.compile("continue(?:\\b)");
	}


}
