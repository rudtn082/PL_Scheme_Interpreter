package interpreter;
import parser.ast.*;
import parser.parse.*;
import lexer.*;
import java.io.File;

public class CuteInterpreter {
	private void errorLog(String err) {
		System.out.println(err);
	}
	
	public Node runExpr(Node rootExpr) {
		if (rootExpr == null)
			return null;
		if (rootExpr instanceof IdNode)
			return rootExpr;
		else if (rootExpr instanceof IntNode)
			return rootExpr;
		else if (rootExpr instanceof BooleanNode)
			return rootExpr;
		else if (rootExpr instanceof ListNode)
			return runList((ListNode) rootExpr);
		else
			errorLog("run Expr error");
		
		return null;
	}
	
	private Node runList(ListNode list) {
		if(list.equals(ListNode.EMPTYLIST))
			return list;
		if(list.car() instanceof FunctionNode){
			return runFunction((FunctionNode)list.car(), list.cdr());
		}
		if(list.car() instanceof BinaryOpNode){
			return runBinary(list);
		}
		return list;
	}
	
	private Node runFunction(FunctionNode operator, ListNode operand) {
		switch (operator.value){
			// CAR, CDR, CONS� ���� ���� ����
			case CAR:
		        if (operand.car() instanceof QuoteNode) { // car������ ���� ��尡 `���� �˻�
		           ListNode result = (ListNode)runQuote(operand); // runQuote�� ���� nodeInside�� ����Ʈ ���� ����Ʈ��带 ������
		           return result.car();
		        }else {
		        	errorLog("run Expr error");
			        return null;
		        }
		    case CDR:
		        if (operand.car() instanceof QuoteNode) { // car������ ���� ��尡 `���� �˻�
		        ListNode result = (ListNode)runQuote(operand); // runQuote�� ���� nodeInside�� ����Ʈ ���� ����Ʈ��带 ������
		        return result.cdr();
		        }else {
		        	errorLog("run Expr error");
			        return null;
		        }
			case CONS:
		        if(operand.car() instanceof QuoteNode) {
			        ListNode result = ListNode.cons((ListNode)runQuote(operand), (ListNode)runQuote(operand.cdr()));
			        return result;
		        }
		        else {
			        ListNode result = ListNode.cons(operand.car(), (ListNode)runQuote(operand.cdr()));
			        return result;
		        }
			case COND:
				if(operand.cdr() == null) {
		        	errorLog("run Expr error");
			        return null;
				}
				if(((ListNode)operand.car()).car() instanceof BooleanNode) { // ����Ʈ �ȿ� ��尡 Boolean �ϰ��
					if(((ListNode)operand.car()).car() == BooleanNode.TRUE_NODE) return ((ListNode)operand.car()).cdr().car();
					else return runFunction((FunctionNode)operator, operand.cdr());
				}
				else if(((ListNode)operand.car()).car() instanceof ListNode) { // ����Ʈ �ȿ� ����Ʈ�� ������
					Node temp = null;
					if(((ListNode)((ListNode)operand.car()).car()).car() instanceof FunctionNode) {
						temp = runFunction((FunctionNode)((ListNode)((ListNode)operand.car()).car()).car(), ((ListNode)((ListNode)operand.car()).car()).cdr());
					}
					else if(((ListNode)((ListNode)operand.car()).car()).car() instanceof BinaryOpNode) {
						temp = runBinary((ListNode)((ListNode)operand.car()).car());
					}
					if(temp instanceof IntNode) return temp;
					else if(temp instanceof BooleanNode) {
						if(temp == BooleanNode.TRUE_NODE) return ((ListNode)operand.car()).cdr().car();
						else return runFunction((FunctionNode)operator, operand.cdr());
					}
				}
				else if(((ListNode)operand.car()).car() instanceof FunctionNode) { // ����Ʈ �ȿ� ��尡 funchtion �� ��� 
					if(runFunction((FunctionNode)((ListNode)operand.car()).car(), ((ListNode)operand.car()).cdr()) == BooleanNode.TRUE_NODE) return ((ListNode)operand.car()).cdr().car();
					else return runFunction((FunctionNode)operator, operand.cdr());
				}
				else if(((ListNode)operand.car()).car() instanceof BinaryOpNode) { // ����Ʈ �ȿ� ��尡 BinaryOpNode �� ��� 
					if(runBinary((ListNode)operand.car()) == BooleanNode.TRUE_NODE) return ((ListNode)operand.car()).cdr().car();
					else return runFunction((FunctionNode)operator, operand.cdr());
				}
				else {
		        	errorLog("run Expr error");
			        return null;
				}
			case NULL_Q:
		         if(((ListNode)runQuote(operand)).car() == null) { // ����Ʈ�� null�� ���
		            BooleanNode result =  BooleanNode.TRUE_NODE;
		            return result;
		         }else{
		            BooleanNode result =  BooleanNode.FALSE_NODE;
		            return result;
		         }
			case ATOM_Q:
				if (runQuote(operand) instanceof IdNode || runQuote(operand) instanceof IntNode) { // list�� �ƴѰ��� IdNode �̰ų�  IntNode
					return BooleanNode.TRUE_NODE;
				} else if ((ListNode)runQuote(operand) instanceof ListNode) // list�ϰ��
					return BooleanNode.FALSE_NODE;
				else {
		        	errorLog("run Expr error");
			        return null;
		        }
			case EQ_Q:
				if(runQuote(operand) instanceof IdNode || runQuote(operand) instanceof IntNode) { // IdNode �̰ų�  IntNode �� ���
					if(runQuote(operand).toString().equals(runQuote(operand.cdr()).toString())) return BooleanNode.TRUE_NODE;
					else return BooleanNode.FALSE_NODE;
				}
				else { // ListNode �� ���
					ListNode temp = (ListNode)runQuote((ListNode)operand);
					ListNode temp2 = (ListNode)runQuote((ListNode)operand.cdr());
					
					while(temp.car() != null && temp.cdr() != null && temp2.car() != null && temp2.cdr() != null) {
						if(temp.car().toString().equals(temp2.car().toString())) {
							temp = temp.cdr();
							temp2 = temp2.cdr();
							continue;
						}
						else return BooleanNode.FALSE_NODE;
					}
					if(temp.car() != null || temp.cdr() != null || temp2.car() != null || temp2.cdr() != null) return BooleanNode.FALSE_NODE;
					else return BooleanNode.TRUE_NODE;
				}
			case NOT:
				if(operand.car() instanceof BooleanNode) { // BooleanNode �ϰ��
					if(operand == BooleanNode.TRUE_NODE) return BooleanNode.FALSE_NODE;
					else return BooleanNode.TRUE_NODE;
				}
				else if(((ListNode)operand.car()).car() instanceof FunctionNode) { // FunctionNode �ϰ��
					if(runFunction((FunctionNode)((ListNode)operand.car()).car(), ((ListNode)operand.car()).cdr()) == BooleanNode.TRUE_NODE) return BooleanNode.FALSE_NODE;
					else return BooleanNode.TRUE_NODE;
				}
				else if (((ListNode)operand.car()).car() instanceof BinaryOpNode) { // BinaryOpNode �ϰ��
					if(runBinary((ListNode)operand.car()) == BooleanNode.TRUE_NODE) return BooleanNode.FALSE_NODE;
					else return BooleanNode.TRUE_NODE;
				}
				else {
		        	errorLog("run Expr error");
			        return null;
				}
			default:
				break;
		}
		return null;
	}
	
	private Node runBinary(ListNode list) {
	      BinaryOpNode operator = (BinaryOpNode) list.car();

	      Node node1 = list.cdr().car(); // �� ���
	      Node node2 = list.cdr().cdr().car(); // �� ���
	      IntNode num1 = (IntNode)runExpr(node1);  // runExpr������ ����Ʈ ����̰� ����Ʈ�� ��尡 ������ �� ��� ���
	      IntNode num2 = (IntNode)runExpr(node2);

	      switch (operator.value) {
	      case PLUS:
	         try {
	            IntNode result = new IntNode(num1.value + num2.value + "");
	            return result;
	         } catch (Exception e) {
	            errorLog("run Expr error");
	         }
	      case MINUS:
	         try {
	            
	            IntNode result = new IntNode(num1.value - num2.value + "");
	            return result;
	         } catch (Exception e) {
	            errorLog("run Expr error");
	         }
	      case DIV:
	         try {
	            IntNode result = new IntNode(num1.value / num2.value + "");
	            return result;
	         } catch (Exception e) {
	            errorLog("run Expr error");
	         }
	      case TIMES:
	         try {
	            IntNode result = new IntNode(num1.value * num2.value + "");
	            return result;
	         } catch (Exception e) {
	            errorLog("run Expr error");
	         }
	      case LT:
	            if(num1.value < num2.value) {
	               BooleanNode result = BooleanNode.TRUE_NODE;
	               return result;
	            }else if(num1.value > num2.value) {
	               BooleanNode result = BooleanNode.FALSE_NODE;
	               return result;
	            }else {
	               errorLog("run Expr error");
	            }
	      case GT:
	         if(num1.value > num2.value) {
	            BooleanNode result = BooleanNode.TRUE_NODE;
	            return result;
	         }else if(num1.value < num2.value) {
	            BooleanNode result = BooleanNode.FALSE_NODE;
	            return result;
	         }else {
	            errorLog("run Expr error");
	         }
	         
	      case EQ:
	         try {
	         if(num1.value == num2.value) {
	            BooleanNode result = BooleanNode.TRUE_NODE;
	            return result;
	         }else {
	            BooleanNode result = BooleanNode.FALSE_NODE;
	            return result;
	         }
	         }catch(Exception e) {
	            errorLog("run Expr error");
	         }
	      default:
	         break;
	      }
	      return null;
	   }
	
	private Node runQuote(ListNode node) {
		return ((QuoteNode)node.car()).nodeInside();
	}
	
	public static void main(String[] args) {
		ClassLoader cloader = CuteInterpreter.class.getClassLoader();
		File file = new
		File(cloader.getResource("interpreter/as07.txt").getFile());
		CuteParser cuteParser = new CuteParser(file);
		Node parseTree = cuteParser.parseExpr();
		CuteInterpreter i = new CuteInterpreter();
		Node resultNode = i.runExpr(parseTree);
		NodePrinter.getPrinter(System.out).prettyPrint(resultNode);
	}
}
	