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
import com.skytala.eCommerce.command.AddInventoryItemAttribute;
import com.skytala.eCommerce.command.DeleteInventoryItemAttribute;
import com.skytala.eCommerce.command.UpdateInventoryItemAttribute;
import com.skytala.eCommerce.entity.InventoryItemAttribute;
import com.skytala.eCommerce.entity.InventoryItemAttributeMapper;
import com.skytala.eCommerce.event.InventoryItemAttributeAdded;
import com.skytala.eCommerce.event.InventoryItemAttributeDeleted;
import com.skytala.eCommerce.event.InventoryItemAttributeFound;
import com.skytala.eCommerce.event.InventoryItemAttributeUpdated;
import com.skytala.eCommerce.query.FindInventoryItemAttributesBy;

@RestController
@RequestMapping("/api/inventoryItemAttribute")
public class InventoryItemAttributeController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<InventoryItemAttribute>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public InventoryItemAttributeController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a InventoryItemAttribute
	 * @return a List with the InventoryItemAttributes
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<InventoryItemAttribute> findInventoryItemAttributesBy(@RequestParam Map<String, String> allRequestParams) {

		FindInventoryItemAttributesBy query = new FindInventoryItemAttributesBy(allRequestParams);

		int usedTicketId;

		synchronized (InventoryItemAttributeController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(InventoryItemAttributeFound.class,
				event -> sendInventoryItemAttributesFoundMessage(((InventoryItemAttributeFound) event).getInventoryItemAttributes(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendInventoryItemAttributesFoundMessage(List<InventoryItemAttribute> inventoryItemAttributes, int usedTicketId) {
		queryReturnVal.put(usedTicketId, inventoryItemAttributes);
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
	public boolean createInventoryItemAttribute(HttpServletRequest request) {

		InventoryItemAttribute inventoryItemAttributeToBeAdded = new InventoryItemAttribute();
		try {
			inventoryItemAttributeToBeAdded = InventoryItemAttributeMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createInventoryItemAttribute(inventoryItemAttributeToBeAdded);

	}

	/**
	 * creates a new InventoryItemAttribute entry in the ofbiz database
	 * 
	 * @param inventoryItemAttributeToBeAdded
	 *            the InventoryItemAttribute thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createInventoryItemAttribute(InventoryItemAttribute inventoryItemAttributeToBeAdded) {

		AddInventoryItemAttribute com = new AddInventoryItemAttribute(inventoryItemAttributeToBeAdded);
		int usedTicketId;

		synchronized (InventoryItemAttributeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(InventoryItemAttributeAdded.class,
				event -> sendInventoryItemAttributeChangedMessage(((InventoryItemAttributeAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateInventoryItemAttribute(HttpServletRequest request) {

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

		InventoryItemAttribute inventoryItemAttributeToBeUpdated = new InventoryItemAttribute();

		try {
			inventoryItemAttributeToBeUpdated = InventoryItemAttributeMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateInventoryItemAttribute(inventoryItemAttributeToBeUpdated);

	}

	/**
	 * Updates the InventoryItemAttribute with the specific Id
	 * 
	 * @param inventoryItemAttributeToBeUpdated the InventoryItemAttribute thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateInventoryItemAttribute(InventoryItemAttribute inventoryItemAttributeToBeUpdated) {

		UpdateInventoryItemAttribute com = new UpdateInventoryItemAttribute(inventoryItemAttributeToBeUpdated);

		int usedTicketId;

		synchronized (InventoryItemAttributeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(InventoryItemAttributeUpdated.class,
				event -> sendInventoryItemAttributeChangedMessage(((InventoryItemAttributeUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a InventoryItemAttribute from the database
	 * 
	 * @param inventoryItemAttributeId:
	 *            the id of the InventoryItemAttribute thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deleteinventoryItemAttributeById(@RequestParam(value = "inventoryItemAttributeId") String inventoryItemAttributeId) {

		DeleteInventoryItemAttribute com = new DeleteInventoryItemAttribute(inventoryItemAttributeId);

		int usedTicketId;

		synchronized (InventoryItemAttributeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(InventoryItemAttributeDeleted.class,
				event -> sendInventoryItemAttributeChangedMessage(((InventoryItemAttributeDeleted) event).isSuccess(), usedTicketId));

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

	public void sendInventoryItemAttributeChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/inventoryItemAttribute/\" plus one of the following: "
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
