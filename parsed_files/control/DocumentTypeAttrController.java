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
import com.skytala.eCommerce.command.AddDocumentTypeAttr;
import com.skytala.eCommerce.command.DeleteDocumentTypeAttr;
import com.skytala.eCommerce.command.UpdateDocumentTypeAttr;
import com.skytala.eCommerce.entity.DocumentTypeAttr;
import com.skytala.eCommerce.entity.DocumentTypeAttrMapper;
import com.skytala.eCommerce.event.DocumentTypeAttrAdded;
import com.skytala.eCommerce.event.DocumentTypeAttrDeleted;
import com.skytala.eCommerce.event.DocumentTypeAttrFound;
import com.skytala.eCommerce.event.DocumentTypeAttrUpdated;
import com.skytala.eCommerce.query.FindDocumentTypeAttrsBy;

@RestController
@RequestMapping("/api/documentTypeAttr")
public class DocumentTypeAttrController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<DocumentTypeAttr>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public DocumentTypeAttrController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a DocumentTypeAttr
	 * @return a List with the DocumentTypeAttrs
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<DocumentTypeAttr> findDocumentTypeAttrsBy(@RequestParam Map<String, String> allRequestParams) {

		FindDocumentTypeAttrsBy query = new FindDocumentTypeAttrsBy(allRequestParams);

		int usedTicketId;

		synchronized (DocumentTypeAttrController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(DocumentTypeAttrFound.class,
				event -> sendDocumentTypeAttrsFoundMessage(((DocumentTypeAttrFound) event).getDocumentTypeAttrs(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendDocumentTypeAttrsFoundMessage(List<DocumentTypeAttr> documentTypeAttrs, int usedTicketId) {
		queryReturnVal.put(usedTicketId, documentTypeAttrs);
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
	public boolean createDocumentTypeAttr(HttpServletRequest request) {

		DocumentTypeAttr documentTypeAttrToBeAdded = new DocumentTypeAttr();
		try {
			documentTypeAttrToBeAdded = DocumentTypeAttrMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createDocumentTypeAttr(documentTypeAttrToBeAdded);

	}

	/**
	 * creates a new DocumentTypeAttr entry in the ofbiz database
	 * 
	 * @param documentTypeAttrToBeAdded
	 *            the DocumentTypeAttr thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createDocumentTypeAttr(DocumentTypeAttr documentTypeAttrToBeAdded) {

		AddDocumentTypeAttr com = new AddDocumentTypeAttr(documentTypeAttrToBeAdded);
		int usedTicketId;

		synchronized (DocumentTypeAttrController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(DocumentTypeAttrAdded.class,
				event -> sendDocumentTypeAttrChangedMessage(((DocumentTypeAttrAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateDocumentTypeAttr(HttpServletRequest request) {

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

		DocumentTypeAttr documentTypeAttrToBeUpdated = new DocumentTypeAttr();

		try {
			documentTypeAttrToBeUpdated = DocumentTypeAttrMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateDocumentTypeAttr(documentTypeAttrToBeUpdated);

	}

	/**
	 * Updates the DocumentTypeAttr with the specific Id
	 * 
	 * @param documentTypeAttrToBeUpdated the DocumentTypeAttr thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateDocumentTypeAttr(DocumentTypeAttr documentTypeAttrToBeUpdated) {

		UpdateDocumentTypeAttr com = new UpdateDocumentTypeAttr(documentTypeAttrToBeUpdated);

		int usedTicketId;

		synchronized (DocumentTypeAttrController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(DocumentTypeAttrUpdated.class,
				event -> sendDocumentTypeAttrChangedMessage(((DocumentTypeAttrUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a DocumentTypeAttr from the database
	 * 
	 * @param documentTypeAttrId:
	 *            the id of the DocumentTypeAttr thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deletedocumentTypeAttrById(@RequestParam(value = "documentTypeAttrId") String documentTypeAttrId) {

		DeleteDocumentTypeAttr com = new DeleteDocumentTypeAttr(documentTypeAttrId);

		int usedTicketId;

		synchronized (DocumentTypeAttrController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(DocumentTypeAttrDeleted.class,
				event -> sendDocumentTypeAttrChangedMessage(((DocumentTypeAttrDeleted) event).isSuccess(), usedTicketId));

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

	public void sendDocumentTypeAttrChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/documentTypeAttr/\" plus one of the following: "
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
