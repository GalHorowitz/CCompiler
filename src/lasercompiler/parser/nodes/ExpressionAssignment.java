package lasercompiler.parser.nodes;

public class ExpressionAssignment extends Expression {

	private final String variable;
	private final Expression value;
	
	public ExpressionAssignment(String variable, Expression value) {
		this.variable = variable;
		this.value = value;
	}
	
	public String getVariable() {
		return variable;
	}
	
	public Expression getValue() {
		return value;
	}
	
	@Override
	public String toString() {
		return variable + " = " + value.toString();
	}

}
