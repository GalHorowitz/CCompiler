package lasercompiler.lexer.tokens;

import java.util.regex.Pattern;

public class TokenBracketOpen extends Token {

    public TokenBracketOpen() {
        super("TokenBracketOpen");
    }

    public static Pattern getPattern() {
        return Pattern.compile("\\[");
    }

}
