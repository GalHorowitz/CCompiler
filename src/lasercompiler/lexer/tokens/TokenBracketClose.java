package lasercompiler.lexer.tokens;

import java.util.regex.Pattern;

public class TokenBracketClose extends Token {

    public TokenBracketClose() {
        super("TokenBracketClose");
    }

    public static Pattern getPattern() {
        return Pattern.compile("\\]");
    }

}
