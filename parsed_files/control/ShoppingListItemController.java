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
import com.skytala.eCommerce.command.AddShoppingListItem;
import com.skytala.eCommerce.command.DeleteShoppingListItem;
import com.skytala.eCommerce.command.UpdateShoppingListItem;
import com.skytala.eCommerce.entity.ShoppingListItem;
import com.skytala.eCommerce.entity.ShoppingListItemMapper;
import com.skytala.eCommerce.event.ShoppingListItemAdded;
import com.skytala.eCommerce.event.ShoppingListItemDeleted;
import com.skytala.eCommerce.event.ShoppingListItemFound;
import com.skytala.eCommerce.event.ShoppingListItemUpdated;
import com.skytala.eCommerce.query.FindShoppingListItemsBy;

@RestController
@RequestMapping("/api/shoppingListItem")
public class ShoppingListItemController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<ShoppingListItem>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public ShoppingListItemController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a ShoppingListItem
	 * @return a List with the ShoppingListItems
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<ShoppingListItem> findShoppingListItemsBy(@RequestParam Map<String, String> allRequestParams) {

		FindShoppingListItemsBy query = new FindShoppingListItemsBy(allRequestParams);

		int usedTicketId;

		synchronized (ShoppingListItemController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ShoppingListItemFound.class,
				event -> sendShoppingListItemsFoundMessage(((ShoppingListItemFound) event).getShoppingListItems(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendShoppingListItemsFoundMessage(List<ShoppingListItem> shoppingListItems, int usedTicketId) {
		queryReturnVal.put(usedTicketId, shoppingListItems);
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
	public boolean createShoppingListItem(HttpServletRequest request) {

		ShoppingListItem shoppingListItemToBeAdded = new ShoppingListItem();
		try {
			shoppingListItemToBeAdded = ShoppingListItemMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createShoppingListItem(shoppingListItemToBeAdded);

	}

	/**
	 * creates a new ShoppingListItem entry in the ofbiz database
	 * 
	 * @param shoppingListItemToBeAdded
	 *            the ShoppingListItem thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createShoppingListItem(ShoppingListItem shoppingListItemToBeAdded) {

		AddShoppingListItem com = new AddShoppingListItem(shoppingListItemToBeAdded);
		int usedTicketId;

		synchronized (ShoppingListItemController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ShoppingListItemAdded.class,
				event -> sendShoppingListItemChangedMessage(((ShoppingListItemAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateShoppingListItem(HttpServletRequest request) {

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

		ShoppingListItem shoppingListItemToBeUpdated = new ShoppingListItem();

		try {
			shoppingListItemToBeUpdated = ShoppingListItemMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateShoppingListItem(shoppingListItemToBeUpdated);

	}

	/**
	 * Updates the ShoppingListItem with the specific Id
	 * 
	 * @param shoppingListItemToBeUpdated the ShoppingListItem thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateShoppingListItem(ShoppingListItem shoppingListItemToBeUpdated) {

		UpdateShoppingListItem com = new UpdateShoppingListItem(shoppingListItemToBeUpdated);

		int usedTicketId;

		synchronized (ShoppingListItemController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ShoppingListItemUpdated.class,
				event -> sendShoppingListItemChangedMessage(((ShoppingListItemUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a ShoppingListItem from the database
	 * 
	 * @param shoppingListItemId:
	 *            the id of the ShoppingListItem thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deleteshoppingListItemById(@RequestParam(value = "shoppingListItemId") String shoppingListItemId) {

		DeleteShoppingListItem com = new DeleteShoppingListItem(shoppingListItemId);

		int usedTicketId;

		synchronized (ShoppingListItemController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ShoppingListItemDeleted.class,
				event -> sendShoppingListItemChangedMessage(((ShoppingListItemDeleted) event).isSuccess(), usedTicketId));

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

	public void sendShoppingListItemChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/shoppingListItem/\" plus one of the following: "
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
