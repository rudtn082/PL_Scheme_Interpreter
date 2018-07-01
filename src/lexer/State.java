package lexer;

import static lexer.TokenType.*;
import static lexer.TransitionOutput.GOTO_ACCEPT_ID;
import static lexer.TransitionOutput.GOTO_ACCEPT_INT;
import static lexer.TransitionOutput.GOTO_EOS;
import static lexer.TransitionOutput.GOTO_FAILED;
import static lexer.TransitionOutput.GOTO_MATCHED;
import static lexer.TransitionOutput.GOTO_SIGN;
import static lexer.TransitionOutput.GOTO_START;


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
					if(v == 35) { // 첫 문자가 #일 경우.
						context.append(v);
						return GOTO_ACCEPT_ID; // 그 다음에 T나 F가 나올거니까 ID로 보내준다.
					}
					else return GOTO_MATCHED(fromSpecialCharactor(v), cr.toString()); // #이 아니면 첫 번째에 문자가 오는경우는 특수문자니까 MATCHED로 보내준다.
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
				case END_OF_STREAM:
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
