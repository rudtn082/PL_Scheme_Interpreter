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
					Character cr = new Character(v); // toString()�� ��������  cr�� �������ش�.
					if(v == 35) { // ù ���ڰ� #�� ���.
						context.append(v);
						return GOTO_ACCEPT_ID; // �� ������ T�� F�� ���ðŴϱ� ID�� �����ش�.
					}
					else return GOTO_MATCHED(fromSpecialCharactor(v), cr.toString()); // #�� �ƴϸ� ù ��°�� ���ڰ� ���°��� Ư�����ڴϱ� MATCHED�� �����ش�.
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
					if(v == 63) { // ? �ϰ�쿡
						String a = context.getLexime();
						Character cr = new Character(v); // toString()�� ��������  cr�� �������ش�.
						String b = a.concat(cr.toString()); // ���࿡ null�� ?�� ������ �� �ΰ��� ���ļ� null?�� �������
						return GOTO_MATCHED(Token.ofName(b).type(), b); // MATCHED�� �����ش�.
					}
					else return GOTO_FAILED;
				case WS:
				case END_OF_STREAM:
					String temp = context.getLexime();
					return GOTO_MATCHED(Token.ofName(temp).type(), temp); //���࿡ Ű�����ϰ�츦 ó���ϱ� ���ؼ� ofName���� �����ش�.
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
