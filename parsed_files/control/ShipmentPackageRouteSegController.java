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
import com.skytala.eCommerce.command.AddShipmentPackageRouteSeg;
import com.skytala.eCommerce.command.DeleteShipmentPackageRouteSeg;
import com.skytala.eCommerce.command.UpdateShipmentPackageRouteSeg;
import com.skytala.eCommerce.entity.ShipmentPackageRouteSeg;
import com.skytala.eCommerce.entity.ShipmentPackageRouteSegMapper;
import com.skytala.eCommerce.event.ShipmentPackageRouteSegAdded;
import com.skytala.eCommerce.event.ShipmentPackageRouteSegDeleted;
import com.skytala.eCommerce.event.ShipmentPackageRouteSegFound;
import com.skytala.eCommerce.event.ShipmentPackageRouteSegUpdated;
import com.skytala.eCommerce.query.FindShipmentPackageRouteSegsBy;

@RestController
@RequestMapping("/api/shipmentPackageRouteSeg")
public class ShipmentPackageRouteSegController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<ShipmentPackageRouteSeg>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public ShipmentPackageRouteSegController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a ShipmentPackageRouteSeg
	 * @return a List with the ShipmentPackageRouteSegs
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<ShipmentPackageRouteSeg> findShipmentPackageRouteSegsBy(@RequestParam Map<String, String> allRequestParams) {

		FindShipmentPackageRouteSegsBy query = new FindShipmentPackageRouteSegsBy(allRequestParams);

		int usedTicketId;

		synchronized (ShipmentPackageRouteSegController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ShipmentPackageRouteSegFound.class,
				event -> sendShipmentPackageRouteSegsFoundMessage(((ShipmentPackageRouteSegFound) event).getShipmentPackageRouteSegs(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendShipmentPackageRouteSegsFoundMessage(List<ShipmentPackageRouteSeg> shipmentPackageRouteSegs, int usedTicketId) {
		queryReturnVal.put(usedTicketId, shipmentPackageRouteSegs);
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
	public boolean createShipmentPackageRouteSeg(HttpServletRequest request) {

		ShipmentPackageRouteSeg shipmentPackageRouteSegToBeAdded = new ShipmentPackageRouteSeg();
		try {
			shipmentPackageRouteSegToBeAdded = ShipmentPackageRouteSegMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createShipmentPackageRouteSeg(shipmentPackageRouteSegToBeAdded);

	}

	/**
	 * creates a new ShipmentPackageRouteSeg entry in the ofbiz database
	 * 
	 * @param shipmentPackageRouteSegToBeAdded
	 *            the ShipmentPackageRouteSeg thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createShipmentPackageRouteSeg(ShipmentPackageRouteSeg shipmentPackageRouteSegToBeAdded) {

		AddShipmentPackageRouteSeg com = new AddShipmentPackageRouteSeg(shipmentPackageRouteSegToBeAdded);
		int usedTicketId;

		synchronized (ShipmentPackageRouteSegController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ShipmentPackageRouteSegAdded.class,
				event -> sendShipmentPackageRouteSegChangedMessage(((ShipmentPackageRouteSegAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateShipmentPackageRouteSeg(HttpServletRequest request) {

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

		ShipmentPackageRouteSeg shipmentPackageRouteSegToBeUpdated = new ShipmentPackageRouteSeg();

		try {
			shipmentPackageRouteSegToBeUpdated = ShipmentPackageRouteSegMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateShipmentPackageRouteSeg(shipmentPackageRouteSegToBeUpdated);

	}

	/**
	 * Updates the ShipmentPackageRouteSeg with the specific Id
	 * 
	 * @param shipmentPackageRouteSegToBeUpdated the ShipmentPackageRouteSeg thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateShipmentPackageRouteSeg(ShipmentPackageRouteSeg shipmentPackageRouteSegToBeUpdated) {

		UpdateShipmentPackageRouteSeg com = new UpdateShipmentPackageRouteSeg(shipmentPackageRouteSegToBeUpdated);

		int usedTicketId;

		synchronized (ShipmentPackageRouteSegController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ShipmentPackageRouteSegUpdated.class,
				event -> sendShipmentPackageRouteSegChangedMessage(((ShipmentPackageRouteSegUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a ShipmentPackageRouteSeg from the database
	 * 
	 * @param shipmentPackageRouteSegId:
	 *            the id of the ShipmentPackageRouteSeg thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deleteshipmentPackageRouteSegById(@RequestParam(value = "shipmentPackageRouteSegId") String shipmentPackageRouteSegId) {

		DeleteShipmentPackageRouteSeg com = new DeleteShipmentPackageRouteSeg(shipmentPackageRouteSegId);

		int usedTicketId;

		synchronized (ShipmentPackageRouteSegController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ShipmentPackageRouteSegDeleted.class,
				event -> sendShipmentPackageRouteSegChangedMessage(((ShipmentPackageRouteSegDeleted) event).isSuccess(), usedTicketId));

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

	public void sendShipmentPackageRouteSegChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/shipmentPackageRouteSeg/\" plus one of the following: "
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
