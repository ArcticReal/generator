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
import com.skytala.eCommerce.command.AddContentOperation;
import com.skytala.eCommerce.command.DeleteContentOperation;
import com.skytala.eCommerce.command.UpdateContentOperation;
import com.skytala.eCommerce.entity.ContentOperation;
import com.skytala.eCommerce.entity.ContentOperationMapper;
import com.skytala.eCommerce.event.ContentOperationAdded;
import com.skytala.eCommerce.event.ContentOperationDeleted;
import com.skytala.eCommerce.event.ContentOperationFound;
import com.skytala.eCommerce.event.ContentOperationUpdated;
import com.skytala.eCommerce.query.FindContentOperationsBy;

@RestController
@RequestMapping("/api/contentOperation")
public class ContentOperationController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<ContentOperation>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public ContentOperationController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a ContentOperation
	 * @return a List with the ContentOperations
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<ContentOperation> findContentOperationsBy(@RequestParam Map<String, String> allRequestParams) {

		FindContentOperationsBy query = new FindContentOperationsBy(allRequestParams);

		int usedTicketId;

		synchronized (ContentOperationController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ContentOperationFound.class,
				event -> sendContentOperationsFoundMessage(((ContentOperationFound) event).getContentOperations(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendContentOperationsFoundMessage(List<ContentOperation> contentOperations, int usedTicketId) {
		queryReturnVal.put(usedTicketId, contentOperations);
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
	public boolean createContentOperation(HttpServletRequest request) {

		ContentOperation contentOperationToBeAdded = new ContentOperation();
		try {
			contentOperationToBeAdded = ContentOperationMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createContentOperation(contentOperationToBeAdded);

	}

	/**
	 * creates a new ContentOperation entry in the ofbiz database
	 * 
	 * @param contentOperationToBeAdded
	 *            the ContentOperation thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createContentOperation(ContentOperation contentOperationToBeAdded) {

		AddContentOperation com = new AddContentOperation(contentOperationToBeAdded);
		int usedTicketId;

		synchronized (ContentOperationController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ContentOperationAdded.class,
				event -> sendContentOperationChangedMessage(((ContentOperationAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateContentOperation(HttpServletRequest request) {

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

		ContentOperation contentOperationToBeUpdated = new ContentOperation();

		try {
			contentOperationToBeUpdated = ContentOperationMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateContentOperation(contentOperationToBeUpdated);

	}

	/**
	 * Updates the ContentOperation with the specific Id
	 * 
	 * @param contentOperationToBeUpdated the ContentOperation thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateContentOperation(ContentOperation contentOperationToBeUpdated) {

		UpdateContentOperation com = new UpdateContentOperation(contentOperationToBeUpdated);

		int usedTicketId;

		synchronized (ContentOperationController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ContentOperationUpdated.class,
				event -> sendContentOperationChangedMessage(((ContentOperationUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a ContentOperation from the database
	 * 
	 * @param contentOperationId:
	 *            the id of the ContentOperation thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deletecontentOperationById(@RequestParam(value = "contentOperationId") String contentOperationId) {

		DeleteContentOperation com = new DeleteContentOperation(contentOperationId);

		int usedTicketId;

		synchronized (ContentOperationController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ContentOperationDeleted.class,
				event -> sendContentOperationChangedMessage(((ContentOperationDeleted) event).isSuccess(), usedTicketId));

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

	public void sendContentOperationChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/contentOperation/\" plus one of the following: "
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
