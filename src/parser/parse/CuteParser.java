package parser.parse;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Iterator;
import lexer.*;
import parser.ast.*;

public class CuteParser {
	private Iterator<Token> tokens;
	private static Node END_OF_LIST = new Node(){}; // 새로 추가된 부분
	
	public CuteParser(File file) {
		try {
			tokens = Scanner.scan(file);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private Token getNextToken() {
		
	if (!tokens.hasNext())
		return null;
		return tokens.next();
	}

	public Node parseExpr() {
		Token t = getNextToken();
		if (t == null) {
			System.out.println("No more token");
			return null;
		}
		TokenType tType = t.type();
		String tLexeme = t.lexme();
		
		switch (tType) {
			case ID:
				IdNode idNode = new IdNode(tLexeme);
				return idNode;
				
			case INT:
				IntNode intNode = new IntNode(tLexeme);
				if (tLexeme == null)
				System.out.println("???");
				return intNode;
				
			// BinaryOpNode +, -, /, *가 해당
			case DIV:
			case EQ:
			case MINUS:
			case GT:
			case PLUS:
			case TIMES:
			case LT:
				BinaryOpNode binarayNode = new BinaryOpNode();
				binarayNode.setValue(tType);
				return binarayNode;
				
				// FunctionNode 키워드가 FunctionNode에 해당
			case ATOM_Q:
			case CAR:
			case CDR:
			case COND:
			case CONS:
			case DEFINE:
			case EQ_Q:
			case LAMBDA:
			case NOT:
			case NULL_Q:
				FunctionNode functionNode = new FunctionNode();
				functionNode.setValue(tType);
				return functionNode;
				
			// BooleanNode
			case FALSE:
				return BooleanNode.FALSE_NODE;
			case TRUE:
				return BooleanNode.TRUE_NODE;
			case L_PAREN:
				return parseExprList();
			case R_PAREN:
				return END_OF_LIST ;
			case APOSTROPHE:
				return new QuoteNode(parseExpr());
			case QUOTE:
				return new QuoteNode(parseExpr());
				
			default:
				// head의 next를 만들고 head를 반환하도록 작성
				System.out.println("Parsing Error!");
				return null;
			}
	}
	
	private ListNode parseExprList() {
		Node head = parseExpr();
		if (head == null)
		return null;
		if (head == END_OF_LIST) // if next token is RPAREN
		return ListNode.ENDLIST;
		ListNode tail = parseExprList(); if (tail == null) return null;
		return ListNode.cons(head, tail);
		}
}