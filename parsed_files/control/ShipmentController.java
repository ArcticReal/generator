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
import com.skytala.eCommerce.command.AddShipment;
import com.skytala.eCommerce.command.DeleteShipment;
import com.skytala.eCommerce.command.UpdateShipment;
import com.skytala.eCommerce.entity.Shipment;
import com.skytala.eCommerce.entity.ShipmentMapper;
import com.skytala.eCommerce.event.ShipmentAdded;
import com.skytala.eCommerce.event.ShipmentDeleted;
import com.skytala.eCommerce.event.ShipmentFound;
import com.skytala.eCommerce.event.ShipmentUpdated;
import com.skytala.eCommerce.query.FindShipmentsBy;

@RestController
@RequestMapping("/api/shipment")
public class ShipmentController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<Shipment>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public ShipmentController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a Shipment
	 * @return a List with the Shipments
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<Shipment> findShipmentsBy(@RequestParam Map<String, String> allRequestParams) {

		FindShipmentsBy query = new FindShipmentsBy(allRequestParams);

		int usedTicketId;

		synchronized (ShipmentController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ShipmentFound.class,
				event -> sendShipmentsFoundMessage(((ShipmentFound) event).getShipments(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendShipmentsFoundMessage(List<Shipment> shipments, int usedTicketId) {
		queryReturnVal.put(usedTicketId, shipments);
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
	public boolean createShipment(HttpServletRequest request) {

		Shipment shipmentToBeAdded = new Shipment();
		try {
			shipmentToBeAdded = ShipmentMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createShipment(shipmentToBeAdded);

	}

	/**
	 * creates a new Shipment entry in the ofbiz database
	 * 
	 * @param shipmentToBeAdded
	 *            the Shipment thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createShipment(Shipment shipmentToBeAdded) {

		AddShipment com = new AddShipment(shipmentToBeAdded);
		int usedTicketId;

		synchronized (ShipmentController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ShipmentAdded.class,
				event -> sendShipmentChangedMessage(((ShipmentAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateShipment(HttpServletRequest request) {

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

		Shipment shipmentToBeUpdated = new Shipment();

		try {
			shipmentToBeUpdated = ShipmentMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateShipment(shipmentToBeUpdated);

	}

	/**
	 * Updates the Shipment with the specific Id
	 * 
	 * @param shipmentToBeUpdated the Shipment thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateShipment(Shipment shipmentToBeUpdated) {

		UpdateShipment com = new UpdateShipment(shipmentToBeUpdated);

		int usedTicketId;

		synchronized (ShipmentController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ShipmentUpdated.class,
				event -> sendShipmentChangedMessage(((ShipmentUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a Shipment from the database
	 * 
	 * @param shipmentId:
	 *            the id of the Shipment thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deleteshipmentById(@RequestParam(value = "shipmentId") String shipmentId) {

		DeleteShipment com = new DeleteShipment(shipmentId);

		int usedTicketId;

		synchronized (ShipmentController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ShipmentDeleted.class,
				event -> sendShipmentChangedMessage(((ShipmentDeleted) event).isSuccess(), usedTicketId));

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

	public void sendShipmentChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/shipment/\" plus one of the following: "
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
