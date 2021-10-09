package lasercompiler.parser.nodes;

public class ExpressionAssignment extends Expression {

	private final ExpressionLValue lvalue;
	private final Expression value;
	
	public ExpressionAssignment(ExpressionLValue lvalue, Expression value) {
		this.lvalue = lvalue;
		this.value = value;
	}
	
	public ExpressionLValue getLValue() {
		return lvalue;
	}
	
	public Expression getValue() {
		return value;
	}
	
	@Override
	public String toString() {
		return lvalue + " = " + value.toString();
	}

}
