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
import com.skytala.eCommerce.command.AddPhysicalInventory;
import com.skytala.eCommerce.command.DeletePhysicalInventory;
import com.skytala.eCommerce.command.UpdatePhysicalInventory;
import com.skytala.eCommerce.entity.PhysicalInventory;
import com.skytala.eCommerce.entity.PhysicalInventoryMapper;
import com.skytala.eCommerce.event.PhysicalInventoryAdded;
import com.skytala.eCommerce.event.PhysicalInventoryDeleted;
import com.skytala.eCommerce.event.PhysicalInventoryFound;
import com.skytala.eCommerce.event.PhysicalInventoryUpdated;
import com.skytala.eCommerce.query.FindPhysicalInventorysBy;

@RestController
@RequestMapping("/api/physicalInventory")
public class PhysicalInventoryController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<PhysicalInventory>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public PhysicalInventoryController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a PhysicalInventory
	 * @return a List with the PhysicalInventorys
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<PhysicalInventory> findPhysicalInventorysBy(@RequestParam Map<String, String> allRequestParams) {

		FindPhysicalInventorysBy query = new FindPhysicalInventorysBy(allRequestParams);

		int usedTicketId;

		synchronized (PhysicalInventoryController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(PhysicalInventoryFound.class,
				event -> sendPhysicalInventorysFoundMessage(((PhysicalInventoryFound) event).getPhysicalInventorys(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendPhysicalInventorysFoundMessage(List<PhysicalInventory> physicalInventorys, int usedTicketId) {
		queryReturnVal.put(usedTicketId, physicalInventorys);
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
	public boolean createPhysicalInventory(HttpServletRequest request) {

		PhysicalInventory physicalInventoryToBeAdded = new PhysicalInventory();
		try {
			physicalInventoryToBeAdded = PhysicalInventoryMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createPhysicalInventory(physicalInventoryToBeAdded);

	}

	/**
	 * creates a new PhysicalInventory entry in the ofbiz database
	 * 
	 * @param physicalInventoryToBeAdded
	 *            the PhysicalInventory thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createPhysicalInventory(PhysicalInventory physicalInventoryToBeAdded) {

		AddPhysicalInventory com = new AddPhysicalInventory(physicalInventoryToBeAdded);
		int usedTicketId;

		synchronized (PhysicalInventoryController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(PhysicalInventoryAdded.class,
				event -> sendPhysicalInventoryChangedMessage(((PhysicalInventoryAdded) event).isSuccess(), usedTicketId));

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
	public boolean updatePhysicalInventory(HttpServletRequest request) {

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

		PhysicalInventory physicalInventoryToBeUpdated = new PhysicalInventory();

		try {
			physicalInventoryToBeUpdated = PhysicalInventoryMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updatePhysicalInventory(physicalInventoryToBeUpdated);

	}

	/**
	 * Updates the PhysicalInventory with the specific Id
	 * 
	 * @param physicalInventoryToBeUpdated the PhysicalInventory thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updatePhysicalInventory(PhysicalInventory physicalInventoryToBeUpdated) {

		UpdatePhysicalInventory com = new UpdatePhysicalInventory(physicalInventoryToBeUpdated);

		int usedTicketId;

		synchronized (PhysicalInventoryController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(PhysicalInventoryUpdated.class,
				event -> sendPhysicalInventoryChangedMessage(((PhysicalInventoryUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a PhysicalInventory from the database
	 * 
	 * @param physicalInventoryId:
	 *            the id of the PhysicalInventory thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deletephysicalInventoryById(@RequestParam(value = "physicalInventoryId") String physicalInventoryId) {

		DeletePhysicalInventory com = new DeletePhysicalInventory(physicalInventoryId);

		int usedTicketId;

		synchronized (PhysicalInventoryController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(PhysicalInventoryDeleted.class,
				event -> sendPhysicalInventoryChangedMessage(((PhysicalInventoryDeleted) event).isSuccess(), usedTicketId));

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

	public void sendPhysicalInventoryChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/physicalInventory/\" plus one of the following: "
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
