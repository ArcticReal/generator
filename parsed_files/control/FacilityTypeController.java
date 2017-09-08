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
import com.skytala.eCommerce.command.AddFacilityType;
import com.skytala.eCommerce.command.DeleteFacilityType;
import com.skytala.eCommerce.command.UpdateFacilityType;
import com.skytala.eCommerce.entity.FacilityType;
import com.skytala.eCommerce.entity.FacilityTypeMapper;
import com.skytala.eCommerce.event.FacilityTypeAdded;
import com.skytala.eCommerce.event.FacilityTypeDeleted;
import com.skytala.eCommerce.event.FacilityTypeFound;
import com.skytala.eCommerce.event.FacilityTypeUpdated;
import com.skytala.eCommerce.query.FindFacilityTypesBy;

@RestController
@RequestMapping("/api/facilityType")
public class FacilityTypeController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<FacilityType>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public FacilityTypeController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a FacilityType
	 * @return a List with the FacilityTypes
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<FacilityType> findFacilityTypesBy(@RequestParam Map<String, String> allRequestParams) {

		FindFacilityTypesBy query = new FindFacilityTypesBy(allRequestParams);

		int usedTicketId;

		synchronized (FacilityTypeController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(FacilityTypeFound.class,
				event -> sendFacilityTypesFoundMessage(((FacilityTypeFound) event).getFacilityTypes(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendFacilityTypesFoundMessage(List<FacilityType> facilityTypes, int usedTicketId) {
		queryReturnVal.put(usedTicketId, facilityTypes);
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
	public boolean createFacilityType(HttpServletRequest request) {

		FacilityType facilityTypeToBeAdded = new FacilityType();
		try {
			facilityTypeToBeAdded = FacilityTypeMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createFacilityType(facilityTypeToBeAdded);

	}

	/**
	 * creates a new FacilityType entry in the ofbiz database
	 * 
	 * @param facilityTypeToBeAdded
	 *            the FacilityType thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createFacilityType(FacilityType facilityTypeToBeAdded) {

		AddFacilityType com = new AddFacilityType(facilityTypeToBeAdded);
		int usedTicketId;

		synchronized (FacilityTypeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(FacilityTypeAdded.class,
				event -> sendFacilityTypeChangedMessage(((FacilityTypeAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateFacilityType(HttpServletRequest request) {

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

		FacilityType facilityTypeToBeUpdated = new FacilityType();

		try {
			facilityTypeToBeUpdated = FacilityTypeMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateFacilityType(facilityTypeToBeUpdated);

	}

	/**
	 * Updates the FacilityType with the specific Id
	 * 
	 * @param facilityTypeToBeUpdated the FacilityType thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateFacilityType(FacilityType facilityTypeToBeUpdated) {

		UpdateFacilityType com = new UpdateFacilityType(facilityTypeToBeUpdated);

		int usedTicketId;

		synchronized (FacilityTypeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(FacilityTypeUpdated.class,
				event -> sendFacilityTypeChangedMessage(((FacilityTypeUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a FacilityType from the database
	 * 
	 * @param facilityTypeId:
	 *            the id of the FacilityType thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deletefacilityTypeById(@RequestParam(value = "facilityTypeId") String facilityTypeId) {

		DeleteFacilityType com = new DeleteFacilityType(facilityTypeId);

		int usedTicketId;

		synchronized (FacilityTypeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(FacilityTypeDeleted.class,
				event -> sendFacilityTypeChangedMessage(((FacilityTypeDeleted) event).isSuccess(), usedTicketId));

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

	public void sendFacilityTypeChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/facilityType/\" plus one of the following: "
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
