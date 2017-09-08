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
import com.skytala.eCommerce.command.AddInventoryItemLabelAppl;
import com.skytala.eCommerce.command.DeleteInventoryItemLabelAppl;
import com.skytala.eCommerce.command.UpdateInventoryItemLabelAppl;
import com.skytala.eCommerce.entity.InventoryItemLabelAppl;
import com.skytala.eCommerce.entity.InventoryItemLabelApplMapper;
import com.skytala.eCommerce.event.InventoryItemLabelApplAdded;
import com.skytala.eCommerce.event.InventoryItemLabelApplDeleted;
import com.skytala.eCommerce.event.InventoryItemLabelApplFound;
import com.skytala.eCommerce.event.InventoryItemLabelApplUpdated;
import com.skytala.eCommerce.query.FindInventoryItemLabelApplsBy;

@RestController
@RequestMapping("/api/inventoryItemLabelAppl")
public class InventoryItemLabelApplController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<InventoryItemLabelAppl>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public InventoryItemLabelApplController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a InventoryItemLabelAppl
	 * @return a List with the InventoryItemLabelAppls
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<InventoryItemLabelAppl> findInventoryItemLabelApplsBy(@RequestParam Map<String, String> allRequestParams) {

		FindInventoryItemLabelApplsBy query = new FindInventoryItemLabelApplsBy(allRequestParams);

		int usedTicketId;

		synchronized (InventoryItemLabelApplController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(InventoryItemLabelApplFound.class,
				event -> sendInventoryItemLabelApplsFoundMessage(((InventoryItemLabelApplFound) event).getInventoryItemLabelAppls(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendInventoryItemLabelApplsFoundMessage(List<InventoryItemLabelAppl> inventoryItemLabelAppls, int usedTicketId) {
		queryReturnVal.put(usedTicketId, inventoryItemLabelAppls);
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
	public boolean createInventoryItemLabelAppl(HttpServletRequest request) {

		InventoryItemLabelAppl inventoryItemLabelApplToBeAdded = new InventoryItemLabelAppl();
		try {
			inventoryItemLabelApplToBeAdded = InventoryItemLabelApplMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createInventoryItemLabelAppl(inventoryItemLabelApplToBeAdded);

	}

	/**
	 * creates a new InventoryItemLabelAppl entry in the ofbiz database
	 * 
	 * @param inventoryItemLabelApplToBeAdded
	 *            the InventoryItemLabelAppl thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createInventoryItemLabelAppl(InventoryItemLabelAppl inventoryItemLabelApplToBeAdded) {

		AddInventoryItemLabelAppl com = new AddInventoryItemLabelAppl(inventoryItemLabelApplToBeAdded);
		int usedTicketId;

		synchronized (InventoryItemLabelApplController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(InventoryItemLabelApplAdded.class,
				event -> sendInventoryItemLabelApplChangedMessage(((InventoryItemLabelApplAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateInventoryItemLabelAppl(HttpServletRequest request) {

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

		InventoryItemLabelAppl inventoryItemLabelApplToBeUpdated = new InventoryItemLabelAppl();

		try {
			inventoryItemLabelApplToBeUpdated = InventoryItemLabelApplMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateInventoryItemLabelAppl(inventoryItemLabelApplToBeUpdated);

	}

	/**
	 * Updates the InventoryItemLabelAppl with the specific Id
	 * 
	 * @param inventoryItemLabelApplToBeUpdated the InventoryItemLabelAppl thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateInventoryItemLabelAppl(InventoryItemLabelAppl inventoryItemLabelApplToBeUpdated) {

		UpdateInventoryItemLabelAppl com = new UpdateInventoryItemLabelAppl(inventoryItemLabelApplToBeUpdated);

		int usedTicketId;

		synchronized (InventoryItemLabelApplController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(InventoryItemLabelApplUpdated.class,
				event -> sendInventoryItemLabelApplChangedMessage(((InventoryItemLabelApplUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a InventoryItemLabelAppl from the database
	 * 
	 * @param inventoryItemLabelApplId:
	 *            the id of the InventoryItemLabelAppl thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deleteinventoryItemLabelApplById(@RequestParam(value = "inventoryItemLabelApplId") String inventoryItemLabelApplId) {

		DeleteInventoryItemLabelAppl com = new DeleteInventoryItemLabelAppl(inventoryItemLabelApplId);

		int usedTicketId;

		synchronized (InventoryItemLabelApplController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(InventoryItemLabelApplDeleted.class,
				event -> sendInventoryItemLabelApplChangedMessage(((InventoryItemLabelApplDeleted) event).isSuccess(), usedTicketId));

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

	public void sendInventoryItemLabelApplChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/inventoryItemLabelAppl/\" plus one of the following: "
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
