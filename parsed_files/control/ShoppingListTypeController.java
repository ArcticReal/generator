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
import com.skytala.eCommerce.command.AddShoppingListType;
import com.skytala.eCommerce.command.DeleteShoppingListType;
import com.skytala.eCommerce.command.UpdateShoppingListType;
import com.skytala.eCommerce.entity.ShoppingListType;
import com.skytala.eCommerce.entity.ShoppingListTypeMapper;
import com.skytala.eCommerce.event.ShoppingListTypeAdded;
import com.skytala.eCommerce.event.ShoppingListTypeDeleted;
import com.skytala.eCommerce.event.ShoppingListTypeFound;
import com.skytala.eCommerce.event.ShoppingListTypeUpdated;
import com.skytala.eCommerce.query.FindShoppingListTypesBy;

@RestController
@RequestMapping("/api/shoppingListType")
public class ShoppingListTypeController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<ShoppingListType>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public ShoppingListTypeController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a ShoppingListType
	 * @return a List with the ShoppingListTypes
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<ShoppingListType> findShoppingListTypesBy(@RequestParam Map<String, String> allRequestParams) {

		FindShoppingListTypesBy query = new FindShoppingListTypesBy(allRequestParams);

		int usedTicketId;

		synchronized (ShoppingListTypeController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ShoppingListTypeFound.class,
				event -> sendShoppingListTypesFoundMessage(((ShoppingListTypeFound) event).getShoppingListTypes(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendShoppingListTypesFoundMessage(List<ShoppingListType> shoppingListTypes, int usedTicketId) {
		queryReturnVal.put(usedTicketId, shoppingListTypes);
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
	public boolean createShoppingListType(HttpServletRequest request) {

		ShoppingListType shoppingListTypeToBeAdded = new ShoppingListType();
		try {
			shoppingListTypeToBeAdded = ShoppingListTypeMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createShoppingListType(shoppingListTypeToBeAdded);

	}

	/**
	 * creates a new ShoppingListType entry in the ofbiz database
	 * 
	 * @param shoppingListTypeToBeAdded
	 *            the ShoppingListType thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createShoppingListType(ShoppingListType shoppingListTypeToBeAdded) {

		AddShoppingListType com = new AddShoppingListType(shoppingListTypeToBeAdded);
		int usedTicketId;

		synchronized (ShoppingListTypeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ShoppingListTypeAdded.class,
				event -> sendShoppingListTypeChangedMessage(((ShoppingListTypeAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateShoppingListType(HttpServletRequest request) {

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

		ShoppingListType shoppingListTypeToBeUpdated = new ShoppingListType();

		try {
			shoppingListTypeToBeUpdated = ShoppingListTypeMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateShoppingListType(shoppingListTypeToBeUpdated);

	}

	/**
	 * Updates the ShoppingListType with the specific Id
	 * 
	 * @param shoppingListTypeToBeUpdated the ShoppingListType thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateShoppingListType(ShoppingListType shoppingListTypeToBeUpdated) {

		UpdateShoppingListType com = new UpdateShoppingListType(shoppingListTypeToBeUpdated);

		int usedTicketId;

		synchronized (ShoppingListTypeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ShoppingListTypeUpdated.class,
				event -> sendShoppingListTypeChangedMessage(((ShoppingListTypeUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a ShoppingListType from the database
	 * 
	 * @param shoppingListTypeId:
	 *            the id of the ShoppingListType thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deleteshoppingListTypeById(@RequestParam(value = "shoppingListTypeId") String shoppingListTypeId) {

		DeleteShoppingListType com = new DeleteShoppingListType(shoppingListTypeId);

		int usedTicketId;

		synchronized (ShoppingListTypeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ShoppingListTypeDeleted.class,
				event -> sendShoppingListTypeChangedMessage(((ShoppingListTypeDeleted) event).isSuccess(), usedTicketId));

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

	public void sendShoppingListTypeChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/shoppingListType/\" plus one of the following: "
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
