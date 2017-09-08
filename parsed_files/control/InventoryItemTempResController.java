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
import com.skytala.eCommerce.command.AddInventoryItemTempRes;
import com.skytala.eCommerce.command.DeleteInventoryItemTempRes;
import com.skytala.eCommerce.command.UpdateInventoryItemTempRes;
import com.skytala.eCommerce.entity.InventoryItemTempRes;
import com.skytala.eCommerce.entity.InventoryItemTempResMapper;
import com.skytala.eCommerce.event.InventoryItemTempResAdded;
import com.skytala.eCommerce.event.InventoryItemTempResDeleted;
import com.skytala.eCommerce.event.InventoryItemTempResFound;
import com.skytala.eCommerce.event.InventoryItemTempResUpdated;
import com.skytala.eCommerce.query.FindInventoryItemTempRessBy;

@RestController
@RequestMapping("/api/inventoryItemTempRes")
public class InventoryItemTempResController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<InventoryItemTempRes>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public InventoryItemTempResController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a InventoryItemTempRes
	 * @return a List with the InventoryItemTempRess
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<InventoryItemTempRes> findInventoryItemTempRessBy(@RequestParam Map<String, String> allRequestParams) {

		FindInventoryItemTempRessBy query = new FindInventoryItemTempRessBy(allRequestParams);

		int usedTicketId;

		synchronized (InventoryItemTempResController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(InventoryItemTempResFound.class,
				event -> sendInventoryItemTempRessFoundMessage(((InventoryItemTempResFound) event).getInventoryItemTempRess(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendInventoryItemTempRessFoundMessage(List<InventoryItemTempRes> inventoryItemTempRess, int usedTicketId) {
		queryReturnVal.put(usedTicketId, inventoryItemTempRess);
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
	public boolean createInventoryItemTempRes(HttpServletRequest request) {

		InventoryItemTempRes inventoryItemTempResToBeAdded = new InventoryItemTempRes();
		try {
			inventoryItemTempResToBeAdded = InventoryItemTempResMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createInventoryItemTempRes(inventoryItemTempResToBeAdded);

	}

	/**
	 * creates a new InventoryItemTempRes entry in the ofbiz database
	 * 
	 * @param inventoryItemTempResToBeAdded
	 *            the InventoryItemTempRes thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createInventoryItemTempRes(InventoryItemTempRes inventoryItemTempResToBeAdded) {

		AddInventoryItemTempRes com = new AddInventoryItemTempRes(inventoryItemTempResToBeAdded);
		int usedTicketId;

		synchronized (InventoryItemTempResController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(InventoryItemTempResAdded.class,
				event -> sendInventoryItemTempResChangedMessage(((InventoryItemTempResAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateInventoryItemTempRes(HttpServletRequest request) {

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

		InventoryItemTempRes inventoryItemTempResToBeUpdated = new InventoryItemTempRes();

		try {
			inventoryItemTempResToBeUpdated = InventoryItemTempResMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateInventoryItemTempRes(inventoryItemTempResToBeUpdated);

	}

	/**
	 * Updates the InventoryItemTempRes with the specific Id
	 * 
	 * @param inventoryItemTempResToBeUpdated the InventoryItemTempRes thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateInventoryItemTempRes(InventoryItemTempRes inventoryItemTempResToBeUpdated) {

		UpdateInventoryItemTempRes com = new UpdateInventoryItemTempRes(inventoryItemTempResToBeUpdated);

		int usedTicketId;

		synchronized (InventoryItemTempResController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(InventoryItemTempResUpdated.class,
				event -> sendInventoryItemTempResChangedMessage(((InventoryItemTempResUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a InventoryItemTempRes from the database
	 * 
	 * @param inventoryItemTempResId:
	 *            the id of the InventoryItemTempRes thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deleteinventoryItemTempResById(@RequestParam(value = "inventoryItemTempResId") String inventoryItemTempResId) {

		DeleteInventoryItemTempRes com = new DeleteInventoryItemTempRes(inventoryItemTempResId);

		int usedTicketId;

		synchronized (InventoryItemTempResController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(InventoryItemTempResDeleted.class,
				event -> sendInventoryItemTempResChangedMessage(((InventoryItemTempResDeleted) event).isSuccess(), usedTicketId));

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

	public void sendInventoryItemTempResChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/inventoryItemTempRes/\" plus one of the following: "
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
