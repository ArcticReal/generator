package parser;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class Parser {

	private String pathToFolder = "/home/work/workspace/ControllingParser/parsed_files/";
	private String entityName;
	private String entityNamelc;
	private String entityNamelcs;
	private  final boolean folderStructureByType = true;
	private final Charset ENCODING = StandardCharsets.UTF_8;
	private final List<String> eventTypes = new LinkedList<>();

	public Parser() throws IOException {
		eventTypes.add("Added");
		eventTypes.add("Updated");
		eventTypes.add("Deleted");
		eventTypes.add("Found");

	}

	public void parseAll() throws IOException {
		readFromXmls();
		List<String> entityNames = readConfig().get("ENTITY_NAME");

		
		try {

			for (int i = 0; i < entityNames.size(); i++) {
				this.entityName = entityNames.get(i);
				this.entityNamelc = firstToLowerCase(entityName);
				this.entityNamelcs = firstToLowerCase(entityName) + "s";

				File f;
				if (folderStructureByType) {
					
					this.pathToFolder = "/home/work/workspace/ControllingParser/parsed_files/event/";
					
				}else {
					this.pathToFolder = "/home/work/workspace/ControllingParser/parsed_files/" + entityName + "/";

				}
				f = new File(pathToFolder);
				f.mkdirs();
				
				for (int j = 0; j < eventTypes.size(); j++) {
					
					parseEventClass(eventTypes.get(j));
					
				}
				
				if (folderStructureByType) {
			
					this.pathToFolder = "/home/work/workspace/ControllingParser/parsed_files/control/";
	
					f = new File(pathToFolder);
					f.mkdirs();
				}
				
				parseControllerClass();
			}
			
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println(e.getMessage());
		}
	}

	public void parseEventClass(String eventType) throws IOException {

		PrintWriter writer = new PrintWriter(pathToFolder + entityName + eventType + ".java", "UTF-8");

		String writeVal = getEventClassHeader(eventType);

		if (eventType.equals("Found")) {
			writeVal += "	private List<" + entityName + "> " + entityNamelcs + ";\n\n";
			writeVal += "	public " + entityName + eventType + "(List<" + entityName + "> " + entityNamelcs + ") {\n";
			writeVal += "		this.set" + entityName + "s(" + entityNamelcs + ");\n	}\n\n";
			writeVal += "	public List<" + entityName + "> get" + entityName + "s()" + "	{\n" + "		return "
					+ entityNamelcs + ";\n	}\n\n" + "	public void set" + entityName + "s(List<" + entityName + "> "
					+ entityNamelcs + ")" + "	{\n" + "		this." + entityNamelcs + " = " + entityNamelcs + ";\n"
					+ "	}\n" + "}";

		} else {
			writeVal += "	private boolean success;\n\n";
			writeVal += "	public " + entityName + eventType + "(boolean success) {\n";
			writeVal += "		this.setSuccess(success);\n	}\n\n";
			writeVal += "	public boolean isSuccess()" + "	{\n" + "		return success;\n	}\n\n"
					+ "	public void setSuccess(boolean success)" + "	{\n" + "		this.success = success;\n"
					+ "	}\n" + "}";
		}

		writer.println(writeVal);
		writer.close();

	}

	public String getEventClassHeader(String eventType) {
		String returnVal = "";
		returnVal += "package com.skytala.eCommerce.event;\n\n";
		if(eventType.equals("Found")) {
			returnVal += "import java.util.List;\n\n" + 
					"import com.skytala.eCommerce.entity.Party;\n" + 
					"";
		}
		returnVal += "import com.skytala.eCommerce.control.Event;\n\n";
		returnVal += "public class " + entityName + eventType + " implements Event{\n\n";
		return returnVal;
	}

	
	public void parseControllerClass() throws IOException {

		PrintWriter writer = new PrintWriter(pathToFolder + entityName + "Controller.java", "UTF-8");
		
		String writeVal = getControllerClassHeader() + "\n";
		
		writeVal += getControllerClassConstructor() + "\n" +
					getControllerClassFindMethod() + "\n" +
					getControllerClassSendMessageMethod("Found") + "\n" + 
					getControllerClassCreateMethods() + "\n" +
					getControllerClassUpdateMethods() + "\n" +
					getControllerClassDeleteMethod() + "\n" +
					getControllerClassSendMessageMethod("Changed") + "\n" +
					getControllerClassErrorMethod();
		
		
		writeVal += "}";
		writer.println(writeVal);
	
		writer.close();
	}
	
	public String getControllerClassHeader() {
		
		String returnVal = "";
		
		returnVal += 	"package com.skytala.eCommerce.control;\n\n" + 
				"import java.io.BufferedReader;\n" + 
				"import java.io.IOException;\n" + 
				"import java.io.InputStreamReader;\n" + 
				"import java.util.HashMap;\n" + 
				"import java.util.Iterator;\n" + 
				"import java.util.List;\n" + 
				"import java.util.Map;\n" + 
				"import java.util.Set;\n" + 
				"\n" + 
				"import javax.servlet.http.HttpServletRequest;\n" + 
				"\n" + 
				"import org.springframework.web.bind.annotation.RequestMapping;\n" + 
				"import org.springframework.web.bind.annotation.RequestMethod;\n" + 
				"import org.springframework.web.bind.annotation.RequestParam;\n" + 
				"import org.springframework.web.bind.annotation.RestController;\n" + 
				"\n" + 
				"import com.google.common.base.Splitter;\n" + 
				"import com.skytala.eCommerce.command.Add" + entityName + ";\n" + 
				"import com.skytala.eCommerce.command.Delete" + entityName + ";\n" + 
				"import com.skytala.eCommerce.command.Update" + entityName + ";\n" + 
				"import com.skytala.eCommerce.entity." + entityName + ";\n" + 
				"import com.skytala.eCommerce.entity." + entityName + "Mapper;\n" + 
				"import com.skytala.eCommerce.event." + entityName + "Added;\n" + 
				"import com.skytala.eCommerce.event." + entityName + "Deleted;\n" + 
				"import com.skytala.eCommerce.event." + entityName + "Found;\n" + 
				"import com.skytala.eCommerce.event." + entityName + "Updated;\n" +
				"import com.skytala.eCommerce.query.Find" + entityName + "sBy;\n" + 
						"\n" + 
						"@RestController\n" + 
						"@RequestMapping(\"/api/" + firstToLowerCase(entityName) + "\")\n" + 
						"public class " + entityName + "Controller {\n\n" + 
						"	private static int requestTicketId = 0;\n" + 
						"	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();\n" + 
						"	private static Map<Integer, List<" + entityName + ">> queryReturnVal = new HashMap<>();\n" + 
						"	private static Map<String, RequestMethod> validRequests = new HashMap<>();\n";
		
		
		return returnVal;
	}

	public String getControllerClassConstructor() {
		String returnVal = "";
		
		returnVal += "	public " + entityName + "Controller() {\n" + 
				"\n" + 
				"		validRequests.put(\"find\", RequestMethod.GET);\n" + 
				"		validRequests.put(\"add\", RequestMethod.POST);\n" + 
				"		validRequests.put(\"update\", RequestMethod.PUT);\n" + 
				"		validRequests.put(\"removeById\", RequestMethod.DELETE);\n" + 
				"\n" + 
				"	}\n" + 
				"";
		
		return returnVal;
	}
	
	public String getControllerClassFindMethod() {
		
		String returnVal = "";
		
		//method description
		returnVal += 	"	/**\n" + 
						"	 * \n" + 
						"	 * @param allRequestParams\n" + 
						"	 *            all params by which you want to find a " + entityName + "\n" + 
						"	 * @return a List with the " + entityName + "s\n" + 
						"	 */\n" + 
		//method
						"	@RequestMapping(method = RequestMethod.GET, value = \"/find\")\n" + 
						"	public List<" + entityName + "> find" + entityName + "sBy(@RequestParam Map<String, String> allRequestParams) {\n\n" + 
						"		Find" + entityName + "sBy query = new Find" + entityName + "sBy(allRequestParams);\n\n" + 
						"		int usedTicketId;\n\n" + 
						"		synchronized (" + entityName + "Controller.class) {\n" + 
						"			usedTicketId = requestTicketId;\n" + 
						"			requestTicketId++;\n" + 
						"		}\n" + 
						"		Broker.instance().subscribe(" + entityName + "Found.class,\n" + 
						"				event -> send" + entityName + "sFoundMessage(((" + entityName + "Found) event).get" + entityName + "s(), usedTicketId));\n\n" + 
						"		query.execute();\n\n" + 
						"		while (!queryReturnVal.containsKey(usedTicketId)) {\n\n" + 
						"		}\n" + 
						"		return queryReturnVal.remove(usedTicketId);\n\n" + 
						"	}\n" + 
						"";
		
		
		return returnVal;
		
	}

	public String getControllerClassCreateMethods() {
		String returnVal = "";
		
		returnVal += 	"	/**\n" + 
						"	 * \n" + 
						"	 * this method will only be called by Springs DispatcherServlet\n" + 
						"	 * \n" + 
						"	 * @param request\n" + 
						"	 *            HttpServletRequest\n" + 
						"	 * @return true on success; false on fail\n" + 
						"	 */\n" + 
						"	@RequestMapping(method = RequestMethod.POST, value = \"/add\", consumes = \"application/x-www-form-urlencoded\")\n" + 
						"	public boolean create" + entityName + "(HttpServletRequest request) {\n\n" + 
						"		" + entityName + " " + entityNamelc + "ToBeAdded = new " + entityName + "();\n" + 
						"		try {\n" + 
						"			" + entityNamelc + "ToBeAdded = " + entityName + "Mapper.map(request);\n" + 
						"		} catch (Exception e) {\n" + 
						"			System.out.println(e.getMessage());\n" + 
						"			e.printStackTrace();\n" + 
						"			return false;\n" + 
						"		}\n\n" + 
						"		return this.create" + entityName + "(" + entityNamelc + "ToBeAdded);\n\n" + 
						"	}\n\n" + 
						"	/**\n" + 
						"	 * creates a new " + entityName + " entry in the ofbiz database\n" + 
						"	 * \n" + 
						"	 * @param " + entityNamelc + "ToBeAdded\n" + 
						"	 *            the " + entityName + " thats to be added\n" + 
						"	 * @return true on success; false on fail\n" + 
						"	 */\n" + 
						"	public boolean create" + entityName + "(" + entityName + " " + entityNamelc + "ToBeAdded) {\n" + 
						"\n" + 
						"		Add" + entityName + " com = new Add" + entityName + "(" + entityNamelc + "ToBeAdded);\n" + 
						"		int usedTicketId;\n" + 
						"\n" + 
						"		synchronized (" + entityName + "Controller.class) {\n" + 
						"\n" + 
						"			usedTicketId = requestTicketId;\n" + 
						"			requestTicketId++;\n" + 
						"		}\n" + 
						"		Broker.instance().subscribe(" + entityName + "Added.class,\n" + 
						"				event -> send" + entityName + "ChangedMessage(((" + entityName + "Added) event).isSuccess(), usedTicketId));\n" + 
						"\n" + 
						"		try {\n" + 
						"			Scheduler.instance().schedule(com).executeNext();\n" + 
						"		} catch (Exception e) {\n" + 
						"			System.out.println(e.getMessage());\n" + 
						"			e.printStackTrace();\n" + 
						"			return false;\n" + 
						"		}\n" + 
						"		while (!commandReturnVal.containsKey(usedTicketId)) {\n" + 
						"		}\n" + 
						"\n" + 
						"		return commandReturnVal.remove(usedTicketId);\n" + 
						"\n" + 
						"	}\n" + 
						"";
		
		
		return returnVal;
	}

	public String getControllerClassUpdateMethods() {
		
		String returnVal = 	"	/**\n" + 
							"	 * this method will only be called by Springs DispatcherServlet\n" + 
							"	 * \n" + 
							"	 * @param request HttpServletRequest object\n" + 
							"	 * @return true on success, false on fail\n" + 
							"	 */\n" + 
							"	@RequestMapping(method = RequestMethod.PUT, value = \"/update\", consumes = \"application/x-www-form-urlencoded\")\n" + 
							"	public boolean update" + entityName + "(HttpServletRequest request) {\n\n" + 
							"		BufferedReader br;\n" + 
							"		String data = null;\n" + 
							"		Map<String, String> dataMap = null;\n\n" + 
							"		try {\n" + 
							"			br = new BufferedReader(new InputStreamReader(request.getInputStream()));\n" + 
							"			if (br != null) {\n" + 
							"				data = java.net.URLDecoder.decode(br.readLine(), \"UTF-8\");\n" + 
							"			}\n" + 
							"		} catch (IOException e1) {\n" + 
							"			e1.printStackTrace();\n" + 
							"			return false;\n" + 
							"		}\n\n" + 
							"		dataMap = Splitter.on('&').trimResults().withKeyValueSeparator(Splitter.on('=').limit(2).trimResults())\n" + 
							"				.split(data);\n\n" + 
							"		" + entityName + " " + entityNamelc + "ToBeUpdated = new " + entityName + "();\n\n" + 
							"		try {\n" + 
							"			" + entityNamelc + "ToBeUpdated = " + entityName + "Mapper.mapstrstr(dataMap);\n" + 
							"		} catch (Exception e) {\n" + 
							"			e.printStackTrace();\n" + 
							"			return false;\n" + 
							"		}\n\n" + 
							"		return update" + entityName + "(" + entityNamelc + "ToBeUpdated);\n\n" + 
							"	}\n\n" + 
							"	/**\n" + 
							"	 * Updates the " + entityName + " with the specific Id\n" + 
							"	 * \n" + 
							"	 * @param " + entityNamelc + "ToBeUpdated the " + entityName + " thats to be updated\n" + 
							"	 * @return true on success, false on fail\n" + 
							"	 */\n" + 
							"	public boolean update" + entityName + "(" + entityName + " " + entityNamelc + "ToBeUpdated) {\n\n" + 
							"		Update" + entityName + " com = new Update" + entityName + "(" + entityNamelc + "ToBeUpdated);\n\n" + 
							"		int usedTicketId;\n\n" + 
							"		synchronized (" + entityName + "Controller.class) {\n\n" + 
							"			usedTicketId = requestTicketId;\n" + 
							"			requestTicketId++;\n" + 
							"		}\n" + 
							"		Broker.instance().subscribe(" + entityName + "Updated.class,\n" + 
							"				event -> send" + entityName + "ChangedMessage(((" + entityName + "Updated) event).isSuccess(), usedTicketId));\n\n" + 
							"		try {\n" + 
							"			Scheduler.instance().schedule(com).executeNext();\n" + 
							"		} catch (Exception e) {\n" + 
							"			System.out.println(e.getMessage());\n" + 
							"			e.printStackTrace();\n" + 
							"			return false;\n" + 
							"		}\n" + 
							"		while (!commandReturnVal.containsKey(usedTicketId)) {\n" + 
							"		}\n\n" + 
							"		return commandReturnVal.remove(usedTicketId);\n" + 
							"	}\n" + 
							"";
		
		
		
		return returnVal;
	}
	
	public String getControllerClassDeleteMethod() {
		String returnVal = "	/**\n" + 
				"	 * removes a " + entityName + " from the database\n" + 
				"	 * \n" + 
				"	 * @param " + entityNamelc + "Id:\n" + 
				"	 *            the id of the " + entityName + " thats to be removed\n" + 
				"	 * \n" + 
				"	 * @return true on success; false on fail\n" + 
				"	 * \n" + 
				"	 */\n" + 
				"	@RequestMapping(method = RequestMethod.DELETE, value = \"/removeById\")\n" + 
				"	public boolean delete" + entityNamelc + "ById(@RequestParam(value = \"" + entityNamelc + "Id\") String " + entityNamelc + "Id) {\n\n" + 
				"		Delete" + entityName + " com = new Delete" + entityName + "(" + entityNamelc + "Id);\n\n" + 
				"		int usedTicketId;\n\n" + 
				"		synchronized (" + entityName + "Controller.class) {\n\n" + 
				"			usedTicketId = requestTicketId;\n" + 
				"			requestTicketId++;\n" + 
				"		}\n" + 
				"		Broker.instance().subscribe(" + entityName + "Deleted.class,\n" + 
				"				event -> send" + entityName + "ChangedMessage(((" + entityName + "Deleted) event).isSuccess(), usedTicketId));\n\n" + 
				"		try {\n" + 
				"			Scheduler.instance().schedule(com).executeNext();\n" + 
				"		} catch (Exception e) {\n" + 
				"			System.out.println(e.getMessage());\n" + 
				"			e.printStackTrace();\n" + 
				"			return false;\n" + 
				"		}\n" + 
				"		while (!commandReturnVal.containsKey(usedTicketId)) {\n" + 
				"		}\n\n" + 
				"		return commandReturnVal.remove(usedTicketId);\n" + 
				"	}\n" + 
				"";
		
		
		return returnVal;
	}
	
	public String getControllerClassErrorMethod() {
		String returnVal = "	@RequestMapping(value = (\" * \"))\n" + 
				"	public String returnErrorPage(HttpServletRequest request) {\n\n" + 
				"		String usedUri = request.getRequestURI();\n" + 
				"		String[] splittedString = usedUri.split(\"/\");\n" + 
				"\n" + 
				"		String usedRequest = splittedString[splittedString.length - 1];\n" + 
				"\n" + 
				"		if (validRequests.containsKey(usedRequest)) {\n" + 
				"			return \"Error: request method \" + request.getMethod() + \" not allowed for \\\"\" + usedUri + \"\\\"!\\n\"\n" + 
				"					+ \"Please use \" + validRequests.get(usedRequest) + \"!\";\n" + 
				"\n" + 
				"		}\n" + 
				"\n" + 
				"		String returnVal = \"Error 404: Page not found! Valid pages are: \\\"eCommerce/api/" + entityNamelc + "/\\\" plus one of the following: \"\n" + 
				"				+ \"\";\n" + 
				"\n" + 
				"		Set<String> keySet = validRequests.keySet();\n" + 
				"		Iterator<String> it = keySet.iterator();\n" + 
				"\n" + 
				"		while (it.hasNext()) {\n" + 
				"			returnVal += \"\\\"\" + it.next() + \"\\\"\";\n" + 
				"			if (it.hasNext())\n" + 
				"				returnVal += \", \";\n" + 
				"		}\n" + 
				"\n" + 
				"		returnVal += \"!\";\n" + 
				"\n" + 
				"		return returnVal;\n" + 
				"\n" + 
				"	}\n" + 
				"";
		
		return returnVal;
	}
	
	public String getControllerClassSendMessageMethod(String messageName) {
		String returnVal = "";
		
		if (messageName.equals("Changed")) {
			returnVal += 	"	public void send" + entityName + "ChangedMessage(boolean success, int usedTicketId) {\n" + 
							"		commandReturnVal.put(usedTicketId, success);\n" + 
							"	}\n";
		}else {
			returnVal += 	"	public void send" + entityName + "sFoundMessage(List<" + entityName + "> " + entityNamelcs + ", int usedTicketId) {\n" + 
							"		queryReturnVal.put(usedTicketId, " + entityNamelcs + ");\n" + 
							"	}\n";
		}
		
		return returnVal;
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
		List<String> entityNames = new LinkedList<>();
		
		String path = "/home/work/workspace/ControllingParser/xmls";
		File f = new File(path);
		
		for (File fileEntry : f.listFiles()) {
			Scanner scanner = new Scanner(Paths.get(fileEntry.getAbsolutePath()), ENCODING.name());
			
			while(scanner.hasNext()) {
				String line = scanner.nextLine();
				if(line.contains("<entity entity-name=")) {
					String[] splittedString;
					splittedString = line.trim().split("\"");
					entityNames.add(splittedString[1]);
				}
			}
			scanner.close();
		}
		
		String writeVal = "ENTITY_NAME = ";
		for (int i = 0; i < entityNames.size(); i++) {
			writeVal += entityNames.get(i);
			if(i < entityNames.size()-1) {
				writeVal += ", ";
			}
		}
		PrintWriter writer = new PrintWriter(path + "/../parse.conf");
		writer.print(writeVal);
		writer.close();
		
	}
	
	public String firstToLowerCase(String string) {

		return string.replaceFirst(string.substring(0, 1), string.toLowerCase().substring(0, 1));

	}
}
