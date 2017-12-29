package parser;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.stream.Collectors;

public class Parser {

	private boolean parseEvent = true;
	private boolean parseControl = true;
	private boolean generateComQuery = true;
	private boolean generateEntityMapper = true;
	private String pathToFolder = System.getProperty("user.dir") + "/parsed_files/";
	public String pathToXmls = "/home/work/workspace/ControllingParser/xmls";
	public String pathToSource = "/home/work/workspace/ofbiz/plugins/eCommerce/src/main/java";
	private String entityName;
	private String entityNamelc;
	private String entityNamelcs;
	private String parentEntity;
	private String PK;
	private final boolean folderStructureByType = true;
	private final Charset ENCODING = StandardCharsets.UTF_8;
	private final List<String> eventTypes = new LinkedList<>();
	private List<String> parentEntities = new LinkedList<>();
	private List<String> parentRelationNames = new LinkedList<>();
	private List<String> entityNames = new LinkedList<>();
	private Map<String, String> packageNames = new HashMap<>();
	private Map<String, String> controllerMappings = new HashMap<>();
	Map<String, List<String>> FKs;
	Map<String, List<String>> PKs;

	public Parser() throws IOException {
		eventTypes.add("Added");
		eventTypes.add("Updated");
		eventTypes.add("Deleted");
		eventTypes.add("Found");

		ForeignKeyParser keyParser = new ForeignKeyParser(null);
		FKs = keyParser.scanXmlsForForeignKeysIn();
		PKs = keyParser.scanXmlsForPrimaryKeys();

	}

	public void parseAll() throws IOException {
		parentEntities = readConfig().get("PARENT_ENTITIES");
		GenerateClass comGen = new GenerateClass(pathToFolder);

		for (int p = 0; p < parentEntities.size(); p++) {
			parentEntity = parentEntities.get(p);
			comGen.setParentEntity(parentEntity);
			entityNames = readConfig().get(parentEntity);
			parsePosiibleParents(entityNames);
			try {

				for (int i = 0; i < entityNames.size(); i++) {
					this.entityName = firstToUpperCase(entityNames.get(i));
					this.entityNamelc = firstToLowerCase(entityName);
					this.entityNamelcs = firstToLowerCase(entityName) + "s";

					File f;
					if (folderStructureByType) {

						this.pathToFolder = System.getProperty("user.dir") + "/parsed_files/event/";

					} else {
						this.pathToFolder = System.getProperty("user.dir") + "/parsed_files/" + entityName + "/";

					}
					f = new File(pathToFolder);
					f.mkdirs();

					PK = getPK();
					generatePackages();
					comGen.setPackageNames(packageNames);

					if (parseEvent) {

						for (int j = 0; j < eventTypes.size(); j++) {

							parseEventClass(eventTypes.get(j));

						}
					}

					if (folderStructureByType) {
						this.pathToFolder = System.getProperty("user.dir") + "/parsed_files/Command/";
						f = new File(pathToFolder);
						f.mkdirs();

						this.pathToFolder = System.getProperty("user.dir") + "/parsed_files/Query/";
						f = new File(pathToFolder);
						f.mkdirs();

						this.pathToFolder = System.getProperty("user.dir") + "/parsed_files/control/";
						f = new File(pathToFolder);
						f.mkdirs();

						if (parseControl) {
							parseControllerClass();
						}

						if (generateEntityMapper) {
							comGen.generateEntityClass(entityName, parentEntity,
									firstToLowerCase(parentEntity) + "-entitymodel.xml");
							comGen.generateMapper(entityName, parentEntity,
									firstToLowerCase(parentEntity) + "-entitymodel.xml");

						}
						// comGen.generateAllEntitysandMappers();

						if (generateComQuery) {

							comGen.generateAllCommandsAndQuerys(entityName, parentEntity, PK);
						}
					}
				}

			} catch (IOException e) {
				e.printStackTrace();
				System.out.println(e.getMessage());
			} finally {
				pathToFolder = System.getProperty("user.dir") + "/parsed_files/";

			}
		}
	}

	public void generateCommandAndQuery() {

	}

	public void parseEventClass(String eventType) throws IOException {

		PrintWriter writer = new PrintWriter(pathToFolder + entityName + eventType + ".java", "UTF-8");

		String writeVal = getEventClassHeader(eventType);

		if (eventType.equals("Found")) {
			writeVal += "	private List<" + entityName + "> " + entityNamelcs + ";\n\n";
			writeVal += "	public " + entityName + eventType + "(List<" + entityName + "> " + entityNamelcs + ") {\n";
			writeVal += "		this." + entityNamelc + "s = " + entityNamelcs + ";\n	}\n\n";
			writeVal += "	public List<" + entityName + "> get" + entityName + "s()" + "	{\n" + "		return "
					+ entityNamelcs + ";\n	}\n\n" + "}";

		} else {
			if (eventType.equals("Added")) {
				writeVal += "" + "	private " + entityName + " added" + entityName + ";\n";
			}
			writeVal += "	private boolean success;\n\n";
			if (eventType.equals("Added")) {
				writeVal += "	public " + entityName + "Added(" + entityName + " added" + entityName
						+ ", boolean success){\n" + "		this.added" + entityName + " = added" + entityName + ";\n";
			} else {

				writeVal += "	public " + entityName + eventType + "(boolean success) {\n";
			}
			writeVal += "		this.success = success;\n	}\n\n";
			writeVal += "	public boolean isSuccess()" + "	{\n" + "		return success;\n	}\n\n";
			if (eventType.equals("Added")) {
				writeVal += "	public " + entityName + " getAdded" + entityName + "() {\n" + "		return added"
						+ entityName + ";\n" + "	}\n" + "\n";

			}
			writeVal += "}";

		}

		writer.println(writeVal);
		writer.close();

	}

	public String getEventClassHeader(String eventType) {
		String returnVal = "";
		String packageName = "";

		returnVal = packageNames.get(PackageTypes.EVENT) + "\n\n";

		/*
		 * if (entityName.equals(parentEntity)) { returnVal +=
		 * "package com.skytala.eCommerce.domain." + entityNamelc + ".event;\n\n";
		 * 
		 * } else { returnVal += "package com.skytala.eCommerce.domain." +
		 * firstToLowerCase(parentEntity) + ".relations." + entityNamelc +
		 * ".event;\n\n";
		 * 
		 * }
		 */
		if (eventType.equals("Found")) {
			returnVal += "import java.util.List;\n\n";
		}
		returnVal += "import com.skytala.eCommerce.framework.pubsub.Event;\n\n";

		packageName = packageNames.get(PackageTypes.MODEL).replaceFirst("package", "import").replace(";", ".");
		returnVal += packageName + entityName + ";\n";
		/*
		 * if (entityName.equals(parentEntity)) { returnVal +=
		 * "import com.skytala.eCommerce.domain." + entityNamelc + ".model." +
		 * entityName + ";\n" + "";
		 * 
		 * } else { returnVal += "import com.skytala.eCommerce.domain." +
		 * firstToLowerCase(parentEntity) + ".relations." + entityNamelc + ".model." +
		 * entityName + ";\n" + "";
		 * 
		 * }
		 */

		returnVal += "public class " + entityName + eventType + " implements Event{\n\n";
		return returnVal;
	}

	public void parseControllerClass() throws IOException {

		PrintWriter writer = new PrintWriter(pathToFolder + entityName + "Controller.java", "UTF-8");

		String writeVal = getControllerClassHeader() + "\n";

		writeVal += getControllerClassConstructor() + "\n" + getControllerClassFindMethod() + "\n" + "\n"
				+ getControllerClassCreateMethods() + "\n" + getControllerClassUpdateMethods() + "\n" + "\n"
				+ getControllerClassFindMethodUpdated() + "\n" + getControllerClassDeleteMethodUpdated() + "\n";

		writeVal += "}";
		writer.println(writeVal);

		writer.close();


	}



	public String getControllerClassHeader() {

		String returnVal = packageNames.get(PackageTypes.CONTROL) + "\n";
		String packageName = "";


		returnVal += "\n" + "import java.io.BufferedReader;\n" + "import java.io.IOException;\n"
				+ "import java.io.InputStreamReader;\n" + "import java.util.HashMap;\n" + "import java.util.Iterator;\n"
				+ "import java.util.List;\n" + "import java.util.Map;\n" + "import java.util.Set;\n" + "\n"
				+ "import javax.servlet.http.HttpServletRequest;\n" + "\n"
				+ "import org.springframework.http.HttpStatus;\n" + "import org.springframework.http.MediaType;\n"
				+ "import org.springframework.http.ResponseEntity;\n"
				+ "import org.springframework.web.bind.annotation.*;\n" + "\n"
				+ "import com.google.common.base.Splitter;\n";

		packageName = packageNames.get(PackageTypes.COMMAND).replaceFirst("package", "import").replace(";", ".");
		returnVal += packageName + "Add" + entityName + ";\n" + packageName + "Delete" + entityName + ";\n"
				+ packageName + "Update" + entityName + ";\n";

		packageName = packageNames.get(PackageTypes.EVENT).replaceFirst("package", "import").replace(";", ".");
		returnVal += packageName + entityName + "Added;\n" + packageName + entityName + "Deleted;\n" + packageName
				+ entityName + "Found;\n" + packageName + entityName + "Updated;\n";

		packageName = packageNames.get(PackageTypes.MAPPER).replaceFirst("package", "import").replace(";", ".");
		returnVal += packageName + entityName + "Mapper;\n";

		packageName = packageNames.get(PackageTypes.MODEL).replaceFirst("package", "import").replace(";", ".");
		returnVal += packageName + entityName + ";\n";

		packageName = packageNames.get(PackageTypes.QUERY).replaceFirst("package", "import").replace(";", ".");
		returnVal += packageName + "Find" + entityName + "sBy;\n"
				+ "import com.skytala.eCommerce.framework.exceptions.RecordNotFoundException;\n"
				+ "import com.skytala.eCommerce.framework.pubsub.Scheduler;\n\n"
				+ "import static com.skytala.eCommerce.framework.pubsub.ResponseUtil.*;\n\n"
				+ "@RestController\n"
				//+ "@CrossOrigin\n"
				;

		String mapping = getMapping();
		controllerMappings.put(entityName, mapping);


		returnVal += "@RequestMapping(\"" + mapping + "/" + entityNamelcs + "\")\n" + "public class " + entityName + "Controller {\n\n"
				+ "	private static Map<String, RequestMethod> validRequests = new HashMap<>();\n";

		return returnVal;
	}

	public String getMapping(String entityName){

		if(controllerMappings.isEmpty()){
			try {
				parseAll();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		String mapping = controllerMappings.get(entityName).replaceFirst("/", "");
		mapping = mapping + "/" + firstToLowerCase(entityName) + "s";
		return mapping;
	}

	private String getMapping() {

		String mapping = packageNames.get(PackageTypes.CONTROL);
		mapping = mapping.replace("com.skytala.eCommerce.domain.", "%%%/").split("%%%")[1];
		mapping = mapping.replace(".", "/");
		mapping = mapping.replace(";", "");
		mapping = mapping.replace("/relations/","/");
		mapping = mapping.replace("/control/","/");
		mapping = mapping.replaceAll("\\Q/\\E[a-zA-Z]*$", "");
		return mapping;
	}

	public String getControllerClassConstructor() {
		String returnVal = "";

		returnVal += "	public " + entityName + "Controller() {\n" + "\n"
				+ "		validRequests.put(\"find\", RequestMethod.GET);\n"
				+ "		validRequests.put(\"add\", RequestMethod.POST);\n"
				+ "		validRequests.put(\"update\", RequestMethod.PUT);\n"
				+ "		validRequests.put(\"removeById\", RequestMethod.DELETE);\n" + "	}\n";

		return returnVal;
	}

	public String getControllerClassFindMethod() {

		String returnVal = "";

		// method description
		returnVal += "	/**\n" + "	 * \n" + "	 * @param allRequestParams\n"
				+ "	 *            all params by which you want to find a " + entityName + "\n"
				+ "	 * @return a List with the " + entityName + "s\n" + "	 * @throws Exception \n"
				+ "	 */\n" +
				// method
				"	@GetMapping(\"/find\")\n"
				+ "	public ResponseEntity<List<" + entityName + ">> find" + entityName
				+ "sBy(@RequestParam(required = false) Map<String, String> allRequestParams) throws Exception {\n\n"
				+ "		Find" + entityName + "sBy query = new Find" + entityName + "sBy(allRequestParams);\n"
				+ "		if (allRequestParams == null) {\n"
				+ "			query.setFilter(new HashMap<>());\n"
				+ "		}\n\n"
				+ "		List<" + entityName + "> " + entityNamelcs + " =((" + entityName
				+ "Found) Scheduler.execute(query).data()).get" + entityName + "s();\n\n"
				+ "		return ResponseEntity.ok().body(" + entityNamelcs + ");\n\n"
				+ "	}";

		return returnVal;

	}

	public String getControllerClassCreateMethods() {
		String returnVal = "";

		returnVal += "	/**\n" + "	 * creates a new " + entityName + " entry in the ofbiz database\n"
				+ "	 * \n" + "	 * @param " + entityNamelc + "ToBeAdded\n" + "	 *            the " + entityName
				+ " thats to be added\n" + "	 * @return true on success; false on fail\n" + "	 */\n"
				+ "	@RequestMapping(method = RequestMethod.POST, value = \"/add\", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)\n"
				+ "	public ResponseEntity<" + entityName + "> create" + entityName + "(@RequestBody " + entityName + " "
				+ entityNamelc + "ToBeAdded) throws Exception {\n" + "\n"
				+ "		Add" + entityName + " command = new Add" + entityName + "(" + entityNamelc + "ToBeAdded);\n"
				+ "		" + entityName + " " + entityNamelc + " = ((" + entityName + "Added) Scheduler.execute(command).data()).getAdded"
				+ entityName + "();\n" + "		\n" + "		if (" + entityNamelc + " != null) \n"
				+ "			return successful(" + entityNamelc + ");\n"
				+ "		else \n"
				+ "			return conflict(null);\n" + "	}\n"
				+ "";

		return returnVal;
	}

	public String getAttrType(String entityName, String attrName) {

		return null;
	}

	public String getPK() {
		List<String> fKeys = new LinkedList<>();
		List<String> fKeysAll = FKs.get(entityName);
		List<String> pKeys = PKs.get(entityName);
		String PK = null;
		System.out.println("entity: " + entityName);

		if (pKeys.contains(entityNamelc + "Id")) {
			PK = entityNamelc + "Id";
		} else {

			for (int i = 0; i < fKeysAll.size(); i++) {
				String FK = fKeysAll.get(i).split(":")[3].trim();
				fKeys.add(FK);
			}
			if (fKeys.containsAll(pKeys)) {
				PK = null;
			} else {

				String possibleKey = null;
				for (int j = 0; j < pKeys.size(); j++) {
					if (!fKeys.contains(pKeys.get(j))) {

						possibleKey = pKeys.get(j);
						if (possibleKey.contains("Date") || possibleKey.contains("Quantity")
								|| possibleKey.contains("sequenceNum")) {
							continue;
						}
						if (PK != null) {

							PK = null;
							break;
						} else {

							PK = possibleKey;

						}

					}
				}
			}
		}

		return PK;
	}

	public String getControllerClassUpdateMethods() throws IOException {

		// String PK = getPK();
		String PKuc;
		if (PK != null)
			PKuc = firstToUpperCase(PK);
		else
			PKuc = null;

		String returnVal = "";
		if (PK != null) {

			returnVal += "	/**\n" + "	 * Updates the " + entityName
					+ " with the specific Id\n" + "	 * \n" + "	 * @param " + entityNamelc + "ToBeUpdated\n"
					+ "	 *            the " + entityName + " thats to be updated\n"
					+ "	 * @return true on success, false on fail\n" + "	 * @throws Exception \n" + "	 */\n"
					+ "	@RequestMapping(method = RequestMethod.PUT, value = \"/{" + PK
					+ "}\", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)\n"
					+ "	public ResponseEntity<String> update" + entityName + "(@RequestBody " + entityName + " "
					+ entityNamelc + "ToBeUpdated,\n" + "			@PathVariable String " + PK
					+ ") throws Exception {\n\n" + "		" + entityNamelc + "ToBeUpdated.set" + PKuc + "(" + PK
					+ ");\n\n";
		} else {

			returnVal += "	/**\n" + "	 * Updates the " + entityName + " with the specific Id\n" + "	 * \n"
					+ "	 * @param " + entityNamelc + "ToBeUpdated\n" + "	 *            the " + entityName
					+ " thats to be updated\n" + "	 * @return true on success, false on fail\n"
					+ "	 * @throws Exception \n" + "	 */\n"
					+ "	@RequestMapping(method = RequestMethod.PUT, value = \"/{" + PK
					+ "Val}\", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)\n"
					+ "	public ResponseEntity<String> update" + entityName + "(@RequestBody " + entityName + " "
					+ entityNamelc + "ToBeUpdated,\n" + "			@PathVariable String " + PK
					+ "Val) throws Exception {\n\n" + "//		" + entityNamelc + "ToBeUpdated.set" + PKuc + "(" + PK
					+ ");\n\n";

		}
		returnVal += "" + "		Update" + entityName + " command = new Update" + entityName + "(" + entityNamelc
				+ "ToBeUpdated);\n\n" + "		try {\n" + "			if(((" + entityName
				+ "Updated) Scheduler.execute(command).data()).isSuccess()) \n"
				+ "				return noContent();	\n"
				+ "		} catch (RecordNotFoundException e) {\n"
				+ "			return notFound();\n" + "		}\n\n"
				+ "		return conflict();\n" + "	}";

		return returnVal;
	}

	public String getControllerClassErrorMethod() {
		String returnVal = "	@RequestMapping(value = (\" ** \"))\n"
				+ "	public ResponseEntity<Object> returnErrorPage(HttpServletRequest request) {\n\n"
				+ "		String usedUri = request.getRequestURI();\n"
				+ "		String[] splittedString = usedUri.split(\"/\");\n" + "\n"
				+ "		String usedRequest = splittedString[splittedString.length - 1];\n" + "\n"
				+ "		if (validRequests.containsKey(usedRequest)) {\n"
				+ "			String returnVal = \"Error: request method \" + request.getMethod() + \" not allowed for \\\"\" + usedUri\n"
				+ "					+ \"\\\"!\\n\"" + " + \"Please use \" + validRequests.get(usedRequest) + \"!\";\n"
				+ "\n" + "			return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(returnVal);\n"
				+ "		}\n" + "\n"
				+ "		String returnVal = \"Error 404: Page not found! Valid pages are: \\\"eCommerce/api/"
				+ entityNamelc + "/\\\" plus one of the following: \"\n" + "				+ \"\";\n" + "\n"
				+ "		Set<String> keySet = validRequests.keySet();\n"
				+ "		Iterator<String> it = keySet.iterator();\n" + "\n" + "		while (it.hasNext()) {\n"
				+ "			returnVal += \"\\\"\" + it.next() + \"\\\"\";\n" + "			if (it.hasNext())\n"
				+ "				returnVal += \", \";\n" + "		}\n" + "\n" + "		returnVal += \"!\";\n" + "\n"
				+ "		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(returnVal);\n" + "\n" + "	}\n" + "";

		return returnVal;
	}

	public String getControllerClassFindMethodUpdated() {
		String retVal = "";

		retVal += "	@GetMapping(\"/{" + entityNamelc + "Id}\")\n"
				+ "	public ResponseEntity<" + entityName + "> findById(@PathVariable String " + entityNamelc
				+ "Id) throws Exception {\n"
				+ "		HashMap<String, String> requestParams = new HashMap<String, String>();\n"
				+ "		requestParams.put(\"" + entityNamelc + "Id\", " + entityNamelc + "Id);\n" + "		try {\n\n"
				+ "			List<" + entityName + "> found" + entityName + " = find" + entityName + "sBy(requestParams).getBody();\n"
				+ "			if(found" + entityName + ".size()==1){"
				+ "				return successful(found" + entityName + ".get(0));\n"
				+ "			}else{\n"
				+ "				return notFound();\n"
				+ "			}\n"
				+ "		} catch (RecordNotFoundException e) {\n\n"
				+ "			return notFound();\n" + "		}\n\n"
				+ "	}\n";

		return retVal;
	}

	public String getControllerClassDeleteMethodUpdated() {
		String retVal = "";

		retVal += "	@DeleteMapping(\"/{" + entityNamelc + "Id}\")\n"
				+ "	public ResponseEntity<String> delete" + entityName + "ByIdUpdated(@PathVariable String "
				+ entityNamelc + "Id) throws Exception {\n" + "		Delete" + entityName + " command = new Delete"
				+ entityName + "(" + entityNamelc + "Id);\n" + "\n" + "		try {\n" + "			if (((" + entityName
				+ "Deleted) Scheduler.execute(command).data()).isSuccess())\n"
				+ "				return noContent();\n"
				+ "		} catch (RecordNotFoundException e) {\n"
				+ "			return notFound();\n" + "		}\n" + "\n"
				+ "		return conflict();\n" + "\n" + "	}\n" + "";

		return retVal;
	}

	public Map<String, List<String>> readConfig() throws IOException {

		Path path = Paths.get("/home/work/workspace/ControllingParser/parse.conf");
		List<String> configList = new LinkedList<>();
		Scanner scanner = new Scanner(path, ENCODING.name());

		while (scanner.hasNext()) {
			configList.add(scanner.nextLine());
		}

		scanner.close();

		Map<String, List<String>> config = new HashMap<>();
		String[] splittedString;
		for (int i = 0; i < configList.size(); i++) {
			splittedString = configList.get(i).split("=");
			String[] splittedSubString = splittedString[1].split(",");
			List<String> values = new LinkedList<>();
			for (int j = 0; j < splittedSubString.length; j++) {
				values.add(splittedSubString[j].trim());
			}

			config.put(splittedString[0].trim(), values);
		}

		return config;

	}

	public void readFromXmls() throws IOException {
		Map<String, List<String>> entities = new HashMap<>();
		List<String> entityNames = new LinkedList<>();
		List<String> parentEntities = new LinkedList<>();

		String path = pathToXmls;
		File f = new File(path);

		for (File fileEntry : f.listFiles()) {
			if (fileEntry.isDirectory()) {
				continue;
			}
			parentEntities.add(firstToUpperCase(fileEntry.getName().replace("-entitymodel", "").replace(".xml", "")));

			Scanner scanner = new Scanner(Paths.get(fileEntry.getAbsolutePath()), ENCODING.name());

			while (scanner.hasNext()) {
				String line = scanner.nextLine();
				if (line.contains("<entity entity-name=")) {
					String[] splittedString;
					splittedString = line.trim().split("\"");
					entityNames.add(splittedString[1]);
				}
			}
			scanner.close();
			entities.put(firstToUpperCase(fileEntry.getName().replace("-entitymodel", "").replace(".xml", "")),
					entityNames);
			entityNames = new LinkedList<>();
		}

		String writeVal = "PARENT_ENTITIES = ";
		for (int i = 0; i < parentEntities.size(); i++) {
			writeVal += parentEntities.get(i);
			if (i < parentEntities.size() - 1) {
				writeVal += ", ";
			}
		}

		for (int j = 0; j < parentEntities.size(); j++) {
			entityNames = entities.get(parentEntities.get(j));

			writeVal += "\n" + parentEntities.get(j) + " = ";
			for (int i = 0; i < entityNames.size(); i++) {
				writeVal += entityNames.get(i);
				if (i < entityNames.size() - 1) {
					writeVal += ", ";
				}
			}

		}
		PrintWriter writer = new PrintWriter(pathToFolder.replace("/parsed_files", "") + "/parse.conf");
		writer.print(writeVal);
		writer.close();

	}

	public String firstToLowerCase(String string) {

		return string.replaceFirst(string.substring(0, 1), string.toLowerCase().substring(0, 1));

	}

	public String firstToUpperCase(String string) {
		return string.replaceFirst(string.substring(0, 1), string.toUpperCase().substring(0, 1));

	}

	public void moveAllFiles() throws IOException {
		Iterator<String> it = readConfig().get("PARENT_ENTITIES").iterator();

		while (it.hasNext()) {
			Iterator<String> subIt = readConfig().get(it.next()).iterator();
			while (subIt.hasNext()) {
				entityName = subIt.next();
				entityNamelc = firstToLowerCase(entityName);
				moveToPackages();

			}
		}
	}

	public void moveToPackages() throws IOException {
		System.out.println("backup files in " + pathToSource);
		backUpBlacklistedFiles(pathToSource);
		changeMappingsOfBlacklistedFiles();

		File parsedFiles = new File(System.getProperty("user.dir") + "/parsed_files");
		if (parsedFiles.isDirectory()) {
			for (File f : parsedFiles.listFiles()) {

				String folderName = f.getName();
				System.out.println("\nmoving " + folderName + "...");
				if (folderName.equals("Command") || folderName.equals("control") || folderName.equals("entity")
						|| folderName.equals("event") || folderName.equals("mapper") || folderName.equals("Query")
						|| folderName.equals("tests") || folderName.equals("service_controller")|| folderName.equals("backup")) {
					if(!folderName.equals("control")) {
						
						//continue;
					}
					int count = 0;
					for (File fi : f.listFiles()) {
						// System.out.println("Trying to move " + fi.getName());
						if (fi.isDirectory()) {
							System.err.println(fi.getName() + " is not a file, not moving!");
							continue;
						}
						if (isBlacklisted(fi.getName())&&!folderName.equals("backup")) {
							System.err.println(fi.getName() + " is blacklisted, not moving!");
							continue;
						}

						String name = fi.getName();

						String URIfolder = pathToSource + "/" + parsePackage(fi) + "/";
						String URI = pathToSource + "/" + parsePackage(fi) + "/" + name;

						File fDest = new File(URIfolder);
						fDest.mkdirs();
						fDest = new File(URI);




						if(fDest.exists()){
							fDest.delete();
						}

						fi.renameTo(fDest);
						++count;

					}
					System.out.println(count);
				} else {
					System.out.println(folderName
							+ "\tfiles should not be moved to project, cancelling, press esc. to cancel cancel. (just kidding, by the time you could react it would already be cancelled)");
				}

			}
		}
	}

	public void changeMappingsOfBlacklistedFiles() throws IOException {
		File backupFolder = new File(pathToFolder + "backup");
		for(File f : backupFolder.listFiles()){
			try{

				File generatedController = new File(pathToFolder + "control/" + f.getName());
				String mapping = Files.readAllLines(generatedController.toPath())
						.stream()
						.filter((line)-> line.contains("@RequestMapping"))
						.collect(Collectors.toList()).get(0);

				if(mapping!=null){
					int row = 0;
					List<String> allLines = Files.readAllLines(f.toPath());
					for (String line : allLines){
						if(line.contains("@RequestMapping")){
							allLines.add(row, mapping);
							allLines.remove(row+1);
							break;
						}
						row++;
					}
					Files.write(f.toPath(), allLines);
				}

			}catch(Exception e){
				continue;
			}

		}

	}

	public void backUpBlacklistedFiles(String URL) throws FileNotFoundException {

		//System.out.println("backup files in " + URL);

		if(URL == null) {
			URL = pathToSource;
		}
		File f = new File(URL);
		if(f.isDirectory()) {
			for(File fi : f.listFiles()) {
				backUpBlacklistedFiles(fi.getAbsolutePath());
			}
		}else if(f.exists()){
			if(isBlacklisted(f.getName())) {
				File fBackUp = new File(pathToFolder + "backup/");
				if(!fBackUp.exists()) {
					fBackUp.mkdirs();
				}
				fBackUp = new File(fBackUp.getAbsolutePath()+"/"+f.getName());
				f.renameTo(fBackUp);
			}
		}
	}

	public void parsePosiibleParents(List<String> relationNames) {
		parentRelationNames = new LinkedList<>();
		for (int i = 0; i < relationNames.size(); i++) {
			String possibleParent = relationNames.get(i);
			for (int j = 0; j < relationNames.size(); j++) {
				if (possibleParent.contains(relationNames.get(j)) && !possibleParent.equals(relationNames.get(j))) {
					possibleParent = null;
					break;
				}
			}
			if (possibleParent != null) {
				parentRelationNames.add(possibleParent);

			}
		}
	}

	public void mergeBackupFiles() throws FileNotFoundException {
		
		//File folderWithContent = new File(pathToFolder + "backup");
		File folderWithImports = new File(pathToFolder);
		
		for(File folderWithImportFiles : folderWithImports.listFiles()) {
			if(folderWithImportFiles.isDirectory()) {
				if(folderWithImportFiles.getName().equals("backup")) {
					System.out.println("skipped backup folder");
					continue;
				}
				System.out.println("searching through folder: " + folderWithImportFiles.getName());
				for(File fileWithImports : folderWithImportFiles.listFiles()){
					if(isBlacklisted(fileWithImports.getName())) {
						System.out.println("found blacklisted file");
						
						File fileWithContent = new File(pathToFolder + "backup/" + fileWithImports.getName());
						if(fileWithContent.exists()) {
							
							generateMergedFile(fileWithImports, fileWithContent);
						}
						
					}
				}
			}
		}
		
	}
	
	public void generateMergedFile(File fileWithImports, File fileWithContent) throws FileNotFoundException {
		
		System.out.println("merge file: " + fileWithContent.getName());
		
		String writeVal = "";
		
		Scanner sc = new Scanner(fileWithImports);
		while(sc.hasNextLine()) {
			String line = sc.nextLine();
			if(line.contains("class ")) {
				break;
			}
			
			writeVal += line + "\n";
			
		}
		sc.close();
		
		
		
		sc = new Scanner(fileWithContent);
		String contents = "";
		while(sc.hasNextLine()) {
			String line = sc.nextLine();
			if(line.contains("class ")) {
				contents = "";
			}
			
			contents += line + "\n";
			
		}
		sc.close();
		
		writeVal += contents;
		
		PrintWriter writer = new PrintWriter(fileWithContent);
		writer.print(writeVal);
		writer.close();
		
		
	}
	
	public void generatePackages() {
		packageNames = new HashMap<>();

		if (parentEntity.equals(entityName)) {
			packageNames.put(PackageTypes.COMMAND,
					"package com.skytala.eCommerce.domain." + firstToLowerCase(entityName) + ".command;");
			packageNames.put(PackageTypes.CONTROL,
					"package com.skytala.eCommerce.domain." + firstToLowerCase(entityName) + ";");
			packageNames.put(PackageTypes.EVENT,
					"package com.skytala.eCommerce.domain." + firstToLowerCase(entityName) + ".event;");
			packageNames.put(PackageTypes.MAPPER,
					"package com.skytala.eCommerce.domain." + firstToLowerCase(entityName) + ".mapper;");
			packageNames.put(PackageTypes.MODEL,
					"package com.skytala.eCommerce.domain." + firstToLowerCase(entityName) + ".model;");
			packageNames.put(PackageTypes.QUERY,
					"package com.skytala.eCommerce.domain." + firstToLowerCase(entityName) + ".query;");

		} else {

			String parentRelation = getParentRelation();
			if (parentRelation != null) {

				packageNames.put(PackageTypes.COMMAND, ("package com.skytala.eCommerce.domain." 
						+ firstToLowerCase(parentEntity) + ".relations."
						+ firstToLowerCase(parentRelation) + ".command."
						+ firstToLowerCase(entityName
											.replace(parentRelation, "")) + ";")
						.replace(".class;", ".clazz;")
						.replace(".package;", ".paccage;"));
				
				packageNames.put(PackageTypes.CONTROL, ("package com.skytala.eCommerce.domain." 
						+ firstToLowerCase(parentEntity) + ".relations."
						+ firstToLowerCase(parentRelation) + ".control."
						+ firstToLowerCase(entityName
											.replace(parentRelation, "")) + ";")
						.replace(".class;", ".clazz;")
						.replace(".package;", ".paccage;"));
				
				packageNames.put(PackageTypes.EVENT, ("package com.skytala.eCommerce.domain."
						+ firstToLowerCase(parentEntity) + ".relations." 
						+ firstToLowerCase(parentRelation) + ".event."
						+ firstToLowerCase(entityName
											.replace(parentRelation, "")) + ";")
						.replace(".class;", ".clazz;")
						.replace(".package;", ".paccage;"));
				
				packageNames.put(PackageTypes.MAPPER, ("package com.skytala.eCommerce.domain."
						+ firstToLowerCase(parentEntity) + ".relations." 
						+ firstToLowerCase(parentRelation) + ".mapper."
						+ firstToLowerCase(entityName
											.replace(parentRelation, "")) + ";")
						.replace(".class;", ".clazz;")
						.replace(".package;", ".paccage;"));
				
				packageNames.put(PackageTypes.MODEL, ("package com.skytala.eCommerce.domain."
						+ firstToLowerCase(parentEntity) + ".relations." 
						+ firstToLowerCase(parentRelation) + ".model."
						+ firstToLowerCase(entityName
											.replace(parentRelation, "")) + ";")
						.replace(".class;", ".clazz;")
						.replace(".package;", ".paccage;"));
				
				packageNames.put(PackageTypes.QUERY, ("package com.skytala.eCommerce.domain."
						+ firstToLowerCase(parentEntity) + ".relations." 
						+ firstToLowerCase(parentRelation) + ".query."
						+ firstToLowerCase(entityName
											.replace(parentRelation, "")) + ";")
						.replace(".class;", ".clazz;")
						.replace(".package;", ".paccage;"));

			} else {

				packageNames.put(PackageTypes.COMMAND, "package com.skytala.eCommerce.domain."
						+ firstToLowerCase(parentEntity) + ".relations." + entityNamelc + ".command;");
				
				packageNames.put(PackageTypes.CONTROL, "package com.skytala.eCommerce.domain."
						+ firstToLowerCase(parentEntity) + ".relations." + entityNamelc + ";");
				
				packageNames.put(PackageTypes.EVENT, "package com.skytala.eCommerce.domain."
						+ firstToLowerCase(parentEntity) + ".relations." + entityNamelc + ".event;");
				
				packageNames.put(PackageTypes.MAPPER, "package com.skytala.eCommerce.domain."
						+ firstToLowerCase(parentEntity) + ".relations." + entityNamelc + ".mapper;");
				
				packageNames.put(PackageTypes.MODEL, "package com.skytala.eCommerce.domain."
						+ firstToLowerCase(parentEntity) + ".relations." + entityNamelc + ".model;");
				
				packageNames.put(PackageTypes.QUERY, "package com.skytala.eCommerce.domain."
						+ firstToLowerCase(parentEntity) + ".relations." + entityNamelc + ".query;");
				

			}

		}
	}

	public String getParentRelation() {

		for (int i = 0; i < parentRelationNames.size(); i++) {

			if (entityName.contains(parentRelationNames.get(i)) && !entityName.equals(parentRelationNames.get(i))) {
				return parentRelationNames.get(i);
			}

		}
		return null;

	}

	/*
	 * public void lookThroughRelations(String filename) { File f = new
	 * File(filename); if(!f.exists()) { return; } List<String> relationNames = new
	 * LinkedList<>(); parentRelationNames = new LinkedList<>();
	 * 
	 * for(File fi: f.listFiles()) { relationNames.add(fi.getName()); } for(int i =
	 * 0; i < relationNames.size(); i++) { String possibleParent =
	 * relationNames.get(i); for(int j = 0; j < relationNames.size(); j++) {
	 * if(possibleParent.contains(relationNames.get(j))&&!possibleParent.equals(
	 * relationNames.get(j))) { possibleParent = null; break; } } if(possibleParent
	 * != null) { parentRelationNames.add(possibleParent);
	 * //System.out.println("Parent: " + possibleParent); } } Iterator<String> it =
	 * parentRelationNames.iterator(); while(it.hasNext()) { String
	 * currentParentRelation = it.next(); for(int i = 0; i < relationNames.size();
	 * i++) { if(relationNames.get(i).startsWith(currentParentRelation)&&!
	 * currentParentRelation.equals(relationNames.get(i))) {
	 * 
	 * //String currentFolder = filename + relationNames.get(i); //String
	 * newLocation = filename + currentParentRelation;
	 * 
	 * } }
	 * 
	 * }
	 * 
	 * }
	 */

	public void deleteUnneccesaryFiles() throws IOException {
		if (true)
			;
		// return;
		String sourceLocation = "";
		Map<String, List<String>> conf = readConfig();

		Iterator<String> it = conf.get("PARENT_ENTITIES").iterator();

		while (it.hasNext()) {
			String parentEntity = it.next();

			sourceLocation = "com/skytala/eCommerce/domain/" + firstToLowerCase(parentEntity) + "/relations/";

			File f = new File(pathToSource + "/" + sourceLocation);
			if (!f.exists()) {
				continue;
			}
			for (File fi : f.listFiles()) {
				deleteFilesRecursive(fi.getAbsolutePath().replace(firstToLowerCase(parentEntity) + "/relations/", ""));

			}

		}
	}

	public void deleteFilesRecursive(String filename) {
		File f = new File(filename);
		if (f.exists()) {

			for (File sf : f.listFiles()) {
				if (!sf.isDirectory()) {
					sf.delete();
				} else {
					deleteFilesRecursive(sf.getAbsolutePath());

				}
			}
			f.delete();
		}
	}

	public boolean isBlacklisted(String filename) throws FileNotFoundException {

		String pathBlacklist = System.getProperty("user.dir") + "/blacklist.conf";
		Scanner sc = new Scanner(new File(pathBlacklist));

		while (sc.hasNextLine()) {
			String line = sc.nextLine();
			if (line.equals(filename)) {
				sc.close();
				return true;
			}
		}

		sc.close();
		return false;
	}

	public String parsePackage(File f) throws FileNotFoundException {

		if (f.isDirectory())
			return null;
		Scanner sc = new Scanner(f);
		while (sc.hasNextLine()) {
			String line = sc.nextLine();
			if (line.contains("package")) {
				line = line.replace("package ", "").replace(";", "").replace(".", "/");
				sc.close();
				return line;
			}

		}
		sc.close();

		return "";
	}

	public void setControl(boolean control) {
		this.parseControl = control;
	}

	public void setEvent(boolean event) {
		this.parseEvent = event;
	}

	public void generateEntityAndMapper(boolean entMap) {
		generateEntityMapper = entMap;

	}

	public void setgenerateCommandQuery(boolean comQuery) {
		generateComQuery = comQuery;
	}
}
