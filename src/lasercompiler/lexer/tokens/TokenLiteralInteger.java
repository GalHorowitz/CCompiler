package lasercompiler.lexer.tokens;

import java.util.regex.Pattern;

public class TokenLiteralInteger extends Token {

	private String literal;
	
	public TokenLiteralInteger(String literal) {
		super("TokenLiteralInteger");
		this.literal = literal;
	}

	public static Pattern getPattern() {
		return Pattern.compile("[0-9]+\\b");
	}
	
	public String getLiteral() {
		return literal;
	}
	
	@Override
	public String toString() {
		return this.name+"("+this.literal+")";
	}
	
}
