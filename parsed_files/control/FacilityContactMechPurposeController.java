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
import com.skytala.eCommerce.command.AddFacilityContactMechPurpose;
import com.skytala.eCommerce.command.DeleteFacilityContactMechPurpose;
import com.skytala.eCommerce.command.UpdateFacilityContactMechPurpose;
import com.skytala.eCommerce.entity.FacilityContactMechPurpose;
import com.skytala.eCommerce.entity.FacilityContactMechPurposeMapper;
import com.skytala.eCommerce.event.FacilityContactMechPurposeAdded;
import com.skytala.eCommerce.event.FacilityContactMechPurposeDeleted;
import com.skytala.eCommerce.event.FacilityContactMechPurposeFound;
import com.skytala.eCommerce.event.FacilityContactMechPurposeUpdated;
import com.skytala.eCommerce.query.FindFacilityContactMechPurposesBy;

@RestController
@RequestMapping("/api/facilityContactMechPurpose")
public class FacilityContactMechPurposeController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<FacilityContactMechPurpose>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public FacilityContactMechPurposeController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a FacilityContactMechPurpose
	 * @return a List with the FacilityContactMechPurposes
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<FacilityContactMechPurpose> findFacilityContactMechPurposesBy(@RequestParam Map<String, String> allRequestParams) {

		FindFacilityContactMechPurposesBy query = new FindFacilityContactMechPurposesBy(allRequestParams);

		int usedTicketId;

		synchronized (FacilityContactMechPurposeController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(FacilityContactMechPurposeFound.class,
				event -> sendFacilityContactMechPurposesFoundMessage(((FacilityContactMechPurposeFound) event).getFacilityContactMechPurposes(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendFacilityContactMechPurposesFoundMessage(List<FacilityContactMechPurpose> facilityContactMechPurposes, int usedTicketId) {
		queryReturnVal.put(usedTicketId, facilityContactMechPurposes);
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
	public boolean createFacilityContactMechPurpose(HttpServletRequest request) {

		FacilityContactMechPurpose facilityContactMechPurposeToBeAdded = new FacilityContactMechPurpose();
		try {
			facilityContactMechPurposeToBeAdded = FacilityContactMechPurposeMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createFacilityContactMechPurpose(facilityContactMechPurposeToBeAdded);

	}

	/**
	 * creates a new FacilityContactMechPurpose entry in the ofbiz database
	 * 
	 * @param facilityContactMechPurposeToBeAdded
	 *            the FacilityContactMechPurpose thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createFacilityContactMechPurpose(FacilityContactMechPurpose facilityContactMechPurposeToBeAdded) {

		AddFacilityContactMechPurpose com = new AddFacilityContactMechPurpose(facilityContactMechPurposeToBeAdded);
		int usedTicketId;

		synchronized (FacilityContactMechPurposeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(FacilityContactMechPurposeAdded.class,
				event -> sendFacilityContactMechPurposeChangedMessage(((FacilityContactMechPurposeAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateFacilityContactMechPurpose(HttpServletRequest request) {

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

		FacilityContactMechPurpose facilityContactMechPurposeToBeUpdated = new FacilityContactMechPurpose();

		try {
			facilityContactMechPurposeToBeUpdated = FacilityContactMechPurposeMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateFacilityContactMechPurpose(facilityContactMechPurposeToBeUpdated);

	}

	/**
	 * Updates the FacilityContactMechPurpose with the specific Id
	 * 
	 * @param facilityContactMechPurposeToBeUpdated the FacilityContactMechPurpose thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateFacilityContactMechPurpose(FacilityContactMechPurpose facilityContactMechPurposeToBeUpdated) {

		UpdateFacilityContactMechPurpose com = new UpdateFacilityContactMechPurpose(facilityContactMechPurposeToBeUpdated);

		int usedTicketId;

		synchronized (FacilityContactMechPurposeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(FacilityContactMechPurposeUpdated.class,
				event -> sendFacilityContactMechPurposeChangedMessage(((FacilityContactMechPurposeUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a FacilityContactMechPurpose from the database
	 * 
	 * @param facilityContactMechPurposeId:
	 *            the id of the FacilityContactMechPurpose thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deletefacilityContactMechPurposeById(@RequestParam(value = "facilityContactMechPurposeId") String facilityContactMechPurposeId) {

		DeleteFacilityContactMechPurpose com = new DeleteFacilityContactMechPurpose(facilityContactMechPurposeId);

		int usedTicketId;

		synchronized (FacilityContactMechPurposeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(FacilityContactMechPurposeDeleted.class,
				event -> sendFacilityContactMechPurposeChangedMessage(((FacilityContactMechPurposeDeleted) event).isSuccess(), usedTicketId));

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

	public void sendFacilityContactMechPurposeChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/facilityContactMechPurpose/\" plus one of the following: "
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
