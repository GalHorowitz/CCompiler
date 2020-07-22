package lasercompiler.parser.nodes;

public class ExpressionConstantInteger extends Expression {
	
	private final int value;
	
	public ExpressionConstantInteger(Integer value) {
		this.value = value;
	}
	
	public int getValue() {
		return value;
	}
	
	@Override
	public String toString() {
		return String.valueOf(value);
	}

}
