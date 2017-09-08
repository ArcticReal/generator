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
import com.skytala.eCommerce.command.AddCommunicationEventRole;
import com.skytala.eCommerce.command.DeleteCommunicationEventRole;
import com.skytala.eCommerce.command.UpdateCommunicationEventRole;
import com.skytala.eCommerce.entity.CommunicationEventRole;
import com.skytala.eCommerce.entity.CommunicationEventRoleMapper;
import com.skytala.eCommerce.event.CommunicationEventRoleAdded;
import com.skytala.eCommerce.event.CommunicationEventRoleDeleted;
import com.skytala.eCommerce.event.CommunicationEventRoleFound;
import com.skytala.eCommerce.event.CommunicationEventRoleUpdated;
import com.skytala.eCommerce.query.FindCommunicationEventRolesBy;

@RestController
@RequestMapping("/api/communicationEventRole")
public class CommunicationEventRoleController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<CommunicationEventRole>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public CommunicationEventRoleController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a CommunicationEventRole
	 * @return a List with the CommunicationEventRoles
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<CommunicationEventRole> findCommunicationEventRolesBy(@RequestParam Map<String, String> allRequestParams) {

		FindCommunicationEventRolesBy query = new FindCommunicationEventRolesBy(allRequestParams);

		int usedTicketId;

		synchronized (CommunicationEventRoleController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(CommunicationEventRoleFound.class,
				event -> sendCommunicationEventRolesFoundMessage(((CommunicationEventRoleFound) event).getCommunicationEventRoles(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendCommunicationEventRolesFoundMessage(List<CommunicationEventRole> communicationEventRoles, int usedTicketId) {
		queryReturnVal.put(usedTicketId, communicationEventRoles);
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
	public boolean createCommunicationEventRole(HttpServletRequest request) {

		CommunicationEventRole communicationEventRoleToBeAdded = new CommunicationEventRole();
		try {
			communicationEventRoleToBeAdded = CommunicationEventRoleMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createCommunicationEventRole(communicationEventRoleToBeAdded);

	}

	/**
	 * creates a new CommunicationEventRole entry in the ofbiz database
	 * 
	 * @param communicationEventRoleToBeAdded
	 *            the CommunicationEventRole thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createCommunicationEventRole(CommunicationEventRole communicationEventRoleToBeAdded) {

		AddCommunicationEventRole com = new AddCommunicationEventRole(communicationEventRoleToBeAdded);
		int usedTicketId;

		synchronized (CommunicationEventRoleController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(CommunicationEventRoleAdded.class,
				event -> sendCommunicationEventRoleChangedMessage(((CommunicationEventRoleAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateCommunicationEventRole(HttpServletRequest request) {

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

		CommunicationEventRole communicationEventRoleToBeUpdated = new CommunicationEventRole();

		try {
			communicationEventRoleToBeUpdated = CommunicationEventRoleMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateCommunicationEventRole(communicationEventRoleToBeUpdated);

	}

	/**
	 * Updates the CommunicationEventRole with the specific Id
	 * 
	 * @param communicationEventRoleToBeUpdated the CommunicationEventRole thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateCommunicationEventRole(CommunicationEventRole communicationEventRoleToBeUpdated) {

		UpdateCommunicationEventRole com = new UpdateCommunicationEventRole(communicationEventRoleToBeUpdated);

		int usedTicketId;

		synchronized (CommunicationEventRoleController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(CommunicationEventRoleUpdated.class,
				event -> sendCommunicationEventRoleChangedMessage(((CommunicationEventRoleUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a CommunicationEventRole from the database
	 * 
	 * @param communicationEventRoleId:
	 *            the id of the CommunicationEventRole thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deletecommunicationEventRoleById(@RequestParam(value = "communicationEventRoleId") String communicationEventRoleId) {

		DeleteCommunicationEventRole com = new DeleteCommunicationEventRole(communicationEventRoleId);

		int usedTicketId;

		synchronized (CommunicationEventRoleController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(CommunicationEventRoleDeleted.class,
				event -> sendCommunicationEventRoleChangedMessage(((CommunicationEventRoleDeleted) event).isSuccess(), usedTicketId));

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

	public void sendCommunicationEventRoleChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/communicationEventRole/\" plus one of the following: "
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
