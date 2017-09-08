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
import com.skytala.eCommerce.command.AddShipmentTypeAttr;
import com.skytala.eCommerce.command.DeleteShipmentTypeAttr;
import com.skytala.eCommerce.command.UpdateShipmentTypeAttr;
import com.skytala.eCommerce.entity.ShipmentTypeAttr;
import com.skytala.eCommerce.entity.ShipmentTypeAttrMapper;
import com.skytala.eCommerce.event.ShipmentTypeAttrAdded;
import com.skytala.eCommerce.event.ShipmentTypeAttrDeleted;
import com.skytala.eCommerce.event.ShipmentTypeAttrFound;
import com.skytala.eCommerce.event.ShipmentTypeAttrUpdated;
import com.skytala.eCommerce.query.FindShipmentTypeAttrsBy;

@RestController
@RequestMapping("/api/shipmentTypeAttr")
public class ShipmentTypeAttrController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<ShipmentTypeAttr>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public ShipmentTypeAttrController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a ShipmentTypeAttr
	 * @return a List with the ShipmentTypeAttrs
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<ShipmentTypeAttr> findShipmentTypeAttrsBy(@RequestParam Map<String, String> allRequestParams) {

		FindShipmentTypeAttrsBy query = new FindShipmentTypeAttrsBy(allRequestParams);

		int usedTicketId;

		synchronized (ShipmentTypeAttrController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ShipmentTypeAttrFound.class,
				event -> sendShipmentTypeAttrsFoundMessage(((ShipmentTypeAttrFound) event).getShipmentTypeAttrs(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendShipmentTypeAttrsFoundMessage(List<ShipmentTypeAttr> shipmentTypeAttrs, int usedTicketId) {
		queryReturnVal.put(usedTicketId, shipmentTypeAttrs);
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
	public boolean createShipmentTypeAttr(HttpServletRequest request) {

		ShipmentTypeAttr shipmentTypeAttrToBeAdded = new ShipmentTypeAttr();
		try {
			shipmentTypeAttrToBeAdded = ShipmentTypeAttrMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createShipmentTypeAttr(shipmentTypeAttrToBeAdded);

	}

	/**
	 * creates a new ShipmentTypeAttr entry in the ofbiz database
	 * 
	 * @param shipmentTypeAttrToBeAdded
	 *            the ShipmentTypeAttr thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createShipmentTypeAttr(ShipmentTypeAttr shipmentTypeAttrToBeAdded) {

		AddShipmentTypeAttr com = new AddShipmentTypeAttr(shipmentTypeAttrToBeAdded);
		int usedTicketId;

		synchronized (ShipmentTypeAttrController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ShipmentTypeAttrAdded.class,
				event -> sendShipmentTypeAttrChangedMessage(((ShipmentTypeAttrAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateShipmentTypeAttr(HttpServletRequest request) {

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

		ShipmentTypeAttr shipmentTypeAttrToBeUpdated = new ShipmentTypeAttr();

		try {
			shipmentTypeAttrToBeUpdated = ShipmentTypeAttrMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateShipmentTypeAttr(shipmentTypeAttrToBeUpdated);

	}

	/**
	 * Updates the ShipmentTypeAttr with the specific Id
	 * 
	 * @param shipmentTypeAttrToBeUpdated the ShipmentTypeAttr thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateShipmentTypeAttr(ShipmentTypeAttr shipmentTypeAttrToBeUpdated) {

		UpdateShipmentTypeAttr com = new UpdateShipmentTypeAttr(shipmentTypeAttrToBeUpdated);

		int usedTicketId;

		synchronized (ShipmentTypeAttrController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ShipmentTypeAttrUpdated.class,
				event -> sendShipmentTypeAttrChangedMessage(((ShipmentTypeAttrUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a ShipmentTypeAttr from the database
	 * 
	 * @param shipmentTypeAttrId:
	 *            the id of the ShipmentTypeAttr thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deleteshipmentTypeAttrById(@RequestParam(value = "shipmentTypeAttrId") String shipmentTypeAttrId) {

		DeleteShipmentTypeAttr com = new DeleteShipmentTypeAttr(shipmentTypeAttrId);

		int usedTicketId;

		synchronized (ShipmentTypeAttrController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ShipmentTypeAttrDeleted.class,
				event -> sendShipmentTypeAttrChangedMessage(((ShipmentTypeAttrDeleted) event).isSuccess(), usedTicketId));

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

	public void sendShipmentTypeAttrChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/shipmentTypeAttr/\" plus one of the following: "
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
