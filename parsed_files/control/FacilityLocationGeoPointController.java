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
import com.skytala.eCommerce.command.AddFacilityLocationGeoPoint;
import com.skytala.eCommerce.command.DeleteFacilityLocationGeoPoint;
import com.skytala.eCommerce.command.UpdateFacilityLocationGeoPoint;
import com.skytala.eCommerce.entity.FacilityLocationGeoPoint;
import com.skytala.eCommerce.entity.FacilityLocationGeoPointMapper;
import com.skytala.eCommerce.event.FacilityLocationGeoPointAdded;
import com.skytala.eCommerce.event.FacilityLocationGeoPointDeleted;
import com.skytala.eCommerce.event.FacilityLocationGeoPointFound;
import com.skytala.eCommerce.event.FacilityLocationGeoPointUpdated;
import com.skytala.eCommerce.query.FindFacilityLocationGeoPointsBy;

@RestController
@RequestMapping("/api/facilityLocationGeoPoint")
public class FacilityLocationGeoPointController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<FacilityLocationGeoPoint>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public FacilityLocationGeoPointController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a FacilityLocationGeoPoint
	 * @return a List with the FacilityLocationGeoPoints
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<FacilityLocationGeoPoint> findFacilityLocationGeoPointsBy(@RequestParam Map<String, String> allRequestParams) {

		FindFacilityLocationGeoPointsBy query = new FindFacilityLocationGeoPointsBy(allRequestParams);

		int usedTicketId;

		synchronized (FacilityLocationGeoPointController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(FacilityLocationGeoPointFound.class,
				event -> sendFacilityLocationGeoPointsFoundMessage(((FacilityLocationGeoPointFound) event).getFacilityLocationGeoPoints(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendFacilityLocationGeoPointsFoundMessage(List<FacilityLocationGeoPoint> facilityLocationGeoPoints, int usedTicketId) {
		queryReturnVal.put(usedTicketId, facilityLocationGeoPoints);
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
	public boolean createFacilityLocationGeoPoint(HttpServletRequest request) {

		FacilityLocationGeoPoint facilityLocationGeoPointToBeAdded = new FacilityLocationGeoPoint();
		try {
			facilityLocationGeoPointToBeAdded = FacilityLocationGeoPointMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createFacilityLocationGeoPoint(facilityLocationGeoPointToBeAdded);

	}

	/**
	 * creates a new FacilityLocationGeoPoint entry in the ofbiz database
	 * 
	 * @param facilityLocationGeoPointToBeAdded
	 *            the FacilityLocationGeoPoint thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createFacilityLocationGeoPoint(FacilityLocationGeoPoint facilityLocationGeoPointToBeAdded) {

		AddFacilityLocationGeoPoint com = new AddFacilityLocationGeoPoint(facilityLocationGeoPointToBeAdded);
		int usedTicketId;

		synchronized (FacilityLocationGeoPointController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(FacilityLocationGeoPointAdded.class,
				event -> sendFacilityLocationGeoPointChangedMessage(((FacilityLocationGeoPointAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateFacilityLocationGeoPoint(HttpServletRequest request) {

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

		FacilityLocationGeoPoint facilityLocationGeoPointToBeUpdated = new FacilityLocationGeoPoint();

		try {
			facilityLocationGeoPointToBeUpdated = FacilityLocationGeoPointMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateFacilityLocationGeoPoint(facilityLocationGeoPointToBeUpdated);

	}

	/**
	 * Updates the FacilityLocationGeoPoint with the specific Id
	 * 
	 * @param facilityLocationGeoPointToBeUpdated the FacilityLocationGeoPoint thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateFacilityLocationGeoPoint(FacilityLocationGeoPoint facilityLocationGeoPointToBeUpdated) {

		UpdateFacilityLocationGeoPoint com = new UpdateFacilityLocationGeoPoint(facilityLocationGeoPointToBeUpdated);

		int usedTicketId;

		synchronized (FacilityLocationGeoPointController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(FacilityLocationGeoPointUpdated.class,
				event -> sendFacilityLocationGeoPointChangedMessage(((FacilityLocationGeoPointUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a FacilityLocationGeoPoint from the database
	 * 
	 * @param facilityLocationGeoPointId:
	 *            the id of the FacilityLocationGeoPoint thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deletefacilityLocationGeoPointById(@RequestParam(value = "facilityLocationGeoPointId") String facilityLocationGeoPointId) {

		DeleteFacilityLocationGeoPoint com = new DeleteFacilityLocationGeoPoint(facilityLocationGeoPointId);

		int usedTicketId;

		synchronized (FacilityLocationGeoPointController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(FacilityLocationGeoPointDeleted.class,
				event -> sendFacilityLocationGeoPointChangedMessage(((FacilityLocationGeoPointDeleted) event).isSuccess(), usedTicketId));

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

	public void sendFacilityLocationGeoPointChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/facilityLocationGeoPoint/\" plus one of the following: "
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
