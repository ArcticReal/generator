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
import com.skytala.eCommerce.command.AddShipmentContactMechType;
import com.skytala.eCommerce.command.DeleteShipmentContactMechType;
import com.skytala.eCommerce.command.UpdateShipmentContactMechType;
import com.skytala.eCommerce.entity.ShipmentContactMechType;
import com.skytala.eCommerce.entity.ShipmentContactMechTypeMapper;
import com.skytala.eCommerce.event.ShipmentContactMechTypeAdded;
import com.skytala.eCommerce.event.ShipmentContactMechTypeDeleted;
import com.skytala.eCommerce.event.ShipmentContactMechTypeFound;
import com.skytala.eCommerce.event.ShipmentContactMechTypeUpdated;
import com.skytala.eCommerce.query.FindShipmentContactMechTypesBy;

@RestController
@RequestMapping("/api/shipmentContactMechType")
public class ShipmentContactMechTypeController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<ShipmentContactMechType>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public ShipmentContactMechTypeController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a ShipmentContactMechType
	 * @return a List with the ShipmentContactMechTypes
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<ShipmentContactMechType> findShipmentContactMechTypesBy(@RequestParam Map<String, String> allRequestParams) {

		FindShipmentContactMechTypesBy query = new FindShipmentContactMechTypesBy(allRequestParams);

		int usedTicketId;

		synchronized (ShipmentContactMechTypeController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ShipmentContactMechTypeFound.class,
				event -> sendShipmentContactMechTypesFoundMessage(((ShipmentContactMechTypeFound) event).getShipmentContactMechTypes(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendShipmentContactMechTypesFoundMessage(List<ShipmentContactMechType> shipmentContactMechTypes, int usedTicketId) {
		queryReturnVal.put(usedTicketId, shipmentContactMechTypes);
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
	public boolean createShipmentContactMechType(HttpServletRequest request) {

		ShipmentContactMechType shipmentContactMechTypeToBeAdded = new ShipmentContactMechType();
		try {
			shipmentContactMechTypeToBeAdded = ShipmentContactMechTypeMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createShipmentContactMechType(shipmentContactMechTypeToBeAdded);

	}

	/**
	 * creates a new ShipmentContactMechType entry in the ofbiz database
	 * 
	 * @param shipmentContactMechTypeToBeAdded
	 *            the ShipmentContactMechType thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createShipmentContactMechType(ShipmentContactMechType shipmentContactMechTypeToBeAdded) {

		AddShipmentContactMechType com = new AddShipmentContactMechType(shipmentContactMechTypeToBeAdded);
		int usedTicketId;

		synchronized (ShipmentContactMechTypeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ShipmentContactMechTypeAdded.class,
				event -> sendShipmentContactMechTypeChangedMessage(((ShipmentContactMechTypeAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateShipmentContactMechType(HttpServletRequest request) {

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

		ShipmentContactMechType shipmentContactMechTypeToBeUpdated = new ShipmentContactMechType();

		try {
			shipmentContactMechTypeToBeUpdated = ShipmentContactMechTypeMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateShipmentContactMechType(shipmentContactMechTypeToBeUpdated);

	}

	/**
	 * Updates the ShipmentContactMechType with the specific Id
	 * 
	 * @param shipmentContactMechTypeToBeUpdated the ShipmentContactMechType thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateShipmentContactMechType(ShipmentContactMechType shipmentContactMechTypeToBeUpdated) {

		UpdateShipmentContactMechType com = new UpdateShipmentContactMechType(shipmentContactMechTypeToBeUpdated);

		int usedTicketId;

		synchronized (ShipmentContactMechTypeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ShipmentContactMechTypeUpdated.class,
				event -> sendShipmentContactMechTypeChangedMessage(((ShipmentContactMechTypeUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a ShipmentContactMechType from the database
	 * 
	 * @param shipmentContactMechTypeId:
	 *            the id of the ShipmentContactMechType thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deleteshipmentContactMechTypeById(@RequestParam(value = "shipmentContactMechTypeId") String shipmentContactMechTypeId) {

		DeleteShipmentContactMechType com = new DeleteShipmentContactMechType(shipmentContactMechTypeId);

		int usedTicketId;

		synchronized (ShipmentContactMechTypeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ShipmentContactMechTypeDeleted.class,
				event -> sendShipmentContactMechTypeChangedMessage(((ShipmentContactMechTypeDeleted) event).isSuccess(), usedTicketId));

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

	public void sendShipmentContactMechTypeChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/shipmentContactMechType/\" plus one of the following: "
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
