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
import com.skytala.eCommerce.command.AddShipmentPackage;
import com.skytala.eCommerce.command.DeleteShipmentPackage;
import com.skytala.eCommerce.command.UpdateShipmentPackage;
import com.skytala.eCommerce.entity.ShipmentPackage;
import com.skytala.eCommerce.entity.ShipmentPackageMapper;
import com.skytala.eCommerce.event.ShipmentPackageAdded;
import com.skytala.eCommerce.event.ShipmentPackageDeleted;
import com.skytala.eCommerce.event.ShipmentPackageFound;
import com.skytala.eCommerce.event.ShipmentPackageUpdated;
import com.skytala.eCommerce.query.FindShipmentPackagesBy;

@RestController
@RequestMapping("/api/shipmentPackage")
public class ShipmentPackageController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<ShipmentPackage>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public ShipmentPackageController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a ShipmentPackage
	 * @return a List with the ShipmentPackages
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<ShipmentPackage> findShipmentPackagesBy(@RequestParam Map<String, String> allRequestParams) {

		FindShipmentPackagesBy query = new FindShipmentPackagesBy(allRequestParams);

		int usedTicketId;

		synchronized (ShipmentPackageController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ShipmentPackageFound.class,
				event -> sendShipmentPackagesFoundMessage(((ShipmentPackageFound) event).getShipmentPackages(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendShipmentPackagesFoundMessage(List<ShipmentPackage> shipmentPackages, int usedTicketId) {
		queryReturnVal.put(usedTicketId, shipmentPackages);
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
	public boolean createShipmentPackage(HttpServletRequest request) {

		ShipmentPackage shipmentPackageToBeAdded = new ShipmentPackage();
		try {
			shipmentPackageToBeAdded = ShipmentPackageMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createShipmentPackage(shipmentPackageToBeAdded);

	}

	/**
	 * creates a new ShipmentPackage entry in the ofbiz database
	 * 
	 * @param shipmentPackageToBeAdded
	 *            the ShipmentPackage thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createShipmentPackage(ShipmentPackage shipmentPackageToBeAdded) {

		AddShipmentPackage com = new AddShipmentPackage(shipmentPackageToBeAdded);
		int usedTicketId;

		synchronized (ShipmentPackageController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ShipmentPackageAdded.class,
				event -> sendShipmentPackageChangedMessage(((ShipmentPackageAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateShipmentPackage(HttpServletRequest request) {

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

		ShipmentPackage shipmentPackageToBeUpdated = new ShipmentPackage();

		try {
			shipmentPackageToBeUpdated = ShipmentPackageMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateShipmentPackage(shipmentPackageToBeUpdated);

	}

	/**
	 * Updates the ShipmentPackage with the specific Id
	 * 
	 * @param shipmentPackageToBeUpdated the ShipmentPackage thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateShipmentPackage(ShipmentPackage shipmentPackageToBeUpdated) {

		UpdateShipmentPackage com = new UpdateShipmentPackage(shipmentPackageToBeUpdated);

		int usedTicketId;

		synchronized (ShipmentPackageController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ShipmentPackageUpdated.class,
				event -> sendShipmentPackageChangedMessage(((ShipmentPackageUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a ShipmentPackage from the database
	 * 
	 * @param shipmentPackageId:
	 *            the id of the ShipmentPackage thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deleteshipmentPackageById(@RequestParam(value = "shipmentPackageId") String shipmentPackageId) {

		DeleteShipmentPackage com = new DeleteShipmentPackage(shipmentPackageId);

		int usedTicketId;

		synchronized (ShipmentPackageController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ShipmentPackageDeleted.class,
				event -> sendShipmentPackageChangedMessage(((ShipmentPackageDeleted) event).isSuccess(), usedTicketId));

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

	public void sendShipmentPackageChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/shipmentPackage/\" plus one of the following: "
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
