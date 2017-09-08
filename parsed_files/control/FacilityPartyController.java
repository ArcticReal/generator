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
import com.skytala.eCommerce.command.AddFacilityParty;
import com.skytala.eCommerce.command.DeleteFacilityParty;
import com.skytala.eCommerce.command.UpdateFacilityParty;
import com.skytala.eCommerce.entity.FacilityParty;
import com.skytala.eCommerce.entity.FacilityPartyMapper;
import com.skytala.eCommerce.event.FacilityPartyAdded;
import com.skytala.eCommerce.event.FacilityPartyDeleted;
import com.skytala.eCommerce.event.FacilityPartyFound;
import com.skytala.eCommerce.event.FacilityPartyUpdated;
import com.skytala.eCommerce.query.FindFacilityPartysBy;

@RestController
@RequestMapping("/api/facilityParty")
public class FacilityPartyController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<FacilityParty>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public FacilityPartyController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a FacilityParty
	 * @return a List with the FacilityPartys
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<FacilityParty> findFacilityPartysBy(@RequestParam Map<String, String> allRequestParams) {

		FindFacilityPartysBy query = new FindFacilityPartysBy(allRequestParams);

		int usedTicketId;

		synchronized (FacilityPartyController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(FacilityPartyFound.class,
				event -> sendFacilityPartysFoundMessage(((FacilityPartyFound) event).getFacilityPartys(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendFacilityPartysFoundMessage(List<FacilityParty> facilityPartys, int usedTicketId) {
		queryReturnVal.put(usedTicketId, facilityPartys);
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
	public boolean createFacilityParty(HttpServletRequest request) {

		FacilityParty facilityPartyToBeAdded = new FacilityParty();
		try {
			facilityPartyToBeAdded = FacilityPartyMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createFacilityParty(facilityPartyToBeAdded);

	}

	/**
	 * creates a new FacilityParty entry in the ofbiz database
	 * 
	 * @param facilityPartyToBeAdded
	 *            the FacilityParty thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createFacilityParty(FacilityParty facilityPartyToBeAdded) {

		AddFacilityParty com = new AddFacilityParty(facilityPartyToBeAdded);
		int usedTicketId;

		synchronized (FacilityPartyController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(FacilityPartyAdded.class,
				event -> sendFacilityPartyChangedMessage(((FacilityPartyAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateFacilityParty(HttpServletRequest request) {

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

		FacilityParty facilityPartyToBeUpdated = new FacilityParty();

		try {
			facilityPartyToBeUpdated = FacilityPartyMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateFacilityParty(facilityPartyToBeUpdated);

	}

	/**
	 * Updates the FacilityParty with the specific Id
	 * 
	 * @param facilityPartyToBeUpdated the FacilityParty thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateFacilityParty(FacilityParty facilityPartyToBeUpdated) {

		UpdateFacilityParty com = new UpdateFacilityParty(facilityPartyToBeUpdated);

		int usedTicketId;

		synchronized (FacilityPartyController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(FacilityPartyUpdated.class,
				event -> sendFacilityPartyChangedMessage(((FacilityPartyUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a FacilityParty from the database
	 * 
	 * @param facilityPartyId:
	 *            the id of the FacilityParty thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deletefacilityPartyById(@RequestParam(value = "facilityPartyId") String facilityPartyId) {

		DeleteFacilityParty com = new DeleteFacilityParty(facilityPartyId);

		int usedTicketId;

		synchronized (FacilityPartyController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(FacilityPartyDeleted.class,
				event -> sendFacilityPartyChangedMessage(((FacilityPartyDeleted) event).isSuccess(), usedTicketId));

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

	public void sendFacilityPartyChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/facilityParty/\" plus one of the following: "
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
