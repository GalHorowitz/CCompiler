package lasercompiler;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import lasercompiler.codegen.CodeGenerator;
import lasercompiler.codegen.GenerationException;
import lasercompiler.codegen.laserlang.CodeGeneratorLaserLang;
import lasercompiler.lexer.LexException;
import lasercompiler.lexer.Lexer;
import lasercompiler.lexer.tokens.Token;
import lasercompiler.parser.ParseException;
import lasercompiler.parser.Parser;
import lasercompiler.parser.nodes.Program;

public class Main {

	public static void main(String[] args)
			throws IOException, ParseException, InterruptedException, GenerationException, LexException {
		if (args.length < 1 || !args[0].endsWith(".c")) {
			System.out.println("C Filename required.");
			System.exit(1);
		}
		boolean printDebugMessages = args.length == 2;
		boolean laser = true;

		Path sourcePath = FileSystems.getDefault().getPath(args[0]);
		if (!sourcePath.toFile().exists()) {
			System.out.println("File does not exist.");
			System.exit(1);
		}

		String source = new String(Files.readAllBytes(sourcePath));
		List<Token> tokens = Lexer.lex(source);
		if (printDebugMessages) {
			System.out.print("Tokens: ");
			System.out.println(tokens);
		}

		Program prog = Parser.parseProgram(tokens);

		if (printDebugMessages) {
			System.out.println("======= AST: ");
			System.out.println(prog);
			System.out.print("\n");
		}

		CodeGenerator codeGen = new CodeGeneratorLaserLang();
		
		String code = codeGen.generate(prog);

		if (printDebugMessages) {
			System.out.println("======= ASM CODE:");
			System.out.println(code);

			if(!laser) {
				System.out.println("Running gcc on generated assembly...");
			}
		}

		if(!laser) {
			File tempFile = File.createTempFile(sourcePath.getFileName().toString(), ".s");
			tempFile.deleteOnExit();
			FileWriter writer = new FileWriter(tempFile);
			writer.write(code);
			writer.close();
	
	//		System.out.println(args[0].substring(0, args[0].length() - 2));
			ProcessBuilder process = new ProcessBuilder("gcc", tempFile.getCanonicalPath(), "-o",
					args[0].substring(0, args[0].length() - 2));
			if (printDebugMessages) {
				process.inheritIO();
			} else {
				process.redirectErrorStream(true);
			}
			process.start().waitFor();
		}else {
			Files.write(Paths.get("output.lsr"), code.getBytes());
		}
	}
}
