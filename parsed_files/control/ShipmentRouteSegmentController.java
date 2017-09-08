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
import com.skytala.eCommerce.command.AddShipmentRouteSegment;
import com.skytala.eCommerce.command.DeleteShipmentRouteSegment;
import com.skytala.eCommerce.command.UpdateShipmentRouteSegment;
import com.skytala.eCommerce.entity.ShipmentRouteSegment;
import com.skytala.eCommerce.entity.ShipmentRouteSegmentMapper;
import com.skytala.eCommerce.event.ShipmentRouteSegmentAdded;
import com.skytala.eCommerce.event.ShipmentRouteSegmentDeleted;
import com.skytala.eCommerce.event.ShipmentRouteSegmentFound;
import com.skytala.eCommerce.event.ShipmentRouteSegmentUpdated;
import com.skytala.eCommerce.query.FindShipmentRouteSegmentsBy;

@RestController
@RequestMapping("/api/shipmentRouteSegment")
public class ShipmentRouteSegmentController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<ShipmentRouteSegment>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public ShipmentRouteSegmentController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a ShipmentRouteSegment
	 * @return a List with the ShipmentRouteSegments
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<ShipmentRouteSegment> findShipmentRouteSegmentsBy(@RequestParam Map<String, String> allRequestParams) {

		FindShipmentRouteSegmentsBy query = new FindShipmentRouteSegmentsBy(allRequestParams);

		int usedTicketId;

		synchronized (ShipmentRouteSegmentController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ShipmentRouteSegmentFound.class,
				event -> sendShipmentRouteSegmentsFoundMessage(((ShipmentRouteSegmentFound) event).getShipmentRouteSegments(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendShipmentRouteSegmentsFoundMessage(List<ShipmentRouteSegment> shipmentRouteSegments, int usedTicketId) {
		queryReturnVal.put(usedTicketId, shipmentRouteSegments);
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
	public boolean createShipmentRouteSegment(HttpServletRequest request) {

		ShipmentRouteSegment shipmentRouteSegmentToBeAdded = new ShipmentRouteSegment();
		try {
			shipmentRouteSegmentToBeAdded = ShipmentRouteSegmentMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createShipmentRouteSegment(shipmentRouteSegmentToBeAdded);

	}

	/**
	 * creates a new ShipmentRouteSegment entry in the ofbiz database
	 * 
	 * @param shipmentRouteSegmentToBeAdded
	 *            the ShipmentRouteSegment thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createShipmentRouteSegment(ShipmentRouteSegment shipmentRouteSegmentToBeAdded) {

		AddShipmentRouteSegment com = new AddShipmentRouteSegment(shipmentRouteSegmentToBeAdded);
		int usedTicketId;

		synchronized (ShipmentRouteSegmentController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ShipmentRouteSegmentAdded.class,
				event -> sendShipmentRouteSegmentChangedMessage(((ShipmentRouteSegmentAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateShipmentRouteSegment(HttpServletRequest request) {

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

		ShipmentRouteSegment shipmentRouteSegmentToBeUpdated = new ShipmentRouteSegment();

		try {
			shipmentRouteSegmentToBeUpdated = ShipmentRouteSegmentMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateShipmentRouteSegment(shipmentRouteSegmentToBeUpdated);

	}

	/**
	 * Updates the ShipmentRouteSegment with the specific Id
	 * 
	 * @param shipmentRouteSegmentToBeUpdated the ShipmentRouteSegment thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateShipmentRouteSegment(ShipmentRouteSegment shipmentRouteSegmentToBeUpdated) {

		UpdateShipmentRouteSegment com = new UpdateShipmentRouteSegment(shipmentRouteSegmentToBeUpdated);

		int usedTicketId;

		synchronized (ShipmentRouteSegmentController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ShipmentRouteSegmentUpdated.class,
				event -> sendShipmentRouteSegmentChangedMessage(((ShipmentRouteSegmentUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a ShipmentRouteSegment from the database
	 * 
	 * @param shipmentRouteSegmentId:
	 *            the id of the ShipmentRouteSegment thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deleteshipmentRouteSegmentById(@RequestParam(value = "shipmentRouteSegmentId") String shipmentRouteSegmentId) {

		DeleteShipmentRouteSegment com = new DeleteShipmentRouteSegment(shipmentRouteSegmentId);

		int usedTicketId;

		synchronized (ShipmentRouteSegmentController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ShipmentRouteSegmentDeleted.class,
				event -> sendShipmentRouteSegmentChangedMessage(((ShipmentRouteSegmentDeleted) event).isSuccess(), usedTicketId));

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

	public void sendShipmentRouteSegmentChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/shipmentRouteSegment/\" plus one of the following: "
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
