package lasercompiler.lexer.tokens;

import java.util.regex.Pattern;

public class TokenKeywordDo extends Token {

	public TokenKeywordDo() {
		super("TokenKeywordIf");
	}
	
	public static Pattern getPattern() {
		return Pattern.compile("do(?:\\b)");
	}


}
