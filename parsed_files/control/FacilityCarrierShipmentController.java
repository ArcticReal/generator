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
import com.skytala.eCommerce.command.AddFacilityCarrierShipment;
import com.skytala.eCommerce.command.DeleteFacilityCarrierShipment;
import com.skytala.eCommerce.command.UpdateFacilityCarrierShipment;
import com.skytala.eCommerce.entity.FacilityCarrierShipment;
import com.skytala.eCommerce.entity.FacilityCarrierShipmentMapper;
import com.skytala.eCommerce.event.FacilityCarrierShipmentAdded;
import com.skytala.eCommerce.event.FacilityCarrierShipmentDeleted;
import com.skytala.eCommerce.event.FacilityCarrierShipmentFound;
import com.skytala.eCommerce.event.FacilityCarrierShipmentUpdated;
import com.skytala.eCommerce.query.FindFacilityCarrierShipmentsBy;

@RestController
@RequestMapping("/api/facilityCarrierShipment")
public class FacilityCarrierShipmentController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<FacilityCarrierShipment>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public FacilityCarrierShipmentController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a FacilityCarrierShipment
	 * @return a List with the FacilityCarrierShipments
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<FacilityCarrierShipment> findFacilityCarrierShipmentsBy(@RequestParam Map<String, String> allRequestParams) {

		FindFacilityCarrierShipmentsBy query = new FindFacilityCarrierShipmentsBy(allRequestParams);

		int usedTicketId;

		synchronized (FacilityCarrierShipmentController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(FacilityCarrierShipmentFound.class,
				event -> sendFacilityCarrierShipmentsFoundMessage(((FacilityCarrierShipmentFound) event).getFacilityCarrierShipments(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendFacilityCarrierShipmentsFoundMessage(List<FacilityCarrierShipment> facilityCarrierShipments, int usedTicketId) {
		queryReturnVal.put(usedTicketId, facilityCarrierShipments);
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
	public boolean createFacilityCarrierShipment(HttpServletRequest request) {

		FacilityCarrierShipment facilityCarrierShipmentToBeAdded = new FacilityCarrierShipment();
		try {
			facilityCarrierShipmentToBeAdded = FacilityCarrierShipmentMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createFacilityCarrierShipment(facilityCarrierShipmentToBeAdded);

	}

	/**
	 * creates a new FacilityCarrierShipment entry in the ofbiz database
	 * 
	 * @param facilityCarrierShipmentToBeAdded
	 *            the FacilityCarrierShipment thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createFacilityCarrierShipment(FacilityCarrierShipment facilityCarrierShipmentToBeAdded) {

		AddFacilityCarrierShipment com = new AddFacilityCarrierShipment(facilityCarrierShipmentToBeAdded);
		int usedTicketId;

		synchronized (FacilityCarrierShipmentController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(FacilityCarrierShipmentAdded.class,
				event -> sendFacilityCarrierShipmentChangedMessage(((FacilityCarrierShipmentAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateFacilityCarrierShipment(HttpServletRequest request) {

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

		FacilityCarrierShipment facilityCarrierShipmentToBeUpdated = new FacilityCarrierShipment();

		try {
			facilityCarrierShipmentToBeUpdated = FacilityCarrierShipmentMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateFacilityCarrierShipment(facilityCarrierShipmentToBeUpdated);

	}

	/**
	 * Updates the FacilityCarrierShipment with the specific Id
	 * 
	 * @param facilityCarrierShipmentToBeUpdated the FacilityCarrierShipment thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateFacilityCarrierShipment(FacilityCarrierShipment facilityCarrierShipmentToBeUpdated) {

		UpdateFacilityCarrierShipment com = new UpdateFacilityCarrierShipment(facilityCarrierShipmentToBeUpdated);

		int usedTicketId;

		synchronized (FacilityCarrierShipmentController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(FacilityCarrierShipmentUpdated.class,
				event -> sendFacilityCarrierShipmentChangedMessage(((FacilityCarrierShipmentUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a FacilityCarrierShipment from the database
	 * 
	 * @param facilityCarrierShipmentId:
	 *            the id of the FacilityCarrierShipment thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deletefacilityCarrierShipmentById(@RequestParam(value = "facilityCarrierShipmentId") String facilityCarrierShipmentId) {

		DeleteFacilityCarrierShipment com = new DeleteFacilityCarrierShipment(facilityCarrierShipmentId);

		int usedTicketId;

		synchronized (FacilityCarrierShipmentController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(FacilityCarrierShipmentDeleted.class,
				event -> sendFacilityCarrierShipmentChangedMessage(((FacilityCarrierShipmentDeleted) event).isSuccess(), usedTicketId));

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

	public void sendFacilityCarrierShipmentChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/facilityCarrierShipment/\" plus one of the following: "
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
