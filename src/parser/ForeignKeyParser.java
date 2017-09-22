package parser;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class ForeignKeyParser {

	String pathToXmls = "/home/work/workspace/ControllingParser/xmls/";
	private final String writeTo = System.getProperty("user.dir")+ "/parsed_files/";
	private final Charset ENCODING = StandardCharsets.UTF_8;

	public void doIt() throws IOException {
		writePrimaryKeys();
		writeForeignKeysFrom();
		writeForeignKeysTo();
	}

	public void writePrimaryKeys() throws IOException {

		String path = writeTo + "/PKs/";
		File f = new File(path);

		f.mkdirs();

		Map<String, List<String>> pks = scanXmlsForPrimaryKeys();

		Iterator<String> it = pks.keySet().iterator();

		while (it.hasNext()) {
			String entityName = it.next();

			PrintWriter writer = new PrintWriter(path + entityName + ".txt");

			System.out.println(entityName);
			writer.println(entityName + ":");
			List<String> pkForEntity = pks.get(entityName);

			int count = 1;
			for (int i = 0; i < pkForEntity.size(); i++) {
				writer.println("\n" + count +": Primary Key Attribute: " + pkForEntity.get(i) + "\n");
				count ++;
			}

			writer.close();
			
		}

	}
	
	public Map<String, List<String>> scanXmlsForPrimaryKeys() throws IOException {
		Map<String, List<String>> returnVal = new HashMap<>();

		File f = new File(pathToXmls);
		for (File fileEntry : f.listFiles()) {
			Scanner scanner = new Scanner(Paths.get(fileEntry.getAbsolutePath()), ENCODING.name());

			String currentEntity = "";
			List<String> pk = new LinkedList<>();

			while (scanner.hasNext()) {
				String line = scanner.nextLine();
				
				if (line.contains("<view-entity entity-name=\"")) {
					while (!line.contains("</view-entity>")) {
						line = scanner.nextLine();
					}
				}

				String[] splittedLine = line.split("\"");
				if (line.contains("<entity entity-name=\"")) {
					currentEntity = splittedLine[1];
				}

				if (line.contains("<prim-key field=\"")) {
					pk.add(splittedLine[1]);
				}

				if(line.contains("</entity>")) {
					returnVal.put(currentEntity, pk);
					pk = new LinkedList<>();
				}
				
			}
			scanner.close();
			
		}
		return returnVal;
	}
	
	
	public void writeForeignKeysFrom() throws IOException {
		Parser parser = new Parser();
		parser.readFromXmls();
		String path = writeTo + "/FKs/from/";
		File f = new File(path);

		f.mkdirs();

		Map<String, List<String>> FKs = scanXmlsForForeignKeysIn();

		Iterator<String> it = FKs.keySet().iterator();

		while (it.hasNext()) {
			String entityName = it.next();

			PrintWriter writer = new PrintWriter(path + entityName + ".txt");

			System.out.println(entityName);
			writer.println(entityName + ":");
			List<String> fkForEntity = FKs.get(entityName);

			int count = 1;
			for (int i = 0; i < fkForEntity.size(); i++) {
				writer.println("\n" + count +": Foreign Key Name:		" + fkForEntity.get(i).split(":")[0].trim() + "\n	Related Entity:		"
						+ fkForEntity.get(i).split(":")[1].trim() + "\n	referenced Field:	"
						+ fkForEntity.get(i).split(":")[2].trim() + "\n");
				count ++;
			}

			writer.close();
		}

	}

	public Map<String, List<String>> scanXmlsForForeignKeysIn() throws IOException {
		Map<String, List<String>> returnVal = new HashMap<>();

		File f = new File(pathToXmls);
		for (File fileEntry : f.listFiles()) {
			Scanner scanner = new Scanner(Paths.get(fileEntry.getAbsolutePath()), ENCODING.name());

			String currentEntity = "";
			List<String> fK = new LinkedList<>();

			while (scanner.hasNext()) {
				String line = scanner.nextLine();
				if (line.contains("<view-entity entity-name=\"")) {
					while (!line.contains("</view-entity>")) {
						line = scanner.nextLine();
					}
				}

				String[] splittedLine = line.split("\"");
				if (line.contains("<entity entity-name=\"")) {
					currentEntity = splittedLine[1];
				}

				if (line.contains("<relation type=\"")) {
					String fkName = "";
					String relEntityName = "";
					String refField = "";

					for (int i = 0; i < splittedLine.length - 1; i++) {

						if (splittedLine[i].contains("fk-name=")) {
							fkName = (splittedLine[i + 1]);
						}

						if (splittedLine[i].contains("rel-entity-name=")) {
							relEntityName = (splittedLine[i + 1]);
						}

					}

					line = scanner.nextLine();
					splittedLine = line.split("\"");

					for (int i = 0; i < splittedLine.length - 1; i++) {

						if (splittedLine[i].contains("field-name=")) {
							refField = (splittedLine[i + 1]);
						}

					}

					fK.add(fkName + " : " + relEntityName + " : " + refField);

				}

				if (line.contains("</entity>")) {

					returnVal.put(currentEntity, fK);
					fK = new LinkedList<String>();
				}

			}

			scanner.close();
		}

		return returnVal;

	}

	public void writeForeignKeysTo() throws IOException {
		Parser parser = new Parser();
		parser.readFromXmls();
		Iterator<String> it = parser.readConfig().get("ENTITY_NAME").iterator();
		String path = writeTo + "/FKs/to/";
		File f = new File(path);

		f.mkdirs();
		while (it.hasNext()) {

			String nextEntity = it.next();
			//nextEntity = "Product";
			PrintWriter writer = new PrintWriter(path + nextEntity + ".txt");

			System.out.println(nextEntity);

			Map<String, String> foreignKeys = scanXmlsForForeignKeys(nextEntity);

			writer.println(nextEntity + ":");
			Iterator<String> it2 = foreignKeys.keySet().iterator();
			int count = 1;
			while (it2.hasNext()) {
				String next = it2.next();

				writer.println("\n" + count + "	Entity: " + next.split(":")[0].trim() + "\n" + "	Foreign Key Name: "
						+ next.split(":")[1].trim() + "\n" + "	related field: " + foreignKeys.get(next));
				count++;
			}

			writer.close();
			//break;
		}

	}

	public Map<String, String> scanXmlsForForeignKeys(String entityName) throws IOException {
		Map<String, String> returnVal = new HashMap<>();

		File f = new File(pathToXmls);
		for (File fileEntry : f.listFiles()) {
			Scanner scanner = new Scanner(Paths.get(fileEntry.getAbsolutePath()), ENCODING.name());

			String currentEntity = "";
			while (scanner.hasNext()) {
				String line = scanner.nextLine();
				if (line.contains("<entity entity-name=\"")) {
					String[] splittedLine = line.split("\"");
					currentEntity = splittedLine[1];
				}

				if (line.contains("<view-entity entity-name=\"")) {
					while (!line.contains("</view-entity>")) {
						line = scanner.nextLine();
					}
				}

				String fkName = "";
				String relEntityName = "";
				if (line.contains("<relation type=\"one\"")) {
					String[] splittedLine = line.split("\"");
					for (int i = 0; i < splittedLine.length - 1; i++) {

						if (splittedLine[i].contains("fk-name=")) {
							fkName = splittedLine[i + 1];
						}

						if (splittedLine[i].contains("rel-entity-name=")) {
							relEntityName = splittedLine[i + 1];
						}

					}
					line = scanner.nextLine();
					splittedLine = line.split("\"");
					String refField = "";
					for (int i = 0; i < splittedLine.length - 1; i++) {

						if (splittedLine[i].contains("field-name=")) {
							refField = splittedLine[i + 1];
						}

					}

					if (relEntityName.equals(entityName)) {

						returnVal.put(currentEntity + ": " + fkName, refField);
					}

				}

			}

			scanner.close();
		}

		return returnVal;
	}
}
