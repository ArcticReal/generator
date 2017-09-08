package parser;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.LinkedList;
import java.util.List;

public class ServiceControlGenerator {

	private String pathToFolder = "/home/work/workspace/ControllingParser/parsed_files/";

	
	
	public void doIt() throws FileNotFoundException {
		File f = new File(pathToFolder);
		f.mkdirs();
		
		List<String> list = new LinkedList<>();
		list.add("productId");
		
		String wrVal = "";
		
		wrVal+=getControllerHeader()+
				getControllerMethod("countProductView", list)+"\n}";
		
		PrintWriter writer = new PrintWriter(pathToFolder + "ServiceController.java");
		
		writer.println(wrVal);
		writer.close();
		
	}

	public String getControllerHeader() {
		
		String retVal = "";
		retVal += ""
				+ "package com.skytala.eCommerce.control;\n\n" + 
				"import java.util.HashMap;\n" + 
				"import java.util.Map;\n\n" + 
				"import javax.servlet.http.HttpSession;\n\n" + 
				"import org.apache.ofbiz.service.GenericServiceException;\n" + 
				"import org.apache.ofbiz.service.LocalDispatcher;\n" + 
				"import org.springframework.web.bind.annotation.RequestMapping;\n" + 
				"import org.springframework.web.bind.annotation.RequestParam;\n" + 
				"import org.springframework.web.bind.annotation.RestController;\n\n" + 
				""
				+ ""
				+ ""
				+ "@RestController\n"
				+ "@RequestMapping(\"/api/service\")\n"
				+ "public class ServiceController{\n\n"
				+ ""
				+ "";
		
		return retVal;
	}
	
	public String getControllerMethod(String serviceName, List<String> params) {
	
		String retVal = "";
		retVal+="	@RequestMapping(\"/"+serviceName+"\")\n" + 
				"	public Map<String, Object> invokeTestService(HttpSession session, ";
		
		for(int i = 0;i < params.size(); i++) {
			retVal+="@RequestParam(value=\""+ params.get(i) + "\") String "+params.get(i);
			if(i<params.size()-1) {
				retVal+=", ";
			}
		}
		
		
		retVal+=") {\n" + 
				"		\n" +
				"		Map<String, Object> paramMap = new HashMap<>();\n";
		
		for(int i = 0;i < params.size(); i++) {
			
			retVal+="		paramMap.put(\""+params.get(i)+"\","+params.get(i)+");\n";
			
		}

		
		
		retVal+="		Map<String, Object> result = new HashMap<>();\n" + 
				"		LocalDispatcher dispatcher = (LocalDispatcher) session.getServletContext().getAttribute(\"dispatcher\");\n" + 
				"		try {\n" + 
				"			result = dispatcher.runSync(\""+serviceName+"\", paramMap);\n" + 
				"		} catch (GenericServiceException e) {\n" + 
				"			e.printStackTrace();\n" + 
				"		}\n\n" + 
				"		return result;\n" +
				"	}";
		
		return retVal;
	}
	
	
}