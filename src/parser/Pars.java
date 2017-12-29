package parser;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

public class Pars {

	private static String path;

/*	public static void main(String[] args) {
		Pars par = new Pars();
	
		par.initPath();
		par.getAllEntitys("/EntityXML/entities.xml");

		par.getAllServicesAndAttributes(new File(path + "/GITXML/miscellaneous/service_xmls/"));
		par.parsParsedServices();
		par.parsOverride();
		par.eliminateOut();
		par.eliminateDuplicates();
		par.split();
		
	}
*/
	public void doServices(){
		initPath();

		getAllServicesAndAttributes(new File(path + "/service_xmls/"));
	}

	public void initPath() {
		path = System.getProperty("user.dir");
	}

	public void split() {
		PrintWriter pw = null;
		File file = new File(path+"/xmls/");
		BufferedReader br;
		FileReader fr;
		String servicename;
		String line = "";
		String description = "";
		LinkedList<String> servicenames = new LinkedList<String>();
		int count = 0;

		try {
			for (File f : file.listFiles()) {

				fr = new FileReader(f);
				br = new BufferedReader(fr);
				while ((line = br.readLine()) != null) {
					if (description.equals("") && line.contains("<description")) {
						description = line;
					}
					if (line.contains("<service name=")) {
						servicename = this.parsanything(line, "<service name=");
						if (!servicename.equals("")) {
							servicenames.add(servicename);
						}
					}
				}
				if (servicenames.size() > 0) {
					pw = new PrintWriter(path + "/parsed_files/parsed/" + count + ".txt");
					count++;
					if (!description.equals("")) {
						pw.println("<description>"+f.getName()+"</description>");
					}
					for (int i = 0; i < servicenames.size(); i++) {
						printService(pw, servicenames.get(i));
					}
				}

				servicenames.clear();
				description = "";
				br.close();
				fr.close();
				if (pw != null) {
					pw.close();
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void printService(PrintWriter pw, String name) {
		BufferedReader br;
		FileReader fr;
		String line = "";
		String parsname = "";
		boolean lock = false;
		try {
			fr = new FileReader(path + "/outputParser/noduplicates.txt");
			br = new BufferedReader(fr);
			while ((line = br.readLine()) != null && !lock) {
				parsname = "";
				if (line.contains("Servicename:")) {
					parsname = this.parsanything(line, "Servicename:");
				}
				if (!parsname.equals("")) {
					if (parsname.equals(name)) {
						pw.println(line);
						while ((line = br.readLine()) != null && !line.contains("Servicename:")) {
							pw.println(line);
						}
						lock = true;
					}
				}
			}
			fr.close();
			br.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void getAllServicesAndAttributes(File file) {

		try {
			PrintWriter pw  = new PrintWriter(path + "/outputParser/services.txt");
			pw.close();
			int servicefilecount = 0;
			for (File f : file.listFiles()) {
				servicefilecount++;
				parsxmlservices(f);
				System.out.println("File: " + servicefilecount);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void parsxmlservices(File file) {
		FileReader fr = null;
		try {
			PrintWriter pw = new PrintWriter(new FileOutputStream(new File(path + "/outputParser/services.txt"), true));

			fr = new FileReader(file);
			BufferedReader br = new BufferedReader(fr);

			String bufstring = null;

			String entiname = "";
			String servicename = "";
			String implementstr = "";
			String defaultenti = "";
			boolean pk = false;
			boolean entioptional = false;
			boolean includeall = false;
			boolean inService = false;

			while ((bufstring = br.readLine()) != null) {

				LinkedList<String> excludelinklist = new LinkedList<String>();

				servicename = this.parsservicename(bufstring, pw);

				if (!servicename.equals("")) {
					inService = true;
				}

				if (bufstring.contains("default-entity-name") && !bufstring.equals("")) {
					defaultenti = this.parsentiname(bufstring);
					defaultenti = defaultenti.toLowerCase();

				}

				if (bufstring.toLowerCase().contains("<auto-attributes include=" + '"' + "nonpk" + '"')) {
					pk = false;
				} else if (bufstring.toLowerCase().contains("<auto-attributes include=" + '"' + "pk" + '"')) {
					pk = true;
					entioptional = false;
				}
				if (bufstring.contains("<auto-attributes")) {
					if (bufstring.toLowerCase().contains("entity-name=")) {
						entiname = this.parsanything(bufstring, "entity-name=");
						entiname = entiname.toLowerCase();
					}

					if (bufstring.toLowerCase().contains("include=" + '"' + "nonpk")) {
						pk = false;
					} else if (bufstring.contains("include=" + '"' + "pk" + '"')) {
						pk = true;
						entioptional = false;
					}

					if (bufstring.toLowerCase().contains("include=" + '"' + "all" + '"')) {
						includeall = true;
					}

					if (bufstring.toLowerCase().contains("optional=" + '"' + "false")) {

						entioptional = false;
					}

					if (bufstring.toLowerCase().contains("optional=" + '"' + "true")) {
						entioptional = true;

					}
					if (!entiname.equals("")) {
						if (includeall) {
							excludelinklist = this.checkforexclude(entiname, servicename, file);
							this.parsEntityAttribute(entiname, pw, excludelinklist, true, entioptional);
							this.parsEntityAttribute(entiname, pw, excludelinklist, false, entioptional);
							includeall = false;

						} else {
							excludelinklist = this.checkforexclude(entiname, servicename, file);
							this.parsEntityAttribute(entiname, pw, excludelinklist, pk, entioptional);
							entiname = "";

						}
					} else if (!defaultenti.equals("")) {
						if (includeall) {
							excludelinklist = this.checkforexclude(defaultenti, servicename, file);
							this.parsEntityAttribute(defaultenti, pw, excludelinklist, true, entioptional);
							this.parsEntityAttribute(defaultenti, pw, excludelinklist, false, entioptional);
							includeall = false;
						} else {

							excludelinklist = this.checkforexclude(defaultenti, servicename, file);
							this.parsEntityAttribute(defaultenti, pw, excludelinklist, pk, entioptional);
						}
					}

				}
				if (bufstring.contains("<implements service=")) {
					implementstr = this.parsanything(bufstring, "<implements service=");
					String modestr = this.parsanything(bufstring, "mode");
					pw.println("implements: " + '"' + implementstr + '"' + "  mode:" + '"' + modestr + '"');
				}
				if (inService) {
					if (bufstring.contains("<invoke name=")) {
						implementstr = this.parsanything(bufstring, "<invoke name=");
						// String modestr = this.parsanything(bufstring, key);
						pw.println("implements: " + '"' + implementstr + '"' + "  mode:" + '"' + "IN" + '"');

					}
				}
				if (bufstring.contains("</service>")) {
					inService = false;
				}

				if (bufstring.contains("<override name")) {
					pw.println(bufstring);

				}

				if (excludelinklist != null) {
					excludelinklist.clear();
				}
				this.parsattribute(bufstring, pw);

			}

			br.close();
			pw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public void eliminateOut() {
		BufferedReader br;
		FileReader fr;
		PrintWriter pw;
		//PrintWriter pr;
		String line;
		String mode;
		String name;
		String type;
		String optional;

		try {
			fr = new FileReader(path + "/outputParser/final.txt");
			br = new BufferedReader(fr);
			pw = new PrintWriter(path + "/outputParser/parsedServices.txt");

			while ((line = br.readLine()) != null) {
				if (line.contains("Attr:")) {
					mode = this.parsanything(line, "mode");

					if (mode.equals("OUT")) {

					} else {
						name = this.parsanything(line, "Attr:");
						type = this.parsanything(line, "type:");
						optional = this.parsanything(line, "optional:");
						pw.println("Attr: " + '"' + name + '"' + "  type:" + '"' + type + '"' + "  optional:" + '"'
								+ optional + '"');
					}
				} else {
					pw.println(line);
				}
			}
			br.close();
			fr.close();
			pw.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void parsOverride() {
		BufferedReader br;
		FileReader fr;
		PrintWriter pw;
		String line;
		String servicename = null;
		String attrname;
		String attrtype;
		String attroptional;
		String attrmode;
		String overridename = "";
		String overrideoptional = "";
		String overridemode = "";
		LinkedList<String> attributes = new LinkedList<String>();
		LinkedList<String> types = new LinkedList<String>();
		LinkedList<String> optional = new LinkedList<String>();
		LinkedList<String> modes = new LinkedList<String>();
		try {
			fr = new FileReader(path + "/outputParser/parsedParsed.txt");
			br = new BufferedReader(fr);
			pw = new PrintWriter(path + "/outputParser/final.txt");

			while ((line = br.readLine()) != null) {

				if (line.contains("Servicename")) {

					for (int i = 0; i < attributes.size(); i++) {

						pw.println("Attr:" + '"' + attributes.get(i) + '"' + "  " + "type:" + '"' + types.get(i) + '"'
								+ "  " + "optional:" + '"' + optional.get(i) + '"' + " mode:" + '"' + modes.get(i)
								+ '"');
					}
					servicename = this.parsanything(line, "Servicename: ");
					pw.println("");
					pw.println("");
					pw.println("Servicename: " + '"' + servicename + '"');
					attributes.clear();
					optional.clear();
					types.clear();
					modes.clear();
				}
				if (line.contains("Attr:")) {
					attrname = this.parsanything(line, "Attr:");
					attrtype = this.parsanything(line, "type:");
					attroptional = this.parsanything(line, "optional:");
					attrmode = this.parsanything(line, "mode:");

					// System.out.println(attrname + " " + attrtype + " " +
					// attropt);
					attributes.add(attrname);
					types.add(attrtype);
					optional.add(attroptional);
					modes.add(attrmode);
				}
				if (line.contains("<override")) {
					if (line.contains("name")) {
						overridename = this.parsanything(line, "name=");
					}
					if (line.contains("optional")) {
						overrideoptional = this.parsanything(line, "optional=");
					}
					if (line.contains("mode")) {
						overridemode = this.parsanything(line, "mode");
					}

					for (int i = 0; i < attributes.size(); i++) {
						if (attributes.get(i).equals(overridename)) {
							if (!overrideoptional.equals("")) {
								optional.set(i, overrideoptional);
							}
							if (!overridemode.equals("")) {
								modes.set(i, overridemode);
							}
						}
					}
					overridename = "";
					overrideoptional = "";
					overridemode = "";
				}
			}
			for (int i = 0; i < attributes.size(); i++) {
				pw.println("Attr:" + '"' + attributes.get(i) + '"' + "  " + "type:" + '"' + types.get(i) + '"' + "  "
						+ "optional:" + '"' + optional.get(i) + '"');
			}
			pw.close();
			br.close();
			fr.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public void eliminateDuplicates() {
		BufferedReader br;
		FileReader fr;
		PrintWriter pw;
		String line;
		String attribute = "";
		String servicenamestr = "";
		boolean lock = false;
		boolean namelock = false;

		LinkedList<String> mylist = new LinkedList<String>();
		LinkedList<String> servicenames = new LinkedList<String>();
		try {
			int count = 0;
			fr = new FileReader(path + "/outputParser/parsedServices.txt");
			br = new BufferedReader(fr);
			pw = new PrintWriter(path + "/outputParser/noduplicates.txt");
			while ((line = br.readLine()) != null) {
				if (line.contains("Servicename: ")) {
					namelock = false;
					servicenamestr = this.parsanything(line, "Servicename:");
					for (int i = 0; i < servicenames.size(); i++) {
						if (servicenames.get(i).equals(servicenamestr)) {
							System.out.println(servicenamestr + " existiert doppelt");
							namelock = true;
						}
					}
					if (namelock) {
						mylist.clear();
					} else {
						servicenames.add(servicenamestr);
						mylist.clear();
						count++;
						System.out.println("Eliminate Duplicates of Service: "+count);
					}
				}
				if (!namelock) {

					if (line.contains("Attr: ")) {
						attribute = this.parsanything(line, "Attr: ");
					} else {
						pw.println(line);
					}

					if (!attribute.equals("")) {
						for (int i = 0; i < mylist.size(); i++) {
							if (mylist.get(i).equals(attribute)) {
								lock = true;
							}
						}
						if (!lock) {
							pw.println(line);
							mylist.add(attribute);
						}
						lock = false;
						attribute = "";
					}
				}
			}
			System.out.println("Reached end of file");
			br.close();
			fr.close();
			pw.close();

		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public void getAllServicenames(File file) {
		PrintWriter pw;
		FileReader fr;
		BufferedReader br;
		Set<String> meinSet = new HashSet<String>();

		int count = 0;
		try {
			pw = new PrintWriter(new FileOutputStream(new File("services.txt"), true));
			for (File f : file.listFiles()) {

				// for (int i = 0; i <= 18; i++) {
				// if (i == 0) {
				// fr = new
				// FileReader(path+"/GITXML/miscellaneous/service_xmls/services.xml");
				// } else {
				// fr = new
				// FileReader(path+"/GITXML/miscellaneous/service_xmls/services"+i+".xml");
				fr = new FileReader(f);

				br = new BufferedReader(fr);
				String line;

				while ((line = br.readLine()) != null) {
					line = this.parsanything(line, "<service name");
					if (!line.equals("")) {
						// pw.println(line);
						if (meinSet.add(line)) {
							pw.println(line);
							count++;
						}
					}
				}
			}
			pw.println(count);
			pw.close();
		} catch (Exception e) {

		}

	}

	public void getAllPrimaryKeys(String filename) {
		try {
			FileReader fr = new FileReader(path + "/XMLSrc/" + filename);
			// PrintWriter pw = new PrintWriter("ausgabe.txt");
			PrintWriter pw = new PrintWriter(new FileOutputStream(new File("entitys.txt"), true));
			BufferedReader br = new BufferedReader(fr);
			String entimax, enticache;
			entimax = "";
			enticache = "";
			String test, buff;
			int max, cache;
			max = 0;
			cache = 0;

			boolean inEntity = false;
			while ((test = br.readLine()) != null) {
				buff = test;
				test = this.parsanything(test, "<entity entity-name=" + '"');
				if (!test.equals("")) {
					pw.println(test);
					inEntity = true;
					cache = 0;
					enticache = test;
				}
				if (buff.contains("</entity>")) {
					inEntity = false;
					pw.println("");
					if (cache > max) {
						max = cache;
						entimax = enticache;
					}
				}
				if (inEntity == true) {
					buff = this.parsanything(buff, "<prim-key field=");

					if (!buff.equals("")) {
						pw.println("Primary Key:  " + buff);
						cache++;
					}
				}
			}
			pw.println(max + "  " + entimax);

			pw.close();
			fr.close();
		} catch (IOException e) {
			// 
		}

	}

	public void getAllEntitys(String filename) {
		try {
			FileReader fr = new FileReader(path + filename);
			// PrintWriter pw = new PrintWriter("ausgabe.txt");
			PrintWriter pw = new PrintWriter(new FileOutputStream(new File(path+"parsed_files/entitys.txt"), true));
			BufferedReader br = new BufferedReader(fr);

			String test;
			while ((test = br.readLine()) != null) {
				// while(br.read()!=-1){
				// test = br.readLine();
				test = this.parsanything(test, "<entity entity-name=" + '"');
				if (!test.equals("")) {

					pw.println(test);
				}
			}

			pw.close();
			fr.close();
		} catch (IOException e) {
			// 
		}

	}

	public void parsxmlentity() {
		FileReader fr;
		PrintWriter pw;
		BufferedReader br;
		String line;
		LinkedList<String> mylist = new LinkedList<String>();

		try {
			pw = new PrintWriter("ausgabe.txt", "UTF-8");
			fr = new FileReader("productentity.xml");
			br = new BufferedReader(fr);

			while (br.read() != -1) {
				line = br.readLine();
				mylist = this.parsEntityAttributeTypes(mylist, line, pw);
			}
			for (int i = 0; i < mylist.size(); i++) {

				pw.println(mylist.get(i));

			}
			pw.close();
			fr.close();
			br.close();
		} catch (IOException e) {
			// 
		}

	}

	public LinkedList<String> ParsSingleEntity(String entityname, int key, String filename) {
		LinkedList<String> attributenamelist = new LinkedList<String>();
		LinkedList<String> attributetypelist = new LinkedList<String>();
//		System.out.println(path + "/xmls/" + filename);
		try {

			FileReader fr = new FileReader(path + "/xmls/" + filename);
			BufferedReader br = new BufferedReader(fr);
			String line;
			String buf;

			while ((line = br.readLine()) != null) {

				buf = "<entity entity-name=" + '"' + entityname + '"';
				if (line.contains(buf)) {
					while (!line.contains("</entity>")) {
						if (line.contains("<field name")) {
							attributenamelist = this.parsAnything(line, "<field name", attributenamelist);
							attributetypelist = this.parsAnything(line, "type=", attributetypelist);

						}
						line = br.readLine();
					}

				}
			}

			br.close();
		} catch (IOException e) {
			// 
		}
		

		if (key == 0)
			return attributenamelist;
		else {
			return attributetypelist;
		}
	}

	public LinkedList<String> parsEntityAttributeTypes(LinkedList<String> mylist, String line, PrintWriter pw) {
		LinkedList<String> compare = new LinkedList<String>();
		if (line.contains("<field name")) {
			compare = this.parsAnything(line, "type=", compare);
		}

		for (int i = 0; i < mylist.size(); i++) {
			for (int x = 0; x < compare.size(); x++) {
				if (mylist.get(i).equals(compare.get(x))) {
					return mylist;
				}

			}

		}

		if (compare.size() != 0)
			mylist.add(compare.getFirst());

		return mylist;
	}

	public String parsservicename(String bufstring, PrintWriter pw) {
		String servicename = "";
		servicename = this.parsanything(bufstring, "<service name");
		if (!servicename.equals("")) {
			pw.println("");
			pw.println("");
			pw.println("Servicename: " + '"' + servicename + '"');
		}

		return servicename;
	}

	public String parsentiname(String bufstring) {
		bufstring = bufstring.toLowerCase();
		boolean mybool = false;
		boolean lock = false;
		String servicename = "";

		if (bufstring.contains("default-entity-name")) {
			for (int i = 0; lock == false; i++) {
				int index = bufstring.indexOf("default-entity-name") + i;
				if (mybool == true && bufstring.charAt(index) != '"') {
					servicename = servicename + bufstring.charAt(index);
				}
				if (bufstring.charAt(bufstring.indexOf("default-entity-name") + i) == '"') {
					if (mybool == true) {
						lock = true;
					}
					mybool = true;
				}

			}

		}

		return servicename;
	}

	public void parsattribute(String bufstring, PrintWriter pw) {
		String attr = "";
		String mode;
		String type;
		String optional;

		if (bufstring.contains("<attribute")) {
			attr = this.parsanything(bufstring, "name");
		}
		mode = this.parsanything(bufstring, "mode=");
		type = this.parsanything(bufstring, "type=");
		optional = this.parsanything(bufstring, "optional=");
		if (optional.equals("")) {
			optional = "false";
		}
		if (!attr.equals("")) {
			if (mode.equals("OUT")) {
				pw.println("Attr:  " + '"' + attr + '"' + "  type:  " + '"' + type + '"' + "  optional: " + '"'
						+ optional + '"' + " mode:" + '"' + mode + '"');

			} else {
				pw.println("Attr:  " + '"' + attr + '"' + "  type:  " + '"' + type + '"' + "  optional: " + '"'
						+ optional + '"' + " mode" + '"' + mode + '"');
			}
		}

	}

	public LinkedList<String> parsAnything(String bufstring, String key, LinkedList<String> linklist) {
		boolean mybool, lock;
		mybool = false;
		lock = false;
		String servicename = "";

		if (bufstring.contains(key)) {
			for (int i = 0; i < bufstring.length() && lock == false; i++) {
				if (mybool) {

					if (bufstring.charAt(i + bufstring.indexOf(key)) != '"') {
						servicename = servicename + bufstring.charAt(i + bufstring.indexOf(key));
					}
				}

				if (bufstring.charAt(i + bufstring.indexOf(key)) == '"') {

					mybool = !mybool;
					if (mybool == false) {
						linklist.add(servicename);
						lock = true;

					}

				}

			}

		}

		return linklist;
	}

	public String parsanything(String bufstring, String key) {
		boolean mybool, lock;
		mybool = false;
		lock = false;
		String servicename = "";
		if (bufstring.contains(key)) {
			for (int i = 0; i < bufstring.length() && lock == false; i++) {
				if (mybool) {

					if (bufstring.charAt(i + bufstring.indexOf(key)) != '"') {
						servicename = servicename + bufstring.charAt(i + bufstring.indexOf(key));
					}
				}
				if (bufstring.charAt(i + bufstring.indexOf(key)) == '"') {

					mybool = !mybool;
					if (mybool == false) {
						return servicename;
					}
				}
			}

		}

		return "";
	}

	public void parsEntityAttribute(String entityname, PrintWriter pw, LinkedList<String> exclude, boolean primaryKey,
			boolean optional) {
		if (!entityname.equals("")) {
			LinkedList<String> pk = new LinkedList<String>();

			FileReader fr;
			BufferedReader br;
			String line;
			String Fieldname;
			String attrtype;
			String filepath;
			// filepath = path+"/XMLSrc/test.xml";
			filepath = path + "/xmls/entities.xml";
			// File myfile = new File(path+"/XMLSrc/");

			boolean lock = true;
			boolean excluded = false;
			boolean ispk = false;
			try {

				// for (File f : myfile.listFiles()) {

				fr = new FileReader(filepath);
				br = new BufferedReader(fr);

				while ((line = br.readLine()) != null && lock) {
					if (line.toLowerCase().contains("<entity entity-name=" + '"' + entityname + '"')) {

						while (lock) {
							if (primaryKey) {
								line = br.readLine();
								// line = line.toLowerCase();
								Fieldname = this.parsanything(line, "<field name=");
								attrtype = this.parsanything(line, "type=");
								if (!Fieldname.equals("") && !attrtype.equals("")) {

									for (int i = 0; i < exclude.size(); i++) {
										if (exclude.get(i).equals(Fieldname)) {
											System.out.println("Excluded: " + Fieldname);
											excluded = true;
										}

									}
									if (!excluded) {
										pk = this.getPK(filepath, entityname);
										for (int i = 0; i < pk.size(); i++) {
											if (pk.get(i).equals(Fieldname)) {
												attrtype = this.convertEntityTypeToJava(attrtype);
												pw.println("Attr: " + '"' + Fieldname + '"' + "  " + "type:" + '"'
														+ attrtype + '"' + " optional:" + '"' + optional + '"' + "  "
														+ '"' + "pk" + '"');
												ispk = true;
											}
										}
										ispk = false;
									}
									excluded = false;

								}
								if (line.contains("</entity")) {
									lock = false;
								}

							} else {

								line = br.readLine();
								// line = line.toLowerCase();
								Fieldname = this.parsanything(line, "<field name=");
								attrtype = this.parsanything(line, "type=");
								if (!Fieldname.equals("") && !attrtype.equals("")) {

									for (int i = 0; i < exclude.size(); i++) {
										if (exclude.get(i).equals(Fieldname)) {
											System.out.println("Excluded: " + Fieldname);
											excluded = true;
										}

									}
									if (!excluded) {
										pk = this.getPK(filepath, entityname);
										for (int i = 0; i < pk.size(); i++) {
											if (pk.get(i).equals(Fieldname)) {

												ispk = true;
											}
										}
										if (!ispk) {

											attrtype = this.convertEntityTypeToJava(attrtype);
											pw.println("Attr:  " + '"' + Fieldname + '"' + "  " + "  type:" + '"'
													+ attrtype + '"' + "  optional:" + '"' + optional + '"' + "  " + '"'
													+ "nopk" + '"');

										}
										ispk = false;
									}
									excluded = false;

								}
								if (line.contains("</entity")) {
									lock = false;
								}

							}

						}
					}

				}
				// }
			} catch (IOException e) {
				e.printStackTrace();

			}

		}

	}

	public LinkedList<String> getPK(String path, String entityname) {
		LinkedList<String> pks = new LinkedList<String>();
		try {
			FileReader fr = new FileReader(path);
			BufferedReader br = new BufferedReader(fr);
			String line;

			while ((line = br.readLine()) != null) {
				if (line.toLowerCase().contains("<entity entity-name=" + '"' + entityname + '"')) {
					while (!(line = br.readLine()).contains("</entity")) {
						line = this.parsanything(line, "<prim-key");
						if (!line.equals("")) {
							pks.add(line);
						}
					}
				}

			}

			br.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return pks;
	}

	public LinkedList<String> checkforexclude(String entityname, String servicename, File f) {

		if (!entityname.equals("")) {
			LinkedList<String> excludelist = new LinkedList<String>();
			String line = null;
			FileReader fr;
			//BufferedReader br;
			boolean inservice = false;
			try {
				fr = new FileReader(f);
				BufferedReader br = new BufferedReader(fr);

				while ((line = br.readLine()) != null) {

					if (line.toLowerCase().contains("<service name=" + '"' + servicename + '"')) {
						inservice = true;
					}
					if (inservice) {
						for (line = br.readLine(); !line.contains("</service"); line = br.readLine()) {
							if (line.contains("<exclude")) {
								excludelist = this.parsAnything(line, "<exclude field-name", excludelist);
							}
						}
						br.close();
						return excludelist;
					}
				}
				br.close();
			} catch (IOException e) {
				// 
				e.printStackTrace();
			}
			return excludelist;
		}
		return null;
	}

	public void parsParsedServices() {
		PrintWriter pw;
		FileReader fr;
		BufferedReader br;

		try {
			pw = new PrintWriter(path + "/outputParser/parsedParsed.txt");
			fr = new FileReader(path + "/outputParser/services.txt");
			br = new BufferedReader(fr);

			String line;
			String implementsService;
			String mode;

			while ((line = br.readLine()) != null) {
				if (line.contains("implements")) {
					mode = this.parsanything(line, "mode:");
					implementsService = this.parsanything(line, "implements");
					if (!implementsService.equals("")) {
						this.printImplementsAttributes(pw, implementsService, mode);
					}
				} else {
					pw.println(line);
				}
			}

			fr.close();
			br.close();
			pw.close();
		} catch (IOException e) {
			// 
		}

	}

	public void printImplementsAttributes(PrintWriter pw, String servicename, String mode) {
		FileReader fr;
		BufferedReader br;
		String line = "";
		String buf = "";
		String implement = "";
		String name = "";
		String type = "";
		String opt = "";
		String mymode = "";
		boolean doOnce = true;
		try {
			fr = new FileReader(path + "/outputParser/services.txt");
			br = new BufferedReader(fr);

			while ((line = br.readLine()) != null && doOnce) {
				if (line.contains("Servicename: " + '"' + servicename + '"')) {
					while (!(line = br.readLine()).contains("Servicename")) {
						buf = this.parsanything(line, "Attr");
						if (!buf.equals("")) {
							name = this.parsanything(line, "Attr:");
							type = this.parsanything(line, "type:");
							opt = this.parsanything(line, "optional");
							mymode = this.parsanything(line, "mode:");

							pw.println("Attr: " + '"' + name + '"' + "  type:" + '"' + type + '"' + "  optional:" + '"'
									+ opt + '"' + "  mode:" + '"' + mymode + '"');
							// pw.println(line);
						}
						if (line.contains("implements")) {
							implement = this.parsanything(line, "implements:");
							mymode = this.parsanything(line, "mode");
							if (!implement.equals("")) {
								this.printImplementsAttributes(pw, implement, mode);
							}
						}

					}
					doOnce = false;
				}
			}

		} catch (IOException e) {
			// 
		}

	}

	public String convertEntityTypeToJava(String type) {

		String converted;

		switch (type) {
		case "id-ne":
			converted = "String";
			break;
		case "name":
			converted = "String";
			break;
		case "indicator":
			converted = "boolean";
			break;
		case "url":
			converted = "String";
			break;
		case "long-varchar":
			converted = "String";
			break;
		case "id":
			converted = "String";
			break;
		case "date-time":
			converted = "Timestamp";
			break;
		case "numeric":
			converted = "long";
			break;
		case "description":
			converted = "String";
			break;
		case "very-long":
			converted = "String";
			break;
		case "id-long-ne":
			converted = "String";
			break;
		case "value":
			converted = "long";
			break;
		case "fixed-point":
			converted = "BigDecimal";
			break;
		case "comment":
			converted = "String";
			break;
		case "currency-amount":
			converted = "BigDecimal";
			break;
		case "very-short":
			converted = "String";
			break;
		case "id-long":
			converted = "String";
			break;
		case "currency-precise":
			converted = "BigDecimal";
			break;
		case "id-vlong":
			converted = "String";
			break;
		case "short-varchar":
			converted = "String";
			break;
		case "id-vlong-ne":
			converted = "String";
			break;
		case "floating-point":
			converted = "BigDecimal";
			break;
		case "email":
			converted = "String";
			break;
		case "date":
			converted = "Timestamp";
			break;
		case "credit-card-date":
			converted = "Timestamp";
			break;
		case "credit-card-number":
			converted = "String";
			break;
		case "time":
			converted = "Timestamp";
			break;
		// TODO Change again
		case "byte-array":
			converted = "byte[]";
			break;
		// TODO Change again
		case "object":
			converted = "Object";
			// copybuffer.add("Object");
			break;
		// might make some Problems
		case "blob":
			converted = "java.nio.ByteBuffer";
			break;

		default:
			System.out.println("Error mit:  " + type);
			converted = "Object";
			break;
		}

		return converted;
	}
}