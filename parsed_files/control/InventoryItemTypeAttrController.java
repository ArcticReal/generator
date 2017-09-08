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
import com.skytala.eCommerce.command.AddInventoryItemTypeAttr;
import com.skytala.eCommerce.command.DeleteInventoryItemTypeAttr;
import com.skytala.eCommerce.command.UpdateInventoryItemTypeAttr;
import com.skytala.eCommerce.entity.InventoryItemTypeAttr;
import com.skytala.eCommerce.entity.InventoryItemTypeAttrMapper;
import com.skytala.eCommerce.event.InventoryItemTypeAttrAdded;
import com.skytala.eCommerce.event.InventoryItemTypeAttrDeleted;
import com.skytala.eCommerce.event.InventoryItemTypeAttrFound;
import com.skytala.eCommerce.event.InventoryItemTypeAttrUpdated;
import com.skytala.eCommerce.query.FindInventoryItemTypeAttrsBy;

@RestController
@RequestMapping("/api/inventoryItemTypeAttr")
public class InventoryItemTypeAttrController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<InventoryItemTypeAttr>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public InventoryItemTypeAttrController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a InventoryItemTypeAttr
	 * @return a List with the InventoryItemTypeAttrs
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<InventoryItemTypeAttr> findInventoryItemTypeAttrsBy(@RequestParam Map<String, String> allRequestParams) {

		FindInventoryItemTypeAttrsBy query = new FindInventoryItemTypeAttrsBy(allRequestParams);

		int usedTicketId;

		synchronized (InventoryItemTypeAttrController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(InventoryItemTypeAttrFound.class,
				event -> sendInventoryItemTypeAttrsFoundMessage(((InventoryItemTypeAttrFound) event).getInventoryItemTypeAttrs(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendInventoryItemTypeAttrsFoundMessage(List<InventoryItemTypeAttr> inventoryItemTypeAttrs, int usedTicketId) {
		queryReturnVal.put(usedTicketId, inventoryItemTypeAttrs);
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
	public boolean createInventoryItemTypeAttr(HttpServletRequest request) {

		InventoryItemTypeAttr inventoryItemTypeAttrToBeAdded = new InventoryItemTypeAttr();
		try {
			inventoryItemTypeAttrToBeAdded = InventoryItemTypeAttrMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createInventoryItemTypeAttr(inventoryItemTypeAttrToBeAdded);

	}

	/**
	 * creates a new InventoryItemTypeAttr entry in the ofbiz database
	 * 
	 * @param inventoryItemTypeAttrToBeAdded
	 *            the InventoryItemTypeAttr thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createInventoryItemTypeAttr(InventoryItemTypeAttr inventoryItemTypeAttrToBeAdded) {

		AddInventoryItemTypeAttr com = new AddInventoryItemTypeAttr(inventoryItemTypeAttrToBeAdded);
		int usedTicketId;

		synchronized (InventoryItemTypeAttrController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(InventoryItemTypeAttrAdded.class,
				event -> sendInventoryItemTypeAttrChangedMessage(((InventoryItemTypeAttrAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateInventoryItemTypeAttr(HttpServletRequest request) {

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

		InventoryItemTypeAttr inventoryItemTypeAttrToBeUpdated = new InventoryItemTypeAttr();

		try {
			inventoryItemTypeAttrToBeUpdated = InventoryItemTypeAttrMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateInventoryItemTypeAttr(inventoryItemTypeAttrToBeUpdated);

	}

	/**
	 * Updates the InventoryItemTypeAttr with the specific Id
	 * 
	 * @param inventoryItemTypeAttrToBeUpdated the InventoryItemTypeAttr thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateInventoryItemTypeAttr(InventoryItemTypeAttr inventoryItemTypeAttrToBeUpdated) {

		UpdateInventoryItemTypeAttr com = new UpdateInventoryItemTypeAttr(inventoryItemTypeAttrToBeUpdated);

		int usedTicketId;

		synchronized (InventoryItemTypeAttrController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(InventoryItemTypeAttrUpdated.class,
				event -> sendInventoryItemTypeAttrChangedMessage(((InventoryItemTypeAttrUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a InventoryItemTypeAttr from the database
	 * 
	 * @param inventoryItemTypeAttrId:
	 *            the id of the InventoryItemTypeAttr thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deleteinventoryItemTypeAttrById(@RequestParam(value = "inventoryItemTypeAttrId") String inventoryItemTypeAttrId) {

		DeleteInventoryItemTypeAttr com = new DeleteInventoryItemTypeAttr(inventoryItemTypeAttrId);

		int usedTicketId;

		synchronized (InventoryItemTypeAttrController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(InventoryItemTypeAttrDeleted.class,
				event -> sendInventoryItemTypeAttrChangedMessage(((InventoryItemTypeAttrDeleted) event).isSuccess(), usedTicketId));

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

	public void sendInventoryItemTypeAttrChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/inventoryItemTypeAttr/\" plus one of the following: "
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
