package lasercompiler.lexer.tokens;

import java.util.regex.Pattern;

public class TokenComment extends Token {

	public TokenComment() {
		super("TokenComment");
	}

	
	public static Pattern getPattern() {
		return Pattern.compile("\\/\\/");
	}

}
