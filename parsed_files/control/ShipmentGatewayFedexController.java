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
import com.skytala.eCommerce.command.AddShipmentGatewayFedex;
import com.skytala.eCommerce.command.DeleteShipmentGatewayFedex;
import com.skytala.eCommerce.command.UpdateShipmentGatewayFedex;
import com.skytala.eCommerce.entity.ShipmentGatewayFedex;
import com.skytala.eCommerce.entity.ShipmentGatewayFedexMapper;
import com.skytala.eCommerce.event.ShipmentGatewayFedexAdded;
import com.skytala.eCommerce.event.ShipmentGatewayFedexDeleted;
import com.skytala.eCommerce.event.ShipmentGatewayFedexFound;
import com.skytala.eCommerce.event.ShipmentGatewayFedexUpdated;
import com.skytala.eCommerce.query.FindShipmentGatewayFedexsBy;

@RestController
@RequestMapping("/api/shipmentGatewayFedex")
public class ShipmentGatewayFedexController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<ShipmentGatewayFedex>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public ShipmentGatewayFedexController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a ShipmentGatewayFedex
	 * @return a List with the ShipmentGatewayFedexs
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<ShipmentGatewayFedex> findShipmentGatewayFedexsBy(@RequestParam Map<String, String> allRequestParams) {

		FindShipmentGatewayFedexsBy query = new FindShipmentGatewayFedexsBy(allRequestParams);

		int usedTicketId;

		synchronized (ShipmentGatewayFedexController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ShipmentGatewayFedexFound.class,
				event -> sendShipmentGatewayFedexsFoundMessage(((ShipmentGatewayFedexFound) event).getShipmentGatewayFedexs(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendShipmentGatewayFedexsFoundMessage(List<ShipmentGatewayFedex> shipmentGatewayFedexs, int usedTicketId) {
		queryReturnVal.put(usedTicketId, shipmentGatewayFedexs);
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
	public boolean createShipmentGatewayFedex(HttpServletRequest request) {

		ShipmentGatewayFedex shipmentGatewayFedexToBeAdded = new ShipmentGatewayFedex();
		try {
			shipmentGatewayFedexToBeAdded = ShipmentGatewayFedexMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createShipmentGatewayFedex(shipmentGatewayFedexToBeAdded);

	}

	/**
	 * creates a new ShipmentGatewayFedex entry in the ofbiz database
	 * 
	 * @param shipmentGatewayFedexToBeAdded
	 *            the ShipmentGatewayFedex thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createShipmentGatewayFedex(ShipmentGatewayFedex shipmentGatewayFedexToBeAdded) {

		AddShipmentGatewayFedex com = new AddShipmentGatewayFedex(shipmentGatewayFedexToBeAdded);
		int usedTicketId;

		synchronized (ShipmentGatewayFedexController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ShipmentGatewayFedexAdded.class,
				event -> sendShipmentGatewayFedexChangedMessage(((ShipmentGatewayFedexAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateShipmentGatewayFedex(HttpServletRequest request) {

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

		ShipmentGatewayFedex shipmentGatewayFedexToBeUpdated = new ShipmentGatewayFedex();

		try {
			shipmentGatewayFedexToBeUpdated = ShipmentGatewayFedexMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateShipmentGatewayFedex(shipmentGatewayFedexToBeUpdated);

	}

	/**
	 * Updates the ShipmentGatewayFedex with the specific Id
	 * 
	 * @param shipmentGatewayFedexToBeUpdated the ShipmentGatewayFedex thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateShipmentGatewayFedex(ShipmentGatewayFedex shipmentGatewayFedexToBeUpdated) {

		UpdateShipmentGatewayFedex com = new UpdateShipmentGatewayFedex(shipmentGatewayFedexToBeUpdated);

		int usedTicketId;

		synchronized (ShipmentGatewayFedexController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ShipmentGatewayFedexUpdated.class,
				event -> sendShipmentGatewayFedexChangedMessage(((ShipmentGatewayFedexUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a ShipmentGatewayFedex from the database
	 * 
	 * @param shipmentGatewayFedexId:
	 *            the id of the ShipmentGatewayFedex thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deleteshipmentGatewayFedexById(@RequestParam(value = "shipmentGatewayFedexId") String shipmentGatewayFedexId) {

		DeleteShipmentGatewayFedex com = new DeleteShipmentGatewayFedex(shipmentGatewayFedexId);

		int usedTicketId;

		synchronized (ShipmentGatewayFedexController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ShipmentGatewayFedexDeleted.class,
				event -> sendShipmentGatewayFedexChangedMessage(((ShipmentGatewayFedexDeleted) event).isSuccess(), usedTicketId));

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

	public void sendShipmentGatewayFedexChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/shipmentGatewayFedex/\" plus one of the following: "
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
