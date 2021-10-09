package lasercompiler.parser.nodes;

public abstract class Declaration extends BlockItem {
    public abstract String getIdentifier();
    public abstract boolean hasInitializer();
    public abstract Expression getInitializer();
}
