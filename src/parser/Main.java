package parser;

import java.io.File;
import java.io.IOException;

public class Main {

	static boolean keys = false;
	static boolean controlEvent = true;
	static boolean allEntitys = false;
	static boolean searchService = false;
	static boolean generateService = false;
	static boolean test = false;
	

	
	public static void main(String[] args) throws IOException {

		Parser parser = new Parser();
		ForeignKeyParser kparser = new ForeignKeyParser();
		ServiceControlGenerator generator = new ServiceControlGenerator();

		
		System.out.println(System.getProperty("user.dir"));
		
		for(int i = 0; i < args.length; i++) {
			if(!args[i].contains("-")) {
				System.out.println("Syntax error!");
				showHelp();
				break;
			}
			
			if(args[i].equals("-h")) {
				showHelp();
				break;
			}
			
			if(args[i].equals("-clear")) {
				File f = new File(System.getProperty("user.dir")+ "/parse.conf");
				f.delete();
				f.createNewFile();
			}
			
			if(args[i].equals("-entity")) {
				
				String path = args[i+1];
				
				if(path == null) {
					System.out.println("Syntax error! No path!");
					showHelp();
					break;
				}
				
				allEntitys = true;
				parser.pathToXmls = path;
				
				i++;
				continue;
			}
			
			if(args[i].equals("-keys")) {
				
				String path = args[i+1];
				
				if(path == null) {
					System.out.println("Syntax error! No path!");
					showHelp();
					break;
				}

				keys = true;
				kparser.pathToXmls = path;
				
				i++;
				continue;
				
				
			}
			
			if(args[i].equals("-controlEvent")) {
				controlEvent=true;
				
			}
			
			if(args[i].equals("-service")) {
				String path = args[i+1];
				
				if(path == null) {
					System.out.println("Syntax error! No path!");
					showHelp();
					break;
				}

				generateService = true;
				generator.pathToXmls = path;
				
				i++;
				continue;
				
			}
			
			
			
		}
		
		if(args.length == 0) {
			System.out.println("Syntax error! No options!");
			showHelp();

		}

		if (allEntitys) {
			parser.readFromXmls();
		}
		
		if (keys) {
			
			kparser.doIt();
		}

		if (controlEvent) {
			
			parser.parseAll();
		}

		if (searchService) {
			
			ServiceSearcher search = new ServiceSearcher();
			search.doIt();
		}

		if (generateService) {
			
			try {
				generator.doIt();
			} catch (Exception e) {
				e.printStackTrace();
				System.out.println(e.getMessage());
			}
		}

		
		if (test) {
			Test test = new Test();
			test.test();
			
		}
	}
	
	static void showHelp() {
		
		System.out.println("Format is: Parser {<option> (<path>)}\n");
		System.out.println("Available Options are:");
		System.out.println("\t-h: shows this page\n");
		System.out.println("\t-clear: removes entries from config file\n");
		System.out.println("\t-entity <source folder>: parses all entitys from xml in <source folder> and writes them into the config file. All other tasks will depend on this config\n");
		System.out.println("\t-keys <source folder>: parses all foreign keys and primary keys from entity files in the specific Folder\n");
		System.out.println("\t-controlEvent: generates Control and Event Classes from the config file\n");
//		System.out.println("\t-searchService <source folder>: searches for all services in servicedef files in the specific folder\n");
		System.out.println("\t-service <source folder>: generates all sservice files from parsed files in <source folder>");
		
		
	}

}
