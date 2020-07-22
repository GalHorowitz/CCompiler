package lasercompiler.parser.nodes;

public class ExpressionPrefixOperation extends Expression {

	public enum PrefixOperator {
		Increment("++"), Decrement("--");
		public final String op;

		PrefixOperator(String op) {
			this.op = op;
		}
	}

	private final ExpressionVariable variable;
	private final PrefixOperator operator;

	public ExpressionPrefixOperation(PrefixOperator operator, ExpressionVariable variable) {
		this.operator = operator;
		this.variable = variable;
	}

	public PrefixOperator getOperator() {
		return operator;
	}

	public ExpressionVariable getVariableExpression() {
		return variable;
	}

	@Override
	public String toString() {
		return operator.op + "(" + variable.toString() + ")";
	}

}
