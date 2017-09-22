package parser;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

public class ServiceControlGenerator {

	private String pathToFolder = System.getProperty("user.dir")+ "/parsed_files/service_controller/";
	String pathToXmls = "/home/work/ofbiz_Miscellaneous/parsed/";
	private final Charset ENCODING = StandardCharsets.UTF_8;
	private Set<String> descriptions = new HashSet<>();
	private List<String> doubleDescr = new LinkedList<>();

	public void doIt() throws Exception {

		File destination = new File(pathToFolder);
		destination.mkdirs();

		File directory = new File(pathToXmls);

		int count = 0;
		
		for (File f : directory.listFiles()) {
			String className = "";
			try {
				className = getDescriptionToClassname(f.getAbsolutePath());
				
			}catch(Exception e) {
				if(e.getMessage().contains("No description found")) {
					className = count +"StandardServiceController";
				}
			}

			
			Map<String, Map<Boolean, Map<String, String>>> allServices = readAllServices(f.getAbsolutePath());

			String wrVal = "";

			wrVal += getControllerHeader(className);

			Iterator<String> it = allServices.keySet().iterator();

			while (it.hasNext()) {
				String serviceName = it.next();

				if (serviceName.equals("generateQRCodeImage")) {
					System.out.println(serviceName);
					System.out.println(className);
					System.out.println(f.getAbsolutePath());
					System.out.println("\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n");
				}
				Map<Boolean, Map<String, String>> attributes = allServices.get(serviceName);

				Map<String, String> params = attributes.get(false);
				Map<String, String> paramsOpt = attributes.get(true);

				wrVal += getControllerMethod(serviceName, params, paramsOpt);

			}

			wrVal += getControllerErrorPage() + "\n}";

			count++;
			System.out.println(count + ":");

			System.out.println(f.getAbsolutePath());
			System.out.println(className + "\n");

			PrintWriter writer = new PrintWriter(pathToFolder + className + ".java");

			writer.println(wrVal);
			writer.close();
		}

		for (int i = 0; i < doubleDescr.size(); i++) {

			System.out.println(doubleDescr.get(i));
		}

	}

	public Map<String, Map<Boolean, Map<String, String>>> readAllServices(String path) throws IOException {
		Map<String, Map<Boolean, Map<String, String>>> allServices = new HashMap<>();
		Map<Boolean, Map<String, String>> attributes = new HashMap<>();
		Map<String, String> attributesNOpt = new HashMap<>();
		Map<String, String> attributesOpt = new HashMap<>();

		String serviceName = "";

		Scanner scanner = new Scanner(Paths.get(path), ENCODING.name());

		while (scanner.hasNext()) {
			String line = scanner.nextLine();

			if (line.contains("Servicename: \"")) {
				if (!serviceName.equals("")) {
					attributes.put(true, attributesOpt);
					attributes.put(false, attributesNOpt);
					allServices.put(serviceName, attributes);

					attributesOpt = new HashMap<>();
					attributesNOpt = new HashMap<>();
					attributes = new HashMap<>();

				}
				serviceName = line.split("\"")[1];
				while (scanner.hasNextLine()) {
					line = scanner.nextLine();
					if (line.contains("Attr: \"")) {
						if (line.contains("optional:\"false\"")) {
							attributesNOpt.put(line.split("\"")[1], line.split("\"")[3]);
						} else {
							attributesOpt.put(line.split("\"")[1], line.split("\"")[3]);

						}
					} else {
						break;
					}
				}

			}
		}
		scanner.close();

		attributes.put(true, attributesOpt);
		attributes.put(false, attributesNOpt);
		allServices.put(serviceName, attributes);

		return allServices;
	}

	public String getControllerHeader(String className) {

		String retVal = "";
		retVal += "" + "package com.skytala.eCommerce.service;\n\n" + "import java.math.BigDecimal;\n"
				+ "import java.sql.Timestamp;\n" + "import java.util.List;\n" + "import java.util.HashMap;\n"
				+ "import java.util.Map;\n\n" + "import javax.servlet.http.HttpSession;\n\n"
				+ "import org.apache.ofbiz.service.GenericServiceException;\n"
				+ "import org.apache.ofbiz.service.LocalDispatcher;\n"
				+ "import org.apache.ofbiz.entity.GenericValue;\n"
				+ "import org.apache.ofbiz.service.ServiceAuthException;\n"
				+ "import org.apache.ofbiz.service.ServiceValidationException;\n"
				+ "import org.springframework.http.HttpStatus;\n" + "import org.springframework.http.ResponseEntity;\n"
				+ "import org.springframework.web.bind.annotation.RequestMapping;\n"
				+ "import org.springframework.web.bind.annotation.RequestParam;\n"
				+ "import org.springframework.web.bind.annotation.RequestMethod;\n" + "\n"
				+ "import org.springframework.web.bind.annotation.RestController;\n\n" + "" + "" + ""
				+ "@RestController\n" + "@RequestMapping(\"/service/" + className.replaceAll("Service", "") + "\")\n"
				+ "public class " + className + "{\n\n" + "" + "";

		return retVal;
	}

	public String getControllerMethod(String serviceName, Map<String, String> params, Map<String, String> paramsOpt) {

		String retVal = "";
		retVal += "	@RequestMapping(method = RequestMethod.POST, value = \"/" + serviceName + "\")\n"
				+ "	public ResponseEntity<Object> " + serviceName + "(HttpSession session, ";

		if (!params.isEmpty()) {
			Iterator<String> it = params.keySet().iterator();

			while (it.hasNext()) {
				String pName = it.next();
				retVal += "@RequestParam(value=\"" + pName + "\") " + params.get(pName) + " "
						+ pName.replaceAll("\\.", "");
				retVal += ", ";

			}

		}

		if (!paramsOpt.isEmpty()) {
			Iterator<String> it = paramsOpt.keySet().iterator();

			while (it.hasNext()) {
				String pName = it.next();
				retVal += "@RequestParam(value=\"" + pName + "\", required=false) " + paramsOpt.get(pName) + " "
						+ pName.replaceAll("\\.", "");
				retVal += ", ";

			}
		}

		retVal = retVal.substring(0, retVal.length() - 2);

		retVal += ") {\n" + "		\n" + "		Map<String, Object> paramMap = new HashMap<>();\n";

		if (!params.isEmpty()) {
			Iterator<String> it = params.keySet().iterator();

			while (it.hasNext()) {
				String pName = it.next();
				retVal += "		paramMap.put(\"" + pName + "\"," + pName.replaceAll("\\.", "") + ");\n";

			}
		}

		if (!paramsOpt.isEmpty()) {
			Iterator<String> it = paramsOpt.keySet().iterator();

			while (it.hasNext()) {
				String pName = it.next();
				retVal += "		paramMap.put(\"" + pName + "\"," + pName.replaceAll("\\.", "") + ");\n";

			}
		}

		if (!params.containsKey("userLogin") && !paramsOpt.containsKey("userLogin")) {
			retVal += "		paramMap.put(\"userLogin\", session.getAttribute(\"userLogin\"));\n";
		}

		retVal += "\n		Map<String, Object> result = new HashMap<>();\n"
				+ "		LocalDispatcher dispatcher = (LocalDispatcher) session.getServletContext().getAttribute(\"dispatcher\");\n"
				+ "		try {\n" + "			result = dispatcher.runSync(\"" + serviceName + "\", paramMap);\n"
				+ "		} catch (ServiceAuthException e) {\n" + "\n" + "			e.printStackTrace();\n"
				+ "			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);\n" + "\n"
				+ "		} catch (ServiceValidationException e) {\n"
				+ "			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).header(\"Session-ID\", \"JSESSIONID=\" + session.getId()).body(e.getMessage());\n"
				+ "		} catch (GenericServiceException e) {\n" + "			e.printStackTrace();\n"
				+ "			return ResponseEntity.badRequest().header(\"Session-ID\", \"JSESSIONID=\" + session.getId()).body(e.getMessage());\n" + "		}"
				+ "\n		return ResponseEntity.ok().header(\"Session-ID\", \"JSESSIONID=\" + session.getId()).body(result);\n"
				+ "	}\n\n" + "" + "";

		return retVal;
	}

	public String getControllerErrorPage() {
		String retVal = "";
		retVal += "	@RequestMapping(value = (\" * \"))\n"
				+ "	public ResponseEntity<Object> returnErrorPage(HttpSession session) {\n"
				+ "		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(\"Requested service does not exist. JSESSIONID=\" + session.getId());\n"
				+ "	}\n";

		return retVal;
	}

	public String getDescriptionToClassname(String path) throws Exception {

		Scanner scanner = new Scanner(Paths.get(path), ENCODING.name());

		String returnVal = "";

		while (scanner.hasNextLine()) {
			String line = scanner.nextLine();
			if (line.contains("<description>")) {

				returnVal = line.split(">")[1].split("<")[0].replaceAll(" ", "").replace("(", "").replace(")", "")
						.replaceAll("Services", "Service%%%").split("%%%")[0] + "Controller";;
			}

		}

		scanner.close();
		if (returnVal.equals("")) {
			throw new Exception("No description found for path: " + path);
		}

		if (!descriptions.add(returnVal)) {
			Integer i = 0;
			while (!descriptions.add(returnVal+i.toString())) {
				i++;
			}

			returnVal = returnVal+i.toString();
			doubleDescr.add(returnVal);
		}

		return returnVal;
	}

}