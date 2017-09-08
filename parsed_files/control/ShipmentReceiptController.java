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
import com.skytala.eCommerce.command.AddShipmentReceipt;
import com.skytala.eCommerce.command.DeleteShipmentReceipt;
import com.skytala.eCommerce.command.UpdateShipmentReceipt;
import com.skytala.eCommerce.entity.ShipmentReceipt;
import com.skytala.eCommerce.entity.ShipmentReceiptMapper;
import com.skytala.eCommerce.event.ShipmentReceiptAdded;
import com.skytala.eCommerce.event.ShipmentReceiptDeleted;
import com.skytala.eCommerce.event.ShipmentReceiptFound;
import com.skytala.eCommerce.event.ShipmentReceiptUpdated;
import com.skytala.eCommerce.query.FindShipmentReceiptsBy;

@RestController
@RequestMapping("/api/shipmentReceipt")
public class ShipmentReceiptController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<ShipmentReceipt>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public ShipmentReceiptController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a ShipmentReceipt
	 * @return a List with the ShipmentReceipts
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<ShipmentReceipt> findShipmentReceiptsBy(@RequestParam Map<String, String> allRequestParams) {

		FindShipmentReceiptsBy query = new FindShipmentReceiptsBy(allRequestParams);

		int usedTicketId;

		synchronized (ShipmentReceiptController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ShipmentReceiptFound.class,
				event -> sendShipmentReceiptsFoundMessage(((ShipmentReceiptFound) event).getShipmentReceipts(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendShipmentReceiptsFoundMessage(List<ShipmentReceipt> shipmentReceipts, int usedTicketId) {
		queryReturnVal.put(usedTicketId, shipmentReceipts);
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
	public boolean createShipmentReceipt(HttpServletRequest request) {

		ShipmentReceipt shipmentReceiptToBeAdded = new ShipmentReceipt();
		try {
			shipmentReceiptToBeAdded = ShipmentReceiptMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createShipmentReceipt(shipmentReceiptToBeAdded);

	}

	/**
	 * creates a new ShipmentReceipt entry in the ofbiz database
	 * 
	 * @param shipmentReceiptToBeAdded
	 *            the ShipmentReceipt thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createShipmentReceipt(ShipmentReceipt shipmentReceiptToBeAdded) {

		AddShipmentReceipt com = new AddShipmentReceipt(shipmentReceiptToBeAdded);
		int usedTicketId;

		synchronized (ShipmentReceiptController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ShipmentReceiptAdded.class,
				event -> sendShipmentReceiptChangedMessage(((ShipmentReceiptAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateShipmentReceipt(HttpServletRequest request) {

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

		ShipmentReceipt shipmentReceiptToBeUpdated = new ShipmentReceipt();

		try {
			shipmentReceiptToBeUpdated = ShipmentReceiptMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateShipmentReceipt(shipmentReceiptToBeUpdated);

	}

	/**
	 * Updates the ShipmentReceipt with the specific Id
	 * 
	 * @param shipmentReceiptToBeUpdated the ShipmentReceipt thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateShipmentReceipt(ShipmentReceipt shipmentReceiptToBeUpdated) {

		UpdateShipmentReceipt com = new UpdateShipmentReceipt(shipmentReceiptToBeUpdated);

		int usedTicketId;

		synchronized (ShipmentReceiptController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ShipmentReceiptUpdated.class,
				event -> sendShipmentReceiptChangedMessage(((ShipmentReceiptUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a ShipmentReceipt from the database
	 * 
	 * @param shipmentReceiptId:
	 *            the id of the ShipmentReceipt thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deleteshipmentReceiptById(@RequestParam(value = "shipmentReceiptId") String shipmentReceiptId) {

		DeleteShipmentReceipt com = new DeleteShipmentReceipt(shipmentReceiptId);

		int usedTicketId;

		synchronized (ShipmentReceiptController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ShipmentReceiptDeleted.class,
				event -> sendShipmentReceiptChangedMessage(((ShipmentReceiptDeleted) event).isSuccess(), usedTicketId));

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

	public void sendShipmentReceiptChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/shipmentReceipt/\" plus one of the following: "
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
