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
import com.skytala.eCommerce.command.AddShoppingListWorkEffort;
import com.skytala.eCommerce.command.DeleteShoppingListWorkEffort;
import com.skytala.eCommerce.command.UpdateShoppingListWorkEffort;
import com.skytala.eCommerce.entity.ShoppingListWorkEffort;
import com.skytala.eCommerce.entity.ShoppingListWorkEffortMapper;
import com.skytala.eCommerce.event.ShoppingListWorkEffortAdded;
import com.skytala.eCommerce.event.ShoppingListWorkEffortDeleted;
import com.skytala.eCommerce.event.ShoppingListWorkEffortFound;
import com.skytala.eCommerce.event.ShoppingListWorkEffortUpdated;
import com.skytala.eCommerce.query.FindShoppingListWorkEffortsBy;

@RestController
@RequestMapping("/api/shoppingListWorkEffort")
public class ShoppingListWorkEffortController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<ShoppingListWorkEffort>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public ShoppingListWorkEffortController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a ShoppingListWorkEffort
	 * @return a List with the ShoppingListWorkEfforts
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<ShoppingListWorkEffort> findShoppingListWorkEffortsBy(@RequestParam Map<String, String> allRequestParams) {

		FindShoppingListWorkEffortsBy query = new FindShoppingListWorkEffortsBy(allRequestParams);

		int usedTicketId;

		synchronized (ShoppingListWorkEffortController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ShoppingListWorkEffortFound.class,
				event -> sendShoppingListWorkEffortsFoundMessage(((ShoppingListWorkEffortFound) event).getShoppingListWorkEfforts(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendShoppingListWorkEffortsFoundMessage(List<ShoppingListWorkEffort> shoppingListWorkEfforts, int usedTicketId) {
		queryReturnVal.put(usedTicketId, shoppingListWorkEfforts);
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
	public boolean createShoppingListWorkEffort(HttpServletRequest request) {

		ShoppingListWorkEffort shoppingListWorkEffortToBeAdded = new ShoppingListWorkEffort();
		try {
			shoppingListWorkEffortToBeAdded = ShoppingListWorkEffortMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createShoppingListWorkEffort(shoppingListWorkEffortToBeAdded);

	}

	/**
	 * creates a new ShoppingListWorkEffort entry in the ofbiz database
	 * 
	 * @param shoppingListWorkEffortToBeAdded
	 *            the ShoppingListWorkEffort thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createShoppingListWorkEffort(ShoppingListWorkEffort shoppingListWorkEffortToBeAdded) {

		AddShoppingListWorkEffort com = new AddShoppingListWorkEffort(shoppingListWorkEffortToBeAdded);
		int usedTicketId;

		synchronized (ShoppingListWorkEffortController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ShoppingListWorkEffortAdded.class,
				event -> sendShoppingListWorkEffortChangedMessage(((ShoppingListWorkEffortAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateShoppingListWorkEffort(HttpServletRequest request) {

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

		ShoppingListWorkEffort shoppingListWorkEffortToBeUpdated = new ShoppingListWorkEffort();

		try {
			shoppingListWorkEffortToBeUpdated = ShoppingListWorkEffortMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateShoppingListWorkEffort(shoppingListWorkEffortToBeUpdated);

	}

	/**
	 * Updates the ShoppingListWorkEffort with the specific Id
	 * 
	 * @param shoppingListWorkEffortToBeUpdated the ShoppingListWorkEffort thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateShoppingListWorkEffort(ShoppingListWorkEffort shoppingListWorkEffortToBeUpdated) {

		UpdateShoppingListWorkEffort com = new UpdateShoppingListWorkEffort(shoppingListWorkEffortToBeUpdated);

		int usedTicketId;

		synchronized (ShoppingListWorkEffortController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ShoppingListWorkEffortUpdated.class,
				event -> sendShoppingListWorkEffortChangedMessage(((ShoppingListWorkEffortUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a ShoppingListWorkEffort from the database
	 * 
	 * @param shoppingListWorkEffortId:
	 *            the id of the ShoppingListWorkEffort thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deleteshoppingListWorkEffortById(@RequestParam(value = "shoppingListWorkEffortId") String shoppingListWorkEffortId) {

		DeleteShoppingListWorkEffort com = new DeleteShoppingListWorkEffort(shoppingListWorkEffortId);

		int usedTicketId;

		synchronized (ShoppingListWorkEffortController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ShoppingListWorkEffortDeleted.class,
				event -> sendShoppingListWorkEffortChangedMessage(((ShoppingListWorkEffortDeleted) event).isSuccess(), usedTicketId));

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

	public void sendShoppingListWorkEffortChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/shoppingListWorkEffort/\" plus one of the following: "
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
