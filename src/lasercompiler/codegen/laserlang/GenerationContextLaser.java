package lasercompiler.codegen.laserlang;

import java.util.HashMap;

import lasercompiler.codegen.GenerationContext;

public class GenerationContextLaser extends GenerationContext {
	
	public GenerationContextLaser() {
		super();
		stackIndex = 0;
	}
	
	@Override
	public GenerationContext subContext() {
		GenerationContextLaser c = new GenerationContextLaser();
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
		stackIndex += count;
		return curStack;
	}

}
