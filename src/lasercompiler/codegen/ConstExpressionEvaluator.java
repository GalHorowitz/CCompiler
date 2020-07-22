package lasercompiler.codegen;

import java.util.ArrayList;
import java.util.List;

import lasercompiler.parser.nodes.Expression;
import lasercompiler.parser.nodes.ExpressionAssignment;
import lasercompiler.parser.nodes.ExpressionBinaryOperation;
import lasercompiler.parser.nodes.ExpressionConditional;
import lasercompiler.parser.nodes.ExpressionConstantInteger;
import lasercompiler.parser.nodes.ExpressionFunctionCall;
import lasercompiler.parser.nodes.ExpressionPostfixOperation;
import lasercompiler.parser.nodes.ExpressionPrefixOperation;
import lasercompiler.parser.nodes.ExpressionUnaryOperation;
import lasercompiler.parser.nodes.ExpressionVariable;

public class ConstExpressionEvaluator {

	public static Expression evalExpression(Expression exp) {
		if (exp instanceof ExpressionAssignment) {
			return new ExpressionAssignment(((ExpressionAssignment) exp).getVariable(),
					evalExpression(((ExpressionAssignment) exp).getValue()));
		} else if (exp instanceof ExpressionBinaryOperation) {
			ExpressionBinaryOperation newExp = new ExpressionBinaryOperation(
					((ExpressionBinaryOperation) exp).getOperator(),
					evalExpression(((ExpressionBinaryOperation) exp).getLeft()),
					evalExpression(((ExpressionBinaryOperation) exp).getRight()));
			if ((newExp.getLeft() instanceof ExpressionConstantInteger)
					&& (newExp.getRight() instanceof ExpressionConstantInteger)) {
				int leftValue = ((ExpressionConstantInteger) newExp.getLeft()).getValue();
				int rightValue = ((ExpressionConstantInteger) newExp.getRight()).getValue();
				int result;
				switch (newExp.getOperator()) {
				case Addition:
					result = leftValue + rightValue;
					break;
				case BitwiseAnd:
					result = leftValue & rightValue;
					break;
				case BitwiseOr:
					result = leftValue | rightValue;
					break;
				case BitwiseXor:
					result = leftValue ^ rightValue;
					break;
				case Comma:
					result = rightValue;
					break;
				case Division:
					result = leftValue / rightValue;
					break;
				case Equal:
					result = (leftValue == rightValue) ? 1 : 0;
					break;
				case GreaterThan:
					result = (leftValue > rightValue) ? 1 : 0;
					break;
				case GreaterThanEq:
					result = (leftValue >= rightValue) ? 1 : 0;
					break;
				case LessThan:
					result = (leftValue < rightValue) ? 1 : 0;
					break;
				case LessThanEq:
					result = (leftValue <= rightValue) ? 1 : 0;
					break;
				case LogicalAnd:
					result = ((leftValue != 0) && (rightValue != 0)) ? 1 : 0;
					break;
				case LogicalOr:
					result = ((leftValue != 0) || (rightValue != 0)) ? 1 : 0;
					break;
				case Modulo:
					result = leftValue % rightValue;
					break;
				case Multiplication:
					result = leftValue * rightValue;
					break;
				case NotEqual:
					result = (leftValue != rightValue) ? 1 : 0;
					break;
				case ShiftLeft:
					result = leftValue << rightValue;
					break;
				case ShiftRight:
					result = leftValue >> rightValue;
					break;
				case Subtraction:
					result = leftValue - rightValue;
					break;
				default:
					throw new IllegalStateException();
				}

				return new ExpressionConstantInteger(result);
			}
			return newExp;
		} else if (exp instanceof ExpressionConditional) {
			ExpressionConditional newExp = new ExpressionConditional(evalExpression(((ExpressionConditional) exp).getCondition()),
					evalExpression(((ExpressionConditional) exp).getTrueValue()),
					evalExpression(((ExpressionConditional) exp).getFalseValue()));
			if(newExp.getCondition() instanceof ExpressionConstantInteger) {
				if(((ExpressionConstantInteger)newExp.getCondition()).getValue() != 0) {
					return newExp.getTrueValue();
				}else {
					return newExp.getFalseValue();
				}
			}
			return newExp;
		} else if (exp instanceof ExpressionConstantInteger) {
			return exp;
		} else if (exp instanceof ExpressionFunctionCall) {
			List<Expression> args = new ArrayList<Expression>();
			for (Expression arg : ((ExpressionFunctionCall) exp).getArguments()) {
				args.add(evalExpression(arg));
			}
			return new ExpressionFunctionCall(((ExpressionFunctionCall) exp).getFunctionName(), args);
		} else if (exp instanceof ExpressionPostfixOperation) {
			return exp;
		} else if (exp instanceof ExpressionPrefixOperation) {
			return exp;
		} else if (exp instanceof ExpressionUnaryOperation) {
			ExpressionUnaryOperation newExp = new ExpressionUnaryOperation(((ExpressionUnaryOperation) exp).getOperator(),
					evalExpression(((ExpressionUnaryOperation) exp).getExpression()));
			if(newExp.getExpression() instanceof ExpressionConstantInteger) {
				int value = ((ExpressionConstantInteger) newExp.getExpression()).getValue();
				int result;
				switch(newExp.getOperator()) {
				case BitwiseComplement:
					result = ~value;
					break;
				case LogicalNegation:
					result = (value == 0)?1:0;
					break;
				case Negation:
					result = -value;
					break;
				default:
					throw new IllegalStateException();
				}
				return new ExpressionConstantInteger(result);
			}
			return newExp;
		} else if (exp instanceof ExpressionVariable) {
			return exp;
		} else {
			throw new IllegalStateException();
		}
	}

}
