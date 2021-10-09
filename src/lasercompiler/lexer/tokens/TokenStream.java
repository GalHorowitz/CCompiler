package lasercompiler.lexer.tokens;

import java.util.List;

public class TokenStream {
    private final List<Token> tokens;
    private int curIndex;

    public TokenStream(List<Token> tokens) {
        this.tokens = tokens;
        this.curIndex = 0;
    }

    private TokenStream(List<Token> tokens, int startIndex){
        this.tokens = tokens;
        this.curIndex = startIndex;
    }

    public Token take() {
        if (isEmpty()) {
            throw new IndexOutOfBoundsException();
        }
        curIndex += 1;
        return tokens.get(curIndex - 1);
    }

    public Token get(int off) {
        if (curIndex + off >= tokens.size()) {
            throw new IndexOutOfBoundsException();
        }

        return tokens.get(curIndex + off);
    }

    public boolean isEmpty() {
        return curIndex == tokens.size();
    }

    public boolean has(int amount) {
        return curIndex + amount <= tokens.size();
    }

    public TokenStream createView() {
        return new TokenStream(tokens, curIndex);
    }

    @Override
    public String toString() {
        return tokens.toString();
    }

    public String toStringView() {
        return tokens.subList(curIndex, tokens.size()).toString();
    }
}
