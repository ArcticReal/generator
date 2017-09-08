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
import com.skytala.eCommerce.command.AddShipmentAttribute;
import com.skytala.eCommerce.command.DeleteShipmentAttribute;
import com.skytala.eCommerce.command.UpdateShipmentAttribute;
import com.skytala.eCommerce.entity.ShipmentAttribute;
import com.skytala.eCommerce.entity.ShipmentAttributeMapper;
import com.skytala.eCommerce.event.ShipmentAttributeAdded;
import com.skytala.eCommerce.event.ShipmentAttributeDeleted;
import com.skytala.eCommerce.event.ShipmentAttributeFound;
import com.skytala.eCommerce.event.ShipmentAttributeUpdated;
import com.skytala.eCommerce.query.FindShipmentAttributesBy;

@RestController
@RequestMapping("/api/shipmentAttribute")
public class ShipmentAttributeController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<ShipmentAttribute>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public ShipmentAttributeController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a ShipmentAttribute
	 * @return a List with the ShipmentAttributes
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<ShipmentAttribute> findShipmentAttributesBy(@RequestParam Map<String, String> allRequestParams) {

		FindShipmentAttributesBy query = new FindShipmentAttributesBy(allRequestParams);

		int usedTicketId;

		synchronized (ShipmentAttributeController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ShipmentAttributeFound.class,
				event -> sendShipmentAttributesFoundMessage(((ShipmentAttributeFound) event).getShipmentAttributes(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendShipmentAttributesFoundMessage(List<ShipmentAttribute> shipmentAttributes, int usedTicketId) {
		queryReturnVal.put(usedTicketId, shipmentAttributes);
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
	public boolean createShipmentAttribute(HttpServletRequest request) {

		ShipmentAttribute shipmentAttributeToBeAdded = new ShipmentAttribute();
		try {
			shipmentAttributeToBeAdded = ShipmentAttributeMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createShipmentAttribute(shipmentAttributeToBeAdded);

	}

	/**
	 * creates a new ShipmentAttribute entry in the ofbiz database
	 * 
	 * @param shipmentAttributeToBeAdded
	 *            the ShipmentAttribute thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createShipmentAttribute(ShipmentAttribute shipmentAttributeToBeAdded) {

		AddShipmentAttribute com = new AddShipmentAttribute(shipmentAttributeToBeAdded);
		int usedTicketId;

		synchronized (ShipmentAttributeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ShipmentAttributeAdded.class,
				event -> sendShipmentAttributeChangedMessage(((ShipmentAttributeAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateShipmentAttribute(HttpServletRequest request) {

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

		ShipmentAttribute shipmentAttributeToBeUpdated = new ShipmentAttribute();

		try {
			shipmentAttributeToBeUpdated = ShipmentAttributeMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateShipmentAttribute(shipmentAttributeToBeUpdated);

	}

	/**
	 * Updates the ShipmentAttribute with the specific Id
	 * 
	 * @param shipmentAttributeToBeUpdated the ShipmentAttribute thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateShipmentAttribute(ShipmentAttribute shipmentAttributeToBeUpdated) {

		UpdateShipmentAttribute com = new UpdateShipmentAttribute(shipmentAttributeToBeUpdated);

		int usedTicketId;

		synchronized (ShipmentAttributeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ShipmentAttributeUpdated.class,
				event -> sendShipmentAttributeChangedMessage(((ShipmentAttributeUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a ShipmentAttribute from the database
	 * 
	 * @param shipmentAttributeId:
	 *            the id of the ShipmentAttribute thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deleteshipmentAttributeById(@RequestParam(value = "shipmentAttributeId") String shipmentAttributeId) {

		DeleteShipmentAttribute com = new DeleteShipmentAttribute(shipmentAttributeId);

		int usedTicketId;

		synchronized (ShipmentAttributeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ShipmentAttributeDeleted.class,
				event -> sendShipmentAttributeChangedMessage(((ShipmentAttributeDeleted) event).isSuccess(), usedTicketId));

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

	public void sendShipmentAttributeChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/shipmentAttribute/\" plus one of the following: "
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
