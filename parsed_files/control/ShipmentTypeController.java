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
import com.skytala.eCommerce.command.AddShipmentType;
import com.skytala.eCommerce.command.DeleteShipmentType;
import com.skytala.eCommerce.command.UpdateShipmentType;
import com.skytala.eCommerce.entity.ShipmentType;
import com.skytala.eCommerce.entity.ShipmentTypeMapper;
import com.skytala.eCommerce.event.ShipmentTypeAdded;
import com.skytala.eCommerce.event.ShipmentTypeDeleted;
import com.skytala.eCommerce.event.ShipmentTypeFound;
import com.skytala.eCommerce.event.ShipmentTypeUpdated;
import com.skytala.eCommerce.query.FindShipmentTypesBy;

@RestController
@RequestMapping("/api/shipmentType")
public class ShipmentTypeController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<ShipmentType>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public ShipmentTypeController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a ShipmentType
	 * @return a List with the ShipmentTypes
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<ShipmentType> findShipmentTypesBy(@RequestParam Map<String, String> allRequestParams) {

		FindShipmentTypesBy query = new FindShipmentTypesBy(allRequestParams);

		int usedTicketId;

		synchronized (ShipmentTypeController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ShipmentTypeFound.class,
				event -> sendShipmentTypesFoundMessage(((ShipmentTypeFound) event).getShipmentTypes(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendShipmentTypesFoundMessage(List<ShipmentType> shipmentTypes, int usedTicketId) {
		queryReturnVal.put(usedTicketId, shipmentTypes);
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
	public boolean createShipmentType(HttpServletRequest request) {

		ShipmentType shipmentTypeToBeAdded = new ShipmentType();
		try {
			shipmentTypeToBeAdded = ShipmentTypeMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createShipmentType(shipmentTypeToBeAdded);

	}

	/**
	 * creates a new ShipmentType entry in the ofbiz database
	 * 
	 * @param shipmentTypeToBeAdded
	 *            the ShipmentType thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createShipmentType(ShipmentType shipmentTypeToBeAdded) {

		AddShipmentType com = new AddShipmentType(shipmentTypeToBeAdded);
		int usedTicketId;

		synchronized (ShipmentTypeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ShipmentTypeAdded.class,
				event -> sendShipmentTypeChangedMessage(((ShipmentTypeAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateShipmentType(HttpServletRequest request) {

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

		ShipmentType shipmentTypeToBeUpdated = new ShipmentType();

		try {
			shipmentTypeToBeUpdated = ShipmentTypeMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateShipmentType(shipmentTypeToBeUpdated);

	}

	/**
	 * Updates the ShipmentType with the specific Id
	 * 
	 * @param shipmentTypeToBeUpdated the ShipmentType thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateShipmentType(ShipmentType shipmentTypeToBeUpdated) {

		UpdateShipmentType com = new UpdateShipmentType(shipmentTypeToBeUpdated);

		int usedTicketId;

		synchronized (ShipmentTypeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ShipmentTypeUpdated.class,
				event -> sendShipmentTypeChangedMessage(((ShipmentTypeUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a ShipmentType from the database
	 * 
	 * @param shipmentTypeId:
	 *            the id of the ShipmentType thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deleteshipmentTypeById(@RequestParam(value = "shipmentTypeId") String shipmentTypeId) {

		DeleteShipmentType com = new DeleteShipmentType(shipmentTypeId);

		int usedTicketId;

		synchronized (ShipmentTypeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ShipmentTypeDeleted.class,
				event -> sendShipmentTypeChangedMessage(((ShipmentTypeDeleted) event).isSuccess(), usedTicketId));

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

	public void sendShipmentTypeChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/shipmentType/\" plus one of the following: "
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
