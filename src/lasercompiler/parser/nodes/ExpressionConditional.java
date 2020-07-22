package lasercompiler.parser.nodes;

public class ExpressionConditional extends Expression {
	
	private final Expression condition;
	private final Expression trueValue;
	private final Expression falseValue;
	
	public ExpressionConditional(Expression condition, Expression trueValue, Expression falseValue) {
		this.condition = condition;
		this.trueValue = trueValue;
		this.falseValue = falseValue;
	}
	
	public Expression getCondition() {
		return condition;
	}
	
	public Expression getTrueValue() {
		return trueValue;
	}
	
	public Expression getFalseValue() {
		return falseValue;
	}
	
	@Override
	public String toString() {
		return "("+condition.toString()+")?("+trueValue.toString()+"):("+falseValue.toString()+")";
	}

}
