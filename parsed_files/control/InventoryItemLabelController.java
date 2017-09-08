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
import com.skytala.eCommerce.command.AddInventoryItemLabel;
import com.skytala.eCommerce.command.DeleteInventoryItemLabel;
import com.skytala.eCommerce.command.UpdateInventoryItemLabel;
import com.skytala.eCommerce.entity.InventoryItemLabel;
import com.skytala.eCommerce.entity.InventoryItemLabelMapper;
import com.skytala.eCommerce.event.InventoryItemLabelAdded;
import com.skytala.eCommerce.event.InventoryItemLabelDeleted;
import com.skytala.eCommerce.event.InventoryItemLabelFound;
import com.skytala.eCommerce.event.InventoryItemLabelUpdated;
import com.skytala.eCommerce.query.FindInventoryItemLabelsBy;

@RestController
@RequestMapping("/api/inventoryItemLabel")
public class InventoryItemLabelController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<InventoryItemLabel>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public InventoryItemLabelController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a InventoryItemLabel
	 * @return a List with the InventoryItemLabels
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<InventoryItemLabel> findInventoryItemLabelsBy(@RequestParam Map<String, String> allRequestParams) {

		FindInventoryItemLabelsBy query = new FindInventoryItemLabelsBy(allRequestParams);

		int usedTicketId;

		synchronized (InventoryItemLabelController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(InventoryItemLabelFound.class,
				event -> sendInventoryItemLabelsFoundMessage(((InventoryItemLabelFound) event).getInventoryItemLabels(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendInventoryItemLabelsFoundMessage(List<InventoryItemLabel> inventoryItemLabels, int usedTicketId) {
		queryReturnVal.put(usedTicketId, inventoryItemLabels);
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
	public boolean createInventoryItemLabel(HttpServletRequest request) {

		InventoryItemLabel inventoryItemLabelToBeAdded = new InventoryItemLabel();
		try {
			inventoryItemLabelToBeAdded = InventoryItemLabelMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createInventoryItemLabel(inventoryItemLabelToBeAdded);

	}

	/**
	 * creates a new InventoryItemLabel entry in the ofbiz database
	 * 
	 * @param inventoryItemLabelToBeAdded
	 *            the InventoryItemLabel thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createInventoryItemLabel(InventoryItemLabel inventoryItemLabelToBeAdded) {

		AddInventoryItemLabel com = new AddInventoryItemLabel(inventoryItemLabelToBeAdded);
		int usedTicketId;

		synchronized (InventoryItemLabelController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(InventoryItemLabelAdded.class,
				event -> sendInventoryItemLabelChangedMessage(((InventoryItemLabelAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateInventoryItemLabel(HttpServletRequest request) {

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

		InventoryItemLabel inventoryItemLabelToBeUpdated = new InventoryItemLabel();

		try {
			inventoryItemLabelToBeUpdated = InventoryItemLabelMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateInventoryItemLabel(inventoryItemLabelToBeUpdated);

	}

	/**
	 * Updates the InventoryItemLabel with the specific Id
	 * 
	 * @param inventoryItemLabelToBeUpdated the InventoryItemLabel thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateInventoryItemLabel(InventoryItemLabel inventoryItemLabelToBeUpdated) {

		UpdateInventoryItemLabel com = new UpdateInventoryItemLabel(inventoryItemLabelToBeUpdated);

		int usedTicketId;

		synchronized (InventoryItemLabelController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(InventoryItemLabelUpdated.class,
				event -> sendInventoryItemLabelChangedMessage(((InventoryItemLabelUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a InventoryItemLabel from the database
	 * 
	 * @param inventoryItemLabelId:
	 *            the id of the InventoryItemLabel thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deleteinventoryItemLabelById(@RequestParam(value = "inventoryItemLabelId") String inventoryItemLabelId) {

		DeleteInventoryItemLabel com = new DeleteInventoryItemLabel(inventoryItemLabelId);

		int usedTicketId;

		synchronized (InventoryItemLabelController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(InventoryItemLabelDeleted.class,
				event -> sendInventoryItemLabelChangedMessage(((InventoryItemLabelDeleted) event).isSuccess(), usedTicketId));

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

	public void sendInventoryItemLabelChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/inventoryItemLabel/\" plus one of the following: "
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
