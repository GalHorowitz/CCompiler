package lasercompiler.parser;

import java.util.ArrayList;
import java.util.List;

import lasercompiler.lexer.tokens.*;
import lasercompiler.parser.nodes.*;
import lasercompiler.parser.nodes.ExpressionBinaryOperation.BinaryOperator;
import lasercompiler.parser.nodes.ExpressionPostfixOperation.PostfixOperator;
import lasercompiler.parser.nodes.ExpressionPrefixOperation.PrefixOperator;
import lasercompiler.parser.nodes.ExpressionUnaryOperation.UnaryOperator;
import lasercompiler.validator.Validator;

public class Parser {

	// <program> ::= { <function> | <declaration> }
	public static Program parseProgram(TokenStream tokens) throws ParseException {
		List<Function> functionDeclarations = new ArrayList<Function>();
		List<Declaration> globalVariables = new ArrayList<Declaration>();
		while(tokens.has(3)) {
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
	private static Function parseFunction(TokenStream tokens) throws ParseException {
		if (tokens.isEmpty() || !(tokens.take() instanceof TokenTypeInteger)) {
			throw new ParseException("Failed to parse function, missing integer return type");
		}

		Token nameToken;
		if (tokens.isEmpty() || !((nameToken = tokens.take()) instanceof TokenIdentifier)) {
			throw new ParseException("Failed to parse function, missing name identifier");
		}
		String name = ((TokenIdentifier) nameToken).getIdentifier();

		if (tokens.isEmpty() || !(tokens.take() instanceof TokenParenOpen)) {
			throw new ParseException("Failed to parse function("+name+"), missing opening parentheses");
		}
		
		List<String> parameters = new ArrayList<String>();
		
		boolean foundComma = true;
		
		while(!tokens.isEmpty() && (tokens.get(0) instanceof TokenTypeInteger)) {
			if(!foundComma) {
				throw new ParseException("Failed to parse function("+name+"), missing comma between parameters");
			}
			foundComma = false;
			tokens.take();
			if (tokens.isEmpty() || !(tokens.get(0) instanceof TokenIdentifier)) {
				throw new ParseException("Failed to parse function("+name+"), missing parameter name");
			}
			TokenIdentifier param = (TokenIdentifier) tokens.take();
			parameters.add(param.getIdentifier());
			if(!tokens.isEmpty() && (tokens.get(0) instanceof TokenComma)) {
				tokens.take();
				foundComma = true;
			}
		}
		
		if (tokens.isEmpty() || !(tokens.take() instanceof TokenParenClose)) {
			throw new ParseException("Failed to parse function("+name+"), missing closing parentheses");
		}
		
		if(!tokens.isEmpty() && (tokens.get(0) instanceof TokenSemicolon)) {
			tokens.take();
			return new Function(name, parameters);
		}else if(tokens.isEmpty() || !(tokens.take() instanceof TokenBraceOpen)) {
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

		if (tokens.isEmpty() || !(tokens.take() instanceof TokenBraceClose)) {
			throw new ParseException("Failed to parse function, missing closing curly brace");
		}

		return new Function(name, parameters, items);

	}

	/*
	 * <block-item> ::= <statement> | <declaration>
	 * <declaration> ::= "int" <identifier> "[" <int> "]" ";" | "int" <identifier> [ = <expression> ] ";"
	 */
	private static BlockItem parseBlockItem(TokenStream tokens) throws ParseException {
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
	
	private static Declaration parseDeclaration(TokenStream tokens) throws ParseException {
		if(tokens.isEmpty() || !(tokens.take() instanceof TokenTypeInteger)) {
			throw new ParseException("Failed to parse declaration, missing type");
		}
		Token variableToken;
		if (tokens.isEmpty() || !((variableToken = tokens.take()) instanceof TokenIdentifier)) {
			throw new ParseException("Failed to parse declaration, missing name");
		}
		Declaration d;
		String name = ((TokenIdentifier)variableToken).getIdentifier();
		if(!tokens.isEmpty() && tokens.get(0) instanceof TokenAssignment) {
			tokens.take();
			Expression exp = parseExpression(tokens);
			d = new DeclarationVariable(name, exp);
		} else if(!tokens.isEmpty() && tokens.get(0) instanceof TokenBracketOpen) {
			tokens.take();
			int arraySize = parseInteger(tokens).getValue();
			if(arraySize <= 0) {
				throw new ParseException("Failed to parse array declaration, array size must be positive");
			}
			if(!(tokens.take() instanceof TokenBracketClose)) {
				throw new ParseException("Failed to parse array declaration, missing closing brackets");
			}
			d = new DeclarationArray(name, arraySize);
		} else {
			d = new DeclarationVariable(name);
		}
		
		if (tokens.isEmpty() || !(tokens.take() instanceof TokenSemicolon)) {
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
	private static Statement parseStatement(TokenStream tokens) throws ParseException {
		if(tokens.isEmpty()) {
			throw new ParseException("Failed to parse empty statement");
		}
		
		Statement s;
		Token tok = tokens.get(0);
		
		if(tok instanceof TokenKeywordReturn) {
			tokens.take();
			Expression exp = parseExpression(tokens);
			s = new StatementReturn(exp);

			if (tokens.isEmpty() || !(tokens.take() instanceof TokenSemicolon)) {
				throw new ParseException("Failed to parse statement, missing semicolon");
			}
		} else if(tok instanceof TokenKeywordIf) {
			tokens.take();
			if (tokens.isEmpty() || !(tokens.take() instanceof TokenParenOpen)) {
				throw new ParseException("Failed to parse if statement, missing open parentheses");
			}
			Expression condition = parseExpression(tokens);
			if (tokens.isEmpty() || !(tokens.take() instanceof TokenParenClose)) {
				throw new ParseException("Failed to parse if statement, missing closing parentheses");
			}
			Statement ifBody = parseStatement(tokens);
			if(!tokens.isEmpty() && (tokens.get(0) instanceof TokenKeywordElse)) {
				tokens.take();
				Statement elseBody = parseStatement(tokens);
				s = new StatementIf(condition, ifBody, elseBody);
			}else {
				s = new StatementIf(condition, ifBody);
			}		
		} else if(tok instanceof TokenBraceOpen) {
			tokens.take();
			List<BlockItem> items = new ArrayList<BlockItem>();
			while(!tokens.isEmpty() && !(tokens.get(0) instanceof TokenBraceClose)) {
				items.add(parseBlockItem(tokens));
			}
			
			if (tokens.isEmpty() || !(tokens.take() instanceof TokenBraceClose)) {
				throw new ParseException("Failed to parse block, missing closing curly brace");
			}
			
			s = new StatementCompound(items);
		} else if(tok instanceof TokenKeywordFor) {
			tokens.take();
			if (tokens.isEmpty() || !(tokens.take() instanceof TokenParenOpen)) {
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
				if (tokens.isEmpty() || !(tokens.take() instanceof TokenSemicolon)) {
					throw new ParseException("Failed to parse for statement, missing semicolon");
				}
				Expression postExp = parseExpressionOptional(tokens);

				if (tokens.isEmpty() || !(tokens.take() instanceof TokenParenClose)) {
					throw new ParseException("Failed to parse for statement, missing closing parentheses");
				}
				Statement body = parseStatement(tokens);
				
				List<BlockItem> forBlock = new ArrayList<BlockItem>();
				forBlock.add(initialDecl);
				forBlock.add(new StatementFor(null, condition, postExp, body));
				s = new StatementCompound(forBlock);
			}else {
				Expression initalExp = parseExpressionOptional(tokens);
				if (tokens.isEmpty() || !(tokens.take() instanceof TokenSemicolon)) {
					throw new ParseException("Failed to parse for statement, missing semicolon");
				}
				Expression condition = parseExpressionOptional(tokens);
				if(condition == null) { // C spec says a missing condition in a for loop should be replaced with a constant non-zero expression
					condition = new ExpressionConstantInteger(1);
				}
				if (tokens.isEmpty() || !(tokens.take() instanceof TokenSemicolon)) {
					throw new ParseException("Failed to parse for statement, missing semicolon");
				}
				Expression postExp = parseExpressionOptional(tokens);
				if (tokens.isEmpty() || !(tokens.take() instanceof TokenParenClose)) {
					throw new ParseException("Failed to parse for statement, missing closing parentheses");
				}
				Statement body = parseStatement(tokens);
				s = new StatementFor(initalExp, condition, postExp, body);
			}
		} else if(tok instanceof TokenKeywordWhile) {
			tokens.take();
			if (tokens.isEmpty() || !(tokens.take() instanceof TokenParenOpen)) {
				throw new ParseException("Failed to parse while statement, missing open parentheses");
			}
			Expression condition = parseExpression(tokens);
			if (tokens.isEmpty() || !(tokens.take() instanceof TokenParenClose)) {
				throw new ParseException("Failed to parse while statement, missing closing parentheses");
			}
			Statement body = parseStatement(tokens);
			s = new StatementWhile(condition, body);
		} else if(tok instanceof TokenKeywordDo) {
			tokens.take();
			Statement body = parseStatement(tokens);
			if (tokens.isEmpty() || !(tokens.take() instanceof TokenKeywordWhile)) {
				throw new ParseException("Failed to parse do-while statement, missing while keyword");
			}
			if (tokens.isEmpty() || !(tokens.take() instanceof TokenParenOpen)) {
				throw new ParseException("Failed to parse do-while statement, missing open parentheses");
			}
			Expression condition = parseExpression(tokens);
			if (tokens.isEmpty() || !(tokens.take() instanceof TokenParenClose)) {
				throw new ParseException("Failed to parse do-while statement, missing closing parentheses");
			}
			if (tokens.isEmpty() || !(tokens.take() instanceof TokenSemicolon)) {
				throw new ParseException("Failed to parse for statement, missing semicolon");
			}
			s = new StatementDo(body, condition);
		} else if(tok instanceof TokenKeywordBreak) {
			tokens.take();
			if (tokens.isEmpty() || !(tokens.take() instanceof TokenSemicolon)) {
				throw new ParseException("Failed to parse break statement, missing semicolon");
			}
			s = new StatementBreak();
		} else if(tok instanceof TokenKeywordContinue) {
			tokens.take();
			if (tokens.isEmpty() || !(tokens.take() instanceof TokenSemicolon)) {
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

			if (tokens.isEmpty() || !(tokens.take() instanceof TokenSemicolon)) {
				throw new ParseException("Failed to parse statement, missing semicolon");
			}
		}

		return s;
	}

	/* 
	 * <exp-option> ::= <exp> | ""
	 */
	private static Expression parseExpressionOptional(TokenStream tokens) throws ParseException {
		if((tokens.get(0) instanceof TokenSemicolon) || (tokens.get(0) instanceof TokenParenClose)) {
			return null;
		}else {
			return parseExpression(tokens);
		}
	}
	
	/* 
	 * -- Semantic type segmentation to handle operator-precedence and left-associativity
	 * <expression> ::= <assignment-exp> { "," <assignment-exp> }
	 * <assignment-exp> ::= <lvalue> <assign_op> <expression> | <conditional-exp>
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
	 * <factor> ::= ("++" | "--") <lvalue> | <unary_op> <factor> | <atom>
	 * <atom> ::= <int> | <function-call> | "(" <expression> ")" | <lvalue> ("++" | "--") | <lvalue>
	 * <lvalue> ::= <identifier> "[" <expression> "]" | <identifier>
	 * <function-call> ::= <identifier> "(" [ <assignment-exp> { "," <assignment-exp> } ] ")"
	 */
	private static Expression parseExpression(TokenStream tokens) throws ParseException {
		Expression firstTerm = parseAssignmentExpression(tokens);
		if (tokens.isEmpty())
			return firstTerm;

		Token nextToken = tokens.get(0);
		while (nextToken instanceof TokenComma) {
			tokens.take();
			Expression nextTerm = parseAssignmentExpression(tokens);
			firstTerm = new ExpressionBinaryOperation(BinaryOperator.Comma, firstTerm, nextTerm);
			if (tokens.isEmpty())
				break;
			nextToken = tokens.get(0);
		}

		return firstTerm;
	}
	
	private static Expression parseAssignmentExpression(TokenStream tokens) throws ParseException {
		if(tokens.isEmpty()) {
			throw new ParseException("Failed to parse empty exception");
		}

		boolean isAssignment = false;
		TokenStream view = tokens.createView();
		try {
			parseLValue(view);
			isAssignment = isAssignOpToken(view.get(0));
		} catch (ParseException ignored) {}

		if(isAssignment) {
			ExpressionLValue lvalue = parseLValue(tokens);
			Token assignToken = tokens.take();
			Expression value = parseExpression(tokens);
			if(assignToken instanceof TokenAssignment) {
				// Nothing to do
			}else if(assignToken instanceof TokenAssignmentAdd){
				value = new ExpressionBinaryOperation(BinaryOperator.Addition, lvalue, value);
			}else if(assignToken instanceof TokenAssignmentSubtract){
				value = new ExpressionBinaryOperation(BinaryOperator.Subtraction, lvalue, value);
			}else if(assignToken instanceof TokenAssignmentMultiply){
				value = new ExpressionBinaryOperation(BinaryOperator.Multiplication, lvalue, value);
			}else if(assignToken instanceof TokenAssignmentDivide){
				value = new ExpressionBinaryOperation(BinaryOperator.Division, lvalue, value);
			}else if(assignToken instanceof TokenAssignmentModulo){
				value = new ExpressionBinaryOperation(BinaryOperator.Modulo, lvalue, value);
			}else if(assignToken instanceof TokenAssignmentShiftLeft){
				value = new ExpressionBinaryOperation(BinaryOperator.ShiftLeft, lvalue, value);
			}else if(assignToken instanceof TokenAssignmentShiftRight){
				value = new ExpressionBinaryOperation(BinaryOperator.ShiftRight, lvalue, value);
			}else if(assignToken instanceof TokenAssignmentBitwiseAnd){
				value = new ExpressionBinaryOperation(BinaryOperator.BitwiseAnd, lvalue, value);
			}else if(assignToken instanceof TokenAssignmentBitwiseXor){
				value = new ExpressionBinaryOperation(BinaryOperator.BitwiseXor, lvalue, value);
			}else if(assignToken instanceof TokenAssignmentBitwiseOr){
				value = new ExpressionBinaryOperation(BinaryOperator.BitwiseOr, lvalue, value);
			}else {
				throw new IllegalStateException();
			}
			return new ExpressionAssignment(lvalue, value);
		}else {
			return parseConditionalExpression(tokens);
		}
	}
	
	private static Expression parseConditionalExpression(TokenStream tokens) throws ParseException {
		Expression firstExp = parseLogicalOrExpression(tokens);
		
		if(!tokens.isEmpty() && (tokens.get(0) instanceof TokenQuestionMark)) {
			tokens.take();
			Expression trueValue = parseExpression(tokens);
			if (tokens.isEmpty() || !(tokens.take() instanceof TokenColon)) {
				throw new ParseException("Failed to parse ternary conditional expression, missing colon");
			}
			Expression falseValue = parseConditionalExpression(tokens);
			return new ExpressionConditional(firstExp, trueValue, falseValue);
		}else {
			return firstExp;
		}
	}
	
	private static Expression parseLogicalOrExpression(TokenStream tokens) throws ParseException {
		Expression firstTerm = parseLogicalAndExpression(tokens);
		if (tokens.isEmpty())
			return firstTerm;

		Token nextToken = tokens.get(0);
		while (nextToken instanceof TokenLogicalOr) {
			tokens.take();
			Expression nextTerm = parseLogicalAndExpression(tokens);
			firstTerm = new ExpressionBinaryOperation(BinaryOperator.LogicalOr, firstTerm, nextTerm);
			if (tokens.isEmpty())
				break;
			nextToken = tokens.get(0);
		}

		return firstTerm;
	}
	
	private static Expression parseLogicalAndExpression(TokenStream tokens) throws ParseException {
		Expression firstTerm = parseBitwiseOrExpression(tokens);
		if (tokens.isEmpty())
			return firstTerm;

		Token nextToken = tokens.get(0);
		while (nextToken instanceof TokenLogicalAnd) {
			tokens.take();
			Expression nextTerm = parseBitwiseOrExpression(tokens);
			firstTerm = new ExpressionBinaryOperation(BinaryOperator.LogicalAnd, firstTerm, nextTerm);
			if (tokens.isEmpty())
				break;
			nextToken = tokens.get(0);
		}

		return firstTerm;
	}
	
	private static Expression parseBitwiseOrExpression(TokenStream tokens) throws ParseException {
		Expression firstTerm = parseBitwiseXorExpression(tokens);
		if (tokens.isEmpty())
			return firstTerm;

		Token nextToken = tokens.get(0);
		while (nextToken instanceof TokenBitwiseOr) {
			tokens.take();
			Expression nextTerm = parseBitwiseXorExpression(tokens);
			firstTerm = new ExpressionBinaryOperation(BinaryOperator.BitwiseOr, firstTerm, nextTerm);
			if (tokens.isEmpty())
				break;
			nextToken = tokens.get(0);
		}

		return firstTerm;
	}
	
	private static Expression parseBitwiseXorExpression(TokenStream tokens) throws ParseException {
		Expression firstTerm = parseBitwiseAndExpression(tokens);
		if (tokens.isEmpty())
			return firstTerm;

		Token nextToken = tokens.get(0);
		while (nextToken instanceof TokenBitwiseXor) {
			tokens.take();
			Expression nextTerm = parseBitwiseAndExpression(tokens);
			firstTerm = new ExpressionBinaryOperation(BinaryOperator.BitwiseXor, firstTerm, nextTerm);
			if (tokens.isEmpty())
				break;
			nextToken = tokens.get(0);
		}

		return firstTerm;
	}
	
	private static Expression parseBitwiseAndExpression(TokenStream tokens) throws ParseException {
		Expression firstTerm = parseEqualityExpression(tokens);
		if (tokens.isEmpty())
			return firstTerm;

		Token nextToken = tokens.get(0);
		while (nextToken instanceof TokenBitwiseAnd) {
			tokens.take();
			Expression nextTerm = parseEqualityExpression(tokens);
			firstTerm = new ExpressionBinaryOperation(BinaryOperator.BitwiseAnd, firstTerm, nextTerm);
			if (tokens.isEmpty())
				break;
			nextToken = tokens.get(0);
		}

		return firstTerm;
	}
	
	private static Expression parseEqualityExpression(TokenStream tokens) throws ParseException {
		Expression firstTerm = parseRelationExpression(tokens);
		if (tokens.isEmpty())
			return firstTerm;

		Token nextToken = tokens.get(0);
		while ((nextToken instanceof TokenEqual) || (nextToken instanceof TokenNotEqual)) {
			BinaryOperator operator = (tokens.take() instanceof TokenEqual) ? BinaryOperator.Equal
					: BinaryOperator.NotEqual;
			Expression nextTerm = parseRelationExpression(tokens);
			firstTerm = new ExpressionBinaryOperation(operator, firstTerm, nextTerm);
			if (tokens.isEmpty())
				break;
			nextToken = tokens.get(0);
		}

		return firstTerm;
	}
	
	private static Expression parseRelationExpression(TokenStream tokens) throws ParseException {
		Expression firstTerm = parseShiftExpression(tokens);
		if (tokens.isEmpty())
			return firstTerm;

		Token nextToken = tokens.get(0);
		while ((nextToken instanceof TokenLessThan) || (nextToken instanceof TokenLessThanEq) || (nextToken instanceof TokenGreaterThan) || (nextToken instanceof TokenGreaterThanEq)) {
			BinaryOperator operator;
			if(tokens.take() instanceof TokenLessThan) operator = BinaryOperator.LessThan;
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
	
	private static Expression parseShiftExpression(TokenStream tokens) throws ParseException {
		Expression firstTerm = parseAdditiveExpression(tokens);
		if (tokens.isEmpty())
			return firstTerm;

		Token nextToken = tokens.get(0);
		while ((nextToken instanceof TokenShiftLeft) || (nextToken instanceof TokenShiftRight)) {
			BinaryOperator operator = (tokens.take() instanceof TokenShiftLeft) ? BinaryOperator.ShiftLeft
					: BinaryOperator.ShiftRight;
			Expression nextTerm = parseAdditiveExpression(tokens);
			firstTerm = new ExpressionBinaryOperation(operator, firstTerm, nextTerm);
			if (tokens.isEmpty())
				break;
			nextToken = tokens.get(0);
		}

		return firstTerm;
	}
	
	private static Expression parseAdditiveExpression(TokenStream tokens) throws ParseException {
		Expression firstTerm = parseTerm(tokens);
		if (tokens.isEmpty())
			return firstTerm;

		Token nextToken = tokens.get(0);
		while ((nextToken instanceof TokenPlus) || (nextToken instanceof TokenMinus)) {
			BinaryOperator operator = (tokens.take() instanceof TokenPlus) ? BinaryOperator.Addition
					: BinaryOperator.Subtraction;
			Expression nextTerm = parseTerm(tokens);
			firstTerm = new ExpressionBinaryOperation(operator, firstTerm, nextTerm);
			if (tokens.isEmpty())
				break;
			nextToken = tokens.get(0);
		}

		return firstTerm;
	}

	private static Expression parseTerm(TokenStream tokens) throws ParseException {
		Expression firstFactor = parseFactor(tokens);
		if (tokens.isEmpty())
			return firstFactor;

		Token nextToken = tokens.get(0);
		while ((nextToken instanceof TokenMultiplication) || (nextToken instanceof TokenDivision) || (nextToken instanceof TokenModulo)) {
			BinaryOperator operator;
			if(tokens.take() instanceof TokenMultiplication) operator = BinaryOperator.Multiplication;
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
	
	private static Expression parseFactor(TokenStream tokens) throws ParseException {
		if(tokens.isEmpty()) {
			throw new ParseException("Failed to parse empty expression(factor)");
		}
		Token firstToken = tokens.get(0);
		if((firstToken instanceof TokenIncrement) || (firstToken instanceof TokenDecrement)) {
			PrefixOperator op = (tokens.take() instanceof TokenIncrement)?PrefixOperator.Increment:PrefixOperator.Decrement;
			ExpressionLValue lvalue = parseLValue(tokens);
			return new ExpressionPrefixOperation(op, lvalue);
		}else if(isUnaryOp(firstToken)) {
			tokens.take();
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

	private static Expression parseAtom(TokenStream tokens) throws ParseException {
		if (tokens.isEmpty()) {
			throw new ParseException("Failed to parse empty expression(atom)");
		}
		Token tok = tokens.get(0);

		if(tok instanceof TokenLiteralInteger) {
			return parseInteger(tokens);
		} else if (tokens.has(2) && (tokens.get(0) instanceof TokenIdentifier)	&& (tokens.get(1) instanceof TokenParenOpen)) {
			TokenIdentifier functionName = (TokenIdentifier) tokens.take();
			tokens.take();
			
			List<Expression> arguments = new ArrayList<Expression>();
			
			boolean foundComma = true;
			while(!tokens.isEmpty() && !(tokens.get(0) instanceof TokenParenClose)) {
				if(!foundComma) {
					throw new ParseException("Failed to parse function call("+functionName.getIdentifier()+"), missing comma between arguments");
				}
				foundComma = false;

				arguments.add(parseAssignmentExpression(tokens));
				
				if(!tokens.isEmpty() && (tokens.get(0) instanceof TokenComma)) {
					tokens.take();
					foundComma = true;
				}
			}
			
			if (tokens.isEmpty() || !(tokens.take() instanceof TokenParenClose)) {
				throw new ParseException("Failed to parse function call("+functionName.getIdentifier()+"), missing closing parentheses");
			}
			
			return new ExpressionFunctionCall(functionName.getIdentifier(), arguments);			
		} else if (tok instanceof TokenParenOpen) {
			tokens.take();
			Expression exp = parseExpression(tokens);
			if(!(tokens.take() instanceof TokenParenClose)) {
				throw new ParseException("Failed to parse expression, missing closing parentheses");
			}
			return exp;
		} else {
			ExpressionLValue lvalue = parseLValue(tokens);
			if(!tokens.isEmpty() && ((tokens.get(0) instanceof TokenIncrement) || (tokens.get(0) instanceof TokenDecrement))) {
				PostfixOperator op = (tokens.take() instanceof TokenIncrement)?PostfixOperator.Increment:PostfixOperator.Decrement;
				return new ExpressionPostfixOperation(op, lvalue);
			}else {
				return lvalue;
			}
		}
	}

	private static ExpressionLValue parseLValue(TokenStream tokens) throws ParseException {
		if(tokens.isEmpty()) {
			throw new ParseException("Failed to parse expression(lvalue)");
		}
		Token tok = tokens.take();
		if(tok instanceof TokenIdentifier){
			String identifier = ((TokenIdentifier) tok).getIdentifier();

			if(tokens.isEmpty() || !(tokens.get(0) instanceof TokenBracketOpen)) {
				return new ExpressionVariable(identifier);
			}
			tokens.take();

			Expression index = parseExpression(tokens);
			if(!(tokens.take() instanceof TokenBracketClose)) {
				throw new ParseException("Failed to parse expression(array-subscript), missing closing brackets");
			}

			return new ExpressionArraySubscript(identifier, index);
		}else {
			throw new ParseException("Failed to parse expression(lvalue), missing identifier");
		}
	}

	private static ExpressionConstantInteger parseInteger(TokenStream tokens) throws ParseException {
		if(tokens.isEmpty()) {
			throw new ParseException("Failed to parse expression(integer)");
		}

		Token tok = tokens.take();
		if(tok instanceof TokenLiteralInteger) {
			try {
				return new ExpressionConstantInteger(Integer.parseInt(((TokenLiteralInteger) tok).getLiteral()));
			} catch (NumberFormatException e) {
				throw new ParseException("Failed to parse expression(integer), invalid integer literal");
			}
		} else {
			throw new ParseException("Failed to parse expression(integer), not an integer?");
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