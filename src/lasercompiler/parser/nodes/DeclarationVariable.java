package lasercompiler.parser.nodes;

public class DeclarationVariable extends Declaration {
    private final String identifier;
    private final boolean hasInitializer;
    private Expression initializer;

    public DeclarationVariable(String identifier) {
        this.identifier = identifier;
        this.hasInitializer = false;
    }

    public DeclarationVariable(String identifier, Expression initializer) {
        this.identifier = identifier;
        this.hasInitializer = true;
        this.initializer = initializer;
    }

    public Expression getInitializer() {
        return initializer;
    }

    public String getIdentifier() {
        return identifier;
    }

    public boolean hasInitializer() {
        return hasInitializer;
    }

    @Override
    public String toString() {
        StringBuilder s = new StringBuilder("int ");
        s.append(identifier);
        if(hasInitializer) {
            s.append(" = ");
            s.append(initializer.toString());
        }
        return s.toString();
    }
}
