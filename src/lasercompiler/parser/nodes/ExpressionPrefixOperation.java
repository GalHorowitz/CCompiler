package lasercompiler.parser.nodes;

public class ExpressionPrefixOperation extends Expression {

	public enum PrefixOperator {
		Increment("++"), Decrement("--");
		public final String op;

		PrefixOperator(String op) {
			this.op = op;
		}
	}

	private final ExpressionLValue lvalue;
	private final PrefixOperator operator;

	public ExpressionPrefixOperation(PrefixOperator operator, ExpressionLValue lvalue) {
		this.operator = operator;
		this.lvalue = lvalue;
	}

	public PrefixOperator getOperator() {
		return operator;
	}

	public ExpressionLValue getLValue() {
		return lvalue;
	}

	@Override
	public String toString() {
		return operator.op + "(" + lvalue.toString() + ")";
	}

}
