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
import com.skytala.eCommerce.command.AddPicklistRole;
import com.skytala.eCommerce.command.DeletePicklistRole;
import com.skytala.eCommerce.command.UpdatePicklistRole;
import com.skytala.eCommerce.entity.PicklistRole;
import com.skytala.eCommerce.entity.PicklistRoleMapper;
import com.skytala.eCommerce.event.PicklistRoleAdded;
import com.skytala.eCommerce.event.PicklistRoleDeleted;
import com.skytala.eCommerce.event.PicklistRoleFound;
import com.skytala.eCommerce.event.PicklistRoleUpdated;
import com.skytala.eCommerce.query.FindPicklistRolesBy;

@RestController
@RequestMapping("/api/picklistRole")
public class PicklistRoleController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<PicklistRole>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public PicklistRoleController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a PicklistRole
	 * @return a List with the PicklistRoles
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<PicklistRole> findPicklistRolesBy(@RequestParam Map<String, String> allRequestParams) {

		FindPicklistRolesBy query = new FindPicklistRolesBy(allRequestParams);

		int usedTicketId;

		synchronized (PicklistRoleController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(PicklistRoleFound.class,
				event -> sendPicklistRolesFoundMessage(((PicklistRoleFound) event).getPicklistRoles(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendPicklistRolesFoundMessage(List<PicklistRole> picklistRoles, int usedTicketId) {
		queryReturnVal.put(usedTicketId, picklistRoles);
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
	public boolean createPicklistRole(HttpServletRequest request) {

		PicklistRole picklistRoleToBeAdded = new PicklistRole();
		try {
			picklistRoleToBeAdded = PicklistRoleMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createPicklistRole(picklistRoleToBeAdded);

	}

	/**
	 * creates a new PicklistRole entry in the ofbiz database
	 * 
	 * @param picklistRoleToBeAdded
	 *            the PicklistRole thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createPicklistRole(PicklistRole picklistRoleToBeAdded) {

		AddPicklistRole com = new AddPicklistRole(picklistRoleToBeAdded);
		int usedTicketId;

		synchronized (PicklistRoleController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(PicklistRoleAdded.class,
				event -> sendPicklistRoleChangedMessage(((PicklistRoleAdded) event).isSuccess(), usedTicketId));

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
	public boolean updatePicklistRole(HttpServletRequest request) {

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

		PicklistRole picklistRoleToBeUpdated = new PicklistRole();

		try {
			picklistRoleToBeUpdated = PicklistRoleMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updatePicklistRole(picklistRoleToBeUpdated);

	}

	/**
	 * Updates the PicklistRole with the specific Id
	 * 
	 * @param picklistRoleToBeUpdated the PicklistRole thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updatePicklistRole(PicklistRole picklistRoleToBeUpdated) {

		UpdatePicklistRole com = new UpdatePicklistRole(picklistRoleToBeUpdated);

		int usedTicketId;

		synchronized (PicklistRoleController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(PicklistRoleUpdated.class,
				event -> sendPicklistRoleChangedMessage(((PicklistRoleUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a PicklistRole from the database
	 * 
	 * @param picklistRoleId:
	 *            the id of the PicklistRole thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deletepicklistRoleById(@RequestParam(value = "picklistRoleId") String picklistRoleId) {

		DeletePicklistRole com = new DeletePicklistRole(picklistRoleId);

		int usedTicketId;

		synchronized (PicklistRoleController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(PicklistRoleDeleted.class,
				event -> sendPicklistRoleChangedMessage(((PicklistRoleDeleted) event).isSuccess(), usedTicketId));

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

	public void sendPicklistRoleChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/picklistRole/\" plus one of the following: "
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
