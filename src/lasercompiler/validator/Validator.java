package lasercompiler.validator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lasercompiler.parser.ParseException;
import lasercompiler.parser.nodes.*;

public class Validator {

	public static Program validateAndLink(List<Function> functionDeclarations, List<Declaration> globalVariablesDecl) throws ParseException {
		Map<String, Function> functions = new HashMap<String, Function>();
		
		for(Function funcDecl : functionDeclarations) {
			if(funcDecl.hasBody()) {
				if(functions.containsKey(funcDecl.getName())) {
					if(functions.get(funcDecl.getName()).hasBody()) {
						throw new ParseException("Failed to parse program, illegal re-definition of function("+funcDecl.getName()+")");
					}else if(functions.get(funcDecl.getName()).getParameters().size() != funcDecl.getParameters().size()) {
						throw new ParseException("Failed to parse program, illegal re-declaration with different parameters of function("+funcDecl.getName()+")");
					}
				}
				functions.put(funcDecl.getName(), funcDecl);
			} else {
				if(functions.containsKey(funcDecl.getName())) {
					if(functions.get(funcDecl.getName()).getParameters().size() != funcDecl.getParameters().size()) {
						throw new ParseException("Failed to parse program, illegal re-declaration with different parameters of function("+funcDecl.getName()+")");
					}
				} else {
					functions.put(funcDecl.getName(), funcDecl);
				}
			}
		}
		
		Map<String, Declaration> globalVars = new HashMap<String, Declaration>();
		
		for(Declaration globalVarDecl : globalVariablesDecl) {
			if(functions.containsKey(globalVarDecl.getIdentifier())) {
				throw new ParseException("Failed to parse program, illegal re-definition of function entity as global variable");
			}
			
			if(globalVarDecl.hasInitializer()) {
				if(globalVars.containsKey(globalVarDecl.getIdentifier())) {
					if(globalVars.get(globalVarDecl.getIdentifier()).hasInitializer()) {
						throw new ParseException("Failed to parse program, illegal re-definition of global variable("+globalVarDecl.getIdentifier()+")");
					}
				}
				globalVars.put(globalVarDecl.getIdentifier(), globalVarDecl);
			}else {
				if(!globalVars.containsKey(globalVarDecl.getIdentifier())) {
					globalVars.put(globalVarDecl.getIdentifier(), globalVarDecl);
				}
			}
		}
		
		ValidationContext programContext = new ValidationContext(functions, new ArrayList<String>(globalVars.keySet()));
		for(Declaration gvar : globalVariablesDecl) {
			if(gvar instanceof DeclarationVariable) {
				programContext.addVariable(gvar.getIdentifier());
			} else if(gvar instanceof DeclarationArray) {
				programContext.addArray((gvar.getIdentifier()));
			} else {
				throw new IllegalStateException();
			}
		}
		
		for(Function func : functions.values()) {
			traverseFunction(func, new ValidationContext(programContext));
		}
		
		return new Program(new ArrayList<Function>(functions.values()), new ArrayList<Declaration>(globalVars.values()));
	}
	
	private static void traverseFunction(Function func, ValidationContext context) throws ParseException {
		for(String parameter : func.getParameters()) {
			context.addVariable(parameter);
		}
		if(func.hasBody()) {
			for(BlockItem item : func.getBody()) {
				traverseBlockItem(item, context);
			}
		}
	}
	
	private static void traverseBlockItem(BlockItem item, ValidationContext context) throws ParseException {
		if(item instanceof Declaration) {
			Declaration d = (Declaration) item;
			if(!context.canDeclareVariable(d.getIdentifier())) {
				throw new ParseException("Failed to validate variable declaration, illegal re-declaration of a variable("+d.getIdentifier()+")");
			}
			if(d instanceof DeclarationVariable) {
				context.addVariable(d.getIdentifier());
			} else if(d instanceof DeclarationArray) {
				context.addArray(d.getIdentifier());
			} else {
				throw new IllegalStateException();
			}
			if(d.hasInitializer()) {
				traverseExpression(d.getInitializer(), context);
			}
		}else if(item instanceof Statement){
			traverseStatement((Statement)item, context);
		}else {
			throw new IllegalStateException();
		}
	}
	
	private static void traverseStatement(Statement s, ValidationContext context) throws ParseException {
		if(s instanceof StatementCompound) {
			ValidationContext blockContext = new ValidationContext(context);
			for(BlockItem item : ((StatementCompound) s).getItems()) {
				traverseBlockItem(item, blockContext);
			}
		} else if(s instanceof StatementWhile) {
			StatementWhile sw = (StatementWhile) s;
			ValidationContext whileContext = new ValidationContext(context);
			whileContext.setInsideLoop(true);
			traverseStatement(sw.getBody(), whileContext);
			traverseExpression(sw.getCondition(), context);
		} else if(s instanceof StatementDo) {			
			StatementDo sd = (StatementDo) s;
			ValidationContext doContext = new ValidationContext(context);
			doContext.setInsideLoop(true);
			traverseStatement(sd.getBody(), doContext);
			traverseExpression(sd.getCondition(), context);
		} else if(s instanceof StatementExpression) {
			StatementExpression se = (StatementExpression) s;
			if(!se.isNullExpression())
				traverseExpression(se.getExpression(), context);
		} else if(s instanceof StatementFor) {
			StatementFor sf = (StatementFor) s;
			ValidationContext forContext = new ValidationContext(context);
			forContext.setInsideLoop(true);
			traverseStatement(sf.getBody(), forContext);
			if(sf.hasInitialExp())
				traverseExpression(sf.getInitialExp(), context);
			traverseExpression(sf.getCondition(), context);
			if(sf.hasPostExp())
				traverseExpression(sf.getPostExp(), context);
		} else if(s instanceof StatementIf) {
			StatementIf si = (StatementIf) s;
			traverseStatement(si.getIfBody(), context);
			if(si.hasElse())
				traverseStatement(si.getElseBody(), context);
			traverseExpression(si.getCondition(), context);
		} else if(s instanceof StatementReturn) {
			traverseExpression(((StatementReturn) s).getExpression(), context);
		} else if(s instanceof StatementBreak) {
			if(!context.isInsideLoop()) {
				throw new ParseException("Failed to validate break statement, illegal usage outside of loop");
			}
		}else if (s instanceof StatementContinue) {
			if(!context.isInsideLoop()) {
				throw new ParseException("Failed to validate continue statement, illegal usage outside of loop");
			}
		} else {
			throw new IllegalStateException();
		}
	}
	
	private static void traverseExpression(Expression exp, ValidationContext context) throws ParseException {
		if(exp instanceof ExpressionFunctionCall) {
			ExpressionFunctionCall efc = (ExpressionFunctionCall) exp;
			if(context.getFunctions().containsKey(efc.getFunctionName())) {
				Function func = context.getFunctions().get(efc.getFunctionName());
				if(func.getParameters().size() != efc.getArguments().size()) {
					throw new ParseException("Failed to parse function call("+efc.getFunctionName()+"), incorrect amount of arguments");
				}
			}else {
				if(context.getGlobalVariables().contains(efc.getFunctionName())) {
					throw new ParseException("Failed to parse function call("+efc.getFunctionName()+"), global variable is not a function");
				}
				System.out.println("Warning: Function "+efc.getFunctionName()+" called without explicit declaration");
			}
		}else if(exp instanceof ExpressionAssignment) {
			ExpressionAssignment ea = (ExpressionAssignment) exp;
			if (ea.getLValue() instanceof ExpressionVariable) {
				if (!context.hasVariable(ea.getLValue().getIdentifier())) {
					throw new ParseException("Failed to validate variable assignment, illegal assignment to unknown variable(" + ea.getLValue().getIdentifier() + ")");
				}
			} else if(ea.getLValue() instanceof ExpressionArraySubscript) {
				if (!context.hasArray(ea.getLValue().getIdentifier())) {
					throw new ParseException("Failed to validate variable assignment, illegal assignment to unknown array(" + ea.getLValue().getIdentifier() + ")");
				}
			} else {
				throw new IllegalStateException();
			}
			traverseExpression(ea.getValue(), context);
		}else if(exp instanceof ExpressionBinaryOperation) {
			traverseExpression(((ExpressionBinaryOperation) exp).getLeft(), context);
			traverseExpression(((ExpressionBinaryOperation) exp).getRight(), context);
		}else if(exp instanceof ExpressionConditional) {
			traverseExpression(((ExpressionConditional) exp).getCondition(), context);
			traverseExpression(((ExpressionConditional) exp).getTrueValue(), context);
			traverseExpression(((ExpressionConditional) exp).getFalseValue(), context);
		}else if(exp instanceof ExpressionConstantInteger) {
			//
		}else if(exp instanceof ExpressionPostfixOperation) {
			traverseExpression(((ExpressionPostfixOperation) exp).getLValue(), context);
		}else if(exp instanceof ExpressionPrefixOperation) {
			traverseExpression(((ExpressionPrefixOperation) exp).getLValue(), context);
		}else if(exp instanceof ExpressionUnaryOperation) {
			traverseExpression(((ExpressionUnaryOperation) exp).getExpression(), context);
		}else if(exp instanceof ExpressionVariable) {
			ExpressionVariable ev = (ExpressionVariable) exp;
			if(!context.hasVariable(ev.getIdentifier())) {
				throw new ParseException("Failed to validate variable reference, illegal reference to unknown variable("+ev.getIdentifier()+")");
			}
		}else if(exp instanceof ExpressionArraySubscript) {
			ExpressionArraySubscript eas = (ExpressionArraySubscript) exp;
			if(!context.hasArray(eas.getIdentifier())) {
				throw new ParseException("Failed to validate variable reference, illegal reference to unknown array variable("+eas.getIdentifier()+")");
			}
		}else {
			throw new IllegalStateException();
		}
	}
	
}
