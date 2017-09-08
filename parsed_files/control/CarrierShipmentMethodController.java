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
import com.skytala.eCommerce.command.AddCarrierShipmentMethod;
import com.skytala.eCommerce.command.DeleteCarrierShipmentMethod;
import com.skytala.eCommerce.command.UpdateCarrierShipmentMethod;
import com.skytala.eCommerce.entity.CarrierShipmentMethod;
import com.skytala.eCommerce.entity.CarrierShipmentMethodMapper;
import com.skytala.eCommerce.event.CarrierShipmentMethodAdded;
import com.skytala.eCommerce.event.CarrierShipmentMethodDeleted;
import com.skytala.eCommerce.event.CarrierShipmentMethodFound;
import com.skytala.eCommerce.event.CarrierShipmentMethodUpdated;
import com.skytala.eCommerce.query.FindCarrierShipmentMethodsBy;

@RestController
@RequestMapping("/api/carrierShipmentMethod")
public class CarrierShipmentMethodController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<CarrierShipmentMethod>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public CarrierShipmentMethodController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a CarrierShipmentMethod
	 * @return a List with the CarrierShipmentMethods
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<CarrierShipmentMethod> findCarrierShipmentMethodsBy(@RequestParam Map<String, String> allRequestParams) {

		FindCarrierShipmentMethodsBy query = new FindCarrierShipmentMethodsBy(allRequestParams);

		int usedTicketId;

		synchronized (CarrierShipmentMethodController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(CarrierShipmentMethodFound.class,
				event -> sendCarrierShipmentMethodsFoundMessage(((CarrierShipmentMethodFound) event).getCarrierShipmentMethods(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendCarrierShipmentMethodsFoundMessage(List<CarrierShipmentMethod> carrierShipmentMethods, int usedTicketId) {
		queryReturnVal.put(usedTicketId, carrierShipmentMethods);
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
	public boolean createCarrierShipmentMethod(HttpServletRequest request) {

		CarrierShipmentMethod carrierShipmentMethodToBeAdded = new CarrierShipmentMethod();
		try {
			carrierShipmentMethodToBeAdded = CarrierShipmentMethodMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createCarrierShipmentMethod(carrierShipmentMethodToBeAdded);

	}

	/**
	 * creates a new CarrierShipmentMethod entry in the ofbiz database
	 * 
	 * @param carrierShipmentMethodToBeAdded
	 *            the CarrierShipmentMethod thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createCarrierShipmentMethod(CarrierShipmentMethod carrierShipmentMethodToBeAdded) {

		AddCarrierShipmentMethod com = new AddCarrierShipmentMethod(carrierShipmentMethodToBeAdded);
		int usedTicketId;

		synchronized (CarrierShipmentMethodController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(CarrierShipmentMethodAdded.class,
				event -> sendCarrierShipmentMethodChangedMessage(((CarrierShipmentMethodAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateCarrierShipmentMethod(HttpServletRequest request) {

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

		CarrierShipmentMethod carrierShipmentMethodToBeUpdated = new CarrierShipmentMethod();

		try {
			carrierShipmentMethodToBeUpdated = CarrierShipmentMethodMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateCarrierShipmentMethod(carrierShipmentMethodToBeUpdated);

	}

	/**
	 * Updates the CarrierShipmentMethod with the specific Id
	 * 
	 * @param carrierShipmentMethodToBeUpdated the CarrierShipmentMethod thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateCarrierShipmentMethod(CarrierShipmentMethod carrierShipmentMethodToBeUpdated) {

		UpdateCarrierShipmentMethod com = new UpdateCarrierShipmentMethod(carrierShipmentMethodToBeUpdated);

		int usedTicketId;

		synchronized (CarrierShipmentMethodController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(CarrierShipmentMethodUpdated.class,
				event -> sendCarrierShipmentMethodChangedMessage(((CarrierShipmentMethodUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a CarrierShipmentMethod from the database
	 * 
	 * @param carrierShipmentMethodId:
	 *            the id of the CarrierShipmentMethod thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deletecarrierShipmentMethodById(@RequestParam(value = "carrierShipmentMethodId") String carrierShipmentMethodId) {

		DeleteCarrierShipmentMethod com = new DeleteCarrierShipmentMethod(carrierShipmentMethodId);

		int usedTicketId;

		synchronized (CarrierShipmentMethodController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(CarrierShipmentMethodDeleted.class,
				event -> sendCarrierShipmentMethodChangedMessage(((CarrierShipmentMethodDeleted) event).isSuccess(), usedTicketId));

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

	public void sendCarrierShipmentMethodChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/carrierShipmentMethod/\" plus one of the following: "
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
