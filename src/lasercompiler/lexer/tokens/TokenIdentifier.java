package lasercompiler.lexer.tokens;

import java.util.regex.Pattern;

public class TokenIdentifier extends Token {

	private String identifier;
	
	public TokenIdentifier(String identifier) {
		super("TokenIdentifier");
		this.identifier = identifier;
	}

	@Override
	public String toString() {
		return this.name+"("+this.identifier+")";
	}
	
	public static Pattern getPattern() {
		return Pattern.compile("[a-zA-Z]\\w*");
	}
	
	public String getIdentifier() {
		return identifier;
	}
	
}
