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
import com.skytala.eCommerce.command.AddPartyRelationshipType;
import com.skytala.eCommerce.command.DeletePartyRelationshipType;
import com.skytala.eCommerce.command.UpdatePartyRelationshipType;
import com.skytala.eCommerce.entity.PartyRelationshipType;
import com.skytala.eCommerce.entity.PartyRelationshipTypeMapper;
import com.skytala.eCommerce.event.PartyRelationshipTypeAdded;
import com.skytala.eCommerce.event.PartyRelationshipTypeDeleted;
import com.skytala.eCommerce.event.PartyRelationshipTypeFound;
import com.skytala.eCommerce.event.PartyRelationshipTypeUpdated;
import com.skytala.eCommerce.query.FindPartyRelationshipTypesBy;

@RestController
@RequestMapping("/api/partyRelationshipType")
public class PartyRelationshipTypeController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<PartyRelationshipType>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public PartyRelationshipTypeController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a PartyRelationshipType
	 * @return a List with the PartyRelationshipTypes
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<PartyRelationshipType> findPartyRelationshipTypesBy(@RequestParam Map<String, String> allRequestParams) {

		FindPartyRelationshipTypesBy query = new FindPartyRelationshipTypesBy(allRequestParams);

		int usedTicketId;

		synchronized (PartyRelationshipTypeController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(PartyRelationshipTypeFound.class,
				event -> sendPartyRelationshipTypesFoundMessage(((PartyRelationshipTypeFound) event).getPartyRelationshipTypes(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendPartyRelationshipTypesFoundMessage(List<PartyRelationshipType> partyRelationshipTypes, int usedTicketId) {
		queryReturnVal.put(usedTicketId, partyRelationshipTypes);
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
	public boolean createPartyRelationshipType(HttpServletRequest request) {

		PartyRelationshipType partyRelationshipTypeToBeAdded = new PartyRelationshipType();
		try {
			partyRelationshipTypeToBeAdded = PartyRelationshipTypeMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createPartyRelationshipType(partyRelationshipTypeToBeAdded);

	}

	/**
	 * creates a new PartyRelationshipType entry in the ofbiz database
	 * 
	 * @param partyRelationshipTypeToBeAdded
	 *            the PartyRelationshipType thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createPartyRelationshipType(PartyRelationshipType partyRelationshipTypeToBeAdded) {

		AddPartyRelationshipType com = new AddPartyRelationshipType(partyRelationshipTypeToBeAdded);
		int usedTicketId;

		synchronized (PartyRelationshipTypeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(PartyRelationshipTypeAdded.class,
				event -> sendPartyRelationshipTypeChangedMessage(((PartyRelationshipTypeAdded) event).isSuccess(), usedTicketId));

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
	public boolean updatePartyRelationshipType(HttpServletRequest request) {

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

		PartyRelationshipType partyRelationshipTypeToBeUpdated = new PartyRelationshipType();

		try {
			partyRelationshipTypeToBeUpdated = PartyRelationshipTypeMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updatePartyRelationshipType(partyRelationshipTypeToBeUpdated);

	}

	/**
	 * Updates the PartyRelationshipType with the specific Id
	 * 
	 * @param partyRelationshipTypeToBeUpdated the PartyRelationshipType thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updatePartyRelationshipType(PartyRelationshipType partyRelationshipTypeToBeUpdated) {

		UpdatePartyRelationshipType com = new UpdatePartyRelationshipType(partyRelationshipTypeToBeUpdated);

		int usedTicketId;

		synchronized (PartyRelationshipTypeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(PartyRelationshipTypeUpdated.class,
				event -> sendPartyRelationshipTypeChangedMessage(((PartyRelationshipTypeUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a PartyRelationshipType from the database
	 * 
	 * @param partyRelationshipTypeId:
	 *            the id of the PartyRelationshipType thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deletepartyRelationshipTypeById(@RequestParam(value = "partyRelationshipTypeId") String partyRelationshipTypeId) {

		DeletePartyRelationshipType com = new DeletePartyRelationshipType(partyRelationshipTypeId);

		int usedTicketId;

		synchronized (PartyRelationshipTypeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(PartyRelationshipTypeDeleted.class,
				event -> sendPartyRelationshipTypeChangedMessage(((PartyRelationshipTypeDeleted) event).isSuccess(), usedTicketId));

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

	public void sendPartyRelationshipTypeChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/partyRelationshipType/\" plus one of the following: "
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
