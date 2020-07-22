package lasercompiler.parser.nodes;

public class Declaration extends BlockItem {

	private final String variable;
	private final boolean hasInitializer;
	private Expression initializer;
	
	public Declaration(String variable) {
		this.variable = variable;
		this.hasInitializer = false;
	}
	
	public Declaration(String variable, Expression initializer) {
		this.variable = variable;
		this.hasInitializer = true;
		this.initializer = initializer;
	}
	
	public Expression getInitializer() {
		return initializer;
	}
	
	public String getVariable() {
		return variable;
	}
	
	public boolean hasInitializer() {
		return hasInitializer;
	}

	@Override
	public String toString() {
		StringBuilder s = new StringBuilder("int ");
		s.append(variable);
		if(hasInitializer) {
			s.append(" = ");
			s.append(initializer.toString());
		}
		return s.toString();
	}

}
