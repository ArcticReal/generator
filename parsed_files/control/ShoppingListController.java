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
import com.skytala.eCommerce.command.AddShoppingList;
import com.skytala.eCommerce.command.DeleteShoppingList;
import com.skytala.eCommerce.command.UpdateShoppingList;
import com.skytala.eCommerce.entity.ShoppingList;
import com.skytala.eCommerce.entity.ShoppingListMapper;
import com.skytala.eCommerce.event.ShoppingListAdded;
import com.skytala.eCommerce.event.ShoppingListDeleted;
import com.skytala.eCommerce.event.ShoppingListFound;
import com.skytala.eCommerce.event.ShoppingListUpdated;
import com.skytala.eCommerce.query.FindShoppingListsBy;

@RestController
@RequestMapping("/api/shoppingList")
public class ShoppingListController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<ShoppingList>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public ShoppingListController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a ShoppingList
	 * @return a List with the ShoppingLists
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<ShoppingList> findShoppingListsBy(@RequestParam Map<String, String> allRequestParams) {

		FindShoppingListsBy query = new FindShoppingListsBy(allRequestParams);

		int usedTicketId;

		synchronized (ShoppingListController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ShoppingListFound.class,
				event -> sendShoppingListsFoundMessage(((ShoppingListFound) event).getShoppingLists(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendShoppingListsFoundMessage(List<ShoppingList> shoppingLists, int usedTicketId) {
		queryReturnVal.put(usedTicketId, shoppingLists);
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
	public boolean createShoppingList(HttpServletRequest request) {

		ShoppingList shoppingListToBeAdded = new ShoppingList();
		try {
			shoppingListToBeAdded = ShoppingListMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createShoppingList(shoppingListToBeAdded);

	}

	/**
	 * creates a new ShoppingList entry in the ofbiz database
	 * 
	 * @param shoppingListToBeAdded
	 *            the ShoppingList thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createShoppingList(ShoppingList shoppingListToBeAdded) {

		AddShoppingList com = new AddShoppingList(shoppingListToBeAdded);
		int usedTicketId;

		synchronized (ShoppingListController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ShoppingListAdded.class,
				event -> sendShoppingListChangedMessage(((ShoppingListAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateShoppingList(HttpServletRequest request) {

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

		ShoppingList shoppingListToBeUpdated = new ShoppingList();

		try {
			shoppingListToBeUpdated = ShoppingListMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateShoppingList(shoppingListToBeUpdated);

	}

	/**
	 * Updates the ShoppingList with the specific Id
	 * 
	 * @param shoppingListToBeUpdated the ShoppingList thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateShoppingList(ShoppingList shoppingListToBeUpdated) {

		UpdateShoppingList com = new UpdateShoppingList(shoppingListToBeUpdated);

		int usedTicketId;

		synchronized (ShoppingListController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ShoppingListUpdated.class,
				event -> sendShoppingListChangedMessage(((ShoppingListUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a ShoppingList from the database
	 * 
	 * @param shoppingListId:
	 *            the id of the ShoppingList thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deleteshoppingListById(@RequestParam(value = "shoppingListId") String shoppingListId) {

		DeleteShoppingList com = new DeleteShoppingList(shoppingListId);

		int usedTicketId;

		synchronized (ShoppingListController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ShoppingListDeleted.class,
				event -> sendShoppingListChangedMessage(((ShoppingListDeleted) event).isSuccess(), usedTicketId));

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

	public void sendShoppingListChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/shoppingList/\" plus one of the following: "
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
