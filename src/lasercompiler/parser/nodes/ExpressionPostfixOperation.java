package lasercompiler.parser.nodes;

public class ExpressionPostfixOperation extends Expression {

	public enum PostfixOperator {
		Increment("++"), Decrement("--");
		public final String op;

		PostfixOperator(String op) {
			this.op = op;
		}
	}

	private final ExpressionVariable variable;
	private final PostfixOperator operator;

	public ExpressionPostfixOperation(PostfixOperator operator, ExpressionVariable variable) {
		this.operator = operator;
		this.variable = variable;
	}

	public PostfixOperator getOperator() {
		return operator;
	}

	public ExpressionVariable getVariableExpression() {
		return variable;
	}

	@Override
	public String toString() {
		return "(" + variable.toString() + ")" + operator.op;
	}

}
