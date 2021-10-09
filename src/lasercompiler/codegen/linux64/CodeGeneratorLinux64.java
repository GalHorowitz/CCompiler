package lasercompiler.codegen.linux64;

import java.util.List;

import lasercompiler.codegen.CodeGenerator;
import lasercompiler.codegen.ConstExpressionEvaluator;
import lasercompiler.codegen.GenerationContext;
import lasercompiler.codegen.GenerationException;
import lasercompiler.parser.nodes.*;
import lasercompiler.parser.nodes.ExpressionBinaryOperation.BinaryOperator;

public class CodeGeneratorLinux64 implements CodeGenerator {

	private int labelCounter = 0;

	public String generate(Program prog) throws GenerationException {
		StringBuilder code = new StringBuilder(t(".intel_syntax noprefix"));
		
		List<Function> functions = prog.getFunctions();
		boolean foundMain = false;
		for(Function func : functions) {
			if(func.getName().equals("main")) {
				foundMain = true;
				break;
			}
		}
		if(!foundMain) {
			throw new GenerationException("Failed to generate program, no main function");
		}
		
		List<Declaration> globalVars = prog.getGlobalVariables();

		for(Declaration decl : globalVars) {
			code.append(t(".globl "+decl.getIdentifier()));
			if(decl.hasInitializer()) {
				code.append(t(".data"));
				code.append(t(".align 4"));
				code.append(decl.getIdentifier()).append(":\n");
				if(!(decl instanceof DeclarationVariable)) {
					throw new GenerationException("Array initializer not yet implemented");
				}
				Expression initializer = ConstExpressionEvaluator.evalExpression(decl.getInitializer());
				if(!(initializer instanceof ExpressionConstantInteger)) {
					throw new GenerationException("Failed to generate global variable("+decl.getIdentifier()+"), initial value must be constant");
				}
				ExpressionConstantInteger eci = (ExpressionConstantInteger) initializer;
				code.append(t(".long "+eci.getValue()));
			}else {
				code.append(t(".bss"));
				code.append(t(".align 4"));
				code.append(decl.getIdentifier()).append(":\n");
				if(decl instanceof DeclarationVariable) {
					code.append(t(".zero 4"));
				} else if(decl instanceof DeclarationArray) {
					int bytes = ((DeclarationArray) decl).getSize() * 4;
					code.append(t(".zero "+bytes));
				} else {
					throw new IllegalStateException();
				}
			}
		}
		
		code.append(t(".text"));
		for(Function func : functions) {
			if(func.hasBody()) {
				code.append(t(".globl "+func.getName()));
				code.append(generateFunction(func));
			}
		}
		
		return code.toString();
	}
	
	/*
	 * We use the x86-64 System V user-space Function Calling convention, which is used by x64 Linux systems.
	 */
	private String generateFunction(Function func){
		
		StringBuilder code = new StringBuilder();
		GenerationContext contextData = new GenerationContextLinux64();
		
		code.append(func.getName());
		code.append(":\n");
		code.append(t("push rbp"));
		code.append(t("mov rbp, rsp"));
		
		int paramCount = func.getParameters().size(); 
		if(paramCount>0) {
			code.append(t("push rdi"));
			contextData.addVariable(func.getParameters().get(0));
		}
		if(paramCount>1) {
			code.append(t("push rsi"));
			contextData.addVariable(func.getParameters().get(1));
		}
		if(paramCount>2) {
			code.append(t("push rdx"));
			contextData.addVariable(func.getParameters().get(2));
		}
		if(paramCount>3) {
			code.append(t("push rcx"));
			contextData.addVariable(func.getParameters().get(3));
		}
		if(paramCount>4) {
			code.append(t("push r8"));
			contextData.addVariable(func.getParameters().get(4));
		}
		if(paramCount>5) {
			code.append(t("push r9"));
			contextData.addVariable(func.getParameters().get(5));
		}
		int paramOffset = 16;
		for(int i = 6; i < paramCount; i++) {
			contextData.addVariable(func.getParameters().get(i), paramOffset, true);
			paramOffset += 8;
		}
		
		code.append(generateBlock(func.getBody(), contextData));
		
		return code.toString();
	}
	
	private String generateBlock(List<BlockItem> items, GenerationContext contextData){
		
		StringBuilder code = new StringBuilder();
		for (BlockItem s : items) {
			if(s instanceof Declaration) {
				if (s instanceof DeclarationVariable) {
					DeclarationVariable dec = (DeclarationVariable) s;
					if(dec.hasInitializer()) {
						code.append(generateExpression(dec.getInitializer(), contextData.subContext()));
					}
					code.append(t("push rax")); // We will be pushing garbage if there is no initialization, but we need to make space for the variable.

					contextData.addVariable(dec.getIdentifier());
				} else if(s instanceof DeclarationArray) {
					DeclarationArray dec = (DeclarationArray) s;
					int bytes = dec.getSize() * 4;
					code.append(t("sub rsp, "+bytes));

					contextData.addArray(dec.getIdentifier(), dec.getSize());
				} else {
					throw new IllegalStateException();
				}
			}else if(s instanceof Statement){
				code.append(generateStatement((Statement) s, contextData.subContext()));
			}else {
				throw new IllegalStateException();
			}
		}
		
		int bytesToDeallocate = 8 * contextData.getNumVariablesToClear();
		if(bytesToDeallocate != 0) {
			code.append(t("add rsp, "+bytesToDeallocate));
		}
		
		return code.toString();
	}
	
	private String generateStatement(Statement s, GenerationContext contextData){
		StringBuilder code = new StringBuilder();
		if (s instanceof StatementReturn) {
			code.append(generateExpression(((StatementReturn)s).getExpression(), contextData));
			code.append(t("mov rsp, rbp"));
			code.append(t("pop rbp"));
			code.append(t("ret"));
		} else if (s instanceof StatementExpression) {
			if(!((StatementExpression) s).isNullExpression()) {
				code.append(generateExpression(((StatementExpression) s).getExpression(), contextData));
			}
		} else if (s instanceof StatementIf) {
			StatementIf sif = (StatementIf) s;
			code.append(generateExpression(sif.getCondition(), contextData));
			
			if(sif.hasElse()) {
				String falseValueLabel = getUniqueLabel();
				String endLabel = getUniqueLabel();
				code.append(t("cmp eax, 0"));
				code.append(t("je "+falseValueLabel));
				code.append(generateStatement(sif.getIfBody(), contextData));
				code.append(t("jmp "+endLabel));
				code.append(falseValueLabel+":\n");
				code.append(generateStatement(sif.getElseBody(), contextData));
				code.append(endLabel+":\n");
			}else {
				String endLabel = getUniqueLabel();
				code.append(t("cmp eax, 0"));
				code.append(t("je "+endLabel));
				code.append(generateStatement(sif.getIfBody(), contextData));
				code.append(endLabel+":\n");
			}
		} else if (s instanceof StatementWhile) {
			StatementWhile swhile = (StatementWhile) s;
			String condLabel = getUniqueLabel();
			String endLabel = getUniqueLabel();
			code.append(condLabel+":\n");
			code.append(generateExpression(swhile.getCondition(), contextData));
			code.append(t("cmp eax, 0"));
			code.append(t("je "+endLabel));
			
			GenerationContext whileContext = contextData.subContext();
			whileContext.setBreakLabel(endLabel);
			whileContext.setContinueLabel(condLabel);
			
			code.append(generateStatement(swhile.getBody(), whileContext));
			code.append(t("jmp "+condLabel));
			code.append(endLabel+":\n");
		} else if (s instanceof StatementDo) {
			StatementDo sdo = (StatementDo) s;
			String startLabel = getUniqueLabel();
			String condLabel = getUniqueLabel();
			String endLabel = getUniqueLabel();
			code.append(startLabel+":\n");
			
			GenerationContext doContext = contextData.subContext();
			doContext.setBreakLabel(endLabel);
			doContext.setContinueLabel(condLabel);
			
			code.append(generateStatement(sdo.getBody(), doContext));
			code.append(condLabel+":\n");
			code.append(generateExpression(sdo.getCondition(), contextData));
			code.append(t("cmp eax, 0"));
			code.append(t("jne "+startLabel));
			code.append(endLabel+":\n");
		} else if (s instanceof StatementFor) {
			StatementFor sfor = (StatementFor) s;
			String condLabel = getUniqueLabel();
			String continueLabel = getUniqueLabel();
			String endLabel = getUniqueLabel();
			if(sfor.hasInitialExp()) {
				code.append(generateExpression(sfor.getInitialExp(), contextData));
			}
			code.append(condLabel+":\n");
			code.append(generateExpression(sfor.getCondition(), contextData));
			code.append(t("cmp eax, 0"));
			code.append(t("je "+endLabel));
			
			GenerationContext forContext = contextData.subContext();
			forContext.setBreakLabel(endLabel);
			forContext.setContinueLabel(continueLabel);
			
			code.append(generateStatement(sfor.getBody(), forContext));
			
			code.append(continueLabel+":\n");			
			if(sfor.hasPostExp()) {
				code.append(generateExpression(sfor.getPostExp(), contextData));
			}
			code.append(t("jmp "+condLabel));
			code.append(endLabel+":\n");
		} else if (s instanceof StatementBreak) {
//			if(contextData.getBreakLabel() == null) {
//				throw new GenerationException("Failed to generate break statement, not inside a loop");
//			}
			code.append(t("jmp "+contextData.getBreakLabel()));
		} else if (s instanceof StatementContinue) {
//			if(contextData.getContinueLabel() == null) {
//				throw new GenerationException("Failed to generate continue statement, not inside a loop");
//			}
			code.append(t("jmp "+contextData.getContinueLabel()));
		} else if (s instanceof StatementCompound) {
			code.append(generateBlock(((StatementCompound) s).getItems(), contextData.subContext()));
		} else {
			throw new IllegalStateException();
		}
		return code.toString();
	}

	private String generateExpression(Expression exp, GenerationContext contextData){
		StringBuilder code = new StringBuilder();
		if (exp instanceof ExpressionConstantInteger) {
			code.append("\tmov eax, ");
			code.append(((ExpressionConstantInteger) exp).getValue());
			code.append("\n");
		} else if(exp instanceof ExpressionAssignment) {
			ExpressionAssignment as = (ExpressionAssignment) exp;
			code.append(generateExpression(as.getValue(), contextData));
			code.append(generateLValueAssignment(as.getLValue(), contextData));
		} else if(exp instanceof ExpressionConditional) {
			String falseValueLabel = getUniqueLabel();
			String endLabel = getUniqueLabel();
			ExpressionConditional cond = (ExpressionConditional) exp;
			code.append(generateExpression(cond.getCondition(), contextData));
			code.append(t("cmp eax, 0"));
			code.append(t("je "+falseValueLabel));
			code.append(generateExpression(cond.getTrueValue(), contextData));
			code.append(t("jmp "+endLabel));
			code.append(falseValueLabel+":\n");
			code.append(generateExpression(cond.getFalseValue(), contextData));
			code.append(endLabel+":\n");
		} else if (exp instanceof ExpressionFunctionCall) {
			ExpressionFunctionCall efc = (ExpressionFunctionCall) exp;
			int argsCount = efc.getArguments().size();
			
			int paddingBytes = 8*(((argsCount > 6)?argsCount-6:0)+1);// # of bytes allocated for arguments + padding value itself
			if(paddingBytes != 0) {
				// Code for 16-byte stack alignment
				code.append(t("mov rax, rsp"));
				code.append(t("sub rax, " + paddingBytes)); 
				code.append(t("xor rdx, rdx"));
				code.append(t("mov rcx, 16"));
				code.append(t("idiv rcx"));
				code.append(t("sub rsp, rdx"));
				code.append(t("push rdx"));
			}
			
			if(argsCount > 0) {
				code.append(generateExpression(efc.getArguments().get(0), contextData));
				code.append(t("mov edi, eax"));
			}
			if(argsCount > 1) {
				code.append(generateExpression(efc.getArguments().get(1), contextData));
				code.append(t("mov esi, eax"));
			}
			if(argsCount > 2) {
				code.append(generateExpression(efc.getArguments().get(2), contextData));
				code.append(t("mov edx, eax"));
			}
			if(argsCount > 3) {
				code.append(generateExpression(efc.getArguments().get(3), contextData));
				code.append(t("mov ecx, eax"));
			}
			if(argsCount > 4) {
				code.append(generateExpression(efc.getArguments().get(4), contextData));
				code.append(t("mov r8d, eax"));
			}
			if(argsCount > 5) {
				code.append(generateExpression(efc.getArguments().get(5), contextData));
				code.append(t("mov r9d, eax"));
			}
			for(int i = 6; i < argsCount; i++) {
				code.append(generateExpression(efc.getArguments().get(i), contextData));
				code.append(t("push rax"));
			}
			code.append(t("call "+efc.getFunctionName()));
			if(argsCount>6) {
				int bytesToRemove = 8 * (argsCount - 6);
				code.append(t("add rsp, "+bytesToRemove));
			}
			
			// Restore stack-alignment
			code.append(t("pop rdx"));
			code.append(t("add rsp, rdx"));
		} else if (exp instanceof ExpressionUnaryOperation) {
			ExpressionUnaryOperation unaryOp = (ExpressionUnaryOperation) exp;
			code.append(generateExpression(unaryOp.getExpression(), contextData));
			switch (unaryOp.getOperator()) {
			case BitwiseComplement:
				code.append(t("not eax"));
				break;
			case LogicalNegation:
				code.append(t("cmp eax, 0"));
				code.append(t("mov eax, 0"));
				code.append(t("sete al"));
				break;
			case Negation:
				code.append(t("neg eax"));
				break;
			default:
				throw new IllegalStateException();
			}
		} else if(exp instanceof ExpressionVariable) {
			ExpressionVariable av = (ExpressionVariable) exp;

			if (contextData.hasVariable(av.getIdentifier())) {
				code.append(t("mov eax, [rbp" + signedToString(contextData.getVariableOffset(av.getIdentifier())) + "]"));
			} else {
				code.append(t("mov eax, " + av.getIdentifier() + "[rip]"));
			}
		} else if(exp instanceof ExpressionArraySubscript) {
			ExpressionArraySubscript eas = (ExpressionArraySubscript) exp;
			code.append(generateExpression(eas.getIndex(), contextData));
			if (contextData.hasVariable(eas.getIdentifier())) {
				code.append(t("add rax, "+signedToString(contextData.getVariableOffset(eas.getIdentifier()))));
				code.append(t("mov eax, [rbp+rax]"));
			} else {
				code.append(t("lea rbx, " + eas.getIdentifier() + "[rip]"));
				code.append(t("mov eax, [rbx+rax]"));
			}
		} else if (exp instanceof ExpressionPrefixOperation) {
			ExpressionPrefixOperation prefixOp = (ExpressionPrefixOperation) exp;
			
			code.append(generateExpression(prefixOp.getLValue(), contextData));
			switch(prefixOp.getOperator()) {
			case Decrement:
				code.append(t("dec eax"));
				break;
			case Increment:
				code.append(t("inc eax"));
				break;
			default:
				throw new IllegalStateException();
			}

			code.append(generateLValueAssignment(prefixOp.getLValue(), contextData));
		} else if (exp instanceof ExpressionPostfixOperation) {
			ExpressionPostfixOperation postfixOp = (ExpressionPostfixOperation) exp;
			
			code.append(generateExpression(postfixOp.getLValue(), contextData));
			switch(postfixOp.getOperator()) {
			case Decrement:
				code.append(t("dec eax"));
				break;
			case Increment:
				code.append(t("inc eax"));
				break;
			default:
				throw new IllegalStateException();
			}

			code.append(generateLValueAssignment(postfixOp.getLValue(), contextData));

			switch(postfixOp.getOperator()) {
			case Decrement:
				code.append(t("inc eax"));
				break;
			case Increment:
				code.append(t("dec eax"));
				break;
			default:
				throw new IllegalStateException();
			}
		} else if (exp instanceof ExpressionBinaryOperation) {
			ExpressionBinaryOperation binaryOp = (ExpressionBinaryOperation) exp;
			code.append(generateExpression(binaryOp.getLeft(), contextData));
			if (binaryOp.getOperator() != BinaryOperator.LogicalAnd
					&& binaryOp.getOperator() != BinaryOperator.LogicalOr
					&& binaryOp.getOperator() != BinaryOperator.Comma) {
				code.append(t("push rax"));
				code.append(generateExpression(binaryOp.getRight(), contextData));
				code.append(t("mov ecx, eax"));
				code.append(t("pop rax"));
			}

			switch (binaryOp.getOperator()) {
			case Comma:
				code.append(generateExpression(binaryOp.getRight(), contextData));
				break;
			case Equal:
				code.append(t("cmp eax, ecx"));
				code.append(t("mov eax, 0"));
				code.append(t("sete al"));
				break;
			case GreaterThan:
				code.append(t("cmp eax, ecx"));
				code.append(t("mov eax, 0"));
				code.append(t("setg al"));
				break;
			case GreaterThanEq:
				code.append(t("cmp eax, ecx"));
				code.append(t("mov eax, 0"));
				code.append(t("setge al"));
				break;
			case LessThan:
				code.append(t("cmp eax, ecx"));
				code.append(t("mov eax, 0"));
				code.append(t("setl al"));
				break;
			case LessThanEq:
				code.append(t("cmp eax, ecx"));
				code.append(t("mov eax, 0"));
				code.append(t("setle al"));
				break;
			case LogicalAnd:
				String andSecondCheckLabel = getUniqueLabel();
				String andEndLabel = getUniqueLabel();

				code.append(t("cmp eax, 0"));
				code.append(t("jne " + andSecondCheckLabel));
//				code.append(t("mov eax, 0")); eax is already zero
				code.append(t("jmp " + andEndLabel));
				code.append(andSecondCheckLabel + ":\n");
				code.append(generateExpression(binaryOp.getRight(), contextData));
				code.append(t("cmp eax, 0"));
				code.append(t("mov eax, 0"));
				code.append(t("setne al"));
				code.append(andEndLabel + ":\n");
				break;
			case LogicalOr:
				String orSecondCheckLabel = getUniqueLabel();
				String orEndLabel = getUniqueLabel();

				code.append(t("cmp eax, 0"));
				code.append(t("je " + orSecondCheckLabel));
				code.append(t("mov eax, 1"));
				code.append(t("jmp " + orEndLabel));
				code.append(orSecondCheckLabel + ":\n");
				code.append(generateExpression(binaryOp.getRight(), contextData));
				code.append(t("cmp eax, 0"));
				code.append(t("mov eax, 0"));
				code.append(t("setne al"));
				code.append(orEndLabel + ":\n");
				break;
			case NotEqual:
				code.append(t("cmp eax, ecx"));
				code.append(t("mov eax, 0"));
				code.append(t("setne al"));
				break;
			case Addition:
				code.append(t("add eax, ecx"));
				break;
			case Division:
				code.append(t("cdq"));
				code.append(t("idiv ecx"));
				break;
			case Multiplication:
				code.append(t("imul eax, ecx"));
				break;
			case Subtraction:
				code.append(t("sub eax, ecx"));
				break;
			case BitwiseAnd:
				code.append(t("and eax, ecx"));
				break;
			case BitwiseOr:
				code.append(t("or eax, ecx"));
				break;
			case BitwiseXor:
				code.append(t("xor eax, ecx"));
				break;
			case Modulo:
				code.append(t("cdq"));
				code.append(t("idiv ecx"));
				code.append(t("mov eax, edx"));
				break;
			case ShiftLeft:
				code.append(t("sal eax, cl"));
				break;
			case ShiftRight:
				code.append(t("sar eax, cl"));
				break;
			default:
				throw new IllegalStateException();
			}
		} else {
			throw new IllegalStateException();
		}
		return code.toString();
	}

	private String generateLValueAssignment(ExpressionLValue exp, GenerationContext contextData) {
		StringBuilder code = new StringBuilder();
		if(exp instanceof ExpressionVariable) {
			if (contextData.hasVariable(exp.getIdentifier())) {
				code.append(t("mov [rbp" + signedToString(contextData.getVariableOffset(exp.getIdentifier())) + "], eax"));
			} else {
				code.append(t("mov " + exp.getIdentifier() + "[rip], eax"));
			}
		} else if(exp instanceof ExpressionArraySubscript) {
			code.append(t("push rax"));
			ExpressionArraySubscript eas = (ExpressionArraySubscript) exp;
			code.append(generateExpression(eas.getIndex(), contextData));
			code.append(t("pop rbx"));
			if (contextData.hasVariable(exp.getIdentifier())) {
				code.append(t("add rax, " + signedToString(contextData.getVariableOffset(exp.getIdentifier()))));
				code.append(t("mov [rbp+rax], ebx"));
			} else {
				code.append(t("lea rcx, " + exp.getIdentifier() + "[rip]"));
				code.append(t("mov [rcx+rax], ebx"));
			}
		} else {
			throw new IllegalStateException();
		}

		return code.toString();
	}

	private String getUniqueLabel() {
		return "label" + (labelCounter++);
	}
	
	private static String t(String s) {
		return "\t" + s + "\n";
	}
	
	private static String signedToString(int num) {
		if(num<0) {
			return String.valueOf(num);
		}else {
			return "+"+String.valueOf(num);
		}
	}

}
