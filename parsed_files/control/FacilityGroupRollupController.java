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
import com.skytala.eCommerce.command.AddFacilityGroupRollup;
import com.skytala.eCommerce.command.DeleteFacilityGroupRollup;
import com.skytala.eCommerce.command.UpdateFacilityGroupRollup;
import com.skytala.eCommerce.entity.FacilityGroupRollup;
import com.skytala.eCommerce.entity.FacilityGroupRollupMapper;
import com.skytala.eCommerce.event.FacilityGroupRollupAdded;
import com.skytala.eCommerce.event.FacilityGroupRollupDeleted;
import com.skytala.eCommerce.event.FacilityGroupRollupFound;
import com.skytala.eCommerce.event.FacilityGroupRollupUpdated;
import com.skytala.eCommerce.query.FindFacilityGroupRollupsBy;

@RestController
@RequestMapping("/api/facilityGroupRollup")
public class FacilityGroupRollupController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<FacilityGroupRollup>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public FacilityGroupRollupController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a FacilityGroupRollup
	 * @return a List with the FacilityGroupRollups
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<FacilityGroupRollup> findFacilityGroupRollupsBy(@RequestParam Map<String, String> allRequestParams) {

		FindFacilityGroupRollupsBy query = new FindFacilityGroupRollupsBy(allRequestParams);

		int usedTicketId;

		synchronized (FacilityGroupRollupController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(FacilityGroupRollupFound.class,
				event -> sendFacilityGroupRollupsFoundMessage(((FacilityGroupRollupFound) event).getFacilityGroupRollups(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendFacilityGroupRollupsFoundMessage(List<FacilityGroupRollup> facilityGroupRollups, int usedTicketId) {
		queryReturnVal.put(usedTicketId, facilityGroupRollups);
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
	public boolean createFacilityGroupRollup(HttpServletRequest request) {

		FacilityGroupRollup facilityGroupRollupToBeAdded = new FacilityGroupRollup();
		try {
			facilityGroupRollupToBeAdded = FacilityGroupRollupMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createFacilityGroupRollup(facilityGroupRollupToBeAdded);

	}

	/**
	 * creates a new FacilityGroupRollup entry in the ofbiz database
	 * 
	 * @param facilityGroupRollupToBeAdded
	 *            the FacilityGroupRollup thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createFacilityGroupRollup(FacilityGroupRollup facilityGroupRollupToBeAdded) {

		AddFacilityGroupRollup com = new AddFacilityGroupRollup(facilityGroupRollupToBeAdded);
		int usedTicketId;

		synchronized (FacilityGroupRollupController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(FacilityGroupRollupAdded.class,
				event -> sendFacilityGroupRollupChangedMessage(((FacilityGroupRollupAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateFacilityGroupRollup(HttpServletRequest request) {

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

		FacilityGroupRollup facilityGroupRollupToBeUpdated = new FacilityGroupRollup();

		try {
			facilityGroupRollupToBeUpdated = FacilityGroupRollupMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateFacilityGroupRollup(facilityGroupRollupToBeUpdated);

	}

	/**
	 * Updates the FacilityGroupRollup with the specific Id
	 * 
	 * @param facilityGroupRollupToBeUpdated the FacilityGroupRollup thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateFacilityGroupRollup(FacilityGroupRollup facilityGroupRollupToBeUpdated) {

		UpdateFacilityGroupRollup com = new UpdateFacilityGroupRollup(facilityGroupRollupToBeUpdated);

		int usedTicketId;

		synchronized (FacilityGroupRollupController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(FacilityGroupRollupUpdated.class,
				event -> sendFacilityGroupRollupChangedMessage(((FacilityGroupRollupUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a FacilityGroupRollup from the database
	 * 
	 * @param facilityGroupRollupId:
	 *            the id of the FacilityGroupRollup thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deletefacilityGroupRollupById(@RequestParam(value = "facilityGroupRollupId") String facilityGroupRollupId) {

		DeleteFacilityGroupRollup com = new DeleteFacilityGroupRollup(facilityGroupRollupId);

		int usedTicketId;

		synchronized (FacilityGroupRollupController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(FacilityGroupRollupDeleted.class,
				event -> sendFacilityGroupRollupChangedMessage(((FacilityGroupRollupDeleted) event).isSuccess(), usedTicketId));

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

	public void sendFacilityGroupRollupChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/facilityGroupRollup/\" plus one of the following: "
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
