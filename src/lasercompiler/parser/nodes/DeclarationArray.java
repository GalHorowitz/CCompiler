package lasercompiler.parser.nodes;

public class DeclarationArray extends Declaration {

    private final String identifier;
    private final int size;

    public DeclarationArray(String identifier, int size) {
        this.identifier = identifier;
        this.size = size;
    }

    public String getIdentifier() {
        return identifier;
    }

    public int getSize() {
        return size;
    }

    public boolean hasInitializer() {
        return false;
    }

    public Expression getInitializer() {
        return null;
    }

    @Override
    public String toString() {
        return "int " + identifier + "[" + size + "]";
    }

}
