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
import com.skytala.eCommerce.command.AddContentPurposeOperation;
import com.skytala.eCommerce.command.DeleteContentPurposeOperation;
import com.skytala.eCommerce.command.UpdateContentPurposeOperation;
import com.skytala.eCommerce.entity.ContentPurposeOperation;
import com.skytala.eCommerce.entity.ContentPurposeOperationMapper;
import com.skytala.eCommerce.event.ContentPurposeOperationAdded;
import com.skytala.eCommerce.event.ContentPurposeOperationDeleted;
import com.skytala.eCommerce.event.ContentPurposeOperationFound;
import com.skytala.eCommerce.event.ContentPurposeOperationUpdated;
import com.skytala.eCommerce.query.FindContentPurposeOperationsBy;

@RestController
@RequestMapping("/api/contentPurposeOperation")
public class ContentPurposeOperationController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<ContentPurposeOperation>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public ContentPurposeOperationController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a ContentPurposeOperation
	 * @return a List with the ContentPurposeOperations
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<ContentPurposeOperation> findContentPurposeOperationsBy(@RequestParam Map<String, String> allRequestParams) {

		FindContentPurposeOperationsBy query = new FindContentPurposeOperationsBy(allRequestParams);

		int usedTicketId;

		synchronized (ContentPurposeOperationController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ContentPurposeOperationFound.class,
				event -> sendContentPurposeOperationsFoundMessage(((ContentPurposeOperationFound) event).getContentPurposeOperations(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendContentPurposeOperationsFoundMessage(List<ContentPurposeOperation> contentPurposeOperations, int usedTicketId) {
		queryReturnVal.put(usedTicketId, contentPurposeOperations);
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
	public boolean createContentPurposeOperation(HttpServletRequest request) {

		ContentPurposeOperation contentPurposeOperationToBeAdded = new ContentPurposeOperation();
		try {
			contentPurposeOperationToBeAdded = ContentPurposeOperationMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createContentPurposeOperation(contentPurposeOperationToBeAdded);

	}

	/**
	 * creates a new ContentPurposeOperation entry in the ofbiz database
	 * 
	 * @param contentPurposeOperationToBeAdded
	 *            the ContentPurposeOperation thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createContentPurposeOperation(ContentPurposeOperation contentPurposeOperationToBeAdded) {

		AddContentPurposeOperation com = new AddContentPurposeOperation(contentPurposeOperationToBeAdded);
		int usedTicketId;

		synchronized (ContentPurposeOperationController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ContentPurposeOperationAdded.class,
				event -> sendContentPurposeOperationChangedMessage(((ContentPurposeOperationAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateContentPurposeOperation(HttpServletRequest request) {

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

		ContentPurposeOperation contentPurposeOperationToBeUpdated = new ContentPurposeOperation();

		try {
			contentPurposeOperationToBeUpdated = ContentPurposeOperationMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateContentPurposeOperation(contentPurposeOperationToBeUpdated);

	}

	/**
	 * Updates the ContentPurposeOperation with the specific Id
	 * 
	 * @param contentPurposeOperationToBeUpdated the ContentPurposeOperation thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateContentPurposeOperation(ContentPurposeOperation contentPurposeOperationToBeUpdated) {

		UpdateContentPurposeOperation com = new UpdateContentPurposeOperation(contentPurposeOperationToBeUpdated);

		int usedTicketId;

		synchronized (ContentPurposeOperationController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ContentPurposeOperationUpdated.class,
				event -> sendContentPurposeOperationChangedMessage(((ContentPurposeOperationUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a ContentPurposeOperation from the database
	 * 
	 * @param contentPurposeOperationId:
	 *            the id of the ContentPurposeOperation thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deletecontentPurposeOperationById(@RequestParam(value = "contentPurposeOperationId") String contentPurposeOperationId) {

		DeleteContentPurposeOperation com = new DeleteContentPurposeOperation(contentPurposeOperationId);

		int usedTicketId;

		synchronized (ContentPurposeOperationController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ContentPurposeOperationDeleted.class,
				event -> sendContentPurposeOperationChangedMessage(((ContentPurposeOperationDeleted) event).isSuccess(), usedTicketId));

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

	public void sendContentPurposeOperationChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/contentPurposeOperation/\" plus one of the following: "
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
