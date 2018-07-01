package lexer;

public enum TokenType {
	INT, ID,
	TRUE, FALSE, NOT,
	PLUS, MINUS, TIMES, DIV,
	LT, GT, EQ,
	L_PAREN, R_PAREN,
	DEFINE, LAMBDA, COND, QUOTE,
	CAR, CDR, CONS,
	ATOM_Q, NULL_Q, EQ_Q;

	static TokenType fromSpecialCharactor(char ch) {
		switch ( ch ) {
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
		default:
			throw new IllegalArgumentException("unregistered char: " + ch);
		}
	}
}

//각각의 특수문자에 대한 case 추가.