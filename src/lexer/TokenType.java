package lexer;

public enum TokenType {
		INT,
		ID, QUESTION,
		TRUE, FALSE, NOT,
		PLUS, MINUS, TIMES, DIV,
		LT, GT, EQ, APOSTROPHE,
		L_PAREN, R_PAREN,
		DEFINE, LAMBDA, COND, QUOTE,
		CAR, CDR, CONS,
		ATOM_Q, NULL_Q, EQ_Q;
	
		static TokenType fromSpecialCharactor(char ch) {
			switch ( ch ) {
			//���� ǥ������ �����Ͽ� ch�� ��Ī�Ǵ� keyword�� ��ȯ�ϴ� case�� �ۼ�
			case 33:
				return NOT;
			case 39:
				return APOSTROPHE;
			case 40:
				return L_PAREN;
			case 41:
				return R_PAREN;
			case 42:
				return TIMES;
			case 43:
				return PLUS;
			case 45:
				return MINUS;
			case 47:
				return DIV;
			case 60:
				return LT;
			case 61:
				return EQ;
			case 62:
				return GT;
			case 63:
				return QUESTION;
			default:
				throw new IllegalArgumentException("unregistered char: " + ch);
			}
		}
}

// ������ Ư�����ڿ� ���� case �߰�.