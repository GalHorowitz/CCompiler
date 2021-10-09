package lasercompiler.parser.nodes;

public class ExpressionPostfixOperation extends Expression {

	public enum PostfixOperator {
		Increment("++"), Decrement("--");
		public final String op;

		PostfixOperator(String op) {
			this.op = op;
		}
	}

	private final ExpressionLValue lvalue;
	private final PostfixOperator operator;

	public ExpressionPostfixOperation(PostfixOperator operator, ExpressionLValue lvalue) {
		this.operator = operator;
		this.lvalue = lvalue;
	}

	public PostfixOperator getOperator() {
		return operator;
	}

	public ExpressionLValue getLValue() {
		return lvalue;
	}

	@Override
	public String toString() {
		return "(" + lvalue.toString() + ")" + operator.op;
	}

}
