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
import com.skytala.eCommerce.command.AddShipmentGatewayUps;
import com.skytala.eCommerce.command.DeleteShipmentGatewayUps;
import com.skytala.eCommerce.command.UpdateShipmentGatewayUps;
import com.skytala.eCommerce.entity.ShipmentGatewayUps;
import com.skytala.eCommerce.entity.ShipmentGatewayUpsMapper;
import com.skytala.eCommerce.event.ShipmentGatewayUpsAdded;
import com.skytala.eCommerce.event.ShipmentGatewayUpsDeleted;
import com.skytala.eCommerce.event.ShipmentGatewayUpsFound;
import com.skytala.eCommerce.event.ShipmentGatewayUpsUpdated;
import com.skytala.eCommerce.query.FindShipmentGatewayUpssBy;

@RestController
@RequestMapping("/api/shipmentGatewayUps")
public class ShipmentGatewayUpsController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<ShipmentGatewayUps>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public ShipmentGatewayUpsController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a ShipmentGatewayUps
	 * @return a List with the ShipmentGatewayUpss
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<ShipmentGatewayUps> findShipmentGatewayUpssBy(@RequestParam Map<String, String> allRequestParams) {

		FindShipmentGatewayUpssBy query = new FindShipmentGatewayUpssBy(allRequestParams);

		int usedTicketId;

		synchronized (ShipmentGatewayUpsController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ShipmentGatewayUpsFound.class,
				event -> sendShipmentGatewayUpssFoundMessage(((ShipmentGatewayUpsFound) event).getShipmentGatewayUpss(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendShipmentGatewayUpssFoundMessage(List<ShipmentGatewayUps> shipmentGatewayUpss, int usedTicketId) {
		queryReturnVal.put(usedTicketId, shipmentGatewayUpss);
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
	public boolean createShipmentGatewayUps(HttpServletRequest request) {

		ShipmentGatewayUps shipmentGatewayUpsToBeAdded = new ShipmentGatewayUps();
		try {
			shipmentGatewayUpsToBeAdded = ShipmentGatewayUpsMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createShipmentGatewayUps(shipmentGatewayUpsToBeAdded);

	}

	/**
	 * creates a new ShipmentGatewayUps entry in the ofbiz database
	 * 
	 * @param shipmentGatewayUpsToBeAdded
	 *            the ShipmentGatewayUps thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createShipmentGatewayUps(ShipmentGatewayUps shipmentGatewayUpsToBeAdded) {

		AddShipmentGatewayUps com = new AddShipmentGatewayUps(shipmentGatewayUpsToBeAdded);
		int usedTicketId;

		synchronized (ShipmentGatewayUpsController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ShipmentGatewayUpsAdded.class,
				event -> sendShipmentGatewayUpsChangedMessage(((ShipmentGatewayUpsAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateShipmentGatewayUps(HttpServletRequest request) {

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

		ShipmentGatewayUps shipmentGatewayUpsToBeUpdated = new ShipmentGatewayUps();

		try {
			shipmentGatewayUpsToBeUpdated = ShipmentGatewayUpsMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateShipmentGatewayUps(shipmentGatewayUpsToBeUpdated);

	}

	/**
	 * Updates the ShipmentGatewayUps with the specific Id
	 * 
	 * @param shipmentGatewayUpsToBeUpdated the ShipmentGatewayUps thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateShipmentGatewayUps(ShipmentGatewayUps shipmentGatewayUpsToBeUpdated) {

		UpdateShipmentGatewayUps com = new UpdateShipmentGatewayUps(shipmentGatewayUpsToBeUpdated);

		int usedTicketId;

		synchronized (ShipmentGatewayUpsController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ShipmentGatewayUpsUpdated.class,
				event -> sendShipmentGatewayUpsChangedMessage(((ShipmentGatewayUpsUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a ShipmentGatewayUps from the database
	 * 
	 * @param shipmentGatewayUpsId:
	 *            the id of the ShipmentGatewayUps thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deleteshipmentGatewayUpsById(@RequestParam(value = "shipmentGatewayUpsId") String shipmentGatewayUpsId) {

		DeleteShipmentGatewayUps com = new DeleteShipmentGatewayUps(shipmentGatewayUpsId);

		int usedTicketId;

		synchronized (ShipmentGatewayUpsController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ShipmentGatewayUpsDeleted.class,
				event -> sendShipmentGatewayUpsChangedMessage(((ShipmentGatewayUpsDeleted) event).isSuccess(), usedTicketId));

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

	public void sendShipmentGatewayUpsChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/shipmentGatewayUps/\" plus one of the following: "
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
