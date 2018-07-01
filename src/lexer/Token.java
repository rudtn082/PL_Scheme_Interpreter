package lexer;

import java.util.HashMap;
import java.util.Map;

public class Token {
	private final TokenType type;
	private final String lexme;
	
	static Token ofName(String lexme) {
		TokenType type = KEYWORDS.get(lexme);
			if ( type != null ) {
				return new Token(type, lexme);
			}
			else if( lexme.startsWith("#") ) { // ���࿡ #���� ������ lexme�� ��쿡
		         if( lexme.endsWith("T") ) { // T�� ������ 
		            return new Token(TokenType.TRUE, lexme); // TURE�� ��ūŸ�԰� �� lexme���� ��ū�� �����ؼ� �������ش�.
		         }
		         else if( lexme.endsWith("F") ) { // F�� ������
		            return new Token(TokenType.FALSE, lexme); // FALSE�� ��ūŸ�԰� �� lexme���� ��ū�� �����ؼ� �������ش�.
		         }
		         else  throw new ScannerException("invalid ID=" + lexme); 
		      }
			else {
	            return new Token(TokenType.ID, lexme);
	         }
		}
	
	Token(TokenType type, String lexme) {
		this.type = type;
		this.lexme = lexme;
	}
	public TokenType type() {
		TokenType type = KEYWORDS.get(this.lexme); // ��ūŸ�Կ��� �´� Ű���带 �����ͼ�
		if ( type != null ) { // ���࿡ Ű���� �ϰ�쿡��
			Token temp = ofName(this.lexme); // ofName���� ������
			return temp.type; // �� ���� �������ش�.
		}
		return this.type; // Ű���尡 �ƴϸ� �׳� type�� �������ش�.
	}
	public String lexme() {
		return this.lexme;
	}
	@Override
	public String toString() {
		return String.format("%s(%s)", type, lexme);
	}
	private static final Map<String,TokenType> KEYWORDS = new HashMap<>();
	static {
		KEYWORDS.put("define", TokenType.DEFINE);
		KEYWORDS.put("lambda", TokenType.LAMBDA);
		KEYWORDS.put("cond", TokenType.COND);
		KEYWORDS.put("quote", TokenType.QUOTE);
		KEYWORDS.put("not", TokenType.NOT);
		KEYWORDS.put("cdr", TokenType.CDR);
		KEYWORDS.put("car", TokenType.CAR);
		KEYWORDS.put("cons", TokenType.CONS);
		KEYWORDS.put("eq?", TokenType.EQ_Q);
		KEYWORDS.put("null?", TokenType.NULL_Q);
		KEYWORDS.put("atom?", TokenType.ATOM_Q);
	}
}