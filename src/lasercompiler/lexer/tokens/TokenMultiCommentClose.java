package lasercompiler.lexer.tokens;

import java.util.regex.Pattern;

public class TokenMultiCommentClose extends Token {

	public TokenMultiCommentClose() {
		super("TokenMultiCommentClose");
	}

	
	public static Pattern getPattern() {
		return Pattern.compile("\\*\\/");
	}

}
