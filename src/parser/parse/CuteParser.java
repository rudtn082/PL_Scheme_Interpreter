package parser.parse;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Iterator;

import lexer.Scanner;
import lexer.Token;
import lexer.TokenType;
import parser.ast.BinarayOpNode;
import parser.ast.BooleanNode;
import parser.ast.FunctionNode;
import parser.ast.IdNode;
import parser.ast.IntNode;
import parser.ast.ListNode;
import parser.ast.Node;

public class CuteParser {
	private Iterator<Token> tokens;
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
				IdNode idNode = new IdNode();
				idNode.value = tLexeme;
				return idNode;
				
			case INT:
				IntNode intNode = new IntNode();
				if (tLexeme == null)
				System.out.println("???");
				intNode.value = new Integer(tLexeme);
				return intNode;
				
			// BinaryOpNode +, -, /, *�� �ش�
			case DIV:
			case EQ:
			case MINUS:
			case GT:
			case PLUS:
			case TIMES:
			case LT:
				BinarayOpNode binarayNode = new BinarayOpNode();
				binarayNode.setValue(tType);
				return binarayNode;
				
				// FunctionNode Ű���尡 FunctionNode�� �ش�
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
				BooleanNode falseNode = new BooleanNode();
				falseNode.value = false;
				return falseNode;
				
			case TRUE:
				BooleanNode trueNode = new BooleanNode();
				trueNode.value = true;
				return trueNode;
			// case L_PAREN�� ���� case R_PAREN�� ���
			// L_PAREN�� ��� parseExprList()�� ȣ���Ͽ� ó��
			case L_PAREN:
				ListNode listNode = new ListNode();
				listNode.value = parseExprList();
				return listNode;
			case R_PAREN:
				return null;
			default:
				// head�� next�� ����� head�� ��ȯ�ϵ��� �ۼ�
				System.out.println("Parsing Error!");
				return null;
			}
	}

	// List�� value�� �����ϴ� �޼ҵ�
	private Node parseExprList() {
		Node head = parseExpr();
		// head�� next ��带 set�Ͻÿ�.
		if (head == null) // if next token is RPAREN
		return null;
		head.setNext(parseExprList());
		return head;
	}
}