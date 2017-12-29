package parser;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.Scanner;

public class TestClassGenerator {

	private String entityName;
	private String entityNamelc;
	private String entityNameCaps;
	private String foreignKey;
	private String foreignKeyCaps;
	private String nonKeyAttribute;
	private String nonKeyAttributelc;
	private String nonKeyAttributeCaps;
	private String writeTo = System.getProperty("user.dir")+ "/parsed_files/";
	private String pathToEntity = System.getProperty("user.dir")+"/parsed_files/entity/";
	
	public TestClassGenerator() {
		
	
	}
	
	
	public void doIt() throws Exception {
		Parser parser = new Parser();
		Iterator<String> it = parser.readConfig().get("Product").iterator();
//		System.out.println(writeTo);
		while(it.hasNext()) {
			entityName = it.next();
			entityNamelc = parser.firstToLowerCase(entityName);
			entityNameCaps = entityName.toUpperCase();
			foreignKey = getFroeignKey(entityName);
			nonKeyAttribute = "test"; //getNonKAttribute(entityName);
			if(foreignKey==null||nonKeyAttribute==null) {
				continue;
			}
			foreignKeyCaps = foreignKey.toUpperCase();
			foreignKey = parser.firstToUpperCase(foreignKey);
			nonKeyAttributeCaps = nonKeyAttribute.toUpperCase();
			nonKeyAttributelc = nonKeyAttribute;
			nonKeyAttribute = parser.firstToUpperCase(nonKeyAttribute);
			
			
			File f = new File(writeTo+"tests/");
			f.mkdirs();
			f = new File(writeTo+"tests/"+entityName+"ControllerTest.java");
			
			PrintWriter writer = new PrintWriter(f);
		
			String clazz = "";
			clazz += generateTestClassHeader() 
					+ generateCreateTests()
					+ generateUpdateTests()
					+ generateGetTests()
					+ generateDeleteTests() + "}";
			writer.println(clazz);
			writer.close();
		}
	}
	
	private String generateTestClassHeader() {
		String retVal = "";
		
		retVal += "package com.skytala.eCommerce.domain."+entityNamelc+".test.controller;\n" + 
				"\n" + 
				"import static org.hamcrest.Matchers.containsString;\n" + 
				"import static org.hamcrest.Matchers.hasSize;\n" + 
				"import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;\n" + 
				"import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;\n" + 
				"import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;\n" + 
				"import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;\n" + 
				"import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;\n" + 
				"import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;\n" + 
				"\n" + 
				"import java.util.List;\n" + 
				"import org.apache.ofbiz.entity.Delegator;\n" + 
				"import org.apache.ofbiz.entity.DelegatorFactory;\n" + 
				"import org.apache.ofbiz.entity.GenericEntityException;\n" + 
				"import org.junit.Before;\n" + 
				"import org.junit.Test;\n" + 
				"import org.junit.runner.RunWith;\n" + 
				"import org.mockito.MockitoAnnotations;\n" + 
				"import org.springframework.http.MediaType;\n" + 
				"import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;\n" + 
				"import org.springframework.test.annotation.Rollback;\n" + 
				"import org.springframework.test.context.ContextConfiguration;\n" + 
				"import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;\n" + 
				"import org.springframework.test.context.web.WebAppConfiguration;\n" + 
				"import org.springframework.test.web.servlet.MockMvc;\n" + 
				"import org.springframework.test.web.servlet.setup.MockMvcBuilders;\n" + 
				"import org.springframework.transaction.annotation.Transactional;\n" + 
				"\n" + 
				"import com.fasterxml.jackson.core.JsonProcessingException;\n" + 
				"import com.fasterxml.jackson.databind.ObjectMapper;\n" + 
				"import com.jayway.jsonpath.JsonPath;\n" + 
				"import com.skytala.eCommerce.config.WebAppConfig;\n" + 
				"import com.skytala.eCommerce.domain."+entityNamelc+"."+entityName+"Controller;\n" + 
				"import com.skytala.eCommerce.domain."+entityNamelc+".model."+entityName+";\n" + 
				"\n" + 
				"import junit.framework.TestCase;\n" + 
				"\n" + 
				"@RunWith(SpringJUnit4ClassRunner.class)\n" + 
				"@WebAppConfiguration\n" + 
				"@ContextConfiguration(classes = { WebAppConfig.class })\n" + 
				"public class "+entityName+"ControllerTest extends TestCase {\n" + 
				"\n" + 
				"	// "+entityName+" Attributes\n" + 
				"	private static final String DEFAULT_"+entityNameCaps+"_ID = \"AAAAAAAAAA\";\n" + 
				"	private static final String EXISTING_"+entityNameCaps+"_ID_NON_DELETABLE = \"EXISTING_ID\";\n" + 
				"	private static final String EXISTING_"+entityNameCaps+"_ID_DELETABLE = \"EXISTING_ID_DELABLE\";\n" + 
				"	private static final String NON_EXISTING_"+entityNameCaps+"_ID = \"NON_EXISTING_XYZAABC\";\n" + 
				"\n" +
				"	private static final String DEFAULT_"+nonKeyAttributeCaps+" = \"AAAAAAAAAA\";\n" + 
				"	private static final String UPDATED_"+nonKeyAttributeCaps+" = \"BBBBBBBBBB\";\n" + 
				"	private static final String EXISTING_"+nonKeyAttributeCaps+" = \"EXISTING_"+nonKeyAttributeCaps+"\";\n" + 
				"\n" +
				"	private static final String "+foreignKeyCaps+" = \"NON_EXISTING_XYZAABBC\";\n" + 
				"\n" + 
				"	ObjectMapper objectMapper;\n" + 
				"\n" + 
				"	MockMvc mockMvc;\n" + 
				"\n" + 
				"	@Override\n" + 
				"	@Before\n" + 
				"	public void setUp() throws Exception {\n" + 
				"\n" + 
				"		objectMapper = new MappingJackson2HttpMessageConverter().getObjectMapper();\n" + 
				"		MockitoAnnotations.initMocks(this);\n" + 
				"		this.mockMvc = MockMvcBuilders.standaloneSetup(new "+entityName+"Controller()).build();\n" + 
				"\n" + 
				"		Delegator delegator = DelegatorFactory.getDelegator(\"test\");\n" + 
				"\n" + 
				"		"+entityName+" "+entityNamelc+" = new "+entityName+"();\n" + 
				"		"+entityNamelc+".set"+entityName+"Id(EXISTING_"+entityNameCaps+"_ID_NON_DELETABLE);\n" + 
				"		"+entityNamelc+".set"+nonKeyAttribute+"(EXISTING_"+nonKeyAttributeCaps+");\n" + 
				"//		"+entityName+".setAutoCreateKeywords(true);\n" + 
				"\n" + 
				"		try {\n" + 
				"			delegator.createOrStore(delegator.makeValue(\""+entityName+"\", "+entityNamelc+".mapAttributeField()));\n" + 
				"		} catch (GenericEntityException e) {\n" + 
				"			e.printStackTrace();\n" + 
				"		}\n" + 
				"\n" + 
				"		"+entityNamelc+" = new "+entityName+"();\n" + 
				"		"+entityNamelc+".set"+entityName+"Id(EXISTING_"+entityNameCaps+"_ID_DELETABLE);\n" + 
				"		"+entityNamelc+".set"+nonKeyAttribute+"(\"test"+entityName+"Del\");\n" + 
				"//		"+entityNamelc+".setAutoCreateKeywords(false);\n" + 
				"\n" + 
				"		try {\n" + 
				"			delegator.createOrStore(delegator.makeValue(\""+entityName+"\", "+entityNamelc+".mapAttributeField()));\n" + 
				"		} catch (GenericEntityException e) {\n" + 
				"			e.printStackTrace();\n" + 
				"		}\n" + 
				"\n" + 
				"		super.setUp();\n" + 
				"	}\n" + 
				"\n" + 
				"";
		
		return retVal;
	}

	private String generateGetTests() {
		String retVal = "";
		
		retVal += "	/**\n" + 
				"	 * get test\n" + 
				"	 *\n" + 
				"	 * TestCase 1: get existing "+entityName+"\n" + 
				"	 * \n" + 
				"	 * Given data: ID is used\n" + 
				"	 * \n" + 
				"	 * Expected behavior: Gets data of "+entityName+" with given ID from DB\n" + 
				"	 * \n" + 
				"	 * Expected status code: 200 OK\n" + 
				"	 * \n" + 
				"	 * @throws Exception\n" + 
				"	 * \n" + 
				"	 */\n" + 
				"	@Test\n" + 
				"	public void testGetExisting"+entityName+"() throws Exception {\n" + 
				"		mockMvc.perform(get(\"/"+entityNamelc+"s/\" + EXISTING_"+entityNameCaps+"_ID_NON_DELETABLE)).andExpect(status().isOk())\n" + 
				"				.andExpect(jsonPath(\"$."+entityNamelc+"Id\", containsString(EXISTING_"+entityNameCaps+"_ID_NON_DELETABLE)));\n" + 
				"\n" + 
				"	}\n" + 
				"\n" + 
				"	/**\n" + 
				"	 * get test\n" + 
				"	 * \n" + 
				"	 * TestCase 2: get non-existing "+entityName+"\n" + 
				"	 * \n" + 
				"	 * Given data: ID not used\n" + 
				"	 * \n" + 
				"	 * Expected behavior: No data gotten from DB\n" + 
				"	 * \n" + 
				"	 * Expected status code: 404 NOT_FOUND\n" + 
				"	 * \n" + 
				"	 * @throws Exception\n" + 
				"	 */\n" + 
				"	@Test\n" + 
				"	public void testGetNonExisting"+entityName+"() throws Exception {\n" + 
				"		mockMvc.perform(get(\"/"+entityNamelc+"s/\" + NON_EXISTING_"+entityNameCaps+"_ID)).andExpect(status().isNotFound());\n" + 
				"\n" + 
				"	}\n\n";
		
		
		return retVal;
	}

	private String generateCreateTests() {
		String retVal = "";
		
		retVal += "	/**\n" + 
				"	 * Create test\n" + 
				"	 *\n" + 
				"	 * TestCase 1: create "+entityName+" that doesn't exist\n" + 
				"	 * \n" + 
				"	 * Given data: ID is not used in DB, data is valid (no FK Constraints are hurt)\n" + 
				"	 * \n" + 
				"	 * Expected behavior: "+entityName+" created, given ID overwritten, new ID\n" + 
				"	 * automatically generated\n" + 
				"	 * \n" + 
				"	 * Expected status code: 201 CREATED\n" + 
				"	 * \n" + 
				"	 * \n" + 
				"	 */\n" + 
				"	@Test\n" + 
				"	@Transactional\n" + 
				"	@Rollback\n" + 
				"	public void testCreate"+entityName+"ThatDoesntExist() throws JsonProcessingException, Exception {\n" + 
				"\n" + 
				"		"+entityName+" "+entityNamelc+" = new "+entityName+"();\n" + 
				"		"+entityNamelc+".set"+entityName+"Id(DEFAULT_"+entityNameCaps+"_ID);\n" + 
				"		"+entityNamelc+".set"+nonKeyAttribute+"(DEFAULT_"+nonKeyAttributeCaps+");\n" + 
				"\n" + 
				"		String contentExpected = mockMvc\n" + 
				"				.perform(post(\"/"+entityNamelc+"s/add\").contentType(MediaType.APPLICATION_JSON_UTF8)\n" + 
				"						.content(objectMapper.writeValueAsString("+entityNamelc+")))\n" + 
				"				.andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();\n" + 
				"\n" + 
				"		"+entityName+" created"+entityName+" = objectMapper.readValue(contentExpected, "+entityName+".class);\n" + 
				"\n" + 
				"		String contentIs = mockMvc.perform(get(\"/"+entityNamelc+"s/\" + created"+entityName+".get"+entityName+"Id())).andExpect(status().isOk())\n" + 
				"				.andReturn().getResponse().getContentAsString();\n" + 
				"		\n" + 
				"		assertEquals(contentIs, contentExpected);\n" + 
				"		\n" + 
				"	}\n" + 
				"\n" + 
				"	/**\n" + 
				"	 * Create test\n" + 
				"	 * \n" + 
				"	 * TestCase 2: create "+entityName+" that exists\n" + 
				"	 * \n" + 
				"	 * Given data: ID is used in DB, data is valid (no FK Constraints are hurt)\n" + 
				"	 * \n" + 
				"	 * Expected behavior: "+entityName+" created, given ID overwritten, new ID\n" + 
				"	 * automatically generated (NO UPDATE!)\n" + 
				"	 * \n" + 
				"	 * Expected status code: 201 CREATED\n" + 
				"	 * \n" + 
				"	 * \n" + 
				"	 * @throws JsonProcessingException\n" + 
				"	 * @throws Exception\n" + 
				"	 */\n" + 
				"	@Test\n" + 
				"	public void testCreate"+entityName+"ThatExists() throws JsonProcessingException, Exception {\n" + 
				"\n" + 
				"		"+entityName+" "+entityNamelc+" = new "+entityName+"();\n" + 
				"		"+entityNamelc+".set"+entityName+"Id(EXISTING_"+entityNameCaps+"_ID_DELETABLE);\n" + 
				"		"+entityNamelc+".set"+nonKeyAttribute+"(DEFAULT_"+nonKeyAttributeCaps+");\n" + 
				"\n" + 
				"		mockMvc.perform(post(\"/"+entityNamelc+"s/add\").contentType(MediaType.APPLICATION_JSON_UTF8)\n" + 
				"				.content(objectMapper.writeValueAsString("+entityNamelc+"))).andExpect(status().isCreated());\n" + 
				"\n" + 
				"		mockMvc.perform(get(\"/"+entityNamelc+"s/\" + DEFAULT_"+entityNameCaps+"_ID)).andExpect(status().isNotFound());\n" + 
				"			\n" + 
				"	}\n" + 
				"\n" + 
				"	/**\n" + 
				"	 * create test\n" + 
				"	 * \n" + 
				"	 * TestCase 3: create "+entityName+" with invalid data\n" + 
				"	 * \n" + 
				"	 * Given data: Data is invalid (at least FK Constraint is hurt)\n" + 
				"	 * \n" + 
				"	 * Expected behavior: "+entityName+" not created\n" + 
				"	 * \n" + 
				"	 * Expected status code: 409 CONFLICT\n" + 
				"	 * \n" + 
				"	 * @throws Exception\n" + 
				"	 * @throws JsonProcessingException\n" + 
				"	 * \n" + 
				"	 * \n" + 
				"	 */\n" + 
				"	@Test\n" + 
				"	public void testCreate"+entityName+"WithInvalidData() throws JsonProcessingException, Exception {\n" + 
				"\n" + 
				"		"+entityName+" "+entityNamelc+" = new "+entityName+"();\n" + 
				"		"+entityNamelc+".set"+entityName+"Id(DEFAULT_"+entityNameCaps+"_ID);\n" + 
				"		"+entityNamelc+".set"+foreignKey+"("+foreignKeyCaps+");\n" + 
				"\n" + 
				"		mockMvc.perform(post(\"/"+entityNamelc+"s/add\").contentType(MediaType.APPLICATION_JSON_UTF8)\n" + 
				"				.content(objectMapper.writeValueAsString("+entityNamelc+"))).andExpect(status().isConflict());\n" + 
				"\n" + 
				"		mockMvc.perform(get(\"/"+entityNamelc+"s/\" + DEFAULT_"+entityNameCaps+"_ID)).andExpect(status().isNotFound());\n" + 
				"\n" + 
				"	}\n" + 
				"";
		
		
		return retVal;
	}

	private String generateUpdateTests() {
		String retVal = "";
		
		retVal += "	/**\n" + 
				"	 * update test\n" + 
				"	 * \n" + 
				"	 * TestCase 1: update existing "+entityName+" with valid data\n" + 
				"	 * \n" + 
				"	 * Given data: ID is used, data is valid\n" + 
				"	 * \n" + 
				"	 * Expected behavior: "+entityName+" updated\n" + 
				"	 * \n" + 
				"	 * Expected status code: 204 NO_CONTENT\n" + 
				"	 * \n" + 
				"	 * @throws Exception\n" + 
				"	 * @throws JsonProcessingException\n" + 
				"	 * \n" + 
				"	 */\n" + 
				"	@Test\n" + 
				"	public void testUpdateExisting"+entityName+"WithValidData() throws JsonProcessingException, Exception {\n" + 
				"\n" + 
				"		"+entityName+" "+entityNamelc+" = new "+entityName+"();\n" + 
				"		"+entityNamelc+".set"+entityName+"Id(EXISTING_"+entityNameCaps+"_ID_NON_DELETABLE);\n" + 
				"		"+entityNamelc+".set"+nonKeyAttribute+"(UPDATED_"+nonKeyAttributeCaps+");\n" + 
				"\n" + 
				"		mockMvc.perform(put(\"/"+entityNamelc+"s/\" + EXISTING_"+entityNameCaps+"_ID_NON_DELETABLE)\n" + 
				"				.contentType(MediaType.APPLICATION_JSON_UTF8).content(objectMapper.writeValueAsString("+entityNamelc+")))\n" + 
				"				.andExpect(status().isNoContent());\n" + 
				"\n" + 
				"		mockMvc.perform(get(\"/"+entityNamelc+"s/\" + EXISTING_"+entityNameCaps+"_ID_NON_DELETABLE)).andExpect(status().isOk())\n" + 
				"				.andExpect(jsonPath(\"$."+nonKeyAttributelc+"\").value(UPDATED_"+nonKeyAttributeCaps+"));\n" + 
				"\n" + 
				"	}\n" + 
				"\n" + 
				"	/**\n" + 
				"	 * update test\n" + 
				"	 * \n" + 
				"	 * TestCase 2: update non-existing "+entityName+"\n" + 
				"	 * \n" + 
				"	 * Given data: ID is not used\n" + 
				"	 * \n" + 
				"	 * Expected behavior: No "+entityName+" updated or created\n" + 
				"	 * \n" + 
				"	 * Expected status code: 404 NOT_FOUND\n" + 
				"	 * \n" + 
				"	 * @throws Exception\n" + 
				"	 * @throws JsonProcessingException\n" + 
				"	 * \n" + 
				"	 */\n" + 
				"	@Test\n" + 
				"	public void testUpdateNonExisting"+entityName+"WithValidData() throws JsonProcessingException, Exception {\n" + 
				"		"+entityName+" "+entityNamelc+" = new "+entityName+"();\n" + 
				"		"+entityNamelc+".set"+entityName+"Id(NON_EXISTING_"+entityNameCaps+"_ID);\n" + 
				"		"+entityNamelc+".set"+nonKeyAttribute+"(UPDATED_"+nonKeyAttributeCaps+");\n" + 
				"\n" + 
				"		mockMvc.perform(put(\"/"+entityNamelc+"s/\" + NON_EXISTING_"+entityNameCaps+"_ID).contentType(MediaType.APPLICATION_JSON_UTF8)\n" + 
				"				.content(objectMapper.writeValueAsString("+entityNamelc+"))).andExpect(status().isNotFound());\n" + 
				"\n" + 
				"		mockMvc.perform(get(\"/"+entityNamelc+"s/\" + NON_EXISTING_"+entityNameCaps+"_ID)).andExpect(status().isNotFound());\n" + 
				"\n" + 
				"	}\n" + 
				"\n" + 
				"	/**\n" + 
				"	 * update test\n" + 
				"	 * \n" + 
				"	 * TestCase 3: update existing "+entityName+" with invalid data\n" + 
				"	 * \n" + 
				"	 * Given data: ID is used, Data is invalid (at least FK Constraint is hurt)\n" + 
				"	 * \n" + 
				"	 * Expected behavior: "+entityName+" not updated\n" + 
				"	 * \n" + 
				"	 * Expected status code: 409 CONFLICT\n" + 
				"	 * \n" + 
				"	 * @throws Exception\n" + 
				"	 * @throws JsonProcessingException\n" + 
				"	 * \n" + 
				"	 */\n" + 
				"	@Test\n" + 
				"	public void testUpdateExisting"+entityName+"WithInvalidData() throws JsonProcessingException, Exception {\n" + 
				"		"+entityName+" "+entityNamelc+" = new "+entityName+"();\n" + 
				"		"+entityNamelc+".set"+entityName+"Id(EXISTING_"+entityNameCaps+"_ID_NON_DELETABLE);\n" + 
				"		"+entityNamelc+".set"+nonKeyAttribute+"(UPDATED_"+nonKeyAttributeCaps+");\n" + 
				"		"+entityNamelc+".set"+foreignKey+"("+foreignKeyCaps+");\n" + 
				"\n" + 
				"		mockMvc.perform(put(\"/"+entityNamelc+"s/\" + EXISTING_"+entityNameCaps+"_ID_NON_DELETABLE)\n" + 
				"				.contentType(MediaType.APPLICATION_JSON_UTF8).content(objectMapper.writeValueAsString("+entityNamelc+")))\n" + 
				"				.andExpect(status().isConflict());\n" + 
				"\n" + 
				"		mockMvc.perform(get(\"/"+entityNamelc+"s/\" + EXISTING_"+entityNameCaps+"_ID_NON_DELETABLE)).andExpect(status().isOk())\n" + 
				"				.andExpect(jsonPath(\"$."+nonKeyAttributelc+"\").value(EXISTING_"+nonKeyAttributeCaps+"));\n" + 
				"	}\n" + 
				"\n" + 
				"	/**\n" + 
				"	 * update test\n" + 
				"	 * \n" + 
				"	 * TestCase 4: update non-existing "+entityName+" with invalid data\n" + 
				"	 * \n" + 
				"	 * Given data: ID is not used, Data is invalid (at least one FK Constraint is\n" + 
				"	 * hurt)\n" + 
				"	 * \n" + 
				"	 * Expected behavior: No "+entityName+" updated or created\n" + 
				"	 * \n" + 
				"	 * Expected status code: 404 NOT_FOUND\n" + 
				"	 * \n" + 
				"	 * @throws Exception\n" + 
				"	 * @throws JsonProcessingException\n" + 
				"	 * \n" + 
				"	 */\n" + 
				"	@Test\n" + 
				"	public void testUpdateNonExisting"+entityName+"WithInvalidData() throws JsonProcessingException, Exception {\n" + 
				"		"+entityName+" "+entityNamelc+" = new "+entityName+"();\n" + 
				"		"+entityNamelc+".set"+entityName+"Id(NON_EXISTING_"+entityNameCaps+"_ID);\n" + 
				"		"+entityNamelc+".set"+nonKeyAttribute+"(UPDATED_"+nonKeyAttributeCaps+");\n" + 
				"		"+entityNamelc+".set"+foreignKey+"("+foreignKeyCaps+");\n" + 
				"\n" + 
				"		mockMvc.perform(put(\"/"+entityNamelc+"s/\" + NON_EXISTING_"+entityNameCaps+"_ID).contentType(MediaType.APPLICATION_JSON_UTF8)\n" + 
				"				.content(objectMapper.writeValueAsString("+entityNamelc+"))).andExpect(status().isNotFound());\n" + 
				"\n" + 
				"		mockMvc.perform(get(\"/"+entityNamelc+"s/\" + NON_EXISTING_"+entityNameCaps+"_ID)).andExpect(status().isNotFound());\n" + 
				"\n" + 
				"	}\n" + 
				"";
		
		return retVal;
	}

	private String generateDeleteTests() {
		String retVal = "	/**\n" + 
				"	 * delete test\n" + 
				"	 * \n" + 
				"	 * TestCase 1: delete existing deletable "+entityName+"\n" + 
				"	 * \n" + 
				"	 * Given data: ID is used, tuple deletable\n" + 
				"	 * \n" + 
				"	 * Expected behavior: tuple deleted\n" + 
				"	 * \n" + 
				"	 * Expected status code: 204 NO_CONTENT\n" + 
				"	 * \n" + 
				"	 * @throws Exception\n" + 
				"	 * \n" + 
				"	 */\n" + 
				"	@Test\n" + 
				"	public void testDeleteExistingDeletable"+entityName+"() throws Exception {\n" + 
				"		String result = mockMvc.perform(delete(\"/"+entityNamelc+"s/\" + EXISTING_"+entityNameCaps+"_ID_DELETABLE))\n" + 
				"				.andExpect(status().isNoContent()).andReturn().getResponse().getContentAsString();\n" + 
				"\n" + 
				"		assertEquals(result, \"\");\n" + 
				"\n" + 
				"		mockMvc.perform(get(\"/"+entityNamelc+"s/\" + EXISTING_"+entityNameCaps+"_ID_DELETABLE)).andExpect(status().isNotFound());\n" + 
				"\n" + 
				"	}\n" + 
				"\n" + 
				"	/**\n" + 
				"	 * delete test\n" + 
				"	 * \n" + 
				"	 * TestCase 2: delete existing non-deletable "+entityName+"\n" + 
				"	 * \n" + 
				"	 * Given data: ID is used, tuple not deletable (FK Constraint)\n" + 
				"	 * \n" + 
				"	 * Expected behavior: tuple not deleted\n" + 
				"	 * \n" + 
				"	 * Expected status code: 409 CONFLICT\n" + 
				"	 * \n" + 
				"	 * @throws Exception\n" + 
				"	 * \n" + 
				"	 */\n" + 
				"	@Test\n" + 
				"	public void testDeleteExistingNonDeletable"+entityName+"() throws Exception {\n" + 
				"\n" + 
				"		mockMvc.perform(delete(\"/"+entityNamelc+"s/\" + EXISTING_"+entityNameCaps+"_ID_NON_DELETABLE)).andExpect(status().isConflict())\n" + 
				"				.andExpect(jsonPath(\"$\").value(\""+entityName+" could not be deleted\"));\n" + 
				"\n" + 
				"		mockMvc.perform(get(\"/"+entityNamelc+"s/\" + EXISTING_"+entityNameCaps+"_ID_NON_DELETABLE)).andExpect(status().isOk())\n" + 
				"				.andExpect(jsonPath(\"$."+entityNamelc+"Id\").value(EXISTING_"+entityNameCaps+"_ID_NON_DELETABLE));\n" + 
				"\n" + 
				"	}\n" + 
				"\n" + 
				"	/**\n" + 
				"	 * delete test\n" + 
				"	 * \n" + 
				"	 * TestCase 3: delete non-existing "+entityName+"\n" + 
				"	 * \n" + 
				"	 * Given data: ID is not used\n" + 
				"	 * \n" + 
				"	 * Expected behavior: no "+entityName+" deleted\n" + 
				"	 * \n" + 
				"	 * Expected status code: 404 NOT_FOUND\n" + 
				"	 * \n" + 
				"	 * @throws Exception\n" + 
				"	 * \n" + 
				"	 * \n" + 
				"	 */\n" + 
				"	@Test\n" + 
				"	public void testDeleteNonExisting"+entityName+"() throws Exception {\n" + 
				"\n" + 
				"		int count"+entityName+"sBeforeDelete = 0;\n" + 
				"\n" + 
				"		String responseStr = mockMvc.perform(get(\"/"+entityNamelc+"s/find\")).andReturn().getResponse().getContentAsString();\n" + 
				"\n" + 
				"		List<?> "+entityNamelc+"s = JsonPath.read(responseStr, \"$\");\n" + 
				"		count"+entityName+"sBeforeDelete = "+entityNamelc+"s.size();\n" + 
				"\n" + 
				"		mockMvc.perform(delete(\"/"+entityNamelc+"s/\" + NON_EXISTING_"+entityNameCaps+"_ID)).andExpect(status().isNotFound());\n" + 
				"\n" + 
				"		mockMvc.perform(get(\"/"+entityNamelc+"s/find\")).andExpect(status().isOk())\n" + 
				"				.andExpect(jsonPath(\"$\", hasSize(count"+entityName+"sBeforeDelete)));\n" + 
				"\n" + 
				"	}\n" + 
				"";
		
		
		
		return retVal;
	}

	
	public String getFroeignKey(String entityName) throws FileNotFoundException {
		
		File fileFKs = new File(writeTo+"FKs/from/"+entityName+".txt");
		File filePKs = new File(writeTo+"PKs/"+entityName+".txt");
		
		Scanner sFK = new Scanner(fileFKs);
		
		while (sFK.hasNextLine()){
			String fLine = sFK.nextLine();
			if(!fLine.contains("Foreign key Field:")) {
				continue;
			}else {
				
				
				String FKfield = fLine.split(":")[1].trim();
				Scanner sPK = new Scanner(filePKs);

				//System.out.println(FKfield);

				while(sPK.hasNextLine()) {
					String pLine = sPK.nextLine();
					if(!pLine.contains("Primary Key Attribute:")) {
						continue;
					}else {
						String PKfield = pLine.split(":")[2].trim();
						if(FKfield.equals(PKfield)) {


						}else {
							
							System.out.println(FKfield);
							sPK.close();
							sFK.close();
							return FKfield;
						}
					}
					

				}
				sPK.close();
				
			}
			
		}
		sFK.close();
		System.err.println("you failed");
		return null;
	}
	
	public String getNonKAttribute(String entityName) throws Exception {

		File fileFKs = new File(writeTo+"FKs/from/"+entityName+".txt");
		File filePKs = new File(writeTo+"PKs/"+entityName+".txt");
		File fileEntity = new File(pathToEntity+entityName+".java");
		
		
		System.out.println(pathToEntity+entityName+".java");
		
		if(!fileEntity.exists()) {
			throw new Exception("No such File!");
		}
		
		boolean isValid = true;
		
		Scanner sEntity = new Scanner(fileEntity);
		while(sEntity.hasNextLine()) {
			String line = sEntity.nextLine();
			if(line.contains("private")&&!line.contains("static")) {
				String type = line.split(" ")[1];
				if(line.split(" ").length<3) {
					System.out.println(entityName);
					continue;
				}
				String name = line.split(" ")[2].replace(";", "").trim();
				
				isValid = true;
				if(!type.equals("String")) {
					isValid = false;
					
				}
				
				//System.out.println(name);
				
				Scanner sPKs = new Scanner(filePKs);
				Scanner sFKs = new Scanner(fileFKs);
				
				while(sPKs.hasNextLine()) {
					if(sPKs.nextLine().contains(name)) {
						isValid = false;
						break;
					}
				}
				while(sFKs.hasNextLine()) {
					if(sFKs.nextLine().contains(name)) {
						isValid = false;
						break;			
					}
				}
				
				if(isValid) {
					sPKs.close();
					sFKs.close();
					sEntity.close();
					System.out.println(name);
					return  name;
				}
				
				
				
				sPKs.close();
				sFKs.close();
			}
			
		}
		
		
		
		
		
		
		sEntity.close();
		
		
		return null;
	}
	
}
