package lasercompiler.parser.nodes;

public class StatementIf extends Statement {
	
	private final Expression condition;
	private final Statement ifBody;
	private final boolean hasElse;
	private Statement elseBody;
	
	public StatementIf(Expression condition, Statement ifBody) {
		this.condition = condition;
		this.ifBody = ifBody;
		this.hasElse = false;
	}

	public StatementIf(Expression condition, Statement ifBody, Statement elseBody) {
		this.condition = condition;
		this.ifBody = ifBody;
		this.hasElse = true;
		this.elseBody = elseBody;
	}
	
	public Expression getCondition() {
		return condition;
	}
	
	public Statement getIfBody() {
		return ifBody;
	}
	
	public Statement getElseBody() {
		return elseBody;
	}
	
	public boolean hasElse() {
		return hasElse;
	}
	
	@Override
	public String toString() {
		StringBuilder format = new StringBuilder("if (");
		format.append(condition.toString());
		format.append(")\n");
		if(!(ifBody instanceof StatementCompound)) {
			format.append("\t");
		}
		format.append(ifBody.toString());
		if(hasElse) {
			format.append("\nelse\n");
			if(!(elseBody instanceof StatementCompound)) {
				format.append("\t");
			}
			format.append(elseBody.toString());
		}
		
		return format.toString();
	}
	
}
