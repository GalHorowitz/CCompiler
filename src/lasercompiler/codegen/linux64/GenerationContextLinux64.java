package lasercompiler.codegen.linux64;

import java.util.HashMap;

import lasercompiler.codegen.GenerationContext;

public class GenerationContextLinux64 extends GenerationContext {

	public GenerationContextLinux64() {
		super();
		stackIndex = -8;
	}
	
	@Override
	public GenerationContext subContext() {
		GenerationContextLinux64 c = new GenerationContextLinux64();
		c.stackIndex = stackIndex;
		c.variableMap = new HashMap<String, Integer>(variableMap);
		c.breakLabel = breakLabel;
		c.continueLabel = continueLabel;
		c.numVariablesToClear = 0;
		return c;
	}
		
	@Override
	public int pushStackIndex(int count) {
		int curStack = stackIndex;
		stackIndex -= 8 * count;
		return curStack;
	}

}
