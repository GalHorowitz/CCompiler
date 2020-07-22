package lasercompiler.lexer.tokens;

import java.util.regex.Pattern;

public class TokenQuestionMark extends Token {

	public TokenQuestionMark() {
		super("TokenQuestionMark");
	}

	
	public static Pattern getPattern() {
		return Pattern.compile("\\?");
	}

}
