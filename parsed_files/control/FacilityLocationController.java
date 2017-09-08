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
import com.skytala.eCommerce.command.AddFacilityLocation;
import com.skytala.eCommerce.command.DeleteFacilityLocation;
import com.skytala.eCommerce.command.UpdateFacilityLocation;
import com.skytala.eCommerce.entity.FacilityLocation;
import com.skytala.eCommerce.entity.FacilityLocationMapper;
import com.skytala.eCommerce.event.FacilityLocationAdded;
import com.skytala.eCommerce.event.FacilityLocationDeleted;
import com.skytala.eCommerce.event.FacilityLocationFound;
import com.skytala.eCommerce.event.FacilityLocationUpdated;
import com.skytala.eCommerce.query.FindFacilityLocationsBy;

@RestController
@RequestMapping("/api/facilityLocation")
public class FacilityLocationController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<FacilityLocation>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public FacilityLocationController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a FacilityLocation
	 * @return a List with the FacilityLocations
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<FacilityLocation> findFacilityLocationsBy(@RequestParam Map<String, String> allRequestParams) {

		FindFacilityLocationsBy query = new FindFacilityLocationsBy(allRequestParams);

		int usedTicketId;

		synchronized (FacilityLocationController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(FacilityLocationFound.class,
				event -> sendFacilityLocationsFoundMessage(((FacilityLocationFound) event).getFacilityLocations(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendFacilityLocationsFoundMessage(List<FacilityLocation> facilityLocations, int usedTicketId) {
		queryReturnVal.put(usedTicketId, facilityLocations);
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
	public boolean createFacilityLocation(HttpServletRequest request) {

		FacilityLocation facilityLocationToBeAdded = new FacilityLocation();
		try {
			facilityLocationToBeAdded = FacilityLocationMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createFacilityLocation(facilityLocationToBeAdded);

	}

	/**
	 * creates a new FacilityLocation entry in the ofbiz database
	 * 
	 * @param facilityLocationToBeAdded
	 *            the FacilityLocation thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createFacilityLocation(FacilityLocation facilityLocationToBeAdded) {

		AddFacilityLocation com = new AddFacilityLocation(facilityLocationToBeAdded);
		int usedTicketId;

		synchronized (FacilityLocationController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(FacilityLocationAdded.class,
				event -> sendFacilityLocationChangedMessage(((FacilityLocationAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateFacilityLocation(HttpServletRequest request) {

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

		FacilityLocation facilityLocationToBeUpdated = new FacilityLocation();

		try {
			facilityLocationToBeUpdated = FacilityLocationMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateFacilityLocation(facilityLocationToBeUpdated);

	}

	/**
	 * Updates the FacilityLocation with the specific Id
	 * 
	 * @param facilityLocationToBeUpdated the FacilityLocation thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateFacilityLocation(FacilityLocation facilityLocationToBeUpdated) {

		UpdateFacilityLocation com = new UpdateFacilityLocation(facilityLocationToBeUpdated);

		int usedTicketId;

		synchronized (FacilityLocationController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(FacilityLocationUpdated.class,
				event -> sendFacilityLocationChangedMessage(((FacilityLocationUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a FacilityLocation from the database
	 * 
	 * @param facilityLocationId:
	 *            the id of the FacilityLocation thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deletefacilityLocationById(@RequestParam(value = "facilityLocationId") String facilityLocationId) {

		DeleteFacilityLocation com = new DeleteFacilityLocation(facilityLocationId);

		int usedTicketId;

		synchronized (FacilityLocationController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(FacilityLocationDeleted.class,
				event -> sendFacilityLocationChangedMessage(((FacilityLocationDeleted) event).isSuccess(), usedTicketId));

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

	public void sendFacilityLocationChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/facilityLocation/\" plus one of the following: "
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
