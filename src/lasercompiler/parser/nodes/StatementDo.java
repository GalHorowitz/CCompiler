package lasercompiler.parser.nodes;

public class StatementDo extends Statement {
	
	private final Statement body;
	private final Expression condition;
	
	public StatementDo(Statement body, Expression condition) {
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
		StringBuilder format = new StringBuilder("do");
		if(!(body instanceof StatementCompound)) {
			format.append("\t");
		}
		format.append(body.toString());
		format.append("while (");
		format.append(condition.toString());
		format.append(")");
		return format.toString();
	}
	
}
