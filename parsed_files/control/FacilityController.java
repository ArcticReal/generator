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
import com.skytala.eCommerce.command.AddFacility;
import com.skytala.eCommerce.command.DeleteFacility;
import com.skytala.eCommerce.command.UpdateFacility;
import com.skytala.eCommerce.entity.Facility;
import com.skytala.eCommerce.entity.FacilityMapper;
import com.skytala.eCommerce.event.FacilityAdded;
import com.skytala.eCommerce.event.FacilityDeleted;
import com.skytala.eCommerce.event.FacilityFound;
import com.skytala.eCommerce.event.FacilityUpdated;
import com.skytala.eCommerce.query.FindFacilitysBy;

@RestController
@RequestMapping("/api/facility")
public class FacilityController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<Facility>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public FacilityController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a Facility
	 * @return a List with the Facilitys
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<Facility> findFacilitysBy(@RequestParam Map<String, String> allRequestParams) {

		FindFacilitysBy query = new FindFacilitysBy(allRequestParams);

		int usedTicketId;

		synchronized (FacilityController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(FacilityFound.class,
				event -> sendFacilitysFoundMessage(((FacilityFound) event).getFacilitys(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendFacilitysFoundMessage(List<Facility> facilitys, int usedTicketId) {
		queryReturnVal.put(usedTicketId, facilitys);
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
	public boolean createFacility(HttpServletRequest request) {

		Facility facilityToBeAdded = new Facility();
		try {
			facilityToBeAdded = FacilityMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createFacility(facilityToBeAdded);

	}

	/**
	 * creates a new Facility entry in the ofbiz database
	 * 
	 * @param facilityToBeAdded
	 *            the Facility thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createFacility(Facility facilityToBeAdded) {

		AddFacility com = new AddFacility(facilityToBeAdded);
		int usedTicketId;

		synchronized (FacilityController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(FacilityAdded.class,
				event -> sendFacilityChangedMessage(((FacilityAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateFacility(HttpServletRequest request) {

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

		Facility facilityToBeUpdated = new Facility();

		try {
			facilityToBeUpdated = FacilityMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateFacility(facilityToBeUpdated);

	}

	/**
	 * Updates the Facility with the specific Id
	 * 
	 * @param facilityToBeUpdated the Facility thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateFacility(Facility facilityToBeUpdated) {

		UpdateFacility com = new UpdateFacility(facilityToBeUpdated);

		int usedTicketId;

		synchronized (FacilityController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(FacilityUpdated.class,
				event -> sendFacilityChangedMessage(((FacilityUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a Facility from the database
	 * 
	 * @param facilityId:
	 *            the id of the Facility thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deletefacilityById(@RequestParam(value = "facilityId") String facilityId) {

		DeleteFacility com = new DeleteFacility(facilityId);

		int usedTicketId;

		synchronized (FacilityController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(FacilityDeleted.class,
				event -> sendFacilityChangedMessage(((FacilityDeleted) event).isSuccess(), usedTicketId));

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

	public void sendFacilityChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/facility/\" plus one of the following: "
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
