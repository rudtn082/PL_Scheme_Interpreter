package lexer;

import static lexer.TokenType.*;
import static lexer.TransitionOutput.*;


enum State {
	START {
		@Override
		public TransitionOutput transit(ScanContext context) {
			Char ch = context.getCharStream().nextChar();
			char v = ch.value();
			switch ( ch.type() ) {
				case LETTER:
					context.append(v);
					return GOTO_ACCEPT_ID;
				case DIGIT:
					context.append(v);
					return GOTO_ACCEPT_INT;
				case SPECIAL_CHAR:
					Character cr = new Character(v); // toString()을 쓰기위해  cr을 생성해준다.
					context.append(v);
					if(v == 35) { // 첫 문자가 #일 경우.
						return GOTO_ACCEPT_ID; // 그 다음에 T나 F가 나올거니까 ID로 보내준다.
					}
					else return GOTO_SIGN; // 그 외 특수문자
				case WS:
					return GOTO_START;
				case END_OF_STREAM:
					return GOTO_EOS;
				default:
					throw new AssertionError();
			}
		}
	},
	ACCEPT_ID {
		@Override
		public TransitionOutput transit(ScanContext context) {
			Char ch = context.getCharStream().nextChar();
			char v = ch.value();
			switch ( ch.type() ) {
				case LETTER:
				case DIGIT:
					context.append(v);
					return GOTO_ACCEPT_ID;
				case SPECIAL_CHAR:
					if(v == 63) { // ? 일경우에
						String a = context.getLexime();
						Character cr = new Character(v); // toString()을 쓰기위해  cr을 생성해준다.
						String b = a.concat(cr.toString()); // 만약에 null과 ?가 있으면 그 두개를 합쳐서 null?로 만들어줌
						return GOTO_MATCHED(Token.ofName(b).type(), b); // MATCHED로 보내준다.
					}
					else return GOTO_FAILED;
				case WS:
				case END_OF_STREAM:
					String temp = context.getLexime();
					return GOTO_MATCHED(Token.ofName(temp).type(), temp); //만약에 키워드일경우를 처리하기 위해서 ofName으로 보내준다.
				default:
					throw new AssertionError();
			}
		}
	},
	ACCEPT_INT {
		@Override
		public TransitionOutput transit(ScanContext context) {
			Char ch = context.getCharStream().nextChar();
			switch ( ch.type() ) {
				case LETTER:
					return GOTO_FAILED;
				case DIGIT:
					context.append(ch.value());
					return GOTO_ACCEPT_INT;
				case SPECIAL_CHAR:
					return GOTO_FAILED;
				case WS:
				case END_OF_STREAM:
					return GOTO_MATCHED(INT, context.getLexime());
				default:
					throw new AssertionError();
			}
		}
	},
	SIGN {
		@Override
		public TransitionOutput transit(ScanContext context) {
			Char ch = context.getCharStream().nextChar();
			char v = ch.value();
			switch ( ch.type() ) {
				case LETTER:
					return GOTO_FAILED;
				case DIGIT:
					context.append(v);
					return GOTO_ACCEPT_INT;
				case WS:
					char a = context.getLexime().charAt(0);
					Character cr = new Character(a);
					String b = new String(cr.toString());
					return GOTO_MATCHED(TokenType.fromSpecialCharactor(a), b);
				case END_OF_STREAM:
					char a2 = context.getLexime().charAt(0);
					Character cr2 = new Character(a2);
					String b2 = new String(cr2.toString());
					if(a2 == 41)
						return GOTO_MATCHED(fromSpecialCharactor(a2), b2); // 마지막에 ')'이 왔을 때 실패하지 않고 MATCHED로 가게끔
					return GOTO_FAILED;
				default:
					throw new AssertionError();
			}
		}
	},
	MATCHED {
		@Override
		public TransitionOutput transit(ScanContext context) {
			throw new IllegalStateException("at final state");
		}
	},
	FAILED{
		@Override
		public TransitionOutput transit(ScanContext context) {
			throw new IllegalStateException("at final state");
		}
	},
	EOS {
		@Override
		public TransitionOutput transit(ScanContext context) {
			return GOTO_EOS;
		}
	};
	
	abstract TransitionOutput transit(ScanContext context);
}
