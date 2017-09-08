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
import com.skytala.eCommerce.command.AddInventoryItemDetail;
import com.skytala.eCommerce.command.DeleteInventoryItemDetail;
import com.skytala.eCommerce.command.UpdateInventoryItemDetail;
import com.skytala.eCommerce.entity.InventoryItemDetail;
import com.skytala.eCommerce.entity.InventoryItemDetailMapper;
import com.skytala.eCommerce.event.InventoryItemDetailAdded;
import com.skytala.eCommerce.event.InventoryItemDetailDeleted;
import com.skytala.eCommerce.event.InventoryItemDetailFound;
import com.skytala.eCommerce.event.InventoryItemDetailUpdated;
import com.skytala.eCommerce.query.FindInventoryItemDetailsBy;

@RestController
@RequestMapping("/api/inventoryItemDetail")
public class InventoryItemDetailController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<InventoryItemDetail>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public InventoryItemDetailController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a InventoryItemDetail
	 * @return a List with the InventoryItemDetails
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<InventoryItemDetail> findInventoryItemDetailsBy(@RequestParam Map<String, String> allRequestParams) {

		FindInventoryItemDetailsBy query = new FindInventoryItemDetailsBy(allRequestParams);

		int usedTicketId;

		synchronized (InventoryItemDetailController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(InventoryItemDetailFound.class,
				event -> sendInventoryItemDetailsFoundMessage(((InventoryItemDetailFound) event).getInventoryItemDetails(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendInventoryItemDetailsFoundMessage(List<InventoryItemDetail> inventoryItemDetails, int usedTicketId) {
		queryReturnVal.put(usedTicketId, inventoryItemDetails);
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
	public boolean createInventoryItemDetail(HttpServletRequest request) {

		InventoryItemDetail inventoryItemDetailToBeAdded = new InventoryItemDetail();
		try {
			inventoryItemDetailToBeAdded = InventoryItemDetailMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createInventoryItemDetail(inventoryItemDetailToBeAdded);

	}

	/**
	 * creates a new InventoryItemDetail entry in the ofbiz database
	 * 
	 * @param inventoryItemDetailToBeAdded
	 *            the InventoryItemDetail thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createInventoryItemDetail(InventoryItemDetail inventoryItemDetailToBeAdded) {

		AddInventoryItemDetail com = new AddInventoryItemDetail(inventoryItemDetailToBeAdded);
		int usedTicketId;

		synchronized (InventoryItemDetailController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(InventoryItemDetailAdded.class,
				event -> sendInventoryItemDetailChangedMessage(((InventoryItemDetailAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateInventoryItemDetail(HttpServletRequest request) {

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

		InventoryItemDetail inventoryItemDetailToBeUpdated = new InventoryItemDetail();

		try {
			inventoryItemDetailToBeUpdated = InventoryItemDetailMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateInventoryItemDetail(inventoryItemDetailToBeUpdated);

	}

	/**
	 * Updates the InventoryItemDetail with the specific Id
	 * 
	 * @param inventoryItemDetailToBeUpdated the InventoryItemDetail thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateInventoryItemDetail(InventoryItemDetail inventoryItemDetailToBeUpdated) {

		UpdateInventoryItemDetail com = new UpdateInventoryItemDetail(inventoryItemDetailToBeUpdated);

		int usedTicketId;

		synchronized (InventoryItemDetailController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(InventoryItemDetailUpdated.class,
				event -> sendInventoryItemDetailChangedMessage(((InventoryItemDetailUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a InventoryItemDetail from the database
	 * 
	 * @param inventoryItemDetailId:
	 *            the id of the InventoryItemDetail thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deleteinventoryItemDetailById(@RequestParam(value = "inventoryItemDetailId") String inventoryItemDetailId) {

		DeleteInventoryItemDetail com = new DeleteInventoryItemDetail(inventoryItemDetailId);

		int usedTicketId;

		synchronized (InventoryItemDetailController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(InventoryItemDetailDeleted.class,
				event -> sendInventoryItemDetailChangedMessage(((InventoryItemDetailDeleted) event).isSuccess(), usedTicketId));

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

	public void sendInventoryItemDetailChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/inventoryItemDetail/\" plus one of the following: "
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
