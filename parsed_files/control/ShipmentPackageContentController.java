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
import com.skytala.eCommerce.command.AddShipmentPackageContent;
import com.skytala.eCommerce.command.DeleteShipmentPackageContent;
import com.skytala.eCommerce.command.UpdateShipmentPackageContent;
import com.skytala.eCommerce.entity.ShipmentPackageContent;
import com.skytala.eCommerce.entity.ShipmentPackageContentMapper;
import com.skytala.eCommerce.event.ShipmentPackageContentAdded;
import com.skytala.eCommerce.event.ShipmentPackageContentDeleted;
import com.skytala.eCommerce.event.ShipmentPackageContentFound;
import com.skytala.eCommerce.event.ShipmentPackageContentUpdated;
import com.skytala.eCommerce.query.FindShipmentPackageContentsBy;

@RestController
@RequestMapping("/api/shipmentPackageContent")
public class ShipmentPackageContentController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<ShipmentPackageContent>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public ShipmentPackageContentController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a ShipmentPackageContent
	 * @return a List with the ShipmentPackageContents
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<ShipmentPackageContent> findShipmentPackageContentsBy(@RequestParam Map<String, String> allRequestParams) {

		FindShipmentPackageContentsBy query = new FindShipmentPackageContentsBy(allRequestParams);

		int usedTicketId;

		synchronized (ShipmentPackageContentController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ShipmentPackageContentFound.class,
				event -> sendShipmentPackageContentsFoundMessage(((ShipmentPackageContentFound) event).getShipmentPackageContents(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendShipmentPackageContentsFoundMessage(List<ShipmentPackageContent> shipmentPackageContents, int usedTicketId) {
		queryReturnVal.put(usedTicketId, shipmentPackageContents);
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
	public boolean createShipmentPackageContent(HttpServletRequest request) {

		ShipmentPackageContent shipmentPackageContentToBeAdded = new ShipmentPackageContent();
		try {
			shipmentPackageContentToBeAdded = ShipmentPackageContentMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createShipmentPackageContent(shipmentPackageContentToBeAdded);

	}

	/**
	 * creates a new ShipmentPackageContent entry in the ofbiz database
	 * 
	 * @param shipmentPackageContentToBeAdded
	 *            the ShipmentPackageContent thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createShipmentPackageContent(ShipmentPackageContent shipmentPackageContentToBeAdded) {

		AddShipmentPackageContent com = new AddShipmentPackageContent(shipmentPackageContentToBeAdded);
		int usedTicketId;

		synchronized (ShipmentPackageContentController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ShipmentPackageContentAdded.class,
				event -> sendShipmentPackageContentChangedMessage(((ShipmentPackageContentAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateShipmentPackageContent(HttpServletRequest request) {

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

		ShipmentPackageContent shipmentPackageContentToBeUpdated = new ShipmentPackageContent();

		try {
			shipmentPackageContentToBeUpdated = ShipmentPackageContentMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateShipmentPackageContent(shipmentPackageContentToBeUpdated);

	}

	/**
	 * Updates the ShipmentPackageContent with the specific Id
	 * 
	 * @param shipmentPackageContentToBeUpdated the ShipmentPackageContent thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateShipmentPackageContent(ShipmentPackageContent shipmentPackageContentToBeUpdated) {

		UpdateShipmentPackageContent com = new UpdateShipmentPackageContent(shipmentPackageContentToBeUpdated);

		int usedTicketId;

		synchronized (ShipmentPackageContentController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ShipmentPackageContentUpdated.class,
				event -> sendShipmentPackageContentChangedMessage(((ShipmentPackageContentUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a ShipmentPackageContent from the database
	 * 
	 * @param shipmentPackageContentId:
	 *            the id of the ShipmentPackageContent thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deleteshipmentPackageContentById(@RequestParam(value = "shipmentPackageContentId") String shipmentPackageContentId) {

		DeleteShipmentPackageContent com = new DeleteShipmentPackageContent(shipmentPackageContentId);

		int usedTicketId;

		synchronized (ShipmentPackageContentController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ShipmentPackageContentDeleted.class,
				event -> sendShipmentPackageContentChangedMessage(((ShipmentPackageContentDeleted) event).isSuccess(), usedTicketId));

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

	public void sendShipmentPackageContentChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/shipmentPackageContent/\" plus one of the following: "
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
