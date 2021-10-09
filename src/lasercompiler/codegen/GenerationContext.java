package lasercompiler.codegen;

import java.util.HashMap;
import java.util.Map;

public abstract class GenerationContext {

	protected int stackIndex;
	protected Map<String, Integer> variableMap;
	protected String breakLabel;
	protected String continueLabel;
	protected int numVariablesToClear;

	public GenerationContext() {
//		stackIndex = -8;
		variableMap = new HashMap<String, Integer>();
		breakLabel = null;
		continueLabel = null;
		numVariablesToClear = 0;
	}
	
	public abstract GenerationContext subContext();

	public int getStackIndex() {
		return stackIndex;
	}

	public void setStackIndex(int stackIndex) {
		this.stackIndex = stackIndex;
	}

//	/**
//	 * 'Push' an 8 byte value onto the stack
//	 * 
//	 * @return the stack index
//	 */
//	public int pushStackIndex() {
//		stackIndex -= elementSize;
//		return stackIndex + elementSize;
//	}
	/**
	 * 'Push' a value of size `count` onto the stack (update stackIndex)
	 * @return the stack index of the new value
	 */
	public abstract int pushStackIndex(int count);

	public void addVariable(String var) {
		addVariable(var, pushStackIndex(1));
	}

	public void addVariable(String var, int offset) {
		variableMap.put(var, offset);
		numVariablesToClear++;
	}
	
	public void addVariable(String var, int offset, boolean dontClear) {
		variableMap.put(var, offset);
		if(!dontClear) numVariablesToClear++;
	}

	public void addArray(String arr, int size) {
		variableMap.put(arr, pushStackIndex(size));
		numVariablesToClear += size;
	}

	public int getVariableOffset(String var) {
		return variableMap.get(var);
	}

	public boolean hasVariable(String var) {
		return variableMap.containsKey(var);
	}

	public void setBreakLabel(String breakLabel) {
		this.breakLabel = breakLabel;
	}
	
	public void setContinueLabel(String continueLabel) {
		this.continueLabel = continueLabel;
	}
	
	public String getBreakLabel() {
		return breakLabel;
	}
	
	public String getContinueLabel() {
		return continueLabel;
	}
	
	public int getNumVariablesToClear() {
		return numVariablesToClear;
	}

}
