package com.skytala.eCommerce.control;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.google.common.base.Splitter;
import com.skytala.eCommerce.command.AddDocumentAttribute;
import com.skytala.eCommerce.command.DeleteDocumentAttribute;
import com.skytala.eCommerce.command.UpdateDocumentAttribute;
import com.skytala.eCommerce.entity.DocumentAttribute;
import com.skytala.eCommerce.entity.DocumentAttributeMapper;
import com.skytala.eCommerce.event.DocumentAttributeAdded;
import com.skytala.eCommerce.event.DocumentAttributeDeleted;
import com.skytala.eCommerce.event.DocumentAttributeFound;
import com.skytala.eCommerce.event.DocumentAttributeUpdated;
import com.skytala.eCommerce.query.FindDocumentAttributesBy;

@RestController
@RequestMapping("/api/documentAttribute")
public class DocumentAttributeController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<DocumentAttribute>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public DocumentAttributeController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a DocumentAttribute
	 * @return a List with the DocumentAttributes
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<DocumentAttribute> findDocumentAttributesBy(@RequestParam Map<String, String> allRequestParams) {

		FindDocumentAttributesBy query = new FindDocumentAttributesBy(allRequestParams);

		int usedTicketId;

		synchronized (DocumentAttributeController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(DocumentAttributeFound.class,
				event -> sendDocumentAttributesFoundMessage(((DocumentAttributeFound) event).getDocumentAttributes(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendDocumentAttributesFoundMessage(List<DocumentAttribute> documentAttributes, int usedTicketId) {
		queryReturnVal.put(usedTicketId, documentAttributes);
	}

	/**
	 * 
	 * this method will only be called by Springs DispatcherServlet
	 * 
	 * @param request
	 *            HttpServletRequest
	 * @return true on success; false on fail
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/add", consumes = "application/x-www-form-urlencoded")
	public boolean createDocumentAttribute(HttpServletRequest request) {

		DocumentAttribute documentAttributeToBeAdded = new DocumentAttribute();
		try {
			documentAttributeToBeAdded = DocumentAttributeMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createDocumentAttribute(documentAttributeToBeAdded);

	}

	/**
	 * creates a new DocumentAttribute entry in the ofbiz database
	 * 
	 * @param documentAttributeToBeAdded
	 *            the DocumentAttribute thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createDocumentAttribute(DocumentAttribute documentAttributeToBeAdded) {

		AddDocumentAttribute com = new AddDocumentAttribute(documentAttributeToBeAdded);
		int usedTicketId;

		synchronized (DocumentAttributeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(DocumentAttributeAdded.class,
				event -> sendDocumentAttributeChangedMessage(((DocumentAttributeAdded) event).isSuccess(), usedTicketId));

		try {
			Scheduler.instance().schedule(com).executeNext();
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}
		while (!commandReturnVal.containsKey(usedTicketId)) {
		}

		return commandReturnVal.remove(usedTicketId);

	}

	/**
	 * this method will only be called by Springs DispatcherServlet
	 * 
	 * @param request HttpServletRequest object
	 * @return true on success, false on fail
	 */
	@RequestMapping(method = RequestMethod.PUT, value = "/update", consumes = "application/x-www-form-urlencoded")
	public boolean updateDocumentAttribute(HttpServletRequest request) {

		BufferedReader br;
		String data = null;
		Map<String, String> dataMap = null;

		try {
			br = new BufferedReader(new InputStreamReader(request.getInputStream()));
			if (br != null) {
				data = java.net.URLDecoder.decode(br.readLine(), "UTF-8");
			}
		} catch (IOException e1) {
			e1.printStackTrace();
			return false;
		}

		dataMap = Splitter.on('&').trimResults().withKeyValueSeparator(Splitter.on('=').limit(2).trimResults())
				.split(data);

		DocumentAttribute documentAttributeToBeUpdated = new DocumentAttribute();

		try {
			documentAttributeToBeUpdated = DocumentAttributeMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateDocumentAttribute(documentAttributeToBeUpdated);

	}

	/**
	 * Updates the DocumentAttribute with the specific Id
	 * 
	 * @param documentAttributeToBeUpdated the DocumentAttribute thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateDocumentAttribute(DocumentAttribute documentAttributeToBeUpdated) {

		UpdateDocumentAttribute com = new UpdateDocumentAttribute(documentAttributeToBeUpdated);

		int usedTicketId;

		synchronized (DocumentAttributeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(DocumentAttributeUpdated.class,
				event -> sendDocumentAttributeChangedMessage(((DocumentAttributeUpdated) event).isSuccess(), usedTicketId));

		try {
			Scheduler.instance().schedule(com).executeNext();
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}
		while (!commandReturnVal.containsKey(usedTicketId)) {
		}

		return commandReturnVal.remove(usedTicketId);
	}

	/**
	 * removes a DocumentAttribute from the database
	 * 
	 * @param documentAttributeId:
	 *            the id of the DocumentAttribute thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deletedocumentAttributeById(@RequestParam(value = "documentAttributeId") String documentAttributeId) {

		DeleteDocumentAttribute com = new DeleteDocumentAttribute(documentAttributeId);

		int usedTicketId;

		synchronized (DocumentAttributeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(DocumentAttributeDeleted.class,
				event -> sendDocumentAttributeChangedMessage(((DocumentAttributeDeleted) event).isSuccess(), usedTicketId));

		try {
			Scheduler.instance().schedule(com).executeNext();
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}
		while (!commandReturnVal.containsKey(usedTicketId)) {
		}

		return commandReturnVal.remove(usedTicketId);
	}

	public void sendDocumentAttributeChangedMessage(boolean success, int usedTicketId) {
		commandReturnVal.put(usedTicketId, success);
	}

	@RequestMapping(value = (" * "))
	public String returnErrorPage(HttpServletRequest request) {

		String usedUri = request.getRequestURI();
		String[] splittedString = usedUri.split("/");

		String usedRequest = splittedString[splittedString.length - 1];

		if (validRequests.containsKey(usedRequest)) {
			return "Error: request method " + request.getMethod() + " not allowed for \"" + usedUri + "\"!\n"
					+ "Please use " + validRequests.get(usedRequest) + "!";

		}

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/documentAttribute/\" plus one of the following: "
				+ "";

		Set<String> keySet = validRequests.keySet();
		Iterator<String> it = keySet.iterator();

		while (it.hasNext()) {
			returnVal += "\"" + it.next() + "\"";
			if (it.hasNext())
				returnVal += ", ";
		}

		returnVal += "!";

		return returnVal;

	}
}
