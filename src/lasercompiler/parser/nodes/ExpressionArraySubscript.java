package lasercompiler.parser.nodes;

public class ExpressionArraySubscript extends ExpressionLValue {

    private final String arrayIdentifier;
    private final Expression index;

    public ExpressionArraySubscript(String arrayIdentifier, Expression index) {
        this.arrayIdentifier = arrayIdentifier;
        this.index = index;
    }

    public String getIdentifier() {
        return arrayIdentifier;
    }

    public Expression getIndex() {
        return index;
    }

    @Override
    public String toString() {
        return arrayIdentifier + "[" + index.toString() + "]";
    }
}
