package lasercompiler.parser.nodes;

import java.util.List;

public class Program {
	private final List<Function> functions;
	private final List<Declaration> globalVariables;
	
	public Program(List<Function> functions, List<Declaration> globalVariables) {
		this.functions = functions;
		this.globalVariables = globalVariables;
	}
	
	public List<Function> getFunctions() {
		return functions;
	}
	
	public List<Declaration> getGlobalVariables() {
		return globalVariables;
	}
	
	@Override
	public String toString() {
		StringBuilder format = new StringBuilder();
		for(Declaration d : globalVariables) {
			format.append(d.toString());
			format.append("\n");
		}
		
		int i = 0;
		for(Function func : functions) {
			format.append(func.toString());
			if(i < functions.size()-1) {
				format.append("\n");
			}
			i++;
		}
		
		return format.toString();
	}
	
}
