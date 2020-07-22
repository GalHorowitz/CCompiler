package lasercompiler.lexer.tokens;

import java.util.regex.Pattern;

public class TokenComplement extends Token {

	public TokenComplement() {
		super("TokenComplement");
	}

	
	public static Pattern getPattern() {
		return Pattern.compile("~");
	}

}
