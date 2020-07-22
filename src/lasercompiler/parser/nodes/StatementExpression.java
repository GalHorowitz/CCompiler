package lasercompiler.parser.nodes;

public class StatementExpression extends Statement {

	private final Expression exp;
	private final boolean nullExpression;
	
	public StatementExpression(Expression expression) {
		this.exp = expression;
		this.nullExpression = false;
	}	
	
	public StatementExpression() {
		this.exp = null;
		this.nullExpression = true;
	}
	
	public boolean isNullExpression() {
		return nullExpression;
	}
	
	public Expression getExpression() {
		return exp;
	}
	
	@Override
	public String toString() {
		if(nullExpression) {
			return "";
		}else {
			return exp.toString();
		}
	}
	
}
