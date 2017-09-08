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
import com.skytala.eCommerce.command.AddShipmentMethodType;
import com.skytala.eCommerce.command.DeleteShipmentMethodType;
import com.skytala.eCommerce.command.UpdateShipmentMethodType;
import com.skytala.eCommerce.entity.ShipmentMethodType;
import com.skytala.eCommerce.entity.ShipmentMethodTypeMapper;
import com.skytala.eCommerce.event.ShipmentMethodTypeAdded;
import com.skytala.eCommerce.event.ShipmentMethodTypeDeleted;
import com.skytala.eCommerce.event.ShipmentMethodTypeFound;
import com.skytala.eCommerce.event.ShipmentMethodTypeUpdated;
import com.skytala.eCommerce.query.FindShipmentMethodTypesBy;

@RestController
@RequestMapping("/api/shipmentMethodType")
public class ShipmentMethodTypeController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<ShipmentMethodType>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public ShipmentMethodTypeController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a ShipmentMethodType
	 * @return a List with the ShipmentMethodTypes
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<ShipmentMethodType> findShipmentMethodTypesBy(@RequestParam Map<String, String> allRequestParams) {

		FindShipmentMethodTypesBy query = new FindShipmentMethodTypesBy(allRequestParams);

		int usedTicketId;

		synchronized (ShipmentMethodTypeController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ShipmentMethodTypeFound.class,
				event -> sendShipmentMethodTypesFoundMessage(((ShipmentMethodTypeFound) event).getShipmentMethodTypes(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendShipmentMethodTypesFoundMessage(List<ShipmentMethodType> shipmentMethodTypes, int usedTicketId) {
		queryReturnVal.put(usedTicketId, shipmentMethodTypes);
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
	public boolean createShipmentMethodType(HttpServletRequest request) {

		ShipmentMethodType shipmentMethodTypeToBeAdded = new ShipmentMethodType();
		try {
			shipmentMethodTypeToBeAdded = ShipmentMethodTypeMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createShipmentMethodType(shipmentMethodTypeToBeAdded);

	}

	/**
	 * creates a new ShipmentMethodType entry in the ofbiz database
	 * 
	 * @param shipmentMethodTypeToBeAdded
	 *            the ShipmentMethodType thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createShipmentMethodType(ShipmentMethodType shipmentMethodTypeToBeAdded) {

		AddShipmentMethodType com = new AddShipmentMethodType(shipmentMethodTypeToBeAdded);
		int usedTicketId;

		synchronized (ShipmentMethodTypeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ShipmentMethodTypeAdded.class,
				event -> sendShipmentMethodTypeChangedMessage(((ShipmentMethodTypeAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateShipmentMethodType(HttpServletRequest request) {

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

		ShipmentMethodType shipmentMethodTypeToBeUpdated = new ShipmentMethodType();

		try {
			shipmentMethodTypeToBeUpdated = ShipmentMethodTypeMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateShipmentMethodType(shipmentMethodTypeToBeUpdated);

	}

	/**
	 * Updates the ShipmentMethodType with the specific Id
	 * 
	 * @param shipmentMethodTypeToBeUpdated the ShipmentMethodType thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateShipmentMethodType(ShipmentMethodType shipmentMethodTypeToBeUpdated) {

		UpdateShipmentMethodType com = new UpdateShipmentMethodType(shipmentMethodTypeToBeUpdated);

		int usedTicketId;

		synchronized (ShipmentMethodTypeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ShipmentMethodTypeUpdated.class,
				event -> sendShipmentMethodTypeChangedMessage(((ShipmentMethodTypeUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a ShipmentMethodType from the database
	 * 
	 * @param shipmentMethodTypeId:
	 *            the id of the ShipmentMethodType thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deleteshipmentMethodTypeById(@RequestParam(value = "shipmentMethodTypeId") String shipmentMethodTypeId) {

		DeleteShipmentMethodType com = new DeleteShipmentMethodType(shipmentMethodTypeId);

		int usedTicketId;

		synchronized (ShipmentMethodTypeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ShipmentMethodTypeDeleted.class,
				event -> sendShipmentMethodTypeChangedMessage(((ShipmentMethodTypeDeleted) event).isSuccess(), usedTicketId));

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

	public void sendShipmentMethodTypeChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/shipmentMethodType/\" plus one of the following: "
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
