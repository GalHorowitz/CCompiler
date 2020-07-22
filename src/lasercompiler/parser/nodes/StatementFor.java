package lasercompiler.parser.nodes;

public class StatementFor extends Statement {
	
	private Expression initialExp;
	private final boolean hasInitialExp;
	private final Expression condition;
	private Expression postExp;
	private final boolean hasPostExp;
	private final Statement body;
	
	public StatementFor(Expression initalExp, Expression condition, Expression postExp, Statement body) {
		this.hasInitialExp = (initalExp != null);
		this.initialExp = initalExp;
		this.condition = condition;
		this.postExp = postExp;
		this.hasPostExp = (postExp != null);
		this.body = body;
	}
	
	public Expression getInitialExp() {
		return initialExp;
	}
	
	public Expression getCondition() {
		return condition;
	}
	
	public Expression getPostExp() {
		return postExp;
	}
	
	public Statement getBody() {
		return body;
	}
	
	public boolean hasInitialExp() {
		return hasInitialExp;
	}
	
	public boolean hasPostExp() {
		return hasPostExp;
	}
	
	@Override
	public String toString() {
		StringBuilder format = new StringBuilder("for (");
		if(hasInitialExp) {
			format.append(initialExp.toString());
		}
		format.append("; ");
		format.append(condition.toString());
		format.append("; ");
		if(hasPostExp) {
			format.append(postExp.toString());
		}
		format.append(")\n");
		if(!(body instanceof StatementCompound)) {
			format.append("\t");
		}
		format.append(body.toString());
		return format.toString();
	}
	
}
