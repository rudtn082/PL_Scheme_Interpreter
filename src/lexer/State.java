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
					Character cr = new Character(v); // toString()�� ��������  cr�� �������ش�.
					context.append(v);
					if(v == 35) { // ù ���ڰ� #�� ���.
						return GOTO_ACCEPT_ID; // �� ������ T�� F�� ���ðŴϱ� ID�� �����ش�.
					}
					else return GOTO_SIGN; // �� �� Ư������
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
					char a = context.getLexime().charAt(0);
					Character cr = new Character(a);
					String b = new String(cr.toString());
					return GOTO_MATCHED(TokenType.fromSpecialCharactor(a), b);
				case END_OF_STREAM:
					char a2 = context.getLexime().charAt(0);
					Character cr2 = new Character(a2);
					String b2 = new String(cr2.toString());
					if(a2 == 41)
						return GOTO_MATCHED(fromSpecialCharactor(a2), b2); // �������� ')'�� ���� �� �������� �ʰ� MATCHED�� ���Բ�
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
