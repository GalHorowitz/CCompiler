package lasercompiler.validator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lasercompiler.parser.nodes.Function;

public class ValidationContext {

	private List<String> scopedVariables;
	private List<String> availableVariables;
	private Map<String, Function> functions;
	private List<String> globalVariables;
	private boolean insideLoop;
	
	public ValidationContext(Map<String, Function> functions, List<String> globalVariables) {
		this.scopedVariables = new ArrayList<String>();
		this.availableVariables = new ArrayList<String>();
		this.functions = functions;
		this.globalVariables = globalVariables;
		this.insideLoop = false;
	}
	
	public ValidationContext(ValidationContext other) {
		this.scopedVariables = new ArrayList<String>();
		this.availableVariables = new ArrayList<String>(other.availableVariables);
		this.functions = new HashMap<String, Function>(other.functions);
		this.globalVariables = new ArrayList<String>(other.globalVariables);
		this.insideLoop = other.insideLoop;
	}
	
	public boolean isInsideLoop() {
		return insideLoop;
	}
	
	public void setInsideLoop(boolean insideLoop) {
		this.insideLoop = insideLoop;
	}
	
	public void addVariable(String var) {
		this.scopedVariables.add(var);
		this.availableVariables.add(var);
	}
	
	public boolean hasVariable(String var) {
		return this.availableVariables.contains(var);
	}
	
	public boolean canDeclareVariable(String var) {
		return !this.scopedVariables.contains(var);
	}
	
	public Map<String, Function> getFunctions() {
		return functions;
	}
	
	public List<String> getGlobalVariables() {
		return globalVariables;
	}
	
}
