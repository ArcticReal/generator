package parser;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class ServiceControlGenerator {

	private String pathToFolder = "/home/work/workspace/ControllingParser/parsed_files/";
	private String pathToServiceFile = "/home/work/workspace/ControllingParser/services.txt";
	private final Charset ENCODING = StandardCharsets.UTF_8;

	public void doIt() throws IOException {
		File f = new File(pathToFolder);
		f.mkdirs();

		Map<String, Map<Boolean, Map<String, String>>> allServices = readAllServices();

		String wrVal = "";

		wrVal += getControllerHeader();

		Iterator<String> it = allServices.keySet().iterator();

		while (it.hasNext()) {
			String serviceName = it.next();

			Map<Boolean, Map<String, String>> attributes = allServices.get(serviceName);

			Map<String, String> params = attributes.get(false);
			Map<String, String> paramsOpt = attributes.get(true);



			wrVal += getControllerMethod(serviceName, params, paramsOpt);

		}

		wrVal += getControllerErrorPage() + "\n}";

		PrintWriter writer = new PrintWriter(pathToFolder + "ServiceController.java");

		writer.println(wrVal);
		writer.close();

	}

	public Map<String, Map<Boolean, Map<String, String>>> readAllServices() throws IOException {
		Map<String, Map<Boolean, Map<String, String>>> allServices = new HashMap<>();
		Map<Boolean, Map<String, String>> attributes = new HashMap<>();
		Map<String, String> attributesNOpt = new HashMap<>();
		Map<String, String> attributesOpt = new HashMap<>();

		// Scanner scanner = new Scanner(new File(pathToServiceFile, ENCODING.name()));
		//
		// while(scanner.hasNext()) {
		// String line = scanner.nextLine();
		// System.out.println(line);
		// }
		//
		//
		// scanner.close();

		attributesNOpt.put("idToFind", "String");
		attributesOpt.put("goodIdentificationTypeId", "String");
		attributesOpt.put("searchProductFirst", "String");
		attributesOpt.put("searchAllId", "String");
		attributes.put(false, attributesNOpt);
		attributes.put(true, attributesOpt);

		allServices.put("findProductById", attributes);

		attributesNOpt = new HashMap<>();
		attributesOpt = new HashMap<>();
		attributes = new HashMap<>();

		attributesOpt.put("login.username", "String");
		attributesOpt.put("isServiceAuth", "Boolean");
		attributesOpt.put("login.password", "String");
		attributesOpt.put("locale", "java.util.Locale");
		attributesOpt.put("timeZone", "java.util.TimeZone");
		attributesOpt.put("userLogin", "org.apache.ofbiz.entity.GenericValue");
		attributesOpt.put("visitId", "String");

		attributes.put(true, attributesOpt);
		attributes.put(false, attributesNOpt);

		allServices.put("userLogin", attributes);

		return allServices;
	}

	public String getControllerHeader() {

		String retVal = "";
		retVal += "" + "package com.skytala.eCommerce.control;\n\n" + "import java.util.HashMap;\n"
				+ "import java.util.Map;\n\n" + "import javax.servlet.http.HttpSession;\n\n"
				+ "import org.apache.ofbiz.service.GenericServiceException;\n"
				+ "import org.apache.ofbiz.service.LocalDispatcher;\n"
				+ "import org.apache.ofbiz.service.ServiceAuthException;\n"
				+ "import org.springframework.http.HttpStatus;\n"
				+ "import org.springframework.http.ResponseEntity;\n"
				+ "import org.springframework.web.bind.annotation.RequestMapping;\n"
				+ "import org.springframework.web.bind.annotation.RequestParam;\n"
				+ "import org.springframework.web.bind.annotation.RestController;\n\n" + "" + "" + ""
				+ "@RestController\n" + "@RequestMapping(\"/service\")\n" + "public class ServiceController{\n\n"
				+ "" + "";

		return retVal;
	}

	public String getControllerMethod(String serviceName, Map<String, String> params, Map<String, String> paramsOpt) {

		String retVal = "";
		retVal += "	@RequestMapping(\"/" + serviceName + "\")\n" + "	public ResponseEntity<Object> " + serviceName
				+ "(HttpSession session, ";

		if (!params.isEmpty()) {
			Iterator<String> it = params.keySet().iterator();

			while (it.hasNext()) {
				String pName = it.next();
				retVal += "@RequestParam(value=\"" + pName + "\") " + params.get(pName) + " " + pName.replaceAll("\\.", "");
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
		
		if(!params.containsKey("userLogin")&&!paramsOpt.containsKey("userLogin")) {
			retVal += "		paramMap.put(\"userLogin\", session.getAttribute(\"userLogin\"));\n";
		}

		retVal += "\n		Map<String, Object> result = new HashMap<>();\n"
				+ "		LocalDispatcher dispatcher = (LocalDispatcher) session.getServletContext().getAttribute(\"dispatcher\");\n"
				+ "		try {\n" + "			result = dispatcher.runSync(\"" + serviceName + "\", paramMap);\n"
				+ "		} catch (ServiceAuthException e) {\n" + 
				"\n" + 
				"			e.printStackTrace();\n" + 
				"			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);\n" + 
				"\n" + 
				"		} catch (GenericServiceException e) {\n" + 
				"			e.printStackTrace();\n" + 
				"			return ResponseEntity.badRequest().body(null);\n" + 
				"		}\n		return ResponseEntity.ok().body(result);\n" + 
				"	}\n\n" + 
				"" + 
				"";

		return retVal;
	}

	public String getControllerErrorPage() {
		String retVal = "";
		retVal += "	@RequestMapping(value = (\" * \"))\n" + "	public ResponseEntity<Object> returnErrorPage() {\n"
				+ "		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(\"Requested service does not exist.\");\n"
				+ "	}\n";

		return retVal;
	}

}