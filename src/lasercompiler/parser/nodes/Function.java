package lasercompiler.parser.nodes;

import java.util.List;

import lasercompiler.parser.PrettyPrint;

public class Function {

	private final String name;
	private final List<String> parameters;
	private List<BlockItem> body;
	private final boolean hasBody;

	public Function(String name, List<String> parameters, List<BlockItem> body) {
		this.name = name;
		this.parameters = parameters;
		this.body = body;
		this.hasBody = true;
	}

	public Function(String name, List<String> parameters) {
		this.name = name;
		this.parameters = parameters;
		this.hasBody = false;
	}

	public String getName() {
		return name;
	}

	public List<String> getParameters() {
		return parameters;
	}

	public List<BlockItem> getBody() {
		return body;
	}
	
	public boolean hasBody() {
		return hasBody;
	}

	@Override
	public String toString() {
		StringBuilder format = new StringBuilder("int ");
		format.append(name);
		format.append("(");
		for (int i = 0; i < parameters.size(); i++) {
			format.append("int ");
			format.append(parameters.get(i));
			if (i < parameters.size() - 1) {
				format.append(", ");
			}
		}
		format.append(")");
		if(hasBody) {
			format.append(":\n");
			for (BlockItem s : body) {
				format.append(PrettyPrint.tabLines(s.toString()));
			}
		}

		return format.toString();
	}

}
