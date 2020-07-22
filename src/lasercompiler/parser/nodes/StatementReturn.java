package lasercompiler.parser.nodes;

public class StatementReturn extends Statement {

	private final Expression exp;
	
	public StatementReturn(Expression exp) {
		this.exp = exp;
	}
	
	public Expression getExpression() {
		return exp;
	}

	@Override
	public String toString() {
		return "return "+exp.toString();
	}
	
}
