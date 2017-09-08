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
import com.skytala.eCommerce.command.AddFacilityContactMech;
import com.skytala.eCommerce.command.DeleteFacilityContactMech;
import com.skytala.eCommerce.command.UpdateFacilityContactMech;
import com.skytala.eCommerce.entity.FacilityContactMech;
import com.skytala.eCommerce.entity.FacilityContactMechMapper;
import com.skytala.eCommerce.event.FacilityContactMechAdded;
import com.skytala.eCommerce.event.FacilityContactMechDeleted;
import com.skytala.eCommerce.event.FacilityContactMechFound;
import com.skytala.eCommerce.event.FacilityContactMechUpdated;
import com.skytala.eCommerce.query.FindFacilityContactMechsBy;

@RestController
@RequestMapping("/api/facilityContactMech")
public class FacilityContactMechController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<FacilityContactMech>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public FacilityContactMechController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a FacilityContactMech
	 * @return a List with the FacilityContactMechs
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<FacilityContactMech> findFacilityContactMechsBy(@RequestParam Map<String, String> allRequestParams) {

		FindFacilityContactMechsBy query = new FindFacilityContactMechsBy(allRequestParams);

		int usedTicketId;

		synchronized (FacilityContactMechController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(FacilityContactMechFound.class,
				event -> sendFacilityContactMechsFoundMessage(((FacilityContactMechFound) event).getFacilityContactMechs(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendFacilityContactMechsFoundMessage(List<FacilityContactMech> facilityContactMechs, int usedTicketId) {
		queryReturnVal.put(usedTicketId, facilityContactMechs);
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
	public boolean createFacilityContactMech(HttpServletRequest request) {

		FacilityContactMech facilityContactMechToBeAdded = new FacilityContactMech();
		try {
			facilityContactMechToBeAdded = FacilityContactMechMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createFacilityContactMech(facilityContactMechToBeAdded);

	}

	/**
	 * creates a new FacilityContactMech entry in the ofbiz database
	 * 
	 * @param facilityContactMechToBeAdded
	 *            the FacilityContactMech thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createFacilityContactMech(FacilityContactMech facilityContactMechToBeAdded) {

		AddFacilityContactMech com = new AddFacilityContactMech(facilityContactMechToBeAdded);
		int usedTicketId;

		synchronized (FacilityContactMechController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(FacilityContactMechAdded.class,
				event -> sendFacilityContactMechChangedMessage(((FacilityContactMechAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateFacilityContactMech(HttpServletRequest request) {

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

		FacilityContactMech facilityContactMechToBeUpdated = new FacilityContactMech();

		try {
			facilityContactMechToBeUpdated = FacilityContactMechMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateFacilityContactMech(facilityContactMechToBeUpdated);

	}

	/**
	 * Updates the FacilityContactMech with the specific Id
	 * 
	 * @param facilityContactMechToBeUpdated the FacilityContactMech thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateFacilityContactMech(FacilityContactMech facilityContactMechToBeUpdated) {

		UpdateFacilityContactMech com = new UpdateFacilityContactMech(facilityContactMechToBeUpdated);

		int usedTicketId;

		synchronized (FacilityContactMechController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(FacilityContactMechUpdated.class,
				event -> sendFacilityContactMechChangedMessage(((FacilityContactMechUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a FacilityContactMech from the database
	 * 
	 * @param facilityContactMechId:
	 *            the id of the FacilityContactMech thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deletefacilityContactMechById(@RequestParam(value = "facilityContactMechId") String facilityContactMechId) {

		DeleteFacilityContactMech com = new DeleteFacilityContactMech(facilityContactMechId);

		int usedTicketId;

		synchronized (FacilityContactMechController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(FacilityContactMechDeleted.class,
				event -> sendFacilityContactMechChangedMessage(((FacilityContactMechDeleted) event).isSuccess(), usedTicketId));

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

	public void sendFacilityContactMechChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/facilityContactMech/\" plus one of the following: "
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
