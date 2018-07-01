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
			//정규 표현식을 참고하여 ch와 매칭되는 keyword를 반환하는 case문 작성
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

// 각각의 특수문자에 대한 case 추가.