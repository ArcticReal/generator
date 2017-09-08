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
import com.skytala.eCommerce.command.AddShipmentGatewayConfig;
import com.skytala.eCommerce.command.DeleteShipmentGatewayConfig;
import com.skytala.eCommerce.command.UpdateShipmentGatewayConfig;
import com.skytala.eCommerce.entity.ShipmentGatewayConfig;
import com.skytala.eCommerce.entity.ShipmentGatewayConfigMapper;
import com.skytala.eCommerce.event.ShipmentGatewayConfigAdded;
import com.skytala.eCommerce.event.ShipmentGatewayConfigDeleted;
import com.skytala.eCommerce.event.ShipmentGatewayConfigFound;
import com.skytala.eCommerce.event.ShipmentGatewayConfigUpdated;
import com.skytala.eCommerce.query.FindShipmentGatewayConfigsBy;

@RestController
@RequestMapping("/api/shipmentGatewayConfig")
public class ShipmentGatewayConfigController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<ShipmentGatewayConfig>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public ShipmentGatewayConfigController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a ShipmentGatewayConfig
	 * @return a List with the ShipmentGatewayConfigs
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<ShipmentGatewayConfig> findShipmentGatewayConfigsBy(@RequestParam Map<String, String> allRequestParams) {

		FindShipmentGatewayConfigsBy query = new FindShipmentGatewayConfigsBy(allRequestParams);

		int usedTicketId;

		synchronized (ShipmentGatewayConfigController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ShipmentGatewayConfigFound.class,
				event -> sendShipmentGatewayConfigsFoundMessage(((ShipmentGatewayConfigFound) event).getShipmentGatewayConfigs(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendShipmentGatewayConfigsFoundMessage(List<ShipmentGatewayConfig> shipmentGatewayConfigs, int usedTicketId) {
		queryReturnVal.put(usedTicketId, shipmentGatewayConfigs);
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
	public boolean createShipmentGatewayConfig(HttpServletRequest request) {

		ShipmentGatewayConfig shipmentGatewayConfigToBeAdded = new ShipmentGatewayConfig();
		try {
			shipmentGatewayConfigToBeAdded = ShipmentGatewayConfigMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createShipmentGatewayConfig(shipmentGatewayConfigToBeAdded);

	}

	/**
	 * creates a new ShipmentGatewayConfig entry in the ofbiz database
	 * 
	 * @param shipmentGatewayConfigToBeAdded
	 *            the ShipmentGatewayConfig thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createShipmentGatewayConfig(ShipmentGatewayConfig shipmentGatewayConfigToBeAdded) {

		AddShipmentGatewayConfig com = new AddShipmentGatewayConfig(shipmentGatewayConfigToBeAdded);
		int usedTicketId;

		synchronized (ShipmentGatewayConfigController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ShipmentGatewayConfigAdded.class,
				event -> sendShipmentGatewayConfigChangedMessage(((ShipmentGatewayConfigAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateShipmentGatewayConfig(HttpServletRequest request) {

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

		ShipmentGatewayConfig shipmentGatewayConfigToBeUpdated = new ShipmentGatewayConfig();

		try {
			shipmentGatewayConfigToBeUpdated = ShipmentGatewayConfigMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateShipmentGatewayConfig(shipmentGatewayConfigToBeUpdated);

	}

	/**
	 * Updates the ShipmentGatewayConfig with the specific Id
	 * 
	 * @param shipmentGatewayConfigToBeUpdated the ShipmentGatewayConfig thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateShipmentGatewayConfig(ShipmentGatewayConfig shipmentGatewayConfigToBeUpdated) {

		UpdateShipmentGatewayConfig com = new UpdateShipmentGatewayConfig(shipmentGatewayConfigToBeUpdated);

		int usedTicketId;

		synchronized (ShipmentGatewayConfigController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ShipmentGatewayConfigUpdated.class,
				event -> sendShipmentGatewayConfigChangedMessage(((ShipmentGatewayConfigUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a ShipmentGatewayConfig from the database
	 * 
	 * @param shipmentGatewayConfigId:
	 *            the id of the ShipmentGatewayConfig thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deleteshipmentGatewayConfigById(@RequestParam(value = "shipmentGatewayConfigId") String shipmentGatewayConfigId) {

		DeleteShipmentGatewayConfig com = new DeleteShipmentGatewayConfig(shipmentGatewayConfigId);

		int usedTicketId;

		synchronized (ShipmentGatewayConfigController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ShipmentGatewayConfigDeleted.class,
				event -> sendShipmentGatewayConfigChangedMessage(((ShipmentGatewayConfigDeleted) event).isSuccess(), usedTicketId));

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

	public void sendShipmentGatewayConfigChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/shipmentGatewayConfig/\" plus one of the following: "
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
