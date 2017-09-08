package parser;

import java.io.IOException;

public class Main {

	public static void main(String[] args) throws IOException {

		/*ForeignKeyParser kparser = new ForeignKeyParser();
		
		kparser.doIt();
		
		Parser parser = new Parser();
		parser.parseAll();*/
		
		ServiceSearcher search = new ServiceSearcher();
		search.doIt();
		
		Test test = new Test();
		test.test();
		
		//ServiceControlGenerator generator = new ServiceControlGenerator();
		//generator.doIt();
	}

}
