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
import com.skytala.eCommerce.command.AddInventoryItem;
import com.skytala.eCommerce.command.DeleteInventoryItem;
import com.skytala.eCommerce.command.UpdateInventoryItem;
import com.skytala.eCommerce.entity.InventoryItem;
import com.skytala.eCommerce.entity.InventoryItemMapper;
import com.skytala.eCommerce.event.InventoryItemAdded;
import com.skytala.eCommerce.event.InventoryItemDeleted;
import com.skytala.eCommerce.event.InventoryItemFound;
import com.skytala.eCommerce.event.InventoryItemUpdated;
import com.skytala.eCommerce.query.FindInventoryItemsBy;

@RestController
@RequestMapping("/api/inventoryItem")
public class InventoryItemController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<InventoryItem>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public InventoryItemController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a InventoryItem
	 * @return a List with the InventoryItems
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<InventoryItem> findInventoryItemsBy(@RequestParam Map<String, String> allRequestParams) {

		FindInventoryItemsBy query = new FindInventoryItemsBy(allRequestParams);

		int usedTicketId;

		synchronized (InventoryItemController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(InventoryItemFound.class,
				event -> sendInventoryItemsFoundMessage(((InventoryItemFound) event).getInventoryItems(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendInventoryItemsFoundMessage(List<InventoryItem> inventoryItems, int usedTicketId) {
		queryReturnVal.put(usedTicketId, inventoryItems);
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
	public boolean createInventoryItem(HttpServletRequest request) {

		InventoryItem inventoryItemToBeAdded = new InventoryItem();
		try {
			inventoryItemToBeAdded = InventoryItemMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createInventoryItem(inventoryItemToBeAdded);

	}

	/**
	 * creates a new InventoryItem entry in the ofbiz database
	 * 
	 * @param inventoryItemToBeAdded
	 *            the InventoryItem thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createInventoryItem(InventoryItem inventoryItemToBeAdded) {

		AddInventoryItem com = new AddInventoryItem(inventoryItemToBeAdded);
		int usedTicketId;

		synchronized (InventoryItemController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(InventoryItemAdded.class,
				event -> sendInventoryItemChangedMessage(((InventoryItemAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateInventoryItem(HttpServletRequest request) {

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

		InventoryItem inventoryItemToBeUpdated = new InventoryItem();

		try {
			inventoryItemToBeUpdated = InventoryItemMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateInventoryItem(inventoryItemToBeUpdated);

	}

	/**
	 * Updates the InventoryItem with the specific Id
	 * 
	 * @param inventoryItemToBeUpdated the InventoryItem thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateInventoryItem(InventoryItem inventoryItemToBeUpdated) {

		UpdateInventoryItem com = new UpdateInventoryItem(inventoryItemToBeUpdated);

		int usedTicketId;

		synchronized (InventoryItemController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(InventoryItemUpdated.class,
				event -> sendInventoryItemChangedMessage(((InventoryItemUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a InventoryItem from the database
	 * 
	 * @param inventoryItemId:
	 *            the id of the InventoryItem thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deleteinventoryItemById(@RequestParam(value = "inventoryItemId") String inventoryItemId) {

		DeleteInventoryItem com = new DeleteInventoryItem(inventoryItemId);

		int usedTicketId;

		synchronized (InventoryItemController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(InventoryItemDeleted.class,
				event -> sendInventoryItemChangedMessage(((InventoryItemDeleted) event).isSuccess(), usedTicketId));

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

	public void sendInventoryItemChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/inventoryItem/\" plus one of the following: "
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
