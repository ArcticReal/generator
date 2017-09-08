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
import com.skytala.eCommerce.command.AddCarrierShipmentBoxType;
import com.skytala.eCommerce.command.DeleteCarrierShipmentBoxType;
import com.skytala.eCommerce.command.UpdateCarrierShipmentBoxType;
import com.skytala.eCommerce.entity.CarrierShipmentBoxType;
import com.skytala.eCommerce.entity.CarrierShipmentBoxTypeMapper;
import com.skytala.eCommerce.event.CarrierShipmentBoxTypeAdded;
import com.skytala.eCommerce.event.CarrierShipmentBoxTypeDeleted;
import com.skytala.eCommerce.event.CarrierShipmentBoxTypeFound;
import com.skytala.eCommerce.event.CarrierShipmentBoxTypeUpdated;
import com.skytala.eCommerce.query.FindCarrierShipmentBoxTypesBy;

@RestController
@RequestMapping("/api/carrierShipmentBoxType")
public class CarrierShipmentBoxTypeController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<CarrierShipmentBoxType>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public CarrierShipmentBoxTypeController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a CarrierShipmentBoxType
	 * @return a List with the CarrierShipmentBoxTypes
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<CarrierShipmentBoxType> findCarrierShipmentBoxTypesBy(@RequestParam Map<String, String> allRequestParams) {

		FindCarrierShipmentBoxTypesBy query = new FindCarrierShipmentBoxTypesBy(allRequestParams);

		int usedTicketId;

		synchronized (CarrierShipmentBoxTypeController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(CarrierShipmentBoxTypeFound.class,
				event -> sendCarrierShipmentBoxTypesFoundMessage(((CarrierShipmentBoxTypeFound) event).getCarrierShipmentBoxTypes(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendCarrierShipmentBoxTypesFoundMessage(List<CarrierShipmentBoxType> carrierShipmentBoxTypes, int usedTicketId) {
		queryReturnVal.put(usedTicketId, carrierShipmentBoxTypes);
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
	public boolean createCarrierShipmentBoxType(HttpServletRequest request) {

		CarrierShipmentBoxType carrierShipmentBoxTypeToBeAdded = new CarrierShipmentBoxType();
		try {
			carrierShipmentBoxTypeToBeAdded = CarrierShipmentBoxTypeMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createCarrierShipmentBoxType(carrierShipmentBoxTypeToBeAdded);

	}

	/**
	 * creates a new CarrierShipmentBoxType entry in the ofbiz database
	 * 
	 * @param carrierShipmentBoxTypeToBeAdded
	 *            the CarrierShipmentBoxType thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createCarrierShipmentBoxType(CarrierShipmentBoxType carrierShipmentBoxTypeToBeAdded) {

		AddCarrierShipmentBoxType com = new AddCarrierShipmentBoxType(carrierShipmentBoxTypeToBeAdded);
		int usedTicketId;

		synchronized (CarrierShipmentBoxTypeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(CarrierShipmentBoxTypeAdded.class,
				event -> sendCarrierShipmentBoxTypeChangedMessage(((CarrierShipmentBoxTypeAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateCarrierShipmentBoxType(HttpServletRequest request) {

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

		CarrierShipmentBoxType carrierShipmentBoxTypeToBeUpdated = new CarrierShipmentBoxType();

		try {
			carrierShipmentBoxTypeToBeUpdated = CarrierShipmentBoxTypeMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateCarrierShipmentBoxType(carrierShipmentBoxTypeToBeUpdated);

	}

	/**
	 * Updates the CarrierShipmentBoxType with the specific Id
	 * 
	 * @param carrierShipmentBoxTypeToBeUpdated the CarrierShipmentBoxType thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateCarrierShipmentBoxType(CarrierShipmentBoxType carrierShipmentBoxTypeToBeUpdated) {

		UpdateCarrierShipmentBoxType com = new UpdateCarrierShipmentBoxType(carrierShipmentBoxTypeToBeUpdated);

		int usedTicketId;

		synchronized (CarrierShipmentBoxTypeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(CarrierShipmentBoxTypeUpdated.class,
				event -> sendCarrierShipmentBoxTypeChangedMessage(((CarrierShipmentBoxTypeUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a CarrierShipmentBoxType from the database
	 * 
	 * @param carrierShipmentBoxTypeId:
	 *            the id of the CarrierShipmentBoxType thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deletecarrierShipmentBoxTypeById(@RequestParam(value = "carrierShipmentBoxTypeId") String carrierShipmentBoxTypeId) {

		DeleteCarrierShipmentBoxType com = new DeleteCarrierShipmentBoxType(carrierShipmentBoxTypeId);

		int usedTicketId;

		synchronized (CarrierShipmentBoxTypeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(CarrierShipmentBoxTypeDeleted.class,
				event -> sendCarrierShipmentBoxTypeChangedMessage(((CarrierShipmentBoxTypeDeleted) event).isSuccess(), usedTicketId));

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

	public void sendCarrierShipmentBoxTypeChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/carrierShipmentBoxType/\" plus one of the following: "
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
