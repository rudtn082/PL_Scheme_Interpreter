package parser.ast;

public class IntNode implements ValueNode {
// 새로 수정된 IntNode
	public Integer value;

	@Override
	public String toString(){
		return "INT: " + value;
	}
	public IntNode(String text) {
		this.value = new Integer(text);
	}
}