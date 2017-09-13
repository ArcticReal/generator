package parser;

import java.io.IOException;

public class Main {

	static boolean keys = false;
	static boolean controlEvent = false;
	static boolean allEntitys = false;
	static boolean searchService = false;
	static boolean generateService = true;
	static boolean test = false;
	
	public static void main(String[] args) throws IOException {


		if (allEntitys) {
			Parser parser = new Parser();
			parser.readFromXmls();
		}
		
		if (keys) {
			
			ForeignKeyParser kparser = new ForeignKeyParser();
			kparser.doIt();
		}

		if (controlEvent) {
			
			Parser parser = new Parser();
			parser.parseAll();
		}

		if (searchService) {
			
			ServiceSearcher search = new ServiceSearcher();
			search.doIt();
		}

		if (generateService) {
			
			ServiceControlGenerator generator = new ServiceControlGenerator();
			generator.doIt();
		}

		
		if (test) {
			Test test = new Test();
			test.test();
			
		}
	}

}
