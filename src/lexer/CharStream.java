package lexer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;

class CharStream {
	private String reader;
	private Character cache;
	private int temp = 0;
	
	CharStream(String reader) {
		this.reader = reader;
		this.cache = null;
	}
	
	Char nextChar() {
		if ( cache != null ) {
			char ch = cache;
			cache = null;
			
			return Char.of(ch);
		}
		else {
			try {
				if ( temp > reader.length()-1 ) {
					temp = 0;
					return Char.end();
				}
				else {
					int ch = reader.charAt(temp);
					temp++;
					return Char.of((char)ch);
				}
			}
			catch ( Exception e ) {
				throw new ScannerException("" + e);
			}
		}
	}
	
	void pushBack(char ch) {
		cache = ch;
	}
}
