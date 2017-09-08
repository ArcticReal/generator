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
import com.skytala.eCommerce.command.AddReturnItem;
import com.skytala.eCommerce.command.DeleteReturnItem;
import com.skytala.eCommerce.command.UpdateReturnItem;
import com.skytala.eCommerce.entity.ReturnItem;
import com.skytala.eCommerce.entity.ReturnItemMapper;
import com.skytala.eCommerce.event.ReturnItemAdded;
import com.skytala.eCommerce.event.ReturnItemDeleted;
import com.skytala.eCommerce.event.ReturnItemFound;
import com.skytala.eCommerce.event.ReturnItemUpdated;
import com.skytala.eCommerce.query.FindReturnItemsBy;

@RestController
@RequestMapping("/api/returnItem")
public class ReturnItemController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<ReturnItem>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public ReturnItemController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a ReturnItem
	 * @return a List with the ReturnItems
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<ReturnItem> findReturnItemsBy(@RequestParam Map<String, String> allRequestParams) {

		FindReturnItemsBy query = new FindReturnItemsBy(allRequestParams);

		int usedTicketId;

		synchronized (ReturnItemController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ReturnItemFound.class,
				event -> sendReturnItemsFoundMessage(((ReturnItemFound) event).getReturnItems(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendReturnItemsFoundMessage(List<ReturnItem> returnItems, int usedTicketId) {
		queryReturnVal.put(usedTicketId, returnItems);
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
	public boolean createReturnItem(HttpServletRequest request) {

		ReturnItem returnItemToBeAdded = new ReturnItem();
		try {
			returnItemToBeAdded = ReturnItemMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createReturnItem(returnItemToBeAdded);

	}

	/**
	 * creates a new ReturnItem entry in the ofbiz database
	 * 
	 * @param returnItemToBeAdded
	 *            the ReturnItem thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createReturnItem(ReturnItem returnItemToBeAdded) {

		AddReturnItem com = new AddReturnItem(returnItemToBeAdded);
		int usedTicketId;

		synchronized (ReturnItemController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ReturnItemAdded.class,
				event -> sendReturnItemChangedMessage(((ReturnItemAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateReturnItem(HttpServletRequest request) {

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

		ReturnItem returnItemToBeUpdated = new ReturnItem();

		try {
			returnItemToBeUpdated = ReturnItemMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateReturnItem(returnItemToBeUpdated);

	}

	/**
	 * Updates the ReturnItem with the specific Id
	 * 
	 * @param returnItemToBeUpdated the ReturnItem thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateReturnItem(ReturnItem returnItemToBeUpdated) {

		UpdateReturnItem com = new UpdateReturnItem(returnItemToBeUpdated);

		int usedTicketId;

		synchronized (ReturnItemController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ReturnItemUpdated.class,
				event -> sendReturnItemChangedMessage(((ReturnItemUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a ReturnItem from the database
	 * 
	 * @param returnItemId:
	 *            the id of the ReturnItem thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deletereturnItemById(@RequestParam(value = "returnItemId") String returnItemId) {

		DeleteReturnItem com = new DeleteReturnItem(returnItemId);

		int usedTicketId;

		synchronized (ReturnItemController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ReturnItemDeleted.class,
				event -> sendReturnItemChangedMessage(((ReturnItemDeleted) event).isSuccess(), usedTicketId));

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

	public void sendReturnItemChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/returnItem/\" plus one of the following: "
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
