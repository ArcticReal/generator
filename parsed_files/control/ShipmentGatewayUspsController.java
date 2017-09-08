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
import com.skytala.eCommerce.command.AddShipmentGatewayUsps;
import com.skytala.eCommerce.command.DeleteShipmentGatewayUsps;
import com.skytala.eCommerce.command.UpdateShipmentGatewayUsps;
import com.skytala.eCommerce.entity.ShipmentGatewayUsps;
import com.skytala.eCommerce.entity.ShipmentGatewayUspsMapper;
import com.skytala.eCommerce.event.ShipmentGatewayUspsAdded;
import com.skytala.eCommerce.event.ShipmentGatewayUspsDeleted;
import com.skytala.eCommerce.event.ShipmentGatewayUspsFound;
import com.skytala.eCommerce.event.ShipmentGatewayUspsUpdated;
import com.skytala.eCommerce.query.FindShipmentGatewayUspssBy;

@RestController
@RequestMapping("/api/shipmentGatewayUsps")
public class ShipmentGatewayUspsController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<ShipmentGatewayUsps>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public ShipmentGatewayUspsController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a ShipmentGatewayUsps
	 * @return a List with the ShipmentGatewayUspss
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<ShipmentGatewayUsps> findShipmentGatewayUspssBy(@RequestParam Map<String, String> allRequestParams) {

		FindShipmentGatewayUspssBy query = new FindShipmentGatewayUspssBy(allRequestParams);

		int usedTicketId;

		synchronized (ShipmentGatewayUspsController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ShipmentGatewayUspsFound.class,
				event -> sendShipmentGatewayUspssFoundMessage(((ShipmentGatewayUspsFound) event).getShipmentGatewayUspss(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendShipmentGatewayUspssFoundMessage(List<ShipmentGatewayUsps> shipmentGatewayUspss, int usedTicketId) {
		queryReturnVal.put(usedTicketId, shipmentGatewayUspss);
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
	public boolean createShipmentGatewayUsps(HttpServletRequest request) {

		ShipmentGatewayUsps shipmentGatewayUspsToBeAdded = new ShipmentGatewayUsps();
		try {
			shipmentGatewayUspsToBeAdded = ShipmentGatewayUspsMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createShipmentGatewayUsps(shipmentGatewayUspsToBeAdded);

	}

	/**
	 * creates a new ShipmentGatewayUsps entry in the ofbiz database
	 * 
	 * @param shipmentGatewayUspsToBeAdded
	 *            the ShipmentGatewayUsps thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createShipmentGatewayUsps(ShipmentGatewayUsps shipmentGatewayUspsToBeAdded) {

		AddShipmentGatewayUsps com = new AddShipmentGatewayUsps(shipmentGatewayUspsToBeAdded);
		int usedTicketId;

		synchronized (ShipmentGatewayUspsController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ShipmentGatewayUspsAdded.class,
				event -> sendShipmentGatewayUspsChangedMessage(((ShipmentGatewayUspsAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateShipmentGatewayUsps(HttpServletRequest request) {

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

		ShipmentGatewayUsps shipmentGatewayUspsToBeUpdated = new ShipmentGatewayUsps();

		try {
			shipmentGatewayUspsToBeUpdated = ShipmentGatewayUspsMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateShipmentGatewayUsps(shipmentGatewayUspsToBeUpdated);

	}

	/**
	 * Updates the ShipmentGatewayUsps with the specific Id
	 * 
	 * @param shipmentGatewayUspsToBeUpdated the ShipmentGatewayUsps thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateShipmentGatewayUsps(ShipmentGatewayUsps shipmentGatewayUspsToBeUpdated) {

		UpdateShipmentGatewayUsps com = new UpdateShipmentGatewayUsps(shipmentGatewayUspsToBeUpdated);

		int usedTicketId;

		synchronized (ShipmentGatewayUspsController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ShipmentGatewayUspsUpdated.class,
				event -> sendShipmentGatewayUspsChangedMessage(((ShipmentGatewayUspsUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a ShipmentGatewayUsps from the database
	 * 
	 * @param shipmentGatewayUspsId:
	 *            the id of the ShipmentGatewayUsps thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deleteshipmentGatewayUspsById(@RequestParam(value = "shipmentGatewayUspsId") String shipmentGatewayUspsId) {

		DeleteShipmentGatewayUsps com = new DeleteShipmentGatewayUsps(shipmentGatewayUspsId);

		int usedTicketId;

		synchronized (ShipmentGatewayUspsController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ShipmentGatewayUspsDeleted.class,
				event -> sendShipmentGatewayUspsChangedMessage(((ShipmentGatewayUspsDeleted) event).isSuccess(), usedTicketId));

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

	public void sendShipmentGatewayUspsChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/shipmentGatewayUsps/\" plus one of the following: "
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
