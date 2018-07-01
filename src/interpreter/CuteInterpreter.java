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
		else if (value.car() instanceof ListNode) { // ListNode일 경우
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
		if(hm.get(id.toString()) instanceof ListNode) { // 변수를 가져왔는데 리스트일 경우
			// 리스트의 car이 QuoteNode일 경우
			if(((ListNode)(hm.get(id.toString()))).car() instanceof QuoteNode) {
				// 그 리스트의 notdInside의 car이 BinaryOpNode일 경우에는 그냥 리턴해주지 않고 결과값에 대해 리턴해준다.
				if(((ListNode)((QuoteNode)((ListNode)(hm.get(id.toString()))).car()).nodeInside()).car() instanceof BinaryOpNode) {
					return runList(((ListNode)((QuoteNode)((ListNode)(hm.get(id.toString()))).car()).nodeInside()));
				}
			}
			return ((ListNode)(hm.get(id.toString()))).car(); // BinaryOpNode가 아닐 경우 리스트의 car에 대해 리턴
		}
		return hm.get(id.toString()); // 리스트가 아니면 그냥 리턴해준다.
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
		if(list.car() instanceof FunctionNode){ // functionNode일 경우
			if(((ListNode)temp).cdr().car() != null) { // ` a 같은 경우는 변수가 아니기떄문에 원래 처럼 처리
				return runFunction((FunctionNode)list.car(), list.cdr());
			}
			if(hm.get(list.cdr().car().toString()) != null) { // 변수인 경우 바꿔준다.
				temp = hm.get(list.cdr().car().toString());
			}
			return runFunction((FunctionNode)list.car(), (ListNode)temp); // runFunction할때 list.cdr()은 바꿔준것으로 넘김
		}
		else if(list.car() instanceof BinaryOpNode){ // BinaryOpNode일 경우
			return runBinary(list);
		}
		return list;
	}
	
	private Node runFunction(FunctionNode operator, ListNode operand) {
		switch (operator.value){
			// CAR, CDR, CONS등에 대한 동작 구현
			case CAR:
		        if (operand.car() instanceof QuoteNode) { // car다음에 오는 노드가 `인지 검사
		           ListNode result = (ListNode)runQuote(operand); // runQuote를 통해 nodeInside로 리스트 안쪽 리스트노드를 가져옴
		           return result.car();
		        }else {
		        	errorLog("runFunction(CAR) error");
			        return null;
		        }
		    case CDR:
		        if (operand.car() instanceof QuoteNode) { // car다음에 오는 노드가 `인지 검사
		        ListNode result = (ListNode)runQuote(operand); // runQuote를 통해 nodeInside로 리스트 안쪽 리스트노드를 가져옴
		        return result.cdr();
		        }else {
		        	errorLog("runFunction(CDR) error");
			        return null;
		        }
			case CONS:
				if(operand.cdr().car() instanceof QuoteNode) { // 뒤에 노드가  QuoteNode 일 경우
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
				else {  // 뒤에 노드가  QuoteNode 가 아닐경우
					if(operand.cdr().car() instanceof IdNode) { // operand.cdr()과 리스트를 병합해야하는데 operand.cdr()이 변수 일 경우
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
						else { // 뒤의 노드가 변수가 아닌데 IdNode일 경우 에러
				        	errorLog("runFunction(CONS) error");
					        return null;
						}
					}
					else { // 뒤의 노드가 결합할 수 없는 node일 경우 에러
			        	errorLog("runFunction(CONS) error");
				        return null;
					}
				}
			case COND:
				if(operand.cdr() == null) {
		        	errorLog("runFunction(COND) error");
			        return null;
				}
				if(((ListNode)operand.car()).car() instanceof BooleanNode) { // 리스트 안에 앞 노드가 Boolean 일경우
					if(((ListNode)operand.car()).car() == BooleanNode.TRUE_NODE) { // TRUE면 옆에 노드를 리턴해준다.
						if(hm.get((((ListNode)operand.car()).cdr().car()).toString()) != null) { // 옆에 노드를 리턴해주기 전에 변수일 경우
							if (hm.get((((ListNode)operand.car()).cdr().car()).toString()) instanceof ListNode) { // 그 변수가 리스트일 경우 
								return ((ListNode)(hm.get((((ListNode)operand.car()).cdr().car()).toString()))).car(); // 변수의 car에 대해 리턴해준다.
							}
							return hm.get((((ListNode)operand.car()).cdr().car()).toString()); // 변수에 대한 변환을 해준 후 리턴한다.
						}
						else return ((ListNode)operand.car()).cdr().car(); // 변수가 아닐 경우 그냥 리턴해준다.
					}
					else return runFunction((FunctionNode)operator, operand.cdr()); // FALSE면 뒤에 노드에 대해 재귀
				}
				else if(((ListNode)operand.car()).car() instanceof ListNode) { // 리스트 안에 앞 노드가 ListNode일 떄
					Node temp = null;
					if(((ListNode)((ListNode)operand.car()).car()).car() instanceof FunctionNode) { // 해당 리스트가 FunctionNode일 때
						temp = runFunction((FunctionNode)((ListNode)((ListNode)operand.car()).car()).car(), ((ListNode)((ListNode)operand.car()).car()).cdr()); // FunctionNode의 결과를 가져온다.
					}
					else if(((ListNode)((ListNode)operand.car()).car()).car() instanceof BinaryOpNode) { // 해당 리스트가 BinaryOpNode일 때
						temp = runBinary((ListNode)((ListNode)operand.car()).car()); // BinaryOpNode의 결과를 가져온다.
					}
					if(temp instanceof IntNode) {
			        	errorLog("runFunction(COND) error"); // 가져온 결과가 IntNode일 경우에는 에러를 출력한다.
				        return null;
					}
					else if(temp instanceof BooleanNode) { // 가져온 결과가 BooleanNode일 경우에
						if(temp == BooleanNode.TRUE_NODE) {
							if(hm.get((((ListNode)operand.car()).cdr().car()).toString()) != null) { // 옆에 노드를 리턴해주기 전에 변수일 경우
								if (hm.get((((ListNode)operand.car()).cdr().car()).toString()) instanceof ListNode) { // 그 변수가 리스트일 경우 
									return ((ListNode)(hm.get((((ListNode)operand.car()).cdr().car()).toString()))).car(); // 변수의 car에 대해 리턴해준다.
								}
								return hm.get((((ListNode)operand.car()).cdr().car()).toString()); // 변수에 대한 변환을 해준 후 리턴한다.
							}
							else return ((ListNode)operand.car()).cdr().car(); // 변수가 아닐 경우 그냥 리턴해준다.
						}
						else return runFunction((FunctionNode)operator, operand.cdr()); // FALSE이면 뒤에 노드에 대한 재귀
					}
				}
				else if(((ListNode)operand.car()).car() instanceof IdNode) { // 리스트 안에 앞 노드가 IdNode일 경우 (변수)
					if(hm.get((((ListNode)operand.car()).car()).toString()) != null) {
						if(((ListNode)hm.get((((ListNode)operand.car()).car()).toString())).car() == BooleanNode.TRUE_NODE) { // TRUE면 옆에 노드를 리턴해준다.
							if(hm.get((((ListNode)operand.car()).cdr().car()).toString()) != null) { // 옆에 노드를 리턴해주기 전에 변수일 경우
								if (hm.get((((ListNode)operand.car()).cdr().car()).toString()) instanceof ListNode) { // 그 변수가 리스트일 경우 
									return ((ListNode)(hm.get((((ListNode)operand.car()).cdr().car()).toString()))).car(); // 변수의 car에 대해 리턴해준다.
								}
								return hm.get((((ListNode)operand.car()).cdr().car()).toString()); // 변수에 대한 변환을 해준 후 리턴한다.
							}
							else return ((ListNode)operand.car()).cdr().car(); // 변수가 아닐 경우 그냥 리턴해준다.
						}
						else return runFunction((FunctionNode)operator, operand.cdr()); // FALSE이면 뒤에 노드에 대한 재귀
					}
					else { // IdNode인데 변수로 정의되어 있지 않으면 에러
			        	errorLog("runFunction(COND) error");
				        return null;
					}
				}
				else {
		        	errorLog("runFunction(COND) error");
			        return null;
				}
			case NULL_Q:
		         if(((ListNode)runQuote(operand)).car() == null) { // 리스트가 null일 경우
		            BooleanNode result =  BooleanNode.TRUE_NODE;
		            return result;
		         }else{
		            BooleanNode result =  BooleanNode.FALSE_NODE;
		            return result;
		         }
			case ATOM_Q:
				if (operand.car() instanceof QuoteNode) { // runQuote를 할 수 있는 QuoteNode일 경우
					if (runQuote(operand) instanceof IdNode || runQuote(operand) instanceof IntNode) { // list가 아닌경우는 IdNode 이거나  IntNode
						return BooleanNode.TRUE_NODE;
					} else if ((ListNode)runQuote(operand) instanceof ListNode) // list일경우 FALSE
						return BooleanNode.FALSE_NODE;
					else {
			        	errorLog("runFunction(ATOM_Q) error");
				        return null;
			        }
				}
				else { // QuoteNode가 아닌경우는 모두 참
					return BooleanNode.TRUE_NODE;
				}
			case EQ_Q:
				if(operand.car() instanceof IdNode) { // 앞의 노드가 IdNode 일 경우
					if(hm.get(operand.car().toString()) != null) { // 앞의 노드가 변수일 경우
						if(((ListNode)operand.cdr()).car() instanceof IdNode) { // 뒤에 노드가 IdNode 일 경우
							if(hm.get((((ListNode)operand.cdr()).car()).toString()) != null) { // 뒤에 노드가 변수일 경우
								if(hm.get(operand.car().toString()) instanceof ListNode) { // 앞에 변수가 리스트인 경우
									if(hm.get((((ListNode)operand.cdr()).car()).toString()) instanceof ListNode) { // 뒤에 변수도 리스트인 경우
										ListNode temp = (ListNode)runQuote((ListNode)hm.get(operand.car().toString()));
										ListNode temp2 = (ListNode)runQuote((ListNode)hm.get((((ListNode)operand.cdr()).car()).toString()));
										
										while(temp.car() != null && temp.cdr() != null && temp2.car() != null && temp2.cdr() != null) { // 리스트 하나하나에 대한 비교
											if(temp.car().toString().equals(temp2.car().toString())) {
												temp = temp.cdr();
												temp2 = temp2.cdr();
												continue;
											}
											else return BooleanNode.FALSE_NODE; // 하나라도 다르면 FALSE
										}
										if(temp.car() != null || temp.cdr() != null || temp2.car() != null || temp2.cdr() != null) return BooleanNode.FALSE_NODE; // 비교 후 노드의 개수가 다르면 FALSE
										else return BooleanNode.TRUE_NODE;
									}
									else { // 앞에 변수는 리스트인데 뒤에 변수는 리스트가 아닌경우
										return BooleanNode.FALSE_NODE;
									}
								}
								else { // 앞에 변수가 리스트가 아닌 경우
									if(((ListNode)operand.cdr()).car() instanceof ListNode ) { // 뒤에 변수는 리스트인 경우
										return BooleanNode.FALSE_NODE;
									}
									else { // 앞에 변수와 뒤에 변수가 둘다 리스트가아닌 경우
										if(hm.get(operand.car().toString()).toString().equals(hm.get((((ListNode)operand.cdr()).car()).toString()).toString())) return BooleanNode.TRUE_NODE; // 앞에 변수와 뒤에 변수에 대한 EQ_Q
										else return BooleanNode.FALSE_NODE;
									}
								}
							}
							else { // 뒤에 노드가 변수가 아닌 IdNode일 경우 
								if((hm.get(operand.car().toString())).toString().equals((operand.cdr().car()).toString())) return BooleanNode.TRUE_NODE;
								else return BooleanNode.FALSE_NODE;
							}
						}
						else { // 뒤에 노드가 IntNode 일 경우
							if((hm.get(operand.car().toString())).toString().equals((operand.cdr().car()).toString())) return BooleanNode.TRUE_NODE;
							else return BooleanNode.FALSE_NODE;
						}
					}
					else { // IdNode인데 변수가 정의되어있지 않은 경우
						if(operand.car().toString().equals((operand.cdr().car()).toString())) return BooleanNode.TRUE_NODE;
						else return BooleanNode.FALSE_NODE;
					}
				}
				else if(operand.car() instanceof IntNode) { // 앞의 노드가 IntNode 일 경우
					if(((ListNode)operand.cdr()).car() instanceof IdNode) { // 뒤에 노드가 IdNode 일 경우
						if(hm.get((((ListNode)operand.cdr()).car()).toString()) != null) {
							if(hm.get((((ListNode)operand.cdr()).car()).toString()) instanceof ListNode) { // 변수가 리스트인 경우
								return BooleanNode.FALSE_NODE;
							}
							else if(operand.car().toString().equals(hm.get((((ListNode)operand.cdr()).car()).toString()).toString())) return BooleanNode.TRUE_NODE;
							else return BooleanNode.FALSE_NODE;
						}
						else { // 뒤에 노드가 정의가 안되었을경우 FALSE
							return BooleanNode.FALSE_NODE;
						}
					}
					else { // 뒤에 노드가 IntNode 일 경우
						if(operand.car().toString().equals((operand.cdr().car()).toString())) return BooleanNode.TRUE_NODE;
						else return BooleanNode.FALSE_NODE;
					}
				}
				else if(runQuote(operand) instanceof IntNode || runQuote(operand) instanceof IdNode) { // runQuote시 IntNode,IdNode 일 경우
					if(runQuote(operand).toString().equals(runQuote(operand.cdr()).toString())) return BooleanNode.TRUE_NODE;
					else return BooleanNode.FALSE_NODE;
				}
				else { // ListNode 일 경우
					ListNode temp = (ListNode)runQuote((ListNode)operand);
					ListNode temp2 = (ListNode)runQuote((ListNode)operand.cdr());
					
					while(temp.car() != null && temp.cdr() != null && temp2.car() != null && temp2.cdr() != null) { // 리스트 하나하나에 대한 비교
						if(temp.car().toString().equals(temp2.car().toString())) {
							temp = temp.cdr();
							temp2 = temp2.cdr();
							continue;
						}
						else return BooleanNode.FALSE_NODE; // 하나라도 다르면 FALSE
					}
					if(temp.car() != null || temp.cdr() != null || temp2.car() != null || temp2.cdr() != null) return BooleanNode.FALSE_NODE; // 비교 후 노드의 개수가 다르면 FALSE
					else return BooleanNode.TRUE_NODE;
				}
			case NOT:
				if(operand.car() instanceof BooleanNode) { // BooleanNode 일경우
					if(operand.car() == BooleanNode.TRUE_NODE) return BooleanNode.FALSE_NODE;
					else return BooleanNode.TRUE_NODE;
				}
				else if(((ListNode)operand.car()).car() instanceof FunctionNode) { // FunctionNode 일경우
					if(runFunction((FunctionNode)((ListNode)operand.car()).car(), ((ListNode)operand.car()).cdr()) == BooleanNode.TRUE_NODE) return BooleanNode.FALSE_NODE;
					else return BooleanNode.TRUE_NODE;
				}
				else if (((ListNode)operand.car()).car() instanceof BinaryOpNode) { // BinaryOpNode 일경우
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

	      Node node1 = list.cdr().car(); // 앞 노드
	      Node node2 = list.cdr().cdr().car(); // 뒤 노드
	      IntNode num1 = (IntNode)runExpr(node1);  // runExpr했을때 리스트 노드이고 리스트의 노드가 연산자 일 경우 재귀
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
	