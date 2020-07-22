package lasercompiler.lexer.tokens;

import java.util.regex.Pattern;

public class TokenSemicolon extends Token {

	public TokenSemicolon() {
		super("TokenSemicolon");
	}
	

	public static Pattern getPattern() {
		return Pattern.compile(";");
	}


}
