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
import com.skytala.eCommerce.command.AddInventoryItemType;
import com.skytala.eCommerce.command.DeleteInventoryItemType;
import com.skytala.eCommerce.command.UpdateInventoryItemType;
import com.skytala.eCommerce.entity.InventoryItemType;
import com.skytala.eCommerce.entity.InventoryItemTypeMapper;
import com.skytala.eCommerce.event.InventoryItemTypeAdded;
import com.skytala.eCommerce.event.InventoryItemTypeDeleted;
import com.skytala.eCommerce.event.InventoryItemTypeFound;
import com.skytala.eCommerce.event.InventoryItemTypeUpdated;
import com.skytala.eCommerce.query.FindInventoryItemTypesBy;

@RestController
@RequestMapping("/api/inventoryItemType")
public class InventoryItemTypeController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<InventoryItemType>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public InventoryItemTypeController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a InventoryItemType
	 * @return a List with the InventoryItemTypes
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<InventoryItemType> findInventoryItemTypesBy(@RequestParam Map<String, String> allRequestParams) {

		FindInventoryItemTypesBy query = new FindInventoryItemTypesBy(allRequestParams);

		int usedTicketId;

		synchronized (InventoryItemTypeController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(InventoryItemTypeFound.class,
				event -> sendInventoryItemTypesFoundMessage(((InventoryItemTypeFound) event).getInventoryItemTypes(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendInventoryItemTypesFoundMessage(List<InventoryItemType> inventoryItemTypes, int usedTicketId) {
		queryReturnVal.put(usedTicketId, inventoryItemTypes);
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
	public boolean createInventoryItemType(HttpServletRequest request) {

		InventoryItemType inventoryItemTypeToBeAdded = new InventoryItemType();
		try {
			inventoryItemTypeToBeAdded = InventoryItemTypeMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createInventoryItemType(inventoryItemTypeToBeAdded);

	}

	/**
	 * creates a new InventoryItemType entry in the ofbiz database
	 * 
	 * @param inventoryItemTypeToBeAdded
	 *            the InventoryItemType thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createInventoryItemType(InventoryItemType inventoryItemTypeToBeAdded) {

		AddInventoryItemType com = new AddInventoryItemType(inventoryItemTypeToBeAdded);
		int usedTicketId;

		synchronized (InventoryItemTypeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(InventoryItemTypeAdded.class,
				event -> sendInventoryItemTypeChangedMessage(((InventoryItemTypeAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateInventoryItemType(HttpServletRequest request) {

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

		InventoryItemType inventoryItemTypeToBeUpdated = new InventoryItemType();

		try {
			inventoryItemTypeToBeUpdated = InventoryItemTypeMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateInventoryItemType(inventoryItemTypeToBeUpdated);

	}

	/**
	 * Updates the InventoryItemType with the specific Id
	 * 
	 * @param inventoryItemTypeToBeUpdated the InventoryItemType thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateInventoryItemType(InventoryItemType inventoryItemTypeToBeUpdated) {

		UpdateInventoryItemType com = new UpdateInventoryItemType(inventoryItemTypeToBeUpdated);

		int usedTicketId;

		synchronized (InventoryItemTypeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(InventoryItemTypeUpdated.class,
				event -> sendInventoryItemTypeChangedMessage(((InventoryItemTypeUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a InventoryItemType from the database
	 * 
	 * @param inventoryItemTypeId:
	 *            the id of the InventoryItemType thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deleteinventoryItemTypeById(@RequestParam(value = "inventoryItemTypeId") String inventoryItemTypeId) {

		DeleteInventoryItemType com = new DeleteInventoryItemType(inventoryItemTypeId);

		int usedTicketId;

		synchronized (InventoryItemTypeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(InventoryItemTypeDeleted.class,
				event -> sendInventoryItemTypeChangedMessage(((InventoryItemTypeDeleted) event).isSuccess(), usedTicketId));

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

	public void sendInventoryItemTypeChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/inventoryItemType/\" plus one of the following: "
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
