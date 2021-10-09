package lasercompiler.lexer;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lasercompiler.lexer.tokens.*;

public class Lexer {

	private static Pattern whitespacePattern = Pattern.compile("\\s+");

	public static TokenStream lex(String data) throws LexException {
		List<Token> tokens = new ArrayList<Token>();

		int dataPoint = 0;
		boolean inMultiComment = false;
		boolean inLineComment = false;
		while (dataPoint < data.length()) {
			String currentData = data.substring(dataPoint);
			Matcher match;

			if(inMultiComment) {
				if(TokenMultiCommentClose.getPattern().matcher(currentData).lookingAt()) {
					dataPoint += 2;
					inMultiComment = false;
				}else {
					dataPoint++;
				}
				continue;
			}
			if(inLineComment) {
				if(currentData.charAt(0) == '\n') {
					inLineComment = false;
				}
				dataPoint++;
				continue;
			}
			
			if(TokenMultiCommentOpen.getPattern().matcher(currentData).lookingAt()) {
				dataPoint += 2;
				inMultiComment = true;
				continue;
			}
			if(TokenComment.getPattern().matcher(currentData).lookingAt()) {
				dataPoint += 2;
				inLineComment = true;
				continue;
			}
			
			
			if ((match = whitespacePattern.matcher(currentData)).lookingAt()) {
				dataPoint += match.group().length();
				continue;
			}

			if (TokenBraceClose.getPattern().matcher(currentData).lookingAt()) {
				tokens.add(new TokenBraceClose());

			} else if (TokenBraceOpen.getPattern().matcher(currentData).lookingAt()) {
				tokens.add(new TokenBraceOpen());

			} else if (TokenAssignmentAdd.getPattern().matcher(currentData).lookingAt()) {
				tokens.add(new TokenAssignmentAdd());
				dataPoint++;

			} else if (TokenAssignmentSubtract.getPattern().matcher(currentData).lookingAt()) {
				tokens.add(new TokenAssignmentSubtract());
				dataPoint++;

			} else if (TokenAssignmentMultiply.getPattern().matcher(currentData).lookingAt()) {
				tokens.add(new TokenAssignmentMultiply());
				dataPoint++;

			} else if (TokenAssignmentDivide.getPattern().matcher(currentData).lookingAt()) {
				tokens.add(new TokenAssignmentDivide());
				dataPoint++;

			} else if (TokenAssignmentModulo.getPattern().matcher(currentData).lookingAt()) {
				tokens.add(new TokenAssignmentModulo());
				dataPoint++;

			} else if (TokenAssignmentShiftLeft.getPattern().matcher(currentData).lookingAt()) {
				tokens.add(new TokenAssignmentShiftLeft());
				dataPoint += 2;

			} else if (TokenAssignmentShiftRight.getPattern().matcher(currentData).lookingAt()) {
				tokens.add(new TokenAssignmentShiftRight());
				dataPoint += 2;

			} else if (TokenAssignmentBitwiseAnd.getPattern().matcher(currentData).lookingAt()) {
				tokens.add(new TokenAssignmentBitwiseAnd());
				dataPoint++;

			} else if (TokenAssignmentBitwiseXor.getPattern().matcher(currentData).lookingAt()) {
				tokens.add(new TokenAssignmentBitwiseXor());
				dataPoint++;

			} else if (TokenAssignmentBitwiseOr.getPattern().matcher(currentData).lookingAt()) {
				tokens.add(new TokenAssignmentBitwiseOr());
				dataPoint++;

			} else if (TokenIncrement.getPattern().matcher(currentData).lookingAt()) {
				tokens.add(new TokenIncrement());
				dataPoint++;

			} else if (TokenDecrement.getPattern().matcher(currentData).lookingAt()) {
				tokens.add(new TokenDecrement());
				dataPoint++;

			} else if (TokenComma.getPattern().matcher(currentData).lookingAt()) {
				tokens.add(new TokenComma());

			} else if (TokenMinus.getPattern().matcher(currentData).lookingAt()) {
				tokens.add(new TokenMinus());

			} else if (TokenPlus.getPattern().matcher(currentData).lookingAt()) {
				tokens.add(new TokenPlus());

			} else if (TokenPlus.getPattern().matcher(currentData).lookingAt()) {
				tokens.add(new TokenPlus());

			} else if (TokenColon.getPattern().matcher(currentData).lookingAt()) {
				tokens.add(new TokenColon());

			} else if (TokenQuestionMark.getPattern().matcher(currentData).lookingAt()) {
				tokens.add(new TokenQuestionMark());

			} else if (TokenDivision.getPattern().matcher(currentData).lookingAt()) {
				tokens.add(new TokenDivision());

			} else if (TokenMultiplication.getPattern().matcher(currentData).lookingAt()) {
				tokens.add(new TokenMultiplication());

			} else if (TokenLogicalAnd.getPattern().matcher(currentData).lookingAt()) {
				tokens.add(new TokenLogicalAnd());
				dataPoint++;

			} else if (TokenLogicalOr.getPattern().matcher(currentData).lookingAt()) {
				tokens.add(new TokenLogicalOr());
				dataPoint++;

			} else if (TokenEqual.getPattern().matcher(currentData).lookingAt()) {
				tokens.add(new TokenEqual());
				dataPoint++;

			} else if (TokenModulo.getPattern().matcher(currentData).lookingAt()) {
				tokens.add(new TokenModulo());

			} else if (TokenBitwiseAnd.getPattern().matcher(currentData).lookingAt()) {
				tokens.add(new TokenBitwiseAnd());

			} else if (TokenBitwiseOr.getPattern().matcher(currentData).lookingAt()) {
				tokens.add(new TokenBitwiseOr());

			} else if (TokenBitwiseXor.getPattern().matcher(currentData).lookingAt()) {
				tokens.add(new TokenBitwiseXor());

			} else if (TokenShiftLeft.getPattern().matcher(currentData).lookingAt()) {
				tokens.add(new TokenShiftLeft());
				dataPoint++;

			} else if (TokenShiftRight.getPattern().matcher(currentData).lookingAt()) {
				tokens.add(new TokenShiftRight());
				dataPoint++;

			} else if (TokenNotEqual.getPattern().matcher(currentData).lookingAt()) { // Eq Tokens have to come first so they won't be half-lexed
				tokens.add(new TokenNotEqual());
				dataPoint++;

			} else if (TokenLessThanEq.getPattern().matcher(currentData).lookingAt()) {
				tokens.add(new TokenLessThanEq());
				dataPoint++;

			} else if (TokenGreaterThanEq.getPattern().matcher(currentData).lookingAt()) {
				tokens.add(new TokenGreaterThanEq());
				dataPoint++;

			} else if (TokenLessThan.getPattern().matcher(currentData).lookingAt()) {
				tokens.add(new TokenLessThan());

			} else if (TokenGreaterThan.getPattern().matcher(currentData).lookingAt()) {
				tokens.add(new TokenGreaterThan());

			} else if (TokenComplement.getPattern().matcher(currentData).lookingAt()) {
				tokens.add(new TokenComplement());

			} else if (TokenLogicalNegation.getPattern().matcher(currentData).lookingAt()) {
				tokens.add(new TokenLogicalNegation());

			} else if ((match = TokenLiteralInteger.getPattern().matcher(currentData)).lookingAt()) {
				tokens.add(new TokenLiteralInteger(match.group()));
				dataPoint += match.group().length() - 1;

			} else if (TokenTypeInteger.getPattern().matcher(currentData).lookingAt()) {
				tokens.add(new TokenTypeInteger());
				dataPoint += 3 - 1;

			} else if (TokenParenClose.getPattern().matcher(currentData).lookingAt()) {
				tokens.add(new TokenParenClose());

			} else if (TokenParenOpen.getPattern().matcher(currentData).lookingAt()) {
				tokens.add(new TokenParenOpen());

			} else if (TokenBracketClose.getPattern().matcher(currentData).lookingAt()) {
				tokens.add(new TokenBracketClose());

			} else if (TokenBracketOpen.getPattern().matcher(currentData).lookingAt()) {
				tokens.add(new TokenBracketOpen());

			} else if (TokenKeywordReturn.getPattern().matcher(currentData).lookingAt()) {
				tokens.add(new TokenKeywordReturn());
				dataPoint += 6 - 1;

			} else if (TokenKeywordIf.getPattern().matcher(currentData).lookingAt()) {
				tokens.add(new TokenKeywordIf());
				dataPoint += 2 - 1;

			} else if (TokenKeywordFor.getPattern().matcher(currentData).lookingAt()) {
				tokens.add(new TokenKeywordFor());
				dataPoint += 3 - 1;

			} else if (TokenKeywordWhile.getPattern().matcher(currentData).lookingAt()) {
				tokens.add(new TokenKeywordWhile());
				dataPoint += 5 - 1;

			} else if (TokenKeywordDo.getPattern().matcher(currentData).lookingAt()) {
				tokens.add(new TokenKeywordDo());
				dataPoint += 2 - 1;

			} else if (TokenKeywordBreak.getPattern().matcher(currentData).lookingAt()) {
				tokens.add(new TokenKeywordBreak());
				dataPoint += 5 - 1;

			} else if (TokenKeywordContinue.getPattern().matcher(currentData).lookingAt()) {
				tokens.add(new TokenKeywordContinue());
				dataPoint += 8 - 1;

			} else if (TokenKeywordElse.getPattern().matcher(currentData).lookingAt()) {
				tokens.add(new TokenKeywordElse());
				dataPoint += 4 - 1;

			} else if (TokenSemicolon.getPattern().matcher(currentData).lookingAt()) {
				tokens.add(new TokenSemicolon());

			} else if (TokenAssignment.getPattern().matcher(currentData).lookingAt()) {
				tokens.add(new TokenAssignment());

			} else if ((match = TokenIdentifier.getPattern().matcher(currentData)).lookingAt()) {
				tokens.add(new TokenIdentifier(match.group()));
				dataPoint += match.group().length() - 1;
				
			}else {
				throw new LexException("Failed to lex file, unknown token at char "+dataPoint+": "+currentData.charAt(0));
			}

			dataPoint++;
		}
		
		if(inMultiComment) {
			throw new LexException("Failed to lex file, reached EOF before multi-line comment was terminated");
		}

		return new TokenStream(tokens);
	}

}
