package lasercompiler.parser.nodes;

import java.util.List;

public class ExpressionFunctionCall extends Expression {

	private final String functionName;
	private final List<Expression> arguments;

	public ExpressionFunctionCall(String functionName, List<Expression> arguments) {
		this.functionName = functionName;
		this.arguments = arguments;
	}

	public String getFunctionName() {
		return functionName;
	}

	public List<Expression> getArguments() {
		return arguments;
	}

	@Override
	public String toString() {
		StringBuilder format = new StringBuilder(functionName);
		format.append("(");
		for (int i = 0; i < arguments.size(); i++) {
			format.append(arguments.get(i).toString());
			if (i < arguments.size() - 1) {
				format.append(", ");
			}
		}
		format.append(")");
		return format.toString();
	}

}
