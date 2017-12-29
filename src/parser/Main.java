package parser;

import java.io.File;
import java.io.IOException;

public class Main {

	private static boolean keys = false;
	private static boolean FKfrom = false;
	private static boolean entityFields = false;
	private static boolean allEntitys = false;
	private static boolean controlEventComQuEM = false;
	private static boolean commandQuery = false;
	private static boolean entityMapper = false;
	private static boolean control = true;
	private static boolean event = false;
	private static boolean searchService = false;
	private static boolean generateService = false;
	private static boolean test = false;
	private static boolean tests = false;
	private static boolean moveFiles = true;
	private static boolean deleteFiles = false;
	private static boolean integratePackages = false;
	private static boolean backUp = false;
	private static boolean mergeBackUp = false;
	private static boolean changeMappingsOfBackups = false;
	
	
	public static void main(String[] args) throws Exception {

		Parser parser = new Parser();
		ForeignKeyParser kparser = new ForeignKeyParser(parser);
		ServiceControlGenerator generator = new ServiceControlGenerator();

		
		
		System.out.println(System.getProperty("user.dir"));

		for (int i = 0; i < args.length; i++) {
			if (!args[i].contains("-")) {
				System.out.println("Syntax error!");
				showHelp();
				break;
			}

			if (args[i].equals("-h")) {
				showHelp();
				break;
			}

			if (args[i].equals("-clear")) {
				File f = new File(System.getProperty("user.dir") + "/parse.conf");
				f.delete();
				f.createNewFile();
			}

			if (args[i].equals("-entity")) {

				String path = args[i + 1];

				if (path == null) {
					System.out.println("Syntax error! No path!");
					showHelp();
					break;
				}

				allEntitys = true;
				parser.pathToXmls = path;

				i++;
				continue;
			}

			if (args[i].equals("-keys")) {

				String path = args[i + 1];

				if (path == null) {
					System.out.println("Syntax error! No path!");
					showHelp();
					break;
				}

				keys = true;
				kparser.pathToXmls = path;

				i++;
				continue;

			}

			if (args[i].equals("-controlEvent")) {
				controlEventComQuEM = true;

			}

			if (args[i].equals("-service")) {
				String path = args[i + 1];

				if (path == null) {
					System.out.println("Syntax error! No path!");
					showHelp();
					break;
				}

				generateService = true;
				generator.pathToXmls = path;

				i++;

			}

		}

		
		
		if (args.length == 0) {
			System.out.println("Syntax error! No options!");
			showHelp();

		}


		if (allEntitys) {
			parser.readFromXmls();
		}

		if (keys) {

			kparser.doIt();
		}

		if(FKfrom) {
			kparser.writeForeignKeysFrom();
		}

		if(entityFields){
			kparser.writeEntityFields();
		}


		if (controlEventComQuEM) {
			parser.parseAll();
		}else {
			if(control||event||commandQuery||entityMapper) {
				parser.setControl(control);
				parser.setEvent(event);	
				parser.setgenerateCommandQuery(commandQuery);
				parser.generateEntityAndMapper(entityMapper);
				
				parser.parseAll();
			}		
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
		
		if(tests) {
			TestClassGenerator test = new TestClassGenerator();

			test.doIt();
		}

		if(backUp) {
			parser.backUpBlacklistedFiles(null);
		}

		if(changeMappingsOfBackups){
			parser.changeMappingsOfBlacklistedFiles();
		}

		if(mergeBackUp) {
			parser.mergeBackupFiles();
		}
		
		if(moveFiles) {
			//parser.moveAllFiles();
			parser.moveToPackages();
		}
		

		if(deleteFiles) {
			parser.deleteUnneccesaryFiles();
		}
	}

	static void showHelp() {

		System.out.println("Format is: Parser {<option> (<path>)}\n");
		System.out.println("Available Options are:");
		System.out.println("\t-h: shows this page\n");
		System.out.println("\t-clear: removes entries from config file\n");
		System.out.println(
				"\t-entity <source folder>: parses all entitys from xml in <source folder> and writes them into the config file. All other tasks will depend on this config\n");
		System.out.println(
				"\t-keys <source folder>: parses all foreign keys and primary keys from entity files in the specific Folder\n");
		System.out.println("\t-controlEvent: generates Control and Event Classes from the config file\n");
		// System.out.println("\t-searchService <source folder>: searches for all
		// services in servicedef files in the specific folder\n");
		System.out.println(
				"\t-service <source folder>: generates all sservice files from parsed files in <source folder>");

	}

}
