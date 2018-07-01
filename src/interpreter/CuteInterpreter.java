package interpreter;
import parser.ast.*;
import parser.parse.*;
import lexer.*;
import java.io.File;
import java.util.HashMap;
import java.util.Scanner;

public class CuteInterpreter {
	static HashMap<String, Node> hm = new HashMap<String, Node>();
	
	private void errorLog(String err) {
		System.out.println(err);
	}
	
	private void insertTable(IdNode id, ListNode value) {
		if(value.car() instanceof IntNode) {
			hm.put(id.toString(), value.car());
			System.out.println("insertTable success");
		}
		else if (value.car() instanceof QuoteNode || value.car() instanceof BooleanNode) {
			hm.put(id.toString(), value);
			System.out.println("insertTable success");
		}
		else if (value.car() instanceof ListNode) { // ListNode�� ���
			if (((ListNode)value.car()).car() instanceof BinaryOpNode) {
				hm.put(id.toString(), runBinary((ListNode)value.car()));
				System.out.println("insertTable success");
			}
			else if (((ListNode)value.car()).car() instanceof FunctionNode) {
				hm.put(id.toString(), runFunction((FunctionNode)((ListNode)value.car()).car(), ((ListNode)value.car()).cdr()));
				System.out.println("insertTable success");
			}
		}
		else {
			errorLog("insertTable error");
		}
	}
	
	private Node lookupTable(IdNode id) {
		if(hm.get(id.toString()) instanceof ListNode) { // ������ �����Դµ� ����Ʈ�� ���
			// ����Ʈ�� car�� QuoteNode�� ���
			if(((ListNode)(hm.get(id.toString()))).car() instanceof QuoteNode) {
				// �� ����Ʈ�� notdInside�� car�� BinaryOpNode�� ��쿡�� �׳� ���������� �ʰ� ������� ���� �������ش�.
				if(((ListNode)((QuoteNode)((ListNode)(hm.get(id.toString()))).car()).nodeInside()).car() instanceof BinaryOpNode) {
					return runList(((ListNode)((QuoteNode)((ListNode)(hm.get(id.toString()))).car()).nodeInside()));
				}
			}
			return ((ListNode)(hm.get(id.toString()))).car(); // BinaryOpNode�� �ƴ� ��� ����Ʈ�� car�� ���� ����
		}
		return hm.get(id.toString()); // ����Ʈ�� �ƴϸ� �׳� �������ش�.
	}
	
	public Node runExpr(Node rootExpr) {
		if (rootExpr == null)
			return null;
		if (rootExpr instanceof IdNode)
			return lookupTable((IdNode)rootExpr);
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
		Node temp = list.cdr();
		if(list.equals(ListNode.EMPTYLIST))
			return list;
		if(list.car() instanceof FunctionNode){ // functionNode�� ���
			if(((ListNode)temp).cdr().car() != null) { // ` a ���� ���� ������ �ƴϱ⋚���� ���� ó�� ó��
				return runFunction((FunctionNode)list.car(), list.cdr());
			}
			if(hm.get(list.cdr().car().toString()) != null) { // ������ ��� �ٲ��ش�.
				temp = hm.get(list.cdr().car().toString());
			}
			return runFunction((FunctionNode)list.car(), (ListNode)temp); // runFunction�Ҷ� list.cdr()�� �ٲ��ذ����� �ѱ�
		}
		else if(list.car() instanceof BinaryOpNode){ // BinaryOpNode�� ���
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
		        	errorLog("runFunction(CAR) error");
			        return null;
		        }
		    case CDR:
		        if (operand.car() instanceof QuoteNode) { // car������ ���� ��尡 `���� �˻�
		        ListNode result = (ListNode)runQuote(operand); // runQuote�� ���� nodeInside�� ����Ʈ ���� ����Ʈ��带 ������
		        return result.cdr();
		        }else {
		        	errorLog("runFunction(CDR) error");
			        return null;
		        }
			case CONS:
				if(operand.cdr().car() instanceof QuoteNode) { // �ڿ� ��尡  QuoteNode �� ���
					if(operand.car() instanceof QuoteNode) {
				        ListNode result = ListNode.cons((ListNode)runQuote(operand), (ListNode)runQuote(operand.cdr()));
				        return result;
			        }
			        else if(operand.car() instanceof IdNode) { 
			        	if(hm.get((IdNode)operand.car()) != null) {
			        		ListNode result = ListNode.cons(lookupTable((IdNode)operand.car()), (ListNode)runQuote(operand.cdr()));
					        return result;
			        	}
			        	else {
			        		ListNode result = ListNode.cons((IdNode)operand.car(), (ListNode)runQuote(operand.cdr()));
					        return result;
			        	}
			        }
			        else {
				        ListNode result = ListNode.cons(operand.car(), (ListNode)runQuote(operand.cdr()));
				        return result;
			        }
				}
				else {  // �ڿ� ��尡  QuoteNode �� �ƴҰ��
					if(operand.cdr().car() instanceof IdNode) { // operand.cdr()�� ����Ʈ�� �����ؾ��ϴµ� operand.cdr()�� ���� �� ���
						if(hm.get(((ListNode)operand.cdr()).car().toString()) != null) {
							ListNode temp_cdr = (ListNode)runQuote((ListNode)hm.get(((ListNode)operand.cdr()).car().toString()));
							if(operand.car() instanceof QuoteNode) {
						        ListNode result = ListNode.cons((ListNode)runQuote(operand), temp_cdr);
						        return result;
					        }
					        else if(operand.car() instanceof IdNode) { 
					        	ListNode result = ListNode.cons(lookupTable((IdNode)operand.car()), temp_cdr);
						        return result;
					        	
					        }
					        else {
						        ListNode result = ListNode.cons(operand.car(), temp_cdr);
						        return result;
					        }
						}
						else { // ���� ��尡 ������ �ƴѵ� IdNode�� ��� ����
				        	errorLog("runFunction(CONS) error");
					        return null;
						}
					}
					else { // ���� ��尡 ������ �� ���� node�� ��� ����
			        	errorLog("runFunction(CONS) error");
				        return null;
					}
				}
			case COND:
				if(operand.cdr() == null) {
		        	errorLog("runFunction(COND) error");
			        return null;
				}
				if(((ListNode)operand.car()).car() instanceof BooleanNode) { // ����Ʈ �ȿ� �� ��尡 Boolean �ϰ��
					if(((ListNode)operand.car()).car() == BooleanNode.TRUE_NODE) { // TRUE�� ���� ��带 �������ش�.
						if(hm.get((((ListNode)operand.car()).cdr().car()).toString()) != null) { // ���� ��带 �������ֱ� ���� ������ ���
							if (hm.get((((ListNode)operand.car()).cdr().car()).toString()) instanceof ListNode) { // �� ������ ����Ʈ�� ��� 
								return ((ListNode)(hm.get((((ListNode)operand.car()).cdr().car()).toString()))).car(); // ������ car�� ���� �������ش�.
							}
							return hm.get((((ListNode)operand.car()).cdr().car()).toString()); // ������ ���� ��ȯ�� ���� �� �����Ѵ�.
						}
						else return ((ListNode)operand.car()).cdr().car(); // ������ �ƴ� ��� �׳� �������ش�.
					}
					else return runFunction((FunctionNode)operator, operand.cdr()); // FALSE�� �ڿ� ��忡 ���� ���
				}
				else if(((ListNode)operand.car()).car() instanceof ListNode) { // ����Ʈ �ȿ� �� ��尡 ListNode�� ��
					Node temp = null;
					if(((ListNode)((ListNode)operand.car()).car()).car() instanceof FunctionNode) { // �ش� ����Ʈ�� FunctionNode�� ��
						temp = runFunction((FunctionNode)((ListNode)((ListNode)operand.car()).car()).car(), ((ListNode)((ListNode)operand.car()).car()).cdr()); // FunctionNode�� ����� �����´�.
					}
					else if(((ListNode)((ListNode)operand.car()).car()).car() instanceof BinaryOpNode) { // �ش� ����Ʈ�� BinaryOpNode�� ��
						temp = runBinary((ListNode)((ListNode)operand.car()).car()); // BinaryOpNode�� ����� �����´�.
					}
					if(temp instanceof IntNode) {
			        	errorLog("runFunction(COND) error"); // ������ ����� IntNode�� ��쿡�� ������ ����Ѵ�.
				        return null;
					}
					else if(temp instanceof BooleanNode) { // ������ ����� BooleanNode�� ��쿡
						if(temp == BooleanNode.TRUE_NODE) {
							if(hm.get((((ListNode)operand.car()).cdr().car()).toString()) != null) { // ���� ��带 �������ֱ� ���� ������ ���
								if (hm.get((((ListNode)operand.car()).cdr().car()).toString()) instanceof ListNode) { // �� ������ ����Ʈ�� ��� 
									return ((ListNode)(hm.get((((ListNode)operand.car()).cdr().car()).toString()))).car(); // ������ car�� ���� �������ش�.
								}
								return hm.get((((ListNode)operand.car()).cdr().car()).toString()); // ������ ���� ��ȯ�� ���� �� �����Ѵ�.
							}
							else return ((ListNode)operand.car()).cdr().car(); // ������ �ƴ� ��� �׳� �������ش�.
						}
						else return runFunction((FunctionNode)operator, operand.cdr()); // FALSE�̸� �ڿ� ��忡 ���� ���
					}
				}
				else if(((ListNode)operand.car()).car() instanceof IdNode) { // ����Ʈ �ȿ� �� ��尡 IdNode�� ��� (����)
					if(hm.get((((ListNode)operand.car()).car()).toString()) != null) {
						if(((ListNode)hm.get((((ListNode)operand.car()).car()).toString())).car() == BooleanNode.TRUE_NODE) { // TRUE�� ���� ��带 �������ش�.
							if(hm.get((((ListNode)operand.car()).cdr().car()).toString()) != null) { // ���� ��带 �������ֱ� ���� ������ ���
								if (hm.get((((ListNode)operand.car()).cdr().car()).toString()) instanceof ListNode) { // �� ������ ����Ʈ�� ��� 
									return ((ListNode)(hm.get((((ListNode)operand.car()).cdr().car()).toString()))).car(); // ������ car�� ���� �������ش�.
								}
								return hm.get((((ListNode)operand.car()).cdr().car()).toString()); // ������ ���� ��ȯ�� ���� �� �����Ѵ�.
							}
							else return ((ListNode)operand.car()).cdr().car(); // ������ �ƴ� ��� �׳� �������ش�.
						}
						else return runFunction((FunctionNode)operator, operand.cdr()); // FALSE�̸� �ڿ� ��忡 ���� ���
					}
					else { // IdNode�ε� ������ ���ǵǾ� ���� ������ ����
			        	errorLog("runFunction(COND) error");
				        return null;
					}
				}
				else {
		        	errorLog("runFunction(COND) error");
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
				if (operand.car() instanceof QuoteNode) { // runQuote�� �� �� �ִ� QuoteNode�� ���
					if (runQuote(operand) instanceof IdNode || runQuote(operand) instanceof IntNode) { // list�� �ƴѰ��� IdNode �̰ų�  IntNode
						return BooleanNode.TRUE_NODE;
					} else if ((ListNode)runQuote(operand) instanceof ListNode) // list�ϰ�� FALSE
						return BooleanNode.FALSE_NODE;
					else {
			        	errorLog("runFunction(ATOM_Q) error");
				        return null;
			        }
				}
				else { // QuoteNode�� �ƴѰ��� ��� ��
					return BooleanNode.TRUE_NODE;
				}
			case EQ_Q:
				if(operand.car() instanceof IdNode) { // ���� ��尡 IdNode �� ���
					if(hm.get(operand.car().toString()) != null) { // ���� ��尡 ������ ���
						if(((ListNode)operand.cdr()).car() instanceof IdNode) { // �ڿ� ��尡 IdNode �� ���
							if(hm.get((((ListNode)operand.cdr()).car()).toString()) != null) { // �ڿ� ��尡 ������ ���
								if(hm.get(operand.car().toString()) instanceof ListNode) { // �տ� ������ ����Ʈ�� ���
									if(hm.get((((ListNode)operand.cdr()).car()).toString()) instanceof ListNode) { // �ڿ� ������ ����Ʈ�� ���
										ListNode temp = (ListNode)runQuote((ListNode)hm.get(operand.car().toString()));
										ListNode temp2 = (ListNode)runQuote((ListNode)hm.get((((ListNode)operand.cdr()).car()).toString()));
										
										while(temp.car() != null && temp.cdr() != null && temp2.car() != null && temp2.cdr() != null) { // ����Ʈ �ϳ��ϳ��� ���� ��
											if(temp.car().toString().equals(temp2.car().toString())) {
												temp = temp.cdr();
												temp2 = temp2.cdr();
												continue;
											}
											else return BooleanNode.FALSE_NODE; // �ϳ��� �ٸ��� FALSE
										}
										if(temp.car() != null || temp.cdr() != null || temp2.car() != null || temp2.cdr() != null) return BooleanNode.FALSE_NODE; // �� �� ����� ������ �ٸ��� FALSE
										else return BooleanNode.TRUE_NODE;
									}
									else { // �տ� ������ ����Ʈ�ε� �ڿ� ������ ����Ʈ�� �ƴѰ��
										return BooleanNode.FALSE_NODE;
									}
								}
								else { // �տ� ������ ����Ʈ�� �ƴ� ���
									if(((ListNode)operand.cdr()).car() instanceof ListNode ) { // �ڿ� ������ ����Ʈ�� ���
										return BooleanNode.FALSE_NODE;
									}
									else { // �տ� ������ �ڿ� ������ �Ѵ� ����Ʈ���ƴ� ���
										if(hm.get(operand.car().toString()).toString().equals(hm.get((((ListNode)operand.cdr()).car()).toString()).toString())) return BooleanNode.TRUE_NODE; // �տ� ������ �ڿ� ������ ���� EQ_Q
										else return BooleanNode.FALSE_NODE;
									}
								}
							}
							else { // �ڿ� ��尡 ������ �ƴ� IdNode�� ��� 
								if((hm.get(operand.car().toString())).toString().equals((operand.cdr().car()).toString())) return BooleanNode.TRUE_NODE;
								else return BooleanNode.FALSE_NODE;
							}
						}
						else { // �ڿ� ��尡 IntNode �� ���
							if((hm.get(operand.car().toString())).toString().equals((operand.cdr().car()).toString())) return BooleanNode.TRUE_NODE;
							else return BooleanNode.FALSE_NODE;
						}
					}
					else { // IdNode�ε� ������ ���ǵǾ����� ���� ���
						if(operand.car().toString().equals((operand.cdr().car()).toString())) return BooleanNode.TRUE_NODE;
						else return BooleanNode.FALSE_NODE;
					}
				}
				else if(operand.car() instanceof IntNode) { // ���� ��尡 IntNode �� ���
					if(((ListNode)operand.cdr()).car() instanceof IdNode) { // �ڿ� ��尡 IdNode �� ���
						if(hm.get((((ListNode)operand.cdr()).car()).toString()) != null) {
							if(hm.get((((ListNode)operand.cdr()).car()).toString()) instanceof ListNode) { // ������ ����Ʈ�� ���
								return BooleanNode.FALSE_NODE;
							}
							else if(operand.car().toString().equals(hm.get((((ListNode)operand.cdr()).car()).toString()).toString())) return BooleanNode.TRUE_NODE;
							else return BooleanNode.FALSE_NODE;
						}
						else { // �ڿ� ��尡 ���ǰ� �ȵǾ������ FALSE
							return BooleanNode.FALSE_NODE;
						}
					}
					else { // �ڿ� ��尡 IntNode �� ���
						if(operand.car().toString().equals((operand.cdr().car()).toString())) return BooleanNode.TRUE_NODE;
						else return BooleanNode.FALSE_NODE;
					}
				}
				else if(runQuote(operand) instanceof IntNode || runQuote(operand) instanceof IdNode) { // runQuote�� IntNode,IdNode �� ���
					if(runQuote(operand).toString().equals(runQuote(operand.cdr()).toString())) return BooleanNode.TRUE_NODE;
					else return BooleanNode.FALSE_NODE;
				}
				else { // ListNode �� ���
					ListNode temp = (ListNode)runQuote((ListNode)operand);
					ListNode temp2 = (ListNode)runQuote((ListNode)operand.cdr());
					
					while(temp.car() != null && temp.cdr() != null && temp2.car() != null && temp2.cdr() != null) { // ����Ʈ �ϳ��ϳ��� ���� ��
						if(temp.car().toString().equals(temp2.car().toString())) {
							temp = temp.cdr();
							temp2 = temp2.cdr();
							continue;
						}
						else return BooleanNode.FALSE_NODE; // �ϳ��� �ٸ��� FALSE
					}
					if(temp.car() != null || temp.cdr() != null || temp2.car() != null || temp2.cdr() != null) return BooleanNode.FALSE_NODE; // �� �� ����� ������ �ٸ��� FALSE
					else return BooleanNode.TRUE_NODE;
				}
			case NOT:
				if(operand.car() instanceof BooleanNode) { // BooleanNode �ϰ��
					if(operand.car() == BooleanNode.TRUE_NODE) return BooleanNode.FALSE_NODE;
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
		        	errorLog("runFunction(NOT) error");
			        return null;
				}
			case DEFINE:
				if(operand.car() instanceof IdNode) {
					insertTable((IdNode)operand.car(), operand.cdr());
				}
				else {
		        	errorLog("runFunction(DEFINE) error");
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
	            errorLog("runBinary(PLUS) error");
	            return null;
	         }
	      case MINUS:
	         try {
	            IntNode result = new IntNode(num1.value - num2.value + "");
	            return result;
	         } catch (Exception e) {
	        	errorLog("runBinary(MINUS) error");
	            return null;
	         }
	      case DIV:
	         try {
	            IntNode result = new IntNode(num1.value / num2.value + "");
	            return result;
	         } catch (Exception e) {
	        	errorLog("runBinary(DIV) error");
	            return null;
	         }
	      case TIMES:
	         try {
	            IntNode result = new IntNode(num1.value * num2.value + "");
	            return result;
	         } catch (Exception e) {
	        	errorLog("runBinary(TIMES) error");
	            return null;
	         }
	      case LT:
	            if(num1.value < num2.value) {
	               BooleanNode result = BooleanNode.TRUE_NODE;
	               return result;
	            }else if(num1.value > num2.value) {
	               BooleanNode result = BooleanNode.FALSE_NODE;
	               return result;
	            }else {
	               errorLog("runBinary(LT) error");
	               return null;
	            }
	      case GT:
	         if(num1.value > num2.value) {
	            BooleanNode result = BooleanNode.TRUE_NODE;
	            return result;
	         }else if(num1.value < num2.value) {
	            BooleanNode result = BooleanNode.FALSE_NODE;
	            return result;
	         }else {
	        	errorLog("runBinary(GT) error");
	            return null;
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
	        	errorLog("runBinary(EQ) error");
	            return null;
	         }
	      default:
	         break;
	      }
	      return null;
	   }
	
	private Node runQuote(ListNode node) {
		if(node.car() instanceof QuoteNode) {
			return ((QuoteNode)node.car()).nodeInside();
		}
		else {
			errorLog("runQuote error");
			return null;
		}
	}
	
	public static void main(String[] args) {
		while(true) {
			ClassLoader cloader = CuteInterpreter.class.getClassLoader();
			System.out.print("> ");
			Scanner Sc = new Scanner(System.in);
			String st = Sc.nextLine();
			CuteParser cuteParser = new CuteParser(st);
			Node parseTree = cuteParser.parseExpr();
			CuteInterpreter i = new CuteInterpreter();
			Node resultNode = i.runExpr(parseTree);
			System.out.print("... ");
			NodePrinter.getPrinter(System.out).prettyPrint(resultNode);
			System.out.print("\n");
		}
	}
}
	