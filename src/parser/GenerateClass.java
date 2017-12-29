package parser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class GenerateClass {
	private static Pars parser;
	public static String path;
	private static String parentEntity;
	private Map<String, String> packageNames = new HashMap<>();


/*	public static void main(String args[]) {
		GenerateClass gener = new GenerateClass();
		// gener.generateEntityClass("Party", "party.xml");
		// gener.generateMapper("UserLogin", "UserLogin", "login.xml");
		String name = "Party";
		parser = new Pars();
		parser.initPath();

		path = new File("").getAbsolutePath();
		gener.generateAllCommandsAndQuerys();
		gener.generateAllEntitysandMappers();
		//
		// gener.generateCommandAdd(name);
		// gener.generateCommandDelete(name);
		// gener.generateCommandUpdate(name);
		// gener.generateQueryAll(name);
		// // gener.generateQueryById(name);
		// gener.generateQueryBy(name);
		//
	}
*/
	public GenerateClass(String newPath) {
	
		parser = new Pars();
		parser.initPath();

		path = new File("").getAbsolutePath();

	}
	
	public void generateAllEntitysandMappers() {
		
		File f = new File(path + "/xmls/");
		
		try {
			for(File fi:f.listFiles()) {
					
				FileReader fr = new FileReader(fi);
				BufferedReader br = new BufferedReader(fr);
				String name;
	
				// br.mark(0);
				while ((name = br.readLine()) != null && name.length() != 0) {
	
					// br.reset();
					// name = br.readLine();
					System.out.println(name);
					this.generateEntityClass(name, name, fi.getName());
					this.generateMapper(name, name, fi.getName());
	
				}
	
			br.close();
			}
		} catch (Exception e) {
			// TODO: handle exception
		}
	}

	public void generateAllCommandsAndQuerys(String name, String parent, String PK) {

		parentEntity = parent;
		
		this.generateCommandAdd(name, PK);
		this.generateCommandDelete(name);
		this.generateCommandUpdate(name);
		this.generateQueryAll(name);
		// this.generateQueryById(name);
		this.generateQueryBy(name);
	
	}

	public boolean generateEntityClass(String entityname, String parentEntity, String filename) {
		
		PrintWriter pw;

		LinkedList<String> attr = new LinkedList<String>();
		LinkedList<String> type = new LinkedList<String>();
		attr = parser.ParsSingleEntity(entityname, 0, filename);
		type = parser.ParsSingleEntity(entityname, 1, filename);
		type = this.convertEntityTypeListToJava(type);

		try {
			File f = new File(path+"/parsed_files/entity/");
			f.mkdirs();
			pw = new PrintWriter(f.getAbsolutePath() + "/" + entityname + ".java");
			
			String packageName = packageNames.get(PackageTypes.MODEL);
			pw.println(packageName + "\n");
			
			/*
			if(parentEntity.equals(entityname)) {
				
				pw.println("package com.skytala.eCommerce.domain." + this.lowerFirst(entityname) + ".model;");
			}else {
				pw.println("package com.skytala.eCommerce.domain."+lowerFirst(parentEntity)+".relations."+this.lowerFirst(entityname)+".model;\n");
				
			}*/
			
			
			pw.println("import java.util.Map;");
			pw.println("import java.math.BigDecimal;");
			pw.println("import java.sql.Timestamp;");
			pw.println("import java.util.Map;");
			pw.println("import java.io.Serializable;");
			
			
			packageName = packageNames.get(PackageTypes.MAPPER).replaceFirst("package", "import").replace(";", ".");
			pw.println(packageName + entityname + "Mapper;");
			
			/*
			if(parentEntity.equals(entityname)) {
				pw.println("import com.skytala.eCommerce.domain."+this.lowerFirst(entityname)+".mapper."+entityname+"Mapper;");
				
			}else {
				pw.println("import com.skytala.eCommerce.domain."+lowerFirst(parentEntity)+".relations."+this.lowerFirst(entityname)+".mapper."+entityname+"Mapper;");

			}
			*/
			
			pw.println("");
			pw.println("public class " + entityname + " implements Serializable{");
			pw.println("");
			pw.println("private static final long serialVersionUID = 1L;");
			for (int i = 0; i < attr.size(); i++) {
				String zwisch = type.get(i);

				if (!zwisch.equals("byte[]")) {
					zwisch = this.capitFirst(zwisch);
				}
				pw.println("private " + zwisch + " " + attr.get(i) + ';');

			}

			for (int i = 0; i < attr.size(); i++) {
				pw.println("");
				pw.println("public " + this.capitFirst(type.get(i)) + " get" + this.capitFirst(attr.get(i)) + "() {");
				pw.println("return " + attr.get(i) + ";");
				pw.println("}");

				pw.println("");
				pw.println("public void set" + this.capitFirst(attr.get(i)) + "(" + this.capitFirst(type.get(i)) + "  "
						+ attr.get(i) + ") {");
				pw.println("this." + attr.get(i) + " = " + attr.get(i) + ";");
				pw.println("}");

			}
			pw.println("");
			pw.println("");
			pw.println("public Map<String, Object> mapAttributeField() {");

			pw.println("return " + entityname + "Mapper.map(this);");
			pw.println("}");

			pw.println("}");

			pw.close();
		} catch (IOException e) {
			// TODO: handle exception
		}

		return true;
	}

	public void generateMapper(String Name, String parentEntity, String filename) {

		PrintWriter pw;

		LinkedList<String> attr = new LinkedList<String>();
		LinkedList<String> type = new LinkedList<String>();
		attr = parser.ParsSingleEntity(Name, 0, filename);
		type = parser.ParsSingleEntity(Name, 1, filename);
		type = this.convertEntityTypeListToJava(type);

		try {
			File f = new File(path+"/parsed_files/mapper/");
			f.mkdirs();

			pw = new PrintWriter(f.getAbsolutePath()+ "/" + Name + "Mapper.java");

			
			String packageName = packageNames.get(PackageTypes.MAPPER);
			pw.println(packageName + "\n");
			
			/*
			if(parentEntity.equals(Name)) {
				
				pw.println("package com.skytala.eCommerce.domain."+this.lowerFirst(Name)+".mapper;");
			}else {
				pw.println("package com.skytala.eCommerce.domain."+lowerFirst(parentEntity)+".relations."+this.lowerFirst(Name)+".mapper;\n");
				
			}*/
			
			
			pw.println("import java.math.BigDecimal;");
			pw.println("import java.sql.Timestamp;");
			pw.println("import java.util.HashMap;");
			pw.println("import java.util.Map;");
			pw.println("import javax.servlet.http.HttpServletRequest;");
			pw.println("import org.apache.ofbiz.entity.GenericValue;");
			
			
			packageName = packageNames.get(PackageTypes.MODEL).replaceFirst("package", "import").replace(";", ".");
			pw.println(packageName + Name + ";");
			
			/*
			if(parentEntity.equals(Name)) {
				pw.println("import com.skytala.eCommerce.domain."+this.lowerFirst(Name)+".model."+Name+";");
		
			}else {
				pw.println("import com.skytala.eCommerce.domain."+lowerFirst(parentEntity)+".relations."+this.lowerFirst(Name)+".model."+Name+";");
			}*/
			
			pw.println("");
			pw.println("public class " + Name + "Mapper  {");
			pw.println("");
			pw.println("");

			// Generierung der ersten Methode
			pw.println("	public static Map<String, Object> map(" + Name + " " + Name.toLowerCase() + ") {");
			pw.println("");
			pw.println("		Map<String, Object> returnVal = new HashMap<String, Object>();");
			pw.println("");
			pw.println("");

			for (int i = 0; i < attr.size(); i++) {
				/*
				 * if (type.get(i).equals("boolean")) { pw.println( "		if("
				 * + Name.toLowerCase() + ".is" +
				 * this.capitFirst(attr.get(i))+"()" + " != null ){");
				 * pw.println("			returnVal.put(" + '"' + attr.get(i) + '"
				 * ' + "," + Name.toLowerCase() + "." + "get" +
				 * this.capitFirst(attr.get(i)) + "());"); pw.println("}");
				 * pw.println("");
				 * 
				 * } else {
				 */

				pw.println("		if(" + Name.toLowerCase() + ".get" + this.capitFirst(attr.get(i)) + "()"
						+ " != null ){");
				pw.println("			returnVal.put(" + '"' + attr.get(i) + '"' + "," + Name.toLowerCase() + "."
						+ "get" + this.capitFirst(attr.get(i)) + "());");
				pw.println("}");
				pw.println("");

			}
			pw.println("		return returnVal;");
			pw.println("}");
			pw.println("");
			pw.println("");

			// Generierung der zweiten Methode
			pw.println("	public static " + Name + " map(Map<String, Object> fields) {");
			pw.println("");

			pw.println("		" + Name + " returnVal = new " + Name + "();");
			pw.println("");

			for (int i = 0; i < attr.size(); i++) {

				pw.println("		if(fields.get(" + '"' + attr.get(i) + '"' + ")" + " != null) {");
				pw.println("			returnVal.set" + this.capitFirst(attr.get(i)) + "((" + type.get(i)
						+ ") fields.get(" + '"' + attr.get(i) + '"' + "));");
				pw.println("}");
				pw.println("");

			}
			pw.println("");
			pw.println("		return returnVal;");
			pw.println(" } ");

			// Generierung der dritten Methode
			pw.println("	public static " + Name + " mapstrstr(Map<String, String> fields) throws Exception {");
			pw.println("");

			pw.println("		" + Name + " returnVal = new " + Name + "();");
			pw.println("");

			for (int i = 0; i < attr.size(); i++) {

				switch (type.get(i)) {
				case "String":
					pw.println("		if(fields.get(" + '"' + attr.get(i) + '"' + ")" + " != null) {");
					pw.println("			returnVal.set" + this.capitFirst(attr.get(i)) + "((" + type.get(i)
							+ ") fields.get(" + '"' + attr.get(i) + '"' + "));");
					pw.println("}");
					break;
				case "long":
					pw.println("		if(fields.get(" + '"' + attr.get(i) + '"' + ")" + " != null) {");
					pw.println("String buf;");
					pw.println("buf = fields.get(" + '"' + attr.get(i) + '"' + ");");
					pw.println("long ibuf = Long.parseLong(buf);");
					pw.println("			returnVal.set" + this.capitFirst(attr.get(i)) + "(ibuf);");
					pw.println("}");
					break;
				case "BigDecimal":
					pw.println("		if(fields.get(" + '"' + attr.get(i) + '"' + ")" + " != null) {");
					pw.println("String buf;");
					pw.println("buf = fields.get(" + '"' + attr.get(i) + '"' + ");");
					pw.println("float ibuf = Float.parseFloat(buf); ");

					pw.println("BigDecimal bd = BigDecimal.valueOf(ibuf);");
					pw.println("			returnVal.set" + this.capitFirst(attr.get(i)) + "(bd);");
					pw.println("}");
					break;
				case "boolean":
					pw.println("		if(fields.get(" + '"' + attr.get(i) + '"' + ")" + " != null) {");
					pw.println("String buf;");
					pw.println("buf = fields.get(" + '"' + attr.get(i) + '"' + ");");
					pw.println("Boolean ibuf = Boolean.parseBoolean(buf);");
					pw.println("			returnVal.set" + this.capitFirst(attr.get(i)) + "(ibuf);");
					pw.println("}");
					break;
				case "Timestamp":
					pw.println("		if(fields.get(" + '"' + attr.get(i) + '"' + ")" + " != null) {");
					pw.println("String buf = fields.get(" + '"' + attr.get(i) + '"' + ");");
					pw.println("Timestamp ibuf = Timestamp.valueOf(buf);");
					pw.println("			returnVal.set" + this.capitFirst(attr.get(i)) + "(ibuf);");
					pw.println("}");
					break;
				case "byte[]":
					pw.println("		if(fields.get(" + '"' + attr.get(i) + '"' + ")" + " != null) {");
					pw.println("String buf = fields.get(" + '"' + attr.get(i) + '"' + ");");
					pw.println("byte[] ibuf = buf.getBytes();");
					pw.println("			returnVal.set" + this.capitFirst(attr.get(i)) + "(ibuf);");
					pw.println("}");

					
					break;
				case "Object":
					pw.println("		if(fields.get(" + '"' + attr.get(i) + '"' + ")" + " != null) {");
					pw.println("String buf = fields.get(" + '"' + attr.get(i) + '"' + ");");
					pw.println("Object ibuf = buf;");
					pw.println("returnVal.set" + this.capitFirst(attr.get(i)) + "(ibuf);");
					pw.println("}");
					break;
				default:
					System.out.println("Fehler mit " + type.get(i));
					break;
				}

				pw.println("");

			}

			pw.println("");
			pw.println("		return returnVal;");
			pw.println(" } ");

			// Generierung der vierten Methode
			pw.println("	public static " + Name + " map(GenericValue val) {");
			pw.println("");
			pw.println(Name + " returnVal = new " + Name + "();");

			for (int i = 0; i < attr.size(); i++) {
				String typebuf = type.get(i);
				if (typebuf.equals("Date")) {
					typebuf = "Timestamp";
				}

				if (typebuf.equals("byte[]")) {
					pw.println("		returnVal.set" + this.capitFirst(attr.get(i)) + "(val.getBytes(" + '"'
							+ attr.get(i) + '"' + "));");
				} else if(typebuf.equals("Object")){
					pw.println("		returnVal.set" + this.capitFirst(attr.get(i)) 
							+ "(val.get(" + '"' + attr.get(i) + '"' + "));");

				}else {	
					pw.println("		returnVal.set" + this.capitFirst(attr.get(i)) + "(val.get"
							+ this.capitFirst(typebuf) + "(" + '"' + attr.get(i) + '"' + "));");
				}
			}
			pw.println("");
			pw.println("");
			pw.println("return returnVal;");
			pw.println("");
			pw.println("}");
			pw.println("");

			// Generierung der
			pw.println("public static " + Name + " map(HttpServletRequest request) throws Exception {");
			pw.println("");
			pw.println("		" + Name + " returnVal = new " + Name + "();");
			pw.println("");
			pw.println("Map<String, String[]> paramMap = request.getParameterMap();");
			pw.println("");

			if(attr.size()!=0){

				// Hardcodiert f�r Produkt muss noch ge�ndert werden
				pw.println("		if(paramMap.containsKey(" + '"' + attr.get(0) + '"' + ")) {");
				pw.println("returnVal.set" + this.capitFirst(attr.get(0)) + "(request.getParameter(" + '"' + attr.get(0)
						+ '"' + "));");
				pw.println("}");
				pw.println("");
			}

			for (int i = 0; i < attr.size(); i++) {
				// auch Hardcodierte �berpr�fung wegen oben
				if (!attr.get(i).equals(attr.get(0))) {

					pw.println("		if(paramMap.containsKey(" + '"' + attr.get(i) + '"' + "))  {");

					switch (type.get(i)) {
					case "String":
						pw.println("returnVal.set" + this.capitFirst(attr.get(i)) + "(request.getParameter(" + '"'
								+ attr.get(i) + '"' + "));");
						break;

					case "long":
						pw.println("String buf = request.getParameter(" + '"' + attr.get(i) + '"' + ");");
						pw.println("Long ibuf = Long.parseLong(" + "buf" + ");");
						pw.println("returnVal.set" + this.capitFirst(attr.get(i)) + "(ibuf);");
						break;
					case "BigDecimal":
						pw.println("String buf = request.getParameter(" + '"' + attr.get(i) + '"' + ");");
						pw.println("Float ibuf = Float.parseFloat(" + "buf" + ");");
						pw.println("BigDecimal bd = BigDecimal.valueOf(ibuf);");
						pw.println("			returnVal.set" + this.capitFirst(attr.get(i)) + "(bd);");

						break;
					case "Timestamp":
						pw.println("String buf = request.getParameter(" + '"' + attr.get(i) + '"' + ");");
						pw.println("Timestamp ibuf = Timestamp.valueOf(" + "buf" + ");");
						pw.println("returnVal.set" + this.capitFirst(attr.get(i)) + "(ibuf);");

						break;
					case "boolean":
						pw.println("String buf = request.getParameter(" + '"' + attr.get(i) + '"' + ");");
						pw.println("Boolean ibuf = Boolean.parseBoolean(" + "buf" + ");");
						pw.println("returnVal.set" + this.capitFirst(attr.get(i)) + "(ibuf);");
						break;
					case "byte[]":
						pw.println("String buf = request.getParameter(" + '"' + attr.get(i) + '"' + ");");
						pw.println("byte[] ibuf = buf.getBytes();");
						pw.println("returnVal.set" + this.capitFirst(attr.get(i)) + "(ibuf);");
						break;
					case "Object":
						pw.println("String buf = request.getParameter(" + '"' + attr.get(i) + '"' + ");");
						pw.println("Object ibuf = buf;");
						pw.println("returnVal.set" + this.capitFirst(attr.get(i)) + "(ibuf);");
						break;
					default:
						System.out.println("Fehler mit: " + type.get(i));
						break;
					}

					pw.println("}");
				}
			}

			pw.println("return returnVal;");
			pw.println("");
			pw.println("}");

			/*
			 * 
			 * public static Product map(GenericValue val) { Product returnVal =
			 * new Product();
			 * 
			 * returnVal.setProductId(val.getString("productId"));
			 * returnVal.setProductName(val.getString("productName"));
			 * 
			 * 
			 * return returnVal;
			 * 
			 * }
			 * 
			 * 
			 * 
			 * 
			 * 
			 */
			pw.println("}");

			pw.close();
		} catch (

		IOException e) {

		}
	}

	public LinkedList<String> convertEntityTypeListToJava(LinkedList<String> typelist) {
		LinkedList<String> copybuffer = new LinkedList<String>();

		for (int i = 0; i < typelist.size(); i++) {
			switch (typelist.get(i)) {
			case "id-ne":
				copybuffer.add("String");
				break;
			case "name":
				copybuffer.add("String");
				break;
			case "indicator":
				copybuffer.add("boolean");
				break;
			case "url":
				copybuffer.add("String");
				break;
			case "long-varchar":
				copybuffer.add("String");
				break;
			case "id":
				copybuffer.add("String");
				break;
			case "date-time":
				copybuffer.add("Timestamp");
				break;
			case "numeric":
				copybuffer.add("long");
				break;
			case "description":
				copybuffer.add("String");
				break;
			case "very-long":
				copybuffer.add("String");
				break;
			case "id-long-ne":
				copybuffer.add("String");
				break;
			case "value":
				copybuffer.add("String");
				break;
			case "fixed-point":
				copybuffer.add("BigDecimal");
				break;
			case "comment":
				copybuffer.add("String");
				break;
			case "currency-amount":
				copybuffer.add("BigDecimal");
				break;
			case "very-short":
				copybuffer.add("String");
				break;
			case "id-long":
				copybuffer.add("String");
				break;
			case "currency-precise":
				copybuffer.add("BigDecimal");
				break;
			case "id-vlong":
				copybuffer.add("String");
				break;
			case "short-varchar":
				copybuffer.add("String");
				break;
			case "id-vlong-ne":
				copybuffer.add("String");
				break;
			case "floating-point":
				copybuffer.add("BigDecimal");
				break;
			case "email":
				copybuffer.add("String");
				break;
			case "date":
				copybuffer.add("Timestamp");
				break;
			case "credit-card-date":
				copybuffer.add("Timestamp");
				break;
			case "credit-card-number":
				copybuffer.add("String");
				break;
			case "time":
				copybuffer.add("Timestamp");
				break;
			// TODO Change again
			case "byte-array":
				copybuffer.add("byte[]");
				break;
			// TODO Change again
			case "object":
				copybuffer.add("Object");
				// copybuffer.add("Object");
				break;
			case "blob":
				copybuffer.add("java.nio.ByteBuffer");
			default:
				System.out.println("Error mit:  " + typelist.get(i));
				break;
			}

		}

		return copybuffer;
	}

	public String convertEntityTypeToJava(String type) {
		String returnVal = "";

			switch (type) {
				case "id-ne":
					returnVal = "String";
					break;
				case "name":
					returnVal = "String";
					break;
				case "indicator":
					returnVal = "boolean";
					break;
				case "url":
					returnVal = "String";
					break;
				case "long-varchar":
					returnVal = "String";
					break;
				case "id":
					returnVal = "String";
					break;
				case "date-time":
					returnVal = "Timestamp";
					break;
				case "numeric":
					returnVal = "long";
					break;
				case "description":
					returnVal = "String";
					break;
				case "very-long":
					returnVal = "String";
					break;
				case "id-long-ne":
					returnVal = "String";
					break;
				case "value":
					returnVal = "String";
					break;
				case "fixed-point":
					returnVal = "BigDecimal";
					break;
				case "comment":
					returnVal = "String";
					break;
				case "currency-amount":
					returnVal = "BigDecimal";
					break;
				case "very-short":
					returnVal = "String";
					break;
				case "id-long":
					returnVal = "String";
					break;
				case "currency-precise":
					returnVal = "BigDecimal";
					break;
				case "id-vlong":
					returnVal = "String";
					break;
				case "short-varchar":
					returnVal = "String";
					break;
				case "id-vlong-ne":
					returnVal = "String";
					break;
				case "floating-point":
					returnVal = "BigDecimal";
					break;
				case "email":
					returnVal = "String";
					break;
				case "date":
					returnVal = "Timestamp";
					break;
				case "credit-card-date":
					returnVal = "Timestamp";
					break;
				case "credit-card-number":
					returnVal = "String";
					break;
				case "time":
					returnVal = "Timestamp";
					break;
				// TODO Change again
				case "byte-array":
					returnVal = "byte[]";
					break;
				// TODO Change again
				case "object":
					returnVal = "Object";
					break;
				case "blob":
					returnVal = "java.nio.ByteBuffer";
				default:
					System.out.println("Error mit:  " + type);
					break;
			}



		return returnVal;
	}

	public void generateCommandAdd(String name, String PK) {
		PrintWriter pw;
		try {
			
			pw = new PrintWriter(path+"/parsed_files/Command/Add" + name + ".java");

			
			String packageName = packageNames.get(PackageTypes.COMMAND);
			pw.println(packageName);
			
			/*
			if(parentEntity.equals(name)) {
				pw.println("package com.skytala.eCommerce.domain." + this.lowerFirst(name) + ".command;");
			}else {
				pw.println("package com.skytala.eCommerce.domain." + lowerFirst(parentEntity) + ".relations." + this.lowerFirst(name) + ".command;");
			}*/
			
			pw.println("import org.apache.ofbiz.entity.Delegator;");
			pw.println("import org.apache.ofbiz.entity.DelegatorFactory;");
			pw.println("import org.apache.ofbiz.entity.GenericEntityException;");
			pw.println("import org.apache.ofbiz.entity.GenericValue;");
			
			
			packageName = packageNames.get(PackageTypes.EVENT).replaceFirst("package", "import").replace(";", ".");
			pw.println(packageName + name + "Added;");
			
			packageName = packageNames.get(PackageTypes.MAPPER).replaceFirst("package", "import").replace(";", ".");
			pw.println(packageName + name + "Mapper;");
			
			packageName = packageNames.get(PackageTypes.MODEL).replaceFirst("package", "import").replace(";", ".");
			pw.println(packageName + name + ";");

			/*
			if(parentEntity.equals(name)) {
				pw.println("import com.skytala.eCommerce.domain." + this.lowerFirst(name) + ".event." + name + "Added;");
				pw.println("import com.skytala.eCommerce.domain." + this.lowerFirst(name) + ".mapper." + name + "Mapper;");
				pw.println("import com.skytala.eCommerce.domain." + this.lowerFirst(name) + ".model." + name + ";");
			}else {
				pw.println("import com.skytala.eCommerce.domain." + lowerFirst(parentEntity) + ".relations."  + this.lowerFirst(name) + ".event." + name + "Added;");
				pw.println("import com.skytala.eCommerce.domain." + lowerFirst(parentEntity) + ".relations."  + this.lowerFirst(name) + ".mapper." + name + "Mapper;");
				pw.println("import com.skytala.eCommerce.domain." + lowerFirst(parentEntity) + ".relations."  + this.lowerFirst(name) + ".model." + name + ";");
			}*/
			
			
			pw.println("import com.skytala.eCommerce.framework.pubsub.Broker;");
			pw.println("import com.skytala.eCommerce.framework.pubsub.Command;");
			pw.println("import com.skytala.eCommerce.framework.pubsub.Event;");
			pw.println("import edu.emory.mathcs.backport.java.util.concurrent.TimeUnit;");

			// import com.skytala.eCommerce.entity.Document;
			// import com.skytala.eCommerce.event.DocumentAdded;

			pw.println("");
			pw.println("public class Add" + name + " extends Command {");
			pw.println("");
			pw.println("private " + name + " elementToBeAdded;");
			pw.println("public Add" + name + "(" + name + " elementToBeAdded){");
			pw.println("this.elementToBeAdded = elementToBeAdded;");
			pw.println("}");
			pw.println("");
			pw.println("@Override");
			pw.println("public Event execute(){");
			pw.println("");
			pw.println("");
			pw.println("Delegator delegator = DelegatorFactory.getDelegator(" + '"' + "default" + '"' + ");");
			pw.println("");
			pw.println(name + " addedElement = null;");
			pw.println("boolean success = false;");
			pw.println("try {");
			if(PK != null) {
				
				pw.println("elementToBeAdded.set" + capitFirst(PK) + "(delegator.getNextSeqId(" + '"' + name + '"' + "));");
			}
			pw.println("GenericValue newValue = delegator.makeValue(" + '"' + name + '"'
					+ ", elementToBeAdded.mapAttributeField());");
			pw.println("addedElement = " + name + "Mapper.map(delegator.create(newValue));");
			pw.println("success = true;");
			pw.println("} catch(GenericEntityException e) {");
			// TODO CHANGED HERE
			pw.println(" e.printStackTrace(); ");
			pw.println("addedElement = null;");
			pw.println("}");
			pw.println("");
			pw.println("Event resultingEvent = new "+name+"Added(addedElement, success);");
			pw.println("Broker.instance().publish(resultingEvent);");
			pw.println("return resultingEvent;");
			pw.println("}");
			pw.println("}");

			pw.close();
		} catch (Exception e) {
			// TODO: handle exception
		}

	}

	public void generateCommandDelete(String name) {
		PrintWriter pw;

		try {

			
			pw = new PrintWriter(path+"/parsed_files/Command/Delete" + name + ".java");

			String packageName = packageNames.get(PackageTypes.COMMAND);
			pw.println(packageName);

			/*
			if(parentEntity.equals(name)) {
				
				pw.println("package com.skytala.eCommerce.domain." + this.lowerFirst(name) + ".command;");
			}else {
				pw.println("package com.skytala.eCommerce.domain." + lowerFirst(parentEntity) + ".relations." + this.lowerFirst(name) + ".command;");
				
			}*/

			pw.println("import org.apache.ofbiz.base.util.UtilMisc;");
			pw.println("import org.apache.ofbiz.entity.Delegator;");
			pw.println("import org.apache.ofbiz.entity.DelegatorFactory;");
			pw.println("import org.apache.ofbiz.entity.GenericEntityException;");
			pw.println("import org.apache.ofbiz.entity.GenericValue;");
			
			
			packageName = packageNames.get(PackageTypes.EVENT).replaceFirst("package", "import").replace(";", ".");
			pw.println(packageName + name + "Deleted;");
			
			packageName = packageNames.get(PackageTypes.MODEL).replaceFirst("package", "import").replace(";", ".");
			pw.println(packageName + name + ";");

			
			/*
			if(parentEntity.equals(name)) {
				pw.println("import com.skytala.eCommerce.domain." + this.lowerFirst(name) + ".event." + name + "Deleted;");
				pw.println("import com.skytala.eCommerce.domain." + this.lowerFirst(name) + ".model." + name + ";");
			}else {
				pw.println("import com.skytala.eCommerce.domain." + lowerFirst(parentEntity) + ".relations."  + this.lowerFirst(name) + ".event." + name + "Deleted;");
				pw.println("import com.skytala.eCommerce.domain." + lowerFirst(parentEntity) + ".relations."  + this.lowerFirst(name) + ".model." + name + ";");
			}*/

			pw.println("import com.skytala.eCommerce.framework.exceptions.RecordNotFoundException;");
			pw.println("import com.skytala.eCommerce.framework.pubsub.Broker;");
			pw.println("import com.skytala.eCommerce.framework.pubsub.Command;");
			pw.println("import com.skytala.eCommerce.framework.pubsub.Event;");
			pw.println("import org.apache.ofbiz.entity.GenericEntityNotFoundException;");

			pw.println("");
			pw.println("public class Delete" + name + " extends Command {");
			pw.println("");
			pw.println("private String toBeDeletedId;");
			pw.println("public Delete" + name + "(String toBeDeletedId){");
			pw.println("this.toBeDeletedId = toBeDeletedId;");
			pw.println("}");
			pw.println("");
			pw.println("@Override");
			pw.println("public Event execute() {");
			pw.println("");
			pw.println("Delegator delegator = DelegatorFactory.getDelegator(" + '"' + "default" + '"' + ");");
			pw.println("");
			pw.println("boolean success = false;");
			pw.println("");
			pw.println("try{");
			pw.println("int countRemoved = delegator.removeByAnd(" + '"' + name + '"' + ", UtilMisc.toMap(" + '"'
					+ this.lowerFirst(name) + "Id" + '"' + ", toBeDeletedId));");
//			System.out.println(name);
			pw.println("if(countRemoved > 0) {");
			pw.println("success = true;");
			pw.println("}");
			pw.println("else{");
			pw.println("throw new RecordNotFoundException(" + name + ".class);");
			pw.println("}");
			pw.println("} catch (GenericEntityException e) {");
			// TODO Changed Here
			pw.println(" System.err.println(e.getMessage()); ");
			pw.println("if(e.getCause().getClass().equals(GenericEntityNotFoundException.class)) {");
			pw.println("throw new RecordNotFoundException(" + name + ".class);");
			pw.println("}");
			pw.println("}");

			pw.println("Event resultingEvent = new " + name + "Deleted(success);");
			pw.println("Broker.instance().publish(resultingEvent);");
			pw.println("return resultingEvent;");

			pw.println("");
			pw.println("}");
			pw.println("public String getToBeDeletedId() {");
			pw.println("return toBeDeletedId;");
			pw.println("}");
			pw.println("public void setToBeDeletedId(String toBeDeletedId) {");
			pw.println("this.toBeDeletedId = toBeDeletedId;");
			pw.println("}");
			pw.println("}");
			pw.close();

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public void generateCommandUpdate(String name) {
		PrintWriter pw;

		try {

			
			pw = new PrintWriter(path+"/parsed_files/Command/Update" + name + ".java");

			String packageName = packageNames.get(PackageTypes.COMMAND);
			pw.println(packageName);
			
			/*
			if(parentEntity.equals(name)) {
				pw.println("package com.skytala.eCommerce.domain." + this.lowerFirst(name) + ".command;");
			}else {
				pw.println("package com.skytala.eCommerce.domain." + lowerFirst(parentEntity) + ".relations." + this.lowerFirst(name) + ".command;");
			}*/

			pw.println("import org.apache.ofbiz.entity.Delegator;");
			pw.println("import org.apache.ofbiz.entity.DelegatorFactory;");
			pw.println("import org.apache.ofbiz.entity.GenericEntityException;");
			pw.println("import org.apache.ofbiz.entity.GenericValue;");
			pw.println("import org.apache.ofbiz.entity.GenericEntityNotFoundException;");
		
			packageName = packageNames.get(PackageTypes.EVENT).replaceFirst("package", "import").replace(";", ".");
			pw.println(packageName + name + "Updated;");
			
			packageName = packageNames.get(PackageTypes.MODEL).replaceFirst("package", "import").replace(";", ".");
			pw.println(packageName + name + ";");

			/*
			if(parentEntity.equals(name)) {
				pw.println("import com.skytala.eCommerce.domain." + this.lowerFirst(name) + ".event." + name + "Updated;");
				pw.println("import com.skytala.eCommerce.domain." + this.lowerFirst(name) + ".model." + name + ";");
			}else {
				pw.println("import com.skytala.eCommerce.domain." + lowerFirst(parentEntity) + ".relations." + this.lowerFirst(name) + ".event." + name + "Updated;");
				pw.println("import com.skytala.eCommerce.domain." + lowerFirst(parentEntity) + ".relations." + this.lowerFirst(name) + ".model." + name + ";");
			}*/
	
			pw.println("import com.skytala.eCommerce.framework.exceptions.RecordNotFoundException;");
			pw.println("import com.skytala.eCommerce.framework.pubsub.Broker;");
			pw.println("import com.skytala.eCommerce.framework.pubsub.Command;");
			pw.println("import com.skytala.eCommerce.framework.pubsub.Event;");

			pw.println("");
			pw.println("public class Update" + name + " extends Command {");
			pw.println("");
			pw.println("private " + name + " elementToBeUpdated;");
			pw.println("");
			pw.println("public Update" + name + "(" + name + " elementToBeUpdated){");
			pw.println("this.elementToBeUpdated = elementToBeUpdated;");
			pw.println("}");
			pw.println("public " + name + " getElementToBeUpdated() {");
			pw.println("return elementToBeUpdated;");
			pw.println("}");
			pw.println("public void setElementToBeUpdated(" + name + " elementToBeUpdated){");
			pw.println("this.elementToBeUpdated = elementToBeUpdated;");
			pw.println("}");
			pw.println("");
			pw.println("@Override");
			pw.println("public Event execute() throws RecordNotFoundException{");
			pw.println("");
			pw.println("");
			pw.println("Delegator delegator = DelegatorFactory.getDelegator(" + '"' + "default" + '"' + ");");
			pw.println("");
			pw.println("boolean success;");
			pw.println("try{");
			pw.println("GenericValue newValue = delegator.makeValue(" + '"' + name + '"'
					+ ", elementToBeUpdated.mapAttributeField());");
			pw.println("delegator.store(newValue);");
			pw.println("if(delegator.store(newValue) == 0) { ");
			pw.println("throw new RecordNotFoundException(" + name + ".class); ");
			pw.println("}");
			pw.println("success = true;");
			pw.println("} catch (GenericEntityException e) {");

			pw.println("e.printStackTrace();");
			pw.println("if(e.getCause().getClass().equals(GenericEntityNotFoundException.class)) {");
			pw.println("throw new RecordNotFoundException("+name+".class);");
			pw.println("}");
			pw.println("success = false;");
			pw.println("}");
			pw.println("Event resultingEvent = new "+name+"Updated(success);");
			pw.println("Broker.instance().publish(resultingEvent);");
			pw.println("return resultingEvent;");
			pw.println("}");
			pw.println("}");

			pw.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public void generateQueryAll(String name) {
		PrintWriter pw;

		try {

			
			pw = new PrintWriter(path+"/parsed_files/Query/FindAll" + name + "s.java");
			pw.println("");

			String packageName = packageNames.get(PackageTypes.QUERY);
			pw.println(packageName);
			
			/*
			if(parentEntity.equals(name)) {
				pw.println("package com.skytala.eCommerce.domain." + this.lowerFirst(name) + ".query;");
			}else {
				pw.println("package com.skytala.eCommerce.domain." + lowerFirst(parentEntity) + ".relations." + this.lowerFirst(name) + ".query;");
			}*/
			
			pw.println("import java.util.ArrayList;");
			pw.println("import java.util.List;");

			pw.println("import org.apache.ofbiz.entity.Delegator;");
			pw.println("import org.apache.ofbiz.entity.DelegatorFactory;");
			pw.println("import org.apache.ofbiz.entity.GenericEntityException;");
			pw.println("import org.apache.ofbiz.entity.GenericValue;");
			pw.println("import com.skytala.eCommerce.framework.pubsub.Broker;");
			pw.println("import com.skytala.eCommerce.framework.pubsub.Query;");
			pw.println("import com.skytala.eCommerce.framework.pubsub.Event;");
			
			
			packageName = packageNames.get(PackageTypes.EVENT).replaceFirst("package", "import").replace(";", ".");
			pw.println(packageName + name + "Found;");
			
			packageName = packageNames.get(PackageTypes.MAPPER).replaceFirst("package", "import").replace(";", ".");
			pw.println(packageName + name + "Mapper;");

			packageName = packageNames.get(PackageTypes.MODEL).replaceFirst("package", "import").replace(";", ".");
			pw.println(packageName + name + ";");

			/*
			if(parentEntity.equals(name)) {
				pw.println("import com.skytala.eCommerce.domain."+this.lowerFirst(name)+".event."+name+"Found;");
				pw.println("import com.skytala.eCommerce.domain."+this.lowerFirst(name)+".mapper."+name+"Mapper;");
				pw.println("import com.skytala.eCommerce.domain."+this.lowerFirst(name)+".model."+name+";");
			}else {
				pw.println("import com.skytala.eCommerce.domain." + lowerFirst(parentEntity) + ".relations."+this.lowerFirst(name)+".event."+name+"Found;");
				pw.println("import com.skytala.eCommerce.domain." + lowerFirst(parentEntity) + ".relations."+this.lowerFirst(name)+".mapper."+name+"Mapper;");
				pw.println("import com.skytala.eCommerce.domain." + lowerFirst(parentEntity) + ".relations."+this.lowerFirst(name)+".model."+name+";");
			}*/
		
			

			pw.println("");
			pw.println("");

			pw.println("public class FindAll" + name + "s extends Query {");
			pw.println("");
			pw.println("@Override");
			pw.println("public Event execute() {");
			pw.println("");
			pw.println("Delegator delegator = DelegatorFactory.getDelegator(" + '"' + "default" + '"' + ");");
			pw.println("List<" + name + "> returnVal = new ArrayList<" + name + ">();");
			pw.println("try{");
			pw.println("List<GenericValue> results = delegator.findAll(" + '"' + name + '"' + ", false);");
			pw.println("for (int i = 0; i < results.size(); i++) {");
			pw.println("returnVal.add(" + name + "Mapper.map(results.get(i)));");
			pw.println("}");
			pw.println("} catch (GenericEntityException e) {");
			// TODO CHANGED HERE
			pw.println(" System.err.println(e.getMessage()); ");
			pw.println("}");
			pw.println("Event resultingEvent = new "+name+"Found(returnVal);");
			pw.println("Broker.instance().publish(resultingEvent);");
			pw.println("return resultingEvent;");
			pw.println("}");
			pw.println("}");

			pw.close();

		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public void generateQueryById(String name) {
		PrintWriter pw;

		try {

			pw = new PrintWriter(path+"/Query/Find" + name + "By.java");
			pw.println("");
			pw.println("package com.skytala.eCommerce.query;");
			pw.println("import java.sql.Timestamp;");
			pw.println("import java.util.LinkedList;");
			pw.println("import java.util.List;");

			pw.println("import org.apache.ofbiz.entity.Delegator;");
			pw.println("import org.apache.ofbiz.entity.DelegatorFactory;");
			pw.println("import org.apache.ofbiz.entity.GenericEntityException;");
			pw.println("	import org.apache.ofbiz.entity.GenericValue;");

			pw.println("import com.skytala.eCommerce.control.Broker;");
			pw.println("import com.skytala.eCommerce.entity." + name + ";");
			pw.println("import com.skytala.eCommerce.entity." + name + "Mapper;");
			pw.println("import com.skytala.eCommerce.event." + name + "Found;");

			pw.println("");
			pw.println("");
			pw.println("public class Find" + name + "sById implements Query {");
			pw.println("");
			pw.println("private String generalId;");
			pw.println("");
			pw.println("public Find" + name + "sById(String generalId){");
			pw.println("this.generalId = generalId;");
			pw.println("}");
			pw.println("");
			pw.println("public String getGeneralId(){");
			pw.println("return generalId;");
			pw.println("}");
			pw.println("");
			pw.println("public void setGeneralId(String generalId){");
			pw.println("this.generalId = generalId;");
			pw.println("}");
			pw.println("@Override");
			pw.println("public void execute() {");
			pw.println("");
			pw.println("Delegator delegator = DelegatorFactory.getDelegator(" + '"' + "default" + '"' + ");");
			pw.println("List<" + name + "> foundEntries = new LinkedList<>();");
			pw.println("");
			pw.println("try{");
			pw.println("");
			pw.println(
					"EntityCondition cond = EntityCondition.makeCondition(" + '"' + name + "Id" + '"' + ",generalId);");
			pw.println("List<String> orderBy = new LinkedList<>();");
			pw.println("orderBy.add(" + '"' + name + "Id DESC" + '"' + ");");
			pw.println("List<GenericValue> values = delegator.findList(" + '"' + name + '"'
					+ ", cond, null, orderBy, null, false);");
			pw.println("Timestamp currentDate = new Timestamp(System.currentTimeMillis());");
			pw.println("currentDate.setNanos(0);");
			pw.println("");
			pw.println("for (int i = 0; i < values.size(); i++) {");
			pw.println(name + " entry = " + name + "Mapper.map(values.get(i));");
			pw.println("");
			pw.println("foundEntries.add(entry);");
			pw.println("}");
			pw.println("} catch (GenericEntityException e) {");
			// TODO CHANGED HERE TO
			pw.println(" System.err.println(e.getMessage()); ");
			pw.println("}");
			pw.println("Broker.instance().publish(new " + name + "Found(foundEntries));");
			pw.println("}");
			pw.println("}");

			pw.close();
		} catch (Exception e) {
			// TODO: handle exception
		}

	}

	public void generateQueryBy(String name) {
		PrintWriter pw;

		try {

			pw = new PrintWriter(path+"/parsed_files/Query/Find" + name + "sBy.java", "UTF-8");
		

			String packageName = packageNames.get(PackageTypes.QUERY);
			pw.println(packageName);

			/*
			if(parentEntity.equals(name)) {
				pw.println("package com.skytala.eCommerce.domain." + this.lowerFirst(name) + ".query;");
			}else {
				pw.println("package com.skytala.eCommerce.domain." + lowerFirst(parentEntity) + ".relations." + this.lowerFirst(name) + ".query;");
			}*/
			
			pw.println("import java.util.ArrayList;");
			pw.println("import java.util.List;");
			pw.println("import java.util.Iterator;");
			pw.println("import java.util.Map;");
			pw.println("import java.util.LinkedList;");
			pw.println("");

			pw.println("import org.apache.ofbiz.entity.Delegator;");
			pw.println("import org.apache.ofbiz.entity.DelegatorFactory;");
			pw.println("import org.apache.ofbiz.entity.GenericEntityException;");
			pw.println("import org.apache.ofbiz.entity.GenericValue;");
			pw.println("import com.skytala.eCommerce.framework.pubsub.Broker;");
			pw.println("import com.skytala.eCommerce.framework.pubsub.Query;");
			pw.println("import com.skytala.eCommerce.framework.pubsub.Event;");
			pw.println("import com.skytala.eCommerce.framework.exceptions.RecordNotFoundException;");
	
			
			packageName = packageNames.get(PackageTypes.EVENT).replaceFirst("package", "import").replace(";", ".");
			pw.println(packageName + name + "Found;");
			
			packageName = packageNames.get(PackageTypes.MAPPER).replaceFirst("package", "import").replace(";", ".");
			pw.println(packageName + name + "Mapper;");
			
			packageName = packageNames.get(PackageTypes.MODEL).replaceFirst("package", "import").replace(";", ".");
			pw.println(packageName + name + ";");

			/*
			if(parentEntity.equals(name)) {
				pw.println("import com.skytala.eCommerce.domain."+this.lowerFirst(name)+".event."+name+"Added;");
				pw.println("import com.skytala.eCommerce.domain."+this.lowerFirst(name)+".event."+name+"Found;");
				pw.println("import com.skytala.eCommerce.domain."+this.lowerFirst(name)+".mapper."+name+"Mapper;");
				pw.println("import com.skytala.eCommerce.domain."+this.lowerFirst(name)+".model."+name+";");
			}else {
				pw.println("import com.skytala.eCommerce.domain." + lowerFirst(parentEntity) + ".relations."+this.lowerFirst(name)+".event."+name+"Added;");
				pw.println("import com.skytala.eCommerce.domain." + lowerFirst(parentEntity) + ".relations."+this.lowerFirst(name)+".event."+name+"Found;");
				pw.println("import com.skytala.eCommerce.domain." + lowerFirst(parentEntity) + ".relations."+this.lowerFirst(name)+".mapper."+name+"Mapper;");
				pw.println("import com.skytala.eCommerce.domain." + lowerFirst(parentEntity) + ".relations."+this.lowerFirst(name)+".model."+name+";");
			}*/
		

			pw.println("");
			pw.println("public class Find" + name + "sBy extends Query {");
			pw.println("");
			pw.println("");
			pw.println("Map<String, String> filter;");
			pw.println("public Find" + name + "sBy(Map<String, String> filter) {");
			pw.println("this.filter = filter;");
			pw.println("}");
			pw.println("");
			pw.println("@Override");
			pw.println("public Event execute(){");
			pw.println("");
			pw.println("Delegator delegator = DelegatorFactory.getDelegator(" + '"' + "default" + '"' + ");");
			pw.println("List<" + name + "> found" + name + "s = new ArrayList<" + name + ">();");
			pw.println("");
			pw.println("try{");
			// pw.println("List<GenericValue> buf = delegator.findAll(" + '"' +
			// name + '"' + ", false);");

			// TODO Fertigmachen
			pw.println("List<GenericValue> buf = new LinkedList<>();");
			pw.println("if(filter.size()==1&&filter.containsKey(" + '"' + this.lowerFirst(name) + "Id" + '"' + ")) { ");
			pw.println(" GenericValue foundElement = delegator.findOne(" + '"' + name + '"' + ", false, filter);");
			pw.println("if(foundElement != null) { ");
			pw.println("buf.add(foundElement);");
			pw.println("}else { ");
			pw.println("throw new RecordNotFoundException(" + name + ".class); ");
			pw.println(" } ");
			pw.println("}else { ");
			pw.println(" buf = delegator.findAll(" + '"' + name + '"' + ", false); ");
			pw.println(" }");
			pw.println("");

			pw.println("for (int i = 0; i < buf.size(); i++) {");
			pw.println("if(applysToFilter(buf.get(i))) {");
			pw.println("found" + name + "s.add(" + name + "Mapper.map(buf.get(i)));");
			pw.println("}");
			pw.println("}");
			pw.println("");
			pw.println("");
			pw.println("}catch(GenericEntityException e) {");
			pw.println("e.printStackTrace();");
			pw.println("}");
			pw.println("Event resultingEvent = new "+name+"Found(found"+name+"s);");
			pw.println("Broker.instance().publish(resultingEvent);");
			pw.println("return resultingEvent;");
			pw.println("");
			pw.println("}");
			pw.println("public boolean applysToFilter(GenericValue val) {");
			pw.println("");
			pw.println("Iterator<String> iterator = filter.keySet().iterator();");
			pw.println("");
			pw.println("while(iterator.hasNext()) {");
			pw.println("");
			pw.println("String key = iterator.next();");
			pw.println("");
			pw.println("if(val.get(key) == null) {");
			pw.println("return false;");
			pw.println("}");
			pw.println("");
			pw.println("if((val.get(key).toString()).contains(filter.get(key))) {");
			pw.println("}else {");
			pw.println("return false;");
			pw.println("}");
			pw.println("}");
			pw.println("return true;");
			pw.println("}");
			pw.println("public void setFilter(Map<String, String> newFilter) {");
			pw.println("this.filter = newFilter;");
			pw.println("}");
			pw.println("}");

			pw.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public String capitFirst(String eingabe) {
		String buf;
		buf = eingabe.substring(0, 1);
		buf = buf.toUpperCase();

		eingabe = buf + eingabe.substring(1);

		return eingabe;
	}

	public String lowerFirst(String eingabe) {
		String buf;
		buf = eingabe.substring(0, 1);
		buf = buf.toLowerCase();

		eingabe = buf + eingabe.substring(1);

		return eingabe;
	}

	public String getAttrType(String entityName, String parentEntityName, String attrName) {
		List<String> attr;
		attr = parser.ParsSingleEntity(entityName, 0, path + "/xmls/"+ lowerFirst(parentEntityName)+"-entitymodel.xml");
		int index = 0;
		for(int i = 0; i < attr.size(); i++) {
			if(attrName.equals(attr.get(i))) {
				index = i;
				break;
			}
		}
		
		return parser.ParsSingleEntity(entityName, 1, path + "/xmls/"+ lowerFirst(parentEntityName)+"-entitymodel.xml").get(index);
	}
	
	public static String getParentEntity() {
		return parentEntity;
	}

	public void setParentEntity(String parent) {
		parentEntity = parent;
	}

	public static String getPath() {
		return path;
	}

	public static void setPath(String path) {
		GenerateClass.path = path;
	}

	public Map<String, String> getPackageNames() {
		return packageNames;
	}

	public void setPackageNames(Map<String, String> packageNames) {
		this.packageNames = packageNames;
	}
	
	
}
