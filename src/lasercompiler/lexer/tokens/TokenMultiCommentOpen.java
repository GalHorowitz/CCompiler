package lasercompiler.lexer.tokens;

import java.util.regex.Pattern;

public class TokenMultiCommentOpen extends Token {

	public TokenMultiCommentOpen() {
		super("TokenMultiCommentOpen");
	}

	
	public static Pattern getPattern() {
		return Pattern.compile("\\/\\*");
	}

}
