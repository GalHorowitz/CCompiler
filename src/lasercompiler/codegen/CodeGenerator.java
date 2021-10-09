package lasercompiler.codegen;

import lasercompiler.parser.nodes.Program;

public interface CodeGenerator {

	String generate(Program prog) throws GenerationException;
	
}
