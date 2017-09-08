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
import com.skytala.eCommerce.command.AddPartyClassificationGroup;
import com.skytala.eCommerce.command.DeletePartyClassificationGroup;
import com.skytala.eCommerce.command.UpdatePartyClassificationGroup;
import com.skytala.eCommerce.entity.PartyClassificationGroup;
import com.skytala.eCommerce.entity.PartyClassificationGroupMapper;
import com.skytala.eCommerce.event.PartyClassificationGroupAdded;
import com.skytala.eCommerce.event.PartyClassificationGroupDeleted;
import com.skytala.eCommerce.event.PartyClassificationGroupFound;
import com.skytala.eCommerce.event.PartyClassificationGroupUpdated;
import com.skytala.eCommerce.query.FindPartyClassificationGroupsBy;

@RestController
@RequestMapping("/api/partyClassificationGroup")
public class PartyClassificationGroupController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<PartyClassificationGroup>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public PartyClassificationGroupController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a PartyClassificationGroup
	 * @return a List with the PartyClassificationGroups
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<PartyClassificationGroup> findPartyClassificationGroupsBy(@RequestParam Map<String, String> allRequestParams) {

		FindPartyClassificationGroupsBy query = new FindPartyClassificationGroupsBy(allRequestParams);

		int usedTicketId;

		synchronized (PartyClassificationGroupController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(PartyClassificationGroupFound.class,
				event -> sendPartyClassificationGroupsFoundMessage(((PartyClassificationGroupFound) event).getPartyClassificationGroups(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendPartyClassificationGroupsFoundMessage(List<PartyClassificationGroup> partyClassificationGroups, int usedTicketId) {
		queryReturnVal.put(usedTicketId, partyClassificationGroups);
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
	public boolean createPartyClassificationGroup(HttpServletRequest request) {

		PartyClassificationGroup partyClassificationGroupToBeAdded = new PartyClassificationGroup();
		try {
			partyClassificationGroupToBeAdded = PartyClassificationGroupMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createPartyClassificationGroup(partyClassificationGroupToBeAdded);

	}

	/**
	 * creates a new PartyClassificationGroup entry in the ofbiz database
	 * 
	 * @param partyClassificationGroupToBeAdded
	 *            the PartyClassificationGroup thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createPartyClassificationGroup(PartyClassificationGroup partyClassificationGroupToBeAdded) {

		AddPartyClassificationGroup com = new AddPartyClassificationGroup(partyClassificationGroupToBeAdded);
		int usedTicketId;

		synchronized (PartyClassificationGroupController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(PartyClassificationGroupAdded.class,
				event -> sendPartyClassificationGroupChangedMessage(((PartyClassificationGroupAdded) event).isSuccess(), usedTicketId));

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
	public boolean updatePartyClassificationGroup(HttpServletRequest request) {

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

		PartyClassificationGroup partyClassificationGroupToBeUpdated = new PartyClassificationGroup();

		try {
			partyClassificationGroupToBeUpdated = PartyClassificationGroupMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updatePartyClassificationGroup(partyClassificationGroupToBeUpdated);

	}

	/**
	 * Updates the PartyClassificationGroup with the specific Id
	 * 
	 * @param partyClassificationGroupToBeUpdated the PartyClassificationGroup thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updatePartyClassificationGroup(PartyClassificationGroup partyClassificationGroupToBeUpdated) {

		UpdatePartyClassificationGroup com = new UpdatePartyClassificationGroup(partyClassificationGroupToBeUpdated);

		int usedTicketId;

		synchronized (PartyClassificationGroupController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(PartyClassificationGroupUpdated.class,
				event -> sendPartyClassificationGroupChangedMessage(((PartyClassificationGroupUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a PartyClassificationGroup from the database
	 * 
	 * @param partyClassificationGroupId:
	 *            the id of the PartyClassificationGroup thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deletepartyClassificationGroupById(@RequestParam(value = "partyClassificationGroupId") String partyClassificationGroupId) {

		DeletePartyClassificationGroup com = new DeletePartyClassificationGroup(partyClassificationGroupId);

		int usedTicketId;

		synchronized (PartyClassificationGroupController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(PartyClassificationGroupDeleted.class,
				event -> sendPartyClassificationGroupChangedMessage(((PartyClassificationGroupDeleted) event).isSuccess(), usedTicketId));

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

	public void sendPartyClassificationGroupChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/partyClassificationGroup/\" plus one of the following: "
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
