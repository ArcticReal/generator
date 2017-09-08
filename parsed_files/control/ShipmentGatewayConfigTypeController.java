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
import com.skytala.eCommerce.command.AddShipmentGatewayConfigType;
import com.skytala.eCommerce.command.DeleteShipmentGatewayConfigType;
import com.skytala.eCommerce.command.UpdateShipmentGatewayConfigType;
import com.skytala.eCommerce.entity.ShipmentGatewayConfigType;
import com.skytala.eCommerce.entity.ShipmentGatewayConfigTypeMapper;
import com.skytala.eCommerce.event.ShipmentGatewayConfigTypeAdded;
import com.skytala.eCommerce.event.ShipmentGatewayConfigTypeDeleted;
import com.skytala.eCommerce.event.ShipmentGatewayConfigTypeFound;
import com.skytala.eCommerce.event.ShipmentGatewayConfigTypeUpdated;
import com.skytala.eCommerce.query.FindShipmentGatewayConfigTypesBy;

@RestController
@RequestMapping("/api/shipmentGatewayConfigType")
public class ShipmentGatewayConfigTypeController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<ShipmentGatewayConfigType>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public ShipmentGatewayConfigTypeController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a ShipmentGatewayConfigType
	 * @return a List with the ShipmentGatewayConfigTypes
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<ShipmentGatewayConfigType> findShipmentGatewayConfigTypesBy(@RequestParam Map<String, String> allRequestParams) {

		FindShipmentGatewayConfigTypesBy query = new FindShipmentGatewayConfigTypesBy(allRequestParams);

		int usedTicketId;

		synchronized (ShipmentGatewayConfigTypeController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ShipmentGatewayConfigTypeFound.class,
				event -> sendShipmentGatewayConfigTypesFoundMessage(((ShipmentGatewayConfigTypeFound) event).getShipmentGatewayConfigTypes(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendShipmentGatewayConfigTypesFoundMessage(List<ShipmentGatewayConfigType> shipmentGatewayConfigTypes, int usedTicketId) {
		queryReturnVal.put(usedTicketId, shipmentGatewayConfigTypes);
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
	public boolean createShipmentGatewayConfigType(HttpServletRequest request) {

		ShipmentGatewayConfigType shipmentGatewayConfigTypeToBeAdded = new ShipmentGatewayConfigType();
		try {
			shipmentGatewayConfigTypeToBeAdded = ShipmentGatewayConfigTypeMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createShipmentGatewayConfigType(shipmentGatewayConfigTypeToBeAdded);

	}

	/**
	 * creates a new ShipmentGatewayConfigType entry in the ofbiz database
	 * 
	 * @param shipmentGatewayConfigTypeToBeAdded
	 *            the ShipmentGatewayConfigType thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createShipmentGatewayConfigType(ShipmentGatewayConfigType shipmentGatewayConfigTypeToBeAdded) {

		AddShipmentGatewayConfigType com = new AddShipmentGatewayConfigType(shipmentGatewayConfigTypeToBeAdded);
		int usedTicketId;

		synchronized (ShipmentGatewayConfigTypeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ShipmentGatewayConfigTypeAdded.class,
				event -> sendShipmentGatewayConfigTypeChangedMessage(((ShipmentGatewayConfigTypeAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateShipmentGatewayConfigType(HttpServletRequest request) {

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

		ShipmentGatewayConfigType shipmentGatewayConfigTypeToBeUpdated = new ShipmentGatewayConfigType();

		try {
			shipmentGatewayConfigTypeToBeUpdated = ShipmentGatewayConfigTypeMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateShipmentGatewayConfigType(shipmentGatewayConfigTypeToBeUpdated);

	}

	/**
	 * Updates the ShipmentGatewayConfigType with the specific Id
	 * 
	 * @param shipmentGatewayConfigTypeToBeUpdated the ShipmentGatewayConfigType thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateShipmentGatewayConfigType(ShipmentGatewayConfigType shipmentGatewayConfigTypeToBeUpdated) {

		UpdateShipmentGatewayConfigType com = new UpdateShipmentGatewayConfigType(shipmentGatewayConfigTypeToBeUpdated);

		int usedTicketId;

		synchronized (ShipmentGatewayConfigTypeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ShipmentGatewayConfigTypeUpdated.class,
				event -> sendShipmentGatewayConfigTypeChangedMessage(((ShipmentGatewayConfigTypeUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a ShipmentGatewayConfigType from the database
	 * 
	 * @param shipmentGatewayConfigTypeId:
	 *            the id of the ShipmentGatewayConfigType thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deleteshipmentGatewayConfigTypeById(@RequestParam(value = "shipmentGatewayConfigTypeId") String shipmentGatewayConfigTypeId) {

		DeleteShipmentGatewayConfigType com = new DeleteShipmentGatewayConfigType(shipmentGatewayConfigTypeId);

		int usedTicketId;

		synchronized (ShipmentGatewayConfigTypeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ShipmentGatewayConfigTypeDeleted.class,
				event -> sendShipmentGatewayConfigTypeChangedMessage(((ShipmentGatewayConfigTypeDeleted) event).isSuccess(), usedTicketId));

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

	public void sendShipmentGatewayConfigTypeChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/shipmentGatewayConfigType/\" plus one of the following: "
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
