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
import com.skytala.eCommerce.command.AddFacilityGroupRole;
import com.skytala.eCommerce.command.DeleteFacilityGroupRole;
import com.skytala.eCommerce.command.UpdateFacilityGroupRole;
import com.skytala.eCommerce.entity.FacilityGroupRole;
import com.skytala.eCommerce.entity.FacilityGroupRoleMapper;
import com.skytala.eCommerce.event.FacilityGroupRoleAdded;
import com.skytala.eCommerce.event.FacilityGroupRoleDeleted;
import com.skytala.eCommerce.event.FacilityGroupRoleFound;
import com.skytala.eCommerce.event.FacilityGroupRoleUpdated;
import com.skytala.eCommerce.query.FindFacilityGroupRolesBy;

@RestController
@RequestMapping("/api/facilityGroupRole")
public class FacilityGroupRoleController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<FacilityGroupRole>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public FacilityGroupRoleController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a FacilityGroupRole
	 * @return a List with the FacilityGroupRoles
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<FacilityGroupRole> findFacilityGroupRolesBy(@RequestParam Map<String, String> allRequestParams) {

		FindFacilityGroupRolesBy query = new FindFacilityGroupRolesBy(allRequestParams);

		int usedTicketId;

		synchronized (FacilityGroupRoleController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(FacilityGroupRoleFound.class,
				event -> sendFacilityGroupRolesFoundMessage(((FacilityGroupRoleFound) event).getFacilityGroupRoles(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendFacilityGroupRolesFoundMessage(List<FacilityGroupRole> facilityGroupRoles, int usedTicketId) {
		queryReturnVal.put(usedTicketId, facilityGroupRoles);
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
	public boolean createFacilityGroupRole(HttpServletRequest request) {

		FacilityGroupRole facilityGroupRoleToBeAdded = new FacilityGroupRole();
		try {
			facilityGroupRoleToBeAdded = FacilityGroupRoleMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createFacilityGroupRole(facilityGroupRoleToBeAdded);

	}

	/**
	 * creates a new FacilityGroupRole entry in the ofbiz database
	 * 
	 * @param facilityGroupRoleToBeAdded
	 *            the FacilityGroupRole thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createFacilityGroupRole(FacilityGroupRole facilityGroupRoleToBeAdded) {

		AddFacilityGroupRole com = new AddFacilityGroupRole(facilityGroupRoleToBeAdded);
		int usedTicketId;

		synchronized (FacilityGroupRoleController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(FacilityGroupRoleAdded.class,
				event -> sendFacilityGroupRoleChangedMessage(((FacilityGroupRoleAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateFacilityGroupRole(HttpServletRequest request) {

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

		FacilityGroupRole facilityGroupRoleToBeUpdated = new FacilityGroupRole();

		try {
			facilityGroupRoleToBeUpdated = FacilityGroupRoleMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateFacilityGroupRole(facilityGroupRoleToBeUpdated);

	}

	/**
	 * Updates the FacilityGroupRole with the specific Id
	 * 
	 * @param facilityGroupRoleToBeUpdated the FacilityGroupRole thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateFacilityGroupRole(FacilityGroupRole facilityGroupRoleToBeUpdated) {

		UpdateFacilityGroupRole com = new UpdateFacilityGroupRole(facilityGroupRoleToBeUpdated);

		int usedTicketId;

		synchronized (FacilityGroupRoleController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(FacilityGroupRoleUpdated.class,
				event -> sendFacilityGroupRoleChangedMessage(((FacilityGroupRoleUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a FacilityGroupRole from the database
	 * 
	 * @param facilityGroupRoleId:
	 *            the id of the FacilityGroupRole thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deletefacilityGroupRoleById(@RequestParam(value = "facilityGroupRoleId") String facilityGroupRoleId) {

		DeleteFacilityGroupRole com = new DeleteFacilityGroupRole(facilityGroupRoleId);

		int usedTicketId;

		synchronized (FacilityGroupRoleController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(FacilityGroupRoleDeleted.class,
				event -> sendFacilityGroupRoleChangedMessage(((FacilityGroupRoleDeleted) event).isSuccess(), usedTicketId));

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

	public void sendFacilityGroupRoleChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/facilityGroupRole/\" plus one of the following: "
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
