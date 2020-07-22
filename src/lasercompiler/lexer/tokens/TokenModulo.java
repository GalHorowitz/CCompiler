package lasercompiler.lexer.tokens;

import java.util.regex.Pattern;

public class TokenModulo extends Token {

	public TokenModulo() {
		super("TokenModulo");
	}

	
	public static Pattern getPattern() {
		return Pattern.compile("\\%");
	}

}
