package parser.ast;

public class IdNode implements ValueNode {
	// ���� ������ IdNode Class
	String idString;
	
	public IdNode(String text) {
		idString = text;
	}
	
	@Override
	public String toString(){
		return "ID: " + idString;
	}
}