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
import com.skytala.eCommerce.command.AddInventoryItemStatus;
import com.skytala.eCommerce.command.DeleteInventoryItemStatus;
import com.skytala.eCommerce.command.UpdateInventoryItemStatus;
import com.skytala.eCommerce.entity.InventoryItemStatus;
import com.skytala.eCommerce.entity.InventoryItemStatusMapper;
import com.skytala.eCommerce.event.InventoryItemStatusAdded;
import com.skytala.eCommerce.event.InventoryItemStatusDeleted;
import com.skytala.eCommerce.event.InventoryItemStatusFound;
import com.skytala.eCommerce.event.InventoryItemStatusUpdated;
import com.skytala.eCommerce.query.FindInventoryItemStatussBy;

@RestController
@RequestMapping("/api/inventoryItemStatus")
public class InventoryItemStatusController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<InventoryItemStatus>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public InventoryItemStatusController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a InventoryItemStatus
	 * @return a List with the InventoryItemStatuss
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<InventoryItemStatus> findInventoryItemStatussBy(@RequestParam Map<String, String> allRequestParams) {

		FindInventoryItemStatussBy query = new FindInventoryItemStatussBy(allRequestParams);

		int usedTicketId;

		synchronized (InventoryItemStatusController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(InventoryItemStatusFound.class,
				event -> sendInventoryItemStatussFoundMessage(((InventoryItemStatusFound) event).getInventoryItemStatuss(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendInventoryItemStatussFoundMessage(List<InventoryItemStatus> inventoryItemStatuss, int usedTicketId) {
		queryReturnVal.put(usedTicketId, inventoryItemStatuss);
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
	public boolean createInventoryItemStatus(HttpServletRequest request) {

		InventoryItemStatus inventoryItemStatusToBeAdded = new InventoryItemStatus();
		try {
			inventoryItemStatusToBeAdded = InventoryItemStatusMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createInventoryItemStatus(inventoryItemStatusToBeAdded);

	}

	/**
	 * creates a new InventoryItemStatus entry in the ofbiz database
	 * 
	 * @param inventoryItemStatusToBeAdded
	 *            the InventoryItemStatus thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createInventoryItemStatus(InventoryItemStatus inventoryItemStatusToBeAdded) {

		AddInventoryItemStatus com = new AddInventoryItemStatus(inventoryItemStatusToBeAdded);
		int usedTicketId;

		synchronized (InventoryItemStatusController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(InventoryItemStatusAdded.class,
				event -> sendInventoryItemStatusChangedMessage(((InventoryItemStatusAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateInventoryItemStatus(HttpServletRequest request) {

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

		InventoryItemStatus inventoryItemStatusToBeUpdated = new InventoryItemStatus();

		try {
			inventoryItemStatusToBeUpdated = InventoryItemStatusMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateInventoryItemStatus(inventoryItemStatusToBeUpdated);

	}

	/**
	 * Updates the InventoryItemStatus with the specific Id
	 * 
	 * @param inventoryItemStatusToBeUpdated the InventoryItemStatus thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateInventoryItemStatus(InventoryItemStatus inventoryItemStatusToBeUpdated) {

		UpdateInventoryItemStatus com = new UpdateInventoryItemStatus(inventoryItemStatusToBeUpdated);

		int usedTicketId;

		synchronized (InventoryItemStatusController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(InventoryItemStatusUpdated.class,
				event -> sendInventoryItemStatusChangedMessage(((InventoryItemStatusUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a InventoryItemStatus from the database
	 * 
	 * @param inventoryItemStatusId:
	 *            the id of the InventoryItemStatus thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deleteinventoryItemStatusById(@RequestParam(value = "inventoryItemStatusId") String inventoryItemStatusId) {

		DeleteInventoryItemStatus com = new DeleteInventoryItemStatus(inventoryItemStatusId);

		int usedTicketId;

		synchronized (InventoryItemStatusController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(InventoryItemStatusDeleted.class,
				event -> sendInventoryItemStatusChangedMessage(((InventoryItemStatusDeleted) event).isSuccess(), usedTicketId));

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

	public void sendInventoryItemStatusChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/inventoryItemStatus/\" plus one of the following: "
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
