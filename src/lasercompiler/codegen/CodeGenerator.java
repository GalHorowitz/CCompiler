package lasercompiler.codegen;

import lasercompiler.parser.nodes.Program;

public interface CodeGenerator {

	public String generate(Program prog) throws GenerationException;
	
}
