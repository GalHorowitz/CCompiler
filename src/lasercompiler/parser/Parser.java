package lasercompiler.parser;

import java.util.ArrayList;
import java.util.List;

import lasercompiler.lexer.tokens.Token;
import lasercompiler.lexer.tokens.TokenAssignment;
import lasercompiler.lexer.tokens.TokenAssignmentAdd;
import lasercompiler.lexer.tokens.TokenAssignmentBitwiseAnd;
import lasercompiler.lexer.tokens.TokenAssignmentBitwiseOr;
import lasercompiler.lexer.tokens.TokenAssignmentBitwiseXor;
import lasercompiler.lexer.tokens.TokenAssignmentDivide;
import lasercompiler.lexer.tokens.TokenAssignmentModulo;
import lasercompiler.lexer.tokens.TokenAssignmentMultiply;
import lasercompiler.lexer.tokens.TokenAssignmentShiftLeft;
import lasercompiler.lexer.tokens.TokenAssignmentShiftRight;
import lasercompiler.lexer.tokens.TokenAssignmentSubtract;
import lasercompiler.lexer.tokens.TokenBitwiseAnd;
import lasercompiler.lexer.tokens.TokenBitwiseOr;
import lasercompiler.lexer.tokens.TokenBitwiseXor;
import lasercompiler.lexer.tokens.TokenBraceClose;
import lasercompiler.lexer.tokens.TokenBraceOpen;
import lasercompiler.lexer.tokens.TokenColon;
import lasercompiler.lexer.tokens.TokenComma;
import lasercompiler.lexer.tokens.TokenComplement;
import lasercompiler.lexer.tokens.TokenDecrement;
import lasercompiler.lexer.tokens.TokenDivision;
import lasercompiler.lexer.tokens.TokenEqual;
import lasercompiler.lexer.tokens.TokenGreaterThan;
import lasercompiler.lexer.tokens.TokenGreaterThanEq;
import lasercompiler.lexer.tokens.TokenIdentifier;
import lasercompiler.lexer.tokens.TokenIncrement;
import lasercompiler.lexer.tokens.TokenKeywordBreak;
import lasercompiler.lexer.tokens.TokenKeywordContinue;
import lasercompiler.lexer.tokens.TokenKeywordDo;
import lasercompiler.lexer.tokens.TokenKeywordElse;
import lasercompiler.lexer.tokens.TokenKeywordFor;
import lasercompiler.lexer.tokens.TokenKeywordIf;
import lasercompiler.lexer.tokens.TokenKeywordReturn;
import lasercompiler.lexer.tokens.TokenKeywordWhile;
import lasercompiler.lexer.tokens.TokenLessThan;
import lasercompiler.lexer.tokens.TokenLessThanEq;
import lasercompiler.lexer.tokens.TokenLiteralInteger;
import lasercompiler.lexer.tokens.TokenLogicalAnd;
import lasercompiler.lexer.tokens.TokenLogicalNegation;
import lasercompiler.lexer.tokens.TokenLogicalOr;
import lasercompiler.lexer.tokens.TokenMinus;
import lasercompiler.lexer.tokens.TokenModulo;
import lasercompiler.lexer.tokens.TokenMultiplication;
import lasercompiler.lexer.tokens.TokenNotEqual;
import lasercompiler.lexer.tokens.TokenParenClose;
import lasercompiler.lexer.tokens.TokenParenOpen;
import lasercompiler.lexer.tokens.TokenPlus;
import lasercompiler.lexer.tokens.TokenQuestionMark;
import lasercompiler.lexer.tokens.TokenSemicolon;
import lasercompiler.lexer.tokens.TokenShiftLeft;
import lasercompiler.lexer.tokens.TokenShiftRight;
import lasercompiler.lexer.tokens.TokenTypeInteger;
import lasercompiler.parser.nodes.BlockItem;
import lasercompiler.parser.nodes.Declaration;
import lasercompiler.parser.nodes.Expression;
import lasercompiler.parser.nodes.ExpressionAssignment;
import lasercompiler.parser.nodes.ExpressionBinaryOperation;
import lasercompiler.parser.nodes.ExpressionBinaryOperation.BinaryOperator;
import lasercompiler.parser.nodes.ExpressionConditional;
import lasercompiler.parser.nodes.ExpressionFunctionCall;
import lasercompiler.parser.nodes.ExpressionConstantInteger;
import lasercompiler.parser.nodes.ExpressionPostfixOperation;
import lasercompiler.parser.nodes.ExpressionPostfixOperation.PostfixOperator;
import lasercompiler.parser.nodes.ExpressionPrefixOperation;
import lasercompiler.parser.nodes.ExpressionPrefixOperation.PrefixOperator;
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
import lasercompiler.validator.Validator;

public class Parser {

	// <program> ::= { <function> | <declaration> }
	public static Program parseProgram(List<Token> tokens) throws ParseException {
		List<Function> functionDeclarations = new ArrayList<Function>();
		List<Declaration> globalVariables = new ArrayList<Declaration>();
		while(tokens.size() >= 3) {
			if(tokens.get(2) instanceof TokenParenOpen) {
				Function function = parseFunction(tokens);
				functionDeclarations.add(function);
			}else {
				Declaration decl = parseDeclaration(tokens);
				globalVariables.add(decl);
			}
		}
		
		if(!tokens.isEmpty()) {
			throw new ParseException("Failed to parse program, tokens at end of program are not a part of a function or a global variable");
		}
		
		
		
		return Validator.validateAndLink(functionDeclarations, globalVariables);
	}

	// <function> ::= "int" <identifier> "(" [ "int" <identifier> { "," "int" <identifier> } ] ")" ( "{" { <block-item> } "}" | ";" )
	private static Function parseFunction(List<Token> tokens) throws ParseException {
		if (tokens.isEmpty() || !(tokens.remove(0) instanceof TokenTypeInteger)) {
			throw new ParseException("Failed to parse function, missing integer return type");
		}

		Token nameToken;
		if (tokens.isEmpty() || !((nameToken = tokens.remove(0)) instanceof TokenIdentifier)) {
			throw new ParseException("Failed to parse function, missing name identifier");
		}
		String name = ((TokenIdentifier) nameToken).getIdentifier();

		if (tokens.isEmpty() || !(tokens.remove(0) instanceof TokenParenOpen)) {
			throw new ParseException("Failed to parse function("+name+"), missing opening parentheses");
		}
		
		List<String> parameters = new ArrayList<String>();
		
		boolean foundComma = true;
		
		while(!tokens.isEmpty() && (tokens.get(0) instanceof TokenTypeInteger)) {
			if(!foundComma) {
				throw new ParseException("Failed to parse function("+name+"), missing comma between parameters");
			}
			foundComma = false;
			tokens.remove(0);
			if (tokens.isEmpty() || !(tokens.get(0) instanceof TokenIdentifier)) {
				throw new ParseException("Failed to parse function("+name+"), missing parameter name");
			}
			TokenIdentifier param = (TokenIdentifier) tokens.remove(0);
			parameters.add(param.getIdentifier());
			if(!tokens.isEmpty() && (tokens.get(0) instanceof TokenComma)) {
				tokens.remove(0);
				foundComma = true;
			}
		}
		
		if (tokens.isEmpty() || !(tokens.remove(0) instanceof TokenParenClose)) {
			throw new ParseException("Failed to parse function("+name+"), missing closing parentheses");
		}
		
		if(!tokens.isEmpty() && (tokens.get(0) instanceof TokenSemicolon)) {
			tokens.remove(0);
			return new Function(name, parameters);
		}else if(tokens.isEmpty() || !(tokens.remove(0) instanceof TokenBraceOpen)) {
			throw new ParseException("Failed to parse function("+name+"), missing semicolon or open curly brace.");
		}

		List<BlockItem> items = new ArrayList<BlockItem>();
		boolean foundReturnStatement = false;
		
		while(!tokens.isEmpty() && !(tokens.get(0) instanceof TokenBraceClose)) {
			BlockItem item = parseBlockItem(tokens);
			items.add(item);
			if(item instanceof StatementReturn) {
				if(!foundReturnStatement) {
					foundReturnStatement = true;
				}else {
					throw new ParseException("Failed to parse function("+name+"), found block items after return statement");
				}
			}
		}
		
		if(!foundReturnStatement) {
			/* 
			 * Spec says function should still return if no return statement is present, and that the main function should return 0 by default.
			 * The Spec specifies the actual return value for functions other than main is undefined, so we'll just return 0 always if no return statement is found.
			 */
			items.add(new StatementReturn(new ExpressionConstantInteger(0))); 
		}

		if (tokens.isEmpty() || !(tokens.remove(0) instanceof TokenBraceClose)) {
			throw new ParseException("Failed to parse function, missing closing curly brace");
		}

		return new Function(name, parameters, items);

	}

	/*
	 * <block-item> ::= <statement> | <declaration>
	 * <declaration> ::= "int" <identifier> [ = <expression> ] ";"
	 */
	private static BlockItem parseBlockItem(List<Token> tokens) throws ParseException {
		if(tokens.isEmpty()) {
			throw new ParseException("Failed to parse empty block item");
		}
		
		BlockItem s;
		if(tokens.get(0) instanceof TokenTypeInteger) {
			s = parseDeclaration(tokens);
		} else {
			s = parseStatement(tokens);
		}

		return s;
	}
	
	private static Declaration parseDeclaration(List<Token> tokens) throws ParseException {
		if(tokens.isEmpty() || !(tokens.remove(0) instanceof TokenTypeInteger)) {
			throw new ParseException("Failed to parse variable declaration, missing type");
		}
		Token variableToken;
		if (tokens.isEmpty() || !((variableToken = tokens.remove(0)) instanceof TokenIdentifier)) {
			throw new ParseException("Failed to parse variable declaration, missing variable name");
		}
		Declaration d;
		String variableName = ((TokenIdentifier)variableToken).getIdentifier();
		if(!tokens.isEmpty() && tokens.get(0) instanceof TokenAssignment) {
			tokens.remove(0);
			Expression exp = parseExpression(tokens);
			d = new Declaration(variableName, exp);
		}else {
			d = new Declaration(variableName);
		}
		
		if (tokens.isEmpty() || !(tokens.remove(0) instanceof TokenSemicolon)) {
			throw new ParseException("Failed to parse variable declaration, missing semicolon");
		}
		
		return d;
	}
	
	/*
	 * <statement> ::= "return" <expression> ";"
	 * 				|  <exp-option> ";"
	 * 				|  "if" "(" <expression> ")" <statement> [ "else" <statement> ]
	 * 				|  "{" { <block-item> } "}"
	 * 				|  "for" "(" <exp-option> ";" <exp-option> ";" <exp-option> ")" <statement>
	 * 				|  "for" "(" <declaration> <exp-option> ";" <exp-option> ")" <statement>
	 * 				|  "while" "(" <expression> ")" <statement>
	 * 				|  "do" <statement> "while" "(" <expression> ")" ";"
	 * 				|  "break" ";"
	 * 				|  "continue" ";"
	 */
	private static Statement parseStatement(List<Token> tokens) throws ParseException {
		if(tokens.isEmpty()) {
			throw new ParseException("Failed to parse empty statement");
		}
		
		Statement s;
		Token tok = tokens.get(0);
		
		if(tok instanceof TokenKeywordReturn) {
			tokens.remove(0);
			Expression exp = parseExpression(tokens);
			s = new StatementReturn(exp);

			if (tokens.isEmpty() || !(tokens.remove(0) instanceof TokenSemicolon)) {
				throw new ParseException("Failed to parse statement, missing semicolon");
			}
		} else if(tok instanceof TokenKeywordIf) {
			tokens.remove(0);
			if (tokens.isEmpty() || !(tokens.remove(0) instanceof TokenParenOpen)) {
				throw new ParseException("Failed to parse if statement, missing open parentheses");
			}
			Expression condition = parseExpression(tokens);
			if (tokens.isEmpty() || !(tokens.remove(0) instanceof TokenParenClose)) {
				throw new ParseException("Failed to parse if statement, missing closing parentheses");
			}
			Statement ifBody = parseStatement(tokens);
			if(!tokens.isEmpty() && (tokens.get(0) instanceof TokenKeywordElse)) {
				tokens.remove(0);
				Statement elseBody = parseStatement(tokens);
				s = new StatementIf(condition, ifBody, elseBody);
			}else {
				s = new StatementIf(condition, ifBody);
			}		
		} else if(tok instanceof TokenBraceOpen) {
			tokens.remove(0);
			List<BlockItem> items = new ArrayList<BlockItem>();
			while(!tokens.isEmpty() && !(tokens.get(0) instanceof TokenBraceClose)) {
				items.add(parseBlockItem(tokens));
			}
			
			if (tokens.isEmpty() || !(tokens.remove(0) instanceof TokenBraceClose)) {
				throw new ParseException("Failed to parse block, missing closing curly brace");
			}
			
			s = new StatementCompound(items);
		} else if(tok instanceof TokenKeywordFor) {
			tokens.remove(0);
			if (tokens.isEmpty() || !(tokens.remove(0) instanceof TokenParenOpen)) {
				throw new ParseException("Failed to parse for statement, missing open parentheses");
			}
			if(tokens.isEmpty()) {
				throw new ParseException("Failed to parse for statement, tokens missing");
			}
			
			if(tokens.get(0) instanceof TokenTypeInteger) {
				Declaration initialDecl = parseDeclaration(tokens);
				Expression condition = parseExpressionOptional(tokens);
				if(condition == null) { // C spec says a missing condition in a for loop should be replaced with a constant non-zero expression
					condition = new ExpressionConstantInteger(1);
				}
				if (tokens.isEmpty() || !(tokens.remove(0) instanceof TokenSemicolon)) {
					throw new ParseException("Failed to parse for statement, missing semicolon");
				}
				Expression postExp = parseExpressionOptional(tokens);

				if (tokens.isEmpty() || !(tokens.remove(0) instanceof TokenParenClose)) {
					throw new ParseException("Failed to parse for statement, missing closing parentheses");
				}
				Statement body = parseStatement(tokens);
				
				List<BlockItem> forBlock = new ArrayList<BlockItem>();
				forBlock.add(initialDecl);
				forBlock.add(new StatementFor(null, condition, postExp, body));
				s = new StatementCompound(forBlock);
			}else {
				Expression initalExp = parseExpressionOptional(tokens);
				if (tokens.isEmpty() || !(tokens.remove(0) instanceof TokenSemicolon)) {
					throw new ParseException("Failed to parse for statement, missing semicolon");
				}
				Expression condition = parseExpressionOptional(tokens);
				if(condition == null) { // C spec says a missing condition in a for loop should be replaced with a constant non-zero expression
					condition = new ExpressionConstantInteger(1);
				}
				if (tokens.isEmpty() || !(tokens.remove(0) instanceof TokenSemicolon)) {
					throw new ParseException("Failed to parse for statement, missing semicolon");
				}
				Expression postExp = parseExpressionOptional(tokens);
				if (tokens.isEmpty() || !(tokens.remove(0) instanceof TokenParenClose)) {
					throw new ParseException("Failed to parse for statement, missing closing parentheses");
				}
				Statement body = parseStatement(tokens);
				s = new StatementFor(initalExp, condition, postExp, body);
			}
		} else if(tok instanceof TokenKeywordWhile) {
			tokens.remove(0);
			if (tokens.isEmpty() || !(tokens.remove(0) instanceof TokenParenOpen)) {
				throw new ParseException("Failed to parse while statement, missing open parentheses");
			}
			Expression condition = parseExpression(tokens);
			if (tokens.isEmpty() || !(tokens.remove(0) instanceof TokenParenClose)) {
				throw new ParseException("Failed to parse while statement, missing closing parentheses");
			}
			Statement body = parseStatement(tokens);
			s = new StatementWhile(condition, body);
		} else if(tok instanceof TokenKeywordDo) {
			tokens.remove(0);
			Statement body = parseStatement(tokens);
			if (tokens.isEmpty() || !(tokens.remove(0) instanceof TokenKeywordWhile)) {
				throw new ParseException("Failed to parse do-while statement, missing while keyword");
			}
			if (tokens.isEmpty() || !(tokens.remove(0) instanceof TokenParenOpen)) {
				throw new ParseException("Failed to parse do-while statement, missing open parentheses");
			}
			Expression condition = parseExpression(tokens);
			if (tokens.isEmpty() || !(tokens.remove(0) instanceof TokenParenClose)) {
				throw new ParseException("Failed to parse do-while statement, missing closing parentheses");
			}
			if (tokens.isEmpty() || !(tokens.remove(0) instanceof TokenSemicolon)) {
				throw new ParseException("Failed to parse for statement, missing semicolon");
			}
			s = new StatementDo(body, condition);
		} else if(tok instanceof TokenKeywordBreak) {
			tokens.remove(0);
			if (tokens.isEmpty() || !(tokens.remove(0) instanceof TokenSemicolon)) {
				throw new ParseException("Failed to parse break statement, missing semicolon");
			}
			s = new StatementBreak();
		} else if(tok instanceof TokenKeywordContinue) {
			tokens.remove(0);
			if (tokens.isEmpty() || !(tokens.remove(0) instanceof TokenSemicolon)) {
				throw new ParseException("Failed to parse continue statement, missing semicolon");
			}
			s = new StatementContinue();
		} else {
			Expression exp = parseExpressionOptional(tokens);
			
			if(exp == null) {
				s = new StatementExpression();
			}else {
				s = new StatementExpression(exp);
			}

			if (tokens.isEmpty() || !(tokens.remove(0) instanceof TokenSemicolon)) {
				throw new ParseException("Failed to parse statement, missing semicolon");
			}
		}

		return s;
	}

	/* 
	 * <exp-option> ::= <exp> | ""
	 */
	private static Expression parseExpressionOptional(List<Token> tokens) throws ParseException {
		if((tokens.get(0) instanceof TokenSemicolon) || (tokens.get(0) instanceof TokenParenClose)) {
			return null;
		}else {
			return parseExpression(tokens);
		}
	}
	
	/* 
	 * -- Semantic type segmentation to handle operator-precedence and left-associativity
	 * <expression> ::= <assignment-exp> { "," <assignment-exp> }
	 * <assignment-exp> ::= <identifier> <assign_op> <expression> | <conditional-exp>
	 * <conditional-exp> ::= <logical-or-exp> [ "?" <expression> ":" <conditonal-exp> ]
	 * <logical-or-exp> ::= <logical-and-exp> { "||" <logical-and-exp> }
	 * <logical-and-exp> ::= <bitwise-or-exp> { "&&" <bitwise-or-exp> }
	 * <bitwise-or-exp> ::= <bitwise-xor-exp> { "|" <bitwise-xor-exp> }
	 * <bitwise-xor-exp> ::= <bitwise-and-exp> { "^" <bitwise-and-exp> }
	 * <bitwise-and-exp> ::= <equality-exp> { "&" <equality-exp> }
	 * <equality-exp> ::= <relational-exp> { ("!=" | "==") <relational-exp> }
	 * <relational-exp> ::= <shift-exp> { ("<" | ">" | "<=" | ">=") <shift-exp> }
	 * <shift-exp> ::= <additive-exp> { ("<<" | ">>") <additive-exp> }
	 * <additive-exp> ::= <term> { ("+" | "-") <term> }
	 * <term> ::= <factor> { ("*" | "/" | "%") <factor> }
	 * <factor> ::= ("++" | "--") <imm> | <unary_op> <factor> | <atom> 		# The imm must evaluate to an identifier, I don't know how to mark that semantically
	 * <atom> ::= <int> | <imm> ("++" | "--") | <function-call> | <imm>  	# The first imm must evaluate to an identifier, I don't know how to mark that semantically
	 * <imm> ::= "(" <expression> ")" | <identifier>
	 * <function-call> ::= <identifier> "(" [ <assignment-exp> { "," <assignment-exp> } ] ")"
	 */
	private static Expression parseExpression(List<Token> tokens) throws ParseException {
		Expression firstTerm = parseAssignmentExpression(tokens);
		if (tokens.isEmpty())
			return firstTerm;

		Token nextToken = tokens.get(0);
		while (nextToken instanceof TokenComma) {
			tokens.remove(0);
			Expression nextTerm = parseAssignmentExpression(tokens);
			firstTerm = new ExpressionBinaryOperation(BinaryOperator.Comma, firstTerm, nextTerm);
			if (tokens.isEmpty())
				break;
			nextToken = tokens.get(0);
		}

		return firstTerm;
	}
	
	private static Expression parseAssignmentExpression(List<Token> tokens) throws ParseException {
		if(tokens.isEmpty()) {
			throw new ParseException("Failed to parse empty exception");
		}
		if(tokens.size() >= 2 && tokens.get(0) instanceof TokenIdentifier && isAssignOpToken(tokens.get(1))) {
			String variable = ((TokenIdentifier)tokens.remove(0)).getIdentifier();		
			Token assignToken = tokens.remove(0);
			Expression value = parseExpression(tokens);			
			ExpressionVariable varExp = new ExpressionVariable(variable);
			if(assignToken instanceof TokenAssignment) {
				// Nothing to do
			}else if(assignToken instanceof TokenAssignmentAdd){
				value = new ExpressionBinaryOperation(BinaryOperator.Addition, varExp, value);
			}else if(assignToken instanceof TokenAssignmentSubtract){
				value = new ExpressionBinaryOperation(BinaryOperator.Subtraction, varExp, value);
			}else if(assignToken instanceof TokenAssignmentMultiply){
				value = new ExpressionBinaryOperation(BinaryOperator.Multiplication, varExp, value);
			}else if(assignToken instanceof TokenAssignmentDivide){
				value = new ExpressionBinaryOperation(BinaryOperator.Division, varExp, value);
			}else if(assignToken instanceof TokenAssignmentModulo){
				value = new ExpressionBinaryOperation(BinaryOperator.Modulo, varExp, value);
			}else if(assignToken instanceof TokenAssignmentShiftLeft){
				value = new ExpressionBinaryOperation(BinaryOperator.ShiftLeft, varExp, value);
			}else if(assignToken instanceof TokenAssignmentShiftRight){
				value = new ExpressionBinaryOperation(BinaryOperator.ShiftRight, varExp, value);
			}else if(assignToken instanceof TokenAssignmentBitwiseAnd){
				value = new ExpressionBinaryOperation(BinaryOperator.BitwiseAnd, varExp, value);
			}else if(assignToken instanceof TokenAssignmentBitwiseXor){
				value = new ExpressionBinaryOperation(BinaryOperator.BitwiseXor, varExp, value);
			}else if(assignToken instanceof TokenAssignmentBitwiseOr){
				value = new ExpressionBinaryOperation(BinaryOperator.BitwiseOr, varExp, value);
			}else {
				throw new IllegalStateException();
			}
			return new ExpressionAssignment(variable, value);
		}else {
			return parseConditionalExpression(tokens);
		}
	}
	
	private static Expression parseConditionalExpression(List<Token> tokens) throws ParseException {
		Expression firstExp = parseLogicalOrExpression(tokens);
		
		if(!tokens.isEmpty() && (tokens.get(0) instanceof TokenQuestionMark)) {
			tokens.remove(0);
			Expression trueValue = parseExpression(tokens);
			if (tokens.isEmpty() || !(tokens.remove(0) instanceof TokenColon)) {
				throw new ParseException("Failed to parse ternary conditional expression, missing colon");
			}
			Expression falseValue = parseConditionalExpression(tokens);
			return new ExpressionConditional(firstExp, trueValue, falseValue);
		}else {
			return firstExp;
		}
	}
	
	private static Expression parseLogicalOrExpression(List<Token> tokens) throws ParseException {
		Expression firstTerm = parseLogicalAndExpression(tokens);
		if (tokens.isEmpty())
			return firstTerm;

		Token nextToken = tokens.get(0);
		while (nextToken instanceof TokenLogicalOr) {
			tokens.remove(0);
			Expression nextTerm = parseLogicalAndExpression(tokens);
			firstTerm = new ExpressionBinaryOperation(BinaryOperator.LogicalOr, firstTerm, nextTerm);
			if (tokens.isEmpty())
				break;
			nextToken = tokens.get(0);
		}

		return firstTerm;
	}
	
	private static Expression parseLogicalAndExpression(List<Token> tokens) throws ParseException {
		Expression firstTerm = parseBitwiseOrExpression(tokens);
		if (tokens.isEmpty())
			return firstTerm;

		Token nextToken = tokens.get(0);
		while (nextToken instanceof TokenLogicalAnd) {
			tokens.remove(0);
			Expression nextTerm = parseBitwiseOrExpression(tokens);
			firstTerm = new ExpressionBinaryOperation(BinaryOperator.LogicalAnd, firstTerm, nextTerm);
			if (tokens.isEmpty())
				break;
			nextToken = tokens.get(0);
		}

		return firstTerm;
	}
	
	private static Expression parseBitwiseOrExpression(List<Token> tokens) throws ParseException {
		Expression firstTerm = parseBitwiseXorExpression(tokens);
		if (tokens.isEmpty())
			return firstTerm;

		Token nextToken = tokens.get(0);
		while (nextToken instanceof TokenBitwiseOr) {
			tokens.remove(0);
			Expression nextTerm = parseBitwiseXorExpression(tokens);
			firstTerm = new ExpressionBinaryOperation(BinaryOperator.BitwiseOr, firstTerm, nextTerm);
			if (tokens.isEmpty())
				break;
			nextToken = tokens.get(0);
		}

		return firstTerm;
	}
	
	private static Expression parseBitwiseXorExpression(List<Token> tokens) throws ParseException {
		Expression firstTerm = parseBitwiseAndExpression(tokens);
		if (tokens.isEmpty())
			return firstTerm;

		Token nextToken = tokens.get(0);
		while (nextToken instanceof TokenBitwiseXor) {
			tokens.remove(0);
			Expression nextTerm = parseBitwiseAndExpression(tokens);
			firstTerm = new ExpressionBinaryOperation(BinaryOperator.BitwiseXor, firstTerm, nextTerm);
			if (tokens.isEmpty())
				break;
			nextToken = tokens.get(0);
		}

		return firstTerm;
	}
	
	private static Expression parseBitwiseAndExpression(List<Token> tokens) throws ParseException {
		Expression firstTerm = parseEqualityExpression(tokens);
		if (tokens.isEmpty())
			return firstTerm;

		Token nextToken = tokens.get(0);
		while (nextToken instanceof TokenBitwiseAnd) {
			tokens.remove(0);
			Expression nextTerm = parseEqualityExpression(tokens);
			firstTerm = new ExpressionBinaryOperation(BinaryOperator.BitwiseAnd, firstTerm, nextTerm);
			if (tokens.isEmpty())
				break;
			nextToken = tokens.get(0);
		}

		return firstTerm;
	}
	
	private static Expression parseEqualityExpression(List<Token> tokens) throws ParseException {
		Expression firstTerm = parseRelationExpression(tokens);
		if (tokens.isEmpty())
			return firstTerm;

		Token nextToken = tokens.get(0);
		while ((nextToken instanceof TokenEqual) || (nextToken instanceof TokenNotEqual)) {
			BinaryOperator operator = (tokens.remove(0) instanceof TokenEqual) ? BinaryOperator.Equal
					: BinaryOperator.NotEqual;
			Expression nextTerm = parseRelationExpression(tokens);
			firstTerm = new ExpressionBinaryOperation(operator, firstTerm, nextTerm);
			if (tokens.isEmpty())
				break;
			nextToken = tokens.get(0);
		}

		return firstTerm;
	}
	
	private static Expression parseRelationExpression(List<Token> tokens) throws ParseException {
		Expression firstTerm = parseShiftExpression(tokens);
		if (tokens.isEmpty())
			return firstTerm;

		Token nextToken = tokens.get(0);
		while ((nextToken instanceof TokenLessThan) || (nextToken instanceof TokenLessThanEq) || (nextToken instanceof TokenGreaterThan) || (nextToken instanceof TokenGreaterThanEq)) {
			BinaryOperator operator;
			if(tokens.remove(0) instanceof TokenLessThan) operator = BinaryOperator.LessThan;
			else if(nextToken instanceof TokenLessThanEq) operator = BinaryOperator.LessThanEq;
			else if(nextToken instanceof TokenGreaterThan) operator = BinaryOperator.GreaterThan;
			else operator = BinaryOperator.GreaterThanEq;
			
			Expression nextTerm = parseShiftExpression(tokens);
			firstTerm = new ExpressionBinaryOperation(operator, firstTerm, nextTerm);
			if (tokens.isEmpty())
				break;
			nextToken = tokens.get(0);
		}

		return firstTerm;
	}
	
	private static Expression parseShiftExpression(List<Token> tokens) throws ParseException {
		Expression firstTerm = parseAdditiveExpression(tokens);
		if (tokens.isEmpty())
			return firstTerm;

		Token nextToken = tokens.get(0);
		while ((nextToken instanceof TokenShiftLeft) || (nextToken instanceof TokenShiftRight)) {
			BinaryOperator operator = (tokens.remove(0) instanceof TokenShiftLeft) ? BinaryOperator.ShiftLeft
					: BinaryOperator.ShiftRight;
			Expression nextTerm = parseAdditiveExpression(tokens);
			firstTerm = new ExpressionBinaryOperation(operator, firstTerm, nextTerm);
			if (tokens.isEmpty())
				break;
			nextToken = tokens.get(0);
		}

		return firstTerm;
	}
	
	private static Expression parseAdditiveExpression(List<Token> tokens) throws ParseException {
		Expression firstTerm = parseTerm(tokens);
		if (tokens.isEmpty())
			return firstTerm;

		Token nextToken = tokens.get(0);
		while ((nextToken instanceof TokenPlus) || (nextToken instanceof TokenMinus)) {
			BinaryOperator operator = (tokens.remove(0) instanceof TokenPlus) ? BinaryOperator.Addition
					: BinaryOperator.Subtraction;
			Expression nextTerm = parseTerm(tokens);
			firstTerm = new ExpressionBinaryOperation(operator, firstTerm, nextTerm);
			if (tokens.isEmpty())
				break;
			nextToken = tokens.get(0);
		}

		return firstTerm;
	}

	private static Expression parseTerm(List<Token> tokens) throws ParseException {
		Expression firstFactor = parseFactor(tokens);
		if (tokens.isEmpty())
			return firstFactor;

		Token nextToken = tokens.get(0);
		while ((nextToken instanceof TokenMultiplication) || (nextToken instanceof TokenDivision) || (nextToken instanceof TokenModulo)) {
			BinaryOperator operator;
			if(tokens.remove(0) instanceof TokenMultiplication) operator = BinaryOperator.Multiplication;
			else if(nextToken instanceof TokenDivision) operator = BinaryOperator.Division;
			else operator = BinaryOperator.Modulo;
			Expression nextFactor = parseFactor(tokens);
			firstFactor = new ExpressionBinaryOperation(operator, firstFactor, nextFactor);
			if (tokens.isEmpty())
				break;
			nextToken = tokens.get(0);
		}

		return firstFactor;
	}
	
	private static Expression parseFactor(List<Token> tokens) throws ParseException {
		if(tokens.isEmpty()) {
			throw new ParseException("Failed to parse empty expression(factor)");
		}
		Token firstToken = tokens.get(0);
		if((firstToken instanceof TokenIncrement) || (firstToken instanceof TokenDecrement)) {
			PrefixOperator op = (tokens.remove(0) instanceof TokenIncrement)?PrefixOperator.Increment:PrefixOperator.Decrement;
			Expression var = parseImm(tokens);
			if(!(var instanceof ExpressionVariable)) {
				throw new ParseException("Failed to parse prefix operator, expression must be a variable");
			}
			return new ExpressionPrefixOperation(op, (ExpressionVariable) var);
		}else if(isUnaryOp(firstToken)) {
			tokens.remove(0);
			UnaryOperator operator;
			if (firstToken instanceof TokenLogicalNegation) {
				operator = UnaryOperator.LogicalNegation;
			} else if (firstToken instanceof TokenComplement) {
				operator = UnaryOperator.BitwiseComplement;
			} else if (firstToken instanceof TokenMinus) {
				operator = UnaryOperator.Negation;
			} else {
				throw new IllegalStateException();
			}
			Expression exp = parseFactor(tokens);
			return new ExpressionUnaryOperation(operator, exp);
		}else {
			return parseAtom(tokens);
		}
	}

	private static Expression parseAtom(List<Token> tokens) throws ParseException {
		if (tokens.isEmpty()) {
			throw new ParseException("Failed to parse empty expression(atom)");
		}
		Token tok = tokens.get(0);

		if(tok instanceof TokenLiteralInteger) {
			tokens.remove(0);
			try {
				return new ExpressionConstantInteger(Integer.parseInt(((TokenLiteralInteger) tok).getLiteral()));
			} catch (NumberFormatException e) {
				throw new ParseException("Failed to parse expression, invalid integer literal");
			}
		} else if (tokens.size() >= 2 && (tokens.get(0) instanceof TokenIdentifier)	&& (tokens.get(1) instanceof TokenParenOpen)) {
			TokenIdentifier functionName = (TokenIdentifier) tokens.remove(0);
			tokens.remove(0);
			
			List<Expression> arguments = new ArrayList<Expression>();
			
			boolean foundComma = true;
			while(!tokens.isEmpty() && !(tokens.get(0) instanceof TokenParenClose)) {
				if(!foundComma) {
					throw new ParseException("Failed to parse function call("+functionName.getIdentifier()+"), missing comma between arguments");
				}
				foundComma = false;

				arguments.add(parseAssignmentExpression(tokens));
				
				if(!tokens.isEmpty() && (tokens.get(0) instanceof TokenComma)) {
					tokens.remove(0);
					foundComma = true;
				}
			}
			
			if (tokens.isEmpty() || !(tokens.remove(0) instanceof TokenParenClose)) {
				throw new ParseException("Failed to parse function call("+functionName.getIdentifier()+"), missing closing parentheses");
			}
			
			return new ExpressionFunctionCall(functionName.getIdentifier(), arguments);			
		}else {
			Expression imm = parseImm(tokens);
			if((imm instanceof ExpressionVariable) && !tokens.isEmpty() && ((tokens.get(0) instanceof TokenIncrement) || (tokens.get(0) instanceof TokenDecrement))) {
				PostfixOperator op = (tokens.remove(0) instanceof TokenIncrement)?PostfixOperator.Increment:PostfixOperator.Decrement;
				return new ExpressionPostfixOperation(op, (ExpressionVariable) imm);
			}else {
				return imm;
			}
		}
	}
	
	private static Expression parseImm(List<Token> tokens) throws ParseException {
		if(tokens.isEmpty()) {
			throw new ParseException("Failed to parse expression(imm)");
		}
		Token tok = tokens.remove(0);
		if (tok instanceof TokenParenOpen) {
			Expression exp = parseExpression(tokens);
			if (tokens.isEmpty() || !(tokens.remove(0) instanceof TokenParenClose)) {
				throw new ParseException("Failed to parse expression(imm), missing closing parentheses");
			}
			return exp;
		}else if(tok instanceof TokenIdentifier){
			return new ExpressionVariable(((TokenIdentifier) tok).getIdentifier());
		}else {
			throw new IllegalStateException();
		}
	}

	// <unary_op> ::= ("!" | "~" | "-")
	private static boolean isUnaryOp(Token tok) {
		return (tok instanceof TokenLogicalNegation) || (tok instanceof TokenComplement) || (tok instanceof TokenMinus);
	}
	
	// <assign_op> ::= ("=" | "+=" | "-=" | "*=" | "/=" | "%=" | "<<=" | ">>=" | "&=" | "^=" | "|=")
	private static boolean isAssignOpToken(Token tok) {
		return (tok instanceof TokenAssignment) || (tok instanceof TokenAssignmentAdd)
				|| (tok instanceof TokenAssignmentSubtract) || (tok instanceof TokenAssignmentMultiply)
				|| (tok instanceof TokenAssignmentDivide) || (tok instanceof TokenAssignmentModulo)
				|| (tok instanceof TokenAssignmentShiftLeft) || (tok instanceof TokenAssignmentShiftRight)
				|| (tok instanceof TokenAssignmentBitwiseAnd) || (tok instanceof TokenAssignmentBitwiseXor)
				|| (tok instanceof TokenAssignmentBitwiseOr);
	}

}