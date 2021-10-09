package lasercompiler.parser.nodes;

public class ExpressionVariable extends ExpressionLValue {

	private final String variable;
	
	public ExpressionVariable(String variable) {
		this.variable = variable;
	}
	
	public String getIdentifier() {
		return variable;
	}
	
	@Override
	public String toString() {
		return variable;
	}

}
