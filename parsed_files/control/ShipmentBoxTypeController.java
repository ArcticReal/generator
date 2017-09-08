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
import com.skytala.eCommerce.command.AddShipmentBoxType;
import com.skytala.eCommerce.command.DeleteShipmentBoxType;
import com.skytala.eCommerce.command.UpdateShipmentBoxType;
import com.skytala.eCommerce.entity.ShipmentBoxType;
import com.skytala.eCommerce.entity.ShipmentBoxTypeMapper;
import com.skytala.eCommerce.event.ShipmentBoxTypeAdded;
import com.skytala.eCommerce.event.ShipmentBoxTypeDeleted;
import com.skytala.eCommerce.event.ShipmentBoxTypeFound;
import com.skytala.eCommerce.event.ShipmentBoxTypeUpdated;
import com.skytala.eCommerce.query.FindShipmentBoxTypesBy;

@RestController
@RequestMapping("/api/shipmentBoxType")
public class ShipmentBoxTypeController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<ShipmentBoxType>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public ShipmentBoxTypeController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a ShipmentBoxType
	 * @return a List with the ShipmentBoxTypes
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<ShipmentBoxType> findShipmentBoxTypesBy(@RequestParam Map<String, String> allRequestParams) {

		FindShipmentBoxTypesBy query = new FindShipmentBoxTypesBy(allRequestParams);

		int usedTicketId;

		synchronized (ShipmentBoxTypeController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ShipmentBoxTypeFound.class,
				event -> sendShipmentBoxTypesFoundMessage(((ShipmentBoxTypeFound) event).getShipmentBoxTypes(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendShipmentBoxTypesFoundMessage(List<ShipmentBoxType> shipmentBoxTypes, int usedTicketId) {
		queryReturnVal.put(usedTicketId, shipmentBoxTypes);
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
	public boolean createShipmentBoxType(HttpServletRequest request) {

		ShipmentBoxType shipmentBoxTypeToBeAdded = new ShipmentBoxType();
		try {
			shipmentBoxTypeToBeAdded = ShipmentBoxTypeMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createShipmentBoxType(shipmentBoxTypeToBeAdded);

	}

	/**
	 * creates a new ShipmentBoxType entry in the ofbiz database
	 * 
	 * @param shipmentBoxTypeToBeAdded
	 *            the ShipmentBoxType thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createShipmentBoxType(ShipmentBoxType shipmentBoxTypeToBeAdded) {

		AddShipmentBoxType com = new AddShipmentBoxType(shipmentBoxTypeToBeAdded);
		int usedTicketId;

		synchronized (ShipmentBoxTypeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ShipmentBoxTypeAdded.class,
				event -> sendShipmentBoxTypeChangedMessage(((ShipmentBoxTypeAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateShipmentBoxType(HttpServletRequest request) {

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

		ShipmentBoxType shipmentBoxTypeToBeUpdated = new ShipmentBoxType();

		try {
			shipmentBoxTypeToBeUpdated = ShipmentBoxTypeMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateShipmentBoxType(shipmentBoxTypeToBeUpdated);

	}

	/**
	 * Updates the ShipmentBoxType with the specific Id
	 * 
	 * @param shipmentBoxTypeToBeUpdated the ShipmentBoxType thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateShipmentBoxType(ShipmentBoxType shipmentBoxTypeToBeUpdated) {

		UpdateShipmentBoxType com = new UpdateShipmentBoxType(shipmentBoxTypeToBeUpdated);

		int usedTicketId;

		synchronized (ShipmentBoxTypeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ShipmentBoxTypeUpdated.class,
				event -> sendShipmentBoxTypeChangedMessage(((ShipmentBoxTypeUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a ShipmentBoxType from the database
	 * 
	 * @param shipmentBoxTypeId:
	 *            the id of the ShipmentBoxType thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deleteshipmentBoxTypeById(@RequestParam(value = "shipmentBoxTypeId") String shipmentBoxTypeId) {

		DeleteShipmentBoxType com = new DeleteShipmentBoxType(shipmentBoxTypeId);

		int usedTicketId;

		synchronized (ShipmentBoxTypeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ShipmentBoxTypeDeleted.class,
				event -> sendShipmentBoxTypeChangedMessage(((ShipmentBoxTypeDeleted) event).isSuccess(), usedTicketId));

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

	public void sendShipmentBoxTypeChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/shipmentBoxType/\" plus one of the following: "
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
