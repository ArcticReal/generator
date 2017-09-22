package parser;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

public class AttributeTypeParser {

	private final String pathToXmls = "/home/work/workspace/ControllingParser/xmls/";
	private final String writeTo = System.getProperty("user.dir")+ "/parsed_files/";
	private final Charset ENCODING = StandardCharsets.UTF_8;

	private List<String> attrTypes = new LinkedList<>();
	private List<String> entityName = new LinkedList<>();
	private List<String> attributeName = new LinkedList<>();

	
	public AttributeTypeParser() {

	}

	public void doIt() throws IOException {

		getDifAttributes();
		parseIt();
		

	}

	public void parseIt() throws IOException {
		
		String writeVal = "";
		
		writeVal += "package com.skytala.ecommerce.test;\n\n"
				+ "public class AttributeTester{\n"
				+ "	private List<String> attrTypes = new LinkedList<>();\n"
				+ "	private List<String> entityName = new LinkedList<>();\n"
				+ "	private List<String> attributeName = new LinkedList<>();\n"
				+ ""
				+ "	public AttributeTester(){\n\n"
				+ "		Delegator delegator = DelegatorFactory.getDelegator(\"default\");\n";
		for(int i = 0; i < attrTypes.size(); i++) {
			writeVal += "		this.attrTypes.add(\"" + attrTypes.get(i) + "\");\n"
					+ "		this.entityName.add(\"" + entityName.get(i) + "\");\n"
					+ "		this.attributeName.add(\"" + attributeName.get(i) + "\");\n\n";
		}
		
		
		writeVal += "		for(int i = 0; i < attrTypes.size(); i++) {\n\n"
				+ "			delegator.findOne(entityName.get(i), false, UtilMisc.toMap(attributeName.get(i),\"\"));\n" + 
				""
				+ "		}"
				+ "	}\n"
				+ ""
				+ ""
				+ "}";
		
		PrintWriter writer = new PrintWriter(writeTo + "AttributeTester.java", ENCODING.name());
		
		writer.print(writeVal);
		writer.close();
	}
	
	public void getDifAttributes() throws IOException {

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
				if (line.contains("type=\"") && line.contains("<field name=\"")) {
					String[] splittedLine = line.split("\"");
					if (splittedLine[2].contains("type")) {
						if (!attrTypes.contains(splittedLine[3])) {

							attrTypes.add(splittedLine[3]);
							entityName.add(currentEntity);
							attributeName.add(splittedLine[1]);

						}

					} else if (splittedLine[4].contains("type")) {
						if (!attrTypes.contains(splittedLine[5])) {

							attrTypes.add(splittedLine[5]);
							entityName.add(currentEntity);
							attributeName.add(splittedLine[1]);

						}

					}

				}

			}

			scanner.close();
		}
		System.out.println(Integer.toString(attrTypes.size()) + ": " + attrTypes);
		System.out.println(Integer.toString(entityName.size()) + ": " + entityName);
		System.out.println(Integer.toString(attributeName.size()) + ": " + attributeName);

	}
}
