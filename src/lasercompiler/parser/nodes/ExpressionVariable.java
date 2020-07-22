package lasercompiler.parser.nodes;

public class ExpressionVariable extends Expression {

	private final String variable;
	
	public ExpressionVariable(String variable) {
		this.variable = variable;
	}
	
	public String getVariable() {
		return variable;
	}
	
	@Override
	public String toString() {
		return variable;
	}

}
