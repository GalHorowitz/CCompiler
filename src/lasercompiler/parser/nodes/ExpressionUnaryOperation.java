package lasercompiler.parser.nodes;

public class ExpressionUnaryOperation extends Expression {

	public enum UnaryOperator {
		Negation("-"), BitwiseComplement("~"), LogicalNegation("!");

		public final String op;

		private UnaryOperator(String op) {
			this.op = op;
		}
	}

	private final UnaryOperator operator;
	private final Expression exp;

	public ExpressionUnaryOperation(UnaryOperator operator, Expression exp) {
		this.operator = operator;
		this.exp = exp;
	}

	public UnaryOperator getOperator() {
		return operator;
	}

	public Expression getExpression() {
		return exp;
	}

	@Override
	public String toString() {
		return operator.op + "(" + exp.toString() + ")";
	}

}
