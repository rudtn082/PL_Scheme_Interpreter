package parser.ast;

public class IdNode implements ValueNode {
	// 새로 수정된 IdNode Class
	String idString;
	
	public IdNode(String text) {
		idString = text;
	}
	
	@Override
	public String toString(){
		return "ID: " + idString;
	}
}