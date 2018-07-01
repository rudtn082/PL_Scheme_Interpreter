package parser.parse;

import java.io.PrintStream;
import parser.ast.*;

public class NodePrinter {
	PrintStream ps;
	
	public static NodePrinter getPrinter(PrintStream ps) {
		return new NodePrinter(ps);
	}
	
	private NodePrinter(PrintStream ps) {
		this.ps = ps;
	}
	
	// ListNode, QuoteNode, Node에 대한 printNode 함수를 각각 overload 형식으로 작성
	private void printNode(ListNode listNode) {
		if (listNode == ListNode.EMPTYLIST) {
			ps.print("( ) ");
			return;
		}
			
		if (listNode == ListNode.ENDLIST) {
			return;
		}
			
		ps.print("( ");
		printNode(listNode.car());
		printNode(listNode.cdr());
		ps.print(" )");
	}
	
	private void printNode(QuoteNode quoteNode) {
		if (quoteNode.nodeInside() == null) {
			return;
		}
		ps.print("\'");
		if(quoteNode.nodeInside() instanceof IdNode) {
			ps.print(quoteNode);
			
		}
		else {
			ListNode listnode = (ListNode)quoteNode.nodeInside();
			ps.print("( ");
			printNode(listnode.car());
			printNode(listnode.cdr());
			ps.print(" )");
		}
	}
	private void printNode(Node node) {
		if (node == null)
			return;
		if (node instanceof ListNode) {
			ListNode listnode = (ListNode)node;
			printNode(listnode);
		}
		else if (node instanceof QuoteNode){
			QuoteNode quoteNode = (QuoteNode)node;
			printNode(quoteNode);
		}
		
		else {
			ps.print("[" + node + "] ");
		}
	}
	
	public void prettyPrint(Node node){
		printNode(node);
	}
}