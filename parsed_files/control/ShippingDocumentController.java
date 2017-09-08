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
import com.skytala.eCommerce.command.AddShippingDocument;
import com.skytala.eCommerce.command.DeleteShippingDocument;
import com.skytala.eCommerce.command.UpdateShippingDocument;
import com.skytala.eCommerce.entity.ShippingDocument;
import com.skytala.eCommerce.entity.ShippingDocumentMapper;
import com.skytala.eCommerce.event.ShippingDocumentAdded;
import com.skytala.eCommerce.event.ShippingDocumentDeleted;
import com.skytala.eCommerce.event.ShippingDocumentFound;
import com.skytala.eCommerce.event.ShippingDocumentUpdated;
import com.skytala.eCommerce.query.FindShippingDocumentsBy;

@RestController
@RequestMapping("/api/shippingDocument")
public class ShippingDocumentController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<ShippingDocument>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public ShippingDocumentController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a ShippingDocument
	 * @return a List with the ShippingDocuments
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<ShippingDocument> findShippingDocumentsBy(@RequestParam Map<String, String> allRequestParams) {

		FindShippingDocumentsBy query = new FindShippingDocumentsBy(allRequestParams);

		int usedTicketId;

		synchronized (ShippingDocumentController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ShippingDocumentFound.class,
				event -> sendShippingDocumentsFoundMessage(((ShippingDocumentFound) event).getShippingDocuments(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendShippingDocumentsFoundMessage(List<ShippingDocument> shippingDocuments, int usedTicketId) {
		queryReturnVal.put(usedTicketId, shippingDocuments);
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
	public boolean createShippingDocument(HttpServletRequest request) {

		ShippingDocument shippingDocumentToBeAdded = new ShippingDocument();
		try {
			shippingDocumentToBeAdded = ShippingDocumentMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createShippingDocument(shippingDocumentToBeAdded);

	}

	/**
	 * creates a new ShippingDocument entry in the ofbiz database
	 * 
	 * @param shippingDocumentToBeAdded
	 *            the ShippingDocument thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createShippingDocument(ShippingDocument shippingDocumentToBeAdded) {

		AddShippingDocument com = new AddShippingDocument(shippingDocumentToBeAdded);
		int usedTicketId;

		synchronized (ShippingDocumentController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ShippingDocumentAdded.class,
				event -> sendShippingDocumentChangedMessage(((ShippingDocumentAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateShippingDocument(HttpServletRequest request) {

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

		ShippingDocument shippingDocumentToBeUpdated = new ShippingDocument();

		try {
			shippingDocumentToBeUpdated = ShippingDocumentMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateShippingDocument(shippingDocumentToBeUpdated);

	}

	/**
	 * Updates the ShippingDocument with the specific Id
	 * 
	 * @param shippingDocumentToBeUpdated the ShippingDocument thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateShippingDocument(ShippingDocument shippingDocumentToBeUpdated) {

		UpdateShippingDocument com = new UpdateShippingDocument(shippingDocumentToBeUpdated);

		int usedTicketId;

		synchronized (ShippingDocumentController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ShippingDocumentUpdated.class,
				event -> sendShippingDocumentChangedMessage(((ShippingDocumentUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a ShippingDocument from the database
	 * 
	 * @param shippingDocumentId:
	 *            the id of the ShippingDocument thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deleteshippingDocumentById(@RequestParam(value = "shippingDocumentId") String shippingDocumentId) {

		DeleteShippingDocument com = new DeleteShippingDocument(shippingDocumentId);

		int usedTicketId;

		synchronized (ShippingDocumentController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ShippingDocumentDeleted.class,
				event -> sendShippingDocumentChangedMessage(((ShippingDocumentDeleted) event).isSuccess(), usedTicketId));

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

	public void sendShippingDocumentChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/shippingDocument/\" plus one of the following: "
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
