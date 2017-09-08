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
import com.skytala.eCommerce.command.AddShipmentItem;
import com.skytala.eCommerce.command.DeleteShipmentItem;
import com.skytala.eCommerce.command.UpdateShipmentItem;
import com.skytala.eCommerce.entity.ShipmentItem;
import com.skytala.eCommerce.entity.ShipmentItemMapper;
import com.skytala.eCommerce.event.ShipmentItemAdded;
import com.skytala.eCommerce.event.ShipmentItemDeleted;
import com.skytala.eCommerce.event.ShipmentItemFound;
import com.skytala.eCommerce.event.ShipmentItemUpdated;
import com.skytala.eCommerce.query.FindShipmentItemsBy;

@RestController
@RequestMapping("/api/shipmentItem")
public class ShipmentItemController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<ShipmentItem>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public ShipmentItemController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a ShipmentItem
	 * @return a List with the ShipmentItems
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<ShipmentItem> findShipmentItemsBy(@RequestParam Map<String, String> allRequestParams) {

		FindShipmentItemsBy query = new FindShipmentItemsBy(allRequestParams);

		int usedTicketId;

		synchronized (ShipmentItemController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ShipmentItemFound.class,
				event -> sendShipmentItemsFoundMessage(((ShipmentItemFound) event).getShipmentItems(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendShipmentItemsFoundMessage(List<ShipmentItem> shipmentItems, int usedTicketId) {
		queryReturnVal.put(usedTicketId, shipmentItems);
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
	public boolean createShipmentItem(HttpServletRequest request) {

		ShipmentItem shipmentItemToBeAdded = new ShipmentItem();
		try {
			shipmentItemToBeAdded = ShipmentItemMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createShipmentItem(shipmentItemToBeAdded);

	}

	/**
	 * creates a new ShipmentItem entry in the ofbiz database
	 * 
	 * @param shipmentItemToBeAdded
	 *            the ShipmentItem thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createShipmentItem(ShipmentItem shipmentItemToBeAdded) {

		AddShipmentItem com = new AddShipmentItem(shipmentItemToBeAdded);
		int usedTicketId;

		synchronized (ShipmentItemController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ShipmentItemAdded.class,
				event -> sendShipmentItemChangedMessage(((ShipmentItemAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateShipmentItem(HttpServletRequest request) {

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

		ShipmentItem shipmentItemToBeUpdated = new ShipmentItem();

		try {
			shipmentItemToBeUpdated = ShipmentItemMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateShipmentItem(shipmentItemToBeUpdated);

	}

	/**
	 * Updates the ShipmentItem with the specific Id
	 * 
	 * @param shipmentItemToBeUpdated the ShipmentItem thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateShipmentItem(ShipmentItem shipmentItemToBeUpdated) {

		UpdateShipmentItem com = new UpdateShipmentItem(shipmentItemToBeUpdated);

		int usedTicketId;

		synchronized (ShipmentItemController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ShipmentItemUpdated.class,
				event -> sendShipmentItemChangedMessage(((ShipmentItemUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a ShipmentItem from the database
	 * 
	 * @param shipmentItemId:
	 *            the id of the ShipmentItem thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deleteshipmentItemById(@RequestParam(value = "shipmentItemId") String shipmentItemId) {

		DeleteShipmentItem com = new DeleteShipmentItem(shipmentItemId);

		int usedTicketId;

		synchronized (ShipmentItemController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ShipmentItemDeleted.class,
				event -> sendShipmentItemChangedMessage(((ShipmentItemDeleted) event).isSuccess(), usedTicketId));

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

	public void sendShipmentItemChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/shipmentItem/\" plus one of the following: "
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
