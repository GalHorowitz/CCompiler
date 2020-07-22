package lasercompiler.parser.nodes;

public class ExpressionBinaryOperation extends Expression {

	public enum BinaryOperator {
		Subtraction("-"), Addition("+"), Multiplication("*"), Division("/"), LogicalAnd("&&"), LogicalOr("||"),
		Equal("=="), NotEqual("!="), LessThan("<"), LessThanEq("<="), GreaterThan(">"), GreaterThanEq(">="),
		Modulo("%"), BitwiseAnd("&"), BitwiseOr("|"), BitwiseXor("^"), ShiftLeft("<<"), ShiftRight(">>"), Comma(","), Power("**");

		public final String op;

		private BinaryOperator(String op) {
			this.op = op;
		}
	}

	private final BinaryOperator operator;
	private final Expression left;
	private final Expression right;

	public ExpressionBinaryOperation(BinaryOperator operator, Expression left, Expression right) {
		this.operator = operator;
		this.left = left;
		this.right = right;
	}

	public Expression getLeft() {
		return left;
	}

	public Expression getRight() {
		return right;
	}

	public BinaryOperator getOperator() {
		return operator;
	}

	@Override
	public String toString() {
		if (operator == BinaryOperator.Comma) {
			return "(" + left.toString() + ")" + operator.op + " (" + right.toString() + ")";
		} else {
			return "(" + left.toString() + ") " + operator.op + " (" + right.toString() + ")";
		}
	}

}
