package lasercompiler.codegen.laserlang;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lasercompiler.codegen.CodeGenerator;
import lasercompiler.codegen.ConstExpressionEvaluator;
import lasercompiler.codegen.GenerationContext;
import lasercompiler.codegen.GenerationException;
import lasercompiler.parser.nodes.BlockItem;
import lasercompiler.parser.nodes.Declaration;
import lasercompiler.parser.nodes.Expression;
import lasercompiler.parser.nodes.ExpressionAssignment;
import lasercompiler.parser.nodes.ExpressionBinaryOperation;
import lasercompiler.parser.nodes.ExpressionBinaryOperation.BinaryOperator;
import lasercompiler.parser.nodes.ExpressionConditional;
import lasercompiler.parser.nodes.ExpressionConstantInteger;
import lasercompiler.parser.nodes.ExpressionFunctionCall;
import lasercompiler.parser.nodes.ExpressionPostfixOperation;
import lasercompiler.parser.nodes.ExpressionPrefixOperation;
import lasercompiler.parser.nodes.ExpressionUnaryOperation;
import lasercompiler.parser.nodes.ExpressionUnaryOperation.UnaryOperator;
import lasercompiler.parser.nodes.ExpressionVariable;
import lasercompiler.parser.nodes.Function;
import lasercompiler.parser.nodes.Program;
import lasercompiler.parser.nodes.Statement;
import lasercompiler.parser.nodes.StatementBreak;
import lasercompiler.parser.nodes.StatementCompound;
import lasercompiler.parser.nodes.StatementContinue;
import lasercompiler.parser.nodes.StatementDo;
import lasercompiler.parser.nodes.StatementExpression;
import lasercompiler.parser.nodes.StatementFor;
import lasercompiler.parser.nodes.StatementIf;
import lasercompiler.parser.nodes.StatementReturn;
import lasercompiler.parser.nodes.StatementWhile;

public class CodeGeneratorLaserLang implements CodeGenerator {

	private final static String HIGHWAY_LABEL = "__FUNC_HIGHWAY__";
	
	private int labelCounter = 0;
	private Map<String, Integer> globalVarsTable;
	private List<String> allowedFunctions;

	/* Stacks:
	 * (TOP)
	 * callerId stack
	 * global vars stack
	 * input stack
	 * register stack (calc stack)
	 * empty stack
	 * Current function stack
	 * caller #1 stack
	 * caller #2 stack
	 * ...
	 * (BOTTOM)
	 */
	
	/*
	 * Convention:
	 * 				We enter into an expression at the register stack, we exit at the same stack
	 * 				We enter into a function at the calle stack (the function has to delete it's own stack, and has to clean its own register stack)
	 * 				Intermediary values are stored at the top of register stack
	 * 				Function return their result by swapping it down into the top of the caller stack
	 * 				There is always a pop right after of a branch to get rid of the boolean result
	 * 				Labels are marked with @LABEL_NAME@, targets of branches are marked with ?LABEL_NAME? and targets of jumps are marked with $LABEL_NAME$
	 * 				Function calls are marked with `TARGET_LABEL` 
	 * 				Function arguments are pushed on the callee stack
	 */
	public String generate(Program prog) throws GenerationException {
		StringBuilder code = new StringBuilder();
		StringBuilder entryCode = new StringBuilder();
		entryCode.append(STACK_UP+STACK_UP); // Create the stack for the caller of main and the stack of main so it can push the return value down
		entryCode.append(STACK_UP+STACK_UP+STACK_UP+STACK_UP);
//		entryCode.append(STACK_UP);
//		entryCode.append(STACK_DOWN);
		entryCode.append("'-1'");
		entryCode.append(STACK_DOWN);
		
		List<Function> functions = prog.getFunctions();
		boolean foundMain = false;
		boolean foundPutChar = false;
		boolean foundPutInt = false;
		boolean foundGetInt = false;
		int mainIndex = -1;
		int putcharIndex = -1;
		int putintIndex = -1;
		int getintIndex = -1;
		for(int i = 0; i < functions.size(); i++) {
			if(functions.get(i).getName().equals("main")) {
				foundMain = true;
				mainIndex = i;
			}else if(functions.get(i).getName().equals("putchar")) {
				if(functions.get(i).getParameters().size() != 1) {
					throw new GenerationException("Failed to generate program, incorrect amount of argument for putchar function in declaration");
				}
				foundPutChar = true;
				putcharIndex = i;
			}else if(functions.get(i).getName().equals("putint")) {
				if(functions.get(i).getParameters().size() != 1) {
					throw new GenerationException("Failed to generate program, incorrect amount of argument for putint function in declaration");
				}
				foundPutInt = true;
				putintIndex = i;
			}else if(functions.get(i).getName().equals("getint")) {
				if(functions.get(i).getParameters().size() != 0) {
					throw new GenerationException("Failed to generate program, incorrect amount of argument for getint function in declaration");
				}
				foundGetInt = true;
				getintIndex = i;
			}
		}
		if(!foundMain) {
			throw new GenerationException("Failed to generate program, no main function");
		}
		
		if(foundPutChar) {
			functions.set(putcharIndex, new Function(functions.get(putcharIndex).getName(), functions.get(putcharIndex).getParameters(), new ArrayList<BlockItem>()));
		}
		if(foundPutInt) {
			functions.set(putintIndex, new Function(functions.get(putintIndex).getName(), functions.get(putintIndex).getParameters(), new ArrayList<BlockItem>()));
		}
		if(foundGetInt) {
			functions.set(getintIndex, new Function(functions.get(getintIndex).getName(), functions.get(getintIndex).getParameters(), new ArrayList<BlockItem>()));
		}
		
		if(mainIndex != 0) { // Make sure main is the first function
			functions.add(0, functions.remove(mainIndex));
		}
		
		
		allowedFunctions = new ArrayList<String>();
		for(int i = 0; i < functions.size(); i++) {
			allowedFunctions.add(functions.get(i).getName());
		}
		
		List<Declaration> globalVars = prog.getGlobalVariables();
		globalVarsTable = new HashMap<String, Integer>();
		
		int varOffset = 0;
		for(Declaration decl : globalVars) {
			globalVarsTable.put(decl.getVariable(), varOffset);
			if(decl.hasInitializer()) {
				Expression initializer = ConstExpressionEvaluator.evalExpression(decl.getInitializer());
				if(!(initializer instanceof ExpressionConstantInteger)) {
					throw new GenerationException("Failed to generate global variable("+decl.getVariable()+"), initial value must be constant");
				}
				ExpressionConstantInteger eci = (ExpressionConstantInteger) initializer;
				entryCode.append("'");
				entryCode.append(eci.getValue());
				entryCode.append("'");
			}else {
				entryCode.append("0");
			}
			varOffset++;
		}
		
		entryCode.append(STACK_DOWN+INPUT_STACK+STACK_DOWN+STACK_DOWN+STACK_DOWN);

		entryCode.append(FORCE_DOWN);
		
		int entryLength = entryCode.length();
		code.append(entryCode);
		code.append("\n ");
		code.append(FORCE_DOWN);
		code.append(paddingOfLength(entryLength-3));
		code.append(FORCE_LEFT);
		code.append("\n");
		
		StringBuilder functionLine = new StringBuilder(" ");
		
		for(Function func : functions) {
			if(func.hasBody()) {
				functionLine.append(generateFunction(func));
				functionLine.append("   ");
			}
		}
				
		Map<String, Integer> labels = new HashMap<String, Integer>();
		Map<Integer, String> jumps = new HashMap<Integer, String>();
		Map<Integer, String> branches = new HashMap<Integer, String>();
		List<Integer> retLabels = new ArrayList<Integer>();
		int linePoint = 0;
		int actualOffset = 0;
		while(linePoint < functionLine.length()) {
			if(functionLine.charAt(linePoint) == '@') {
				linePoint++;
				String labelName = "";
				while(functionLine.charAt(linePoint) != '@') {
					labelName += functionLine.charAt(linePoint);
					linePoint++;
				}
				labels.put(labelName, actualOffset);
			}else if(functionLine.charAt(linePoint) == '$') {
				linePoint++;
				String labelName = "";
				while(functionLine.charAt(linePoint) != '$') {
					labelName += functionLine.charAt(linePoint);
					linePoint++;
				}
				jumps.put(actualOffset, labelName);
			}else if(functionLine.charAt(linePoint) == '?') {
				linePoint++;
				String labelName = "";
				while(functionLine.charAt(linePoint) != '?') {
					labelName += functionLine.charAt(linePoint);
					linePoint++;
				}
				branches.put(actualOffset, labelName);
			}else if(functionLine.charAt(linePoint) == '`'){
				linePoint++;
				String labelName = "";
				while(functionLine.charAt(linePoint) != '`') {
					labelName += functionLine.charAt(linePoint);
					linePoint++;
				}
				// assume we get here lookin at calle stack
				StringBuilder functionCall = new StringBuilder();
				functionCall.append(STACK_UP+STACK_UP+STACK_UP+STACK_UP+STACK_UP);
				functionCall.append("'");
				functionCall.append(retLabels.size());
				functionCall.append("'");
				functionCall.append(STACK_DOWN+STACK_DOWN+STACK_DOWN+STACK_DOWN+STACK_DOWN);
				
				//TODO: same as jumpTo$
				jumps.put(actualOffset+functionCall.length(), labelName);
				functionCall.append(FORCE_DOWN);
				
				retLabels.add(actualOffset+functionCall.length());
				functionCall.append(FORCE_RIGHT);
				functionCall.append(SWAP_UP+STACK_UP+SWAP_UP+STACK_UP);
				code.append(functionCall);
				actualOffset+=functionCall.length();
				
			} else {
				code.append(functionLine.charAt(linePoint));
				actualOffset++;
			}
			linePoint++;
		}		
		code.append("\n");
		
		for(int i = 0; i < actualOffset; i++) {
			if(branches.containsKey(i)) {
				code.append(POP);
			}else {
				code.append(" ");
			}
		}
		code.append("\n");

//		Return "highway"
		int lastOffset = -1;
		for(int i = 0; i < retLabels.size(); i++) {
			int paddingLength = retLabels.get(i)-lastOffset-1;
			code.append(paddingOfLength(paddingLength));
			code.append(POP);
			lastOffset = retLabels.get(i);
		}
		
		
		code.append("\n");
		code.append(FORCE_RIGHT);
		
		int lastBranchOffset = -1;
		for(int i = 0; i < retLabels.size(); i++) {
			int paddingLength = retLabels.get(i)-lastBranchOffset-2;
			code.append(paddingOfLength(paddingLength));
			code.append(BRANCH_UP);
			code.append(DECREMENT);
			lastBranchOffset = retLabels.get(i);
		}
		code.append(paddingOfLength(actualOffset-lastBranchOffset));
		code.append(POP);
		code.append(STACK_UP+STACK_UP+STACK_UP+STACK_UP+STACK_UP+STACK_UP);
		code.append(OUTPUT_STACK);
		code.append(STACK_DOWN+STACK_DOWN+STACK_DOWN+STACK_DOWN+STACK_DOWN+STACK_DOWN);
		code.append(TERMINATE);
		
		code.append("\n");
		
		for(int off : jumps.keySet()) {
			String jumpTarget = jumps.get(off);
			if(jumpTarget.equals(HIGHWAY_LABEL)) {
				code.append(FORCE_UP);
				code.append(paddingOfLength(off-1));
				code.append(FORCE_LEFT);
			}else {
				int labelOff = labels.get(jumpTarget);
				if(off < labelOff) {
					code.append(paddingOfLength(off));
					code.append(FORCE_RIGHT);
					code.append(paddingOfLength(labelOff-off-1));
					code.append(FORCE_UP);
				}else {
					code.append(paddingOfLength(labelOff));
					code.append(FORCE_UP);
					code.append(paddingOfLength(off-labelOff-1));
					code.append(FORCE_LEFT);
				}
			}
			code.append("\n");
		}
		
		for(int off : branches.keySet()) {
			int labelOff = labels.get(branches.get(off));
			if(off < labelOff) {
				code.append(paddingOfLength(off));
				code.append(FORCE_RIGHT);
				code.append(paddingOfLength(labelOff-off-1));
				code.append(FORCE_UP);
			}else {
				code.append(paddingOfLength(labelOff));
				code.append(FORCE_UP);
				code.append(paddingOfLength(off-labelOff-1));
				code.append(FORCE_LEFT);
			}
			code.append("\n");
		}
		
		return code.toString();
	}
	
	private String generateFunction(Function func) throws GenerationException{
		
		StringBuilder code = new StringBuilder();
		GenerationContext contextData = new GenerationContextLaser();
		
		code.append(label(func.getName()));
		
//		code.append(STACK_UP);
		
		for(int i = 0; i < func.getParameters().size(); i++) {
			contextData.addVariable(func.getParameters().get(i), i);
		}	
		
		if(func.getName().equals("putchar")) {
			code.append(INT_TO_CHAR);
			code.append(OUTPUT);
			code.append(generateStatement(new StatementReturn(new ExpressionConstantInteger(0)), contextData));
		}else if(func.getName().equals("putint")) {
//			code.append(SWAP_UP+STACK_UP);
//			code.append(SWAP_UP+STACK_UP);
//			code.append(SWAP_UP+STACK_UP);
//			code.append(SWAP_UP+STACK_UP);
//			code.append(SWAP_UP+STACK_UP);
//			code.append(SWAP_UP);
//			code.append(STACK_DOWN);
//			code.append(STACK_DOWN);
//			code.append(STACK_DOWN);
//			code.append(STACK_DOWN);
//			code.append(STACK_DOWN);
			code.append(OUTPUT);
			code.append(generateStatement(new StatementReturn(new ExpressionConstantInteger(0)), contextData));
		}else if(func.getName().equals("getint")) {
			contextData.addVariable("__chr__");
			code.append(STACK_UP);
			code.append(STACK_UP);
			code.append(STACK_UP);
			code.append(SWAP_DOWN);
			code.append(STACK_DOWN);
			code.append(SWAP_DOWN);
			code.append(STACK_DOWN);
			code.append(SWAP_DOWN);
			code.append(STACK_DOWN);
			code.append(generateStatement(new StatementReturn(new ExpressionVariable("__chr__")), contextData));
		}else {
			code.append(generateBlock(func.getBody(), contextData));
		}
		
		return code.toString();
	}
	
	private String generateBlock(List<BlockItem> items, GenerationContext contextData) throws GenerationException{
		StringBuilder code = new StringBuilder();
		
		for (BlockItem s : items) {
			if(s instanceof Declaration) {
				Declaration dec = (Declaration) s;
				if(dec.hasInitializer()) {
					code.append(STACK_UP);
					code.append(STACK_UP);
					code.append(generateExpression(dec.getInitializer(), contextData.subContext()));
					code.append(SWAP_DOWN);
					code.append(STACK_DOWN);
					code.append(SWAP_DOWN);
					code.append(STACK_DOWN);
				}else {
					code.append("0");
				}
				
				contextData.addVariable(dec.getVariable());
			}else if(s instanceof Statement){
				code.append(generateStatement((Statement) s, contextData.subContext()));
			}else {
				throw new IllegalStateException();
			}
		}
		
		for(int i = 0; i < contextData.getNumVariablesToClear(); i++) {
			code.append(POP);
		}
		
		return code.toString();
	}
	
	private String generateStatement(Statement s, GenerationContext contextData) throws GenerationException{
		StringBuilder code = new StringBuilder();
		if (s instanceof StatementReturn) {
			code.append(STACK_UP);
			code.append(STACK_UP);
			code.append(generateExpression(((StatementReturn)s).getExpression(), contextData));
			code.append(SWAP_DOWN);
			code.append(STACK_DOWN);
			code.append(SWAP_DOWN);
			code.append(STACK_DOWN);
			
			code.append(SWAP_DOWN);
			code.append(POP_STACK); // Does not move us down, currently at empty stack
			code.append(STACK_UP+STACK_UP+STACK_UP+STACK_UP);
			code.append(SWAP_DOWN);
			code.append(STACK_DOWN);
			code.append(SWAP_DOWN);
			code.append(STACK_DOWN);
			code.append(SWAP_DOWN);
			code.append(STACK_DOWN);
			code.append(SWAP_DOWN);
			code.append(STACK_DOWN);
			code.append(SWAP_DOWN);
			code.append(STACK_DOWN);
			code.append(jumpTo(HIGHWAY_LABEL));
		} else if (s instanceof StatementExpression) {
			if(!((StatementExpression) s).isNullExpression()) {
				code.append(STACK_UP);
				code.append(STACK_UP);
				code.append(generateExpression(((StatementExpression) s).getExpression(), contextData));
				code.append(POP);
				code.append(STACK_DOWN);
				code.append(STACK_DOWN);
			}
		} else if (s instanceof StatementIf) {
			StatementIf sif = (StatementIf) s;
			code.append(STACK_UP);
			code.append(STACK_UP);
			code.append(generateExpression(sif.getCondition(), contextData));
			code.append(SWAP_DOWN);
			code.append(STACK_DOWN);
			code.append(SWAP_DOWN);
			code.append(STACK_DOWN);
			
			String falseValueLabel = getUniqueLabel();
			String endLabel = getUniqueLabel();
			code.append(branchTo(falseValueLabel));
			code.append(generateStatement(sif.getIfBody(), contextData));			
			code.append(jumpTo(endLabel));
			
			code.append(label(falseValueLabel));
			if(sif.hasElse()) {
				code.append(generateStatement(sif.getElseBody(), contextData));
			}
			
			code.append(label(endLabel));
		} else if (s instanceof StatementWhile) {
			StatementWhile swhile = (StatementWhile) s;
			String condLabel = getUniqueLabel();
			String endLabel = getUniqueLabel();
			
			code.append(label(condLabel));
			code.append(STACK_UP);
			code.append(STACK_UP);
			code.append(generateExpression(swhile.getCondition(), contextData));
			code.append(SWAP_DOWN);
			code.append(STACK_DOWN);
			code.append(SWAP_DOWN);
			code.append(STACK_DOWN);
			code.append(branchTo(endLabel));
			
			GenerationContext whileContext = contextData.subContext();
			whileContext.setBreakLabel(endLabel);
			whileContext.setContinueLabel(condLabel);
			code.append(generateStatement(swhile.getBody(), whileContext));
			code.append(jumpTo(condLabel));
			code.append(label(endLabel));
		} else if (s instanceof StatementDo) {
			StatementDo sdo = (StatementDo) s;
			String startLabel = getUniqueLabel();
			String condLabel = getUniqueLabel();
			String endLabel = getUniqueLabel();
			
			code.append(label(startLabel));
			
			GenerationContext doContext = contextData.subContext();
			doContext.setBreakLabel(endLabel);
			doContext.setContinueLabel(condLabel);
			code.append(generateStatement(sdo.getBody(), doContext));
			
			code.append(label(condLabel));
			code.append(STACK_UP);
			code.append(STACK_UP);
			code.append(generateExpression(sdo.getCondition(), contextData));
			code.append(SWAP_DOWN);
			code.append(STACK_DOWN);
			code.append(SWAP_DOWN);
			code.append(STACK_DOWN);
			code.append(branchTo(endLabel));
			code.append(jumpTo(startLabel));
			code.append(label(endLabel));
		} else if (s instanceof StatementFor) {
			StatementFor sfor = (StatementFor) s;
			String condLabel = getUniqueLabel();
			String continueLabel = getUniqueLabel();
			String endLabel = getUniqueLabel();
			if(sfor.hasInitialExp()) {
				code.append(STACK_UP);
				code.append(STACK_UP);
				code.append(generateExpression(sfor.getInitialExp(), contextData));
				code.append(POP);
				code.append(STACK_DOWN);
				code.append(STACK_DOWN);
			}
			
			code.append(label(condLabel));
			code.append(STACK_UP);
			code.append(STACK_UP);
			code.append(generateExpression(sfor.getCondition(), contextData));
			code.append(SWAP_DOWN);
			code.append(STACK_DOWN);
			code.append(SWAP_DOWN);
			code.append(STACK_DOWN);
			code.append(branchTo(endLabel));
					
			GenerationContext forContext = contextData.subContext();
			forContext.setBreakLabel(endLabel);
			forContext.setContinueLabel(continueLabel);
			
			code.append(generateStatement(sfor.getBody(), forContext));
			
			code.append(label(continueLabel));			
			if(sfor.hasPostExp()) {
				code.append(STACK_UP);
				code.append(STACK_UP);
				code.append(generateExpression(sfor.getPostExp(), contextData));
				code.append(POP);
				code.append(STACK_DOWN);
				code.append(STACK_DOWN);
			}
			code.append(jumpTo(condLabel));
			code.append(label(endLabel));
		} else if (s instanceof StatementBreak) {
			code.append(jumpTo(contextData.getBreakLabel()));
		} else if (s instanceof StatementContinue) {
			code.append(jumpTo(contextData.getContinueLabel()));
		} else if (s instanceof StatementCompound) {
			code.append(generateBlock(((StatementCompound) s).getItems(), contextData.subContext()));
		} else {
			throw new IllegalStateException();
		}
		return code.toString();
	}

	private String generateExpression(Expression exp, GenerationContext contextData) throws GenerationException{
		StringBuilder code = new StringBuilder();
		if (exp instanceof ExpressionConstantInteger) {
			code.append("'");
			code.append(((ExpressionConstantInteger) exp).getValue());
			code.append("'");
		} else if(exp instanceof ExpressionAssignment) {
			ExpressionAssignment as = (ExpressionAssignment) exp;
			code.append(generateExpression(as.getValue(), contextData));
			code.append(REPLICATE);

			if(contextData.hasVariable(as.getVariable())) {
				code.append(STACK_DOWN+STACK_DOWN);
				code.append(ROTATE_UP);
				for(int i = 0; i < contextData.getVariableOffset(as.getVariable()); i++) {
					code.append(ROTATE_UP);
				}
				code.append(POP);
				code.append(STACK_UP);
				code.append(STACK_UP);
				code.append(SWAP_DOWN);
				code.append(STACK_DOWN);
				code.append(SWAP_DOWN);
				code.append(STACK_DOWN);
				for(int i = 0; i < contextData.getVariableOffset(as.getVariable()); i++) {
					code.append(ROTATE_DOWN);
				}
				code.append(ROTATE_DOWN);
				code.append(STACK_UP);
				code.append(STACK_UP);
			} else {
				code.append(STACK_UP+STACK_UP);
				code.append(ROTATE_UP);
				for(int i = 0; i < globalVarsTable.get(as.getVariable()); i++) {
					code.append(ROTATE_UP);
				}
				code.append(POP);
				code.append(STACK_DOWN);
				code.append(STACK_DOWN);
				code.append(SWAP_UP);
				code.append(STACK_UP);
				code.append(SWAP_UP);
				code.append(STACK_UP);
				for(int i = 0; i < globalVarsTable.get(as.getVariable()); i++) {
					code.append(ROTATE_DOWN);
				}
				code.append(ROTATE_DOWN);
				code.append(STACK_DOWN);
				code.append(STACK_DOWN);
			}
		} else if(exp instanceof ExpressionConditional) {
			ExpressionConditional cond = (ExpressionConditional) exp;

			code.append(generateExpression(cond.getCondition(), contextData));
			
			String falseValueLabel = getUniqueLabel();
			String endLabel = getUniqueLabel();
			code.append(branchTo(falseValueLabel));
			code.append(generateExpression(cond.getTrueValue(), contextData));
			code.append(jumpTo(endLabel));
			
			code.append(label(falseValueLabel));
			code.append(generateExpression(cond.getFalseValue(), contextData));
			
			code.append(label(endLabel));
		} else if (exp instanceof ExpressionFunctionCall) {
			ExpressionFunctionCall efc = (ExpressionFunctionCall) exp;
			if(!allowedFunctions.contains(efc.getFunctionName())) {
				throw new GenerationException("Failed to generate function call("+efc.getFunctionName()+"), did you forget to include a declaration for io functions?");
			}
			
			int argsCount = efc.getArguments().size();
			for(int i = argsCount-1; i >= 0; i--) {
				code.append(generateExpression(efc.getArguments().get(i), contextData));
			}
			
			code.append(STACK_DOWN);
			code.append(REPLICATE_STACK);
			
			for(int i = 0; i < argsCount; i++) {
				code.append(STACK_UP);
				code.append(STACK_UP);
				code.append(SWAP_DOWN);
				code.append(STACK_DOWN);
				code.append(SWAP_DOWN);
				code.append(STACK_DOWN);
			}
			
			code.append(returnLabel(efc.getFunctionName()));
		} else if (exp instanceof ExpressionUnaryOperation) {
			ExpressionUnaryOperation unaryOp = (ExpressionUnaryOperation) exp;
			if(unaryOp.getOperator() != UnaryOperator.LogicalNegation) {
				code.append(generateExpression(unaryOp.getExpression(), contextData));	
			}
			switch (unaryOp.getOperator()) {
			case BitwiseComplement:
				code.append(BITWISE_NOT);
				break;
			case LogicalNegation:
				code.append(generateExpression(new ExpressionConditional(unaryOp.getExpression(), new ExpressionConstantInteger(0), new ExpressionConstantInteger(1)), contextData));
				break;
			case Negation:
				code.append("'-1'");
				code.append(MULTIPLY);
				break;
			default:
				throw new IllegalStateException();
			}
		} else if(exp instanceof ExpressionVariable) {
			ExpressionVariable av = (ExpressionVariable) exp;
			if(contextData.hasVariable(av.getVariable())) {
				code.append(STACK_DOWN+STACK_DOWN);
				code.append(ROTATE_UP);
				for(int i = 0; i < contextData.getVariableOffset(av.getVariable()); i++) {
					code.append(ROTATE_UP);
				}
				code.append(REPLICATE);
				code.append(SWAP_UP);
				for(int i = 0; i < contextData.getVariableOffset(av.getVariable()); i++) {
					code.append(ROTATE_DOWN);
				}
				code.append(ROTATE_DOWN);
				code.append(STACK_UP);
				code.append(SWAP_UP);
				code.append(STACK_UP);
			}else {
				code.append(STACK_UP+STACK_UP);
				code.append(ROTATE_UP);
				for(int i = 0; i < globalVarsTable.get(av.getVariable()); i++) {
					code.append(ROTATE_UP);
				}
				code.append(REPLICATE);
				code.append(SWAP_DOWN);
				for(int i = 0; i < globalVarsTable.get(av.getVariable()); i++) {
					code.append(ROTATE_DOWN);
				}
				code.append(ROTATE_DOWN);
				code.append(STACK_DOWN);
				code.append(SWAP_DOWN);
				code.append(STACK_DOWN);
			}
		} else if (exp instanceof ExpressionPrefixOperation) {
			ExpressionPrefixOperation prefixOp = (ExpressionPrefixOperation) exp;
		
			switch(prefixOp.getOperator()) {
			case Decrement:
				code.append(
						generateExpression(
								new ExpressionAssignment(prefixOp.getVariableExpression().getVariable(),
										new ExpressionBinaryOperation(BinaryOperator.Subtraction,
												prefixOp.getVariableExpression(), new ExpressionConstantInteger(1))),
								contextData));
				break;
			case Increment:
				code.append(
						generateExpression(
								new ExpressionAssignment(prefixOp.getVariableExpression().getVariable(),
										new ExpressionBinaryOperation(BinaryOperator.Addition,
												prefixOp.getVariableExpression(), new ExpressionConstantInteger(1))),
								contextData));
				break;
			default:
				throw new IllegalStateException();
			}
		} else if (exp instanceof ExpressionPostfixOperation) {
			ExpressionPostfixOperation postfixOp = (ExpressionPostfixOperation) exp;
			
			code.append(generateExpression(postfixOp.getVariableExpression(), contextData));
			
			switch(postfixOp.getOperator()) {
			case Decrement:
				code.append(
						generateExpression(
								new ExpressionAssignment(postfixOp.getVariableExpression().getVariable(),
										new ExpressionBinaryOperation(BinaryOperator.Subtraction,
												postfixOp.getVariableExpression(), new ExpressionConstantInteger(1))),
								contextData));
				break;
			case Increment:
				code.append(
						generateExpression(
								new ExpressionAssignment(postfixOp.getVariableExpression().getVariable(),
										new ExpressionBinaryOperation(BinaryOperator.Addition,
												postfixOp.getVariableExpression(), new ExpressionConstantInteger(1))),
								contextData));
				break;
			default:
				throw new IllegalStateException();
			}
			code.append(POP);
		} else if (exp instanceof ExpressionBinaryOperation) {
			ExpressionBinaryOperation binaryOp = (ExpressionBinaryOperation) exp;
			
			if(binaryOp.getOperator() == BinaryOperator.BitwiseXor) {
				Expression termA = new ExpressionBinaryOperation(BinaryOperator.BitwiseAnd, binaryOp.getLeft(),
						new ExpressionUnaryOperation(UnaryOperator.BitwiseComplement, binaryOp.getRight()));
				Expression termB = new ExpressionBinaryOperation(BinaryOperator.BitwiseAnd, binaryOp.getRight(),
						new ExpressionUnaryOperation(UnaryOperator.BitwiseComplement, binaryOp.getLeft()));
				Expression expXor = new ExpressionBinaryOperation(BinaryOperator.BitwiseOr, termA, termB);
				code.append(generateExpression(expXor, contextData));
			} else if(binaryOp.getOperator() == BinaryOperator.GreaterThanEq) {
				code.append(generateExpression(new ExpressionBinaryOperation(BinaryOperator.LogicalOr,
						new ExpressionBinaryOperation(BinaryOperator.Equal, binaryOp.getLeft(), binaryOp.getRight()),
						new ExpressionBinaryOperation(BinaryOperator.GreaterThan, binaryOp.getLeft(),
								binaryOp.getRight())),
						contextData));
			} else if(binaryOp.getOperator() == BinaryOperator.LessThanEq) {
				code.append(generateExpression(new ExpressionBinaryOperation(BinaryOperator.LogicalOr,
						new ExpressionBinaryOperation(BinaryOperator.Equal, binaryOp.getLeft(), binaryOp.getRight()),
						new ExpressionBinaryOperation(BinaryOperator.LessThan, binaryOp.getLeft(),
								binaryOp.getRight())),
						contextData));
			} else if(binaryOp.getOperator() == BinaryOperator.NotEqual) {
				code.append(generateExpression(new ExpressionUnaryOperation(UnaryOperator.LogicalNegation,
						new ExpressionBinaryOperation(BinaryOperator.Equal, binaryOp.getLeft(), binaryOp.getRight())),
						contextData));
			} else if(binaryOp.getOperator() == BinaryOperator.LogicalAnd) {
				code.append(generateExpression(
						new ExpressionConditional(binaryOp.getLeft(),
								new ExpressionConditional(binaryOp.getRight(), new ExpressionConstantInteger(1),
										new ExpressionConstantInteger(0)),
								new ExpressionConstantInteger(0)),
						contextData));
			} else if(binaryOp.getOperator() == BinaryOperator.LogicalOr) {
				code.append(
						generateExpression(new ExpressionConditional(binaryOp.getLeft(),
								new ExpressionConstantInteger(1), new ExpressionConditional(binaryOp.getRight(),
										new ExpressionConstantInteger(1), new ExpressionConstantInteger(0))),
								contextData));
			} else {
				code.append(generateExpression(binaryOp.getLeft(), contextData));
				if (binaryOp.getOperator() != BinaryOperator.LogicalAnd
						&& binaryOp.getOperator() != BinaryOperator.LogicalOr
						&& binaryOp.getOperator() != BinaryOperator.Comma
						&& binaryOp.getOperator() != BinaryOperator.ShiftLeft
						&& binaryOp.getOperator() != BinaryOperator.ShiftRight) {
					code.append(generateExpression(binaryOp.getRight(), contextData));
				}
	
				switch (binaryOp.getOperator()) {
				case Comma:
					code.append(generateExpression(binaryOp.getRight(), contextData));
					break;
				case Equal:
					code.append(EQUALS);
					break;
				case GreaterThan:
					code.append(GREATER_THAN);
					break;
				case GreaterThanEq:
				case LessThanEq:
				case NotEqual:
				case LogicalAnd:
				case LogicalOr:
					throw new IllegalStateException();
				case LessThan:
					code.append(LESS_THAN);
					break;
				case Addition:
					code.append(ADD);
					break;
				case Division:
					code.append(DIVIDE);
					break;
				case Multiplication:
					code.append(MULTIPLY);
					break;
				case Subtraction:
					code.append(SUBTRACT);
					break;
				case BitwiseAnd:
					code.append(BITWISE_AND);
					break;
				case BitwiseOr:
					code.append(BITWISE_OR);
					break;
				case BitwiseXor:
					throw new IllegalStateException();
				case Modulo:
					code.append(MODULO);
					break;
				case Power:
					code.append(POWER);
					break;
				case ShiftLeft:
					code.append(generateExpression(new ExpressionBinaryOperation(BinaryOperator.Power,
							new ExpressionConstantInteger(2), binaryOp.getRight()), contextData));
					code.append(MULTIPLY);
					break;
				case ShiftRight:
					code.append(generateExpression(new ExpressionBinaryOperation(BinaryOperator.Power,
							new ExpressionConstantInteger(2), binaryOp.getRight()), contextData));
					code.append(DIVIDE);
					break;
				default:
					throw new IllegalStateException();
				}
			}
		} else {
			throw new IllegalStateException();
		}
		return code.toString();
	}

	private String getUniqueLabel() {
		return "label" + (labelCounter++);
	}
	
	private static String jumpTo(String s) {
		return "$"+s+"$"+FORCE_DOWN;
	}
	
	private static String branchTo(String s) {
		return "?"+s+"?"+BRANCH_DOWN+POP;
	}
	
	private static String label(String s) {
		return "@"+s+"@"+FORCE_RIGHT;
	}
	
	private static String returnLabel(String s) {
		return "`"+s+"`";
	}
	
	private static String paddingOfLength(int length) {
		if(length<0) throw new IllegalStateException();
		StringBuilder pad = new StringBuilder();
		for(int i = 0; i < length; i++) {
			pad.append(" ");
		}
		return pad.toString();
	}
	
	public final static String MIRROR_MAIN = "\\";
	public final static String MIRROR_SEC = "/";
	public final static String FORCE_RIGHT = ">";
	public final static String FORCE_DOWN = "v";
	public final static String FORCE_LEFT = "<";
	public final static String FORCE_UP = "^";
	public final static String BRANCH_UP = "⌟";
	public final static String BRANCH_DOWN = "⌝";
	public final static String DECREMENT = "(";
	public final static String INCREMENT = ")";
	public final static String CARDINALITY = "c";
	public final static String REPLICATE = "r";
	public final static String REPLICATE_STACK = "R";
	public final static String LOGICAL_NOT = "!";
	public final static String BITWISE_NOT = "~";
	public final static String POP = "p";
	public final static String POP_STACK = "P";
	public final static String OUTPUT = "o";
	public final static String OUTPUT_STACK = "O";
	public final static String INT_TO_CHAR = "b";
	public final static String STRING_TO_INTS = "n";
	public final static String INTS_TO_STRING = "B";
	public final static String ADD = "+";
	public final static String SUBTRACT = "-";
	public final static String MULTIPLY = "×";
	public final static String DIVIDE = "÷";
	public final static String POWER = "*";
	public final static String GREATER_THAN = "g";
	public final static String LESS_THAN = "l";
	public final static String EQUALS = "=";
	public final static String BITWISE_AND = "&";
	public final static String BITWISE_OR = "|";
	public final static String MODULO = "%";
	public final static String STACK_UP = "U";
	public final static String STACK_DOWN = "D";
	public final static String ROTATE_UP = "u";
	public final static String ROTATE_DOWN = "d";
	public final static String SWAP_UP = "s";
	public final static String SWAP_DOWN = "w";
	public final static String INPUT = "i";
	public final static String INPUT_STACK = "I";
	public final static String TERMINATE = "#";

}
