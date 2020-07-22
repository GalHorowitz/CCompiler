package lasercompiler.lexer.tokens;

import java.util.regex.Pattern;

public abstract class Token {

	protected final String name;
	
	public Token(String name) {
		this.name = name;
	}
	
	@Override
	public String toString() {
		return name;
	}
	
	public static Pattern getPattern() {
		throw new IllegalStateException("Pattern not implemented for token");
	};
	
}
