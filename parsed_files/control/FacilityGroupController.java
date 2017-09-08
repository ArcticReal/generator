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
import com.skytala.eCommerce.command.AddFacilityGroup;
import com.skytala.eCommerce.command.DeleteFacilityGroup;
import com.skytala.eCommerce.command.UpdateFacilityGroup;
import com.skytala.eCommerce.entity.FacilityGroup;
import com.skytala.eCommerce.entity.FacilityGroupMapper;
import com.skytala.eCommerce.event.FacilityGroupAdded;
import com.skytala.eCommerce.event.FacilityGroupDeleted;
import com.skytala.eCommerce.event.FacilityGroupFound;
import com.skytala.eCommerce.event.FacilityGroupUpdated;
import com.skytala.eCommerce.query.FindFacilityGroupsBy;

@RestController
@RequestMapping("/api/facilityGroup")
public class FacilityGroupController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<FacilityGroup>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public FacilityGroupController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a FacilityGroup
	 * @return a List with the FacilityGroups
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<FacilityGroup> findFacilityGroupsBy(@RequestParam Map<String, String> allRequestParams) {

		FindFacilityGroupsBy query = new FindFacilityGroupsBy(allRequestParams);

		int usedTicketId;

		synchronized (FacilityGroupController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(FacilityGroupFound.class,
				event -> sendFacilityGroupsFoundMessage(((FacilityGroupFound) event).getFacilityGroups(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendFacilityGroupsFoundMessage(List<FacilityGroup> facilityGroups, int usedTicketId) {
		queryReturnVal.put(usedTicketId, facilityGroups);
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
	public boolean createFacilityGroup(HttpServletRequest request) {

		FacilityGroup facilityGroupToBeAdded = new FacilityGroup();
		try {
			facilityGroupToBeAdded = FacilityGroupMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createFacilityGroup(facilityGroupToBeAdded);

	}

	/**
	 * creates a new FacilityGroup entry in the ofbiz database
	 * 
	 * @param facilityGroupToBeAdded
	 *            the FacilityGroup thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createFacilityGroup(FacilityGroup facilityGroupToBeAdded) {

		AddFacilityGroup com = new AddFacilityGroup(facilityGroupToBeAdded);
		int usedTicketId;

		synchronized (FacilityGroupController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(FacilityGroupAdded.class,
				event -> sendFacilityGroupChangedMessage(((FacilityGroupAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateFacilityGroup(HttpServletRequest request) {

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

		FacilityGroup facilityGroupToBeUpdated = new FacilityGroup();

		try {
			facilityGroupToBeUpdated = FacilityGroupMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateFacilityGroup(facilityGroupToBeUpdated);

	}

	/**
	 * Updates the FacilityGroup with the specific Id
	 * 
	 * @param facilityGroupToBeUpdated the FacilityGroup thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateFacilityGroup(FacilityGroup facilityGroupToBeUpdated) {

		UpdateFacilityGroup com = new UpdateFacilityGroup(facilityGroupToBeUpdated);

		int usedTicketId;

		synchronized (FacilityGroupController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(FacilityGroupUpdated.class,
				event -> sendFacilityGroupChangedMessage(((FacilityGroupUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a FacilityGroup from the database
	 * 
	 * @param facilityGroupId:
	 *            the id of the FacilityGroup thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deletefacilityGroupById(@RequestParam(value = "facilityGroupId") String facilityGroupId) {

		DeleteFacilityGroup com = new DeleteFacilityGroup(facilityGroupId);

		int usedTicketId;

		synchronized (FacilityGroupController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(FacilityGroupDeleted.class,
				event -> sendFacilityGroupChangedMessage(((FacilityGroupDeleted) event).isSuccess(), usedTicketId));

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

	public void sendFacilityGroupChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/facilityGroup/\" plus one of the following: "
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
