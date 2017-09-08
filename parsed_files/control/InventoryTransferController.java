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
import com.skytala.eCommerce.command.AddInventoryTransfer;
import com.skytala.eCommerce.command.DeleteInventoryTransfer;
import com.skytala.eCommerce.command.UpdateInventoryTransfer;
import com.skytala.eCommerce.entity.InventoryTransfer;
import com.skytala.eCommerce.entity.InventoryTransferMapper;
import com.skytala.eCommerce.event.InventoryTransferAdded;
import com.skytala.eCommerce.event.InventoryTransferDeleted;
import com.skytala.eCommerce.event.InventoryTransferFound;
import com.skytala.eCommerce.event.InventoryTransferUpdated;
import com.skytala.eCommerce.query.FindInventoryTransfersBy;

@RestController
@RequestMapping("/api/inventoryTransfer")
public class InventoryTransferController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<InventoryTransfer>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public InventoryTransferController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a InventoryTransfer
	 * @return a List with the InventoryTransfers
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<InventoryTransfer> findInventoryTransfersBy(@RequestParam Map<String, String> allRequestParams) {

		FindInventoryTransfersBy query = new FindInventoryTransfersBy(allRequestParams);

		int usedTicketId;

		synchronized (InventoryTransferController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(InventoryTransferFound.class,
				event -> sendInventoryTransfersFoundMessage(((InventoryTransferFound) event).getInventoryTransfers(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendInventoryTransfersFoundMessage(List<InventoryTransfer> inventoryTransfers, int usedTicketId) {
		queryReturnVal.put(usedTicketId, inventoryTransfers);
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
	public boolean createInventoryTransfer(HttpServletRequest request) {

		InventoryTransfer inventoryTransferToBeAdded = new InventoryTransfer();
		try {
			inventoryTransferToBeAdded = InventoryTransferMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createInventoryTransfer(inventoryTransferToBeAdded);

	}

	/**
	 * creates a new InventoryTransfer entry in the ofbiz database
	 * 
	 * @param inventoryTransferToBeAdded
	 *            the InventoryTransfer thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createInventoryTransfer(InventoryTransfer inventoryTransferToBeAdded) {

		AddInventoryTransfer com = new AddInventoryTransfer(inventoryTransferToBeAdded);
		int usedTicketId;

		synchronized (InventoryTransferController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(InventoryTransferAdded.class,
				event -> sendInventoryTransferChangedMessage(((InventoryTransferAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateInventoryTransfer(HttpServletRequest request) {

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

		InventoryTransfer inventoryTransferToBeUpdated = new InventoryTransfer();

		try {
			inventoryTransferToBeUpdated = InventoryTransferMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateInventoryTransfer(inventoryTransferToBeUpdated);

	}

	/**
	 * Updates the InventoryTransfer with the specific Id
	 * 
	 * @param inventoryTransferToBeUpdated the InventoryTransfer thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateInventoryTransfer(InventoryTransfer inventoryTransferToBeUpdated) {

		UpdateInventoryTransfer com = new UpdateInventoryTransfer(inventoryTransferToBeUpdated);

		int usedTicketId;

		synchronized (InventoryTransferController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(InventoryTransferUpdated.class,
				event -> sendInventoryTransferChangedMessage(((InventoryTransferUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a InventoryTransfer from the database
	 * 
	 * @param inventoryTransferId:
	 *            the id of the InventoryTransfer thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deleteinventoryTransferById(@RequestParam(value = "inventoryTransferId") String inventoryTransferId) {

		DeleteInventoryTransfer com = new DeleteInventoryTransfer(inventoryTransferId);

		int usedTicketId;

		synchronized (InventoryTransferController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(InventoryTransferDeleted.class,
				event -> sendInventoryTransferChangedMessage(((InventoryTransferDeleted) event).isSuccess(), usedTicketId));

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

	public void sendInventoryTransferChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/inventoryTransfer/\" plus one of the following: "
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
