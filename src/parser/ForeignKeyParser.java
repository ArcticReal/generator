package parser;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import parser.Objects.EntityField;
import parser.Objects.EntityFieldWithResolve;
import parser.Objects.EntityTxtJson;
import parser.Objects.ForeignKey;
import parser.Util.StringUtil;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

import static parser.Util.StringUtil.firstToLowerCase;


class ForeignKeyParser {

	String pathToXmls = "/home/work/workspace/ControllingParser/xmls/";
	private final String pathToFKtxt = "/home/work/workspace/ControllingParser/parsed_files/FKs/";
	private final String writeTo = System.getProperty("user.dir")+ "/parsed_files/";
	private final String pathToGraphQL = "/home/work/workspace/QLGen/resources/";
	private final Charset ENCODING = StandardCharsets.UTF_8;
	private Map<String, Map<String, ForeignKey>> fkBuffer = new HashMap<>();
	private Map<String, Map<String, List<ForeignKey>>> fkToBuffer = new HashMap<>();
	private Map<String, Map<String, Map<String, String>>> importBuffer = new HashMap<>();
	private Map<String, String> entityFolderStructureBuffer = new HashMap<>();
	private Parser parser;

	ForeignKeyParser(Parser parser) throws IOException {
		this.parser = parser;
	}

	void doIt() throws IOException {
		writePrimaryKeys();
		writeForeignKeysFrom();
		writeForeignKeysTo();
	}

	private void writePrimaryKeys() throws IOException {

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

	Map<String, List<String>> scanXmlsForPrimaryKeys() throws IOException {
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


	void writeForeignKeysFrom() throws IOException {
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
				writer.println("\n"
						+ count+ ":\n"
						+ "	Foreign Key Name:	" + fkForEntity.get(i).split(":")[0].trim() + "\n"
						+ "	Foreign key Field:	" + fkForEntity.get(i).split(":")[3].trim() + "\n"
						+ "	Related Entity:		" + fkForEntity.get(i).split(":")[1].trim() + "\n"
						+ "	referenced Field:	" + fkForEntity.get(i).split(":")[2].trim() + "\n"
						+ "	isMany:				" + fkForEntity.get(i).split(":")[4].trim() + "\n");
				count ++;
			}

			writer.close();
		}

	}

	Map<String, List<String>> scanXmlsForForeignKeysIn() throws IOException {
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
					String fieldName = "";
					String isMany = "one";

					for (int i = 0; i < splittedLine.length - 1; i++) {

						if(splittedLine[i].contains(" type=")) {
							isMany = splittedLine[i+1];
						}
						if (splittedLine[i].contains("fk-name=")) {
							fkName = (splittedLine[i + 1]);
						}

						if (splittedLine[i].contains("rel-entity-name=")) {
							relEntityName = (splittedLine[i + 1]);
						}

					}
					if(fkName.equals(""))
						continue;

					line = scanner.nextLine();
					splittedLine = line.split("\"");

					for (int i = 0; i < splittedLine.length - 1; i++) {

						if(splittedLine[i].contains("rel-field-name=")) {
							refField = (splittedLine[i + 1]);
						}else if (splittedLine[i].contains("field-name=")) {
							fieldName = (splittedLine[i + 1]);
						}

					}

					fK.add(fkName + " : " + relEntityName + " : " + refField + " : " + fieldName +":" + isMany);

				}

				if (line.contains("</entity>")) {

					returnVal.put(currentEntity, fK);
					fK = new LinkedList<>();
				}

			}

			scanner.close();
		}

		return returnVal;

	}

	private void writeForeignKeysTo() throws IOException {
		Parser parser = new Parser();
		parser.readFromXmls();
		String path = writeTo + "/FKs/to/";
		File f = new File(path);
		f.mkdirs();

		Iterator<String> it = parser.readConfig().keySet().iterator();
		while (it.hasNext()) {

			String nextParentFolder = it.next();
			Iterator<String> specificIt = parser.readConfig().get(nextParentFolder).iterator();
			while(specificIt.hasNext()) {
				String nextEntity = specificIt.next();
				//nextEntity = "Product";
				PrintWriter writer = new PrintWriter(path + nextEntity + ".txt");

				System.out.println(nextEntity);

				Map<String, String> foreignKeys = scanXmlsForForeignKeys(nextEntity);

				writer.println(nextEntity + ":");
				Iterator<String> it2 = foreignKeys.keySet().iterator();
				int count = 1;
				while (it2.hasNext()) {
					String next = it2.next();
					String[] splittedLine = foreignKeys.get(next).split(":");
					writer.println("\n"
							+ count + "\n"
							+ "	Foreign Key Name: 	" + next.split(":")[1].trim() + "\n"
							+ "	Foreign key Field:	" + splittedLine[1].trim() + "\n"
							+ "	Related Entity: 	" + next.split(":")[0].trim() + "\n"
							+ "	referenced Field: 	" + splittedLine[0].trim() + "\n"
							+ "	isMany:				" + splittedLine[2].trim() + "\n"
							+ "	");
					count++;
				}

				writer.close();
				//break;
			}
		}

	}

	private Map<String, String> scanXmlsForForeignKeys(String entityName) throws IOException {
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
					String fieldName = "";
					String isMany = "one";
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
						}else if (splittedLine[i].contains("rel-field-name=")) {
							fieldName = splittedLine[i + 1];
						}

					}

					if (relEntityName.equals(entityName)) {
						if(fieldName.equals(""))
							fieldName = refField;

						returnVal.put(currentEntity + " : " + fkName, refField + " : " + fieldName + " : " + isMany);
					}

				}

			}

			scanner.close();
		}

		return returnVal;
	}

	void writeEntityFields() throws IOException {

		generateFolderStructureBuffer();

		Gson gson = new GsonBuilder()
				.setPrettyPrinting()
				.create();

		Map<String, List<Map<String, EntityTxtJson>>> entityStructure = new HashMap<>();
		for (File f : new File(pathToXmls).listFiles()){

			List<EntityTxtJson> entityList = new LinkedList<>();
			List<EntityTxtJson> inputTypes = new LinkedList<>();

			String folderName = f.getName().replace("-entitymodel.xml", "/");
			String path = pathToGraphQL + "/entity_jsons/" + folderName;

			File directory = new File(path);
			directory.mkdirs();

			Map<String, Map<String, String>> entitiesWithFields = scanXMLsForEntityFields(f);
			List<Map<String, EntityTxtJson>> inputAndOutputTypesList = new LinkedList<>();
			for(String entityName : entitiesWithFields.keySet()){
				Map<String, String> attributes = entitiesWithFields.get(entityName);

				Map<String, EntityTxtJson> inputAndOutputTypes = new HashMap<>();

				List<EntityField> fields = new LinkedList<>();
				attributes.forEach((key, value) -> fields.add(buildEntityField(entityName, key, value)));

				//build Fields For Reversed Keys
				attributes.forEach((key, value) -> {
					fields.addAll(buildEntityFieldsReverseKeys(entityName, key, value));


				});

				fields.forEach((field) -> {
					if (field.getClass().equals(EntityFieldWithResolve.class)){
						pushRequiredImport(entityName,
											field.getFieldType()
												 .replace("new GraphQLList(", "")
												 .replace(")", "")
												 .replaceAll("Type$", ""));
					}
				});

				EntityTxtJson entityObject = new EntityTxtJson();
				entityObject.setEntityName(entityName);
				entityObject.setFields(fields.stream().distinct().collect(Collectors.toList()));

				inputAndOutputTypes.put("objectType", entityObject);


				//build input type
				List<EntityField> inputFields = new LinkedList<>();
				attributes.forEach((attrName, attrType) -> {

					inputFields.add(mapToField(attrName, attrType));
				});

				entityObject = new EntityTxtJson();
				entityObject.setEntityName(entityName);
				entityObject.setFields(inputFields.stream().distinct().collect(Collectors.toList()));

				inputAndOutputTypes.put("inputType", entityObject);
				inputAndOutputTypesList.add(inputAndOutputTypes);
			}
			entityStructure.put(folderName.replace("/", ""), inputAndOutputTypesList);

		}

		PrintWriter writer = new PrintWriter(pathToGraphQL + "/entity_jsons/overall.json");
		writer.print(gson.toJson(entityStructure));
		writer.close();

		writer = new PrintWriter(pathToGraphQL + "/import.json");
		writer.print(gson.toJson(importBuffer));
		writer.close();


	}

	private List<EntityFieldWithResolve> buildEntityFieldsReverseKeys(String entityName, String attrName, String attrType) {
		List<EntityFieldWithResolve> returnVal = new LinkedList<>();
		List<ForeignKey> FKs = getReverseFKs(entityName, attrName);
		if(FKs==null)
			return returnVal;

		for(ForeignKey FK : FKs){

			if(FK!=null&&getFolderName(FK.getRelatedEntity())!=null){
				EntityFieldWithResolve field = new EntityFieldWithResolve();

				EntityField arg = new EntityField();
				arg.setFieldName(attrName);
				arg.setFieldType(attrType);
				List<EntityField> args = new LinkedList<>();
				//args.add(arg);
				field.setArgs(args);

				field.setFieldName(firstToLowerCase(FK.getRelatedEntity()));
				field.setParentType(firstToLowerCase(entityName));
				field.setFetchUrl(parser.getMapping(FK.getRelatedEntity()) + "/find?"
						+ FK.getReferencedField() + "=${"
						+ field.getParentType() + "."
						+ attrName + "}");
				if(FK.isMany()){
					field.setLoader("ofbizArray");
					field.setFieldType("new GraphQLList(" + FK.getRelatedEntity()+ "Type)");
					field.setFieldName(StringUtil.singularToPlural(field.getFieldName()));
				}
				else{
					field.setLoader("ofbiz");
					field.setFieldType(FK.getRelatedEntity() + "Type");
				}
				returnVal.add(field);

			}
		}
		return returnVal;

	}

	private void generateFolderStructureBuffer() throws FileNotFoundException {
		for (File f : new File(pathToXmls).listFiles()){
			if(f.isDirectory())
				continue;
			Scanner scanner = new Scanner(f);
			while(scanner.hasNextLine()){
				String line = scanner.nextLine();
				if(line.contains("<entity entity-name=\"")){
					entityFolderStructureBuffer.put(line.split("\"")[1],
							f.getName().replace("-entitymodel.xml", ""));
				}
			}
			scanner.close();

		}
	}

	private String getFolderName(String entityName) {
		String returnVal = entityFolderStructureBuffer.get(entityName);
		if(returnVal==null){
			//System.out.println("null folder: " + entityName);
		}
		return returnVal;
	}

	private EntityField mapToField(String attrName, String attrType) {
		EntityField field = new EntityField();
		field.setFieldName(attrName);
		field.setFieldType(attrType);
		return field;
	}

	private EntityField buildEntityField(String entityName, String attrName, String attrType){
		EntityField returnVal;
		ForeignKey FK = getFK(entityName, attrName);
		if(FK!=null&&getFolderName(FK.getRelatedEntity())!=null){
			EntityFieldWithResolve field = new EntityFieldWithResolve();

			EntityField arg = new EntityField();
			arg.setFieldName(attrName);
			arg.setFieldType(attrType);
			List<EntityField> args = new LinkedList<>();
			args.add(arg);
			field.setArgs(args);

			field.setParentType(firstToLowerCase(entityName));
			field.setFetchUrl(parser.getMapping(FK.getRelatedEntity()) + "/find?" + FK.getReferencedField() + "=${" +
					field.getParentType() + "." + attrName + "}");
			if(FK.isMany()){
				field.setLoader("ofbizArray");
				field.setFieldType("new GraphQLList(" + FK.getRelatedEntity()+ "Type)");
			}
			else{
				field.setLoader("ofbiz");
				field.setFieldType(FK.getRelatedEntity() + "Type");
			}
			field.setFieldName(attrName.replace("Id", ""));
			returnVal = field;

		}else{
			returnVal = mapToField(attrName, attrType);
		}
		return returnVal;
	}

	private ForeignKey getFK(String entityName, String attrName) {

		if(fkBuffer.get(entityName)==null){
			loadBuffer(entityName);
		}

		if(fkBuffer.get(entityName)==null)
			return null;

		return fkBuffer.get(entityName).get(attrName);

	}

	private List<ForeignKey> getReverseFKs(String entityName, String attrName) {
		if(fkToBuffer.get(entityName)==null){
			loadBuffer(entityName);
		}

		if(fkToBuffer.get(entityName)==null)
			return null;

		return fkToBuffer.get(entityName).get(attrName);
	}

	private void loadBuffer(String entityName) {

		try {
			File fTo = new File(pathToFKtxt + "to/" + entityName + ".txt");
            fkToBuffer.put(entityName, generateKeysTo(entityName, fTo));
			File f = new File(pathToFKtxt + "from/" + entityName + ".txt");
            fkBuffer.put(entityName, generateKeysFrom(entityName, f));
        } catch (FileNotFoundException e) {
            System.err.println(e.getMessage());
        }


	}

	private Map<String, ForeignKey> generateKeysFrom(String entityName, File f) throws FileNotFoundException {
		Map<String, ForeignKey> keys = new HashMap<>();
		Scanner scanner = new Scanner(f);
		while(scanner.hasNextLine()){
			String line = scanner.nextLine();
			if(line.contains("Foreign Key Name:")){
				ForeignKey FK = new ForeignKey();
				FK.setName(line.split(":")[1].trim());

				line = scanner.nextLine();
				if(!line.contains("Foreign key Field:"))
					System.err.println("Someting wong (fkf)");
				String referencingField = line.split(":")[1].trim();

				line = scanner.nextLine();
				if(!line.contains("Related Entity:"))
					System.err.println("Someting wong (rel E) " + entityName);
				FK.setRelatedEntity(line.split(":")[1].trim());

				line = scanner.nextLine();
				if(!line.contains("referenced Field:"))
					System.err.println("Someting wong (ref f)");
				FK.setReferencedField(line.split(":")[1].trim());

				line = scanner.nextLine();
				if(!line.contains("isMany:"))
					System.err.println("someting wong (many)");
				String many = line.split(":")[1].trim();
				boolean isMany;
				switch (many){
					case "one":
						isMany = false;
						break;
					case "many":
						isMany = true;
						break;
					case "one-nofk":
						isMany = false;
						break;
					default:
						System.err.println("someting wong (more than many and one?)" + many);
						isMany = false;
				}
				FK.setMany(isMany);

				if(FK.getReferencedField().equals(""))
					FK.setReferencedField(referencingField);

				keys.put(referencingField, FK);
			}
		}

		scanner.close();

		return keys;
	}

	private Map<String, List<ForeignKey>> generateKeysTo(String entityName, File f) throws FileNotFoundException {
		Map<String, List<ForeignKey>> keys = new HashMap<>();
		Scanner scanner = new Scanner(f);
		while(scanner.hasNextLine()){
			String line = scanner.nextLine();
			if(line.contains("Foreign Key Name:")){
				ForeignKey FK = new ForeignKey();
				FK.setName(line.split(":")[1].trim());

				line = scanner.nextLine();
				if(!line.contains("Foreign key Field:"))
					System.err.println("Someting wong (fkf)");
				String referencingField = line.split(":")[1].trim();

				line = scanner.nextLine();
				if(!line.contains("Related Entity:"))
					System.err.println("Someting wong (rel E) " + entityName);
				FK.setRelatedEntity(line.split(":")[1].trim());

				line = scanner.nextLine();
				if(!line.contains("referenced Field:"))
					System.err.println("Someting wong (ref f)");
				FK.setReferencedField(line.split(":")[1].trim());

				line = scanner.nextLine();
				if(!line.contains("isMany:"))
					System.err.println("someting wong (many)");
				String many = line.split(":")[1].trim();
				boolean isMany;
				switch (many){
					case "one":
						isMany = true;
						break;
					case "many":
						isMany = false;
						break;
					case "one-nofk":
						isMany = true;
						break;
					default:
						System.err.println("someting wong (more than many and one?)" + many);
						isMany = false;
				}
				FK.setMany(isMany);

				if(keys.get(referencingField)==null){
					List<ForeignKey> FKs = new LinkedList<>();
					FKs.add(FK);
					keys.put(referencingField, FKs);
				}else{
					keys.get(referencingField).add(FK);
				}

			}
		}

		scanner.close();

		return keys;
	}

	private void pushRequiredImport(String entityName, String referencedEntity){
		//exclude parent imports
		if(entityName.equals(referencedEntity))
			return;
		String folderName = getFolderName(entityName);
		String referencedFolder = getFolderName(referencedEntity);

		Map<String, Map<String, String>> entitiesImports = importBuffer.get(folderName);
		if(entitiesImports==null){
			Map<String, String> refs = new HashMap<>();
			refs.put(referencedEntity, referencedFolder);
			entitiesImports = new HashMap<>();
			entitiesImports.put(entityName, refs);
			importBuffer.put(folderName, entitiesImports);
		}else{
			Map<String, String> refs = entitiesImports.get(entityName);
			if(refs==null){
				refs = new HashMap<>();
				refs.put(referencedEntity, referencedFolder);
				entitiesImports.put(entityName, refs);
			}else{
				if(!refs.containsKey(referencedEntity))
					refs.put(referencedEntity, referencedFolder);
			}

		}
	}


	private Map<String, Map<String, String>> scanXMLsForEntityFields(File f) throws IOException {

		Map<String, Map<String, String>> returnVal= new HashMap<>();


		Scanner scanner = new Scanner(Paths.get(f.getAbsolutePath()), ENCODING.name());

		String currentEntity = "";
		Map<String, String> attributes = new HashMap<>();
		while(scanner.hasNextLine()){

			String line = scanner.nextLine();

			if (line.contains("<view-entity entity-name=\"")) {
				while (!line.contains("</view-entity>")) {
					line = scanner.nextLine();
				}
			}

			if (line.contains("<entity entity-name=\"")) {
				String[] splittedLine = line.split("\"");
				currentEntity = splittedLine[1];
			}

			if(line.contains("</entity>")){
				returnVal.put(currentEntity, attributes);
				attributes = new HashMap<>();
			}

			if(line.contains("<field name=\"")){
				String[] splittedLine = line.split("\"");
				attributes.put(splittedLine[1], convertTypeToGraphQLType(splittedLine[3]));
			}




		}

		scanner.close();



		return returnVal;

	}

	private String convertTypeToGraphQLType(String type){
		GenerateClass converter = new GenerateClass("");

		String returnVal;

		switch (converter.convertEntityTypeToJava(type)){
			case "String":
				returnVal = "GraphQLString";
				break;
			case "boolean":
				returnVal = "GraphQLBoolean";
				break;
			case "Timestamp":
				returnVal = "GraphQLString";
				break;
			case "long":
				returnVal = "GraphQLInt";
				break;
			case "BigDecimal":
				returnVal = "GraphQLFloat";
				break;
			case "java.nio.ByteBuffer":
				returnVal = "GraphQLString/*this was a byteBuffer TODO find a solution*/";
				break;
			case "Object":
				returnVal = "GraphQLString/*this was an Object TODO find a solution*/";
				break;
			case "byte[]":
				returnVal = "GraphQLString/*this was a byte Array TODO find a solution*/";
				break;

			default:
				returnVal = "GraphQLString/*this was a type I didn't find TODO find a solution*/";

		}

		return returnVal;
	}
}
