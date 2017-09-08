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
import com.skytala.eCommerce.command.AddShipmentItemBilling;
import com.skytala.eCommerce.command.DeleteShipmentItemBilling;
import com.skytala.eCommerce.command.UpdateShipmentItemBilling;
import com.skytala.eCommerce.entity.ShipmentItemBilling;
import com.skytala.eCommerce.entity.ShipmentItemBillingMapper;
import com.skytala.eCommerce.event.ShipmentItemBillingAdded;
import com.skytala.eCommerce.event.ShipmentItemBillingDeleted;
import com.skytala.eCommerce.event.ShipmentItemBillingFound;
import com.skytala.eCommerce.event.ShipmentItemBillingUpdated;
import com.skytala.eCommerce.query.FindShipmentItemBillingsBy;

@RestController
@RequestMapping("/api/shipmentItemBilling")
public class ShipmentItemBillingController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<ShipmentItemBilling>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public ShipmentItemBillingController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a ShipmentItemBilling
	 * @return a List with the ShipmentItemBillings
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<ShipmentItemBilling> findShipmentItemBillingsBy(@RequestParam Map<String, String> allRequestParams) {

		FindShipmentItemBillingsBy query = new FindShipmentItemBillingsBy(allRequestParams);

		int usedTicketId;

		synchronized (ShipmentItemBillingController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ShipmentItemBillingFound.class,
				event -> sendShipmentItemBillingsFoundMessage(((ShipmentItemBillingFound) event).getShipmentItemBillings(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendShipmentItemBillingsFoundMessage(List<ShipmentItemBilling> shipmentItemBillings, int usedTicketId) {
		queryReturnVal.put(usedTicketId, shipmentItemBillings);
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
	public boolean createShipmentItemBilling(HttpServletRequest request) {

		ShipmentItemBilling shipmentItemBillingToBeAdded = new ShipmentItemBilling();
		try {
			shipmentItemBillingToBeAdded = ShipmentItemBillingMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createShipmentItemBilling(shipmentItemBillingToBeAdded);

	}

	/**
	 * creates a new ShipmentItemBilling entry in the ofbiz database
	 * 
	 * @param shipmentItemBillingToBeAdded
	 *            the ShipmentItemBilling thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createShipmentItemBilling(ShipmentItemBilling shipmentItemBillingToBeAdded) {

		AddShipmentItemBilling com = new AddShipmentItemBilling(shipmentItemBillingToBeAdded);
		int usedTicketId;

		synchronized (ShipmentItemBillingController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ShipmentItemBillingAdded.class,
				event -> sendShipmentItemBillingChangedMessage(((ShipmentItemBillingAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateShipmentItemBilling(HttpServletRequest request) {

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

		ShipmentItemBilling shipmentItemBillingToBeUpdated = new ShipmentItemBilling();

		try {
			shipmentItemBillingToBeUpdated = ShipmentItemBillingMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateShipmentItemBilling(shipmentItemBillingToBeUpdated);

	}

	/**
	 * Updates the ShipmentItemBilling with the specific Id
	 * 
	 * @param shipmentItemBillingToBeUpdated the ShipmentItemBilling thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateShipmentItemBilling(ShipmentItemBilling shipmentItemBillingToBeUpdated) {

		UpdateShipmentItemBilling com = new UpdateShipmentItemBilling(shipmentItemBillingToBeUpdated);

		int usedTicketId;

		synchronized (ShipmentItemBillingController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ShipmentItemBillingUpdated.class,
				event -> sendShipmentItemBillingChangedMessage(((ShipmentItemBillingUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a ShipmentItemBilling from the database
	 * 
	 * @param shipmentItemBillingId:
	 *            the id of the ShipmentItemBilling thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deleteshipmentItemBillingById(@RequestParam(value = "shipmentItemBillingId") String shipmentItemBillingId) {

		DeleteShipmentItemBilling com = new DeleteShipmentItemBilling(shipmentItemBillingId);

		int usedTicketId;

		synchronized (ShipmentItemBillingController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ShipmentItemBillingDeleted.class,
				event -> sendShipmentItemBillingChangedMessage(((ShipmentItemBillingDeleted) event).isSuccess(), usedTicketId));

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

	public void sendShipmentItemBillingChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/shipmentItemBilling/\" plus one of the following: "
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
