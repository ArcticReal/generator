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
import com.skytala.eCommerce.command.AddInventoryItemVariance;
import com.skytala.eCommerce.command.DeleteInventoryItemVariance;
import com.skytala.eCommerce.command.UpdateInventoryItemVariance;
import com.skytala.eCommerce.entity.InventoryItemVariance;
import com.skytala.eCommerce.entity.InventoryItemVarianceMapper;
import com.skytala.eCommerce.event.InventoryItemVarianceAdded;
import com.skytala.eCommerce.event.InventoryItemVarianceDeleted;
import com.skytala.eCommerce.event.InventoryItemVarianceFound;
import com.skytala.eCommerce.event.InventoryItemVarianceUpdated;
import com.skytala.eCommerce.query.FindInventoryItemVariancesBy;

@RestController
@RequestMapping("/api/inventoryItemVariance")
public class InventoryItemVarianceController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<InventoryItemVariance>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public InventoryItemVarianceController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a InventoryItemVariance
	 * @return a List with the InventoryItemVariances
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<InventoryItemVariance> findInventoryItemVariancesBy(@RequestParam Map<String, String> allRequestParams) {

		FindInventoryItemVariancesBy query = new FindInventoryItemVariancesBy(allRequestParams);

		int usedTicketId;

		synchronized (InventoryItemVarianceController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(InventoryItemVarianceFound.class,
				event -> sendInventoryItemVariancesFoundMessage(((InventoryItemVarianceFound) event).getInventoryItemVariances(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendInventoryItemVariancesFoundMessage(List<InventoryItemVariance> inventoryItemVariances, int usedTicketId) {
		queryReturnVal.put(usedTicketId, inventoryItemVariances);
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
	public boolean createInventoryItemVariance(HttpServletRequest request) {

		InventoryItemVariance inventoryItemVarianceToBeAdded = new InventoryItemVariance();
		try {
			inventoryItemVarianceToBeAdded = InventoryItemVarianceMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createInventoryItemVariance(inventoryItemVarianceToBeAdded);

	}

	/**
	 * creates a new InventoryItemVariance entry in the ofbiz database
	 * 
	 * @param inventoryItemVarianceToBeAdded
	 *            the InventoryItemVariance thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createInventoryItemVariance(InventoryItemVariance inventoryItemVarianceToBeAdded) {

		AddInventoryItemVariance com = new AddInventoryItemVariance(inventoryItemVarianceToBeAdded);
		int usedTicketId;

		synchronized (InventoryItemVarianceController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(InventoryItemVarianceAdded.class,
				event -> sendInventoryItemVarianceChangedMessage(((InventoryItemVarianceAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateInventoryItemVariance(HttpServletRequest request) {

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

		InventoryItemVariance inventoryItemVarianceToBeUpdated = new InventoryItemVariance();

		try {
			inventoryItemVarianceToBeUpdated = InventoryItemVarianceMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateInventoryItemVariance(inventoryItemVarianceToBeUpdated);

	}

	/**
	 * Updates the InventoryItemVariance with the specific Id
	 * 
	 * @param inventoryItemVarianceToBeUpdated the InventoryItemVariance thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateInventoryItemVariance(InventoryItemVariance inventoryItemVarianceToBeUpdated) {

		UpdateInventoryItemVariance com = new UpdateInventoryItemVariance(inventoryItemVarianceToBeUpdated);

		int usedTicketId;

		synchronized (InventoryItemVarianceController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(InventoryItemVarianceUpdated.class,
				event -> sendInventoryItemVarianceChangedMessage(((InventoryItemVarianceUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a InventoryItemVariance from the database
	 * 
	 * @param inventoryItemVarianceId:
	 *            the id of the InventoryItemVariance thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deleteinventoryItemVarianceById(@RequestParam(value = "inventoryItemVarianceId") String inventoryItemVarianceId) {

		DeleteInventoryItemVariance com = new DeleteInventoryItemVariance(inventoryItemVarianceId);

		int usedTicketId;

		synchronized (InventoryItemVarianceController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(InventoryItemVarianceDeleted.class,
				event -> sendInventoryItemVarianceChangedMessage(((InventoryItemVarianceDeleted) event).isSuccess(), usedTicketId));

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

	public void sendInventoryItemVarianceChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/inventoryItemVariance/\" plus one of the following: "
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
