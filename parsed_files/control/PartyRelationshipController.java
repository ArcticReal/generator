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
import com.skytala.eCommerce.command.AddPartyRelationship;
import com.skytala.eCommerce.command.DeletePartyRelationship;
import com.skytala.eCommerce.command.UpdatePartyRelationship;
import com.skytala.eCommerce.entity.PartyRelationship;
import com.skytala.eCommerce.entity.PartyRelationshipMapper;
import com.skytala.eCommerce.event.PartyRelationshipAdded;
import com.skytala.eCommerce.event.PartyRelationshipDeleted;
import com.skytala.eCommerce.event.PartyRelationshipFound;
import com.skytala.eCommerce.event.PartyRelationshipUpdated;
import com.skytala.eCommerce.query.FindPartyRelationshipsBy;

@RestController
@RequestMapping("/api/partyRelationship")
public class PartyRelationshipController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<PartyRelationship>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public PartyRelationshipController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a PartyRelationship
	 * @return a List with the PartyRelationships
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<PartyRelationship> findPartyRelationshipsBy(@RequestParam Map<String, String> allRequestParams) {

		FindPartyRelationshipsBy query = new FindPartyRelationshipsBy(allRequestParams);

		int usedTicketId;

		synchronized (PartyRelationshipController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(PartyRelationshipFound.class,
				event -> sendPartyRelationshipsFoundMessage(((PartyRelationshipFound) event).getPartyRelationships(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendPartyRelationshipsFoundMessage(List<PartyRelationship> partyRelationships, int usedTicketId) {
		queryReturnVal.put(usedTicketId, partyRelationships);
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
	public boolean createPartyRelationship(HttpServletRequest request) {

		PartyRelationship partyRelationshipToBeAdded = new PartyRelationship();
		try {
			partyRelationshipToBeAdded = PartyRelationshipMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createPartyRelationship(partyRelationshipToBeAdded);

	}

	/**
	 * creates a new PartyRelationship entry in the ofbiz database
	 * 
	 * @param partyRelationshipToBeAdded
	 *            the PartyRelationship thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createPartyRelationship(PartyRelationship partyRelationshipToBeAdded) {

		AddPartyRelationship com = new AddPartyRelationship(partyRelationshipToBeAdded);
		int usedTicketId;

		synchronized (PartyRelationshipController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(PartyRelationshipAdded.class,
				event -> sendPartyRelationshipChangedMessage(((PartyRelationshipAdded) event).isSuccess(), usedTicketId));

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
	public boolean updatePartyRelationship(HttpServletRequest request) {

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

		PartyRelationship partyRelationshipToBeUpdated = new PartyRelationship();

		try {
			partyRelationshipToBeUpdated = PartyRelationshipMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updatePartyRelationship(partyRelationshipToBeUpdated);

	}

	/**
	 * Updates the PartyRelationship with the specific Id
	 * 
	 * @param partyRelationshipToBeUpdated the PartyRelationship thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updatePartyRelationship(PartyRelationship partyRelationshipToBeUpdated) {

		UpdatePartyRelationship com = new UpdatePartyRelationship(partyRelationshipToBeUpdated);

		int usedTicketId;

		synchronized (PartyRelationshipController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(PartyRelationshipUpdated.class,
				event -> sendPartyRelationshipChangedMessage(((PartyRelationshipUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a PartyRelationship from the database
	 * 
	 * @param partyRelationshipId:
	 *            the id of the PartyRelationship thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deletepartyRelationshipById(@RequestParam(value = "partyRelationshipId") String partyRelationshipId) {

		DeletePartyRelationship com = new DeletePartyRelationship(partyRelationshipId);

		int usedTicketId;

		synchronized (PartyRelationshipController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(PartyRelationshipDeleted.class,
				event -> sendPartyRelationshipChangedMessage(((PartyRelationshipDeleted) event).isSuccess(), usedTicketId));

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

	public void sendPartyRelationshipChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/partyRelationship/\" plus one of the following: "
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
