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
import com.skytala.eCommerce.command.AddDocumentType;
import com.skytala.eCommerce.command.DeleteDocumentType;
import com.skytala.eCommerce.command.UpdateDocumentType;
import com.skytala.eCommerce.entity.DocumentType;
import com.skytala.eCommerce.entity.DocumentTypeMapper;
import com.skytala.eCommerce.event.DocumentTypeAdded;
import com.skytala.eCommerce.event.DocumentTypeDeleted;
import com.skytala.eCommerce.event.DocumentTypeFound;
import com.skytala.eCommerce.event.DocumentTypeUpdated;
import com.skytala.eCommerce.query.FindDocumentTypesBy;

@RestController
@RequestMapping("/api/documentType")
public class DocumentTypeController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<DocumentType>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public DocumentTypeController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a DocumentType
	 * @return a List with the DocumentTypes
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<DocumentType> findDocumentTypesBy(@RequestParam Map<String, String> allRequestParams) {

		FindDocumentTypesBy query = new FindDocumentTypesBy(allRequestParams);

		int usedTicketId;

		synchronized (DocumentTypeController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(DocumentTypeFound.class,
				event -> sendDocumentTypesFoundMessage(((DocumentTypeFound) event).getDocumentTypes(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendDocumentTypesFoundMessage(List<DocumentType> documentTypes, int usedTicketId) {
		queryReturnVal.put(usedTicketId, documentTypes);
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
	public boolean createDocumentType(HttpServletRequest request) {

		DocumentType documentTypeToBeAdded = new DocumentType();
		try {
			documentTypeToBeAdded = DocumentTypeMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createDocumentType(documentTypeToBeAdded);

	}

	/**
	 * creates a new DocumentType entry in the ofbiz database
	 * 
	 * @param documentTypeToBeAdded
	 *            the DocumentType thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createDocumentType(DocumentType documentTypeToBeAdded) {

		AddDocumentType com = new AddDocumentType(documentTypeToBeAdded);
		int usedTicketId;

		synchronized (DocumentTypeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(DocumentTypeAdded.class,
				event -> sendDocumentTypeChangedMessage(((DocumentTypeAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateDocumentType(HttpServletRequest request) {

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

		DocumentType documentTypeToBeUpdated = new DocumentType();

		try {
			documentTypeToBeUpdated = DocumentTypeMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateDocumentType(documentTypeToBeUpdated);

	}

	/**
	 * Updates the DocumentType with the specific Id
	 * 
	 * @param documentTypeToBeUpdated the DocumentType thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateDocumentType(DocumentType documentTypeToBeUpdated) {

		UpdateDocumentType com = new UpdateDocumentType(documentTypeToBeUpdated);

		int usedTicketId;

		synchronized (DocumentTypeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(DocumentTypeUpdated.class,
				event -> sendDocumentTypeChangedMessage(((DocumentTypeUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a DocumentType from the database
	 * 
	 * @param documentTypeId:
	 *            the id of the DocumentType thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deletedocumentTypeById(@RequestParam(value = "documentTypeId") String documentTypeId) {

		DeleteDocumentType com = new DeleteDocumentType(documentTypeId);

		int usedTicketId;

		synchronized (DocumentTypeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(DocumentTypeDeleted.class,
				event -> sendDocumentTypeChangedMessage(((DocumentTypeDeleted) event).isSuccess(), usedTicketId));

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

	public void sendDocumentTypeChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/documentType/\" plus one of the following: "
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
