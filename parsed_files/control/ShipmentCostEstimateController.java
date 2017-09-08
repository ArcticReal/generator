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
import com.skytala.eCommerce.command.AddShipmentCostEstimate;
import com.skytala.eCommerce.command.DeleteShipmentCostEstimate;
import com.skytala.eCommerce.command.UpdateShipmentCostEstimate;
import com.skytala.eCommerce.entity.ShipmentCostEstimate;
import com.skytala.eCommerce.entity.ShipmentCostEstimateMapper;
import com.skytala.eCommerce.event.ShipmentCostEstimateAdded;
import com.skytala.eCommerce.event.ShipmentCostEstimateDeleted;
import com.skytala.eCommerce.event.ShipmentCostEstimateFound;
import com.skytala.eCommerce.event.ShipmentCostEstimateUpdated;
import com.skytala.eCommerce.query.FindShipmentCostEstimatesBy;

@RestController
@RequestMapping("/api/shipmentCostEstimate")
public class ShipmentCostEstimateController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<ShipmentCostEstimate>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public ShipmentCostEstimateController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a ShipmentCostEstimate
	 * @return a List with the ShipmentCostEstimates
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<ShipmentCostEstimate> findShipmentCostEstimatesBy(@RequestParam Map<String, String> allRequestParams) {

		FindShipmentCostEstimatesBy query = new FindShipmentCostEstimatesBy(allRequestParams);

		int usedTicketId;

		synchronized (ShipmentCostEstimateController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ShipmentCostEstimateFound.class,
				event -> sendShipmentCostEstimatesFoundMessage(((ShipmentCostEstimateFound) event).getShipmentCostEstimates(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendShipmentCostEstimatesFoundMessage(List<ShipmentCostEstimate> shipmentCostEstimates, int usedTicketId) {
		queryReturnVal.put(usedTicketId, shipmentCostEstimates);
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
	public boolean createShipmentCostEstimate(HttpServletRequest request) {

		ShipmentCostEstimate shipmentCostEstimateToBeAdded = new ShipmentCostEstimate();
		try {
			shipmentCostEstimateToBeAdded = ShipmentCostEstimateMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createShipmentCostEstimate(shipmentCostEstimateToBeAdded);

	}

	/**
	 * creates a new ShipmentCostEstimate entry in the ofbiz database
	 * 
	 * @param shipmentCostEstimateToBeAdded
	 *            the ShipmentCostEstimate thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createShipmentCostEstimate(ShipmentCostEstimate shipmentCostEstimateToBeAdded) {

		AddShipmentCostEstimate com = new AddShipmentCostEstimate(shipmentCostEstimateToBeAdded);
		int usedTicketId;

		synchronized (ShipmentCostEstimateController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ShipmentCostEstimateAdded.class,
				event -> sendShipmentCostEstimateChangedMessage(((ShipmentCostEstimateAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateShipmentCostEstimate(HttpServletRequest request) {

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

		ShipmentCostEstimate shipmentCostEstimateToBeUpdated = new ShipmentCostEstimate();

		try {
			shipmentCostEstimateToBeUpdated = ShipmentCostEstimateMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateShipmentCostEstimate(shipmentCostEstimateToBeUpdated);

	}

	/**
	 * Updates the ShipmentCostEstimate with the specific Id
	 * 
	 * @param shipmentCostEstimateToBeUpdated the ShipmentCostEstimate thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateShipmentCostEstimate(ShipmentCostEstimate shipmentCostEstimateToBeUpdated) {

		UpdateShipmentCostEstimate com = new UpdateShipmentCostEstimate(shipmentCostEstimateToBeUpdated);

		int usedTicketId;

		synchronized (ShipmentCostEstimateController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ShipmentCostEstimateUpdated.class,
				event -> sendShipmentCostEstimateChangedMessage(((ShipmentCostEstimateUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a ShipmentCostEstimate from the database
	 * 
	 * @param shipmentCostEstimateId:
	 *            the id of the ShipmentCostEstimate thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deleteshipmentCostEstimateById(@RequestParam(value = "shipmentCostEstimateId") String shipmentCostEstimateId) {

		DeleteShipmentCostEstimate com = new DeleteShipmentCostEstimate(shipmentCostEstimateId);

		int usedTicketId;

		synchronized (ShipmentCostEstimateController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ShipmentCostEstimateDeleted.class,
				event -> sendShipmentCostEstimateChangedMessage(((ShipmentCostEstimateDeleted) event).isSuccess(), usedTicketId));

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

	public void sendShipmentCostEstimateChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/shipmentCostEstimate/\" plus one of the following: "
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
