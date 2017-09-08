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
import com.skytala.eCommerce.command.AddShipmentContactMech;
import com.skytala.eCommerce.command.DeleteShipmentContactMech;
import com.skytala.eCommerce.command.UpdateShipmentContactMech;
import com.skytala.eCommerce.entity.ShipmentContactMech;
import com.skytala.eCommerce.entity.ShipmentContactMechMapper;
import com.skytala.eCommerce.event.ShipmentContactMechAdded;
import com.skytala.eCommerce.event.ShipmentContactMechDeleted;
import com.skytala.eCommerce.event.ShipmentContactMechFound;
import com.skytala.eCommerce.event.ShipmentContactMechUpdated;
import com.skytala.eCommerce.query.FindShipmentContactMechsBy;

@RestController
@RequestMapping("/api/shipmentContactMech")
public class ShipmentContactMechController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<ShipmentContactMech>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public ShipmentContactMechController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a ShipmentContactMech
	 * @return a List with the ShipmentContactMechs
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<ShipmentContactMech> findShipmentContactMechsBy(@RequestParam Map<String, String> allRequestParams) {

		FindShipmentContactMechsBy query = new FindShipmentContactMechsBy(allRequestParams);

		int usedTicketId;

		synchronized (ShipmentContactMechController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ShipmentContactMechFound.class,
				event -> sendShipmentContactMechsFoundMessage(((ShipmentContactMechFound) event).getShipmentContactMechs(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendShipmentContactMechsFoundMessage(List<ShipmentContactMech> shipmentContactMechs, int usedTicketId) {
		queryReturnVal.put(usedTicketId, shipmentContactMechs);
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
	public boolean createShipmentContactMech(HttpServletRequest request) {

		ShipmentContactMech shipmentContactMechToBeAdded = new ShipmentContactMech();
		try {
			shipmentContactMechToBeAdded = ShipmentContactMechMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createShipmentContactMech(shipmentContactMechToBeAdded);

	}

	/**
	 * creates a new ShipmentContactMech entry in the ofbiz database
	 * 
	 * @param shipmentContactMechToBeAdded
	 *            the ShipmentContactMech thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createShipmentContactMech(ShipmentContactMech shipmentContactMechToBeAdded) {

		AddShipmentContactMech com = new AddShipmentContactMech(shipmentContactMechToBeAdded);
		int usedTicketId;

		synchronized (ShipmentContactMechController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ShipmentContactMechAdded.class,
				event -> sendShipmentContactMechChangedMessage(((ShipmentContactMechAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateShipmentContactMech(HttpServletRequest request) {

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

		ShipmentContactMech shipmentContactMechToBeUpdated = new ShipmentContactMech();

		try {
			shipmentContactMechToBeUpdated = ShipmentContactMechMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateShipmentContactMech(shipmentContactMechToBeUpdated);

	}

	/**
	 * Updates the ShipmentContactMech with the specific Id
	 * 
	 * @param shipmentContactMechToBeUpdated the ShipmentContactMech thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateShipmentContactMech(ShipmentContactMech shipmentContactMechToBeUpdated) {

		UpdateShipmentContactMech com = new UpdateShipmentContactMech(shipmentContactMechToBeUpdated);

		int usedTicketId;

		synchronized (ShipmentContactMechController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ShipmentContactMechUpdated.class,
				event -> sendShipmentContactMechChangedMessage(((ShipmentContactMechUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a ShipmentContactMech from the database
	 * 
	 * @param shipmentContactMechId:
	 *            the id of the ShipmentContactMech thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deleteshipmentContactMechById(@RequestParam(value = "shipmentContactMechId") String shipmentContactMechId) {

		DeleteShipmentContactMech com = new DeleteShipmentContactMech(shipmentContactMechId);

		int usedTicketId;

		synchronized (ShipmentContactMechController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ShipmentContactMechDeleted.class,
				event -> sendShipmentContactMechChangedMessage(((ShipmentContactMechDeleted) event).isSuccess(), usedTicketId));

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

	public void sendShipmentContactMechChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/shipmentContactMech/\" plus one of the following: "
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
