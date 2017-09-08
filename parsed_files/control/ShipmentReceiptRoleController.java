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
import com.skytala.eCommerce.command.AddShipmentReceiptRole;
import com.skytala.eCommerce.command.DeleteShipmentReceiptRole;
import com.skytala.eCommerce.command.UpdateShipmentReceiptRole;
import com.skytala.eCommerce.entity.ShipmentReceiptRole;
import com.skytala.eCommerce.entity.ShipmentReceiptRoleMapper;
import com.skytala.eCommerce.event.ShipmentReceiptRoleAdded;
import com.skytala.eCommerce.event.ShipmentReceiptRoleDeleted;
import com.skytala.eCommerce.event.ShipmentReceiptRoleFound;
import com.skytala.eCommerce.event.ShipmentReceiptRoleUpdated;
import com.skytala.eCommerce.query.FindShipmentReceiptRolesBy;

@RestController
@RequestMapping("/api/shipmentReceiptRole")
public class ShipmentReceiptRoleController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<ShipmentReceiptRole>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public ShipmentReceiptRoleController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a ShipmentReceiptRole
	 * @return a List with the ShipmentReceiptRoles
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<ShipmentReceiptRole> findShipmentReceiptRolesBy(@RequestParam Map<String, String> allRequestParams) {

		FindShipmentReceiptRolesBy query = new FindShipmentReceiptRolesBy(allRequestParams);

		int usedTicketId;

		synchronized (ShipmentReceiptRoleController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ShipmentReceiptRoleFound.class,
				event -> sendShipmentReceiptRolesFoundMessage(((ShipmentReceiptRoleFound) event).getShipmentReceiptRoles(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendShipmentReceiptRolesFoundMessage(List<ShipmentReceiptRole> shipmentReceiptRoles, int usedTicketId) {
		queryReturnVal.put(usedTicketId, shipmentReceiptRoles);
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
	public boolean createShipmentReceiptRole(HttpServletRequest request) {

		ShipmentReceiptRole shipmentReceiptRoleToBeAdded = new ShipmentReceiptRole();
		try {
			shipmentReceiptRoleToBeAdded = ShipmentReceiptRoleMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createShipmentReceiptRole(shipmentReceiptRoleToBeAdded);

	}

	/**
	 * creates a new ShipmentReceiptRole entry in the ofbiz database
	 * 
	 * @param shipmentReceiptRoleToBeAdded
	 *            the ShipmentReceiptRole thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createShipmentReceiptRole(ShipmentReceiptRole shipmentReceiptRoleToBeAdded) {

		AddShipmentReceiptRole com = new AddShipmentReceiptRole(shipmentReceiptRoleToBeAdded);
		int usedTicketId;

		synchronized (ShipmentReceiptRoleController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ShipmentReceiptRoleAdded.class,
				event -> sendShipmentReceiptRoleChangedMessage(((ShipmentReceiptRoleAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateShipmentReceiptRole(HttpServletRequest request) {

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

		ShipmentReceiptRole shipmentReceiptRoleToBeUpdated = new ShipmentReceiptRole();

		try {
			shipmentReceiptRoleToBeUpdated = ShipmentReceiptRoleMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateShipmentReceiptRole(shipmentReceiptRoleToBeUpdated);

	}

	/**
	 * Updates the ShipmentReceiptRole with the specific Id
	 * 
	 * @param shipmentReceiptRoleToBeUpdated the ShipmentReceiptRole thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateShipmentReceiptRole(ShipmentReceiptRole shipmentReceiptRoleToBeUpdated) {

		UpdateShipmentReceiptRole com = new UpdateShipmentReceiptRole(shipmentReceiptRoleToBeUpdated);

		int usedTicketId;

		synchronized (ShipmentReceiptRoleController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ShipmentReceiptRoleUpdated.class,
				event -> sendShipmentReceiptRoleChangedMessage(((ShipmentReceiptRoleUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a ShipmentReceiptRole from the database
	 * 
	 * @param shipmentReceiptRoleId:
	 *            the id of the ShipmentReceiptRole thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deleteshipmentReceiptRoleById(@RequestParam(value = "shipmentReceiptRoleId") String shipmentReceiptRoleId) {

		DeleteShipmentReceiptRole com = new DeleteShipmentReceiptRole(shipmentReceiptRoleId);

		int usedTicketId;

		synchronized (ShipmentReceiptRoleController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ShipmentReceiptRoleDeleted.class,
				event -> sendShipmentReceiptRoleChangedMessage(((ShipmentReceiptRoleDeleted) event).isSuccess(), usedTicketId));

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

	public void sendShipmentReceiptRoleChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/shipmentReceiptRole/\" plus one of the following: "
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
