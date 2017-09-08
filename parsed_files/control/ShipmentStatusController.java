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
import com.skytala.eCommerce.command.AddShipmentStatus;
import com.skytala.eCommerce.command.DeleteShipmentStatus;
import com.skytala.eCommerce.command.UpdateShipmentStatus;
import com.skytala.eCommerce.entity.ShipmentStatus;
import com.skytala.eCommerce.entity.ShipmentStatusMapper;
import com.skytala.eCommerce.event.ShipmentStatusAdded;
import com.skytala.eCommerce.event.ShipmentStatusDeleted;
import com.skytala.eCommerce.event.ShipmentStatusFound;
import com.skytala.eCommerce.event.ShipmentStatusUpdated;
import com.skytala.eCommerce.query.FindShipmentStatussBy;

@RestController
@RequestMapping("/api/shipmentStatus")
public class ShipmentStatusController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<ShipmentStatus>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public ShipmentStatusController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a ShipmentStatus
	 * @return a List with the ShipmentStatuss
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<ShipmentStatus> findShipmentStatussBy(@RequestParam Map<String, String> allRequestParams) {

		FindShipmentStatussBy query = new FindShipmentStatussBy(allRequestParams);

		int usedTicketId;

		synchronized (ShipmentStatusController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ShipmentStatusFound.class,
				event -> sendShipmentStatussFoundMessage(((ShipmentStatusFound) event).getShipmentStatuss(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendShipmentStatussFoundMessage(List<ShipmentStatus> shipmentStatuss, int usedTicketId) {
		queryReturnVal.put(usedTicketId, shipmentStatuss);
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
	public boolean createShipmentStatus(HttpServletRequest request) {

		ShipmentStatus shipmentStatusToBeAdded = new ShipmentStatus();
		try {
			shipmentStatusToBeAdded = ShipmentStatusMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createShipmentStatus(shipmentStatusToBeAdded);

	}

	/**
	 * creates a new ShipmentStatus entry in the ofbiz database
	 * 
	 * @param shipmentStatusToBeAdded
	 *            the ShipmentStatus thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createShipmentStatus(ShipmentStatus shipmentStatusToBeAdded) {

		AddShipmentStatus com = new AddShipmentStatus(shipmentStatusToBeAdded);
		int usedTicketId;

		synchronized (ShipmentStatusController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ShipmentStatusAdded.class,
				event -> sendShipmentStatusChangedMessage(((ShipmentStatusAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateShipmentStatus(HttpServletRequest request) {

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

		ShipmentStatus shipmentStatusToBeUpdated = new ShipmentStatus();

		try {
			shipmentStatusToBeUpdated = ShipmentStatusMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateShipmentStatus(shipmentStatusToBeUpdated);

	}

	/**
	 * Updates the ShipmentStatus with the specific Id
	 * 
	 * @param shipmentStatusToBeUpdated the ShipmentStatus thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateShipmentStatus(ShipmentStatus shipmentStatusToBeUpdated) {

		UpdateShipmentStatus com = new UpdateShipmentStatus(shipmentStatusToBeUpdated);

		int usedTicketId;

		synchronized (ShipmentStatusController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ShipmentStatusUpdated.class,
				event -> sendShipmentStatusChangedMessage(((ShipmentStatusUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a ShipmentStatus from the database
	 * 
	 * @param shipmentStatusId:
	 *            the id of the ShipmentStatus thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deleteshipmentStatusById(@RequestParam(value = "shipmentStatusId") String shipmentStatusId) {

		DeleteShipmentStatus com = new DeleteShipmentStatus(shipmentStatusId);

		int usedTicketId;

		synchronized (ShipmentStatusController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ShipmentStatusDeleted.class,
				event -> sendShipmentStatusChangedMessage(((ShipmentStatusDeleted) event).isSuccess(), usedTicketId));

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

	public void sendShipmentStatusChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/shipmentStatus/\" plus one of the following: "
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
