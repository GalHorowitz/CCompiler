package lasercompiler.parser.nodes;

public class StatementWhile extends Statement {

	private final Expression condition;
	private final Statement body;
	
	public StatementWhile(Expression condition, Statement body) {
		this.condition = condition;
		this.body = body;
	}
	
	public Expression getCondition() {
		return condition;
	}
	
	public Statement getBody() {
		return body;
	}
	
	@Override
	public String toString() {
		StringBuilder format = new StringBuilder("while (");
		format.append(condition.toString());
		format.append(")\n");
		if(!(body instanceof StatementCompound)) {
			format.append("\t");
		}
		format.append(body.toString());
		return format.toString();
	}
	
}
