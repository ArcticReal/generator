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
import com.skytala.eCommerce.command.AddPartyGroup;
import com.skytala.eCommerce.command.DeletePartyGroup;
import com.skytala.eCommerce.command.UpdatePartyGroup;
import com.skytala.eCommerce.entity.PartyGroup;
import com.skytala.eCommerce.entity.PartyGroupMapper;
import com.skytala.eCommerce.event.PartyGroupAdded;
import com.skytala.eCommerce.event.PartyGroupDeleted;
import com.skytala.eCommerce.event.PartyGroupFound;
import com.skytala.eCommerce.event.PartyGroupUpdated;
import com.skytala.eCommerce.query.FindPartyGroupsBy;

@RestController
@RequestMapping("/api/partyGroup")
public class PartyGroupController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<PartyGroup>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public PartyGroupController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a PartyGroup
	 * @return a List with the PartyGroups
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<PartyGroup> findPartyGroupsBy(@RequestParam Map<String, String> allRequestParams) {

		FindPartyGroupsBy query = new FindPartyGroupsBy(allRequestParams);

		int usedTicketId;

		synchronized (PartyGroupController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(PartyGroupFound.class,
				event -> sendPartyGroupsFoundMessage(((PartyGroupFound) event).getPartyGroups(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendPartyGroupsFoundMessage(List<PartyGroup> partyGroups, int usedTicketId) {
		queryReturnVal.put(usedTicketId, partyGroups);
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
	public boolean createPartyGroup(HttpServletRequest request) {

		PartyGroup partyGroupToBeAdded = new PartyGroup();
		try {
			partyGroupToBeAdded = PartyGroupMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createPartyGroup(partyGroupToBeAdded);

	}

	/**
	 * creates a new PartyGroup entry in the ofbiz database
	 * 
	 * @param partyGroupToBeAdded
	 *            the PartyGroup thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createPartyGroup(PartyGroup partyGroupToBeAdded) {

		AddPartyGroup com = new AddPartyGroup(partyGroupToBeAdded);
		int usedTicketId;

		synchronized (PartyGroupController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(PartyGroupAdded.class,
				event -> sendPartyGroupChangedMessage(((PartyGroupAdded) event).isSuccess(), usedTicketId));

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
	public boolean updatePartyGroup(HttpServletRequest request) {

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

		PartyGroup partyGroupToBeUpdated = new PartyGroup();

		try {
			partyGroupToBeUpdated = PartyGroupMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updatePartyGroup(partyGroupToBeUpdated);

	}

	/**
	 * Updates the PartyGroup with the specific Id
	 * 
	 * @param partyGroupToBeUpdated the PartyGroup thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updatePartyGroup(PartyGroup partyGroupToBeUpdated) {

		UpdatePartyGroup com = new UpdatePartyGroup(partyGroupToBeUpdated);

		int usedTicketId;

		synchronized (PartyGroupController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(PartyGroupUpdated.class,
				event -> sendPartyGroupChangedMessage(((PartyGroupUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a PartyGroup from the database
	 * 
	 * @param partyGroupId:
	 *            the id of the PartyGroup thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deletepartyGroupById(@RequestParam(value = "partyGroupId") String partyGroupId) {

		DeletePartyGroup com = new DeletePartyGroup(partyGroupId);

		int usedTicketId;

		synchronized (PartyGroupController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(PartyGroupDeleted.class,
				event -> sendPartyGroupChangedMessage(((PartyGroupDeleted) event).isSuccess(), usedTicketId));

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

	public void sendPartyGroupChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/partyGroup/\" plus one of the following: "
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
