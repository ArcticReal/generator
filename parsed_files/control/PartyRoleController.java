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
import com.skytala.eCommerce.command.AddPartyRole;
import com.skytala.eCommerce.command.DeletePartyRole;
import com.skytala.eCommerce.command.UpdatePartyRole;
import com.skytala.eCommerce.entity.PartyRole;
import com.skytala.eCommerce.entity.PartyRoleMapper;
import com.skytala.eCommerce.event.PartyRoleAdded;
import com.skytala.eCommerce.event.PartyRoleDeleted;
import com.skytala.eCommerce.event.PartyRoleFound;
import com.skytala.eCommerce.event.PartyRoleUpdated;
import com.skytala.eCommerce.query.FindPartyRolesBy;

@RestController
@RequestMapping("/api/partyRole")
public class PartyRoleController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<PartyRole>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public PartyRoleController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a PartyRole
	 * @return a List with the PartyRoles
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<PartyRole> findPartyRolesBy(@RequestParam Map<String, String> allRequestParams) {

		FindPartyRolesBy query = new FindPartyRolesBy(allRequestParams);

		int usedTicketId;

		synchronized (PartyRoleController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(PartyRoleFound.class,
				event -> sendPartyRolesFoundMessage(((PartyRoleFound) event).getPartyRoles(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendPartyRolesFoundMessage(List<PartyRole> partyRoles, int usedTicketId) {
		queryReturnVal.put(usedTicketId, partyRoles);
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
	public boolean createPartyRole(HttpServletRequest request) {

		PartyRole partyRoleToBeAdded = new PartyRole();
		try {
			partyRoleToBeAdded = PartyRoleMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createPartyRole(partyRoleToBeAdded);

	}

	/**
	 * creates a new PartyRole entry in the ofbiz database
	 * 
	 * @param partyRoleToBeAdded
	 *            the PartyRole thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createPartyRole(PartyRole partyRoleToBeAdded) {

		AddPartyRole com = new AddPartyRole(partyRoleToBeAdded);
		int usedTicketId;

		synchronized (PartyRoleController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(PartyRoleAdded.class,
				event -> sendPartyRoleChangedMessage(((PartyRoleAdded) event).isSuccess(), usedTicketId));

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
	public boolean updatePartyRole(HttpServletRequest request) {

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

		PartyRole partyRoleToBeUpdated = new PartyRole();

		try {
			partyRoleToBeUpdated = PartyRoleMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updatePartyRole(partyRoleToBeUpdated);

	}

	/**
	 * Updates the PartyRole with the specific Id
	 * 
	 * @param partyRoleToBeUpdated the PartyRole thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updatePartyRole(PartyRole partyRoleToBeUpdated) {

		UpdatePartyRole com = new UpdatePartyRole(partyRoleToBeUpdated);

		int usedTicketId;

		synchronized (PartyRoleController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(PartyRoleUpdated.class,
				event -> sendPartyRoleChangedMessage(((PartyRoleUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a PartyRole from the database
	 * 
	 * @param partyRoleId:
	 *            the id of the PartyRole thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deletepartyRoleById(@RequestParam(value = "partyRoleId") String partyRoleId) {

		DeletePartyRole com = new DeletePartyRole(partyRoleId);

		int usedTicketId;

		synchronized (PartyRoleController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(PartyRoleDeleted.class,
				event -> sendPartyRoleChangedMessage(((PartyRoleDeleted) event).isSuccess(), usedTicketId));

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

	public void sendPartyRoleChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/partyRole/\" plus one of the following: "
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
