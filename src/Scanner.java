import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;

public class Scanner {
	//��� ǥ���ϱ�
	public enum TokenType{
			ID(3), INT(2);
			
			private final int finalState;
			
			TokenType(int finalState) {
				this.finalState = finalState;
				}
			int getTokenType() {
				return finalState;
			}
	}

	//����Ÿ Ÿ��
	public static class Token {
		public final TokenType type;
		public final String lexme;

		Token(TokenType type, String lexme) {
			this.type = type;
			this.lexme = lexme;
		}

		@Override
		public String toString() {
			return String.format("%s: %s", type.toString(), lexme);
		}
	}

	/*
	Programming-TM
	Control state transition of transition diagram by TM(transition matrix).
	��Array of accept(final) states���� ��� 
	 */

	private int transM[][];
	private String source;
	private StringTokenizer st;
	
	public Scanner(String source) {
		this.transM = new int[4][128];
		this.source = source == null ? "" : source;
		initTM();
	}

	private void initTM() {
		for(int i=0;i<4;i++) { // -1�� �ʱ�ȭ
			for(int j=0;j<128;j++) {
				transM[i][j] = -1;
			}
		}
		for(int i=0;i<4;i++) {
			for(int j=48;j<58;j++) { // �����϶�
				if(i==0) transM[i][j] = 2;
				if(i==1) transM[i][j] = 2;
				if(i==2) transM[i][j] = 2;
				if(i==3) transM[i][j] = 3;
			}
			for(int j=97;j<123;j++) { // �ҹ����϶�
				if(i==0) transM[i][j] = 3;
				if(i==3) transM[i][j] = 3;	
			}
			for(int j=65;j<90;j++) { // �빮���϶�
				if(i==0) transM[i][j] = 3;
				if(i==3) transM[i][j] = 3;	
			}
			transM[0][45] = 1; //'-'�϶�
		}
	}
		
		
	private Token nextToken(){	
		int stateOld = 0, stateNew;

		//��ū�� �� �ִ��� �˻�
		if(!st.hasMoreTokens()) return null;

		//�� ���� ��ū�� ����
		String temp = st.nextToken();

		Token result = null;	
		for(int i = 0; i<temp.length();i++){
			//���ڿ��� ���ڸ� �ϳ��� ������ ���� �Ǻ�
			stateNew = transM[stateOld][temp.charAt(i)];
			
			if(stateNew == -1){
				//�Էµ� ������ ���°� reject �̹Ƿ� �����޼��� ����� return��
				System.out.println(String.format("acceptState error %s\n", temp)); return null;
			}
			stateOld = stateNew;
		}
		for (TokenType t : TokenType.values()){
			if(t.getTokenType() == stateOld){
				result = new Token(t, temp);
				break;
			}
		}
		return result;
	}
	
	public List<Token> tokenize() {
		List<Token> result = new ArrayList<Token>(); 
		st = new StringTokenizer(source);	// ���Ͽ����� ���ڿ��� ��ū���� �и���Ŵ
		Token temp = nextToken();
		
		while(temp != null) {	// ���Ͽ� �ִ� ���ڿ��� null�϶� ���� �ݺ�
			result.add(temp);	//��ū���� ����Ʈ�� ����
			temp = nextToken();
		}
		return result;
	}

		
	public static void main(String[] args){
		//txt file to String
		
		FileReader FR;
		try {
			FR = new FileReader("as02.txt");
			BufferedReader BR = new BufferedReader(FR);
			String source = BR.readLine();
			Scanner s = new Scanner(source);
			List<Token> tokens = s.tokenize();
			for(int i=0; i<tokens.size();i++)
				System.out.println(tokens.get(i));
		} catch (IOException e) {
			e.printStackTrace();
		}	
	}
}
