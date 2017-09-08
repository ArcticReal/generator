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
import com.skytala.eCommerce.command.AddShipmentGatewayDhl;
import com.skytala.eCommerce.command.DeleteShipmentGatewayDhl;
import com.skytala.eCommerce.command.UpdateShipmentGatewayDhl;
import com.skytala.eCommerce.entity.ShipmentGatewayDhl;
import com.skytala.eCommerce.entity.ShipmentGatewayDhlMapper;
import com.skytala.eCommerce.event.ShipmentGatewayDhlAdded;
import com.skytala.eCommerce.event.ShipmentGatewayDhlDeleted;
import com.skytala.eCommerce.event.ShipmentGatewayDhlFound;
import com.skytala.eCommerce.event.ShipmentGatewayDhlUpdated;
import com.skytala.eCommerce.query.FindShipmentGatewayDhlsBy;

@RestController
@RequestMapping("/api/shipmentGatewayDhl")
public class ShipmentGatewayDhlController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<ShipmentGatewayDhl>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public ShipmentGatewayDhlController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a ShipmentGatewayDhl
	 * @return a List with the ShipmentGatewayDhls
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<ShipmentGatewayDhl> findShipmentGatewayDhlsBy(@RequestParam Map<String, String> allRequestParams) {

		FindShipmentGatewayDhlsBy query = new FindShipmentGatewayDhlsBy(allRequestParams);

		int usedTicketId;

		synchronized (ShipmentGatewayDhlController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ShipmentGatewayDhlFound.class,
				event -> sendShipmentGatewayDhlsFoundMessage(((ShipmentGatewayDhlFound) event).getShipmentGatewayDhls(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendShipmentGatewayDhlsFoundMessage(List<ShipmentGatewayDhl> shipmentGatewayDhls, int usedTicketId) {
		queryReturnVal.put(usedTicketId, shipmentGatewayDhls);
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
	public boolean createShipmentGatewayDhl(HttpServletRequest request) {

		ShipmentGatewayDhl shipmentGatewayDhlToBeAdded = new ShipmentGatewayDhl();
		try {
			shipmentGatewayDhlToBeAdded = ShipmentGatewayDhlMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createShipmentGatewayDhl(shipmentGatewayDhlToBeAdded);

	}

	/**
	 * creates a new ShipmentGatewayDhl entry in the ofbiz database
	 * 
	 * @param shipmentGatewayDhlToBeAdded
	 *            the ShipmentGatewayDhl thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createShipmentGatewayDhl(ShipmentGatewayDhl shipmentGatewayDhlToBeAdded) {

		AddShipmentGatewayDhl com = new AddShipmentGatewayDhl(shipmentGatewayDhlToBeAdded);
		int usedTicketId;

		synchronized (ShipmentGatewayDhlController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ShipmentGatewayDhlAdded.class,
				event -> sendShipmentGatewayDhlChangedMessage(((ShipmentGatewayDhlAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateShipmentGatewayDhl(HttpServletRequest request) {

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

		ShipmentGatewayDhl shipmentGatewayDhlToBeUpdated = new ShipmentGatewayDhl();

		try {
			shipmentGatewayDhlToBeUpdated = ShipmentGatewayDhlMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateShipmentGatewayDhl(shipmentGatewayDhlToBeUpdated);

	}

	/**
	 * Updates the ShipmentGatewayDhl with the specific Id
	 * 
	 * @param shipmentGatewayDhlToBeUpdated the ShipmentGatewayDhl thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateShipmentGatewayDhl(ShipmentGatewayDhl shipmentGatewayDhlToBeUpdated) {

		UpdateShipmentGatewayDhl com = new UpdateShipmentGatewayDhl(shipmentGatewayDhlToBeUpdated);

		int usedTicketId;

		synchronized (ShipmentGatewayDhlController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ShipmentGatewayDhlUpdated.class,
				event -> sendShipmentGatewayDhlChangedMessage(((ShipmentGatewayDhlUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a ShipmentGatewayDhl from the database
	 * 
	 * @param shipmentGatewayDhlId:
	 *            the id of the ShipmentGatewayDhl thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deleteshipmentGatewayDhlById(@RequestParam(value = "shipmentGatewayDhlId") String shipmentGatewayDhlId) {

		DeleteShipmentGatewayDhl com = new DeleteShipmentGatewayDhl(shipmentGatewayDhlId);

		int usedTicketId;

		synchronized (ShipmentGatewayDhlController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ShipmentGatewayDhlDeleted.class,
				event -> sendShipmentGatewayDhlChangedMessage(((ShipmentGatewayDhlDeleted) event).isSuccess(), usedTicketId));

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

	public void sendShipmentGatewayDhlChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/shipmentGatewayDhl/\" plus one of the following: "
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
