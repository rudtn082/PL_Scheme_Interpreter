package parser.parse;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.stream.Stream;

public class ParserMain {

	public static final void main(String... args) throws Exception {
		ClassLoader cloader = ParserMain.class.getClassLoader();
		File file = new File(cloader.getResource("as05.txt").getFile());
		CuteParser cuteParser = new CuteParser(file);
		NodePrinter.getPrinter(System.out).prettyPrint(cuteParser.parseExpr());
	}
}
