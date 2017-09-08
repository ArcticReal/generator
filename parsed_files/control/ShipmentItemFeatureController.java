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
import com.skytala.eCommerce.command.AddShipmentItemFeature;
import com.skytala.eCommerce.command.DeleteShipmentItemFeature;
import com.skytala.eCommerce.command.UpdateShipmentItemFeature;
import com.skytala.eCommerce.entity.ShipmentItemFeature;
import com.skytala.eCommerce.entity.ShipmentItemFeatureMapper;
import com.skytala.eCommerce.event.ShipmentItemFeatureAdded;
import com.skytala.eCommerce.event.ShipmentItemFeatureDeleted;
import com.skytala.eCommerce.event.ShipmentItemFeatureFound;
import com.skytala.eCommerce.event.ShipmentItemFeatureUpdated;
import com.skytala.eCommerce.query.FindShipmentItemFeaturesBy;

@RestController
@RequestMapping("/api/shipmentItemFeature")
public class ShipmentItemFeatureController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<ShipmentItemFeature>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public ShipmentItemFeatureController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a ShipmentItemFeature
	 * @return a List with the ShipmentItemFeatures
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<ShipmentItemFeature> findShipmentItemFeaturesBy(@RequestParam Map<String, String> allRequestParams) {

		FindShipmentItemFeaturesBy query = new FindShipmentItemFeaturesBy(allRequestParams);

		int usedTicketId;

		synchronized (ShipmentItemFeatureController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ShipmentItemFeatureFound.class,
				event -> sendShipmentItemFeaturesFoundMessage(((ShipmentItemFeatureFound) event).getShipmentItemFeatures(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendShipmentItemFeaturesFoundMessage(List<ShipmentItemFeature> shipmentItemFeatures, int usedTicketId) {
		queryReturnVal.put(usedTicketId, shipmentItemFeatures);
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
	public boolean createShipmentItemFeature(HttpServletRequest request) {

		ShipmentItemFeature shipmentItemFeatureToBeAdded = new ShipmentItemFeature();
		try {
			shipmentItemFeatureToBeAdded = ShipmentItemFeatureMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createShipmentItemFeature(shipmentItemFeatureToBeAdded);

	}

	/**
	 * creates a new ShipmentItemFeature entry in the ofbiz database
	 * 
	 * @param shipmentItemFeatureToBeAdded
	 *            the ShipmentItemFeature thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createShipmentItemFeature(ShipmentItemFeature shipmentItemFeatureToBeAdded) {

		AddShipmentItemFeature com = new AddShipmentItemFeature(shipmentItemFeatureToBeAdded);
		int usedTicketId;

		synchronized (ShipmentItemFeatureController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ShipmentItemFeatureAdded.class,
				event -> sendShipmentItemFeatureChangedMessage(((ShipmentItemFeatureAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateShipmentItemFeature(HttpServletRequest request) {

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

		ShipmentItemFeature shipmentItemFeatureToBeUpdated = new ShipmentItemFeature();

		try {
			shipmentItemFeatureToBeUpdated = ShipmentItemFeatureMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateShipmentItemFeature(shipmentItemFeatureToBeUpdated);

	}

	/**
	 * Updates the ShipmentItemFeature with the specific Id
	 * 
	 * @param shipmentItemFeatureToBeUpdated the ShipmentItemFeature thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateShipmentItemFeature(ShipmentItemFeature shipmentItemFeatureToBeUpdated) {

		UpdateShipmentItemFeature com = new UpdateShipmentItemFeature(shipmentItemFeatureToBeUpdated);

		int usedTicketId;

		synchronized (ShipmentItemFeatureController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ShipmentItemFeatureUpdated.class,
				event -> sendShipmentItemFeatureChangedMessage(((ShipmentItemFeatureUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a ShipmentItemFeature from the database
	 * 
	 * @param shipmentItemFeatureId:
	 *            the id of the ShipmentItemFeature thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deleteshipmentItemFeatureById(@RequestParam(value = "shipmentItemFeatureId") String shipmentItemFeatureId) {

		DeleteShipmentItemFeature com = new DeleteShipmentItemFeature(shipmentItemFeatureId);

		int usedTicketId;

		synchronized (ShipmentItemFeatureController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ShipmentItemFeatureDeleted.class,
				event -> sendShipmentItemFeatureChangedMessage(((ShipmentItemFeatureDeleted) event).isSuccess(), usedTicketId));

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

	public void sendShipmentItemFeatureChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/shipmentItemFeature/\" plus one of the following: "
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
